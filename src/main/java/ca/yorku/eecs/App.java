package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

/**
 * The main application class for the server.
 * <p>
 * This class sets up a server listening on a specific port and creates handlers for specific paths.
 * It also establishes a connection with the Neo4j database.
 * </p>
 *
 * @since 2023-08-06
 */
public class App {

	/**
	 * The port number where the server will listen for requests.
	 */
	private static final int PORT = 8080;

	/**
	 * The main method for the application.
	 * <p>
	 * This method creates a new HTTP server listening on the specified port, establishes a connection with the
	 * Neo4j database, and assigns handlers to specific paths. Once everything is set up, it starts the server.
	 * </p>
	 *
	 * @param args Command-line arguments. These are not used in this application.
	 * @throws IOException If an I/O error occurs while setting up the server or the database connection.
	 */
	public static void main(String[] args) throws IOException {
		// Create the server
		HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

		// Set up the Neo4j driver
		Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "12345678"), Config.build().withoutEncryption().toConfig());

		// Create contexts and assign handlers
		server.createContext("/api/v1/addActor", new AddActorHandler(driver));
		server.createContext("/api/v1/addMovie", new AddMovieHandler(driver));
		server.createContext("/api/v1/addRelationship", new AddRelationshipHandler(driver));

		// Set the executor
		server.setExecutor(Executors.newCachedThreadPool());

		// Start the server
		server.start();

		System.out.printf("Server started on port %d...\n", PORT);
	}
}
