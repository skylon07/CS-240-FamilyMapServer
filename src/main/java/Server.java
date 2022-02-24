import java.io.*;
import java.net.*;

import com.sun.net.httpserver.*;

import handlers.*;

public class Server {
    /** The maximum number of waiting incoming connections to queue */
    private static final int MAX_WAITING_CONNECTIONS = 12;
    /** The embedded HTTPServer to use */
    private HttpServer server;

    /**
     * The main initialization function to run the server
     *
     * @param portNumber is the port number to run the server on
     */
    private void run(String portNumber) {
        System.out.println("Initializing HTTP Server on port " + portNumber);
        try {
            this.server = HttpServer.create(
                new InetSocketAddress(Integer.parseInt(portNumber)),
                MAX_WAITING_CONNECTIONS
            );
        } catch (IOException err) {
            System.out.println("Server failed to initialize:");
            err.printStackTrace();
        }
        // necessary line of code, but no clue what it does...
        server.setExecutor(null);

        System.out.println("Creating contexts");
        server.createContext("/user/register", new RegisterHandler());
        server.createContext("/user/login", new LoginHandler());
        server.createContext("/fill", new FillHandler());
        server.createContext("/load", new LoadHandler());
        server.createContext("/person", new PersonHandler());
        server.createContext("/event", new EventHandler());
        server.createContext("/", new FileHandler());

        System.out.println("Starting server");
        server.start();
        System.out.println("Server initialized successfully!");
    }

    public static void main(String[] args) {		
		String portNumber = args[0];
		Server server = new Server();
        server.run(portNumber);
	}
}
