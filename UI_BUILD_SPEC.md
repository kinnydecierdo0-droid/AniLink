# AniLink Java Conversion — UI Builder Spec (READ FULLY)

You are building Java Swing screens for a converted PHP marketplace app. Project root:
`C:\xampp\htdocs\localfarmers\AniLink` (Maven, Java 17, JDBC MySQL, NetBeans GUI Builder).

## Non-negotiable rules

1. EVERY screen = ONE `.java` + ONE matching `.form` in the same folder (same base name).
2. `.form` must use Free Design GroupLayout (`type="102"` sequential, `type="103"` parallel) OR DesignBorderLayout for top-level splits OR DesignAbsoluteLayout only inside nested row panels (see LoginForm/RegisterForm/MainFrame examples already in repo).
3. `.java` MUST contain `initComponents()` wrapped in:
   ```
   // <editor-fold defaultstate="collapsed" desc="Generated Code">
   private void initComponents() { ... }
   }// </editor-fold>
   ```
   and end with:
   ```
   // Variables declaration - do not modify
   private javax.swing.XXX name;
   // End of variables declaration
   ```
   Variable names MUST match every component `name` in the `.form`.
4. Event handlers declared in `.form` `<Events>` must exist as `private void handler(java.awt.event.XXX evt)` methods.
5. Do NOT invent DAO methods. Only use APIs listed below. Read the actual DAO source files under `src/main/java/com/anilink/dao/` if unsure.
6. All panels extend `javax.swing.JPanel`.
7. Constructor signature EXACTLY: `public XxxPanel(anilink.shell.MainFrame frame)` — store as `private final MainFrame frame;` then `initComponents();`
8. Every panel MUST implement `public void refreshData()` (reload tables/stats from DB; catch SQLException and show `UiUtil.error(this, ...)` on failure).
9. Wrap DB calls in try/catch SQLException. Never throw from UI.
10. No package-info, no README edits, no pom edits. Only create/modify your assigned files.
11. Use `anilink.util.UiUtil.formatPrice`, `formatDate`, `formatDateTime`, `statusBadge`, `info/error/confirm`, `loadProductIcon`.
12. Currency is Philippine Peso via UiUtil.formatPrice. Seed passwords already bcrypt-hashed in SQL.
13. Colors: primary green `new java.awt.Color(46, 125, 50)`, dark sidebar `27,94,32`, light bg `248,250,248`, light green `232,245,233`, error red `211,47,47`.
14. Font pattern: `new java.awt.Font("Segoe UI", java.awt.Font.BOLD, N)` for titles, plain for labels.
15. Tables: use `DefaultTableModel` with `setColumnIdentifiers` in code (form table model can be empty `columnCount="0" rowCount="0"`). Disable editing: `table.setDefaultEditor(Object.class, null);`
16. JTables go inside JScrollPane in the form (JScrollPaneSupportLayout).
17. Combo boxes for enums filled in `refreshData()` or constructor with `removeAllItems()`.

## Available model/DAO API (verified)

### SessionContext (anilink.session.SessionContext)
- `static User getCurrentUser()`, `getRole()`, `isRole(String)`, `isLoggedIn()`
- `static Farmer getFarmerProfile()`, `int getFarmerId()`
- `static Buyer getBuyerProfile()`, `int getBuyerId()`
- `static List<CartItem> getCart()`, `int cartCount()`, `double cartTotal()`, `clearCart()`, `addToCart(Product,int)`, `Optional<CartItem> findCartItem(int)`
- CartItem: `getProduct()`, `getQuantity()`, `setQuantity(int)`, `getSubtotal()`

### Cards (anilink.shell.Cards)
HOME, MARKETPLACE, PRODUCT, BUYER_DASHBOARD, CART, CHECKOUT, BUYER_ORDERS, BUYER_PROFILE, FARMER_DASHBOARD, FARMER_PRODUCTS, FARMER_ADD_PRODUCT, FARMER_EDIT_PRODUCT, FARMER_ORDERS, FARMER_SALES, FARMER_PROFILE, ADMIN_DASHBOARD, ADMIN_FARMERS, ADMIN_BUYERS, ADMIN_PRODUCTS, ADMIN_ORDERS, ADMIN_REPORTS, ADMIN_PROFILE

### MainFrame (anilink.shell.MainFrame)
- `void showCard(String name)` — switches page (calls refreshData automatically)
- `void openProduct(int productId)`
- `void openEditProduct(int productId)`
- `void openCheckout()`
- `void refreshChrome()`

### UserDAO
- `Optional<User> authenticate(String id, String pw)`
- `Optional<User> findById(int)`, `findByEmail(String)`
- `int insert(User)`, `boolean updateProfile(int id, String first, String middle, String last, String email)`
- `boolean updatePassword(int id, String plain)`, `setActive(int id, boolean active)`
- `boolean emailExists(String)`, `verifyPassword(int userId, String plain)`
- `List<User> searchByRole(String role, String query)`, `int countByRole(String role)`

### FarmerDAO / BuyerDAO
- `Optional<Farmer> findByUserId(int)`, `findById(int)`, `int insert(Farmer)`, `boolean update(int id, String contact, String farmAddress)`, `int productCount(int farmerId)`
- `Optional<Buyer> findByUserId(int)`, `findById(int)`, `int insert(Buyer)`, `boolean update(int id, String contact, String address)`, `int orderCount(int buyerId)`

### CategoryDAO: `List<Category> findAll()` — Category has getId(), getCategoryName(), toString()=name

### ProductDAO
- `List<Product> search(String search, Integer categoryId, String availability, Double min, Double max)` (availability: null|""|"in_stock"|"out_of_stock")
- `List<Product> latestAvailable(int limit)`, `Optional<Product> findById(int)`, `List<Product> findByFarmer(int)`, `List<Product> findAll(String search, Integer categoryId)`
- `int insert(Product)`, `boolean update(Product)` (requires farmerId set), `boolean updateByAdmin(int id, String status)`, `boolean updateQuantity(int id, int qty)`, `boolean delete(int id)`, `int countAll()`
- Product fields: id, farmerId, categoryId, productName, description, price, unit, quantity, image, status, categoryName, farmerName, farmAddress, createdAt

### OrderDAO
- `int placeOrder(int buyerId, List<CartItem> cart, String deliveryMethod, String deliveryAddress, String paymentMethod)` — throws SQLException with message on stock failure
- `List<Order> findByBuyer(int)`, `findForFarmer(int)`, `findAll(String statusFilter /*may be null/""*/)`, `recentForFarmer(int, int)`, `recentAll(int)`
- `Optional<Order> findById(int)`, `List<OrderItem> findItems(int orderId)`
- `boolean updateStatus(int orderId, String status)`, `boolean farmerHasOrderItem(int orderId, int farmerId)`
- `int countByBuyerAndStatus(int buyerId, String status)`, `countByFarmerAndStatus(int, String)`, `countAll()`, `countByStatus(String)`
- `double totalSalesAll()`, `totalSalesForFarmer(int)`, `totalSalesForFarmerSince(int, java.sql.Timestamp)`
- `List<Object[]> salesByCategory()` → `[categoryName, totalDouble, itemsInt]`
- Order fields: id, buyerId, totalAmount, deliveryMethod (pickup|delivery), deliveryAddress, paymentMethod, status, createdAt, buyerName, buyerContact, itemsSummary, getItems()
- OrderItem fields: id, orderId, productId, quantity, price, subtotal, productName, unit, image

### ReviewDAO
- `List<Review> findByProduct(int)`, `boolean hasReviewed(int buyerId, int productId)`, `int insert(int buyerId, int productId, int rating, String review)`
- Review: getRating(), getReview(), getBuyerName(), getCreatedAt(), stars()

### StatsDAO
- `countFarmers()`, `countBuyers()`, `countProducts()`, `countOrders()`, `totalSales()`
- `List<Product> recentProducts(int limit)`, `List<Product> lowStock(int farmerId, int threshold)`

## Business rules to implement (from original PHP)

- Order status lifecycle: pending → confirmed → preparing → ready → completed | cancelled
- Sales only count `completed` orders
- Farmer sees only own products/orders; admin sees all
- Admin can toggle `users.is_active` but NOT disable own account
- Checkout validates stock, decrements quantity, auto out_of_stock at 0, clears cart
- Buyer reviews each product once after completed order (1–5 stars + text)
- Registration: farmer|buyer only; password min 8; PH mobile 09XXXXXXXXX; optional email unique
- Login by email OR contact number
- Low stock alert: quantity 1–5
- Product status: available | out_of_stock | inactive
- Units: kg, piece, bundle, sack, dozen, liter

## Reference files (READ THESE FIRST)
- `src/main/java/com/anilink/ui/auth/LoginForm.java` + `.form` — JFrame Free Design example
- `src/main/java/com/anilink/ui/auth/RegisterForm.java` + `.form` — Dialog + AbsoluteLayout rows
- `src/main/java/com/anilink/ui/main/MainFrame.java` + `.form` — BorderLayout + sidebar
- All of `src/main/java/com/anilink/dao/*.java`

## Minimal valid JPanel .form skeleton

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<Form version="1.5" maxVersion="1.9" type="org.netbeans.modules.form.forminfo.JPanelFormInfo">
  <AuxValues>
    <AuxValue name="FormSettings_autoResourcing" type="java.lang.Integer" value="0"/>
    <AuxValue name="FormSettings_autoSetComponentName" type="java.lang.Boolean" value="false"/>
    <AuxValue name="FormSettings_generateFQN" type="java.lang.Boolean" value="true"/>
    <AuxValue name="FormSettings_generateMnemonicsCode" type="java.lang.Boolean" value="false"/>
    <AuxValue name="FormSettings_i18nAutoMode" type="java.lang.Boolean" value="false"/>
    <AuxValue name="FormSettings_layoutCodeTarget" type="java.lang.Integer" value="1"/>
    <AuxValue name="FormSettings_listenerGenerationStyle" type="java.lang.Integer" value="0"/>
    <AuxValue name="FormSettings_variablesLocal" type="java.lang.Boolean" value="false"/>
    <AuxValue name="FormSettings_variablesModifier" type="java.lang.Integer" value="2"/>
  </AuxValues>
  <Layout>
    <DimensionLayout dim="0">
      <Group type="103" groupAlignment="0" attributes="0">
        <Group type="102" attributes="0">
          <EmptySpace min="0" pref="24" max="32767" attributes="0"/>
          <Group type="103" groupAlignment="0" attributes="0">
            <Component id="lblTitle" alignment="0" min="-2" max="-2" attributes="0"/>
            <Component id="btnAction" alignment="0" min="-2" max="-2" attributes="0"/>
          </Group>
          <EmptySpace min="0" pref="24" max="32767" attributes="0"/>
        </Group>
      </Group>
    </DimensionLayout>
    <DimensionLayout dim="1">
      <Group type="103" groupAlignment="0" attributes="0">
        <Group type="102" attributes="0">
          <EmptySpace min="0" pref="20" max="32767" attributes="0"/>
          <Component id="lblTitle" min="-2" max="-2" attributes="0"/>
          <EmptySpace max="-2" attributes="0"/>
          <Component id="btnAction" min="-2" pref="32" max="-2" attributes="0"/>
          <EmptySpace min="0" pref="20" max="32767" attributes="0"/>
        </Group>
      </Group>
    </DimensionLayout>
  </Layout>
  <SubComponents>
    <Component class="javax.swing.JLabel" name="lblTitle">
      <Properties>
        <Property name="font" type="java.awt.Font" editor="org.netbeans.beaninfo.editors.FontEditor">
          <Font name="Segoe UI" size="18" style="1"/>
        </Property>
        <Property name="text" type="java.lang.String" value="Title"/>
      </Properties>
    </Component>
    <Component class="javax.swing.JButton" name="btnAction">
      <Properties>
        <Property name="text" type="java.lang.String" value="Action"/>
      </Properties>
      <Events>
        <EventHandler event="actionPerformed" listener="java.awt.event.ActionListener" parameters="java.awt.event.ActionEvent" handler="btnActionActionPerformed"/>
      </Events>
    </Component>
  </SubComponents>
</Form>
```

Matching pattern in initComponents: create components, set properties, add ActionListeners, build GroupLayout with identical structure (gaps: EmptySpace max="-2" = default gap; min="0" pref="N" max="32767" = resizable edge gap).

## Layout recipes

**Single vertical column of full-width components** — dim0: sequential [edge gap, parallel(all comps grow), edge gap]; dim1: sequential [edge, comp, gap, comp, gap, ..., edge].

**Label over field rows** — each row is two dim0-sequential items OR just stack labels+fields as separate parallel members in dim0 and sequence in dim1 with default gaps.

**Filter bar (side by side)** — dim0 sequential: label, gap, field, gap, label, gap, field...; dim1 parallel baseline of all controls.

**Stat cards row** — dim0 sequential of fixed-width panels; dim1 single component height.

**Table fills area** — Component id scrollX min="-2" pref="600" max="32767" (dim0) and min="-2" pref="360" max="32767" (dim1).

**AbsoluteLayout nested panel** (like RegisterForm pnlNameRow):
```xml
<Layout class="org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout">
  <Property name="useNullLayout" type="boolean" value="true"/>
</Layout>
...
<Constraints>
  <Constraint layoutClass="org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout" value="org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout$AbsoluteConstraintsDescription">
    <AbsoluteConstraints x="0" y="0" width="200" height="30"/>
  </Constraint>
</Constraints>
```
Java: `panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout()); panel.add(comp, new org.netbeans.lib.awtextra.AbsoluteConstraints(x,y,w,h));`
(absolute-layout dependency is already in pom.xml)

**BorderLayout container:**
```xml
<Layout class="org.netbeans.modules.form.compat2.layouts.DesignBorderLayout"/>
<Constraints>
  <Constraint layoutClass="org.netbeans.modules.form.compat2.layouts.DesignBorderLayout" value="org.netbeans.modules.form.compat2.layouts.DesignBorderLayout$BorderConstraintsDescription">
    <BorderConstraints position="north|south|east|west|center"/>
  </Constraint>
</Constraints>
```
Java: `panel.add(comp, java BorderLayout.NORTH)` etc.

## After writing files
Run from project root:
```
$env:JAVA_HOME="C:\Program Files\Apache NetBeans\jdk"
& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" -q compile
```
Fix ONLY compile errors in YOUR files. Do not touch other packages' files except reading them.
