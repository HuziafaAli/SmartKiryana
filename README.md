# SmartKiryana POS & Inventory Management System

Small grocery shops in Pakistan often rely on manual systems, causing inaccurate billing, poor inventory control, and weak sales tracking. **SmartKiryana** solves these issues by automating billing, managing stock efficiently, and providing useful sales insights.

Built using **JavaFX** for a responsive, modern glassmorphism-inspired UI and **PostgreSQL** for dependable data persistence, SmartKiryana is optimized for high-performance and a professional, feature-dense user experience.

---

##  Key Features

- **Dynamic POS System**: Lightning-fast item lookup, barcode scanner auto-add support, and automatic digital PDF receipt archival.
- **Robust Inventory Control**: Track items by barcode, view low-stock and over-stock alerts, and perform real-time category management.
- **Sales Targets & Performance**: Assign monthly targets for cashiers and calculate dynamic bonuses based on sales performance.
- **Digital Returns Management**: Easily process customer returns against original bills with validation rules.
- **Automated Reporting**: Visual charts, top-selling items dashboards, and PDF exports for comprehensive monthly reports.

---

##  System Architecture & Design Patterns

SmartKiryana implements clean, SOLID architecture and standard GoF and GRASP design patterns:

- **Facade Pattern**: `SystemFacade` serves as a single entry point for UI controllers to interact with underlying services.
- **Template Method Pattern**: Base classes like `ReportTemplate` structure and execute custom data gathering logic.
- **Factory Pattern**: `UserFactory` encapsulates the instantiation of specific user profile roles.
- **Command Pattern**: Encapsulates specific transactional actions like processing refunds and calculating report parameters.
- **Observer Pattern**: Triggers automated alerts on crucial inventory stock transitions.

---

## Database & Prerequisites

The database schema is located at `schema/schema.sql` and includes:
- Auto-incrementing Identity IDs.
- Valid foreign keys with appropriate cascading delete rules.
- Included sample data for quick setup and demonstration.

### Setup Instructions
1. Run the `schema/schema.sql` on your PostgreSQL instance.
2. Update the credentials in `src/util/DatabaseConnection.java` if needed:
   ```java
   private static final String URL = "jdbc:postgresql://localhost:5432/SmartKiryana";
   private static final String USER = "postgres";
   private static final String PASSWORD = "postgres";
   ```

---

## 💻 How to Run

An execution script (`run.bat`) is included in the root directory. To launch the application from the command line:

```bash
run.bat
```

Ensure your `JAVA_HOME` points to a valid Java JDK install (version 11 or newer) with JavaFX properly included in your classpath.

---

### Default Admin Credentials

For immediate login upon setup, use the following default Admin profile:
- **Username**: `admin`
- **Password**: `admin123`
