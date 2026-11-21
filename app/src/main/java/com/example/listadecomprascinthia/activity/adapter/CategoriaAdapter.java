package com.example.listadecomprascinthia.activity.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.example.listadecomprascinthia.R;
import com.example.listadecomprascinthia.activity.model.Categorias;
import com.example.listadecomprascinthia.activity.model.Produto;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter da ExpandableListView: cada categoria é um grupo (cabeçalho)
 * e os produtos dessa categoria formam a sublista do grupo.
 *
 * A lista "plana" de produtos continua sendo a fonte da verdade (é ela que é salva em JSON);
 * o agrupamento é recalculado sempre que notifyDataSetChanged() é chamado.
 */
public class CategoriaAdapter extends BaseExpandableListAdapter {

    public interface SalvarCallback {
        void salvar();
    }

    public interface EditarCallback {
        void editar(Produto produto);
    }

    private final Context context;
    private final LayoutInflater inflater;
    private final List<Produto> listaProdutos;
    private final SalvarCallback salvarCallback;
    private final EditarCallback editarCallback;

    private final List<String> categorias = new ArrayList<>();
    private final Map<String, List<Produto>> produtosPorCategoria = new LinkedHashMap<>();

    private boolean modoRemocao = false;
    // Texto da pesquisa já normalizado (minúsculo e sem acentos); vazio = mostra tudo
    private String filtro = "";

    public CategoriaAdapter(Context context, List<Produto> listaProdutos,
                            SalvarCallback salvarCallback, EditarCallback editarCallback) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listaProdutos = listaProdutos;
        this.salvarCallback = salvarCallback;
        this.editarCallback = editarCallback;
        agrupar();
    }

    public void setModoRemocao(boolean modoRemocao) {
        this.modoRemocao = modoRemocao;
        notifyDataSetChanged();
    }

    /** Filtra os produtos pelo nome. Ignora maiúsculas/minúsculas e acentos ("acucar" acha "Açúcar"). */
    public void setFiltro(String texto) {
        this.filtro = normalizar(texto);
        notifyDataSetChanged();
    }

    public boolean isFiltrando() {
        return !filtro.isEmpty();
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT);
    }

    public boolean passaNoFiltro(Produto p) {
        return filtro.isEmpty() || normalizar(p.getNome_produto()).contains(filtro);
    }

    /** Posição do grupo de uma categoria, ou -1 se ela não tiver produtos. */
    public int getPosicaoCategoria(String categoria) {
        return categorias.indexOf(categoria);
    }

    @Override
    public void notifyDataSetChanged() {
        agrupar();
        super.notifyDataSetChanged();
    }

    private void agrupar() {
        produtosPorCategoria.clear();
        // Categorias conhecidas primeiro, na ordem definida
        for (String c : Categorias.TODAS) {
            produtosPorCategoria.put(c, new ArrayList<>());
        }
        // Categorias desconhecidas (ex.: dados antigos) entram no final
        for (Produto p : listaProdutos) {
            if (!passaNoFiltro(p)) continue;
            String c =p.getCategoria_produto() != null ? p.getCategoria_produto() : Categorias.OUTROS;
            List<Produto> lista = produtosPorCategoria.get(c);
            if (lista == null) {
                lista = new ArrayList<>();
                produtosPorCategoria.put(c, lista);
            }
            lista.add(p);
        }
        // Só mostra categorias que têm produtos
        categorias.clear();
        for (Map.Entry<String, List<Produto>> e : produtosPorCategoria.entrySet()) {
            if (!e.getValue().isEmpty()) {
                categorias.add(e.getKey());
            }
        }
    }

    @Override
    public int getGroupCount() {
        return categorias.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return produtosPorCategoria.get(categorias.get(groupPosition)).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return categorias.get(groupPosition);
    }

    @Override
    public Produto getChild(int groupPosition, int childPosition) {
        return produtosPorCategoria.get(categorias.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return categorias.get(groupPosition).hashCode();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return System.identityHashCode(getChild(groupPosition, childPosition));
    }

    @Override
    public boolean hasStableIds() {
        // IDs estáveis fazem a lista manter os grupos abertos/fechados ao atualizar
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = convertView != null
                ? convertView
                : inflater.inflate(R.layout.item_categoria, parent, false);

        View bolinha = view.findViewById(R.id.bolinhaCategoria);
        TextView nome = view.findViewById(R.id.nomeCategoria);
        TextView contador = view.findViewById(R.id.contadorCategoria);
        ImageView seta = view.findViewById(R.id.setaCategoria);

        String categoria = getGroup(groupPosition);
        List<Produto> produtos = produtosPorCategoria.get(categoria);

        int cor = ContextCompat.getColor(context, Categorias.corDe(categoria));
        ColorStateList corLista = ColorStateList.valueOf(cor);

        ViewCompat.setBackgroundTintList(bolinha, corLista);
        ViewCompat.setBackgroundTintList(contador, corLista);
        nome.setText(categoria);
        nome.setTextColor(cor);

        int marcados = 0;
        for (Produto p : produtos) {
            if (p.isTem()) marcados++;
        }
        contador.setText(marcados + "/" + produtos.size());

        seta.setRotation(isExpanded ? 180f : 0f);
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        View view = convertView != null
                ? convertView
                : inflater.inflate(R.layout.item_produto, parent, false);

        TextView nome = view.findViewById(R.id.nome_produtoid);
        CheckBox checkTem = view.findViewById(R.id.temid);
        CheckBox checkRemover = view.findViewById(R.id.id_selecione);
        ImageButton btnEditar = view.findViewById(R.id.btnEditar);

        Produto produto = getChild(groupPosition, childPosition);

        nome.setText(produto.getNome_produto());
        aplicarEstiloMarcado(nome, produto.isTem());

        // Checkbox "Tem": remove o listener antes de setar o estado (a view é reaproveitada)
        checkTem.setOnCheckedChangeListener(null);
        checkTem.setChecked(produto.isTem());
        checkTem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            produto.setTem(isChecked);
            if (salvarCallback != null) {
                salvarCallback.salvar();
            }
            notifyDataSetChanged(); // atualiza contador da categoria e o estilo do item
        });

        // Checkbox de remoção
        checkRemover.setVisibility(modoRemocao ? View.VISIBLE : View.GONE);
        checkRemover.setOnCheckedChangeListener(null);
        checkRemover.setChecked(produto.isSelecionado());
        checkRemover.setOnCheckedChangeListener((buttonView, isChecked) ->
                produto.setSelecionado(isChecked));

        // Botão editar (escondido no modo remoção)
        btnEditar.setVisibility(modoRemocao ? View.GONE : View.VISIBLE);
        btnEditar.setOnClickListener(v -> {
            if (editarCallback != null) {
                editarCallback.editar(produto);
            }
        });

        // Tocar na linha inteira marca o item (ou seleciona para remoção)
        view.setOnClickListener(v -> {
            if (modoRemocao) {
                checkRemover.toggle();
            } else {
                checkTem.toggle();
            }
        });

        return view;
    }

    private void aplicarEstiloMarcado(TextView nome, boolean marcado) {
        if (marcado) {
            nome.setPaintFlags(nome.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            nome.setAlpha(0.5f);
        } else {
            nome.setPaintFlags(nome.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            nome.setAlpha(1f);
        }
    }
}
