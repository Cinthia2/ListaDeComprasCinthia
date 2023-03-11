package com.example.listadecomprascinthia.activity.model;

public class Produto {




    private String categoria_produto;
    private String nome_produto;
    private boolean  tem;
    private int position;

    public Produto(){

    }
    public Produto(String categoria_produto, String nome_produto, boolean tem) {
        this.categoria_produto = categoria_produto;
        this.nome_produto = nome_produto;
        this.tem = tem;

    }


    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    public String getNome_produto() {
        return nome_produto;
    }

    public void setNome_produto(String nome_produto) {
        this.nome_produto = nome_produto;
    }

    public boolean isTem() {
        return tem;
    }

    public void setTem(boolean tem) {
        this.tem = tem;
    }
    public String getCategoria_produto() {
        return categoria_produto;
    }

    public void setCategoria_produto(String categoria_produto) {
        this.categoria_produto = categoria_produto;
    }
}
//https://github.com/Oziomajnr/RecyclerViewCheckBoxExample2/blob/with-sparse-boolean-array/app/src/main/java/ogbe/ozioma/com/recyclerviewcheckboxexample/Adapter.java