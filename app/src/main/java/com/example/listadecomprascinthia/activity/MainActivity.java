package com.example.listadecomprascinthia.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.listadecomprascinthia.R;
import com.example.listadecomprascinthia.activity.adapter.Adapter;
import com.example.listadecomprascinthia.activity.adapter.CustomAdapter;
import com.example.listadecomprascinthia.activity.model.Produto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.example.listadecomprascinthia.activity.adapter.CustomAdapter;
public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    ListView simpleList;
    List<Produto> listaProdutos = new ArrayList<Produto>();

    //variável para armazenar a escolha do usuário e manter esta informação em um arquivo, mesmo se o
    //usuário fechar o aplicativo
    //Dados para salvar no arquivo TESTE2
    private static final String ARQUIVO_PREFERENCIA = "ArquivoPreferencia";
    private TextView textResultado;

    private CheckBox checkboxTemid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.criarProdutos();

        textResultado = (TextView)findViewById(R.id.resultado);

        simpleList = (ListView) findViewById(R.id.simpleListView);
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), listaProdutos);
        simpleList.setAdapter(customAdapter);


        salvar(listaProdutos);
        recuperar();
        CheckBox binding = findViewById(R.id.marcar_todos);
        binding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //arquivo que vai ser gravado e modo de gravação 0 somente este aplicativo vai poder ler este arquivo
                //SharedPreferences preferences = getSharedPreferences(ARQUIVO_PREFERENCIA, 0);
                //SharedPreferences.Editor editor = preferences.edit();


                Toast.makeText(getApplicationContext(), "MARCAR TODOS CLICADO ", Toast.LENGTH_SHORT).show();
                if (binding.isChecked()) {
                    for (int i = 0; i < listaProdutos.size(); i++) {
                        listaProdutos.get(i).setTem(true);
                        //salva na chave "tem" o valor "true"
                        //editor.putBoolean("chave_marcar_todos"+listaProdutos.get(i), true);
                        //editor.commit();

                    }

                } else {

                    for (int i = 0; i < listaProdutos.size(); i++) {
                        listaProdutos.get(i).setTem(false);
                        //salva na chave "tem" o valor "false"
                        //editor.putBoolean("chave_marcar_todos"+listaProdutos.get(i), false);
                        //editor.commit();
                    }

                }
                customAdapter.notifyDataSetChanged();
            }
        });




        //recuperar dados salvos
        //SharedPreferences preferences = getSharedPreferences(ARQUIVO_PREFERENCIA, 0);

        //validação se existe a chave tem em preferências
        //if (preferences.contains("chave_marcar_todos")){
            //Caso o preferencias não consiga recuperar o dado armazenado seta como false o checkbox marcar_todos
         //Boolean chave_marcar_todos = preferences.getBoolean("chave_marcar_todos", false);
         //binding.setChecked(chave_marcar_todos);
        //  }else{
        //    System.out.println("Olá não foi salvo o marcar todos");

    }

    private void salvar(List<Produto> listaProdutos) {
        Button buttonSave = findViewById(R.id.id_salvar);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String produto = "produto_";

                Toast.makeText(getApplicationContext(), "VC CLICOU NO BOTAO SALVAR "+listaProdutos.get(0).isTem()+" e "+ produto+String.valueOf(0), Toast.LENGTH_SHORT).show();

                //SharedPreferes é simplesmente um arquivo xml com os dados
                SharedPreferences preferences = getSharedPreferences(ARQUIVO_PREFERENCIA, 0);
                SharedPreferences.Editor editor = preferences.edit();
                //putBoolean(chave, valor)
                editor.putBoolean(produto+String.valueOf(0), listaProdutos.get(0).isTem());
                editor.commit();


            }
        });
    }
    private void recuperar(){
        String produto = "produto_";
        Boolean temproduto;
        //SharedPreferes é simplesmente um arquivo xml com os dados
        SharedPreferences preferences = getSharedPreferences(ARQUIVO_PREFERENCIA, 0);
        temproduto = preferences.getBoolean(produto+String.valueOf(0),false);
        textResultado.setText(String.valueOf(temproduto));
        listaProdutos.get(0).setTem(temproduto);

    }


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
/*
Erro de salvar o mesmo registro várias vezes
    private void saveData(){
        //função que serve para salvar os dados em preferências do usuário em uma lista do tipo json
        SharedPreferences preferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(listaProdutos);
        editor.putString("task list", json);
        editor.apply();

    }

    private void loadData(){
        //função que serve para recuperar o dado salvo em preferências do usuário
        SharedPreferences preferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("task list", null);
        Type type = new TypeToken<ArrayList<Produto>>() {}.getType();
        listaProdutos = gson.fromJson(json, type);

        if (listaProdutos == null){
            listaProdutos = new ArrayList<>();
        }
    }

 */
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



