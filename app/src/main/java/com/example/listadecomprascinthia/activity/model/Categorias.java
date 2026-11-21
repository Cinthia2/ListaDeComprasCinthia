package com.example.listadecomprascinthia.activity.model;

import androidx.annotation.ColorRes;

import com.example.listadecomprascinthia.R;

/**
 * Centraliza os nomes das categorias e a cor de cada uma,
 * para não repetir as Strings em vários lugares do app.
 */
public final class Categorias {

    public static final String ALIMENTICIOS = "Produtos Alimentícios";
    public static final String LIMPEZA = "Produtos de Limpeza";
    public static final String HIGIENE = "Produtos de Higiene Pessoal";
    public static final String OUTROS = "Outros";

    /** Ordem em que as categorias aparecem na tela e no modal. */
    public static final String[] TODAS = {ALIMENTICIOS, LIMPEZA, HIGIENE};

    private Categorias() {
    }

    @ColorRes
    public static int corDe(String categoria) {
        if (ALIMENTICIOS.equals(categoria)) return R.color.cat_alimenticios;
        if (LIMPEZA.equals(categoria)) return R.color.cat_limpeza;
        if (HIGIENE.equals(categoria)) return R.color.cat_higiene;
        return R.color.cat_outros;
    }
}
