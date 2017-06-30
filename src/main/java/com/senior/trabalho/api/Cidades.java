package com.senior.trabalho.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/api")
public class Cidades {

    @GET()
    @Path("/teste")
    public String teste() {
        return "Teste.";
    }
}