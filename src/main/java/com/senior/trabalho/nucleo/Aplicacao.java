package com.senior.trabalho.nucleo;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Aplicacao {
    private static final URI BASE_URI = URI.create("http://localhost:80");
    private static HttpServer Servidor;

    public static void main(String[] args) throws IOException {
        iniciarServidor();
        System.in.read();
        finalizarServidor();
    }

    private static void finalizarServidor() {
        Servidor.shutdown();
    }

    public static void iniciarServidor() {
        final ResourceConfig configuracao =
                new ResourceConfig()
                .packages("com.senior.trabalho");

        Servidor = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, configuracao);
    }


}
