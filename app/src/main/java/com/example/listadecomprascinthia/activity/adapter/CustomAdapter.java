package com.example.listadecomprascinthia.activity.adapter;

import static com.example.listadecomprascinthia.R.color.meuVerde;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;

import com.example.listadecomprascinthia.R;
import com.example.listadecomprascinthia.activity.model.Produto;

import java.util.List;

public class CustomAdapter extends BaseAdapter {
    public boolean isAllChecked = false;
    Context context;
    List<Produto> listaProdutos;
    private static final String ARQUIVO_PREFERENCIA = "ArquivoPreferencia";
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext, List<Produto> produtoList){
        this.context = applicationContext;
        this.listaProdutos = produtoList;

        inflter = (LayoutInflater.from(applicationContext));
    }
    public void setAllChecked(boolean isAllChecked) {
        this.isAllChecked = isAllChecked;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return listaProdutos.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view = inflter.inflate(R.layout.adapter_lista, null);
        Button categoria_produto = (Button) view.findViewById(R.id.categoriaid);
        TextView nome_produto = (TextView) view.findViewById(R.id.nome_produtoid);
        CheckBox check_produto = (CheckBox) view.findViewById(R.id.temid);


        categoria_produto.setText(listaProdutos.get(i).getCategoria_produto());
        nome_produto.setText(listaProdutos.get(i).getNome_produto());
        check_produto.setChecked(listaProdutos.get(i).isTem());


            if (listaProdutos.get(i).getCategoria_produto() == "Produtos Alimentícios") {
               // System.out.println("Oie eu sou um produto alimentício");
               // categoria_produto.setBackgroundResource(R.color.meuVerde);
                categoria_produto.setTextColor(Color.parseColor("#ec6202"));
                categoria_produto.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
        if (listaProdutos.get(i).getCategoria_produto() == "Produtos de Limpeza") {

            categoria_produto.setTextColor(Color.parseColor("#1E90FF"));
            categoria_produto.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }

        if (listaProdutos.get(i).getCategoria_produto() == "Produtos de Higiene Pessoal") {

            categoria_produto.setTextColor(Color.parseColor("#DB7093"));
            categoria_produto.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }

        check_produto.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {


              if (check_produto.isChecked()){
                  Toast.makeText(context, "Marcado "+listaProdutos.get(i).getNome_produto(), Toast.LENGTH_SHORT).show();
                  check_produto.setChecked(true);
                  listaProdutos.get(i).setTem(true);
                  check_produto.setTextColor(Color.parseColor("#FF610B53"));
              }else{
                  check_produto.setChecked(false);
                  listaProdutos.get(i).setTem(false);
              }



          }
      });



        //Se todos não tiverem checkados  então desmarque  todos
       // como não é passado a posição então todos são marcados ou desmarcados
     //  if (!isAllChecked) check_produto.setChecked(false);
     //  else check_produto.setChecked(true);


        return view;
    }







}
