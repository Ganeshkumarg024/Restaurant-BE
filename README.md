# 🍽️ Restaurant Billing System - Production-Ready Backend

A complete **monolithic Spring Boot backend** for a **SaaS Restaurant Billing System** designed for scalability, performance, and cost-efficiency.

---

## 📋 Project Overview

✅ Multi-tenant architecture with data isolation  
✅ Offline-first sync support (for mobile)  
✅ Google OAuth2 authentication  
✅ Razorpay payment integration  
✅ PDF invoice generation  
✅ QR code generation for tables  
✅ Dynamic feature management with AOP  
✅ 7-day trial + PRIME subscription (₹4999/month)  
✅ Local file storage (cost-effective MVP)  
✅ Redis caching for performance  
✅ Production-ready error handling and monitoring  

---

## 🏗️ Architecture

### Monolithic Project Structure
com.restaurant.billing/
├── config/ # Security, Redis, Web configurations
├── controller/ # REST API controllers
├── service/ # Business logic layer
├── repository/ # Data access layer
├── entity/ # JPA entities
├── dto/ # Data transfer objects
├── security/ # JWT, authentication, tenant context
├── exception/ # Centralized error handling
├── util/ # Utilities (QR, PDF, etc.)
└── aspect/ # AOP for feature gates

yaml
Copy code

---

## 🚀 Quick Start

### **Prerequisites**
- Java 17+
- PostgreSQL 15+
- Redis 7+
- Gradle 8+

---

### **1️⃣ Clone and Setup**

```bash
# Create project directory
mkdir restaurant-billing-system
cd restaurant-billing-system

# Initialize Gradle project
gradle init --type java-application
2️⃣ Environment Variables
Create a .env file in the project root:

properties
Copy code
# Database
DB_USERNAME=postgres
DB_PASSWORD=your_db_password
DB_NAME=restaurant_billing

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT
JWT_SECRET=your-super-secret-jwt-key-minimum-256-bits-long-change-this-in-production

# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Razorpay
RAZORPAY_KEY_ID=your_razorpay_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret
RAZORPAY_WEBHOOK_SECRET=your_webhook_secret

# Storage
STORAGE_PATH=./uploads

# CORS
CORS_ORIGINS=http://localhost:3000,http://localhost:8080
3️⃣ Database Setup
bash
Copy code
# Create database
psql -U postgres
CREATE DATABASE restaurant_billing;
\q
Migrations will run automatically on application start.

4️⃣ Build and Run
bash
Copy code
# Build project
./gradlew clean build

# Run application
./gradlew bootRun

# Or run jar
java -jar build/libs/restaurant-billing-system-1.0.0.jar
Application starts on http://localhost:8080

📁 Full File Structure
<details> <summary>📂 Expand File Tree</summary>
pgsql
Copy code
restaurant-billing-system/
├── src/
│   ├── main/java/com/restaurant/billing/
│   │   ├── RestaurantBillingApplication.java
│   │   ├── config/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── security/
│   │   ├── exception/
│   │   ├── util/
│   │   ├── annotation/
│   │   └── aspect/
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           ├── V1__initial_schema.sql
│           └── V2__seed_data.sql
├── uploads/
│   ├── menu/
│   ├── invoices/
│   ├── qrcodes/
│   └── logos/
├── build.gradle
├── settings.gradle
├── .env
├── docker-compose.yml
├── Dockerfile
└── README.md
</details>
🔌 API Endpoints
Authentication
Method	Endpoint	Description
POST	/api/auth/google	Google OAuth login
POST	/api/auth/refresh	Refresh access token
GET	/api/auth/me	Get current user info

Menu Management
Method	Endpoint	Description
GET	/api/menu	Get all menu items
GET	/api/menu/{id}	Get menu item by ID
POST	/api/menu	Create menu item
PUT	/api/menu/{id}	Update menu item
DELETE	/api/menu/{id}	Delete menu item
GET	/api/menu/categories	Get all categories

Order Management
Method	Endpoint	Description
GET	/api/orders	List all orders
GET	/api/orders/{id}	Get order by ID
POST	/api/orders	Create order
PATCH	/api/orders/{id}/status	Update order status

Billing
Method	Endpoint	Description
POST	/api/billing/generate	Generate bill for order
GET	/api/billing/{id}	Get bill details
GET	/api/billing/{id}/download	Download invoice PDF
POST	/api/billing/{id}/payment	Record payment

Subscription
Method	Endpoint	Description
POST	/api/subscription/create	Create subscription
POST	/api/subscription/verify	Verify Razorpay payment

Sync (Offline Mobile)
Method	Endpoint	Description
POST	/api/sync	Push local changes
GET	/api/sync/delta	Pull server changes

🔐 Security Features
JWT Authentication
Access Token: 15 minutes

Refresh Token: 7 days

Secure token rotation on refresh

Multi-Tenant Isolation
Tenant ID extracted from JWT

Automatic filtering via TenantInterceptor

Row-level security ready

Feature Gates (AOP)
java
Copy code
@PostMapping("/bookings")
@FeatureGate("TABLE_BOOKING")
public ResponseEntity<?> createBooking() {
    // Only accessible if feature enabled
}
💳 Subscription Plans
Plan	Duration	Price	Max Users	Storage	Features
Trial	7 days	Free	5	1 GB	Core only
PRIME	Monthly	₹4999	50	50 GB	All features

🧩 Feature Catalog
Code	Feature	Type	Trial	PRIME
QR_ORDERING	QR Code Ordering	CORE	✅	✅
TABLE_BOOKING	Table Reservations	CORE	✅	✅
SPLIT_BILLING	Split Bills	CORE	✅	✅
STAFF_MANAGEMENT	Staff Management	CORE	✅	✅
INVENTORY_MANAGEMENT	Inventory	PREMIUM	❌	✅
KITCHEN_DISPLAY	Kitchen Display	PREMIUM	❌	✅
ADVANCED_REPORTS	Analytics	PREMIUM	❌	✅

📱 Mobile App Sync
Example Request
json
Copy code
{
  "deviceId": "device-123",
  "lastSyncTime": "2024-11-10T10:00:00",
  "data": [
    {
      "entityType": "ORDER",
      "entityId": "uuid",
      "operation": "CREATE",
      "clientVersion": 1,
      "payload": { }
    }
  ]
}
🐳 Docker Deployment
Development
bash
Copy code
docker-compose up -d
Production
bash
Copy code
# Build
docker build -t restaurant-billing:latest .

# Run
docker run -d \
  -p 8080:8080 \
  --env-file .env \
  --name restaurant-app \
  restaurant-billing:latest
📊 Database Schema (Key Tables)
tenants — Restaurant accounts

users — Staff members

menu_items — Food/drink items

orders — Customer orders

bills — Generated invoices

payments — Subscription payments

tenant_features — Feature toggles

sync_logs — Offline sync history

🧪 Testing
bash
Copy code
# Unit tests
./gradlew test

# Coverage report
./gradlew test jacocoTestReport

# Integration tests
./gradlew integrationTest
📈 Monitoring
Endpoint	Description
/actuator/health	Health check
/actuator/metrics	System metrics
/actuator/prometheus	Prometheus scrape endpoint

🔧 Configuration (application.yml)
yaml
Copy code
app:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiration: 900000      # 15 mins
    refresh-token-expiration: 604800000  # 7 days
  
  razorpay:
    key-id: ${RAZORPAY_KEY_ID}
    key-secret: ${RAZORPAY_KEY_SECRET}
  
  storage:
    type: local
    local:
      base-path: ./uploads
  
  subscription:
    trial-days: 7
    plans:
      - name: PRIME
        price: 4999
        currency: INR
        billing-cycle: MONTHLY
🚀 Deployment Checklist
✅ Set strong JWT_SECRET
✅ Configure Google OAuth credentials
✅ Setup Razorpay keys & webhooks
✅ Enable PostgreSQL SSL
✅ Setup Redis password
✅ Configure CORS origins
✅ Enable HTTPS
✅ Setup database backups
✅ Enable log aggregation & monitoring

🤝 Admin Operations
Manually Activate Subscription
sql
Copy code
UPDATE tenants 
SET subscription_plan = 'PRIME',
    subscription_status = 'ACTIVE',
    subscription_start_date = NOW(),
    subscription_end_date = NOW() + INTERVAL '1 month',
    max_users = 50,
    max_storage_gb = 50
WHERE owner_email = 'customer@example.com';
Enable Premium Feature
sql
Copy code
INSERT INTO tenant_features (tenant_id, feature_code, is_enabled)
SELECT t.id, 'INVENTORY_MANAGEMENT', true
FROM tenants t
WHERE t.owner_email = 'customer@example.com';
🧰 Logs & Support
📄 Logs Location: logs/application.log

Common Issues
Issue	Solution
JWT token expired	Use refresh token endpoint
Feature not enabled	Check tenant_features table
Payment verification failed	Check Razorpay webhook configuration

📝 License
This project is proprietary software.
© All rights reserved.

👥 Contact
📧 support@yourrestaurant.com

🎯 Next Steps
Setup Google OAuth Console

Create project & credentials

Enable Google+ API

Add redirect URIs

Setup Razorpay

Create merchant account

Configure API keys & webhook

Test in sandbox mode

Deploy to Production

Choose hosting (AWS, GCP, DigitalOcean)

Setup CI/CD, SSL, monitoring

Mobile App Integration

Integrate APIs

Implement offline sync

Test sync scenarios

Built with ❤️ for Restaurant Owners

yaml
Copy code

---

Would you like me to add **GitHub badges** (like build status, license, backend tech badges) at the top of this README? It’ll make it look more professional.











ChatGPT can make mistakes. Check important info. See Cookie Preferences.
