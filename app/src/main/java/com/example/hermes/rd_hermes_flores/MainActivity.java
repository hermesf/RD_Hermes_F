package com.example.hermes.rd_hermes_flores;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private Adaptador adapter;
    private RecyclerView.LayoutManager lManager;
    private SwipeRefreshLayout refreshLayout;
    List<Item> lista;
    RunAsyncTask run;
    public static final String URL="https://hn.algolia.com/api/v1/search_by_date?query=android";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ProgressBar  progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recycler = (RecyclerView) findViewById(R.id.reciclador);
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
        progressBar = (ProgressBar) findViewById(R.id.ProgressBar);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);


        // si existen datos Guardados, los cargo
        pref =getSharedPreferences("data", Context.MODE_PRIVATE);
        String str =pref.getString("DATA","");
        editor = pref.edit();
        Toast.makeText(MainActivity.this, "Loading data...", Toast.LENGTH_SHORT).show();
        if(!str.isEmpty() && str.length()>10){
           // Log.i("debug","Recuperando Data");
            // Recovery data
            lista=getItemArr(str);

        }else{
            new RunAsyncTask().execute();

           //start list
                HashSet items = new HashSet();
                Item item= new Item();
                item.setTitle("Cargando Datos...");
                items.add(item);
                lista=new ArrayList(items);
        }

        adapter = new Adaptador(lista,getApplicationContext());
        recycler.setAdapter(adapter);
        recycler.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));


        // Accion para Swipe to Delete
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Toast.makeText(MainActivity.this, "on Move", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                //Remove swiped item from list and notify the RecyclerView
                final int position = viewHolder.getAdapterPosition();

                deleteElement(lista.get(position).getId());
                lista.remove(position);
                //Trick for upate sucess
                recycler.setAdapter( new Adaptador(lista,getApplication().getApplicationContext()));
                //adapter.notifyDataSetChanged(); Not Work
                Log.i("debug","Removido");
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recycler);

        adapter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


            }
        });

        // Obtener el refreshLayout
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                               @Override
                                               public void onRefresh() {

                                                   Log.i("debug","Refrescando");

                                                  if(isConected()){

                                                      new RunAsyncTask().execute();

                                                  }else {
                                                      refreshLayout.setRefreshing(false);
                                                      Toast.makeText(MainActivity.this, "no network access", Toast.LENGTH_LONG).show();
                                                  }
                                               }
                                           }
        );


    }



    public class RunAsyncTask extends AsyncTask<Void, Integer,String > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
             progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            refreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(Void... url) {

            String resultado = "";

            try {
                resultado = downloadUrl(URL);
            } catch (IOException e) {

                Log.e("debug", "Error al consultar");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return resultado;
        }

        @Override
        protected void onPostExecute(String s) {

            // Backup the data
            editor.putString("DATA", s);
            editor.commit();

           // Log.i("debug","DATA GUARDADA"+s);

            refreshLayout.setRefreshing(false);
            //  actualizar(s);
            lista.clear();
            lista.addAll(lista=getItemArr(s));
            adapter.notifyDataSetChanged();

            recycler.setAdapter( new Adaptador(lista, getApplicationContext()));
        }

        private String downloadUrl(String myurl) throws IOException {

            String respuesta="";

            InputStream is = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milisegundos */);
                conn.setConnectTimeout(15000 /* milisegundos */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Inicia la consulta
                // conn.connect();
                int response = conn.getResponseCode();
                if(response==HttpURLConnection.HTTP_OK){
                   // Log.i("debug", "La respuesta es: " + response);
                    is = conn.getInputStream();
                    //Para descargar la página web completa
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                    String webPage = "";
                    String data = "";
                    while ((data = reader.readLine()) != null) {
                        webPage += data + "\n";
                    }
                    respuesta= webPage;
                    // Se asegura de que el InputStream se cierra después de la aplicación deja de usarla.

                }else{
                    Log.i("debug","Error, codgo de respuesta obtenido "+response);
                    respuesta="Servicio no disponible"+"Error, codgo de respuesta obtenido "+response;

                }
            }catch (Exception e){
                e.printStackTrace();

            }finally {
                if (is != null) {
                    is.close();

                }
            }
            return respuesta;

        }


    }
    public List<Item> getItemArr (String str){

        List<Item> lista = new ArrayList<Item>();

        try{
            JSONObject jsonObj = new JSONObject(str);
            JSONArray hits=sortJSONArray(jsonObj.getJSONArray("hits"));

            // Verifico si la noticia fue eliminada  anteriormente
            String saveData =pref.getString("DELETE","");


            Log.i("debug","Data Guardada");
            Log.i("debug",saveData);
            Log.i("debug","++++++++++++++++++++++++++++++++++");

            editor = pref.edit();
            boolean deleted=false;
            String ids[] = saveData.split(",");
            if(!saveData.isEmpty() && saveData.length()>10){
                deleted=true;
            }


            for (int i = 0; i < hits.length(); i++) {

                JSONObject tmp = hits.getJSONObject(i);

                //Si fué eliminado antes, no lo considero
                if(deleted && Arrays.asList(ids).contains(tmp.getString("objectID"))){
                  //  Log.i("debug","SALTANDO al elemento "+tmp.getString("story_title"));
                    continue;
                }


                Item item = new Item();
                item.setId(tmp.getString("objectID"));
                item.setPosicion(i);
                item.setCreateAt(getTiempo(Long.parseLong(tmp.getString("created_at_i"))));
                item.setAuthor(tmp.getString("author"));
                item.setTitle(tmp.getString("story_title"));

                if ((tmp.getString("story_title") == null || tmp.getString("story_title").trim().isEmpty() || tmp.getString("story_title").equals("null")) && tmp.getString("title").length() > 4)
                    item.setTitle(tmp.getString("title"));

                item.setUrl(tmp.getString("url"));

                if ((tmp.getString("url") == null || tmp.getString("url").trim().isEmpty() || tmp.getString("url").equals("null")) && tmp.getString("story_url").length() > 4)
                    item.setUrl(tmp.getString("story_url"));


                //Agrego el Item a la lista
                lista.add(item);

            }


        }catch (JSONException e){
            Log.i("debug","Error  "+e.getMessage()+"   "+e.getCause());
            e.printStackTrace();
        }
        return lista;
    }

    public JSONArray sortJSONArray(JSONArray jsonArr){

        JSONArray sortedJsonArray = new JSONArray();

        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < jsonArr.length(); i++) {
                jsonValues.add(jsonArr.getJSONObject(i));
            }
        }
        catch (JSONException e) {

        }


        Collections.sort( jsonValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID
            private static final String KEY_NAME = "objectID";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(KEY_NAME);
                    valB = (String) b.get(KEY_NAME);
                }
                catch (JSONException e) {

                }

                return -valA.compareTo(valB);
            }
        });

        for (int i = 0; i < jsonArr.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }

        // Log.w("debug","ORDENADO total "+sortedJsonArray.length());

        return sortedJsonArray;
    }

    private String getTiempo(long f) {

        //Log.w("debug","tiempo recibido "+f);

        String tiempo = "";
        final long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;
        final long MILLSECS_PER_HOUR = 60 * 60 * 1000;
        final long MILLSECS_PER_MINUT = 60 * 1000;

        long fechaInicialMs = f;//new Date().getTime();
        ;
        long fechaFinalMs = System.currentTimeMillis() / 1000L;  // new Date().getTime();

        long diferencia = fechaFinalMs - fechaInicialMs;
        //  Log.w("debu",fechaFinalMs+"  +  "+fechaInicialMs+"   =   "+diferencia );


        //Diferencias  en dias, horas  minutos
        double nDias = diferencia / MILLSECS_PER_DAY;
        double nHoras= diferencia / MILLSECS_PER_HOUR;;

        double nMinuos=Math.floor(diferencia / MILLSECS_PER_MINUT);

        //Año mes y dia actuales
        Calendar fechaFinal = Calendar.getInstance();
        int anioF = fechaFinal.get(Calendar.YEAR);
        int mesF = fechaFinal.get(Calendar.MONTH) + 1;
        int diaF = fechaFinal.get(Calendar.DAY_OF_MONTH);

        //Año mes y dia  de la publicacion
        Calendar fechaI = Calendar.getInstance();
        int anioI = fechaI.get(Calendar.YEAR);
        int mesI = fechaI.get(Calendar.MONTH) + 1;
        int diaI = fechaI.get(Calendar.DAY_OF_MONTH);

        boolean mismoDia=(diaI==diaF && mesI==mesF  && anioI==anioF);

        if(mismoDia){// Considero sóo las horas y minutos
            if(nHoras>=1){
                tiempo=Long.toString(((long)nHoras));
                if((nHoras-Math.floor(nHoras))>=0.5)
                    tiempo+=".5";

                tiempo+='h';

            }else if(nMinuos>=1){
                tiempo=Long.toString(((long)nMinuos));

                if((nMinuos-Math.floor(nMinuos))>=0.5)
                    tiempo+=".5";

                tiempo+='m';

            }else
                tiempo="now";

        }else if(nDias<2){// SI ya paso el dia
            tiempo="Yesterday";
        }else {
            tiempo = Long.toString(((long)nDias))+"d";
        }


        return tiempo;
    }


    public boolean getData(){

        run.execute();

        return false;
    }
    public void deleteElement(String id){

        String str =pref.getString("DELETE","");
        str+=","+id;
        editor.putString("DELETE",str);
        editor.commit();

    }

    public Boolean isConected() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");
            int val= p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
