# MICRO PROJECT REPORT
### *on*
# VEHICLE RENTAL MANAGEMENT SYSTEM

<br>

**Submitted by**

| Sl. No. | Name of Student | Register No. |
| :---: | :--- | :---: |
| 1 | [Student Name 1] | [Register No. 1] |
| 2 | [Student Name 2] | [Register No. 2] |
| 3 | [Student Name 3] | [Register No. 3] |
| 4 | [Student Name 4] | [Register No. 4] |

<br>

**Course: Object-Oriented Programming (Java)**

**Under the guidance of**  
**[Faculty Name / Guide Name]**  
*Department of Computer Science and Engineering*  
**FEDERAL INSTITUTE OF SCIENCE AND TECHNOLOGY (FISAT)**  
**Academic Year 2026–2027**

---

<div style="page-break-after: always;"></div>

## ABSTRACT

The **Vehicle Rental Management System** is a robust, modular desktop application developed in Java using Swing, JDBC, and a relational MySQL database. Designed to modernize and automate the peer-to-peer and agency-to-customer vehicle leasing workflow, the platform delivers a unified ecosystem that interconnects three core stakeholders: **Customers**, **Rental Agencies**, and **Platform Administrators**. 

Traditional rental businesses suffer from fragmented manual operations, spreadsheet-based booking logs, non-transparent condition checks, and unpredictable billing calculations. This project resolves these issues by implementing a structured Object-Oriented software architecture that encapsulates domain models, database persistence layers (Data Access Objects), dynamic pricing engines (Strategy Pattern), and an interactive Swing GUI.

The system incorporates essential real-world features including:
- Multi-role credential authentication with distinct operational dashboards.
- A polymorphic vehicle hierarchy supporting Internal Combustion Engine (ICE) and Electric Vehicles (EVs) across both four-wheeler (Cars) and two-wheeler (Bikes) classifications.
- Transparent mechanical condition checklists (tyres, engine, GPS, insurance).
- Dynamic pricing algorithms that account for standard daily rates, long-term duration discounts, and surge pricing for weekend bookings.
- Lifecycle booking workflows (Requested &rarr; Approved &rarr; Active &rarr; Returned & Inspected) with real-time due-date proximity alerts and overdue warnings.
- Agency registration verification workflows governed by administrative approval.
- An agency return inspection module with automated assessment of late returns, fuel level deficits, and damage penalties.
- In-app notification queues and post-rental customer star-rating and feedback reviews.

By leveraging core Object-Oriented principles—**Inheritance, Polymorphism, Abstraction, and Encapsulation**—alongside design patterns such as the **DAO Pattern**, **Strategy Pattern**, and **Factory Pattern**, the Vehicle Rental Management System delivers an extensible, maintainable, and enterprise-grade software engineering solution.

---

<div style="page-break-after: always;"></div>

## INDEX

| Sl. No. | Contents | Page No. |
| :---: | :--- | :---: |
| **1** | **Introduction** | **4** |
| **2** | **Objectives** | **6** |
| **3** | **Existing System and Disadvantages** | **7** |
| **4** | **Proposed System and Advantages** | **8** |
| **5** | **Methodology / System Design** | **10** |
| | 5.1 Development Methodology | 10 |
| | 5.2 System Architecture | 10 |
| | 5.3 Class Diagram & OOP Design | 11 |
| | 5.4 Request and Data Flow | 12 |
| **6** | **Database Design** | **13** |
| | 6.1 Schema & Table Definitions | 13 |
| | 6.2 Relational Integrity & Workflow Constraints | 15 |
| **7** | **Implementation Details** | **16** |
| | 7.1 Source Files and Package Structure | 16 |
| | 7.2 Libraries and Build Configuration | 18 |
| | 7.3 Multi-Role Authentication & Access Control | 19 |
| | 7.4 Dynamic Pricing Engine (Strategy Pattern) | 20 |
| | 7.5 Booking Lifecycle & Return Inspection Workflow | 21 |
| **8** | **Results and Output** | **23** |
| **9** | **Program Code (Appendices)** | **25** |
| **10** | **Screenshots of Project Interfaces** | **55** |

---

<div style="page-break-after: always;"></div>

## 1. INTRODUCTION

Urban mobility and personal transportation requirements have expanded significantly in recent years. Customers frequently require short-term and medium-term vehicle rentals for business commuting, weekend travel, and emergency transit without the long-term liabilities of vehicle ownership. Concurrently, independent rental agencies and fleet owners seek reliable digital channels to list their vehicles, manage reservations, verify client credentials, and safeguard their capital investments through strict condition logging and return inspections.

Despite the digital transformation in other commercial sectors, many small-to-medium vehicle rental businesses continue to rely on manual ledger books, phone calls, physical paper agreements, or unorganized spreadsheets. These archaic approaches lead to double-bookings, billing ambiguities, lost customer communications, and a lack of accountability during vehicle return and inspection.

The **Vehicle Rental Management System** is engineered as a standalone, enterprise-grade Java desktop application that organizes the complete vehicle rental lifecycle into a secure, intuitive, and synchronized system. The software establishes a high-performance three-tier desktop architecture:

- **Java 21 (LTS)** serves as the core programming language, taking advantage of modern object-oriented paradigms, strong type safety, and clean API design.
- **Java Swing and AWT** form the presentation layer, delivering a responsive Graphical User Interface (GUI) complete with role-specific dashboards, dynamic filtering tables, modal inspection dialogs, and styled operational cards.
- **MySQL Relational Database** provides ACID-compliant persistent storage for user accounts, customer licenses, agency credentials, vehicle fleet specifications, rental bookings, reviews, and notifications.
- **JDBC (Java Database Connectivity)** links the application layer to MySQL using prepared statements to prevent SQL injection and guarantee transaction integrity.
- **Apache Maven** automates dependency management, compilation, and executable packaging.

### Three-Role Operational Structure:
1. **Customer**: Searches and filters available vehicle listings, compares vehicle specifications side-by-side, initiates rental reservations with automatic pricing calculations, monitors live return deadlines via urgency banners, and submits reviews upon trip completion.
2. **Rental Agency**: Submits fleet listings (Cars, Bikes, EVs), manages vehicle maintenance states, approves or rejects incoming customer reservations, conducts return inspections to levy legitimate fuel or damage penalties, and receives automated in-app notifications.
3. **Platform Administrator**: Serves as the central compliance authority. The administrator reviews agency registration credentials, accepts or rejects agency listings, monitors all platform users and vehicles, and oversees system-wide bookings and customer satisfaction ratings.

The project highlights the practical application of fundamental Computer Science concepts, specifically Object-Oriented Programming (OOP) design patterns, relational database normalization, and robust exception handling.

---

<div style="page-break-after: always;"></div>

## 2. OBJECTIVES

The primary objective of the Vehicle Rental Management System is to deliver an automated, secure, and transparent vehicle reservation ecosystem. The specific technical and functional objectives are:

1. **Role-Based Access Control (RBAC)**: Enforce distinct authentication pipelines for Customers, Agencies, and Administrators, ensuring each user interacts only with authorized interfaces and operations.
2. **Polymorphic Fleet Management**: Model a rich, extensible vehicle hierarchy (`Vehicle` &rarr; `Car`, `Bike` &rarr; `ElectricCar`, `ElectricBike`) that encapsulates distinct attributes (battery capacity, range, engine cc, seating capacity, fuel type, transmission).
3. **Dynamic Strategy-Based Pricing Engine**: Implement the **Strategy Design Pattern** to calculate rental costs dynamically based on duration thresholds (`LongTermDiscountStrategy`), calendar conditions (`WeekendSurgePricingStrategy`), or regular tariffs (`StandardPricingStrategy`).
4. **Mechanical Condition Transparency**: Display clear mechanical checkmarks (Tyre condition, Engine health, GPS integration, Insurance validity) so customers can inspect quality before booking.
5. **Vehicle Comparison Utility**: Provide a side-by-side comparative modal window allowing customers to evaluate specs, rates, and powertrain features between two vehicles before committing.
6. **Strict Booking Lifecycle Workflow**: Manage rental status transitions systematically:
   $$\text{PENDING} \longrightarrow \text{APPROVED / REJECTED} \longrightarrow \text{RETURNED}$$
   Prevent invalid status skipping or unauthorized status modifications.
7. **Return Inspection & Settlement Module**: Enable agencies to assess vehicle returns, calculate overdue day penalties, record fuel discrepancy fees, log physical damage notes, and compute a final itemized settlement total.
8. **Real-Time Due Date Alerts**: Automatically alert customers to impending return dates ($0\text{ days left / DUE TODAY}$, $1\text{ day left}$, $2\text{ days left}$, or $\text{OVERDUE}$) with colored urgency banners.
9. **Peer Rating & Feedback Loop**: Allow customers to rate agencies and vehicles on a 1-to-5 star scale with textual comments, aggregating average vehicle ratings across the platform.
10. **Data Persistence & Relational Integrity**: Securely store all records in MySQL using foreign keys with cascading deletions, unique constraints, and JDBC prepared statements to prevent SQL injection.

---

<div style="page-break-after: always;"></div>

## 3. EXISTING SYSTEM AND DISADVANTAGES

### 3.1 Existing System
In traditional vehicle rental services, operational workflows are predominantly handled through offline, manual, or fragmented channels:
- Customers inquire about vehicle availability via phone calls, messaging apps, or walk-in visits to physical rental counters.
- Available vehicles and tariffs are jotted down on paper whiteboards or single-user desktop spreadsheets.
- Customer verification relies on paper photocopies of driving licenses and manual cash deposits without unified record verification.
- Booking reservations, extensions, and scheduling are manually tracked; overlap checks are prone to human memory lapses.
- Vehicle return inspections are subjective and informal, frequently causing disputes over preexisting vehicle dents, fuel shortages, and return delays.

### 3.2 Disadvantages of the Existing System

- **High Rate of Scheduling Conflicts & Double Bookings**: Manual paper and spreadsheet tracking lacks concurrency control, frequently resulting in two customers booking the same vehicle on overlapping dates.
- **Lack of Standardized Pricing Logic**: Discounts and surge charges are applied haphazardly by individual staff members, resulting in non-transparent billing.
- **No Direct Verification or Safety Checks**: Mechanical condition logs (tyre wear, engine faults, GPS status) are not presented to the client, elevating safety risks.
- **Inefficient Agency Onboarding & Oversight**: Without a centralized administrative authority, illegitimate or unverified agencies can operate without verified registration numbers.
- **Lost Records and Data Fragility**: Paper documentation and local spreadsheet files are easily damaged, lost, or accidentally overwritten with no relational integrity.
- **Absent Return Monitoring & Late Penalties**: Manual systems fail to calculate precise return deadlines, depriving agencies of fair compensation for overdue vehicle use and fuel consumption.
- **No Transparent Customer Feedback System**: Prospective renters have no means of inspecting historical customer reviews or ratings for agencies and specific vehicles.

---

<div style="page-break-after: always;"></div>

## 4. PROPOSED SYSTEM AND ADVANTAGES

### 4.1 Proposed System
The **Vehicle Rental Management System** replaces disparate manual methods with a unified, role-based desktop architecture backed by MySQL. Upon launching the application, users are greeted by a modern, tabbed authentication panel. Depending on the authenticated credentials, the application dynamically displays the appropriate environment:

#### 1. Customer Module:
- Comprehensive vehicle catalog with real-time filtering by category (Cars, Bikes, EVs) and keyword searching.
- Interactive vehicle comparison tool for side-by-side inspection.
- Transparent rental rate calculator that automatically detects long-term rentals ($\ge 7$ days) and weekend periods to apply appropriate pricing models.
- "My Bookings" tracking screen with immediate due-status alert banners.
- Customer review submission interface upon booking completion.

#### 2. Rental Agency Module:
- Fleet management dashboard displaying all registered vehicles, operational statuses, and mechanical indicators.
- Multi-powertrain vehicle registration form (Car / Bike / Electric Car / Electric Bike).
- Booking approval and rejection management.
- Return Inspection dialog with itemized penalty calculations (overdue days, fuel fees, repair costs).
- In-app notification center for booking updates and administrative approvals.

#### 3. Administrator Module:
- Agency approval portal allowing the administrator to accept or reject new agency registrations.
- System-wide customer directory displaying contact details and verified driving license numbers.
- Global fleet registry monitoring vehicle readiness across all agencies.
- Centralized booking ledger tracking every reservation and return status.
- Comprehensive review and rating registry.

---

### 4.2 Advantages of the Proposed System

- **Unified Digital Platform**: Connects customers, vehicle providers, and platform supervisors within a single, coherent application.
- **Object-Oriented Design (Clean Architecture)**: Strict separation between Models, Data Access Objects (DAOs), Business Logic (Pricing Strategies), and Presentation (Swing Panels).
- **Automated Transparent Billing**: Pricing is calculated deterministically through code algorithms rather than manual estimation.
- **Relational Integrity & Safety**: MySQL foreign keys enforce referential consistency. Deleting a user cascade-cleans associated customer and agency records.
- **SQL Injection Immunization**: 100% of database interactions utilize parameterized `PreparedStatement` instances.
- **Real-Time Return Monitoring**: Proactive warning banners prevent accidental rental overruns and inform users when a vehicle is due today or overdue.
- **Objective Vehicle Return Settlements**: Formalized inspection forms ensure that fuel shortages, damage, and overdue fines are documented with notes and itemized charges.
- **Scalable Architecture**: Additional vehicle types (e.g., Heavy Commercial, Electric Scooters) or pricing strategies (e.g., Seasonal Holiday Pricing) can be added with minimal code modifications by implementing existing interfaces and abstract classes.

---

<div style="page-break-after: always;"></div>

## 5. METHODOLOGY / SYSTEM DESIGN

### 5.1 Development Methodology
The project was constructed using an **Iterative and Incremental Development Methodology**:
1. **Requirements & Domain Analysis**: Identifying user roles (Customer, Agency, Admin), domain entities, and operational constraints.
2. **Database Modeling**: Designing normalized Third Normal Form (3NF) tables in MySQL, defining primary keys, foreign keys, enums, and default constraints.
3. **Core OOP Hierarchy & Persistence (DAO Layer)**: Implementing model POJOs, abstract vehicle structures, and independent Data Access Objects for database decoupling.
4. **Business Logic & Pricing Strategies**: Developing the Strategy Pattern interfaces and concrete implementations for rate computation.
5. **UI Development**: Creating customized Swing panels, tables, dialogs, and navigation controllers.
6. **Integration, Testing & Refactoring**: Verifying end-to-end user workflows, testing exception handling, and ensuring consistent ID-ascending sort orders across all data tables.

---

### 5.2 System Architecture

The application adopts a **Tiered MVC-DAO Architecture**:

```
+-------------------------------------------------------------------------+
|                           PRESENTATION TIER                             |
|    MainFrame (CardLayout) | LoginPanel | RegisterPanels (Cust/Ag/Admin) |
|    CustomerDashboardPanel | AgencyDashboardPanel | AdminDashboardPanel  |
|    Dialogs: VehicleComparisonDialog | ReturnInspectionDialog | etc.     |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                        BUSINESS & LOGIC TIER                            |
|       PricingStrategy & Factory | NotificationService                   |
|       Validation Engines (Driving License, Password, Admin Secret Key)  |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                       DATA ACCESS LAYER (DAO)                           |
|       UserDAO | AgencyDAO | VehicleDAO | BookingDAO | ReviewDAO         |
|       NotificationDAO | DatabaseConnection                              |
+-------------------------------------------------------------------------+
                                    |  (JDBC Connector/J)
                                    v
+-------------------------------------------------------------------------+
|                          PERSISTENCE TIER                               |
|                MySQL Database (vehicle_rental_db)                       |
|   Tables: users, customers, agencies, vehicles, bookings, reviews,      |
|           notifications                                                 |
+-------------------------------------------------------------------------+
```

---

### 5.3 Class Diagram & OOP Design

The system illustrates the key pillars of Object-Oriented Programming:

```
                          +------------------------+
                          |      <<abstract>>      |
                          |        Vehicle         |
                          +------------------------+
                          | # id: int              |
                          | # brand: String        |
                          | # model: String        |
                          | # dailyRate: double    |
                          +------------------------+
                                  /        \
                                 /          \
              +--------------------+      +--------------------+
              |        Car         |      |        Bike        |
              +--------------------+      +--------------------+
              | - seats: int       |      | - engineCc: int    |
              | - transmission: Str|      | - bikeType: String |
              +--------------------+      +--------------------+
                        |                           |
                        v                           v
              +--------------------+      +--------------------+
              |    ElectricCar     |      |    ElectricBike    |
              +--------------------+      +--------------------+
              | - batteryKwh: dbl  |      | - batteryKwh: dbl  |
              | - rangeKm: int     |      | - rangeKm: int     |
              +--------------------+      +--------------------+
```

#### Mapping to OOP Concepts:
- **Abstraction**: `Vehicle` and `User` act as base abstractions. Customers interact with vehicles through uniform methods (`getDailyRate()`, `calculateRentalCost()`) without needing to know specific subclass mechanics.
- **Inheritance**: `Car` and `Bike` inherit common properties from `Vehicle`. In turn, `ElectricCar` extends `Car`, and `ElectricBike` extends `Bike`, reusing transmission, seat, and engine definitions while introducing EV-specific attributes (`batteryCapacity`, `rangeKm`).
- **Polymorphism**: The `PricingStrategy` interface defines `calculatePrice(double dailyRate, int days, LocalDate start, LocalDate end)`. Concrete strategies (`StandardPricingStrategy`, `LongTermDiscountStrategy`, `WeekendSurgePricingStrategy`) provide runtime polymorphic implementations.
- **Encapsulation**: All fields in model classes are declared `private` or `protected` and accessed strictly through validated public getter and setter methods.

---

### 5.4 Request and Data Flow

1. **Authentication Flow**: User enters credentials on `LoginPanel`. `UserDAO.login()` queries `users` table via prepared statement, verifies credentials, retrieves role (`CUSTOMER`, `AGENCY`, `ADMIN`), and passes the authenticated domain object to `MainFrame` to display the respective dashboard.
2. **Vehicle Booking Flow**:
   - Customer selects vehicle on `CustomerDashboardPanel`.
   - Customer picks rental duration; `PricingStrategyFactory` evaluates parameters and executes dynamic cost calculation.
   - On confirmation, `BookingDAO.createBooking()` persists a new record with status `PENDING`.
   - `NotificationService` dispatches an in-app alert to the respective Agency.
3. **Approval Flow**: Agency inspects pending requests on `AgencyDashboardPanel` and clicks **Approve** or **Reject**. `BookingDAO.updateBookingStatus()` updates the status in MySQL and dispatches an automated notification to the Customer.
4. **Return & Inspection Flow**:
   - Upon physical return, Agency clicks **Complete Return & Inspection**.
   - Agency inputs mileage, fuel fee, and damage notes.
   - `BookingDAO.completeReturnWithInspection()` updates the booking to `RETURNED`, resets vehicle status to `AVAILABLE`, and saves itemized penalty totals.
   - Customer is now unlocked to submit a 1-to-5 star review.

---

<div style="page-break-after: always;"></div>

## 6. DATABASE DESIGN

The backend runs on **MySQL Server** under the database catalog `vehicle_rental_db`. All tables enforce relational constraints and UTF-8 encoding.

### 6.1 Schema & Table Definitions

#### Table 1: `users`
Stores primary authentication and core identity credentials across all roles.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique internal user ID |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | System login username |
| `password` | VARCHAR(255) | NOT NULL | User account password |
| `full_name` | VARCHAR(100) | NOT NULL | Complete legal name |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE | User contact email |
| `phone` | VARCHAR(20) | NOT NULL | Contact telephone number |
| `role` | ENUM | 'ADMIN', 'CUSTOMER', 'AGENCY' | User system authorization level |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account creation date and time |

---

#### Table 2: `agencies`
Contains profile information and licensing numbers for rental agency partners.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique agency identifier |
| `user_id` | INT | NOT NULL, UNIQUE, FK &rarr; users(id) | Associated user account reference |
| `agency_name` | VARCHAR(100) | NOT NULL | Registered commercial agency name |
| `registration_no`| VARCHAR(50) | NOT NULL, UNIQUE | Official government business ID |
| `address` | VARCHAR(255) | NOT NULL | Physical office or fleet location |
| `status` | ENUM | 'PENDING', 'APPROVED', 'REJECTED' | Approval status managed by Admin |

---

#### Table 3: `customers`
Stores verified customer driving credentials.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique customer identifier |
| `user_id` | INT | NOT NULL, UNIQUE, FK &rarr; users(id) | Associated user account reference |
| `license_number`| VARCHAR(50) | NOT NULL, UNIQUE | Valid government driving license |
| `address` | VARCHAR(255) | NULLABLE | Customer residential address |

---

#### Table 4: `vehicles`
Polymorphic fleet repository storing ICE cars, motorcycles, and EV variants.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique vehicle inventory ID |
| `agency_id` | INT | NOT NULL, FK &rarr; agencies(id) | Owning agency partner reference |
| `vehicle_type` | ENUM | 'CAR', 'BIKE' | Primary vehicle classification |
| `brand` | VARCHAR(50) | NOT NULL | Manufacturer (Toyota, Tesla, etc.) |
| `model` | VARCHAR(50) | NOT NULL | Model identifier |
| `year` | INT | NOT NULL | Year of manufacture |
| `license_plate` | VARCHAR(30) | NOT NULL, UNIQUE | Vehicle registration license plate |
| `daily_rate` | DECIMAL(10,2)| NOT NULL | Base cost per 24-hour rental |
| `status` | ENUM | 'AVAILABLE', 'RENTED', 'MAINTENANCE'| Current vehicle readiness status |
| `seating_capacity`| INT | DEFAULT 5 | Number of passenger seats (Cars) |
| `transmission` | VARCHAR(20) | DEFAULT 'Manual' | Manual / Automatic / Single-Speed |
| `fuel_type` | VARCHAR(20) | DEFAULT 'Petrol' | Petrol, Diesel, Hybrid, Electric |
| `engine_cc` | INT | DEFAULT 150 | Engine displacement (Bikes) |
| `bike_type` | VARCHAR(30) | DEFAULT 'Standard' | Cruiser, Sport, Scooter, Standard |
| `colour` | VARCHAR(50) | DEFAULT 'Pearl White' | Exterior body colour |
| `mileage` | VARCHAR(50) | DEFAULT '18 km/l' | Fuel economy / efficiency rating |
| `tyre_condition`| BOOLEAN | DEFAULT TRUE | Mechanical tyre integrity flag |
| `engine_condition`| BOOLEAN | DEFAULT TRUE | Powertrain / engine health flag |
| `gps_enabled` | BOOLEAN | DEFAULT TRUE | GPS telematics equipped flag |
| `is_electric` | BOOLEAN | DEFAULT FALSE | Electric powertrain indicator |
| `battery_capacity`| DECIMAL(6,2)| DEFAULT NULL | Battery pack rating in kWh (EVs) |
| `range_km` | INT | DEFAULT NULL | Full-charge driving range (EVs) |

---

#### Table 5: `bookings`
Core operational transaction table recording reservations, dates, and final settlement figures.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique reservation ID |
| `customer_id` | INT | NOT NULL, FK &rarr; customers(id) | Renting customer reference |
| `vehicle_id` | INT | NOT NULL, FK &rarr; vehicles(id) | Reserved vehicle reference |
| `start_date` | DATE | NOT NULL | Booking commencement date |
| `end_date` | DATE | NOT NULL | Scheduled vehicle return date |
| `total_days` | INT | NOT NULL | Total reservation duration |
| `total_cost` | DECIMAL(10,2)| NOT NULL | Calculated initial rental fee |
| `status` | ENUM | 'PENDING', 'APPROVED', 'REJECTED', 'RETURNED' | Booking progression status |
| `notes` | VARCHAR(255)| NULLABLE | Special customer rental notes |
| `pricing_strategy`| VARCHAR(100)| DEFAULT 'Standard Daily Rate' | Strategy applied during booking |
| `penalty_fee` | DECIMAL(10,2)| DEFAULT 0.00 | Damage / overdue late penalties |
| `fuel_fee` | DECIMAL(10,2)| DEFAULT 0.00 | Fuel level replenishment charges |
| `platform_fee` | DECIMAL(10,2)| DEFAULT 0.00 | Administrative platform fee |
| `inspection_notes`| TEXT | DEFAULT NULL | Agency post-return inspection notes |

---

#### Table 6: `reviews`
Stores customer ratings and feedback following completed rentals.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique review ID |
| `booking_id` | INT | NOT NULL, FK &rarr; bookings(id) | Associated rental transaction ID |
| `customer_id` | INT | NOT NULL, FK &rarr; customers(id) | Reviewing customer reference |
| `agency_id` | INT | NOT NULL, FK &rarr; agencies(id) | Rated agency reference |
| `vehicle_id` | INT | NOT NULL, FK &rarr; vehicles(id) | Rated vehicle reference |
| `rating` | INT | NOT NULL, CHECK (rating BETWEEN 1 AND 5) | Star rating score (1 to 5) |
| `comment` | TEXT | NULLABLE | Detailed textual customer review |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Submission timestamp |

---

#### Table 7: `notifications`
Stores system and workflow alerts delivered to users.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique notification identifier |
| `user_id` | INT | NOT NULL, FK &rarr; users(id) | Recipient user account reference |
| `title` | VARCHAR(150)| NOT NULL | Brief subject title of alert |
| `message` | TEXT | NOT NULL | Detailed notification body |
| `is_read` | BOOLEAN | DEFAULT FALSE | Read/unread tracking flag |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Delivery timestamp |

---

### 6.2 Relational Integrity & Workflow Constraints
- **Foreign Key Cascades**: All child tables (`customers`, `agencies`, `vehicles`, `bookings`, `reviews`, `notifications`) define `ON DELETE CASCADE`. If a user or agency is deleted, orphaned data is automatically cleaned up.
- **Deterministic Sort Orders**: All core DAO queries enforce ascending primary key sorting (`ORDER BY id ASC`), guaranteeing that records appear in predictable, ordered sequence across all user tables.

---

<div style="page-break-after: always;"></div>

## 7. IMPLEMENTATION DETAILS

### 7.1 Source Files and Package Structure

The project codebase follows standard Java package conventions under the namespace `com.vehiclerental`:

```
com.vehiclerental
├── Main.java                          # Application entry point
├── db
│   └── DatabaseConnection.java        # JDBC connection manager & schema migration
├── exceptions
│   ├── AuthenticationException.java   # Login and role validation exceptions
│   ├── BookingException.java          # Reservation logic errors
│   └── DatabaseException.java         # SQL and connection failure wrappers
├── models
│   ├── User.java                      # Base user model
│   ├── Admin.java                     # Administrator domain model
│   ├── Customer.java                  # Customer model (license, address)
│   ├── RentalAgency.java              # Agency model (reg no, approval status)
│   ├── Vehicle.java                   # Abstract root vehicle model
│   ├── Car.java                       # ICE passenger car model
│   ├── Bike.java                      # ICE two-wheeler motorcycle model
│   ├── ElectricCar.java               # EV Car extension (battery, range)
│   ├── ElectricBike.java              # EV Bike extension (battery, range)
│   ├── Booking.java                   # Booking transaction model
│   ├── Review.java                    # Customer review model
│   ├── AgencyStatus.java              # Enum: PENDING, APPROVED, REJECTED
│   ├── BookingStatus.java             # Enum: PENDING, APPROVED, REJECTED, RETURNED
│   └── VehicleStatus.java             # Enum: AVAILABLE, RENTED, MAINTENANCE
├── dao
│   ├── UserDAO.java                   # Authentication, registration, user lookups
│   ├── AgencyDAO.java                 # Agency status management & queries
│   ├── VehicleDAO.java                # Fleet CRUD and multi-attribute search
│   ├── BookingDAO.java                # Reservation workflows & return inspections
│   ├── ReviewDAO.java                 # Review submission & vehicle star averages
│   └── NotificationDAO.java           # In-app notification storage & counts
├── pricing
│   ├── PricingStrategy.java           # Strategy Pattern interface
│   ├── StandardPricingStrategy.java   # Days * base rate calculation
│   ├── LongTermDiscountStrategy.java  # 15% discount for bookings >= 7 days
│   ├── WeekendSurgePricingStrategy.java# 20% surge for weekend rental periods
│   └── PricingStrategyFactory.java    # Factory for selecting appropriate strategy
├── notifications
│   ├── Notification.java              # Notification data holder
│   └── NotificationService.java       # Centralized event-driven notification broker
└── ui
    ├── MainFrame.java                 # Central JFrame switching screens via CardLayout
    ├── auth
    │   ├── LoginPanel.java            # Multi-role login screen with Quick-Demo fills
    │   ├── RegisterCustomerPanel.java # Customer registration form
    │   ├── RegisterAgencyPanel.java   # Agency registration form
    │   └── RegisterAdminPanel.java    # Admin registration with ADMIN2024 secret key
    ├── customer
    │   ├── CustomerDashboardPanel.java# Vehicle browser, search, bookings, alerts
    │   ├── VehicleComparisonDialog.java# Side-by-side spec comparison modal
    │   └── ReviewDialog.java          # Post-rental rating and feedback modal
    ├── agency
    │   ├── AgencyDashboardPanel.java  # Fleet management & booking request approvals
    │   └── ReturnInspectionDialog.java# Vehicle return check & penalty calculator
    ├── admin
    │   └── AdminDashboardPanel.java   # Directory of Agencies, Customers, Fleet, Bookings
    └── common
        └── NotificationDialog.java    # Notification center modal dialog
```

---

### 7.2 Libraries and Build Configuration

The project is managed via Apache Maven. The `pom.xml` configuration defines:
- **Java SE Development Kit**: OpenJDK 21 (LTS).
- **MySQL Connector/J (`mysql-connector-j:8.3.0`)**: Official high-performance JDBC Type 4 driver for MySQL.
- **Maven Compiler Plugin (3.13.0)**: Targets Java 21 byte-code with strict compilation flags.
- **Exec Maven Plugin (3.2.0)**: Configured to launch `com.vehiclerental.Main` directly.

---

### 7.3 Multi-Role Authentication & Access Control

Authentication is managed centrally by `UserDAO` and `LoginPanel`.
- **Role Isolation**: Upon entering credentials, the system cross-references the selected user role with the `users.role` database column. If a registered Customer attempts to log into the Agency portal, the system raises an `AuthenticationException`, blocking unauthorized dashboard access.
- **Admin Secret Key Protection**: Creating an administrative account via `RegisterAdminPanel` requires entering a specialized security key (`ADMIN2024`). Unauthorized attempts are rejected immediately.
- **Quick-Fill Demo Credentials**: For evaluation and grading convenience, `LoginPanel` includes pre-configured quick-fill credentials:
  - **Customer**: `john` / `john123`
  - **Agency**: `apex` / `apex123`
  - **Admin**: `admin` / `admin123`

---

### 7.4 Dynamic Pricing Engine (Strategy Pattern)

Vehicle tariffs are calculated using the **Strategy Design Pattern**. The `PricingStrategy` interface defines a uniform signature:

$$\text{calculatePrice}(\text{dailyRate}, \text{days}, \text{startDate}, \text{endDate})$$

`PricingStrategyFactory` evaluates the reservation duration and calendar dates to instantiate the appropriate strategy:
1. **Long-Term Discount Strategy**: Automatically triggered when a booking duration is $\ge 7\text{ days}$. It grants a **15% discount** on the gross daily rate:
   $$\text{Total} = \text{days} \times \text{dailyRate} \times 0.85$$
2. **Weekend Surge Pricing Strategy**: Triggered when a booking is shorter than 7 days and spans a Saturday or Sunday. It accounts for peak demand by applying a **20% surge**:
   $$\text{Total} = \text{days} \times \text{dailyRate} \times 1.20$$
3. **Standard Pricing Strategy**: Applied to standard weekday rentals under 7 days:
   $$\text{Total} = \text{days} \times \text{dailyRate}$$

---

### 7.5 Booking Lifecycle & Return Inspection Workflow

The booking process follows a strict state transition model:

```
[Customer Requests] ──> PENDING ──> [Agency Decision]
                                       ├──> REJECTED (Vehicle remains AVAILABLE)
                                       └──> APPROVED (Vehicle marked RENTED)
                                                │
                                                └──> [Vehicle In Use]
                                                          │
                                                          v
                                               [Return & Inspection]
                                                          │
                                                          v
                                               RETURNED (Vehicle AVAILABLE)
                                                          │
                                                          v
                                               [Customer Review Unlocked]
```

#### Inspection Settlement Calculation:
During return, the agency inspects the vehicle using `ReturnInspectionDialog`. The total settlement cost is computed as:
$$\text{Final Total} = \text{Original Rental Cost} + (\text{Overdue Days} \times \text{Daily Rate}) + \text{Fuel Deficit Charge} + \text{Damage Penalty Fee}$$

---

<div style="page-break-after: always;"></div>

## 8. RESULTS AND OUTPUT

The Vehicle Rental Management System was compiled and executed on OpenJDK 21 connected to a live MySQL database instance. Comprehensive end-to-end testing verified that all functional requirements execute properly:

1. **Authentication & Session Routing**: Successfully logged in using Customer, Agency, and Admin credentials. Role validation reliably prevented cross-role dashboard access.
2. **Fleet Registration**: Agency users successfully added ICE and Electric vehicles with complete specifications (battery capacity, range, tyre conditions, engine condition, GPS). All values were properly written to MySQL.
3. **Vehicle Search & Comparison**: The vehicle catalog accurately filtered cars by powertrain type and keyword search. The side-by-side comparison modal properly displayed differing specifications between two selected vehicles.
4. **Dynamic Tariff Calculation**: Tested multi-day bookings. Durations over 7 days correctly received the 15% long-term discount, and weekend rentals appropriately reflected the 20% surge rate.
5. **Real-Time Return Alerts**: The Customer dashboard showed distinct alert banners based on scheduled return dates:
   - Due in 2 days: Informational banner.
   - Due today: Red urgent warning banner.
   - Past return date: Overdue warning banner.
6. **Return Inspection**: Agencies successfully processed returns with overdue penalties, fuel surcharges, and damage fees. The vehicle status returned to `AVAILABLE` immediately upon inspection completion.
7. **Customer Reviews**: Customers successfully rated completed rentals on a 1-to-5 star scale. The calculated average rating was correctly updated in the vehicle catalog.

---

<div style="page-break-after: always;"></div>

## 9. PROGRAM CODE (APPENDICES)

### Appendix 9.1: Abstract Model Hierarchy (`Vehicle.java`)
```java
package com.vehiclerental.models;

/**
 * Abstract base class representing a generic Vehicle in the rental fleet.
 * Demonstrates Object-Oriented Abstraction and Encapsulation.
 */
public abstract class Vehicle {
    protected int id;
    protected int agencyId;
    protected String agencyName;
    protected String vehicleType;
    protected String brand;
    protected String model;
    protected int year;
    protected String licensePlate;
    protected double dailyRate;
    protected VehicleStatus status;
    protected String colour;
    protected String mileage;
    protected boolean tyreConditionGood;
    protected boolean engineConditionGood;
    protected boolean gpsEnabled;
    protected boolean insuranceVerified;
    protected boolean isElectric;

    public Vehicle(int id, int agencyId, String agencyName, String vehicleType,
                   String brand, String model, int year, String licensePlate,
                   double dailyRate, VehicleStatus status, String colour,
                   String mileage, boolean tyreGood, boolean engineGood,
                   boolean gps, boolean insurance, boolean isElectric) {
        this.id = id;
        this.agencyId = agencyId;
        this.agencyName = agencyName;
        this.vehicleType = vehicleType;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
        this.dailyRate = dailyRate;
        this.status = status;
        this.colour = colour;
        this.mileage = mileage;
        this.tyreConditionGood = tyreGood;
        this.engineConditionGood = engineGood;
        this.gpsEnabled = gps;
        this.insuranceVerified = insurance;
        this.isElectric = isElectric;
    }

    public int getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public double getDailyRate() { return dailyRate; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
    public boolean isElectric() { return isElectric; }

    /**
     * Polymorphic method to calculate standard rental costs.
     */
    public double calculateRentalCost(int days) {
        return this.dailyRate * days;
    }
}
```

---

### Appendix 9.2: Specialized Electric Vehicle Subclass (`ElectricCar.java`)
```java
package com.vehiclerental.models;

/**
 * Represents an Electric Car extending the Car hierarchy.
 * Demonstrates Multi-level Inheritance and Specialization.
 */
public class ElectricCar extends Car {
    private double batteryCapacityKwh;
    private int rangeKm;
    private double chargingTimeHours;

    public ElectricCar(int id, int agencyId, String agencyName, String brand,
                       String model, int year, String licensePlate, double dailyRate,
                       VehicleStatus status, int seatingCapacity, String transmission,
                       String colour, String mileage, boolean tyreGood,
                       boolean engineGood, boolean gps, boolean insurance,
                       double batteryCapacityKwh, int rangeKm, double chargingTime) {
        super(id, agencyId, agencyName, brand, model, year, licensePlate,
              dailyRate, status, seatingCapacity, transmission, "Electric",
              colour, mileage, tyreGood, engineGood, gps, insurance, true);
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.rangeKm = rangeKm;
        this.chargingTimeHours = chargingTime;
    }

    public double getBatteryCapacityKwh() { return batteryCapacityKwh; }
    public int getRangeKm() { return rangeKm; }
    public double getChargingTimeHours() { return chargingTimeHours; }
}
```

---

### Appendix 9.3: Strategy Pattern Dynamic Pricing Engine (`PricingStrategyFactory.java`)
```java
package com.vehiclerental.pricing;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Factory class responsible for selecting the appropriate PricingStrategy.
 * Demonstrates the Strategy Pattern and Factory Pattern.
 */
public class PricingStrategyFactory {

    public static PricingStrategy getStrategy(int days, LocalDate startDate, LocalDate endDate) {
        // Priority 1: Long term rentals (>= 7 days) qualify for 15% discount
        if (days >= 7) {
            return new LongTermDiscountStrategy();
        }

        // Priority 2: Check if booking spans a weekend
        boolean spansWeekend = false;
        LocalDate curr = startDate;
        while (!curr.isAfter(endDate)) {
            if (curr.getDayOfWeek() == DayOfWeek.SATURDAY || curr.getDayOfWeek() == DayOfWeek.SUNDAY) {
                spansWeekend = true;
                break;
            }
            curr = curr.plusDays(1);
        }

        if (spansWeekend) {
            return new WeekendSurgePricingStrategy();
        }

        // Default: Standard daily tariff
        return new StandardPricingStrategy();
    }
}
```

---

### Appendix 9.4: Database Connection Manager (`DatabaseConnection.java`)
```java
package com.vehiclerental.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection manager handling MySQL connectivity and pooling.
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found in classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

---

<div style="page-break-after: always;"></div>

## 10. SCREENSHOTS OF PROJECT INTERFACES

The following section contains designated visual spaces for capturing and inserting screenshots of the running Java Swing application.

---

### Figure 1: Universal Login Screen with Multi-Role Tabs & Quick-Fill Demo
*Displays Customer, Agency, and Admin login tabs with pre-configured quick demo credential buttons.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 1 ]                               |
|                     Universal Login Screen with Quick-Fill                              |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 1**: Multi-role login screen with tabs for Customer, Agency, and Admin, featuring quick-fill demonstration buttons.

---

### Figure 2: Customer Registration Interface
*New customer onboarding form capturing full name, email, phone, residential address, and verified driving license.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 2 ]                               |
|                        Customer Registration Interface                                  |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 2**: Customer account registration form with driving license validation.

---

### Figure 3: Rental Agency Partner Registration
*Commercial onboarding form requiring agency business name, official registration number, and office address.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 3 ]                               |
|                         Agency Registration Screen                                      |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 3**: Agency registration interface capturing business details for admin verification.

---

### Figure 4: Administrator Registration with Secret Key Validation
*Admin registration screen protected by security key verification (`ADMIN2024`).*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 4 ]                               |
|                       Administrator Registration Screen                                 |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 4**: Admin registration dialog demonstrating secret key verification.

---

### Figure 5: Customer Dashboard – Vehicle Browsing & Search
*Customer vehicle catalog with category filtering, keyword searching, daily rates, and condition indicators.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 5 ]                               |
|                        Customer Browse Vehicles Catalog                                 |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 5**: Customer vehicle browsing table showing live availability, mechanical checks, and daily rental rates.

---

### Figure 6: Side-by-Side Vehicle Specification Comparison Modal
*Modal window displaying two selected vehicles side-by-side for comparative evaluation.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 6 ]                               |
|                     Vehicle Comparison Dialog Interface                                 |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 6**: Side-by-side comparison modal evaluating rates, powertrain specs, and features between two vehicles.

---

### Figure 7: Customer Booking Request & Dynamic Tariff Calculation
*Booking confirmation modal calculating rental totals and identifying applicable pricing strategies.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 7 ]                               |
|                     Customer Booking Request & Calculation                              |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 7**: Booking modal showing date range selection, duration, and dynamic pricing calculations.

---

### Figure 8: Customer "My Bookings" with Real-Time Due Status Alerts
*Customer bookings view showing status badges and return deadline warning banners.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 8 ]                               |
|                     Customer Bookings & Live Alert Banner                               |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 8**: Customer booking history table displaying status badges and return alerts (Due Today / Overdue).

---

### Figure 9: Customer Star Rating & Review Submission Dialog
*Post-rental review dialog allowing customers to rate vehicles from 1 to 5 stars with feedback comments.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 9 ]                               |
|                       Customer Review & Feedback Dialog                                 |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 9**: Post-rental review dialog with star ratings and written customer feedback.

---

### Figure 10: Agency Dashboard – Fleet Inventory Management
*Agency dashboard showing fleet management with vehicle statuses, specifications, and mechanical checkmarks.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 10 ]                              |
|                     Agency Fleet Inventory Management Table                             |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 10**: Agency vehicle inventory panel displaying registered cars, bikes, and operational statuses.

---

### Figure 11: Agency Add Vehicle Dialog (ICE & Electric Powertrain Setup)
*Vehicle creation modal supporting Cars, Bikes, and Electric vehicle attributes.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 11 ]                              |
|                        Agency Add Vehicle Dialog Modal                                  |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 11**: Vehicle registration form supporting both ICE and Electric powertrain configurations.

---

### Figure 12: Agency Booking Requests & Approval Workflow
*Agency booking queue showing pending customer reservations with Accept and Reject actions.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 12 ]                              |
|                       Agency Booking Requests Management                                |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 12**: Agency booking management table with Approve and Reject action buttons.

---

### Figure 13: Agency Return Inspection & Settlement Dialog
*Vehicle inspection dialog for calculating overdue day penalties, fuel replenishment fees, and damage charges.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 13 ]                              |
|                    Agency Return Inspection & Settlement Dialog                         |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 13**: Return inspection interface itemizing late penalties, fuel fees, and damage charges for final settlement.

---

### Figure 14: Admin Dashboard – Agency Partner Directory & Approvals
*Administrator dashboard displaying registered agencies with pending, accepted, and rejected statuses.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 14 ]                              |
|                   Admin Agency Directory & Partner Approval Table                       |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 14**: Admin agencies directory with status-based filtering and Accept/Reject buttons.

---

### Figure 15: Admin Dashboard – Registered Customers Directory
*Platform-wide customer table showing customer IDs, usernames, contact numbers, and driving license numbers.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 15 ]                              |
|                     Admin Registered Customers Directory Table                          |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 15**: Administrator view of registered customers and verified license numbers.

---

### Figure 16: Admin Dashboard – Platform-Wide Fleet Registry
*Administrator fleet inventory table monitoring all vehicles listed across all approved agencies.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 16 ]                              |
|                      Admin Platform-Wide Fleet Registry                                 |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 16**: Platform-wide vehicle registry displaying fleet status, vehicle IDs, and agency ownership.

---

### Figure 17: Admin Dashboard – Global Bookings & Live Due Status Registry
*Administrator ledger displaying all platform bookings, dates, pricing strategies, and return states.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 17 ]                              |
|                       Admin Global Bookings Ledger View                                 |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 17**: Global booking ledger sorted by Booking ID ascending, showing live due statuses and notes.

---

### Figure 18: Admin Dashboard – Customer Feedback & Star Ratings Registry
*Administrator review monitoring table displaying all submitted reviews and star ratings.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 18 ]                              |
|                     Admin Customer Reviews & Star Ratings Table                         |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 18**: Customer reviews directory displaying ratings (1-5 stars), vehicle details, and comments.

---

### Figure 19: In-App User Notification Center Dialog
*Popup dialog displaying real-time system alerts and booking notifications.*

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                                                                         |
|                       [ PASTE SCREENSHOT HERE: FIGURE 19 ]                              |
|                     In-App Notification Center Modal Dialog                             |
|                                                                                         |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```
**Figure 19**: Notification center dialog showing unread counts and automated alert logs.
