package com.senior.trabalho.api;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.misc.TransactionManager;
import com.senior.trabalho.esquema.Cidade;
import com.senior.trabalho.nucleo.Aplicacao;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import javax.ws.rs.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

@Path("/api/cidades")
public class Cidades {
    @POST
    public String inserirCidades(InputStream entrada) throws IOException, SQLException {
        final CsvParserSettings configuracao = new CsvParserSettings();
        configuracao.setLineSeparatorDetectionEnabled(true);
        configuracao.setQuoteDetectionEnabled(true);
        configuracao.setHeaderExtractionEnabled(true);

        BeanListProcessor<Cidade> processador = new BeanListProcessor<>(Cidade.class);
        configuracao.setRowProcessor(processador);

        final CsvParser analisador = new CsvParser(configuracao);

        List<String[]> linhas = analisador.parseAll(entrada);
        List<Cidade> cidades = processador.getBeans();

        Dao<Cidade, String> cidadeDao = DaoManager.createDao(Aplicacao.Conexao, Cidade.class);

        TransactionManager.callInTransaction(Aplicacao.Conexao, (Callable<Void>) () -> {
            for (Cidade cidade: cidades)
                cidadeDao.create(cidade);
            return null;
        });

        return String.valueOf(cidades.size());
    }

    @GET
    @Path("/teste")
    public String teste() {
        return "OK";
    }
}