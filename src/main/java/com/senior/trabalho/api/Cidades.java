package com.senior.trabalho.api;

import com.google.common.collect.ImmutableMap;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.senior.trabalho.api.helpers.APIHelper;
import com.senior.trabalho.esquema.Cidade;
import com.senior.trabalho.nucleo.Aplicacao;
import com.senior.trabalho.util.DistanciaCidades;
import com.senior.trabalho.util.SQLHelper;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/api/cidades")
public class Cidades {
    @POST
    @Path("/importar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response importarCidades(InputStream entrada) throws IOException, SQLException {
        final CsvParserSettings configuracao = new CsvParserSettings();
        configuracao.setLineSeparatorDetectionEnabled(true);
        configuracao.setQuoteDetectionEnabled(true);
        configuracao.setHeaderExtractionEnabled(true);

        BeanListProcessor<Cidade> processador = new BeanListProcessor<>(Cidade.class);
        configuracao.setRowProcessor(processador);

        new CsvParser(configuracao).parseAll(entrada);
        List<Cidade> cidades = processador.getBeans();

        try {
            TransactionManager.callInTransaction(Aplicacao.Conexao, (Callable<Void>) () -> {
                for (Cidade cidade : cidades)
                    Cidade.getDAO().create(cidade);
                return null;
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }

        return JSONAPIResponse.serverOkFor(cidades.size());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response adicionarCidade(Cidade cidade) throws IOException, SQLException {
        try {
            Cidade.getDAO().create(cidade);
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }
        return Response.created(URI.create(
                    Aplicacao.BASE_URI.toString() + "/api/cidades/" + String.valueOf(cidade.getCodigoIBGE())))
                .build();
    }

    @PUT
    @Path("/{codigoIBGE}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response adicionarCidade(Cidade cidade, @PathParam("codigoIBGE") long codigoIBGE) throws IOException, SQLException {
        Boolean criado = false;
        try {
            Cidade cidadeExistente =
                    Cidade.getDAO().queryBuilder()
                            .where().eq("codigoibge", codigoIBGE)
                            .queryForFirst();
            if (cidadeExistente == null) {
                Cidade.getDAO().create(cidade);
                criado = true;
            } else {
                cidade.setId(cidadeExistente.getId());
                Cidade.getDAO().update(cidade);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }
        if (criado)
            return Response.created(URI.create(
                    Aplicacao.BASE_URI.toString() + "/api/cidades/" + String.valueOf(cidade.getCodigoIBGE())))
                    .build();
        return Response.ok().build();
    }

    @DELETE
    @Path("/{codigoIBGE}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removerCidade(@PathParam("codigoIBGE") long codigoIBGE) throws IOException, SQLException {
        try {
            DeleteBuilder<Cidade, String> sql = Cidade.getDAO().deleteBuilder();
            sql.where().eq("codigoibge", codigoIBGE);
            sql.delete();

            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }
    }

    @GET
    @Path("/{codigoIBGE}")
    public Response obterCidade(@PathParam("codigoIBGE") long codigoIBGE) {
        Cidade cidade;
        try {
            cidade = Cidade
                    .getDAO()
                    .queryBuilder()
                    .where().eq("codigoibge", codigoIBGE)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }

        if (cidade == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return JSONAPIResponse.serverOkFor(cidade);
    }

    @GET
    public Response obterCidades(@DefaultValue("") @QueryParam("criterio") final String criterio,
                                 @DefaultValue("") @QueryParam("criterioValor") String criterioValor) {
        List<Cidade> cidades;
        try {
            QueryBuilder<Cidade, String> query = Cidade
                    .getDAO()
                    .queryBuilder();

            if (!criterio.isEmpty()) {
                Optional<Field> campo = APIHelper.obterCampoPorNomeCSV(criterio, Cidade.class);
                if (!campo.isPresent())
                    return JSONAPIResponse.serverErrorFor("Critério inválido");

                if (criterioValor.isEmpty())
                    return JSONAPIResponse.serverErrorFor("Valor de critério inválido");

                Object valorBusca = SQLHelper.converterStringParaTipoCampo(criterioValor, campo.get());

                //NOTE: campo direto do nome da classe; deveria da prioridade a fieldname de @DatabaseField se declarado
                query.where().like(campo.get().getName().toLowerCase(), valorBusca);
            }

            cidades = query.query();
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        } catch (ClassCastException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor("Valor incompatível com coluna");
        }

        //TODO: 404 é certo nesse contexto?
        if (cidades.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();
        return JSONAPIResponse.serverOkFor(cidades);
    }

    @GET
    @Path("/distintas")
    public Response obterCidadesDistintas(@DefaultValue("") @QueryParam("criterio") final String criterio,
                                          @DefaultValue("") @QueryParam("criterioValor") String criterioBusca,
                                          @DefaultValue("") @QueryParam("criterioValor") String criterioBuscaValor) {
        try {
            QueryBuilder<Cidade, String> query = Cidade
                    .getDAO()
                    .queryBuilder();

            if (criterio.isEmpty())
                //NOTE: o correto seria outro status code pra entradas inválidas
                return JSONAPIResponse.serverErrorFor("Critério faltante.");

            Optional<Field> campoCriterio = APIHelper.obterCampoPorNomeCSV(criterio, Cidade.class);
            if (!campoCriterio.isPresent())
                return JSONAPIResponse.serverErrorFor("Critério inválido");


            if (!criterioBusca.isEmpty()) {
                Optional<Field> campoCriterioBusca = APIHelper.obterCampoPorNomeCSV(criterio, Cidade.class);
                if (!campoCriterioBusca.isPresent())
                    return JSONAPIResponse.serverErrorFor("Critério de busca inválido");

                if (criterioBuscaValor.isEmpty()) {
                    return JSONAPIResponse.serverErrorFor("Valor de critério de busca inválido");
                }
                Object valorBusca = SQLHelper.converterStringParaTipoCampo(criterioBuscaValor, campoCriterio.get());

                //NOTE: campo direto do nome da classe; deveria da prioridade a fieldname de @DatabaseField se declarado
                query.where().like(campoCriterioBusca.get().getName().toLowerCase(), valorBusca);
            }

            //NOTE: campo direto do nome da classe; deveria da prioridade a fieldname de @DatabaseField se declarado
            long qtd = query.countOf("DISTINCT "+  campoCriterio.get().getName().toLowerCase());
            return JSONAPIResponse.serverOkFor(ImmutableMap.of("quantidade", qtd));
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }
    }

    @GET
    @Path("/nomes")
    public Response obterNomes(@QueryParam("criterio") String criterio, @QueryParam("valor") String valor) {
        List<Cidade> cidades;
        criterio = criterio.toLowerCase();
        if (!criterio.equals("uf"))
            return JSONAPIResponse.serverErrorFor("Critério inválido");

        try {
            cidades = Cidade
                    .getDAO()
                    .queryBuilder()
                    //NOTE: HATEOAS
                    .selectColumns("nome")
                    .where().like("uf", valor)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }

        List<String> nomes = cidades.stream().map(x -> x.getNome()).collect(Collectors.toList());

        return JSONAPIResponse.serverOkFor(nomes);
    }

    @GET
    @Path("/capitais")
    public Response obterCapitais() {
        List<Cidade> capitais;
        try {
            capitais = Cidade
                    .getDAO()
                    .queryBuilder()
                    .where().eq("ehcapital", true)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }

        capitais.sort(Comparator.comparing(Cidade::getNome));

        return JSONAPIResponse.serverOkFor(capitais);
    }

    @GET
    @Path("/estatisticas")
    @Produces(MediaType.APPLICATION_JSON + ";charset=ANSI")
    public Response obterEstatisticas(@DefaultValue("") @QueryParam("criterio") String criterio /*enum?*/, @DefaultValue("") @QueryParam("valor") String valor) {
        Long quantidade = 0l;

        try {
            QueryBuilder<Cidade, String> query = Cidade.getDAO().queryBuilder();
            if (criterio.toLowerCase().equals("uf"))
                quantidade = query.where().like("uf", valor).countOf();
            else if (criterio.isEmpty())
                quantidade = query.countOf();
            else
                //TODO: encoding
                return JSONAPIResponse.serverErrorFor("Critério inválido.");
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }

        ImmutableMap<String, Object> dados = ImmutableMap.of("quantidade", quantidade);
        return JSONAPIResponse.serverOkFor(dados);
    }

    @GET
    @Path("/estatisticas/estados")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obterEstatisticasEstados() {
        List<String[]> dados;
        try {
            dados = Cidade.getDAO()
                    .queryBuilder()
                    .selectRaw("uf", "COUNT(*)")
                    .groupBy("uf")
                    .orderBy("uf", true)
                    .queryRaw().getResults();
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }

        if (dados.size() == 0)
            return JSONAPIResponse.serverErrorFor("Dados não encontrados");

        Map<String, Integer> resultado = new LinkedHashMap<>();
        for (String[] d: dados)
            resultado.put(d[0], Integer.parseInt(d[1]));

        return JSONAPIResponse.serverOkFor(resultado);
    }

    @GET
    @Path("/estatisticas/extremos/estados")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obterEstatisticasExtremosEstados() {
        List<String[]> dados;
        try {
            dados = Cidade.getDAO().queryRaw(
                "WITH eqtd (uf, qtd) AS (SELECT uf, COUNT(*) FROM cidades GROUP BY uf) " +
                "SELECT * FROM eqtd WHERE qtd = (SELECT MIN(qtd) FROM eqtd) " +
                "UNION ALL " +
                "SELECT * FROM eqtd WHERE qtd = (SELECT MAX(qtd) FROM eqtd) "
            ).getResults();
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }

        if (dados.size() == 0)
            return JSONAPIResponse.serverErrorFor("Dados não encontrados");

        assert dados.size() == 2;

        String[] menor = dados.get(0);
        String[] maior = dados.get(1);
        Object[] resposta = new Object[2];
        resposta[0] =
                ImmutableMap.of("menor",
                    ImmutableMap.of("uf", menor[0], "quantidade", menor[1]));
        resposta[1] =
                ImmutableMap.of("maior",
                        ImmutableMap.of("uf", maior[0], "quantidade", maior[1]));

        return JSONAPIResponse.serverOkFor(resposta);
    }

    @GET
    @Path("/estatisticas/extremos/distancias")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obterEstatisticasExtremosDistancias() {
        List<Cidade> cidades;
        try {
            cidades = Cidade.getDAO().queryBuilder().selectColumns("nome", "latitude", "longitude").query();
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }

        Optional<DistanciaCidades> d2Cidades = cidades.parallelStream()
                .map(x -> cidades.parallelStream().map(y -> new DistanciaCidades(x, y))
                        .max(Comparator.comparing(y -> y.distancia)))
                .flatMap(x -> x.isPresent() ? Stream.of(x.get()) : Stream.empty())
                .max(Comparator.comparing(x -> x.distancia));

        if (!d2Cidades.isPresent())
            return JSONAPIResponse.serverErrorFor("Sem dados");

        Cidade cidadeA;
        Cidade cidadeB;
        try {
            cidadeA = Cidade.getDAO().queryForSameId(d2Cidades.get().cidadeA);
            cidadeB = Cidade.getDAO().queryForSameId(d2Cidades.get().cidadeB);
        } catch (SQLException e) {
            e.printStackTrace();
            return JSONAPIResponse.serverErrorFor(
                    SQLHelper.getSQLExceptionCauseMessage(e)
            );
        }

        Map<String, Object> dados = ImmutableMap.of(
                "distancia", d2Cidades.get().distancia,
                "cidadeA", cidadeA,
                "cidadeB", cidadeB
        );

        return JSONAPIResponse.serverOkFor(dados);
    }

    @GET
    @Path("/teste")
    @Produces(MediaType.APPLICATION_JSON)
    public Response teste() {
        return Response.status(Response.Status.FOUND).entity(
                new JSONAPIResponse("OK")
        ).build();
    }
}