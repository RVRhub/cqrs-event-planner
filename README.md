# CQRS Event Planner
This is project base on CQRS architecture.

## Example Of Rest API
### Commands 
Create Event Command:

**URL** : `/events?memberEmail={email}}`

**Method** : `POST`

Member Offer Command:

**URL** : `/events/{event_id}?memberEmail={email}&place=CafeOne`

**Method** : `POST`

Force Make Decision Command:

**URL** : `/events/{event_id}`

**Method** : `PATCH`

### Query
Get Current State Query:

**URL** : `/events/{event_id}`

**Method** : `GET`
