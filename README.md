# BookPortal

<img src='https://i.imgur.com/FTxUSUD.png' width='60%'>

## Introduction
BookPortal is a full-stack Java-based online Library system, utilizing JavaFX for the client-side GUI and a Java socket server with MongoDB on the server-side. The system allows users to log in, sign up, borrow and return items, as well as search and filter available resources.

## Structure
The project follows a client-server architecture:
### Server
Responsible for handling client connections, managing the database, and processing user requests.

<img src='https://i.imgur.com/g8sKnvu.png' width='60%'>

### Client
A JavaFX application that interacts with the user and communicates with the server to perform various actions.

<img src='https://i.imgur.com/cq2oc22.png' width='60%'>

## Features
### Server-Side
* MongoDB: Utilizes MongoDB as the database to store user data and library resources.
* Password Hashing: Implements secure password storage by hashing passwords with salt.
* Maven: Uses Maven as the build tool for easy dependency management and project building.
* Unit Testing: Includes unit tests using JUnit to ensure the robustness of the core business logic.
* JSON Message Exchange: Supports communication between the server and clients using JSON format.
* POJO Codec: Uses POJO codec to map between MongoDB documents and Java classes for seamless database operations.
* Multi-Client Handling: Can handle multiple client connections using multi-threading (socket-based).
* Logging: Uses Log4j as the logging framework to record important events and errors.
* Singleton Service Component: Implements a singleton design pattern for the database service component to ensure only one instance of the service is active.
* Socket-Based Communication: Uses sockets to exchange information between the server and clients.

### Client-Side
* JavaFX GUI: Provides a rich, interactive user interface built with JavaFX.
* User Account Management: Supports login, signup, logout, and password reset functionalities.
* Library Actions: Allows users to borrow and return library items.
Filter, Sort, and Search: Provides options to filter, sort, and search for library items.
* FXML View Configuration: Utilizes FXML files generated from SceneBuilder to configure JavaFX components for a clear separation of view and logic.
* Logging: Uses Log4j as the logging framework to monitor application events and errors.
* Maven: Uses Maven as the build tool to manage dependencies and build the project.
* JSON Message Exchange: Communicates with the server using JSON format to send and receive information.
* Socket-Based Communication: Connects to the server using sockets for data exchange.