package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import ca.yorku.eecs.api.get.*;
import ca.yorku.eecs.api.put.AddActorHandler;
import ca.yorku.eecs.api.put.AddMovieHandler;
import ca.yorku.eecs.api.put.AddRelationshipHandler;
import com.sun.net.httpserver.HttpServer;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

/**
 * The App class sets up and runs the HTTP server.
 * It sets up the routes for the different API endpoints and starts the server.
 *
 * @since 2023-08-07
 */
public class App {

	/**
	 * The port on which the server will listen.
	 */
	static int PORT = 8080;

	/**
	 * The main method sets up and starts the HTTP server.
	 *
	 * @param args the command line arguments. This is not used.
	 * @throws IOException if there is an error setting up the server.
	 */
	public static void main(String[] args) throws IOException {
		// Creating the server
		HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

		// Setting up the database driver
		Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "12345678"), Config.build().withoutEncryption().toConfig());

		// Setting up the PUT endpoints
		server.createContext("/api/v1/addActor", new AddActorHandler(driver));
		server.createContext("/api/v1/addMovie", new AddMovieHandler(driver));
		server.createContext("/api/v1/addRelationship", new AddRelationshipHandler(driver));

		// Setting up the GET endpoints
		server.createContext("/api/v1/getActor", new GetActorHandler(driver));
		server.createContext("/api/v1/getMovie", new GetMovieHandler(driver));
		server.createContext("/api/v1/hasRelationship", new HasRelationshipHandler(driver));
		server.createContext("/api/v1/computeBaconNumber", new ComputeBaconNumberHandler(driver));
		server.createContext("/api/v1/computeBaconPath", new ComputeBaconPathHandler(driver));

		// Setting the server's executor
		server.setExecutor(Executors.newCachedThreadPool());

		// Starting the server
		server.start();

		// Printing a message to indicate that the server has started
		System.out.printf("Server started on port %d...\n", PORT);
	}
}
