<#
.SYNOPSIS
Tests the titlebar of each target on the hidden desktop WtdHarness and examines the window style from outside the game.

.DESCRIPTION
The game runs with the test harness (core/src/main/java/wvv/windowstitlebardelete/core/dev/Harness.java) and the
commands file in tools/harness/. Sound and narrator are off. The game does not pause when it loses focus.
For a Forge 1.8.9 or 1.12.2 target, the script uses <name>-legacy.txt in place of <name>.txt when that file exists.
Each "style" line that the game writes to log.txt is a checkpoint. At once, the script reads the style of the game
window with GetWindowLong from this process. The in-game style and the outside style must be equal, and the bits must
agree with the "# expect" line before the "style" command.
The log, the screenshots and result.txt go to <OutDir>\<target>\. The exit code is 1 when a target fails.

.EXAMPLE
./tools/harness.ps1                                   # every target
./tools/harness.ps1 -Targets forge-1.8.9              # one target
./tools/harness.ps1 -Targets fabric-1.21.11 -OutDir build\harness-fabric
#>
param(
    # <loader>-<mc>: forge-1.8.9 and forge-1.12.2 run in legacy/; fabric-<mc>, forge-<mc> and neoforge-<mc> run in modern/.
    [string[]]$Targets = @("forge-1.8.9", "forge-1.12.2"),
    # Folder for the results, relative to the repository root.
    [string]$OutDir = "build\harness",
    [int]$TimeoutSeconds = 600,
    # The commands file in tools/harness/.
    [string]$Commands = "titlebar.txt"
)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$legacyVersions = @("1.8.9", "1.12.2")
$styleBits = @{ caption = 0x00C00000; sysmenu = 0x00080000; thickframe = 0x00040000; minimizebox = 0x00020000; maximizebox = 0x00010000 }

Add-Type @"
using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Text;
public static class WtdOutside {
    private delegate bool EnumProc(IntPtr hwnd, IntPtr data);
    [DllImport("user32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
    private static extern IntPtr OpenDesktop(string name, int flags, bool inherit, uint access);
    [DllImport("user32.dll")]
    private static extern bool CloseDesktop(IntPtr desktop);
    [DllImport("user32.dll")]
    private static extern bool EnumDesktopWindows(IntPtr desktop, EnumProc proc, IntPtr data);
    [DllImport("user32.dll")]
    private static extern int GetWindowThreadProcessId(IntPtr hwnd, out int pid);
    [DllImport("user32.dll")]
    private static extern bool IsWindowVisible(IntPtr hwnd);
    [DllImport("user32.dll", CharSet = CharSet.Unicode)]
    private static extern int GetClassName(IntPtr hwnd, StringBuilder name, int length);
    [DllImport("user32.dll", EntryPoint = "GetWindowLongW")]
    private static extern int GetWindowLong(IntPtr hwnd, int index);

    // The visible top-level windows of a desktop as "pid|class|style", where style is GWL_STYLE in hexadecimal.
    public static string[] Windows(string desktopName) {
        // DESKTOP_READOBJECTS | DESKTOP_ENUMERATE.
        IntPtr desktop = OpenDesktop(desktopName, 0, false, 0x0041);
        if (desktop == IntPtr.Zero) return new string[0];
        List<string> found = new List<string>();
        try {
            EnumProc proc = delegate(IntPtr hwnd, IntPtr data) {
                if (!IsWindowVisible(hwnd)) return true;
                int pid;
                GetWindowThreadProcessId(hwnd, out pid);
                StringBuilder name = new StringBuilder(256);
                GetClassName(hwnd, name, name.Capacity);
                found.Add(pid + "|" + name + "|" + GetWindowLong(hwnd, -16).ToString("X8"));
                return true;
            };
            EnumDesktopWindows(desktop, proc, IntPtr.Zero);
            GC.KeepAlive(proc);
        } finally {
            CloseDesktop(desktop);
        }
        return found.ToArray();
    }
}
"@

function Set-Options([string]$runDir) {
    # Turn off sound and the first-start onboarding (it can start the narrator) before the game reads the options.
    New-Item -ItemType Directory -Force $runDir | Out-Null
    $file = Join-Path $runDir "options.txt"
    $options = [ordered]@{}
    if (Test-Path $file) {
        foreach ($line in Get-Content $file) { $i = $line.IndexOf(':'); if ($i -gt 0) { $options[$line.Substring(0, $i)] = $line.Substring($i + 1) } }
    }
    $wanted = @{ onboardAccessibility = "false"; narrator = "0"; soundCategory_master = "0.0"; pauseOnLostFocus = "false";
                 tutorialStep = "none"; skipMultiplayerWarning = "true"; joinedFirstServer = "true"; fullscreen = "false"; guiScale = "0" }
    foreach ($k in $wanted.Keys) { $options[$k] = $wanted[$k] }
    ($options.GetEnumerator() | ForEach-Object { "$($_.Key):$($_.Value)" }) | Set-Content -Encoding ascii $file
    if (-not (Select-String -Path $file -Pattern "^soundCategory_master:0\.0$" -Quiet)) { throw "options.txt in $runDir does not set the sound to 0" }
}

function Set-FmlConfig([string]$runDir) {
    # Forge and NeoForge read earlyWindowControl from config/fml.toml; false makes the game create its own window, as vanilla does.
    $config = Join-Path $runDir "config"
    New-Item -ItemType Directory -Force $config | Out-Null
    $file = Join-Path $config "fml.toml"
    $lines = @(if (Test-Path $file) { Get-Content $file | Where-Object { $_ -notmatch "^\s*earlyWindowControl\s*=" } })
    @($lines) + "earlyWindowControl = false" | Set-Content -Encoding ascii $file
}

# The expectation of each "style" command, in order, from the "# expect" line before it.
function Read-Expectations([string]$file) {
    $result = @()
    $pending = $null
    foreach ($line in Get-Content $file) {
        $t = $line.Trim()
        if ($t -match "^#\s*expect\s+(.+)$") {
            $words = $Matches[1] -split "\s+"
            if ($words.Count % 2 -ne 0) { throw "${file}: expect needs <bit> <on|off> pairs: $t" }
            $pending = [ordered]@{}
            for ($i = 0; $i -lt $words.Count; $i += 2) {
                if (-not $styleBits.ContainsKey($words[$i]) -or $words[$i + 1] -notin @("on", "off")) { throw "${file}: bad expect: $t" }
                $pending[$words[$i]] = $words[$i + 1]
            }
        } elseif ($t -eq "style") {
            $result += , $pending
            $pending = $null
        }
    }
    return , $result
}

# The process ids of all descendants of a process.
function Get-Descendants([int]$parent) {
    $all = Get-CimInstance Win32_Process -Property ProcessId, ParentProcessId, Name, CommandLine
    $ids = [Collections.Generic.HashSet[int]]::new()
    $queue = [Collections.Generic.Queue[int]]::new()
    $queue.Enqueue($parent)
    while ($queue.Count -gt 0) {
        $p = $queue.Dequeue()
        foreach ($c in $all | Where-Object { $_.ParentProcessId -eq $p -and $_.ProcessId -ne $p }) {
            if ($ids.Add([int]$c.ProcessId)) { $queue.Enqueue([int]$c.ProcessId) }
        }
    }
    return $all | Where-Object { $ids.Contains([int]$_.ProcessId) }
}

# The java processes under the launcher whose command line has the harness folder, which include the game.
function Get-GamePids([int]$launcher, [string]$harness) {
    return @(Get-Descendants $launcher | Where-Object {
        $_.Name -match "^javaw?\.exe$" -and $_.CommandLine -and $_.CommandLine.IndexOf($harness, [StringComparison]::OrdinalIgnoreCase) -ge 0
    } | ForEach-Object { [int]$_.ProcessId })
}

# The style of the visible LWJGL, GLFW or SDL window of one of the processes, or $null when there is no such window.
function Get-OutsideStyle([int[]]$pids) {
    foreach ($w in [WtdOutside]::Windows("WtdHarness")) {
        $p, $class, $style = $w -split "\|"
        if ($pids -contains [int]$p -and ($class -eq "LWJGL" -or $class -eq "GLFW30" -or $class.StartsWith("SDL"))) {
            return [Convert]::ToInt32($style, 16)
        }
    }
    return $null
}

function Read-SharedLines([string]$file) {
    if (-not (Test-Path $file)) { return @() }
    $stream = [IO.File]::Open($file, [IO.FileMode]::Open, [IO.FileAccess]::Read, [IO.FileShare]::ReadWrite -bor [IO.FileShare]::Delete)
    try { $text = [IO.StreamReader]::new($stream).ReadToEnd() } finally { $stream.Dispose() }
    return @($text -split "\r?\n" | Where-Object { $_ -ne "" })
}

function Stop-Run([int]$launcher, [string]$harness) {
    # Only a live launcher, because Windows can give the id of an ended process to a new one.
    if (Get-Process -Id $launcher -ErrorAction SilentlyContinue) {
        Get-Descendants $launcher | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }
        Stop-Process -Id $launcher -Force -ErrorAction SilentlyContinue
    }
    Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object { $_.CommandLine -match [regex]::Escape($harness) } |
        ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }
}

function Format-Style($style) { if ($null -eq $style) { "none" } else { "0x{0:X8}" -f $style } }

$failed = 0
foreach ($target in $Targets) {
    $loader, $version = $target -split "-", 2
    $commandsFile = Join-Path $PSScriptRoot "harness\$Commands"
    if ($loader -eq "forge" -and $legacyVersions -contains $version) {
        $dir = Join-Path $root "legacy"; $task = ":${version}:runClientDev"; $runDir = Join-Path $dir "versions\$version\run"
        $legacyFile = $commandsFile -replace "\.txt$", "-legacy.txt"
        if (Test-Path $legacyFile) { $commandsFile = $legacyFile }
    } elseif ($loader -in @("fabric", "forge", "neoforge")) {
        $dir = Join-Path $root "modern"; $task = ":${loader}:${version}:runClient"; $runDir = Join-Path $dir "$loader\versions\$version\run"
    } else {
        throw "unknown target: $target"
    }
    $expectations = Read-Expectations $commandsFile

    Set-Options $runDir
    if ($loader -in @("forge", "neoforge") -and $dir -like "*modern") { Set-FmlConfig $runDir }
    # Each run starts with the default config.
    Remove-Item -Force (Join-Path $runDir "config\windowstitlebardelete.json") -ErrorAction SilentlyContinue

    $harness = Join-Path $env:TEMP "wtd-harness\$target"
    Remove-Item -Recurse -Force $harness -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force $harness | Out-Null
    $commandsCopy = Join-Path $harness "commands.txt"
    Copy-Item $commandsFile $commandsCopy
    # The harness takes the file only when it is older than half a second.
    (Get-Item $commandsCopy).LastWriteTime = (Get-Date).AddSeconds(-5)

    Write-Host "[$target] launching hidden with $(Split-Path -Leaf $commandsFile)"
    $gradleLog = Join-Path $harness "gradle.log"
    $launcher = & (Join-Path $PSScriptRoot "run-hidden.ps1") -Dir $dir -Task $task -Harness $harness -Log $gradleLog
    $harnessLog = Join-Path $harness "log.txt"
    $outside = [Collections.Generic.List[object]]::new()
    $checkpoints = 0
    $quit = $false
    $gamePids = @()
    $nextSearch = Get-Date
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        Start-Sleep -Milliseconds 250
        $alive = [bool](Get-Process -Id $launcher -ErrorAction SilentlyContinue)
        # The process search is slow, so it runs only until the game window is found.
        if ($alive -and (Get-Date) -ge $nextSearch -and $null -eq (Get-OutsideStyle $gamePids)) {
            $gamePids = Get-GamePids $launcher $harness
            $nextSearch = (Get-Date).AddSeconds(3)
        }
        $lines = Read-SharedLines $harnessLog
        $styles = @($lines | Where-Object { $_ -match "^(style 0x|! style)" })
        # The window is stable for 40 ticks after each style line, so one sample now is the outside style of that checkpoint.
        while ($checkpoints -lt $styles.Count) {
            $seen = Get-OutsideStyle $gamePids
            if ($null -eq $seen) {
                $gamePids = Get-GamePids $launcher $harness
                $seen = Get-OutsideStyle $gamePids
            }
            $outside.Add($seen)
            Write-Host "[$target] $($styles[$checkpoints]) | outside $(Format-Style $outside[$checkpoints])"
            $checkpoints++
        }
        if (-not $alive) { break }
        if ($lines -contains "> quit") {
            $quit = $true
            $quitBy = (Get-Date).AddSeconds(30)
            while ((Get-Date) -lt $quitBy -and (Get-Process -Id $launcher -ErrorAction SilentlyContinue)) { Start-Sleep -Seconds 1 }
            break
        }
    }
    if (Get-Process -Id $launcher -ErrorAction SilentlyContinue) {
        Write-Host "[$target] still running; stopping it"
    }
    Stop-Run $launcher $harness

    $out = Join-Path $root "$OutDir\$target"
    Remove-Item -Recurse -Force $out -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force $out | Out-Null
    Get-ChildItem $harness -Include *.png, log.txt, gradle.log -File -Recurse | Copy-Item -Destination $out -Force

    $lines = Read-SharedLines $harnessLog
    $styles = @($lines | Where-Object { $_ -match "^(style 0x|! style)" })
    $report = @("target $target", "commands $(Split-Path -Leaf $commandsFile)")
    $pass = $true
    if ($styles.Count -ne $expectations.Count) {
        $report += "FAIL: $($styles.Count) style lines, expected $($expectations.Count)"
        $pass = $false
    }
    for ($i = 0; $i -lt $styles.Count; $i++) {
        $problems = @()
        $inGame = if ($styles[$i] -match "^style 0x([0-9A-F]{8})") { [Convert]::ToInt32($Matches[1], 16) } else { $null }
        $seen = if ($i -lt $outside.Count) { $outside[$i] } else { $null }
        if ($null -eq $inGame) { $problems += "no in-game style" }
        if ($null -eq $seen) { $problems += "no outside style" }
        if ($null -ne $inGame -and $null -ne $seen -and $inGame -ne $seen) { $problems += "in-game and outside differ" }
        $expect = if ($i -lt $expectations.Count) { $expectations[$i] } else { $null }
        $expectText = if ($expect) { ($expect.GetEnumerator() | ForEach-Object { "$($_.Key) $($_.Value)" }) -join ", " } else { "nothing" }
        if ($expect -and $null -ne $seen) {
            foreach ($e in $expect.GetEnumerator()) {
                $bits = $styleBits[$e.Key]
                $value = $seen -band $bits
                if (($e.Value -eq "on" -and $value -ne $bits) -or ($e.Value -eq "off" -and $value -ne 0)) { $problems += "$($e.Key) is not $($e.Value)" }
            }
        }
        $status = if ($problems) { "FAIL: " + ($problems -join "; ") } else { "PASS" }
        if ($problems) { $pass = $false }
        $report += "checkpoint $($i + 1): expect $expectText | in-game $(Format-Style $inGame) | outside $(Format-Style $seen) | $status"
    }
    $errors = @($lines | Where-Object { $_.StartsWith("!") })
    foreach ($e in $errors) { $report += "FAIL: harness error: $e" }
    if ($errors) { $pass = $false }
    if (-not $quit) { $report += "FAIL: the game did not get to quit"; $pass = $false }
    $report += "result $(if ($pass) { 'PASS' } else { 'FAIL' })"
    $report | Set-Content -Encoding ascii (Join-Path $out "result.txt")
    if (-not $pass) { $failed++ }
    Write-Host "[$target] $(if ($pass) { 'PASS' } else { 'FAIL' }): $(@($styles).Count) checkpoints, results in $out"
}
exit $(if ($failed) { 1 } else { 0 })
