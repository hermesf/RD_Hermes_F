package com.example.hermes.rd_hermes_flores;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Hermes on 14/9/2017.
 */

public class Adaptador extends RecyclerView.Adapter<Adaptador.ItemViewHolder> implements View.OnClickListener  {

    List<Item> items;
    private View.OnClickListener listener;
    Context ct;


    public Adaptador(List<Item> items, Context ct) {
        this.items = items;
        this.ct = ct;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView getUrl() {
            return url;
        }

        public void setUrl(TextView url) {
            this.url = url;
        }

        public TextView titulo;
        public TextView detalle;
        public TextView url;
        TextView id;

        public ItemViewHolder(View v) {
            super(v);
            titulo = (TextView) v.findViewById(R.id.titulo);
            detalle = (TextView) v.findViewById(R.id.detalle);
            id= (TextView) v.findViewById(R.id.id);
            url= (TextView) v.findViewById(R.id.url);

        }
    }


    public Adaptador(List<Item> items) {
        this.items = items;
    }


    public void addAll(List<Item> lista) {
        items.addAll(lista);
        notifyDataSetChanged();
    }

    /*
   Permite limpiar todos los elementos del recycler
    */
    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        v.setOnClickListener(this);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder viewHolder, int i) {
        viewHolder.titulo.setText(items.get(i).getTitle());
        viewHolder.detalle.setText(items.get(i).getAuthor() + " - " + items.get(i).getCreateAt());
        viewHolder.id.setText(items.get(i).getPosicion().toString());
        viewHolder.url.setText(items.get(i).getUrl());
    }

    public void setOnClickListener(View.OnClickListener list) {
        this.listener = list;
    }


    @Override
    public void onClick(View v) {

            if(listener != null) {
                listener.onClick(v);
            }

        String posicion=(String)((TextView)v.findViewById(R.id.id)).getText();
        String titleElement=(String)((TextView)v.findViewById(R.id.titulo)).getText();
        String url=(String)((TextView)v.findViewById(R.id.url)).getText();

        // Abrir Navegador

        if(url.isEmpty() || url==null || url.length()<5){
            Toast.makeText(ct, "Link not avaliable", Toast.LENGTH_SHORT).show();

        }else{
            try {
                Intent intent =new Intent(ct,Browser.class);
                intent.putExtra("URL", url);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ct.startActivity(intent);
            }catch (Exception e) {
                Log.i("debug","Error Capturado   "+e.getMessage());
                e.printStackTrace();
            }


        }



return;
    }




}
