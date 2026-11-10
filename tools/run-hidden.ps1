<#
.SYNOPSIS
Starts a Gradle run task on a hidden Windows desktop, where the game window does not show, take focus or show in the
taskbar, and prints the process id of the cmd.exe that runs it.

.EXAMPLE
./tools/run-hidden.ps1 -Desktop WtdHarness -Dir legacy -Task ":1.8.9:runClientDev" -Harness C:\temp\harness -Log C:\temp\run.log
#>
param(
    # The name of the hidden desktop.
    [Parameter(Mandatory = $true)][string]$Desktop,
    [Parameter(Mandatory = $true)][string]$Dir,
    [Parameter(Mandatory = $true)][string]$Task,
    # More options for Gradle, for example --configure-on-demand.
    [string]$Options,
    [Parameter(Mandatory = $true)][string]$Harness,
    [Parameter(Mandatory = $true)][string]$Log
)

Add-Type @"
using System;
using System.Runtime.InteropServices;
public static class HiddenDesktop {
    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
    public struct STARTUPINFO {
        public int cb; public string lpReserved; public string lpDesktop; public string lpTitle;
        public int dwX, dwY, dwXSize, dwYSize, dwXCountChars, dwYCountChars, dwFillAttribute, dwFlags;
        public short wShowWindow, cbReserved2; public IntPtr lpReserved2, hStdInput, hStdOutput, hStdError;
    }
    [StructLayout(LayoutKind.Sequential)]
    public struct PROCESS_INFORMATION { public IntPtr hProcess, hThread; public int dwProcessId, dwThreadId; }
    [DllImport("user32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
    public static extern IntPtr CreateDesktop(string name, IntPtr device, IntPtr mode, int flags, uint access, IntPtr attributes);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
    public static extern bool CreateProcess(string app, string commandLine, IntPtr pa, IntPtr ta, bool inherit,
        uint flags, IntPtr env, string cwd, ref STARTUPINFO si, out PROCESS_INFORMATION pi);
}
"@

# The access is GENERIC_ALL, and CreateDesktop returns the desktop if it exists.
if ([HiddenDesktop]::CreateDesktop($Desktop, [IntPtr]::Zero, [IntPtr]::Zero, 0, 0x10000000, [IntPtr]::Zero) -eq [IntPtr]::Zero) {
    throw "CreateDesktop failed: $([Runtime.InteropServices.Marshal]::GetLastWin32Error())"
}

$Dir = (Resolve-Path $Dir).Path
$gradlew = Join-Path $Dir "gradlew.bat"
# --no-daemon: a daemon from the visible desktop starts the game window on that desktop.
$commandLine = "cmd.exe /s /c `"`"$gradlew`" --no-daemon $Options $Task `"-Pharness=$Harness`" > `"$Log`" 2>&1`""

$si = New-Object HiddenDesktop+STARTUPINFO
$si.cb = [Runtime.InteropServices.Marshal]::SizeOf($si)
$si.lpDesktop = $Desktop
$pi = New-Object HiddenDesktop+PROCESS_INFORMATION
# CREATE_NO_WINDOW: no console window on the hidden desktop.
if (-not [HiddenDesktop]::CreateProcess([NullString]::Value, $commandLine, [IntPtr]::Zero, [IntPtr]::Zero, $false, 0x08000000,
        [IntPtr]::Zero, $Dir, [ref]$si, [ref]$pi)) {
    throw "CreateProcess failed: $([Runtime.InteropServices.Marshal]::GetLastWin32Error())"
}
$pi.dwProcessId
