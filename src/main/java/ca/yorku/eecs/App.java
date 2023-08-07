package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "12345678"), Config.build().withoutEncryption().toConfig());

        server.createContext("/api/v1/addActor", new AddActorHandler(driver));
        server.createContext("/api/v1/addMovie", new AddMovieHandler(driver));
        server.createContext("/api/v1/addRelationship", new AddRelationshipHandler(driver));

        server.createContext("/api/v1/getActor", new GetActorHandler(driver));
        server.createContext("/api/v1/getMovie", new GetMovieHandler(driver));

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
