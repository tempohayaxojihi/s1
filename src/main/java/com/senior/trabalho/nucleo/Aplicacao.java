package com.senior.trabalho.nucleo;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Aplicacao {
    private static final URI BASE_URI = URI.create("http://localhost:80");
    private static HttpServer Servidor;

    public static void main(String[] args) throws IOException {
        configurarLogGrizzly();
        iniciarServidor();

        System.in.read();

        finalizarServidor();
    }

    public static void configurarLogGrizzly() {
        Logger logger = Logger.getLogger("org.glassfish.grizzly.http.server.HttpHandler");
        logger.setLevel(Level.FINE);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
    }

    private static void finalizarServidor() {
        Servidor.shutdown();
    }

    public static void iniciarServidor() {
        final ResourceConfig configuracao =
                new ResourceConfig()
                .packages("com.senior.trabalho")
                .register(LoggingFeature.class);

        Servidor = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, configuracao);
    }
}
