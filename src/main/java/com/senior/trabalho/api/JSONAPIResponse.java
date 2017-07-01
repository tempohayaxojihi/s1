package com.senior.trabalho.api;

import com.fasterxml.jackson.annotation.*;
import org.glassfish.jersey.server.JSONP;

import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JSONAPIResponse {
    //TODO: serialização condicional (http://www.baeldung.com/jackson-serialize-field-custom-criteria)
    @JsonProperty("message")
    private String message = "";
    @JsonProperty("data")
    private Object data;
    @JsonProperty("errors")
    private List<Object> errors;


    public JSONAPIResponse() {
    }

    public JSONAPIResponse(String message) {
        this.message = message;
    }

    public JSONAPIResponse withError(Object error) {
        addError(error);
        return this;
    }

    public JSONAPIResponse withData(Object data) {
        // TODO: se houver erros, não pode haver data
        this.data = data;
        return this;
    }

    public static Response serverErrorFor(String errorMessage) {
        return serverErrorFor("", errorMessage);
    }

    public static Response serverErrorFor(String message, String errorMessage) {
        JSONAPIResponse apiResponse = new JSONAPIResponse(message).withError(errorMessage);
        return Response.serverError()
                .entity(apiResponse)
                .build();
    }

    public static Response serverOkFor(Object data) {
        return serverOkFor("", data);
    }

    public static Response serverOkFor(String message, Object data) {
        JSONAPIResponse apiResponse = new JSONAPIResponse(message).withData(data);
        return Response.ok()
                .entity(apiResponse)
                .build();
    }

    protected void addError(Object error) {
        // TODO: se houver data não podem haver errors
        if (errors == null)
            errors = new ArrayList<>();
        errors.add(error);
    }
}
