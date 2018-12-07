# exchange1
Currency exchange demo (akka-http)

Example:

Request: Http POST 
{
  "data": [
    {
      "currencyFrom" : "RUB",
      "currencyTo" : "USD",
      "valueFrom" : 15.65
    },
    {
      "currencyFrom" : "RUB",
      "currencyTo" : "EUR",
      "valueFrom" : 20.0
    }
  ]
}

Respond:
{
  "data": [
    {
      "currencyFrom" : "RUB",
      "currencyTo" : "USD",
      "valueFrom" : 15.65,
      "valueTo" : 45.47
    },
    {
      "currencyFrom" : "RUB",
      "currencyTo" : "EUR",
      "valueFrom" : 20.0,
      "valueTo" : 80.0
    }
  ],
  "errorCode": 0,
  "errorMessage": "No errors"
}

