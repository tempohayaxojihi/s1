package com.senior.trabalho.api;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import javax.ws.rs.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Path("/api/cidades")
public class Cidades {
    @POST
    public String inserirCidades(InputStream entrada) throws IOException {
            final CsvParserSettings configuracao = new CsvParserSettings();
            configuracao.setLineSeparatorDetectionEnabled(true);
            configuracao.getFormat().setLineSeparator("\n");
            configuracao.setQuoteDetectionEnabled(true);
            configuracao.setHeaderExtractionEnabled(true);

            final CsvParser analisador = new CsvParser(configuracao);

            List<String[]> linhas = analisador.parseAll(entrada);

            return String.valueOf(linhas.size());
    }

    @GET
    @Path("/teste")
    public String teste() {
        return "OK";
    }
}