# AniLink — Local Farmer Product Marketplace (Java Swing)

Desktop conversion of the original PHP app **AniLink** using **Java, Swing, NetBeans GUI Builder (Design View), Maven, and JDBC (MySQL)**.

## Requirements

- JDK 17+ (Apache NetBeans bundled JDK works)
- Maven (Apache NetBeans bundled Maven works)
- MySQL / MariaDB (XAMPP)
- Apache NetBeans 19+ (for GUI Builder Design view)

## Database setup

1. Start MySQL (XAMPP).
2. Import `database.sql` (creates DB `anilink`, tables, seed accounts).

Or from terminal:

```bat
mysql -u root < database.sql
```

Default JDBC settings are in `src/main/java/com/anilink/util/DBConnection.java`:

| Setting  | Value     |
|----------|-----------|
| Host     | localhost |
| Port     | 3306      |
| Database | anilink   |
| User     | root      |
| Password | (empty)   |

## Open in NetBeans (Design View ready)

1. **File → Open Project…** → select the `AniLink` folder (the one containing `pom.xml`).
2. Wait for Maven import to finish.
3. Expand **Project Sources → Source Packages**.
4. Double-click any form class (e.g. `LoginForm.java`, `MainFrame.java`) → click the **Design** button in the editor toolbar to open the **GUI Builder**.
5. Each `.java` has a sibling `.form` file — that is the visual design.

## Run

### From NetBeans
Right-click project → **Run** (main class `anilink.App`).

### From command line

```bat
cd AniLink
set JAVA_HOME=C:\Program Files\Apache NetBeans\jdk
"%JAVA_HOME%\bin\java" ... 
mvn -q compile exec:java
```

Or package and run:

```bat
mvn -q package
java -jar target\anilink-1.0.0.jar
```

Product images resolve from `images/` in the working directory (uploads) or classpath `src/main/resources/images` (seed images).

## Demo accounts

| Role   | Email            | Password   |
|--------|------------------|------------|
| Admin  | admin@anilink.com | admin123  |
| Farmer | farmer@anilink.com| farmer123 |
| Buyer  | buyer@anilink.com | buyer123  |

Login accepts **email or contact number**.

## Features (parity with original PHP)

- **Guest:** Home, Marketplace (search/category/stock/price filters), product detail, reviews
- **Auth:** Login (email/phone), register farmer/buyer, logout
- **Buyer:** Dashboard, cart, checkout (pickup/delivery + Cash/GCash), order history, reviews, profile + change password
- **Farmer:** Dashboard (stats + low stock), product CRUD (image upload), order status workflow, sales analytics, profile + change password
- **Admin:** Dashboard, enable/disable farmers & buyers, product status/delete, order management, reports (sales by category), profile + change password

## Project layout

```
AniLink/
├── pom.xml
├── database.sql
├── UI_BUILD_SPEC.md
└── src/main/
    ├── java/com/anilink/
    │   ├── App.java
    │   ├── dao/          # JDBC data access
    │   ├── model/        # POJOs
    │   ├── session/      # login session + cart
    │   ├── util/         # DB, bcrypt, formatting
    │   └── ui/           # Swing forms (.java + .form Design files)
    └── resources/images/ # seed product images
```
