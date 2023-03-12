package com.example.listadecomprascinthia.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.listadecomprascinthia.R;
import com.example.listadecomprascinthia.activity.adapter.Adapter;
import com.example.listadecomprascinthia.activity.adapter.CustomAdapter;
import com.example.listadecomprascinthia.activity.model.Produto;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {
    private RecyclerView recyclerView;
    ListView simpleList;
    List<Produto> listaProdutos = new ArrayList<Produto>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.criarProdutos();
        simpleList = (ListView) findViewById(R.id.simpleListView);
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), listaProdutos);
        simpleList.setAdapter(customAdapter);

        CheckBox binding=findViewById(R.id.marcar_todos);
        binding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "MARCAR TODOS CLICADO ", Toast.LENGTH_SHORT).show();
               if (binding.isChecked()){
                for(int i=0;i<listaProdutos.size();i++){
                 listaProdutos.get(i).setTem(true);
                }}else{

                    for(int i=0;i<listaProdutos.size();i++){
                        listaProdutos.get(i).setTem(false);
                    }
                }
                customAdapter.notifyDataSetChanged();
            }
        });

/*
        Button addBtn =findViewById(R.id.add_produto);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Produto produto = new Produto("Categoria", "Nome", false);
                // listaProdutos.add(0,produto);
                Toast.makeText(getApplicationContext(), "Depois adiciono produto ", Toast.LENGTH_SHORT).show();
            }
        });*/

       /* CheckBox binding=findViewById(R.id.marcar_todos);
        binding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "MARCAR TODOS CLICADO ", Toast.LENGTH_SHORT).show();
                customAdapter.setAllChecked(binding.isChecked());
                customAdapter.notifyDataSetChanged();
            }
        });*/
    }


    public void criarProdutos() {

        String[] categorias = {"Produtos Alimentícios", "Produtos de Limpeza", "Produtos de Higiene Pessoal"};


        Produto produto = new Produto(categorias[0], "Arroz", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Feijão", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Açúcar", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Farofa", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Enlatados menos a ervilha", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Café", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Filtro de Café", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Macarrão e Espaguete", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Extrato de Tomate", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Arisco", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Orégano", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Caldo de Knor", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Sal", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Óleo", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Alho", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Tomate", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Batata", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Cebola", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Chá", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Pipoca", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Margarina", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Ovos", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Leite", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Milho", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Selela", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Calabresa", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Salame", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Sucos", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Bolachas", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Nescau", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Erva de tererê", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Trigo", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Pepino", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Maionese", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Mortadela", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Ração do Garfield", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Vinagre", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[0], "Sucrilho", false);
        this.listaProdutos.add(produto);


        produto = new Produto(categorias[1], "Detergente", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[1], "Bom bril", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[1], "Omo", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[1], "Pinho Sol", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[1], "Kiboa", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[1], "Soda", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[1], "Luvas", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[1], "Álcool", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[1], "Sabão em barra", false);
        this.listaProdutos.add(produto);


        produto = new Produto(categorias[2], "Sabonete", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[2], "Skala", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[2], "Buchas", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[2], "Escova de chão", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[2], "Papel higiênico", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[2], "Pasta de dentes", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[2], "Sacos de lixo", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[2], "Enxaguante bucal", false);
        this.listaProdutos.add(produto);

        produto = new Produto(categorias[2], "Amaciante", false);
        this.listaProdutos.add(produto);

       ///listaProdutos.get(0).setTem(true);


    }

}
