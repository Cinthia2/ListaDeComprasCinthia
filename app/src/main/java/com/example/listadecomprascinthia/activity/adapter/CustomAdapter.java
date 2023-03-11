package com.example.listadecomprascinthia.activity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.listadecomprascinthia.R;
import com.example.listadecomprascinthia.activity.model.Produto;

import java.util.List;

public class CustomAdapter extends BaseAdapter {
    public boolean isAllChecked = false;
    Context context;
    List<Produto> listaProdutos;

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
        TextView categoria_produto = (TextView) view.findViewById(R.id.categoriaid);
        TextView nome_produto = (TextView) view.findViewById(R.id.nome_produtoid);
        CheckBox check_produto = (CheckBox) view.findViewById(R.id.temid);

        categoria_produto.setText(listaProdutos.get(i).getCategoria_produto());
        nome_produto.setText(listaProdutos.get(i).getNome_produto());
        check_produto.setChecked(listaProdutos.get(i).isTem());



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
