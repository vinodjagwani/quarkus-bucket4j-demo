# Quarkus + Bucket4j Rate Limiter

This project demonstrates how to implement **API rate limiting** in a Quarkus application using [Bucket4j](https://bucket4j.com/).  
It supports **Free**, **Premium**, and **Enterprise** plans, each with its own request quotas.

---

## 1. Prerequisites

- JDK 21+
- Apache Maven 3.9+
- cURL or Postman for testing
- Quarkus CLI (optional)

---

## 2. Build & Run

### Compile the project
```bash
Build:- mvn clean compile

Run:- mvn quarkus:dev
```

## 3. Test APIs

Free Plan (5 requests / minute)
```
curl --location 'http://localhost:8080/api/rate-limit' \
--header 'X-API-Key: free-123'
```

Premium Plan (20 requests / minute)
```
curl --location 'http://localhost:8080/api/rate-limit' \
--header 'X-API-Key: premium-xyz'
```
Enterprise Plan (100 requests / minute)
```
curl --location 'http://localhost:8080/api/rate-limit' \
--header 'X-API-Key: enterprise-abc'
```

🙏 Thank you for checking this out!