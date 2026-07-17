$lines = Get-Content "d:\Projects\MockProject_062026_Nhom3\API_Document_admin.csv"
for($i=18; $i -lt 50; $i++) {
    if ($i -lt $lines.Count) {
        Write-Host "LINE $i`: $($lines[$i])"
    }
}
