# Dormies - Dormitory Management System

Dormies is a modular JavaFX desktop application designed to manage dormitory assignments, rooms, rent payments, and maintenance logs. The system separates user access into three distinct portals: **Tenants** (who can view their profile, submit maintenance requests, and pay rent), **Staff** (who can assign vacant rooms to residents and manage issues), and **Admins** (who manage room configurations, handle resident eviction, and view comprehensive status reports).

## Key Features
* **Role-Based Authentication:** Dynamic UI routing using independent login interfaces.
* **Database Persisted Operations:** Live SQL querying connected directly to a MySQL database (`dormies_db`).
* **Clean Modern Styling:** Lightweight flat design interface styled using structured CSS with standard and dark-theme toggle support.
* **Concurrent Background Operations:** Multi-threaded database loading to prevent UI rendering thread blocking.

---

## Session Management via Java Serialization
To maintain stateful navigation, Dormies utilizes Java's built-in **Object Serialization**.

### Mechanism:
1. **Creation (`session.dat`):** Upon successful login, the active user session (a `Person` instance of `Tenant`, `Admin`, or `Staff`) is serialized and written to a local file named `session.dat` [1].
2. **Restoration:** When the application is launched, the `App.java` startup method checks for the existence of `session.dat` [1]. If the file exists, it reads the serialized session, restores the active memory instance [1], bypasses the login screen, and forwards the user directly to their respective dashboard.
3. **Termination (Deletion):** When a user clicks **Logout**, `SessionManager.clearSession()` is executed. This deletes the physical `session.dat` file from disk and redirects the application back to `login-view.fxml`.

---

## SOLID Design Principles Applied

### 1. Single Responsibility Principle (SRP)
* **Application:** Database operations have been completely decoupled from the UI controllers. We extracted the database logic from `LoginController`, `RegisterController`, and `DormiesController` into dedicated classes under `com.example.dormies.Dormies.repository` (namely `RoomRepository`, `TenantRepository`, and `MaintenanceRequestRepository`). 
* **Benefits:** 
  * Controllers are only responsible for UI event handling and validation.
  * Databases are managed strictly in Repository classes. Changing database configurations or moving databases no longer requires touching GUI code.

### 2. Dependency Inversion Principle (DIP)
* **Application:** Our controller classes do not depend on concrete database repository classes. Instead, we created a generic repository abstraction interface: `Repository<T>`. Inside `DormiesController`, fields are declared as the abstract interface type (e.g., `private final Repository<Room> roomRepo = new RoomRepository();`).
* **Benefits:** 
  * High-level business modules (Controllers) do not depend on low-level technical execution details (Repositories) [2]. Both depend on the abstraction interface [2].
  * If we need to change our data store in the future (e.g., migrating from MySQL to an API or NoSQL database), we can create a new repository implementing `Repository<T>` and swap it in with zero modifications to our JavaFX controllers [2].
