package com.senior.trabalho.nucleo;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.senior.trabalho.esquema.Cidade;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Aplicacao {
    public static final URI BASE_URI = URI.create("http://localhost:80");
    private static HttpServer Servidor;
    public static ConnectionSource Conexao;

    public static void main(String[] args) throws Exception {
        configurarLogGrizzly();

        Conexao = inicializarAcessoBD();
        inicializarBD();
        iniciarServidor();

        System.in.read();

        Conexao.close();
        finalizarServidor();
    }

    private static void inicializarBD() throws SQLException {
        TableUtils.createTableIfNotExists(Conexao, Cidade.class);
    }

    private static ConnectionSource inicializarAcessoBD() throws SQLException {
        String url = "jdbc:sqlite:assets/bd.sqlite";
        ConnectionSource fonte = new JdbcConnectionSource(url);
        return fonte;
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
                //.register(JacksonFeature.class)
                .register(LoggingFeature.class);

        Servidor = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, configuracao);
    }
}
