# Movie Recommendation System (MRS)

A modern, scalable backend application built with **Spring Boot** and **Java**. This project implements a structured architecture to manage movie data, user interactions, and recommendation logic.

---

## 🚀 Features

* **Layered Architecture:** Clear separation of concerns using Controller, Service, and Repository layers.
* **Data Transfer Objects (DTO):** Optimized data flow between layers and API clients.
* **Custom Exception Handling:** Centralized error management for consistent API responses.
* **Standardized Responses:** Uniform JSON response structures across all endpoints.
* **Maven Managed:** Easy dependency management and build automation.

---

## 📂 Project Structure

Based on the `src/main/java/com/mrs/mrs/` directory:

| Package | Description |
| :--- | :--- |
| **config** | Configuration for Security, Beans, and Application settings. |
| **controller** | REST Endpoints handling incoming HTTP requests. |
| **DTO** | Data Transfer Objects for decoupled data handling. |
| **exception** | Custom exception classes and Global Exception Handler. |
| **model** | Database entities (JPA/Hibernate). |
| **repository** | Interfaces for database abstraction and CRUD operations. |
| **response** | Utility classes for standardizing API output. |
| **service** | Core business logic and service interfaces. |

---

## 🛠️ Tech Stack

* **Language:** Java
* **Framework:** Spring Boot
* **Build Tool:** Maven
* **IDE:** Visual Studio Code / Cursor (as seen in screenshot)

---

## ⚙️ Setup

### Prerequisites
* JDK 17 or higher
* Maven 3.x
