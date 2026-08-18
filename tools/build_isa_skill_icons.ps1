param(
    [string]$StarsectorRoot =
        "C:\Program Files (x86)\Fractal Softworks\Starsector",
    [string]$OutputDirectory = "graphics/icons/skills"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

function Convert-HslToColor(
        [double]$Hue,
        [double]$Saturation,
        [double]$Lightness,
        [int]$Alpha) {
    $Hue = (($Hue % 360.0) + 360.0) % 360.0
    $Saturation = [Math]::Max(0.0, [Math]::Min(1.0, $Saturation))
    $Lightness = [Math]::Max(0.0, [Math]::Min(1.0, $Lightness))

    $chroma = (1.0 - [Math]::Abs(2.0 * $Lightness - 1.0)) * $Saturation
    $section = $Hue / 60.0
    $secondary = $chroma * (1.0 - [Math]::Abs(($section % 2.0) - 1.0))
    $red = 0.0
    $green = 0.0
    $blue = 0.0
    switch ([Math]::Floor($section)) {
        0 { $red = $chroma; $green = $secondary; break }
        1 { $red = $secondary; $green = $chroma; break }
        2 { $green = $chroma; $blue = $secondary; break }
        3 { $green = $secondary; $blue = $chroma; break }
        4 { $red = $secondary; $blue = $chroma; break }
        default { $red = $chroma; $blue = $secondary; break }
    }

    $match = $Lightness - ($chroma / 2.0)
    return [System.Drawing.Color]::FromArgb(
        $Alpha,
        [int][Math]::Round(($red + $match) * 255.0),
        [int][Math]::Round(($green + $match) * 255.0),
        [int][Math]::Round(($blue + $match) * 255.0))
}

function Write-RecoloredIcon(
        [string]$SourcePath,
        [string]$OutputPath) {
    $source = New-Object System.Drawing.Bitmap($SourcePath)
    $result = New-Object System.Drawing.Bitmap(
        $source.Width,
        $source.Height,
        [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        for ($y = 0; $y -lt $source.Height; $y++) {
            for ($x = 0; $x -lt $source.Width; $x++) {
                $pixel = $source.GetPixel($x, $y)
                $saturation = [double]$pixel.GetSaturation()
                $lightness = [double]$pixel.GetBrightness()
                if ($pixel.A -eq 0 -or $saturation -lt 0.10) {
                    $result.SetPixel($x, $y, $pixel)
                    continue
                }

                $sourceHue = [double]$pixel.GetHue()
                $newSaturation = [Math]::Min(1.0, [Math]::Max(0.24,
                    $saturation * 1.08))
                $newLightness = [Math]::Min(1.0, $lightness * 0.98)

                $newHue = if ($lightness -gt 0.63 -and
                        $sourceHue -ge 28.0 -and $sourceHue -le 85.0) {
                    45.0
                } else {
                    188.0
                }

                $newColor = Convert-HslToColor `
                    -Hue $newHue `
                    -Saturation $newSaturation `
                    -Lightness $newLightness `
                    -Alpha $pixel.A
                $result.SetPixel($x, $y, $newColor)
            }
        }

        $directory = Split-Path -Parent $OutputPath
        New-Item -ItemType Directory -Force -Path $directory | Out-Null
        $result.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $result.Dispose()
        $source.Dispose()
    }
}

$vanillaSkills = Join-Path $StarsectorRoot "starsector-core/graphics/icons/skills"
$resolvedOutput = [System.IO.Path]::GetFullPath(
    (Join-Path $PSScriptRoot "../$OutputDirectory"))

Write-RecoloredIcon `
    (Join-Path $vanillaSkills "damage_control2.png") `
    (Join-Path $resolvedOutput "ship_trophy_isa_hull_repair.png")

Write-Host "Wrote Isa skill icons to $resolvedOutput"
