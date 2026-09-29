
# ✈️ Moveo Flight API

Moveo Flight API is a **Spring Boot** backend for managing private flight reservations, online payments, and notifications (Email/SMS).  

It supports:  

- **User registration with OTP (Email/SMS) verification**  
- **Flight search & reservation system**  
- **Payment processing via Stripe**  
- **Notification system (scheduled Email & SMS)**  
- **Webhook handling for payment confirmation**

---

## 📌 Tech Stack

- **Backend:** Spring Boot 3.x  
- **Database:** MySQL (JPA/Hibernate)  
- **Security:** Spring Security + JWT  
- **Payment Gateway:** Stripe  
- **Notifications:**  
  - Email → Mailtrap for test  
  - SMS → Twilio  
- **Scheduling:** Spring Scheduler (cron job for pending notifications)  
- **Frontend:** React + Stripe Elements  

---

## 📂 Project Structure

    moveo-flight-api/  
    ├── controller/        # REST API Controllers  
    ├── model/             # Entities (User, Flight, Reservation, Payment, Notification)  
    ├── repository/        # JPA Repositories  
    ├── service/           # Business logic (PaymentService, NotificationService, OtpService)  
    ├── scheduler/         # Scheduled jobs (send pending notifications)  
    ├── config/            # Security, Stripe, Twilio config  
    └── resources/  
      └── application.yml  
---

## Features

- **Users & Authentication**
  - JWT-based authentication for API access  

- **Flights**
  - Create, update, list available flights  
  - Flight statuses → `AVAILABLE`, `MAINTENANCE`, `RESERVED`  

- **Reservations**
  - Reserve a flight → status initially `PENDING`  
  - After payment success → status becomes `CONFIRMED`  

- **Payments**
- **Notifications**
  - Send booking confirmation (EMAIL / SMS)
  - Scheduler retries pending notifications  

---

## 🔗 API Endpoints

### **Users**

`POST /api/users`

**Required Fields:**

- `fullName`: string
- `email`: string (for Email OTP)
- `phoneNumber`: string (for SMS OTP), must start with the telephone code of the user like +1 for America (e.g: +13344556677)
- `roles`: string (defaults to `"USER"`). List of [USER, ADMIN]

#### Create / Register a new user

Use this endpoint from your React app to create a new user account.

**Request body**

```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+13344556677",
  "password": "MySecurePassword123",
  "roles": "USER"
}
```

**Success response**

```json
{
  "id": 1,
  "email": "john@example.com",
  "roles": "USER",
  "fullName": "John Doe",
  "phoneNumber": "+13344556677",
  "status": "PENDING_VERIFICATION",
  "message": "User registered. OTP sent to phone/email for verification"
}
```

**Frontend usage example**

```js
const response = await fetch("http://localhost:8000/api/users", {
  method: "POST",
  headers: {
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    fullName: "John Doe",
    email: "john@example.com",
    phoneNumber: "+13344556677",
    password: "MySecurePassword123",
    roles: "USER"
  })
});

const user = await response.json();

if (!response.ok) {
  throw new Error(typeof user === "string" ? user : "User registration failed");
}

console.log(user.message);
```

**Important notes for frontend developers**

- The backend sends an OTP after successful registration.
- The created user is initially `PENDING_VERIFICATION` until the OTP is confirmed.
- If OTP delivery fails, the API may return a plain string error.
- `password` is accepted in the request, but it is not returned in the response.

### OTP Verification

`POST /api/users/verify-otp`

**Required Fields:**

- `email`: string
- `otpCode`: string

### Resend OTP

`POST /api/users/resend-otp?email={email}`

**QueryParam:** `email` = string

### Authentication

`POST /api/auth/login`

**Required Fields:**

- `email`: string
- `password`: string

### Logout

`POST /api/auth/logout`

**Required Parameters:**

- `token`

---

### **Dashboard**

#### Get all clients for the dashboard

`GET /api/dashboard/clients`

**Access:** Admin only

**Authentication:** Send a valid JWT in the `Authorization` header.

**Frontend usage example**

```js
const response = await fetch("http://localhost:8000/api/dashboard/clients", {
  method: "GET",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${adminToken}`
  }
});

const clients = await response.json();

if (!response.ok) {
  throw new Error("Failed to load dashboard clients");
}

console.log(clients);
```

**Response shape**

```json
[
  {
    "id": 1,
    "fullName": "John Doe",
    "email": "john@example.com",
    "phoneNumber": "+13344556677",
    "reservationCount": 3,
    "status": "ACTIVE"
  },
  {
    "id": 2,
    "fullName": "Alice Smith",
    "email": "alice@example.com",
    "phoneNumber": "+12025550123",
    "reservationCount": 0,
    "status": "PENDING_VERIFICATION"
  }
]
```

**Field meanings**

- `id`: user identifier
- `fullName`: full user name
- `email`: user email
- `phoneNumber`: phone number
- `reservationCount`: number of reservations linked to the user
- `status`: one of `ACTIVE`, `PENDING_VERIFICATION`, or `DISABLED`

---

### **Flights**

### Adding flight

`POST /api/flights`

**Required Fields:**

- `model`: string
- `capacity`: integer
- `status`: string (defaults to `AVAILABLE`). List of [AVAILABLE, RESERVED, IN_FLIGHT, MAINTENANCE]
- `pricePerHour`: double
- `images`: list of string. e.g ['image1.jpg', 'image2.jpg']

### Get list of all flights

`GET /api/flights`

Response :

```json
[
  {
    "id": 5,
    "model": "Boeing 737",
    "capacity": 560,
    "status": "AVAILABLE",
    "pricePerHour": 1800,
    "images": "[\"img1.jpg\",\"img2.jpg\"]"
  },
  {
    "id": 6,
    "model": "Boeing 737",
    "capacity": 560,
    "status": "AVAILABLE",
    "pricePerHour": 1800,
    "images": "[\"img1.jpg\",\"img2.jpg\"]"
  }
]
```

### Update a flight

`PUT /api/flights/{id}`

**QueryParam:** `id` = integer

**Required Fields:**

- `model`: string
- `capacity`: integer
- `status`: string (defaults to `AVAILABLE`). List of [AVAILABLE, RESERVED, IN_FLIGHT, MAINTENANCE]
- `pricePerHour`: double
- `images`: list of string. e.g ['image1.jpg', 'image2.jpg']

### Delete a flight

`DELETE /api/flights/{id}`

**QueryParam:** `id` = integer

---

### **Reservations**

The reservation API is the main flow for creating, estimating, listing, and canceling flight bookings.

#### Create a reservation

`POST /api/reservations?userId={userId}&flightId={flightId}&destinationId={destinationId}`

**Required query params:**

- `userId`: integer
- `flightId`: integer
- `destinationId`: integer

**Required JSON body:**

```json
{
  "startDate": "2026-06-01T10:00:00",
  "nbPassengers": 2,
  "flightOption": "SIMPLE"
}
```

**Notes for frontend developers:**

- `startDate` must be a valid ISO timestamp.
- `endDate` is **automatically calculated** from `startDate` and the destination's estimated duration — do **not** send it in the request.
- `nbPassengers` must be between `1` and `5`.
- `flightOption` must be one of `SIMPLE`, `WITH_PILOT`, or `EXPRESS`.
- `totalPrice`, `status`, `createdAt`, and `updatedAt` are calculated by the backend and should **not** be sent by the frontend.
- The system only blocks overlapping reservations if there's an existing reservation with `PENDING` or `CONFIRMED` status. Reservations with `CANCELLED` or `CLOSED` status do not block new bookings even if there's a time overlap.

#### Estimate reservation price

`POST /api/reservations/estimate-price`

**Required JSON body:**

```json
{
  "flightId": 2,
  "startDate": "2026-06-01T10:00:00",
  "endDate": "2026-06-01T15:00:00",
  "nbPassengers": 2,
  "flightOption": "SIMPLE"
}
```

**Response:**

```json
{
  "estimatedTotalPrice": 4500
}
```

#### Get details of a reservation

`GET /api/reservations/{id}`

**QueryParam:** `id` = integer

#### Get reservations for a specific user

`GET /api/reservations/user/{userId}`

**QueryParam:** `userId` = integer

#### Get all reservations (admin only)

`GET /api/reservations`

#### Cancel a reservation

`PUT /api/reservations/{id}/cancel`

**QueryParam:** `id` = integer

**Notes:**

- This updates the reservation status to `CANCELLED`.
- Only the authenticated user who owns the reservation can cancel it; the frontend must send a valid JWT in the `Authorization` header.

#### Close a reservation

`PUT /api/reservations/{id}/close`

**QueryParam:** `id` = integer

**Notes:**

- This updates the reservation status to `CLOSED`.
- Use this route when the reservation has been completed and should no longer block future bookings.

---

### **Destinations**

The Destination API is used to manage flight destinations/routes in the Moveo Flight API.

A destination represents a flight route between a `startPoint` and an `endPoint`, with a `distance`, `basePrice`, and `estimatedDuration`.

> [!IMPORTANT]
> The combination of `startPoint` and `endPoint` must be unique.
> This means the same route cannot be created twice.

> [!NOTE]
> `basePrice`, `createdAt`, and `updatedAt` are managed by the backend and should **not** be sent by the frontend.

**Authentication**

All destination routes require a valid JWT in the `Authorization` header unless you are using an endpoint that is explicitly public. For frontend usage, send:

```http
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

#### Available routes

1. `GET /api/destinations`
   - Returns all destinations as `DestinationDTO` objects.
   - Useful for the frontend to populate a route selector, admin list, or search UI.

2. `GET /api/destinations/{id}`
   - Returns one destination by ID.

3. `GET /api/destinations/startPoints`
   - Returns a list of all unique `startPoint` values.

4. `GET /api/destinations/endPoints?startPoint=Paris`
   - Returns all unique `endPoint` values for the selected `startPoint`.

5. `POST /api/destinations`
   - Creates a new destination.
   - This is intended for admin users.

6. `PUT /api/destinations/{id}`
   - Updates an existing destination.
   - This is intended for admin users.

7. `DELETE /api/destinations/{id}`
   - Deletes a destination.
   - This is intended for admin users.

#### Request body for create / update

Use the following fields when creating or updating a destination:

```json
{
  "startPoint": "Paris",
  "endPoint": "Rome",
  "distance": 1450.50,
  "estimatedDuration": 120
}
```

#### Required fields

- `startPoint`: string
- `endPoint`: string
- `distance`: number
- `estimatedDuration`: integer

#### Example responses

`GET /api/destinations`

```json
[
  {
    "id": 1,
    "startPoint": "Paris",
    "endPoint": "Rome",
    "distance": 1450.5,
    "estimatedDuration": 120,
    "basePrice": 500
  }
]
```

`GET /api/destinations/startPoints`

```json
[
  "Paris",
  "Lyon"
]
```

`GET /api/destinations/endPoints?startPoint=Paris`

```json
[
  "Rome",
  "Milan"
]
```

---

### **Payments**

### Initiate a payment

`POST /api/payments/{reservationId}?notificationType={ntype}`

**QueryParam:** `reservationId` = integer, `ntype` = string (either EMAIL or SMS)

### Get payment details for a specific reservation

`GET /api/payments/reservation/{reservationId}`

**QueryParam:** `reservationId` = integer

---

### **Dashboard**

The dashboard endpoints are designed for admin or analytics screens. They return a summary of flights, routes, reservations, and paid route/flight activity.

#### Authentication

Use a valid JWT in the `Authorization` header:

```http
Authorization: Bearer <jwt-token>
```

#### `GET /api/dashboard/summary`

Returns a dashboard summary object with the following fields:

```json
{
  "activeFlights": 8,
  "totalFlights": 12,
  "availableRoutes": [
    {
      "startPoint": "Paris",
      "endPoint": "Rome"
    }
  ],
  "totalReservations": 42,
  "totalClients": 18,
  "latestReservations": [
    {
      "clientName": "Jane Doe",
      "destination": "Paris - Rome",
      "startDate": "2026-05-24T10:00:00",
      "nbPassengers": 2,
      "status": "CONFIRMED"
    }
  ],
  "paidRoutes": [
    {
      "startPoint": "Paris",
      "endPoint": "Rome",
      "flightCount": 5
    }
  ],
  "mostChosenPaidFlight": {
    "id": 4,
    "model": "Boeing 737",
    "capacity": 6,
    "status": "AVAILABLE",
    "pricePerHour": 1800,
    "images": "[\"img1.jpg\",\"img2.jpg\"]",
    "flightCount": 5
  }
}
```

#### Frontend usage example

```js
const response = await fetch("/api/dashboard/summary", {
  headers: {
    Authorization: `Bearer ${token}`
  }
});

const summary = await response.json();
```

#### `GET /api/dashboard/flights`

Returns the full list of flights used by the dashboard UI.

```js
const response = await fetch("/api/dashboard/flights", {
  headers: {
    Authorization: `Bearer ${token}`
  }
});

const flights = await response.json();
```

### **Statistics**

The statistics endpoints provide admin analytics for traffic, active users, estimated revenue, and paid reservation behavior.

#### Authentication

Use a valid JWT in the `Authorization` header:

```http
Authorization: Bearer <jwt-token>
```

#### `GET /api/admin/statistics/traffic?start=2026-01-01&end=2026-01-31`

Returns the reservation traffic for the selected period, plus paid reservation aggregates.

```json
{
  "startDate": "2026-01-01",
  "endDate": "2026-01-31",
  "traffic": 12,
  "paidReservationDestinations": [
    {
      "destinationId": 1,
      "startPoint": "Paris",
      "endPoint": "Rome",
      "reservationCount": 5
    }
  ],
  "mostChosenPaidFlight": {
    "flightId": 4,
    "model": "Boeing 737",
    "reservationCount": 5
  }
}
```

#### `GET /api/admin/statistics/active-users?start=2026-01-01&end=2026-01-31`

Returns the number of distinct users who made reservations in the selected period, plus the same paid reservation aggregates.

```json
{
  "startDate": "2026-01-01",
  "endDate": "2026-01-31",
  "activeUsers": 7,
  "paidReservationDestinations": [
    {
      "destinationId": 1,
      "startPoint": "Paris",
      "endPoint": "Rome",
      "reservationCount": 5
    }
  ],
  "mostChosenPaidFlight": {
    "flightId": 4,
    "model": "Boeing 737",
    "reservationCount": 5
  }
}
```

#### `GET /api/admin/statistics/income?start=2026-01-01&end=2026-01-31`

Returns estimated revenue for the period, plus the same paid reservation aggregates.

```json
{
  "startDate": "2026-01-01",
  "endDate": "2026-01-31",
  "totalRevenue": 120000,
  "paidReservationDestinations": [
    {
      "destinationId": 1,
      "startPoint": "Paris",
      "endPoint": "Rome",
      "reservationCount": 5
    }
  ],
  "mostChosenPaidFlight": {
    "flightId": 4,
    "model": "Boeing 737",
    "reservationCount": 5
  }
}
```

#### Frontend usage example

```js
const response = await fetch(
  `/api/admin/statistics/traffic?start=${start}&end=${end}`,
  {
    headers: {
      Authorization: `Bearer ${token}`
    }
  }
);

const trafficStats = await response.json();
```

#### Notes for frontend developers

- `paidRoutes` in the dashboard summary contains only routes tied to **paid reservations**.
- `mostChosenPaidFlight` in both the dashboard and statistics responses is based on **paid reservations only**, not all reservations.
- `paidReservationDestinations` in the statistics responses is an ordered list of destinations with the count of paid reservations for each route.

---

## 🛠️ Setup & Installation

1️⃣ **Clone repo**

stripe listen --forward-to localhost:8000/api/payments/stripe/webhook

    git clone https://github.com/org/moveo-flight-api.git
    cd moveo-flight-api

2️⃣ **Configure application properties**

    sprint.application.name=moveo-flight-api
    spring.datasource.url = jdbc:postgresql://localhost:5432/flightbd
    spring.datasource.username =  root
    spring.datasource.password =  root
    spring.datasource.jpa.hibernate.ddl-auto = update
    spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
    server.port=8000

    stripe.secret.key=sk_test_xxx
    stripe.currency=eur
    stripe.webhook-secret=whsec_xxx

    spring.mail.host=sandbox.smtp.mailtrap.io
    spring.mail.port=587
    spring.mail.username=xxx
    spring.mail.password=xxx
    spring.mail.properties.mail.smtp.auth=true
    spring.mail.properties.mail.smtp.starttls.enable=true
    spring.mail.from=no-reply@moveo.com

    twilio.account-sid=Acxxxxx
    twilio.auth-token=xxx
    twilio.phone-number=+123456789

3️⃣ Run backend

    mvn clean install
    mvn spring-boot:run

---

## 🏦 Stripe Webhook Setup

For local dev, we need to have the webhook signing secret so need to install the Stripe CLI. See the following steps :

- [Download Stripe CLI](https://stripe.com/docs/stripe-cli#install) and place the stripe file in the path environement variable.
- Login in the stripe cli using the command `stripe login`

    stripe listen --forward-to localhost:8000/api/payments/stripe/webhook

- Copy the whsec key to paymentcontroller
- Note that this method is applied because we are using localhost we not have the domain.
- If we had a domain we were supposed to configure the webhook on the stripe dashboard.

When a payment succeeds:

- Stripe sends payment_intent.succeeded
- Backend updates DB → Payment status → PAID
- Related Reservation status → CONFIRMED
- Notification is created & sent
