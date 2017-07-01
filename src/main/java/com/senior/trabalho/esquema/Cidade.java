package com.senior.trabalho.esquema;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.univocity.parsers.annotations.Parsed;

@DatabaseTable(tableName = "cidades")
public class Cidade {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(index =  true)
    @Parsed(field = "ibge_id")
    private long codigoIBGE;

    public long getCodigoIBGE() {
        return codigoIBGE;
    }

    public void setCodigoIBGE(long codigoIBGE) {
        this.codigoIBGE = codigoIBGE;
    }
}
