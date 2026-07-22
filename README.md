# Dormies - Dormitory Management System

Dormies is a modular JavaFX desktop application designed to manage dormitory assignments, rooms, rent payments, and maintenance logs. The system separates user access into two distinct, isolated portals: **Tenants** (who can view their profile, submit maintenance requests, and pay rent) and **Admins** (who manage room configurations, assign vacant rooms to residents, resolve/delete maintenance issues, evict residents, and view comprehensive status reports).

## Key Features
* **Role-Based Authentication:** Dynamic FXML view switching based on login credentials.
* **Database Persisted Operations:** Live SQL querying connected directly to a MySQL database (`dormies_db`).
* **Clean Modern Styling:** Lightweight flat design interface styled using structured CSS with standard and dark-theme toggle support.
* **Concurrent Background Operations:** Multi-threaded database loading to prevent UI rendering thread blocking.

---

## Session Management via Java Serialization
To maintain stateful navigation, Dormies utilizes Java's built-in **Object Serialization**.

### Mechanism:
1. **Creation (`session.dat`):** Upon successful login, the active user session (a `Person` instance of `Tenant` or `Admin`) is serialized and written to a local file named `session.dat` [1].
2. **Restoration:** When the application is launched, the `App.java` startup method checks for the existence of `session.dat` [1]. If the file exists, it reads the serialized session, restores the active memory instance [1], bypasses the login screen, and forwards the user directly to their respective dashboard.
3. **Termination (Deletion):** When a user clicks **Logout**, `SessionManager.clearSession()` is executed. This deletes the physical `session.dat` file from disk and redirects the application back to `login-view.fxml`.

---

## SOLID Design Principles Applied

### 1. Single Responsibility Principle (SRP)
* **Application:** Database operations have been completely decoupled from the UI controllers. We extracted the database logic from `LoginController`, `RegisterController`, and `DormiesController` into dedicated classes under `com.example.dormies.Repositories` (`RoomRepository`, `TenantRepository`, and `MaintenanceRequestRepository`). 
* **Benefits:** 
  * Controllers are only responsible for UI event handling and validation.
  * Databases are managed strictly in Repository classes. Changing database configurations or moving databases no longer requires touching GUI code.

### 2. Dependency Inversion Principle (DIP)
* **Application:** Our controller classes do not depend on concrete database repository classes. Instead, we created a generic repository abstraction interface: `Repository<T>`. Inside `DormiesController`, fields are declared as the abstract interface type (e.g., `private final Repository<Room> roomRepo = new RoomRepository();`).
* **Benefits:** 
  * High-level business modules (Controllers) do not depend on low-level technical execution details (Repositories) [2]. Both depend on the abstraction interface [2].
  * If we need to change our data store in the future (e.g., migrating from MySQL to an API or NoSQL database), we can create a new repository implementing `Repository<T>` and swap it in with zero modifications to our JavaFX controllers [2].

---

## Design Patterns Applied

### 1. Creational: Factory Method Pattern (`PersonFactory`)
* **Application:** When authenticating via `LoginController`, the concrete user role instance (`Tenant` or `Admin`) is instantiated dynamically through `PersonFactory.createPerson(role, name, id)` based on credentials.
* **Benefit:** Decouples user-object creation from the authentication controller, allowing new roles to be added without modifying the core login logic.

### 2. Structural: Facade Pattern (`DormitoryFacade`)
* **Application:** Unifies all database repository operations (`RoomRepository`, `TenantRepository`, `MaintenanceRequestRepository`) into a single, clean API layer (`DormitoryFacade`).
* **Benefit:** Simplifies multi-repository operations (e.g., `assignRoom()` which updates both `tenant` and `room` records) so the GUI controllers only need to call a single method, hiding low-level database coordination.

### 3. Behavioral: Strategy Pattern (`PaymentStrategy`)
* **Application:** Dynamically handles interchangeable payment processing methods (`CardPaymentStrategy` vs `CashPaymentStrategy`) when the tenant clicks pay rent.
* **Benefit:** Isolates payment processing rules from the `DormiesController`, making it easy to swap or add new payment processors (like PayPal, Gcash, Bank Transfer) without changing your main GUI code.

## Class Diagram
<img width="991" height="419" alt="image" src="https://github.com/user-attachments/assets/83161df3-7284-4f70-a164-5dfbdff5c417" />

## Sequence Diagram
<img width="434" height="426" alt="image" src="https://github.com/user-attachments/assets/22cbf165-b937-4b60-ac23-e82981c68934" />

## Activity Diagram
<img width="234" height="520" alt="image" src="https://github.com/user-attachments/assets/c6c83ec8-da5b-4175-a39e-fecf41bef027" />

## Use Case Diagram
<img width="332" height="407" alt="image" src="https://github.com/user-attachments/assets/30135a5d-2c0e-496b-afee-61fbe09a1345" />



