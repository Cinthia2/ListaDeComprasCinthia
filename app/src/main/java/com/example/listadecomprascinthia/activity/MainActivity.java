package com.example.listadecomprascinthia.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.listadecomprascinthia.R;
import com.example.listadecomprascinthia.activity.adapter.CustomAdapter;
import com.example.listadecomprascinthia.activity.model.Produto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private boolean modoRemocao = false;
    private RecyclerView recyclerView;
    ListView simpleList;
    List<Produto> listaProdutos = new ArrayList<Produto>();
    //Transformar o adapter em variável global
    private CustomAdapter customAdapter;
    //variável para armazenar a escolha do usuário e manter esta informação em um arquivo, mesmo se o
    //usuário fechar o aplicativo Dados para salvar no arquivo TESTE
    private static final String ARQUIVO_PREFERENCIA = "ArquivoPreferencia";
    private TextView textResultado;

    private CheckBox checkboxTemid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this.criarProdutos();

        //label de teste saída das coisas
        textResultado = (TextView)findViewById(R.id.resultado);

        simpleList = (ListView) findViewById(R.id.simpleListView);
        //O Adapter fica acessível em toda Activity
        //chamando o construtor com a callback no último parâmetro para salvar o unchecked
        customAdapter = new CustomAdapter(getApplicationContext(), listaProdutos,  () -> salvarLista());
        simpleList.setAdapter(customAdapter);

        removerItem();
       //recuperaProdutos(listaProdutos);

        recuperaProdutos(listaProdutos);
        //Se a lista inicial estiver vazia cria somente uma única vez os produtos inicializados
        if(listaProdutos.isEmpty()){
            criarProdutos();
            salvarLista(); // SALVANDO APÓS CRIAR NA MEMÓRIA
        }

        //Função para chamar o botão para abrir o modal
        novo_produto();
        //Função para salvar na memória do dispositivo


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

                salvarLista(); // SALVANDO A CADA CHECK
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

    private void novo_produto(){
        //botão +
        FloatingActionButton buttonAdicionarProduto = findViewById(R.id.btnNovoProduto);
        buttonAdicionarProduto.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(),
                    "Novo produto",
                    Toast.LENGTH_SHORT).show();
            // abrir o modal para adicionar mais produtos
            abrirModalProduto();
        });
    }
    //função para salvar o arquivo na memória local do dispositivo


    private void salvarLista() {
        SharedPreferences preferences = getSharedPreferences(ARQUIVO_PREFERENCIA, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(listaProdutos);

        editor.putString("lista_produtos_json", json);
        editor.apply();
    }

    private void removerItem() {

        Button buttonRemover = findViewById(R.id.id_remover);

        buttonRemover.setOnClickListener(v -> {

            if (!modoRemocao) {
                modoRemocao = true;
                customAdapter.setModoRemocao(true);
                customAdapter.notifyDataSetChanged();

                Toast.makeText(this, "Selecione os itens para remover", Toast.LENGTH_SHORT).show();
            } else {

                List<Produto> paraRemover = new ArrayList<>();

                for (Produto p : listaProdutos) {
                    if (p.isSelecionado()) {
                        paraRemover.add(p);
                    }
                }

                listaProdutos.removeAll(paraRemover);

                salvarLista();

                modoRemocao = false;
                customAdapter.setModoRemocao(false);

                customAdapter.notifyDataSetChanged();


            Toast.makeText(getApplicationContext(),
                    "Itens removidos: " + paraRemover.size(),
                    Toast.LENGTH_SHORT).show();
            }
        });


    }

    //Função para abrir o modal de adição de produtos
    private void abrirModalProduto() {

        View view = getLayoutInflater().inflate(R.layout.modal_adicionar_produto, null);

        Spinner spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        EditText nomeProduto = view.findViewById(R.id.editNomeProduto);
        CheckBox checkComprado = view.findViewById(R.id.checkComprado);
        Button btnAdicionar = view.findViewById(R.id.btnAdicionar);

        String[] categorias = {
                "Produtos Alimentícios",
                "Produtos de Limpeza",
                "Produtos de Higiene Pessoal"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categorias
        );

        spinnerCategoria.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnAdicionar.setOnClickListener(v -> {

            String categoria = spinnerCategoria.getSelectedItem().toString();
            String nome = nomeProduto.getText().toString();
            boolean comprado = checkComprado.isChecked();

            Produto produto = new Produto(categoria, nome, comprado);
            this.listaProdutos.add(produto);
            salvarLista();
            customAdapter.notifyDataSetChanged(); // atualiza ListView automático


            Toast.makeText(getApplicationContext(),
                    "Produto "+ produto.getNome_produto() +" adicionado!",
                    Toast.LENGTH_LONG).show();

            dialog.dismiss();


        });


        dialog.show();
    }

    private void recuperaProdutos(List<Produto> listaProdutos) {

        SharedPreferences preferences = getSharedPreferences(ARQUIVO_PREFERENCIA, MODE_PRIVATE);

        String json = preferences.getString("lista_produtos_json", null);

        if (json != null) {

            Gson gson = new Gson();

            Type type = new TypeToken<List<Produto>>() {}.getType();

            List<Produto> listaRecuperada = gson.fromJson(json, type);

            listaProdutos.clear();
            listaProdutos.addAll(listaRecuperada);
        }
    }

    public void criarProdutos() {


        System.out.println("estado da minha lista de produtos "+ this.listaProdutos);


        String[] categorias = {"Produtos Alimentícios", "Produtos de Limpeza", "Produtos de Higiene Pessoal"};


        Produto produto = new Produto(categorias[0], "Arroz", false);
        this.listaProdutos.add(produto);
        System.out.println("estado da minha lista de produtos "+ this.listaProdutos);

        for (Produto p : listaProdutos) {
            System.out.println("Produto Main: "+p.getNome_produto());
        }

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



