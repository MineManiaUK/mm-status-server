package co.minemania.status.statusPlugin;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class StatusServer {
    private HttpServer _server;

    public StatusServer(int port) throws IOException {
        _server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), -1);
        _server.createContext("/", new TextHandler());
        _server.createContext("/json", new JsonHandler());

        _server.setExecutor(null);
        _server.start();
    }

    private static class TextHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String text = "nya\r\n";
            text += "Go to <a href=\"/json\">/json</a> for the actual data (in JSON format)\r\n";

            exchange.sendResponseHeaders(200, text.length());
            try(OutputStream os = exchange.getResponseBody()) {
                os.write(text.getBytes());
            }

            exchange.close();
        }
    }

    private static class JsonHandler implements HttpHandler {
        private static Gson _gson = new Gson();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            ServerResponse responseData = StatusPlugin.Instance.createResponse();
            String text = _gson.toJson(responseData);

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, text.length());
            try(OutputStream os = exchange.getResponseBody()) {
                os.write(text.getBytes());
            }
        }
    }
}
