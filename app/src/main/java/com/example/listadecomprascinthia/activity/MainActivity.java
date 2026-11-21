package com.example.listadecomprascinthia.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.listadecomprascinthia.R;
import com.example.listadecomprascinthia.activity.adapter.CategoriaAdapter;
import com.example.listadecomprascinthia.activity.model.Categorias;
import com.example.listadecomprascinthia.activity.model.Produto;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private boolean modoRemocao = false;
    List<Produto> listaProdutos = new ArrayList<Produto>();

    // Lista agrupada por categoria (cada categoria tem sua sublista de produtos)
    private ExpandableListView listaCategorias;
    private CategoriaAdapter categoriaAdapter;

    //variável para armazenar a escolha do usuário e manter esta informação em um arquivo, mesmo se o
    //usuário fechar o aplicativo
    private static final String ARQUIVO_PREFERENCIA = "ArquivoPreferencia";
    private TextView textResultado;
    private Button buttonRemover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textResultado = findViewById(R.id.resultado);
        listaCategorias = findViewById(R.id.listaCategorias);

        recuperaProdutos(listaProdutos);
        //Se a lista inicial estiver vazia cria somente uma única vez os produtos inicializados
        if (listaProdutos.isEmpty()) {
            criarProdutos();
            salvarLista(); // SALVANDO APÓS CRIAR NA MEMÓRIA
        }

        //chamando o construtor com a callback no último parâmetro para salvar ao marcar/desmarcar
        categoriaAdapter = new CategoriaAdapter(this, listaProdutos, this::salvarLista);
        listaCategorias.setAdapter(categoriaAdapter);
        expandirTodasCategorias();
        atualizarResumo();

        removerItem();
        //Função para chamar o botão para abrir o modal
        novo_produto();

        CheckBox marcarTodos = findViewById(R.id.marcar_todos);
        marcarTodos.setOnClickListener(view -> {
            boolean marcar = marcarTodos.isChecked();
            for (Produto p : listaProdutos) {
                p.setTem(marcar);
            }
            salvarLista(); // SALVANDO A CADA CHECK
            categoriaAdapter.notifyDataSetChanged();
        });
    }

    private void expandirTodasCategorias() {
        for (int i = 0; i < categoriaAdapter.getGroupCount(); i++) {
            listaCategorias.expandGroup(i);
        }
    }

    private void novo_produto() {
        ExtendedFloatingActionButton buttonAdicionarProduto = findViewById(R.id.btnNovoProduto);
        // abrir o modal para adicionar mais produtos
        buttonAdicionarProduto.setOnClickListener(view -> abrirModalProduto());

        // Recolhe o botão (fica só o ícone +) ao rolar a lista e expande de novo ao voltar ao topo
        listaCategorias.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > 0) {
                    buttonAdicionarProduto.shrink();
                } else {
                    buttonAdicionarProduto.extend();
                }
            }
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

        atualizarResumo();
    }

    private void atualizarResumo() {
        if (textResultado == null) return;
        int faltam = 0;
        for (Produto p : listaProdutos) {
            if (!p.isTem()) faltam++;
        }
        textResultado.setText("Faltam " + faltam + " de " + listaProdutos.size() + " itens");
    }

    private void removerItem() {
        buttonRemover = findViewById(R.id.id_remover);

        buttonRemover.setOnClickListener(v -> {
            if (!modoRemocao) {
                modoRemocao = true;
                categoriaAdapter.setModoRemocao(true);
                buttonRemover.setText(R.string.excluir_selecionados);

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
                categoriaAdapter.setModoRemocao(false);
                buttonRemover.setText(R.string.remover);

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
        TextInputLayout inputNomeProduto = view.findViewById(R.id.inputNomeProduto);
        EditText nomeProduto = view.findViewById(R.id.editNomeProduto);
        CheckBox checkComprado = view.findViewById(R.id.checkComprado);
        Button btnAdicionar = view.findViewById(R.id.btnAdicionar);
        Button btnCancelar = view.findViewById(R.id.btnCancelar);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                Categorias.TODAS
        );
        spinnerCategoria.setAdapter(adapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAdicionar.setOnClickListener(v -> {
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String nome = nomeProduto.getText().toString().trim();
            boolean comprado = checkComprado.isChecked();

            if (TextUtils.isEmpty(nome)) {
                inputNomeProduto.setError(getString(R.string.informe_nome_produto));
                return;
            }

            Produto produto = new Produto(categoria, nome, comprado);
            listaProdutos.add(produto);
            salvarLista();
            categoriaAdapter.notifyDataSetChanged();

            // Abre a sublista da categoria e rola até o produto recém adicionado
            int grupo = categoriaAdapter.getPosicaoCategoria(categoria);
            if (grupo >= 0) {
                listaCategorias.expandGroup(grupo);
                listaCategorias.setSelectedChild(grupo, categoriaAdapter.getChildrenCount(grupo) - 1, true);
            }

            Toast.makeText(getApplicationContext(),
                    "Produto " + produto.getNome_produto() + " adicionado em " + categoria,
                    Toast.LENGTH_SHORT).show();

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


        String[] categorias = Categorias.TODAS;


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



