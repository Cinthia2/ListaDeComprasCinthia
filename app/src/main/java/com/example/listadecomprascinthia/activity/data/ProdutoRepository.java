package com.example.listadecomprascinthia.activity.data;

import android.app.Activity;
import android.util.Log;

import com.example.listadecomprascinthia.activity.model.Produto;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Centraliza toda a comunicação com o Firebase:
 * - login anônimo (exigido pelas regras do Firestore)
 * - leitura em tempo real da coleção "produtos"
 * - gravações (adicionar, editar, marcar, remover)
 * - migração da lista antiga salva no celular
 *
 * O Firestore guarda cache no celular, então tudo funciona offline e
 * sincroniza sozinho quando a internet volta.
 */
public class ProdutoRepository {

    private static final String TAG = "ProdutoRepository";
    private static final String COLECAO = "produtos";
    // O Firestore aceita no máximo 500 operações por lote
    private static final int LIMITE_LOTE = 500;

    public interface Resultado {
        void onSucesso();

        void onErro(Exception e);
    }

    public interface ListaListener {
        void onLista(List<Produto> produtos);

        void onErro(Exception e);
    }

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference colecao = db.collection(COLECAO);

    /** Faz o login anônimo. Se o usuário já entrou antes, não precisa de internet. */
    public void entrar(Resultado resultado) {
        if (auth.getCurrentUser() != null) {
            resultado.onSucesso();
            return;
        }
        auth.signInAnonymously()
                .addOnSuccessListener(r -> resultado.onSucesso())
                .addOnFailureListener(resultado::onErro);
    }

    /**
     * Escuta a lista em tempo real. O listener é removido sozinho quando a Activity é destruída.
     * Qualquer alteração (deste celular ou de outro) chama onLista com a lista completa.
     */
    public ListenerRegistration escutar(Activity activity, ListaListener listener) {
        return colecao.orderBy("ordem", Query.Direction.ASCENDING)
                .addSnapshotListener(activity, (snapshot, erro) -> {
                    if (erro != null) {
                        Log.e(TAG, "Erro ao escutar produtos", erro);
                        listener.onErro(erro);
                        return;
                    }
                    List<Produto> produtos = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Produto p = doc.toObject(Produto.class);
                            if (p != null) {
                                produtos.add(p);
                            }
                        }
                    }
                    listener.onLista(produtos);
                });
    }

    /** Gera ID e ordem para um produto novo (sem gravar ainda). */
    private void prepararNovo(Produto produto, long ordem) {
        if (produto.getId() == null) {
            produto.setId(colecao.document().getId());
        }
        if (produto.getOrdem() == 0) {
            produto.setOrdem(ordem);
        }
    }

    public void adicionar(Produto produto) {
        prepararNovo(produto, System.currentTimeMillis());
        colecao.document(produto.getId()).set(produto)
                .addOnFailureListener(e -> Log.e(TAG, "Erro ao adicionar produto", e));
    }

    /** Grava todos os campos do produto (usado na edição). */
    public void atualizar(Produto produto) {
        colecao.document(produto.getId()).set(produto)
                .addOnFailureListener(e -> Log.e(TAG, "Erro ao atualizar produto", e));
    }

    /** Grava só o campo "tem" de cada produto (marcar/desmarcar). */
    public void atualizarTem(List<Produto> produtos) {
        for (int inicio = 0; inicio < produtos.size(); inicio += LIMITE_LOTE) {
            WriteBatch lote = db.batch();
            for (Produto p : produtos.subList(inicio, Math.min(inicio + LIMITE_LOTE, produtos.size()))) {
                lote.update(colecao.document(p.getId()), "tem", p.isTem());
            }
            lote.commit().addOnFailureListener(e -> Log.e(TAG, "Erro ao marcar produtos", e));
        }
    }

    public void remover(List<Produto> produtos) {
        for (int inicio = 0; inicio < produtos.size(); inicio += LIMITE_LOTE) {
            WriteBatch lote = db.batch();
            for (Produto p : produtos.subList(inicio, Math.min(inicio + LIMITE_LOTE, produtos.size()))) {
                lote.delete(colecao.document(p.getId()));
            }
            lote.commit().addOnFailureListener(e -> Log.e(TAG, "Erro ao remover produtos", e));
        }
    }

    /**
     * Envia a lista antiga (do celular) para o Firestore, mas SÓ se o banco ainda estiver vazio.
     * Assim, se outro celular já migrou, os produtos não são duplicados.
     * Consulta direto o servidor: sem internet dá erro e a migração é tentada de novo depois.
     */
    public void migrarSeBancoVazio(List<Produto> produtosLocais, Resultado resultado) {
        colecao.limit(1).get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Log.i(TAG, "Banco já tem produtos; migração não necessária");
                        resultado.onSucesso();
                        return;
                    }
                    long base = System.currentTimeMillis();
                    WriteBatch lote = db.batch();
                    int noLote = 0;
                    List<Task<Void>> commits = new ArrayList<>();
                    for (int i = 0; i < produtosLocais.size(); i++) {
                        Produto p = produtosLocais.get(i);
                        // "base + i" mantém a mesma ordem da lista antiga
                        prepararNovo(p, base + i);
                        lote.set(colecao.document(p.getId()), p);
                        noLote++;
                        if (noLote == LIMITE_LOTE) {
                            commits.add(lote.commit());
                            lote = db.batch();
                            noLote = 0;
                        }
                    }
                    commits.add(lote.commit());
                    Tasks.whenAll(commits)
                            .addOnSuccessListener(v -> {
                                Log.i(TAG, "Migrados " + produtosLocais.size() + " produtos");
                                resultado.onSucesso();
                            })
                            .addOnFailureListener(resultado::onErro);
                })
                .addOnFailureListener(resultado::onErro);
    }
}
