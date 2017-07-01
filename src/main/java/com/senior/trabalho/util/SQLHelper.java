package com.senior.trabalho.util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.SQLException;

public class SQLHelper {
    public static String getSQLExceptionCauseMessage(SQLException e) {
        if (e.getCause() != null)
            return e.getCause().getMessage();
        return e.getMessage();
    }

    public static Object converterStringParaTipoCampo(String valor, Field campo) {
        Type tipo = campo.getType();

        valor = valor.toLowerCase();
        if (tipo.equals(Boolean.class))
            return valor.equals("true") || valor.equals("s") || valor.equals("1");
        else if (tipo.equals(long.class))
            return Long.parseLong(valor);
        else
            return valor;
    }
}
