package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import org.neo4j.driver.v1.*;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "12345678"), Config.build().withoutEncryption().toConfig());
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        // TODO: two lines of code are expected to be added here
        // please refer to the HTML server example
        server.createContext("/api/v1/addActor", new AddActorHandler(driver));
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
