package es.ulpgc;

import es.ulpgc.api.ApiServer;
import es.ulpgc.config.Config;
import es.ulpgc.indexer.IndexerController;

public class App {
    public static void main(String[] args) {
        try {
            Config config = new Config("application.properties");
            int port = Integer.parseInt(config.getProperty("server.port"));
            System.out.println("[App] Configuration loaded. Starting Indexer microservice...");

            IndexerController controller = new IndexerController(config);

            ApiServer apiServer = new ApiServer(port, controller);
            apiServer.start();

            System.out.println("[App] Indexer microservice running on port " + port + " ");
            System.out.println("[App] Available endpoints:");
            System.out.println("  POST /index/update/{book_id}");
            System.out.println("  POST /index/rebuild");
            System.out.println("  GET  /index/status");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                apiServer.stop();
                System.out.println("[App] Microservice stopped gracefully.");
            }));

        } catch (Exception e) {
            System.err.println("[App] Failed to start microservice: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
