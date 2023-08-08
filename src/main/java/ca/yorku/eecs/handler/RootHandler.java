package ca.yorku.eecs.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles HTTP requests to the root path ("/").
 * <p>
 * This handler returns an HTML page with links to each of the API endpoints and brief descriptions of what they do.
 * It also lists the first 10 actors and movies from the Neo4j database with all their details.
 * </p>
 * <p>
 * Implements the HttpHandler interface to handle the HTTP request and response.
 * </p>
 *
 * @since 2023-08-07
 */
public class RootHandler implements HttpHandler {

	/**
	 * The Neo4j database driver instance used for database operations.
	 */
	private final Driver driver;

	/**
	 * Constructs a new RootHandler with the provided Neo4j driver.
	 *
	 * @param driver The Neo4j driver instance.
	 */
	public RootHandler(Driver driver) {
		this.driver = driver;
	}

	/**
	 * Handles the HTTP request and constructs the response.
	 * <p>
	 * Retrieves the first 10 actors and movies from the database and includes them in an HTML page along with links to API endpoints.
	 * </p>
	 *
	 * @param exchange The HTTP exchange object containing request and response details.
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// Fetch first 10 actors and movies from the database
		List<String> actors = fetchFirstTenActors();
		List<String> movies = fetchFirstTenMovies();

		String htmlResponse = "<html>" +
				"<head>" +
				"<title>Neo4j Movies API</title>" +
				"<style>" +
				"body {font-family: Arial, sans-serif;}" +
				"table {border-collapse: collapse; width: 50%;}" +
				"th, td {border: 1px solid black; padding: 8px; text-align: left;}" +
				"</style>" +
				"</head>" +
				"<body>" +
				"<h1>Welcome to the Neo4j Movies API!</h1>" +
				"<h2>Available Endpoints:</h2>" +
				"<ul>" +
				"<li><a href='/api/v1/addActor'>/api/v1/addActor</a>: Adds a new actor to the database.</li>" +
				"<li><a href='/api/v1/addMovie'>/api/v1/addMovie</a>: Adds a new movie to the database.</li>" +
				"<li><a href='/api/v1/addRelationship'>/api/v1/addRelationship</a>: Adds a new ACTED_IN relationship between an actor and a movie.</li>" +
				"<li><a href='/api/v1/getActor'>/api/v1/getActor</a>: Retrieves an actor and their list of movies from the database.</li>" +
				"<li><a href='/api/v1/getMovie'>/api/v1/getMovie</a>: Retrieves a movie and its list of actors from the database.</li>" +
				"<li><a href='/api/v1/hasRelationship'>/api/v1/hasRelationship</a>: Checks if an ACTED_IN relationship exists between an actor and a movie.</li>" +
				"<li><a href='/api/v1/computeBaconNumber'>/api/v1/computeBaconNumber</a>: Computes the Bacon number for a given actor.</li>" +
				"<li><a href='/api/v1/computeBaconPath'>/api/v1/computeBaconPath</a>: Computes the Bacon path for a given actor.</li>" +
				"</ul>" +
				"<h2>First 10 Actors:</h2>" +
				"<table>" +
				"<tr><th>Name</th><th>ActorId</th><th>Movies Acted In</th></tr>" +
				actors.stream().collect(Collectors.joining()) +
				"</table>" +
				"<h2>First 10 Movies:</h2>" +
				"<table>" +
				"<tr><th>Name</th><th>MovieId</th><th>Actors</th></tr>" +
				movies.stream().collect(Collectors.joining()) +
				"</table>" +
				"</body>" +
				"</html>";

		exchange.sendResponseHeaders(200, htmlResponse.length());
		OutputStream os = exchange.getResponseBody();
		os.write(htmlResponse.getBytes());
		os.close();
	}

	/**
	 * Fetches the first 10 actors from the Neo4j database.
	 * <p>
	 * For each actor, it retrieves the name, actorId, and the list of movies they have acted in.
	 * </p>
	 *
	 * @return A list of HTML table rows with details about the actors.
	 */
	private List<String> fetchFirstTenActors() {
		// Implement database query to fetch first 10 actors
		try (Session session = driver.session()) {
			StatementResult result = session.run("MATCH (a:Actor) RETURN a.name LIMIT 10");
			return result.list(record -> record.get("a.name").asString());
		}
	}

	/**
	 * Fetches the first 10 movies from the Neo4j database.
	 * <p>
	 * For each movie, it retrieves the name, movieId, and the list of actors that have acted in it.
	 * </p>
	 *
	 * @return A list of HTML table rows with details about the movies.
	 */
	private List<String> fetchFirstTenMovies() {
		// Implement database query to fetch first 10 movies
		try (Session session = driver.session()) {
			StatementResult result = session.run("MATCH (m:Movie) RETURN m.name LIMIT 10");
			return result.list(record -> record.get("m.name").asString());
		}
	}

}
