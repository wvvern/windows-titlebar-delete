<#
.SYNOPSIS
Tests the titlebar of each target on a hidden desktop and examines the window style from outside the game.

.DESCRIPTION
The game runs with the test harness (core/src/main/java/wvv/windowstitlebardelete/core/dev/Harness.java) and a
commands file from tools/harness/. Sound and narrator are off. The game does not pause when it loses focus.

Rules for a commands file:
- A line is a command: "wait <ticks>", "toggle", "style", "fullscreen <on|off>" or "quit". A line that starts with
  "#" is not a command.
- Each "style" command is a checkpoint. The game writes the style of its window to log.txt, and at once the script
  reads the style of the same window from this process. The two values must be equal.
- Put an "# expect" line before each "style" command. It has pairs of a bit name (caption, thickframe, popup) and
  "on" or "off", or it has "style" and the full style, such as 0x16CF0000. The style must agree with it.
- Put "wait 40" after each "style" command, so that the window does not change while the script reads the style.
- With a "# config-start <text>" line, the game starts with that text as its config file. Without it, the game
  starts with no config file.
- With a "# config-end <text>" line, the config file must contain that text after the game stops.
- End the file with "quit".
- For a Forge 1.8.9 or 1.12.2 target, the script uses <name>-legacy.txt in place of <name>.txt when that file exists.
  Such a file has no "fullscreen" command, which is an error on these targets.

A run passes when each checkpoint passes, log.txt has no line that starts with "!", the game gets to "quit" and the
config file is correct. The logs and result.txt go to <OutDir>\<target>\. The exit code is 1 when a target fails.

.EXAMPLE
pwsh -Command "./tools/harness.ps1 -Targets forge-1.8.9,fabric-1.21.11,neoforge-26.3 -OutDir build\harness"
pwsh -Command "./tools/harness.ps1 -Targets forge-1.8.9,fabric-1.21.11,neoforge-26.3 -Commands remember.txt -OutDir build\harness-remember"
#>
param(
    # <loader>-<node>: forge-1.8.9 and forge-1.12.2 run in legacy/, and the other targets run in modern/.
    [string[]]$Targets = @("forge-1.8.9", "forge-1.12.2"),
    # Folder for the results, relative to the repository root.
    [string]$OutDir = "build\harness",
    [int]$TimeoutSeconds = 600,
    # The commands file in tools/harness/.
    [string]$Commands = "titlebar.txt"
)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$desktop = "WtdHarness"
$legacyVersions = @("1.8.9", "1.12.2")
$styleBits = @{ popup = 0x80000000; caption = 0x00C00000; thickframe = 0x00040000 }

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
    # The game reads this file at its start, so it makes no sound and does not start the narrator from the first-start onboarding.
    New-Item -ItemType Directory -Force $runDir | Out-Null
    @("soundCategory_master:0.0", "narrator:0", "onboardAccessibility:false", "pauseOnLostFocus:false", "tutorialStep:none",
        "skipMultiplayerWarning:true", "joinedFirstServer:true", "fullscreen:false", "exclusiveFullscreen:false") |
        Set-Content -Encoding ascii (Join-Path $runDir "options.txt")
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
            if ($words.Count % 2 -ne 0) { throw "${file}: expect needs <bit> <on|off> pairs or style <hex>: $t" }
            $pending = [ordered]@{}
            for ($i = 0; $i -lt $words.Count; $i += 2) {
                $full = $words[$i] -eq "style" -and $words[$i + 1] -match "^0x[0-9A-Fa-f]{8}$"
                if (-not $full -and (-not $styleBits.ContainsKey($words[$i]) -or $words[$i + 1] -notin @("on", "off"))) { throw "${file}: bad expect: $t" }
                $pending[$words[$i]] = $words[$i + 1]
            }
        } elseif ($t -eq "style") {
            $result += , $pending
            $pending = $null
        }
    }
    return , $result
}

# The text of the "# <name> <text>" line of a commands file, or $null.
function Read-Directive([string]$file, [string]$name) {
    foreach ($line in Get-Content $file) { if ($line.Trim() -match "^#\s*$name\s+(.+)$") { return $Matches[1] } }
    return $null
}

# The ids of all descendant processes of a process.
function Get-Descendants([int]$parent) {
    $all = Get-CimInstance Win32_Process -Property ProcessId, ParentProcessId
    $ids = [Collections.Generic.HashSet[int]]::new()
    $queue = [Collections.Generic.Queue[int]]::new()
    $queue.Enqueue($parent)
    while ($queue.Count -gt 0) {
        $p = $queue.Dequeue()
        foreach ($c in $all | Where-Object { $_.ParentProcessId -eq $p -and $_.ProcessId -ne $p }) {
            if ($ids.Add([int]$c.ProcessId)) { $queue.Enqueue([int]$c.ProcessId) }
        }
    }
    return $ids
}

# The java processes whose command line has the harness folder, which include the game.
function Get-GamePids([string]$harness) {
    return @(Get-CimInstance Win32_Process -Filter "Name = 'java.exe' OR Name = 'javaw.exe'" -Property ProcessId, CommandLine |
        Where-Object { $_.CommandLine -and $_.CommandLine.IndexOf($harness, [StringComparison]::OrdinalIgnoreCase) -ge 0 } |
        ForEach-Object { [int]$_.ProcessId })
}

# The style of the visible LWJGL, GLFW or SDL window of one of the processes, or $null when there is no such window.
function Get-OutsideStyle([int[]]$pids) {
    foreach ($w in [WtdOutside]::Windows($desktop)) {
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
        @(Get-Descendants $launcher) + $launcher | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
    }
    Get-GamePids $harness | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
}

function Format-Style($style) { if ($null -eq $style) { "none" } else { "0x{0:X8}" -f $style } }

$failed = 0
foreach ($target in $Targets) {
    $loader, $version = $target -split "-", 2
    $commandsFile = Join-Path $PSScriptRoot "harness\$Commands"
    if ($loader -eq "forge" -and $legacyVersions -contains $version) {
        $dir = Join-Path $root "legacy"; $task = ":${version}:runClientDev"; $runDir = Join-Path $dir "versions\$version\run"; $gradleOptions = ""
        $legacyFile = $commandsFile -replace "\.txt$", "-legacy.txt"
        if (Test-Path $legacyFile) { $commandsFile = $legacyFile }
    } elseif ($loader -in @("fabric", "forge", "neoforge")) {
        $dir = Join-Path $root "modern"; $task = ":${loader}:${version}:runClient"; $runDir = Join-Path $dir "$loader\versions\$version\run"
        # Gradle then configures only the projects of this target, which is faster and keeps the Loom caches of the other versions apart.
        $gradleOptions = "--configure-on-demand"
        if ($loader -ne "fabric") { Set-FmlConfig $runDir }
    } else {
        throw "unknown target: $target"
    }
    $expectations = Read-Expectations $commandsFile

    Set-Options $runDir
    $configFile = Join-Path $runDir "config\windowstitlebardelete.json"
    $configStart = Read-Directive $commandsFile "config-start"
    Remove-Item -Force $configFile -ErrorAction SilentlyContinue
    if ($configStart) {
        New-Item -ItemType Directory -Force (Split-Path -Parent $configFile) | Out-Null
        Set-Content -Encoding ascii $configFile $configStart
    }

    $harness = Join-Path $env:TEMP "wtd-harness\$target"
    Remove-Item -Recurse -Force $harness -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force $harness | Out-Null
    Copy-Item $commandsFile (Join-Path $harness "commands.txt")

    Write-Host "[$target] launching hidden with $(Split-Path -Leaf $commandsFile)"
    $gradleLog = Join-Path $harness "gradle.log"
    $launcher = & (Join-Path $PSScriptRoot "run-hidden.ps1") -Desktop $desktop -Dir $dir -Task $task -Options $gradleOptions -Harness $harness -Log $gradleLog
    $harnessLog = Join-Path $harness "log.txt"
    $outside = [Collections.Generic.List[object]]::new()
    $gamePids = @()
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        Start-Sleep -Milliseconds 250
        $alive = [bool](Get-Process -Id $launcher -ErrorAction SilentlyContinue)
        $lines = Read-SharedLines $harnessLog
        $styles = @($lines | Where-Object { $_ -match "^(style 0x|! style)" })
        # The window is stable for 40 ticks after each style line, so one sample now is the outside style of that checkpoint.
        while ($outside.Count -lt $styles.Count) {
            $seen = Get-OutsideStyle $gamePids
            if ($null -eq $seen) {
                $gamePids = Get-GamePids $harness
                $seen = Get-OutsideStyle $gamePids
            }
            Write-Host "[$target] $($styles[$outside.Count]) | outside $(Format-Style $seen)"
            $outside.Add($seen)
        }
        if (-not $alive) { break }
        if ($lines -contains "> quit") {
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
    Get-ChildItem $harness -Include log.txt, gradle.log -File -Recurse | Copy-Item -Destination $out -Force

    $lines = Read-SharedLines $harnessLog
    $styles = @($lines | Where-Object { $_ -match "^(style 0x|! style)" })
    $report = @("target $target", "commands $(Split-Path -Leaf $commandsFile)")
    if ($styles.Count -ne $expectations.Count) { $report += "FAIL: $($styles.Count) style lines, expected $($expectations.Count)" }
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
                $bits = if ($e.Key -eq "style") { -1 } else { $styleBits[$e.Key] }
                $wanted = if ($e.Value -eq "on") { $bits } elseif ($e.Value -eq "off") { 0 } else { [Convert]::ToInt32($e.Value, 16) }
                if (($seen -band $bits) -ne $wanted) { $problems += "$($e.Key) is not $($e.Value)" }
            }
        }
        $status = if ($problems) { "FAIL: " + ($problems -join "; ") } else { "PASS" }
        $report += "checkpoint $($i + 1): expect $expectText | in-game $(Format-Style $inGame) | outside $(Format-Style $seen) | $status"
    }
    foreach ($e in $lines | Where-Object { $_.StartsWith("!") }) { $report += "FAIL: harness error: $e" }
    if ($lines -notcontains "> quit") { $report += "FAIL: the game did not get to quit" }
    $configEnd = Read-Directive $commandsFile "config-end"
    if ($configEnd) {
        $saved = if (Test-Path $configFile) { Get-Content -Raw $configFile } else { "" }
        $report += "config file after the run: expect $configEnd | $(if ($saved.Contains($configEnd)) { 'PASS' } else { 'FAIL' })"
    }
    $result = if ($report -cmatch "FAIL") { $failed++; "FAIL" } else { "PASS" }
    $report + "result $result" | Set-Content -Encoding ascii (Join-Path $out "result.txt")
    Write-Host "[$target] ${result}: $($styles.Count) checkpoints, results in $out"
}
exit $(if ($failed) { 1 } else { 0 })
