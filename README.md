# Transfer Service

## What

A sample RESTful API for money transfers between accounts, using in-memory database.

## Why

To have a very basic showcase for implementing trivial bank transfers using Dropwizard 2 + Jdbi 3 + H2 database, and more...

TODO: Add HATEOAS support if needed!
## How

Resource layer receives POST request with from, to account ids and amount. Then resource does some basic validations and then delegate the request to service layer which starts some validations creates transaction, delegates debit/credit to DAO layer and if all steps are successful, two journal entities will be created with common correlation-id.

## How to run

After checking-out the code, navigate to the service root folder and execute `./runt_it` script.

## Test Examples

- You can use the following curl:

`curl -X POST "http://localhost/accounts/ad52e2dc-2a8e-11ea-978f-2e728ce88125/transfers" -H "accept: application/json" -H "Content-Type: application/json" -d "{ \"beneficiaryId\": \"ad52e5a2-2a8e-11ea-978f-2e728ce88125\", \"amount\": 1}"`

- Alternatively, you can use swagger: http://localhost/swagger with same aforementioned UUIDs.

 