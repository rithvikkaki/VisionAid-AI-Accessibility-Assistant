$filePath = "e:\AI-Accessibility-Assistant\vision_aid.html"
$c = Get-Content $filePath
$linesToFix = @(496, 514, 532, 550, 568)
foreach ($lineNum in $linesToFix) {
    if ($c[$lineNum - 1] -notlike "*,") {
        $c[$lineNum - 1] = $c[$lineNum - 1] + ","
    }
}
$c | Set-Content $filePath
