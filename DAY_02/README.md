# Journey Unbound - Full-Stack Authentication Portal

A high-energy transit and travel showcase portal featuring a robust, secure authentication system built from scratch. This project demonstrates how to handle user authentication without heavy frameworks like Spring Boot or Node.js/Express, utilizing a pure Java backend and native web capabilities.

---

## 🎯 Project Motto & Vision

> **"Discover. Journey. Unbound."**

Modern web development is often bogged down by massive frameworks that hide the underlying mechanics of network traffic and database drivers. The motto of this project is to break free from those dependencies (**Unbound**). 

By engineering this application using pure, native technologies, we prove that clean architecture, secure data hydration, and rapid full-stack synchronization can be achieved through fundamental core engineering principles.

---

## 📂 Project Structure

The project separates the frontend user interface from the backend database operations. Below is the exact file layout across your workspace:

```text
REACT_LEARN/
├── DAY_02/
│   └── Back-end/
│       ├── BackendServer.java              # Native Java HTTP server & JDBC logic
│       ├── BackendServer\$LoginHandler.class # Compiled nested class for logins
│       ├── BackendServer\$RegisterHandler.class # Compiled nested class for signups
│       ├── BackendServer.class             # Compiled main backend entry point
│       └── mysql-connector-j-9.2.0.jar     # Genuine 2.5MB MySQL JDBC binary driver
├── src/
│   ├── Login.jsx                           # Dashboard layout & Registration view
│   ├── LoginForm.jsx                       # Login interface with storage actions
│   ├── login.css                           # High-energy custom travel UI styling
│   ├── index.css                           # Core global style tokens
│   └── index.js                            # React application root attachment
├── .vscode/
│   └── launch.json                         # VS Code settings linking the JAR path
├── package.json                            # React dependency list (Axios, etc.)
└── README.md                               # Project documentation guide
```

---

## ⚙️ How It Works (Under the Hood)

This system establishes an end-to-end data pipeline connecting user browser interactions straight to a permanent SQL storage engine:

[React App] ──(JSON over HTTP)──> [Java API Server] ──(JDBC)──> [MySQL Database]│└───> Remembers Session State via LocalStorage / SessionStorage



### 1. Frontend State & Network Call
React manages user credential states through the `useState` hook. When forms are submitted, `axios` wraps the payload into a clean JSON string and sends a `POST` request to the backend.

### 2. Client-Side Session Hydration
*   **LocalStorage**: If the "Keep me logged in" checkbox is checked, session text writes to LocalStorage, remaining intact even if the browser window closes.
*   **SessionStorage**: If left unchecked, data writes to SessionStorage, which wipes completely clean the moment the browser tab is shut down.
*   On page refresh, a `useEffect` initialization hook reads browser storage to keep user states hydrated automatically.

### 3. Native Java HTTP Routing
The backend acts as an independent network listener utilizing standard `com.sun.net.httpserver.HttpServer` on port `5000`. It processes standard JSON request blocks, tracks operational logs, and outputs custom headers to resolve cross-origin browser rules (**CORS Preflight Handshakes**).

### 4. JDBC Database Isolation
Java relies on `java.sql` classes and the `mysql-connector-j-9.2.0.jar` driver to talk to MySQL on port `3306`. It executes protected parameterized strings (`PreparedStatement`) to separate application variables from query logic, preventing malicious SQL injection attacks.
*   **Write Operations**: Inserts a fresh record row during the Registration panel submit event.
*   **Read Operations**: Evaluates if target user credentials exist during the Login validation step.

---

## 🚀 How to Use & Run the Project

Follow these exact operational steps to launch the environment without matching cache errors:

### Prerequisites
1. Ensure your local MySQL service is active and running on port `3306`:
   ```bash
   sudo systemctl start mysql
   ```
2. Create your project database target:
   ```sql
   CREATE DATABASE use_auth;
   USE use_auth;
   CREATE TABLE users (
       id INT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(255) NOT NULL UNIQUE,
       password VARCHAR(255) NOT NULL
   );
   ```

### Running the Backend

#### Option A: Running inside the Native Terminal (Recommended)
Open your terminal window, clear old Java memory spaces, navigate to the source directory, compile, and run:
```bash
killall -9 java
cd /home/apiiit123/Music/REACT_LEARN/DAY_02/Back-end
javac -cp .:mysql-connector-j-9.2.0.jar BackendServer.java
java -cp .:mysql-connector-j-9.2.0.jar BackendServer
```

#### Option B: Running with the VS Code Play Button
To ensure the automated play button loads your database driver, verify your `.vscode/launch.json` file contains this exact configuration block:
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Launch Backend Server",
            "request": "launch",
            "mainClass": "BackendServer",
            "classPaths": [
                ".",
                "\${workspaceFolder}/DAY_02/Back-end/mysql-connector-j-9.2.0.jar"
            ]
        }
    ]
}
```
*   Clear out VS Code's hidden directory compiler cache by typing `Ctrl + Shift + P` -> `Java: Clean Java Language Server Workspace` -> `Restart and Delete`.
*   Open `BackendServer.java` and hit the **Play Button** on the top right.

### Running the Frontend
Open a separate terminal window at your project root and start the web server layout:
```bash
cd /home/apiiit123/Music/REACT_LEARN
npm install
npm start
```
Open `http://localhost:3000` in your web browser, navigate to the registration option to initialize your database user entries, and start exploring!
