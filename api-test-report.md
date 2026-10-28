# Docker API test report
Run: 20261028222323
HTTP checks: 96; expected status matched: 92.

## Findings
- Confirmed: POST /bookings with numGuests=3 for a room with maxGuests=2 returned HTTP 201; subsequent GET returned numGuests=1. The running backend ignored the requested guest count. Current source maps and validates this field, so a stale image/source mismatch is possible and was not repaired during testing.
- Three initial failures were downstream effects of that unexpected successful booking: pending update overlapped, deletion was blocked, and the homestay remained. Isolated follow-up update/deletion passed.
- Homestays, rooms and amenities require authentication in the current configuration; anonymous 401 is expected.
- Admin endpoint tested with USER (403); successful ADMIN access was not tested because no admin credentials were supplied.
- No pre-existing data intentionally changed. QA homestays/bookings removed; primary QA users and unattached amenities retained.

## Endpoint coverage
| Operation | Cases |
|---|---|
| GET /users | 6 |
| PUT /users | 3 |
| POST /users | 5 |
| GET /homestays/{id} | 7 |
| PUT /homestays/{id} | 1 |
| DELETE /homestays/{id} | 3 |
| GET /bookings/{id} | 2 |
| PUT /bookings/{id} | 3 |
| DELETE /bookings/{id} | 3 |
| PUT /bookings/{id}/status | 7 |
| GET /homestays | 2 |
| POST /homestays | 4 |
| GET /homestays/{homestayId}/rooms | 3 |
| POST /homestays/{homestayId}/rooms | 3 |
| POST /homestays/{homestayId}/amenities/{amenityId} | 3 |
| GET /bookings | 1 |
| POST /bookings | 5 |
| GET /bookings/{bookingId}/review | 2 |
| POST /bookings/{bookingId}/review | 4 |
| POST /auth/refresh | 4 |
| POST /auth/logout | 1 |
| POST /auth/login | 5 |
| POST /amenities | 2 |
| GET /users/{id} | 4 |
| DELETE /users/{id} | 2 |
| GET /homestays/{homestayId}/amenities | 2 |
| GET /homestays/owner/{ownerId} | 2 |
| GET /homestays/available | 2 |
| GET /bookings/user/{userId} | 1 |
| GET /bookings/homestay/{homestayId} | 2 |
| GET /admin | 1 |
| GET / | 1 |

## Cases
| Case | Method | Path | Expected | Actual | Pass |
|---|---|---|---|---|---|
| Health | GET | / | 200 | 200 | True |
| OpenAPI | GET | /v3/api-docs | 200 | 200 | True |
| Swagger | GET | /swagger-ui/index.html | 200 | 200 | True |
| Users anonymous | GET | /users | 401 | 401 | True |
| Invalid token | GET | /users | 401 | 401 | True |
| Register | POST | /users | 201 | 201 | True |
| Duplicate email | POST | /users | 409 | 409 | True |
| Invalid email | POST | /users | 400 | 400 | True |
| Missing password | POST | /users | 400 | 400 | True |
| Wrong password | POST | /auth/login | 401 | 401 | True |
| Invalid login body | POST | /auth/login | 400 | 400 | True |
| Login | POST | /auth/login | 200 | 200 | True |
| Users list | GET | /users | 200 | 200 | True |
| Pagination | GET | /users?current=1&pageSize=1 | 200 | 200 | True |
| Invalid pagination | GET | /users?current=0&pageSize=0 | 400 | 400 | True |
| Own profile | GET | /users/6 | 200 | 200 | True |
| Missing user | GET | /users/999999999 | 404 | 404 | True |
| Invalid user ID | GET | /users/0 | 400 | 400 | True |
| USER cannot admin | GET | /admin | 403 | 403 | True |
| Update own name | PUT | /users | 200 | 200 | True |
| Cannot change own email | PUT | /users | 403 | 403 | True |
| Second disposable user | POST | /users | 201 | 201 | True |
| Cannot read other profile | GET | /users/7 | 403 | 403 | True |
| Cannot update other profile | PUT | /users | 403 | 403 | True |
| Create homestay | POST | /homestays | 201 | 201 | True |
| List homestays anonymous blocked | GET | /homestays | 401 | 401 | True |
| Available anonymous blocked | GET | /homestays/available | 401 | 401 | True |
| Homestay detail public | GET | /homestays/2 | 401 | 401 | True |
| List homestays authenticated | GET | /homestays | 200 | 200 | True |
| Available authenticated | GET | /homestays/available | 200 | 200 | True |
| Detail authenticated | GET | /homestays/2 | 200 | 200 | True |
| Owner list | GET | /homestays/owner/6 | 200 | 200 | True |
| Missing homestay authenticated | GET | /homestays/999999999 | 404 | 404 | True |
| Update homestay | PUT | /homestays/2 | 200 | 200 | True |
| Negative price | POST | /homestays | 400 | 400 | True |
| Blank name | POST | /homestays | 400 | 400 | True |
| Create homestay anonymous | POST | /homestays | 401 | 401 | True |
| Create room | POST | /homestays/2/rooms | 201 | 201 | True |
| List rooms public | GET | /homestays/2/rooms | 401 | 401 | True |
| Rooms authenticated | GET | /homestays/2/rooms | 200 | 200 | True |
| Invalid room capacity | POST | /homestays/2/rooms | 400 | 400 | True |
| Room missing parent | POST | /homestays/999999999/rooms | 404 | 404 | True |
| Create amenity | POST | /amenities | 201 | 201 | True |
| Blank amenity | POST | /amenities | 400 | 400 | True |
| Attach amenity | POST | /homestays/2/amenities/2 | 204 | 204 | True |
| Repeat attach | POST | /homestays/2/amenities/2 | 204 | 204 | True |
| List amenities public | GET | /homestays/2/amenities | 401 | 401 | True |
| Amenities authenticated | GET | /homestays/2/amenities | 200 | 200 | True |
| Attach missing amenity | POST | /homestays/2/amenities/999999999 | 404 | 404 | True |
| Create booking | POST | /bookings | 201 | 201 | True |
| List bookings | GET | /bookings | 200 | 200 | True |
| Booking detail | GET | /bookings/2 | 200 | 200 | True |
| Bookings by user | GET | /bookings/user/6 | 200 | 200 | True |
| Bookings by homestay | GET | /bookings/homestay/2 | 200 | 200 | True |
| Missing booking | GET | /bookings/999999999 | 404 | 404 | True |
| Overlap booking | POST | /bookings | 409 | 409 | True |
| Over capacity | POST | /bookings | 400 | 201 | False |
| Zero guests | POST | /bookings | 400 | 400 | True |
| Invalid dates | POST | /bookings | 400 | 400 | True |
| Update pending booking | PUT | /bookings/2 | 200 | 409 | False |
| Review before completion | POST | /bookings/2/review | 409 | 409 | True |
| Review not yet exists | GET | /bookings/2/review | 404 | 404 | True |
| Cannot complete pending | PUT | /bookings/2/status | 409 | 409 | True |
| Invalid status enum | PUT | /bookings/2/status | 400 | 400 | True |
| Confirm booking | PUT | /bookings/2/status | 200 | 200 | True |
| Cannot edit confirmed | PUT | /bookings/2 | 409 | 409 | True |
| Complete booking | PUT | /bookings/2/status | 200 | 200 | True |
| Cannot change finalized | PUT | /bookings/2/status | 409 | 409 | True |
| Invalid review rating | POST | /bookings/2/review | 400 | 400 | True |
| Create review | POST | /bookings/2/review | 201 | 201 | True |
| Read review | GET | /bookings/2/review | 200 | 200 | True |
| Duplicate review | POST | /bookings/2/review | 409 | 409 | True |
| Delete homestay with booking blocked | DELETE | /homestays/2 | 409 | 409 | True |
| Delete test booking | DELETE | /bookings/2 | 204 | 204 | True |
| Delete missing booking | DELETE | /bookings/2 | 404 | 404 | True |
| Delete test homestay | DELETE | /homestays/2 | 200 | 409 | False |
| Deleted homestay unavailable | GET | /homestays/2 | 404 | 200 | False |
| Invalid refresh | POST | /auth/refresh | 401 | 401 | True |
| Refresh valid | POST | /auth/refresh | 200 | 200 | True |
| Refresh replay blocked | POST | /auth/refresh | 401 | 401 | True |
| Logout | POST | /auth/logout | 200 | 200 | True |
| Refresh after logout | POST | /auth/refresh | 401 | 401 | True |
| Other login for cleanup | POST | /auth/login | 200 | 200 | True |
| Delete own disposable account | DELETE | /users/7 | 200 | 200 | True |
| Delete missing account | DELETE | /users/999999999 | 404 | 404 | True |
| Follow-up login | POST | /auth/login | 200 | 200 | True |
| Find own QA user | GET | /users?pageSize=100 | 200 | 200 | True |
| Find remaining QA homestay | GET | /homestays/owner/6 | 200 | 200 | True |
| Inspect test room capacity | GET | /homestays/2/rooms | 200 | 200 | True |
| Inspect excess booking | GET | /bookings/homestay/2 | 200 | 200 | True |
| Pending update isolated | PUT | /bookings/3 | 200 | 200 | True |
| Cancel booking | PUT | /bookings/3/status | 200 | 200 | True |
| Cancelled cannot confirm | PUT | /bookings/3/status | 409 | 409 | True |
| Delete excess test booking | DELETE | /bookings/3 | 204 | 204 | True |
| Delete QA homestay after cleanup | DELETE | /homestays/2 | 200 | 200 | True |
| Verify QA homestay deleted | GET | /homestays/2 | 404 | 404 | True |
