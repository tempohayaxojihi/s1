package com.senior.trabalho.api.helpers;

import com.senior.trabalho.esquema.Cidade;
import com.univocity.parsers.annotations.Parsed;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class APIHelper {
    public static Optional<Field> obterCampoPorNomeCSV(final String nomeCSV, Class classe) {
        return Arrays.stream(Cidade.class.getDeclaredFields()).filter(x -> {
            Parsed p = x.getAnnotation(Parsed.class);
            if (p == null)
                return false;
            return p.field().equals(nomeCSV);
        }).findFirst();
    }
}
