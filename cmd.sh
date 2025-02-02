# get 9 random products
curl http://localhost:8080/api/products/random

# get 10 random products
curl http://localhost:8080/api/products/random\?count\=10

curl -X POST http://localhost:8080/api/purchases -H "Content-Type: application/json" -d '{\n    "userId": 1,\n    "amount": 299.99,\n    "merchantName": "Medicine Store",\n    "cardNumber": "4532XXXXXXXX1234",\n    "isFraud": false\n}'

curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"name": "John Doe", "email": "john@example.com"}'

curl http://localhost:8080/api/users/1