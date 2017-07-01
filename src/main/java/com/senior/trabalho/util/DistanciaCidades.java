package com.senior.trabalho.util;

import com.senior.trabalho.esquema.Cidade;

public class DistanciaCidades {
    public final Cidade cidadeA;
    public final Cidade cidadeB;
    public final double distancia;

    public DistanciaCidades(Cidade cidadeA, Cidade cidadeB) {
        this.cidadeA = cidadeA;
        this.cidadeB = cidadeB;
        this.distancia = Haversine.haversine(
                cidadeA.getLatitude(), cidadeA.getLongitude(),
                cidadeB.getLatitude(), cidadeB.getLongitude());
    }
}
