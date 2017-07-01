package com.senior.trabalho.esquema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.senior.trabalho.nucleo.Aplicacao;
import com.univocity.parsers.annotations.Parsed;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@DatabaseTable(tableName = "cidades")
public class Cidade implements Serializable {
    private static Dao<Cidade, String> DAO;

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(unique = true, index =  true)
    @Parsed(field = "ibge_id")
    private long codigoIBGE;

    @DatabaseField()
    @Parsed(field = "uf")
    private String uf;

    @DatabaseField()
    @Parsed(field = "name")
    private String nome;

    @DatabaseField()
    @Parsed(field = "no_accents")
    private String nomeSemAcentos;

    @DatabaseField()
    @Parsed(field = "alternative_names")
    private String nomeAlternativo;

    @DatabaseField()
    @Parsed(field = "microregion")
    private String microRegiao;

    @DatabaseField()
    @Parsed(field = "mesoregion")
    private String mesoRegiao;

    @DatabaseField()
    @Parsed(field = "capital", defaultNullRead = "false")
    private Boolean ehCapital;

    @DatabaseField(columnName = "longitude")
    @Parsed(field = "lon")
    private double longitude;
    @DatabaseField(columnName = "latitude")
    @Parsed(field = "lat")
    private double latitude;

    public static Dao<Cidade, String> getDAO() throws SQLException {
        // documentação ambígua, aparentemente já há um cache na biblioteca
        // http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_2.html#DAO-Setup
        if (DAO == null) {
            DAO = DaoManager.createDao(Aplicacao.Conexao, Cidade.class);
        }
        return DAO;
    }

    public Cidade() {
    }

    public long getCodigoIBGE() {
        return codigoIBGE;
    }

    public void setCodigoIBGE(long codigoIBGE) {
        this.codigoIBGE = codigoIBGE;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Boolean getEhCapital() {
        return ehCapital;
    }

    public void setEhCapital(Boolean ehCapital) {
        this.ehCapital = ehCapital;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNomeSemAcentos() {
        return nomeSemAcentos;
    }

    public void setNomeSemAcentos(String nomeSemAcentos) {
        this.nomeSemAcentos = nomeSemAcentos;
    }

    public String getNomeAlternativo() {
        return nomeAlternativo;
    }

    public void setNomeAlternativo(String nomeAlternativo) {
        this.nomeAlternativo = nomeAlternativo;
    }

    public String getMicroRegiao() {
        return microRegiao;
    }

    public void setMicroRegiao(String microRegiao) {
        this.microRegiao = microRegiao;
    }

    public String getMesoRegiao() {
        return mesoRegiao;
    }

    public void setMesoRegiao(String mesoRegiao) {
        this.mesoRegiao = mesoRegiao;
    }
}
