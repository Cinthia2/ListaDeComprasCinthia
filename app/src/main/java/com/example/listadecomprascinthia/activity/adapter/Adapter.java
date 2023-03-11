package com.example.listadecomprascinthia.activity.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.listadecomprascinthia.R;
import com.example.listadecomprascinthia.activity.model.Produto;

import java.util.List;
import java.util.logging.Logger;
import android.util.SparseBooleanArray;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
    public boolean isAllChecked = false;
    public boolean isOneChecked = false;
    private List<Produto> listaProdutos;
    SparseBooleanArray itemStateArray= new SparseBooleanArray();

    //Aqui é o construtor
    public Adapter(List<Produto> listaProduto) {
         this.listaProdutos = listaProduto;

    }
    public void setAllChecked(boolean isAllChecked) {
        this.isAllChecked = isAllChecked;
        notifyDataSetChanged();
    }
    public void setOneChecked(boolean isOneChecked) {
        this.isOneChecked = isOneChecked;
        notifyDataSetChanged();
    }
    @Override
    //Cria o objeto
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_lista,parent, false);
        return new MyViewHolder(itemLista);
    }
    @Override
    //Cria a visualização do objeto
    public void onBindViewHolder(MyViewHolder holder, int position) {

       Produto produto = listaProdutos.get(position);
        holder.categoria_produto.setText(produto.getCategoria_produto());
        holder.nome_produto.setText(produto.getNome_produto());
       holder.tem_produto.setChecked(produto.isTem());

       if (!isAllChecked) holder.tem_produto.setChecked(false);
       else holder.tem_produto.setChecked(true);


    }
    @Override
    public int getItemCount() {
        return listaProdutos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  {
       private TextView categoria_produto;
        private TextView nome_produto;
       private  CheckBox tem_produto;
        ///ConstraintLayout rowItem;
        private CheckBox marcar_todos;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            this.categoria_produto = itemView.findViewById(R.id.categoriaid);
            this.nome_produto = itemView.findViewById(R.id.nome_produtoid);
            this.tem_produto = itemView.findViewById(R.id.temid);

/*

            tem_produto.setOnClickListener(new View.OnClickListener(){
                Produto produto = new Produto("hhhhh","ggggg",true);
                @Override
                public void onClick(View v) {
                    boolean isClicked = ((CheckBox) v).isChecked();
                    if (isClicked){
                        listaProdutos.get(getAdapterPosition()).setTem(true);
                       tem_produto.setChecked(true);
                        Toast.makeText(v.getContext(),listaProdutos.get(getAdapterPosition()).getNome_produto(), Toast.LENGTH_LONG).show();
                        //isso que adiciona produto
                      //  listaProdutos.add(0,produto);
                        //atualiza
                       // listaProdutos.set(0,produto);
                        notifyDataSetChanged();
                    }else{
                        listaProdutos.get(getAdapterPosition()).setTem(false);
                    }
                    notifyDataSetChanged();
                    for(int i=0;i< listaProdutos.size(); i++){
                        Log.d("TAG", listaProdutos.toString());
                    }
                }
            });*/

 /*   tem_produto.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    boolean isClicked = ((CheckBox) v).isChecked();
                    if (isClicked){
                        listaProdutos.get(getAdapterPosition()).setTem(true);
                    }else{
                        listaProdutos.get(getAdapterPosition()).setTem(false);
                    }
                    notifyDataSetChanged();
                    for(int i=0;i< listaProdutos.size(); i++){
                        Log.d("TAG", listaProdutos.toString());
                    }
                }
            });*/
        }

       /* void bind(int position) {
            // use the sparse boolean array to check
            if (!itemStateArray.get(position, false)) {
                tem_produto.setChecked(false);
            }
            else {
                tem_produto.setChecked(true);
            }
            tem_produto.setText(String.valueOf(listaProdutos.get(position).getPosition()));
        }
*/

       /* @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (!itemStateArray.get(adapterPosition, false)) {
                tem_produto.setChecked(true);
                itemStateArray.put(adapterPosition, true);
            }
            else  {
                tem_produto.setChecked(false);
                itemStateArray.put(adapterPosition, false);
            }
        }*/
    }

}
