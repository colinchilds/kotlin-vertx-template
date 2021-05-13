# Koddle Template
This is a basic example project utilizing [Koddle](https://github.com/colinchilds/Koddle)

## Getting started:

First, get a DB running locally. A basic PostgreSQL docker image will do fine.
Connect to it and create an example DB:

```sql
create database exampledb;
create user example_sa with encrypted password 'example';
grant all privileges on database exampledb to example_sa;
```

Next, make sure the DB connection options are configured correctly in `config.json` for the DB you just set up.
Run `Migration.kt` and it should complete successfully. Run `MyService.kt` and hit `localhost:8080`. You should see
a basic React frontend. Using cURL or Postman make a request to `localhost:8080/api/inventory` and you should get back an empty list.

## Swagger
Take a look at the two provided Swagger files (`inventory.yaml` and `login.yaml`) to know which endpoints you can hit and what their expected inputs are. For example,
when creating a new inventory item, you can post the following body:
```json
{
    "name": "Item 1",
    "manufacturer": {
        "name": "Apple"
    },
    "releaseDate": "2012-12-21T00:00:00.001Z",
    "count": 42
}
```