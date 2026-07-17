$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
$wb = $excel.Workbooks.Open("d:\Projects\MockProject_062026_Nhom3\API_Document.xlsx")
foreach($ws in $wb.Worksheets) {
    $name = $ws.Name
    Write-Host "Sheet: $name"
    $outputPath = "d:\Projects\MockProject_062026_Nhom3\API_Document_$name.csv"
    $ws.SaveAs($outputPath, 6)
}
$wb.Close($false)
$excel.Quit()
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($excel) | Out-Null
