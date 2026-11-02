$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
using System.Text;
public static class SanbakaWindowEmbedder {
    private delegate bool EnumWindowsProc(IntPtr hwnd, IntPtr lParam);
    [DllImport("user32.dll")] private static extern bool EnumWindows(EnumWindowsProc callback, IntPtr lParam);
    [DllImport("user32.dll", CharSet=CharSet.Unicode)] private static extern int GetWindowText(IntPtr hwnd, StringBuilder text, int count);
    [DllImport("user32.dll")] private static extern uint GetWindowThreadProcessId(IntPtr hwnd, out uint processId);
    [DllImport("user32.dll", EntryPoint="GetWindowLongPtrW", SetLastError=true)] private static extern IntPtr GetWindowLongPtr64(IntPtr hwnd, int index);
    [DllImport("user32.dll", EntryPoint="GetWindowLongW", SetLastError=true)] private static extern int GetWindowLong32(IntPtr hwnd, int index);
    [DllImport("user32.dll", EntryPoint="SetWindowLongPtrW", SetLastError=true)] private static extern IntPtr SetWindowLongPtr64(IntPtr hwnd, int index, IntPtr value);
    [DllImport("user32.dll", EntryPoint="SetWindowLongW", SetLastError=true)] private static extern int SetWindowLong32(IntPtr hwnd, int index, int value);
    [DllImport("user32.dll")] private static extern IntPtr SetParent(IntPtr child, IntPtr parent);
    [DllImport("user32.dll", SetLastError=true)] private static extern bool SetWindowPos(IntPtr hwnd, IntPtr after, int x, int y, int width, int height, uint flags);
    [DllImport("user32.dll", SetLastError=true)] private static extern IntPtr SetThreadDpiAwarenessContext(IntPtr dpiContext);
    [DllImport("user32.dll")] private static extern uint GetDpiForSystem();
    [DllImport("shell32.dll", PreserveSig=true)] private static extern int SHGetPropertyStoreForWindow(IntPtr hwnd, ref Guid iid, out IPropertyStore store);
    [ComImport, Guid("886D8EEB-8CF2-4446-8D02-CDBA1DBDCF99"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    private interface IPropertyStore {
        [PreserveSig] int GetCount(out uint count);
        [PreserveSig] int GetAt(uint index, out PropertyKey key);
        [PreserveSig] int GetValue(ref PropertyKey key, out PropVariant value);
        [PreserveSig] int SetValue(ref PropertyKey key, ref PropVariant value);
        [PreserveSig] int Commit();
    }
    [StructLayout(LayoutKind.Sequential, Pack=4)] private struct PropertyKey { public Guid FormatId; public uint PropertyId; }
    [StructLayout(LayoutKind.Sequential, Pack=4, Size=16)] private struct PropVariant {
        public ushort Type;
        public ushort Reserved1;
        public ushort Reserved2;
        public ushort Reserved3;
        public IntPtr PointerValue;
    }
    [DllImport("user32.dll")] public static extern bool DestroyIcon(IntPtr icon);
    public static IntPtr FindWindow(int pid, string expectedTitle) {
        IntPtr result = IntPtr.Zero;
        EnumWindows((hwnd, unused) => {
            uint owner;
            GetWindowThreadProcessId(hwnd, out owner);
            if (owner == (uint)pid) {
                StringBuilder title = new StringBuilder(256);
                GetWindowText(hwnd, title, title.Capacity);
                if (title.ToString() == expectedTitle) { result = hwnd; return false; }
            }
            return true;
        }, IntPtr.Zero);
        return result;
    }
    private static IntPtr GetStyle(IntPtr hwnd) {
        return IntPtr.Size == 8 ? GetWindowLongPtr64(hwnd, -16) : new IntPtr(GetWindowLong32(hwnd, -16));
    }
    private static void SetStyle(IntPtr hwnd, IntPtr style) {
        if (IntPtr.Size == 8) SetWindowLongPtr64(hwnd, -16, style);
        else SetWindowLong32(hwnd, -16, style.ToInt32());
    }
    public static bool Embed(IntPtr child, IntPtr parent, int width, int height) {
        long style = GetStyle(child).ToInt64();
        style &= ~((long)0x80000000 | 0x00C00000 | 0x00040000 | 0x00080000 | 0x00010000 | 0x00020000);
        style |= 0x40000000 | 0x10000000;
        SetStyle(child, new IntPtr(style));
        SetParent(child, parent);
        return SetWindowPos(child, IntPtr.Zero, 0, 0, width, height, 0x0004 | 0x0020 | 0x0040);
    }
    public static int SetTaskbarIdentity(IntPtr hwnd, string appId, string iconPath) {
        Guid iid = new Guid("886D8EEB-8CF2-4446-8D02-CDBA1DBDCF99");
        IPropertyStore store;
        int result = SHGetPropertyStoreForWindow(hwnd, ref iid, out store);
        if (result < 0) return result;
        Guid format = new Guid("9F4C2855-9F79-4B39-A8D0-E1D42DE1D5F3");
        try {
            PropertyKey iconKey = new PropertyKey { FormatId = format, PropertyId = 3 };
            int iconResult = SetString(store, ref iconKey, iconPath + ",0");
            if (iconResult < 0) return iconResult;
            PropertyKey appKey = new PropertyKey { FormatId = format, PropertyId = 5 };
            int appResult = SetString(store, ref appKey, appId);
            if (appResult < 0) return appResult;
            return store.Commit();
        } finally {
            Marshal.ReleaseComObject(store);
        }
    }
    public static uint EnableSystemDpiAndGetDpi() {
        SetThreadDpiAwarenessContext(new IntPtr(-2));
        return GetDpiForSystem();
    }
    private static int SetString(IPropertyStore store, ref PropertyKey key, string text) {
        PropVariant value = new PropVariant { Type = 31, PointerValue = Marshal.StringToCoTaskMemUni(text) };
        try { return store.SetValue(ref key, ref value); }
        finally { Marshal.FreeCoTaskMem(value.PointerValue); }
    }
}
'@

$form = New-Object System.Windows.Forms.Form
$doubleBufferedProperty = $form.GetType().GetProperty('DoubleBuffered', [System.Reflection.BindingFlags]::Instance -bor [System.Reflection.BindingFlags]::NonPublic)
$doubleBufferedProperty.SetValue($form, $true, $null)
$dpi = 96
try { $dpi = [int][SanbakaWindowEmbedder]::EnableSystemDpiAndGetDpi() } catch {}
$dpiScale = [Math]::Max(1.0, $dpi / 96.0)
$form.Text = [System.Text.RegularExpressions.Regex]::Unescape('Tu\u1ea7n thi c\u1ee7a SVBK')
$form.ClientSize = New-Object System.Drawing.Size([int][Math]::Round(960 * $dpiScale), [int][Math]::Round(640 * $dpiScale))
$form.FormBorderStyle = [System.Windows.Forms.FormBorderStyle]::FixedSingle
$form.MaximizeBox = $false
$form.StartPosition = [System.Windows.Forms.FormStartPosition]::CenterScreen
$form.BackColor = [System.Drawing.Color]::FromArgb(35, 38, 55)
$iconFile = Join-Path $root 'assets/avatars/Sanbaka.jpg'
if (Test-Path -LiteralPath $iconFile) {
    $stateIconBitmap = New-Object System.Drawing.Bitmap($iconFile)
    $stateIconHandle = $stateIconBitmap.GetHicon()
    $form.Icon = [System.Drawing.Icon]::FromHandle($stateIconHandle)
    $buildDirectory = Join-Path $root 'build'
    New-Item -ItemType Directory -Force -Path $buildDirectory | Out-Null
    $iconPath = Join-Path $buildDirectory 'Sanbaka.ico'
    $stateIconStream = [System.IO.File]::Open($iconPath, [System.IO.FileMode]::Create)
    try { $form.Icon.Save($stateIconStream) } finally { $stateIconStream.Dispose() }
    $null = $form.Handle
    [void][SanbakaWindowEmbedder]::SetTaskbarIdentity($form.Handle, 'Sanbaka.Game', $iconPath)
}

$state = [hashtable]::Synchronized(@{
    Progress = 0
    Status = 'PREPARING...'
    Background = $null
    Font = New-Object System.Drawing.Font('Arial', 52, [System.Drawing.FontStyle]::Bold)
    FontCollection = $null
    Stage = 'Prepare'
    CompileProcess = $null
    CompileStdout = $null
    CompileStderr = $null
    JavaProcess = $null
    JavaStdout = $null
    JavaStderr = $null
    Deadline = $null
    HandoffDeadline = $null
    Error = $null
    Dirty = $true
    LastDrawnProgress = -1
    LastDrawnStatus = ''
})

$timer = New-Object System.Windows.Forms.Timer
$timer.Interval = 40
$timer.Add_Tick({
    try {
        if ($state.Stage -eq 'Prepare') {
            foreach ($path in @('assets/background/sanbaka2.jpg', 'OOP-PROJECT-20261/assets/background/sanbaka2.jpg')) {
                if (Test-Path -LiteralPath $path) {
                    $state.Background = [System.Drawing.Image]::FromFile((Resolve-Path -LiteralPath $path).Path)
                    break
                }
            }
            foreach ($path in @('assets/fonts/1FTV-VIP-Starborn.ttf', 'OOP-PROJECT-20261/assets/fonts/1FTV-VIP-Starborn.ttf')) {
                if (Test-Path -LiteralPath $path) {
                    $state.FontCollection = New-Object System.Drawing.Text.PrivateFontCollection
                    $state.FontCollection.AddFontFile((Resolve-Path -LiteralPath $path).Path)
                    if ($state.FontCollection.Families.Length -gt 0) {
                        $state.Font = New-Object System.Drawing.Font($state.FontCollection.Families[0], 52, [System.Drawing.FontStyle]::Bold)
                    }
                    break
                }
            }

            $classDir = Join-Path $root 'build/classes'
            New-Item -ItemType Directory -Force -Path $classDir | Out-Null
            $tempFiles = Get-ChildItem -Path (Join-Path $root 'src/main/java') -Filter 'tempCodeRunnerFile*.java' -File -Recurse
            foreach ($tempFile in $tempFiles) { Remove-Item -LiteralPath $tempFile.FullName -Force }
            $sources = Get-ChildItem -Path (Join-Path $root 'src/main/java') -Filter '*.java' -File -Recurse | ForEach-Object { $_.FullName.Replace('\', '/') }
            [System.IO.File]::WriteAllLines((Join-Path $root 'build/sources.txt'), $sources, [System.Text.UTF8Encoding]::new($false))

            $state.Progress = 3
            $state.Status = 'COMPILING GAME...'
            $compile = New-Object System.Diagnostics.ProcessStartInfo
            $compile.FileName = 'javac'
            $compile.Arguments = '-encoding UTF-8 -d build/classes @build/sources.txt'
            $compile.WorkingDirectory = $root
            $compile.UseShellExecute = $false
            $compile.CreateNoWindow = $true
            $compile.RedirectStandardOutput = $true
            $compile.RedirectStandardError = $true
            $state.CompileProcess = [System.Diagnostics.Process]::Start($compile)
            $state.CompileStdout = $state.CompileProcess.StandardOutput.ReadToEndAsync()
            $state.CompileStderr = $state.CompileProcess.StandardError.ReadToEndAsync()
            $state.Stage = 'Compiling'
            $state.Dirty = $true
        } elseif ($state.Stage -eq 'Compiling') {
            if ($state.Progress -lt 23) {
                $oldProgress = [int]$state.Progress
                $state.Progress = [Math]::Min(23, $state.Progress + 0.12)
                if ([int]$state.Progress -ne $oldProgress) { $state.Dirty = $true }
            }
            $state.CompileProcess.Refresh()
            if ($state.CompileProcess.HasExited) {
                $compileOutput = $state.CompileStdout.Result + $state.CompileStderr.Result
                if ($state.CompileProcess.ExitCode -ne 0) { throw $compileOutput }

                $progressPath = Join-Path $root 'build/loading-progress.txt'
                [System.IO.File]::WriteAllText($progressPath, '25', [System.Text.Encoding]::ASCII)
                $state.Progress = 25
                $state.Status = 'LOADING GAME RESOURCES...'
                $state.Dirty = $true
                $java = New-Object System.Diagnostics.ProcessStartInfo
                $java.FileName = 'java'
                $java.Arguments = '--enable-native-access=ALL-UNNAMED -Dsun.awt.noerasebackground=true -Dsun.java2d.d3d=false -Dsun.java2d.transaccel=true -Dsun.java2d.ddforcevram=true -Dsun.java2d.noddraw=false -Dsun.java2d.opengl=true -Dsanbaka.perfLog=false -cp "build/classes;assets;." sanbaka.SanbakaGame --external-loader "build/loading-progress.txt"'
                $java.WorkingDirectory = $root
                $java.UseShellExecute = $false
                $java.CreateNoWindow = $true
                $java.RedirectStandardOutput = $true
                $java.RedirectStandardError = $true
                $state.JavaProcess = [System.Diagnostics.Process]::Start($java)
                $state.JavaStdout = $state.JavaProcess.StandardOutput.ReadToEndAsync()
                $state.JavaStderr = $state.JavaProcess.StandardError.ReadToEndAsync()
                $state.Deadline = [DateTime]::UtcNow.AddMinutes(5)
                $state.Stage = 'LoadingResources'
            }
        } elseif ($state.Stage -eq 'LoadingResources') {
            $state.JavaProcess.Refresh()
            if ($state.JavaProcess.HasExited) {
                $javaOutput = $state.JavaStdout.Result + $state.JavaStderr.Result
                throw "The game process exited before finishing resource loading. $javaOutput"
            }
            $progressPath = Join-Path $root 'build/loading-progress.txt'
            if (Test-Path -LiteralPath $progressPath) {
                $reportedProgress = 0
                if ([int]::TryParse((Get-Content -LiteralPath $progressPath -Raw), [ref]$reportedProgress)) {
                    if ($reportedProgress -lt 0) { throw 'Java reported a resource-loading error; see the run.cmd window.' }
                    if ($reportedProgress -gt [int]$state.Progress) {
                        $state.Progress = [Math]::Min(100, $reportedProgress)
                        $state.Dirty = $true
                    }
                }
            }
            if ($state.Progress -ge 100) {
                $windowPath = Join-Path $root 'build/loading-window.txt'
                $embeddedPath = Join-Path $root 'build/loading-embedded.txt'
                if (Test-Path -LiteralPath $windowPath) {
                    $javaPid = 0
                    if ([int]::TryParse((Get-Content -LiteralPath $windowPath -Raw), [ref]$javaPid)) {
                        $gameWindow = [SanbakaWindowEmbedder]::FindWindow($javaPid, 'SANBAKA_GAME_CONTENT')
                        if ($gameWindow -ne [IntPtr]::Zero) {
                            if (-not [SanbakaWindowEmbedder]::Embed($gameWindow, $form.Handle, $form.ClientSize.Width, $form.ClientSize.Height)) {
                                throw 'Unable to attach the game window to the loading window.'
                            }
                            [System.IO.File]::WriteAllText($embeddedPath, 'ready', [System.Text.Encoding]::ASCII)
                            $state.Stage = 'GameRunning'
                        }
                    }
                }
            } elseif ([DateTime]::UtcNow -gt $state.Deadline) {
                throw 'The game did not finish loading resources within five minutes.'
            }
        } elseif ($state.Stage -eq 'GameRunning') {
            $state.JavaProcess.Refresh()
            if ($state.JavaProcess.HasExited) {
                $timer.Stop()
                $form.Close()
            }
        }
    } catch {
        $state.Error = $_.Exception.Message
        $state.Status = 'STARTUP FAILED'
        $state.Stage = 'Failed'
        $state.Dirty = $true
        $timer.Stop()
        [System.Windows.Forms.MessageBox]::Show($state.Error, 'Game startup failed') | Out-Null
    }

    $shownProgress = [int]$state.Progress
    if ($state.Dirty -or $shownProgress -ne $state.LastDrawnProgress -or $state.Status -ne $state.LastDrawnStatus) {
        $state.Dirty = $false
        $state.LastDrawnProgress = $shownProgress
        $state.LastDrawnStatus = $state.Status
        $form.Invalidate()
    }
})

$form.Add_Paint({
    param($sender, $eventArgs)
    $g = $eventArgs.Graphics
    $g.ScaleTransform([single]$dpiScale, [single]$dpiScale)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    if ($state.Background) {
        $image = $state.Background
        $scale = [Math]::Max(960.0 / $image.Width, 640.0 / $image.Height)
        $drawW = [int][Math]::Ceiling($image.Width * $scale)
        $drawH = [int][Math]::Ceiling($image.Height * $scale)
        $g.DrawImage($image, [int]((960 - $drawW) / 2), [int]((640 - $drawH) / 2), $drawW, $drawH)
    } else {
        $g.Clear([System.Drawing.Color]::FromArgb(55, 47, 68))
    }
    $shade = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(150, 12, 17, 30))
    $g.FillRectangle($shade, 0, 0, 960, 640)
    $shade.Dispose()

    $title = [System.Text.RegularExpressions.Regex]::Unescape('TU\u1ea6N THI C\u1ee6A SVBK')
    $paintTitleFont = New-Object System.Drawing.Font(
        $state.Font.FontFamily,
        [single]($state.Font.Size / $dpiScale),
        $state.Font.Style,
        $state.Font.Unit)
    $titleSize = $g.MeasureString($title, $paintTitleFont)
    $shadow = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(150, 0, 0, 0))
    $white = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 250, 246, 236))
    $g.DrawString($title, $paintTitleFont, $shadow, [single]((960 - $titleSize.Width) / 2 + 13), 205)
    $g.DrawString($title, $paintTitleFont, $white, [single]((960 - $titleSize.Width) / 2 + 10), 200)
    $paintTitleFont.Dispose()
    $shadow.Dispose(); $white.Dispose()

    $barX = 220; $barY = 374; $barW = 520; $barH = 25
    $track = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(220, 18, 25, 37))
    $g.FillRectangle($track, $barX + 12, $barY, $barW - 24, $barH)
    $g.FillEllipse($track, $barX, $barY, $barH, $barH)
    $g.FillEllipse($track, $barX + $barW - $barH, $barY, $barH, $barH)
    $track.Dispose()
    $outline = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(225, 255, 255, 255), 1)
    $g.DrawLine($outline, $barX + 12, $barY, $barX + $barW - 12, $barY)
    $g.DrawLine($outline, $barX + 12, $barY + $barH, $barX + $barW - 12, $barY + $barH)
    $g.DrawArc($outline, $barX, $barY, $barH, $barH, 90, 180)
    $g.DrawArc($outline, $barX + $barW - $barH, $barY, $barH, $barH, 270, 180)
    $outline.Dispose()
    $fillW = [int](($barW - 6) * [Math]::Min(100, $state.Progress) / 100)
    if ($fillW -gt 0) {
        $fill = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(245, 250, 250, 250))
        $fillH = $barH - 6
        if ($fillW -gt $fillH) { $g.FillRectangle($fill, $barX + 3 + $fillH / 2, $barY + 3, $fillW - $fillH, $fillH) }
        $g.FillEllipse($fill, $barX + 3, $barY + 3, [Math]::Min($fillH, $fillW), $fillH)
        if ($fillW -gt $fillH) { $g.FillEllipse($fill, $barX + 3 + $fillW - $fillH, $barY + 3, $fillH, $fillH) }
        $fill.Dispose()
    }
    $label = if ($state.Error) { 'STARTUP FAILED' } else { $state.Status + '  ' + [int]$state.Progress + '%' }
    $labelFont = New-Object System.Drawing.Font('Arial', [single](12 / $dpiScale), [System.Drawing.FontStyle]::Bold)
    $labelSize = $g.MeasureString($label, $labelFont)
    $labelBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $g.DrawString($label, $labelFont, $labelBrush, [single]((960 - $labelSize.Width) / 2), 417)
    if ($state.Error) { $g.DrawString($state.Error, $labelFont, $labelBrush, 30, 450) }
    $labelBrush.Dispose(); $labelFont.Dispose()
})

$form.Add_FormClosing({
    if ($state.CompileProcess -and -not $state.CompileProcess.HasExited) { $state.CompileProcess.Kill() }
    if ($state.JavaProcess -and -not $state.JavaProcess.HasExited) { $state.JavaProcess.Kill() }
    if ($stateIconHandle -ne [IntPtr]::Zero) { [SanbakaWindowEmbedder]::DestroyIcon($stateIconHandle) | Out-Null }
    if ($stateIconBitmap) { $stateIconBitmap.Dispose() }
})

$timer.Start()
[void]$form.ShowDialog()
if ($state.JavaProcess) { $state.JavaProcess.WaitForExit() }
