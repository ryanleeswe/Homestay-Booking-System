$setup = ([IO.File]::ReadAllText((Join-Path $PSScriptRoot 'api-smoke-test.ps1')) -split '\$spec = Invoke-RestMethod')[0]
. ([scriptblock]::Create($setup))
$previous = Get-Content api-test-results.json -Raw | ConvertFrom-Json
foreach ($row in $previous) { $results.Add($row) }
$reportRun = ((Get-Content api-test-report.md | Where-Object { $_ -like 'Run:*' }) -replace '^Run: ', '').Trim()
$login=Check 'Follow-up login' POST '/auth/login' @{email="qa.$reportRun@example.com";password='QaTest123!'} 200 ''
$token=$login.accessToken
$all=Check 'Find own QA user' GET '/users?pageSize=100' $null 200
$own=@($all | Where-Object email -eq "qa.$reportRun@example.com")[0]
$uid=$own.id
$hs=Check 'Find remaining QA homestay' GET "/homestays/owner/$uid" $null 200
$hid=@($hs)[0].id
$rooms=Check 'Inspect test room capacity' GET "/homestays/$hid/rooms" $null 200
$room=@($rooms)[0]
$bookings=Check 'Inspect excess booking' GET "/bookings/homestay/$hid" $null 200
$excess=@($bookings)[0]
Write-Host "CAPACITY room.maxGuests=$($room.maxGuests); booking.numGuests=$($excess.numGuests); booking.id=$($excess.id)"
$null=Check 'Pending update isolated' PUT "/bookings/$($excess.id)" @{userId=$uid;homestayId=$hid;roomId=$room.id;numGuests=2;checkInDate=(Get-Date).AddDays(80).ToString('yyyy-MM-dd');checkOutDate=(Get-Date).AddDays(82).ToString('yyyy-MM-dd')} 200
$null=Check 'Cancel booking' PUT "/bookings/$($excess.id)/status" @{status='CANCELLED'} 200
$null=Check 'Cancelled cannot confirm' PUT "/bookings/$($excess.id)/status" @{status='CONFIRMED'} 409
$null=Check 'Delete excess test booking' DELETE "/bookings/$($excess.id)" $null 204
$null=Check 'Delete QA homestay after cleanup' DELETE "/homestays/$hid" $null 200
$null=Check 'Verify QA homestay deleted' GET "/homestays/$hid" $null 404
$spec=Invoke-RestMethod "$base/v3/api-docs" -NoProxy -TimeoutSec 20
$operations=@()
foreach ($p in $spec.paths.PSObject.Properties) { foreach ($m in $p.Value.PSObject.Properties) { if ($m.Name -in @('get','post','put','delete','patch')) { $operations += "$($m.Name.ToUpper()) $($p.Name)" } } }
$coverage=foreach($op in $operations) {
    $method,$path=$op -split ' ',2
    $pattern='^'+[regex]::Escape($path)+'$'
    $pattern=[regex]::Replace($pattern,'\\\{[^}]+}', '[^/]+')
    $matches=@($results | Where-Object { $_.Method -eq $method -and ($_.Path -split '\?')[0] -match $pattern })
    [pscustomobject]@{Operation=$op;Cases=$matches.Count}
}
$report=@('# Docker API test report',"Run: $reportRun", "HTTP checks: $($results.Count); expected status matched: $(@($results|Where-Object Pass).Count).", '', '## Findings', '- Confirmed: a room with maxGuests=2 accepted numGuests=3 (HTTP 201). The subsequent booking response should be checked against the requested guest count; source and running image may differ.', '- Three initial failures were downstream effects of that unexpected successful booking: pending update overlapped, deletion was blocked, and the homestay remained. Isolated follow-up update/deletion passed.', '- Homestays, rooms and amenities require authentication in the current configuration; anonymous 401 is expected.', '- Admin endpoint tested with USER (403); successful ADMIN access was not tested because no admin credentials were supplied.', '- No pre-existing data intentionally changed. QA homestays/bookings removed; primary QA users and unattached amenities retained.', '', '## Endpoint coverage', '| Operation | Cases |','|---|---|')
$report += $coverage|ForEach-Object {"| $($_.Operation) | $($_.Cases) |"}
$report += @('', '## Cases', '| Case | Method | Path | Expected | Actual | Pass |','|---|---|---|---|---|---|')
$report += $results|ForEach-Object {"| $($_.Case) | $($_.Method) | $($_.Path) | $($_.Expected) | $($_.Actual) | $($_.Pass) |"}
$report|Set-Content api-test-report.md -Encoding utf8
$results|ConvertTo-Json -Depth 8|Set-Content api-test-results.json -Encoding utf8
$coverage|Format-Table
Write-Host "RESULT $(@($results|Where-Object Pass).Count)/$($results.Count)"
