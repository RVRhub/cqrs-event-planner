# CQRS Event Planner
This is project base on CQRS architecture.

## Example Of Rest API
### Commands 
Create Event Command:

&nbsp;&nbsp; **URL** : `/events?memberEmail={email}}`

&nbsp;&nbsp; **Method** : `POST`

Member Offer Command:

&nbsp;&nbsp; **URL** : `/events/{event_id}?memberEmail={email}&place=CafeOne`

&nbsp;&nbsp; **Method** : `POST`

Force Make Decision Command:

&nbsp;&nbsp; **URL** : `/events/{event_id}`

&nbsp;&nbsp; **Method** : `PATCH`

### Query
Get Current State Query:

&nbsp;&nbsp; **URL** : `/events/{event_id}`

&nbsp;&nbsp; **Method** : `GET`
