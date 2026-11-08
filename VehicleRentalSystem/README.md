# Vehicle Rental System (Java Swing + MySQL)

A centralized third-party vehicle rental platform connecting customers and registered vehicle rental agencies, overseen by a platform administrator. Developed strictly in accordance with **Object-Oriented Programming (OOP)** principles and the architecture presented in the project slides.

---

## 📌 Project Overview & Architecture

### 1. Three Core Modules
1. **Admin Module**:
   - Review and approve/reject newly registered rental agencies.
   - View registered customers.
   - Oversee all vehicle listings platform-wide.
   - Monitor all booking requests and status.
2. **Rental Agency Module**:
   - Register agency details (enters `PENDING` status until Admin approves).
   - Add, manage, and toggle availability of vehicle listings (Cars and Bikes).
   - Review incoming customer booking requests (`Approve` or `Reject`).
   - Track rentals and mark vehicles as `RETURNED`.
3. **Customer Module**:
   - Register and authenticate customer account.
   - Browse and search vehicles across all approved rental agencies.
   - Dynamic real-time rental price calculation based on chosen number of rental days and vehicle pricing formula.
   - Submit booking requests.
   - Track booking status (`PENDING` $\rightarrow$ `APPROVED` / `REJECTED` $\rightarrow$ `RETURNED`).

---

## 🧬 Object-Oriented Programming (OOP) Concepts Implemented

- **Classes & Objects**:
  - `User`, `Admin`, `Customer`, `RentalAgency`, `Vehicle`, `Car`, `Bike`, `Booking`.
- **Encapsulation**:
  - All attributes are private, exposed through validated getters, setters, and constructors.
- **Inheritance**:
  - `Customer`, `RentalAgency`, and `Admin` extend abstract `User`.
  - `Car` and `Bike` extend abstract `Vehicle`.
- **Polymorphism**:
  - `Vehicle` declares abstract method `calculateRentalCharge(int days)`.
  - `Car` overrides `calculateRentalCharge` adding a 5% vehicle insurance and maintenance fee.
  - `Bike` overrides `calculateRentalCharge` adding a flat safety helmet and gear charge.
  - Both provide dynamic pricing breakdowns displayed live in the customer UI as days are adjusted.
- **Abstraction & Exception Handling**:
  - `BookingException`, `AuthenticationException`, `DatabaseException`.
  - Prevents booking overlaps on conflicting dates.

---

## 🗄️ Database Setup (MySQL)

1. Start your local MySQL server (port `3306`).
2. Import the schema and seed data using MySQL CLI or MySQL Workbench:
   ```bash
   mysql -u root -p < database/schema.sql
   ```
   *(If your root user has no password, omit `-p`)*
3. Verify connection configuration in `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=
   ```

---

## 🔑 Pre-seeded Demo Accounts

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Default Platform Administrator |
| **Rental Agency** | `apex` | `apex123` | Status: `APPROVED` (Apex Vehicle Rentals) |
| **Rental Agency** | `speedy` | `speedy123` | Status: `PENDING` (Test Admin approval) |
| **Customer** | `john` | `john123` | Registered Customer (License: DL-987654) |

*You can also create new users for any module directly in the application using the "Create New Account" button.*

---

## 🚀 How to Run

### Method 1: Using Maven (Recommended)
```bash
mvn clean compile exec:java
```

### Method 2: Using the Included Shell Script
```bash
./run.sh
```

---

## 📁 Directory Structure

```
OOPS/
├── pom.xml                                   # Maven dependencies & build file
├── run.sh                                    # Quick run script
├── database/
│   └── schema.sql                            # Full MySQL schema & seed data
├── src/
│   ├── main/
│   │   ├── resources/
│   │   │   └── db.properties                 # Database connection settings
│   │   └── java/
│   │       └── com/
│   │           └── vehiclerental/
│   │               ├── Main.java             # Main Application Entry Point
│   │               ├── models/               # OOP Data Models
│   │               │   ├── User.java         # Base abstract User
│   │               │   ├── Admin.java        # Inherits User
│   │               │   ├── Customer.java     # Inherits User
│   │               │   ├── RentalAgency.java # Inherits User
│   │               │   ├── Vehicle.java      # Base abstract Vehicle
│   │               │   ├── Car.java          # Inherits Vehicle (Polymorphic pricing)
│   │               │   ├── Bike.java         # Inherits Vehicle (Polymorphic pricing)
│   │               │   ├── Booking.java
│   │               │   ├── BookingStatus.java
│   │               │   ├── AgencyStatus.java
│   │               │   └── VehicleStatus.java
│   │               ├── exceptions/           # Custom Exception Classes
│   │               │   ├── AuthenticationException.java
│   │               │   ├── BookingException.java
│   │               │   └── DatabaseException.java
│   │               ├── db/                   # Database Connection
│   │               │   └── DatabaseConnection.java
│   │               ├── dao/                  # Data Access Objects (JDBC)
│   │               │   ├── UserDAO.java
│   │               │   ├── AgencyDAO.java
│   │               │   ├── VehicleDAO.java
│   │               │   └── BookingDAO.java
│   │               └── ui/                   # Java Swing User Interface
│   │                   ├── MainFrame.java    # Role Selection & Window Manager
│   │                   ├── auth/
│   │                   │   ├── LoginPanel.java
│   │                   │   ├── RegisterCustomerPanel.java
│   │                   │   ├── RegisterAgencyPanel.java
│   │                   │   └── RegisterAdminPanel.java
│   │                   ├── admin/
│   │                   │   └── AdminDashboardPanel.java
│   │                   ├── agency/
│   │                   │   └── AgencyDashboardPanel.java
│   │                   └── customer/
│   │                       └── CustomerDashboardPanel.java
└── README.md
```
