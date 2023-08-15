# EECS Movie Database API

## Description

This project provides a RESTful API to interact with a movie database. It leverages the Neo4j graph database to store and manage relationships between actors, movies, and other related entities. The application is written in Java and uses the `com.sun.net.httpserver.HttpServer` class to handle HTTP requests.

## Features

- **Add Actor**: Add a new actor to the database.
- **Add Movie**: Add a new movie to the database.
- **Add Relationship**: Add a relationship between entities in the database.
- **Get Actor**: Retrieve details about a specific actor.
- **Get Movie**: Retrieve details about a specific movie.
- **Has Relationship**: Check if a relationship exists between entities.
- **Compute Bacon Number**: Compute the Bacon number for a given actor.
- **Compute Bacon Path**: Compute the path of relationships leading to Kevin Bacon.

## Prerequisites

- Java Development Kit (JDK)
- Neo4j Database Server

## Configuration

The application is configured to connect to a Neo4j database at `bolt://localhost:7687` with the username `neo4j` and password `12345678`. Make sure the Neo4j server is running and accessible at this address.

## Setup and Running

1. **Compile the Code**: Navigate to the project directory and compile the code using your preferred Java build tool.
2. **Start the Application**: Run the `App` class, which will start the HTTP server on port 8080.
3. **Access the API**: The API endpoints can be accessed at `http://localhost:8080/api/v1/`.

## Testing

**Note**: The tests provided for this project were made using **JUnit** - NOT **Robot**.

Navigate to the `/src/test/java/` folder and run the tests from there.

## API Endpoints

Here's a brief description of the available API endpoints:

- `GET /`: Root endpoint.
- `POST /api/v1/addActor`: Add a new actor.
- `POST /api/v1/addMovie`: Add a new movie.
- `POST /api/v1/addRelationship`: Add a relationship.
- `GET /api/v1/getActor`: Get an actor's details.
- `GET /api/v1/getMovie`: Get a movie's details.
- `GET /api/v1/hasRelationship`: Check a relationship.
- `GET /api/v1/computeBaconNumber`: Compute Bacon number.
- `GET /api/v1/computeBaconPath`: Compute Bacon path.

## Logging

The application uses Java's built-in logging framework. Errors, such as issues starting the server, are logged at the SEVERE level.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Acknowledgments

- York University EECS Department
- Neo4j Community
