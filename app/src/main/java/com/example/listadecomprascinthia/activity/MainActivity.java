package com.example.listadecomprascinthia.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.listadecomprascinthia.activity.data.ProdutoRepository;
import com.example.listadecomprascinthia.activity.model.Categorias;
import com.example.listadecomprascinthia.activity.model.Produto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private boolean modoRemocao = false;
    List<Produto> listaProdutos = new ArrayList<Produto>();

    // Lista agrupada por categoria (cada categoria tem sua sublista de produtos)
    private ExpandableListView listaCategorias;
    private CategoriaAdapter categoriaAdapter;

    //variável para armazenar a escolha do usuário e manter esta informação em um arquivo, mesmo se o
    //usuário fechar o aplicativo
    private static final String ARQUIVO_PREFERENCIA = "ArquivoPreferencia";
    // Indica se a lista antiga do celular já foi enviada para o Firestore
    private static final String CHAVE_MIGRADO = "migrado_firestore";
    private static final String TAG = "MainActivity";

    private final ProdutoRepository repositorio = new ProdutoRepository();
    private boolean listaCarregada = false;

    private TextView textResultado;
    private Button buttonRemover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textResultado = findViewById(R.id.resultado);
        listaCategorias = findViewById(R.id.listaCategorias);

        // A callback "salvar" grava no Firestore o produto marcado/desmarcado
        // e a "editar" abre o modal de edição ao tocar no lápis
        categoriaAdapter = new CategoriaAdapter(this, listaProdutos,
                produto -> repositorio.atualizarTem(Collections.singletonList(produto)),
                this::abrirModalProduto);
        listaCategorias.setAdapter(categoriaAdapter);
        textResultado.setText(R.string.carregando_lista);

        // Login anônimo -> migra a lista antiga (se preciso) -> começa a sincronizar
        conectarFirebase();

        removerItem();
        //Função para chamar o botão para abrir o modal
        novo_produto();
        configurarPesquisa();

        CheckBox marcarTodos = findViewById(R.id.marcar_todos);
        marcarTodos.setOnClickListener(view -> {
            boolean marcar = marcarTodos.isChecked();
            // Durante uma pesquisa, marca/desmarca só os itens encontrados
            List<Produto> alterados = new ArrayList<>();
            for (Produto p : listaProdutos) {
                if (categoriaAdapter.passaNoFiltro(p) && p.isTem() != marcar) {
                    p.setTem(marcar);
                    alterados.add(p);
                }
            }
            repositorio.atualizarTem(alterados);
            atualizarTela();
        });
    }

    // ---------------------------------------------------------------------
    // Firebase
    // ---------------------------------------------------------------------

    private void conectarFirebase() {
        repositorio.entrar(new ProdutoRepository.Resultado() {
            @Override
            public void onSucesso() {
                migrarListaAntigaSeNecessario();
            }

            @Override
            public void onErro(Exception e) {
                Log.e(TAG, "Falha no login anônimo", e);
                textResultado.setText(R.string.sem_conexao);
                Toast.makeText(MainActivity.this, R.string.sem_conexao, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Na primeira vez que o app roda com Firebase, envia a lista que estava salva no celular
     * (SharedPreferences) para o Firestore. A lista antiga continua guardada no celular como backup.
     */
    private void migrarListaAntigaSeNecessario() {
        SharedPreferences preferences = getSharedPreferences(ARQUIVO_PREFERENCIA, MODE_PRIVATE);
        if (preferences.getBoolean(CHAVE_MIGRADO, false)) {
            iniciarSincronizacao();
            return;
        }

        List<Produto> produtosLocais = new ArrayList<>();
        recuperaProdutos(produtosLocais);
        if (produtosLocais.isEmpty()) {
            // Nunca usou o app neste celular: usa a lista inicial padrão
            criarProdutos();
            produtosLocais.addAll(listaProdutos);
            listaProdutos.clear();
        }

        repositorio.migrarSeBancoVazio(produtosLocais, new ProdutoRepository.Resultado() {
            @Override
            public void onSucesso() {
                preferences.edit().putBoolean(CHAVE_MIGRADO, true).apply();
                iniciarSincronizacao();
            }

            @Override
            public void onErro(Exception e) {
                // Provavelmente sem internet: sincroniza o que tiver e tenta migrar na próxima abertura
                Log.e(TAG, "Falha ao migrar lista antiga", e);
                Toast.makeText(MainActivity.this, R.string.migracao_pendente, Toast.LENGTH_LONG).show();
                iniciarSincronizacao();
            }
        });
    }

    /** Escuta o Firestore: toda alteração (deste ou de outro celular) atualiza a tela. */
    private void iniciarSincronizacao() {
        repositorio.escutar(this, new ProdutoRepository.ListaListener() {
            @Override
            public void onLista(List<Produto> produtos) {
                // Mantém os itens selecionados no modo remoção
                Set<String> selecionados = new HashSet<>();
                for (Produto p : listaProdutos) {
                    if (p.isSelecionado()) selecionados.add(p.getId());
                }
                for (Produto p : produtos) {
                    p.setSelecionado(selecionados.contains(p.getId()));
                }

                listaProdutos.clear();
                listaProdutos.addAll(produtos);

                if (!listaCarregada) {
                    listaCarregada = true;
                    // Só mostra "Nenhum produto encontrado" depois que a lista carregou
                    listaCategorias.setEmptyView(findViewById(R.id.listaVazia));
                    atualizarTela();
                    expandirTodasCategorias();
                } else {
                    atualizarTela();
                }
            }

            @Override
            public void onErro(Exception e) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.erro_sincronizar, e.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void atualizarTela() {
        categoriaAdapter.notifyDataSetChanged();
        atualizarResumo();
    }

    private void expandirTodasCategorias() {
        for (int i = 0; i < categoriaAdapter.getGroupCount(); i++) {
            listaCategorias.expandGroup(i);
        }
    }

    private void configurarPesquisa() {
        EditText editPesquisa = findViewById(R.id.editPesquisa);
        editPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                categoriaAdapter.setFiltro(s.toString());
                // Abre todas as categorias para mostrar os resultados
                expandirTodasCategorias();
            }
        });
    }

    private void novo_produto() {
        ExtendedFloatingActionButton buttonAdicionarProduto = findViewById(R.id.btnNovoProduto);
        // abrir o modal para adicionar mais produtos (null = produto novo)
        buttonAdicionarProduto.setOnClickListener(view -> abrirModalProduto(null));

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

    private void atualizarResumo() {
        if (textResultado == null || !listaCarregada) return;
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
                repositorio.remover(paraRemover);
                atualizarResumo();

                modoRemocao = false;
                categoriaAdapter.setModoRemocao(false);
                buttonRemover.setText(R.string.remover);

                Toast.makeText(getApplicationContext(),
                        "Itens removidos: " + paraRemover.size(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Abre o modal de produto.
     * @param produtoEditando null para adicionar um produto novo, ou o produto que será editado.
     */
    private void abrirModalProduto(Produto produtoEditando) {
        boolean editando = produtoEditando != null;

        View view = getLayoutInflater().inflate(R.layout.modal_adicionar_produto, null);

        TextView titulo = view.findViewById(R.id.tituloModal);
        Spinner spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        TextInputLayout inputNomeProduto = view.findViewById(R.id.inputNomeProduto);
        EditText nomeProduto = view.findViewById(R.id.editNomeProduto);
        CheckBox checkComprado = view.findViewById(R.id.checkComprado);
        MaterialButton btnAdicionar = view.findViewById(R.id.btnAdicionar);
        Button btnCancelar = view.findViewById(R.id.btnCancelar);

        List<String> opcoesCategoria = new ArrayList<>(Arrays.asList(Categorias.TODAS));
        // Se o produto tiver uma categoria fora da lista padrão, mantém ela como opção
        if (editando && produtoEditando.getCategoria_produto() != null
                && !opcoesCategoria.contains(produtoEditando.getCategoria_produto())) {
            opcoesCategoria.add(produtoEditando.getCategoria_produto());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                opcoesCategoria
        );
        spinnerCategoria.setAdapter(adapter);

        if (editando) {
            // Preenche o modal com os dados atuais do produto
            titulo.setText(R.string.editar_produto);
            btnAdicionar.setText(R.string.salvar_alteracoes);
            btnAdicionar.setIcon(null);
            nomeProduto.setText(produtoEditando.getNome_produto());
            nomeProduto.setSelection(nomeProduto.getText().length());
            checkComprado.setChecked(produtoEditando.isTem());
            int posicao = opcoesCategoria.indexOf(produtoEditando.getCategoria_produto());
            if (posicao >= 0) {
                spinnerCategoria.setSelection(posicao);
            }
        }

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

            Produto produto;
            String mensagem;
            if (editando) {
                produto = produtoEditando;
                produto.setNome_produto(nome);
                produto.setCategoria_produto(categoria);
                produto.setTem(comprado);
                repositorio.atualizar(produto);
                mensagem = "Produto " + nome + " atualizado";
            } else {
                produto = new Produto(categoria, nome, comprado);
                repositorio.adicionar(produto); // gera o ID e a ordem do produto
                listaProdutos.add(produto);
                mensagem = "Produto " + nome + " adicionado em " + categoria;
            }

            // Atualiza a tela na hora; o Firestore confirma em seguida (mesmo offline)
            atualizarTela();
            rolarAteProduto(produto);

            Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    /** Abre a sublista da categoria do produto e rola até ele (se estiver visível na pesquisa atual). */
    private void rolarAteProduto(Produto produto) {
        int grupo = categoriaAdapter.getPosicaoCategoria(produto.getCategoria_produto());
        if (grupo < 0) return;
        listaCategorias.expandGroup(grupo);
        for (int i = 0; i < categoriaAdapter.getChildrenCount(grupo); i++) {
            // Compara pelo ID: a lista é recriada a cada atualização do Firestore
            if (produto.getId() != null && produto.getId().equals(categoriaAdapter.getChild(grupo, i).getId())) {
                listaCategorias.setSelectedChild(grupo, i, true);
                return;
            }
        }
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



