package es.ulpgc;

import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        }).start(7000);

        new ControlController().registerRoutes(app);
        System.out.println("Control module running on http://localhost:7000");
    }
}