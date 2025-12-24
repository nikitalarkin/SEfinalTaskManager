Task Management System
Contents

Overview

Roles and Access

Technologies

Data Model

Running the Application

Authentication

API Testing

Project Scope

Overview

Task Management System is a backend REST API for managing tasks inside a company.
The project is implemented as a server-side application without a user interface.
It allows users to work with projects, tasks and comments with role-based access control.

Roles and Access

The system supports three user roles: USER, MANAGER and ADMIN.
Regular users can participate in projects, work with tasks and add comments.
Managers can create projects and manage tasks inside them.
Administrators have full access, including user and role management.

Technologies

The application is built using Java 17 and Spring Boot.
Spring Security is used for authentication and authorization with HTTP Basic Authentication.
PostgreSQL is used as the database.
Liquibase manages database schema migrations.
DTOs and MapStruct are used to separate entities from API models.
JUnit and Mockito are used for unit testing.

Data Model

The main entities of the system are User, Role, Project, Task and Comment.
Users can have multiple roles.
Projects are created by managers or administrators and contain multiple tasks.
Tasks belong to projects, have statuses and priorities, and can be assigned to users.
Comments are attached to tasks and created by users.

Running the Application

Java 17 and PostgreSQL are required to run the project.
A PostgreSQL database must be created and configured in the application configuration file.
After starting the application, Liquibase automatically creates all required tables.

Authentication

Authentication is implemented using HTTP Basic Authentication.
Users register with email and password and then authenticate using the same credentials.
Roles are used to restrict access to management and administrative operations.

API Testing

The project does not include a frontend interface.
All API functionality can be tested using the provided Postman collection.
The collection contains example requests for authentication, projects, tasks and comments.

Project Scope

This project was developed as part of a university Software Engineering course.
The implementation fully satisfies the technical requirements of the assignment.