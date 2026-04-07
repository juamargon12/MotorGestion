package com.example.motorgestion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.motorgestion.model.Furgoneta;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class ListadoFurgonetasActivity extends AppCompatActivity {

    private ListView listView;
    private RequestQueue queue;
    private List<Furgoneta> listado = new ArrayList<>();
    private static final String URL = "http://10.0.2.2:9000/api/furgonetas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listado_furgonetas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listaListView);
        queue = Volley.newRequestQueue(this);

        cargarFurgonetasHTTP();
    }

    private void cargarFurgonetasHTTP() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        listado = gson.fromJson(response.toString(), new TypeToken<List<Furgoneta>>(){}.getType());

                        List<String> nombres = new ArrayList<>();
                        for(Furgoneta f : listado) {
                            nombres.add(f.getModelo());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ListadoFurgonetasActivity.this,
                                android.R.layout.simple_list_item_1, nombres);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            Intent intent = new Intent(ListadoFurgonetasActivity.this, DetalleFurgonetaActivity.class);
                            intent.putExtra("ID_FURGONETA", listado.get(i).getNum());
                            startActivity(intent);
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ListadoFurgonetasActivity.this, "Error de red: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(request);
    }
}
