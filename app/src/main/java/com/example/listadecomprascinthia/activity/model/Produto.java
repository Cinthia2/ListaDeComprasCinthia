package com.example.listadecomprascinthia.activity.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

/**
 * Produto da lista. Cada produto é um documento na coleção "produtos" do Firestore.
 * Campos salvos no banco: categoria_produto, nome_produto, tem, ordem.
 */
public class Produto {

    // ID do documento no Firestore (preenchido automaticamente ao ler; não é gravado como campo)
    @DocumentId
    private String id;

    private String categoria_produto;
    private String nome_produto;
    private boolean tem;

    // Usado para manter a ordem em que os produtos foram adicionados
    private long ordem;

    // Estado só da tela (modo remoção) — NÃO vai para o banco
    private boolean selecionado;
    private int position;

    // Construtor vazio obrigatório para o Firestore (e o Gson) recriarem o objeto
    public Produto() {
    }

    public Produto(String categoria_produto, String nome_produto, boolean tem) {
        this.categoria_produto = categoria_produto;
        this.nome_produto = nome_produto;
        this.tem = tem;
        this.selecionado = false; // padrão
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getOrdem() {
        return ordem;
    }

    public void setOrdem(long ordem) {
        this.ordem = ordem;
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

    @Exclude
    public boolean isSelecionado() {
        return selecionado;
    }

    @Exclude
    public void setSelecionado(boolean selecionado) {
        this.selecionado = selecionado;
    }

    @Exclude
    public int getPosition() {
        return position;
    }

    @Exclude
    public void setPosition(int position) {
        this.position = position;
    }
}
