package ca.yorku.eecs.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * RootHandler is a HTTP handler for the root path ("/") of the server.
 * It displays an HTML page with information about the Neo4j Movies API,
 * including available endpoints and the first 10 actors and movies from the database.
 */
public class RootHandler implements HttpHandler {

	/**
	 * Driver instance for interacting with the Neo4j database.
	 */
	private final Driver driver;

	/**
	 * Constructs a new RootHandler instance with the provided Neo4j driver.
	 *
	 * @param driver the Neo4j driver for database operations.
	 */
	public RootHandler(Driver driver) {
		this.driver = driver;
	}

	/**
	 * Handles HTTP requests sent to the root path ("/").
	 * This method generates an HTML page with information about the API and the first 10 actors and movies.
	 *
	 * @param exchange an HttpExchange instance containing the HTTP request received and the response to be sent.
	 * @throws IOException if an I/O error occurs during the process.
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// Fetch the first 10 actors and movies from the database.
		List<String> actors = fetchFirstTenActors();
		List<String> movies = fetchFirstTenMovies();

		// Construct HTML table content for actors.
		StringBuilder actorTableContent = new StringBuilder();
		for (String actor : actors) {
			actorTableContent.append(actor);
		}

		// Construct HTML table content for movies.
		StringBuilder movieTableContent = new StringBuilder();
		for (String movie : movies) {
			movieTableContent.append(movie);
		}

		// Construct the HTML response.
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
				"<table><tr><th>Name</th><th>ActorId</th><th>Movies Acted In</th></tr>" +
				actorTableContent.toString() +
				"</table>" +
				"<h2>First 10 Movies:</h2>" +
				"<table><tr><th>Name</th><th>MovieId</th><th>Actors</th></tr>" +
				movieTableContent.toString() +
				"</table>" +
				"</body>" +
				"</html>";

		// Send the HTTP response.
		exchange.sendResponseHeaders(200, htmlResponse.length());
		OutputStream os = exchange.getResponseBody();
		os.write(htmlResponse.getBytes());
		os.close();
	}

	/**
	 * Fetches the first 10 actors from the Neo4j database.
	 * For each actor, it retrieves the name, actorId, and the list of movies they have acted in.
	 * It then constructs an HTML table row for each actor.
	 *
	 * @return a List of HTML table rows representing the first 10 actors in the database.
	 */
	private List<String> fetchFirstTenActors() {
		// Open a new session with the Neo4j database.
		try (Session session = driver.session()) {
			// Run a Cypher query to fetch the first 10 actors and their movies.
			StatementResult result = session.run("MATCH (a:Actor) OPTIONAL MATCH (a)<-[:ACTED_IN]-(m:Movie) RETURN a.name AS name, a.actorId AS actorId, collect(m.name) AS movies LIMIT 10");

			// Convert the result into a list of HTML table rows.
			return result.list(record -> {
				// Extract data from the record.
				String name = record.get("name").asString();
				String actorId = record.get("actorId").asString();
				List<String> movies = record.get("movies").asList(Value::asString);

				// Construct an HTML table row for the actor.
				return "<tr><td>" + name + "</td><td>" + actorId + "</td><td>" + (movies.isEmpty() ? "N/A" : String.join(", ", movies)) + "</td></tr>";
			});
		}
	}

	/**
	 * Fetches the first 10 movies from the Neo4j database.
	 * For each movie, it retrieves the name, movieId, and the list of actors who have acted in it.
	 * It then constructs an HTML table row for each movie.
	 *
	 * @return a List of HTML table rows representing the first 10 movies in the database.
	 */
	private List<String> fetchFirstTenMovies() {
		// Open a new session with the Neo4j database.
		try (Session session = driver.session()) {
			// Run a Cypher query to fetch the first 10 movies and their actors.
			StatementResult result = session.run("MATCH (m:Movie) OPTIONAL MATCH (m)<-[:ACTED_IN]-(a:Actor) RETURN m.name AS title, m.movieId AS movieId, collect(a.name) AS actors LIMIT 10");

			// Convert the result into a list of HTML table rows.
			return result.list(record -> {
				// Extract data from the record.
				String title = record.get("title").asString();
				String movieId = record.get("movieId").asString();
				List<String> actors = record.get("actors").asList(Value::asString);

				// Construct an HTML table row for the movie.
				return "<tr><td>" + title + "</td><td>" + movieId + "</td><td>" + (actors.isEmpty() ? "N/A" : String.join(", ", actors)) + "</td></tr>";
			});
		}
	}
}
