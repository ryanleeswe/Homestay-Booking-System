$ErrorActionPreference = 'Stop'
$base = 'http://127.0.0.1:8080'
$results = [System.Collections.Generic.List[object]]::new()
$run = Get-Date -Format 'yyyyMMddHHmmss'
$token = ''
function Check($name, $method, $path, $body, $expected, $auth = $script:token) {
    $args = @{ Uri = "$base$path"; Method = $method; SkipHttpErrorCheck = $true; TimeoutSec = 25; NoProxy = $true }
    if ($auth) { $args.Headers = @{ Authorization = "Bearer $auth" } }
    if ($null -ne $body) { $args.Body = $body | ConvertTo-Json -Depth 10; $args.ContentType = 'application/json' }
    $r = Invoke-WebRequest @args
    $json = $null
    try { $json = $r.Content | ConvertFrom-Json } catch {}
    $ok = [int]$r.StatusCode -in @($expected)
    $results.Add([pscustomobject]@{ Case=$name; Method=$method; Path=$path; Expected=(@($expected) -join '/'); Actual=[int]$r.StatusCode; Pass=$ok; Message=$json.message })
    Write-Host "$($results.Count). $ok $method $path => $($r.StatusCode) ($name)"
    if ($null -ne $json.data) { return $json.data }
    return $json
}
$spec = Invoke-RestMethod "$base/v3/api-docs" -NoProxy
$null = Check 'Health' GET '/' $null 200 ''
$null = Check 'OpenAPI' GET '/v3/api-docs' $null 200 ''
$null = Check 'Swagger' GET '/swagger-ui/index.html' $null 200 ''
$null = Check 'Users anonymous' GET '/users' $null 401 ''
$null = Check 'Invalid token' GET '/users' $null 401 'invalid'
$email = "qa.$run@example.com"
$userBody = @{name="QA $run";email=$email;password='QaTest123!'}
$user = Check 'Register' POST '/users' $userBody 201 ''
$userId = $user.id
if (!$userId) { throw 'Registration failed; cannot continue safely.' }
$null = Check 'Duplicate email' POST '/users' $userBody 409 ''
$null = Check 'Invalid email' POST '/users' @{name='QA';email='bad';password='QaTest123!'} 400 ''
$null = Check 'Missing password' POST '/users' @{email="missing.$run@example.com"} 400 ''
$null = Check 'Wrong password' POST '/auth/login' @{email=$email;password='wrong'} 401 ''
$null = Check 'Invalid login body' POST '/auth/login' @{} 400 ''
$login = Check 'Login' POST '/auth/login' @{email=$email;password='QaTest123!'} 200 ''
$token = $login.accessToken
if (!$token) { throw 'Login failed.' }
$refresh = $login.refreshToken
$null = Check 'Users list' GET '/users' $null 200
$null = Check 'Pagination' GET '/users?current=1&pageSize=1' $null 200
$null = Check 'Invalid pagination' GET '/users?current=0&pageSize=0' $null 400
$null = Check 'Own profile' GET "/users/$userId" $null 200
$null = Check 'Missing user' GET '/users/999999999' $null 404
$null = Check 'Invalid user ID' GET '/users/0' $null 400
$null = Check 'USER cannot admin' GET '/admin' $null 403
$null = Check 'Update own name' PUT '/users' @{id=$userId;name='QA Updated';email=$email;password='QaTest123!'} 200
$null = Check 'Cannot change own email' PUT '/users' @{id=$userId;name='QA';email="changed.$run@example.com";password='QaTest123!'} 403
$other = Check 'Second disposable user' POST '/users' @{name='QA Other';email="other.$run@example.com";password='QaTest123!'} 201 ''
$null = Check 'Cannot read other profile' GET "/users/$($other.id)" $null 403
$null = Check 'Cannot update other profile' PUT '/users' @{id=$other.id;name='QA';email="other.$run@example.com";password='QaTest123!'} 403
$hbody = @{ownerId=$userId;name="QA Stay $run";location='Da Lat';description='Disposable API QA';price=800000;numberOfGuests=4;available=$true;status='APPROVED'}
$h = Check 'Create homestay' POST '/homestays' $hbody 201
$hid = $h.id
if (!$hid) { throw 'Homestay creation failed.' }
$null = Check 'List homestays anonymous blocked' GET '/homestays' $null 401 ''
$null = Check 'Available anonymous blocked' GET '/homestays/available' $null 401 ''
$null = Check 'Homestay detail public' GET "/homestays/$hid" $null 401 ''
$null=Check 'List homestays authenticated' GET '/homestays' $null 200
$null=Check 'Available authenticated' GET '/homestays/available' $null 200
$null=Check 'Detail authenticated' GET "/homestays/$hid" $null 200
$null = Check 'Owner list' GET "/homestays/owner/$userId" $null 200
$null = Check 'Missing homestay authenticated' GET '/homestays/999999999' $null 404
$hbody.name='QA Updated Stay'
$null = Check 'Update homestay' PUT "/homestays/$hid" $hbody 200
$hbody.price=-1
$null = Check 'Negative price' POST '/homestays' $hbody 400
$hbody.price=800000
$hbody.name=''
$null = Check 'Blank name' POST '/homestays' $hbody 400
$hbody.name="QA Stay $run"
$null = Check 'Create homestay anonymous' POST '/homestays' $hbody 401 ''
$roomBody=@{name='QA Room';maxGuests=2;pricePerNight=500000}
$room=Check 'Create room' POST "/homestays/$hid/rooms" $roomBody 201
$rid=$room.id
$null=Check 'List rooms public' GET "/homestays/$hid/rooms" $null 401 ''
$null=Check 'Rooms authenticated' GET "/homestays/$hid/rooms" $null 200
$null=Check 'Invalid room capacity' POST "/homestays/$hid/rooms" @{name='QA';maxGuests=0;pricePerNight=1} 400
$null=Check 'Room missing parent' POST '/homestays/999999999/rooms' $roomBody 404
$amenity=Check 'Create amenity' POST '/amenities' @{name="QA WiFi $run"} 201
$aid=$amenity.id
$null=Check 'Blank amenity' POST '/amenities' @{name=''} 400
$null=Check 'Attach amenity' POST "/homestays/$hid/amenities/$aid" $null 204
$null=Check 'Repeat attach' POST "/homestays/$hid/amenities/$aid" $null 204
$null=Check 'List amenities public' GET "/homestays/$hid/amenities" $null 401 ''
$null=Check 'Amenities authenticated' GET "/homestays/$hid/amenities" $null 200
$null=Check 'Attach missing amenity' POST "/homestays/$hid/amenities/999999999" $null 404
$bookingBody=@{userId=$userId;homestayId=$hid;roomId=$rid;checkInDate=(Get-Date).AddDays(30).ToString('yyyy-MM-dd');checkOutDate=(Get-Date).AddDays(32).ToString('yyyy-MM-dd');numGuests=2}
$booking=Check 'Create booking' POST '/bookings' $bookingBody 201
$bid=$booking.id
if (!$bid) { throw 'Booking creation failed.' }
$null=Check 'List bookings' GET '/bookings' $null 200
$null=Check 'Booking detail' GET "/bookings/$bid" $null 200
$null=Check 'Bookings by user' GET "/bookings/user/$userId" $null 200
$null=Check 'Bookings by homestay' GET "/bookings/homestay/$hid" $null 200
$null=Check 'Missing booking' GET '/bookings/999999999' $null 404
$null=Check 'Overlap booking' POST '/bookings' $bookingBody 409
$bookingBody.checkInDate=(Get-Date).AddDays(60).ToString('yyyy-MM-dd')
$bookingBody.checkOutDate=(Get-Date).AddDays(62).ToString('yyyy-MM-dd')
$bookingBody.numGuests=3
$null=Check 'Over capacity' POST '/bookings' $bookingBody 400
$bookingBody.numGuests=0
$null=Check 'Zero guests' POST '/bookings' $bookingBody 400
$bookingBody.numGuests=2
$savedDate=$bookingBody.checkOutDate
$bookingBody.checkOutDate=$bookingBody.checkInDate
$null=Check 'Invalid dates' POST '/bookings' $bookingBody 400
$bookingBody.checkOutDate=$savedDate
$null=Check 'Update pending booking' PUT "/bookings/$bid" $bookingBody 200
$null=Check 'Review before completion' POST "/bookings/$bid/review" @{rating=5;comment='QA'} 409
$null=Check 'Review not yet exists' GET "/bookings/$bid/review" $null 404
$null=Check 'Cannot complete pending' PUT "/bookings/$bid/status" @{status='COMPLETED'} 409
$null=Check 'Invalid status enum' PUT "/bookings/$bid/status" @{status='BAD'} 400
$null=Check 'Confirm booking' PUT "/bookings/$bid/status" @{status='CONFIRMED'} 200
$null=Check 'Cannot edit confirmed' PUT "/bookings/$bid" $bookingBody 409
$null=Check 'Complete booking' PUT "/bookings/$bid/status" @{status='COMPLETED'} 200
$null=Check 'Cannot change finalized' PUT "/bookings/$bid/status" @{status='CANCELLED'} 409
$null=Check 'Invalid review rating' POST "/bookings/$bid/review" @{rating=6;comment='QA'} 400
$null=Check 'Create review' POST "/bookings/$bid/review" @{rating=5;comment='QA Docker test'} 201
$null=Check 'Read review' GET "/bookings/$bid/review" $null 200
$null=Check 'Duplicate review' POST "/bookings/$bid/review" @{rating=4;comment='QA'} 409
$null=Check 'Delete homestay with booking blocked' DELETE "/homestays/$hid" $null 409
$null=Check 'Delete test booking' DELETE "/bookings/$bid" $null 204
$null=Check 'Delete missing booking' DELETE "/bookings/$bid" $null 404
$null=Check 'Delete test homestay' DELETE "/homestays/$hid" $null 200
$null=Check 'Deleted homestay unavailable' GET "/homestays/$hid" $null 404
$null=Check 'Invalid refresh' POST '/auth/refresh' @{refreshToken='invalid'} 401 ''
$newLogin=Check 'Refresh valid' POST '/auth/refresh' @{refreshToken=$refresh} 200 ''
if ($newLogin.accessToken) { $token=$newLogin.accessToken }
$null=Check 'Refresh replay blocked' POST '/auth/refresh' @{refreshToken=$refresh} 401 ''
$null=Check 'Logout' POST '/auth/logout' $null 200
$null=Check 'Refresh after logout' POST '/auth/refresh' @{refreshToken=$newLogin.refreshToken} 401 ''
$otherLogin=Check 'Other login for cleanup' POST '/auth/login' @{email="other.$run@example.com";password='QaTest123!'} 200 ''
$null=Check 'Delete own disposable account' DELETE "/users/$($other.id)" $null 200 $otherLogin.accessToken
$null=Check 'Delete missing account' DELETE '/users/999999999' $null 404
$covered=@($results | ForEach-Object { "$($_.Method) $($_.Path -replace '/\d+', '/{id}' -replace '\?.*','')" } | Sort-Object -Unique)
$report=@("# Docker API test report", "Run: $run", "Base URL: $base", "Passed: $(@($results | Where-Object Pass).Count)/$($results.Count)", '', '| Case | Method | Path | Expected | Actual | Pass |','|---|---|---|---|---|---|')
$report += $results | ForEach-Object { "| $($_.Case) | $($_.Method) | $($_.Path) | $($_.Expected) | $($_.Actual) | $($_.Pass) |" }
$report += @('', 'Test data remaining: primary QA user and unattached QA amenity. No pre-existing data was intentionally changed.', 'Admin success path was not run: no authorized admin credentials supplied.')
$report | Set-Content api-test-report.md -Encoding utf8
$results | ConvertTo-Json -Depth 6 | Set-Content api-test-results.json -Encoding utf8
Write-Host "RESULT $(@($results | Where-Object Pass).Count)/$($results.Count)"
