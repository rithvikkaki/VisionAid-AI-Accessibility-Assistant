$filePath = "e:\AI-Accessibility-Assistant\vision_aid.html"
$c = Get-Content $filePath
$range1 = $c[0..573]
$range2 = $c[597..($c.Length-1)]
$range1 + $range2 | Set-Content $filePath
