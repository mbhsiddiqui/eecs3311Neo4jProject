package ca.yorku.eecs.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

public class RootHandler implements HttpHandler {

	private final Driver driver;

	public RootHandler(Driver driver) {
		this.driver = driver;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		List<String> actors = fetchFirstTenActors();
		List<String> movies = fetchFirstTenMovies();

		StringBuilder actorTableContent = new StringBuilder();
		for (String actor : actors) {
			actorTableContent.append(actor);
		}

		StringBuilder movieTableContent = new StringBuilder();
		for (String movie : movies) {
			movieTableContent.append(movie);
		}

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

		exchange.sendResponseHeaders(200, htmlResponse.length());
		OutputStream os = exchange.getResponseBody();
		os.write(htmlResponse.getBytes());
		os.close();
	}

	private List<String> fetchFirstTenActors() {
		try (Session session = driver.session()) {
			StatementResult result = session.run("MATCH (a:Actor) OPTIONAL MATCH (a)<-[:ACTED_IN]-(m:Movie) RETURN a.name AS name, a.actorId AS actorId, collect(m.name) AS movies LIMIT 10");
			return result.list(record -> {
				String name = record.get("name").asString();
				String actorId = record.get("actorId").asString();
				List<String> movies = record.get("movies").asList(Value::asString);
				return "<tr><td>" + name + "</td><td>" + actorId + "</td><td>" + (movies.isEmpty() ? "N/A" : String.join(", ", movies)) + "</td></tr>";
			});
		}
	}

	private List<String> fetchFirstTenMovies() {
		try (Session session = driver.session()) {
			StatementResult result = session.run("MATCH (m:Movie) OPTIONAL MATCH (m)<-[:ACTED_IN]-(a:Actor) RETURN m.name AS title, m.movieId AS movieId, collect(a.name) AS actors LIMIT 10");
			return result.list(record -> {
				String title = record.get("title").asString();
				String movieId = record.get("movieId").asString();
				List<String> actors = record.get("actors").asList(Value::asString);
				return "<tr><td>" + title + "</td><td>" + movieId + "</td><td>" + (actors.isEmpty() ? "N/A" : String.join(", ", actors)) + "</td></tr>";
			});
		}
	}

}
