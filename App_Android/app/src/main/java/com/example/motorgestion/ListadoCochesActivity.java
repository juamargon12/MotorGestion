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
import com.example.motorgestion.model.Coche;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class ListadoCochesActivity extends AppCompatActivity {

    private ListView listView;
    private RequestQueue queue;
    private List<Coche> cochesList = new ArrayList<>();
    // URL LOCAL AL BACKEND SPRING BOOT API COCHES
    private static final String URL = "http://10.0.2.2:9000/api/coches";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listado_coches);

        // Ajustar a los bordes de la pantalla para móviles modernos
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listaListView);

        // Inicializamos la cola de Volley (Lo pide la Práctica 4)
        queue = Volley.newRequestQueue(this);

        cargarCochesHTTP();
    }

    private void cargarCochesHTTP() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        cochesList = gson.fromJson(response.toString(), new TypeToken<List<Coche>>(){}.getType());

                        // Extraemos los nombres de los modelos para que la lista los muestre
                        List<String> nombres = new ArrayList<>();
                        for(Coche c : cochesList) {
                            nombres.add(c.getModelo());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ListadoCochesActivity.this,
                                android.R.layout.simple_list_item_1, nombres);
                        listView.setAdapter(adapter);

                        // Preparar la navegación: al tocar un coche te debe abrir los Detalles
                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            Intent intent = new Intent(ListadoCochesActivity.this, DetalleCocheActivity.class);
                            intent.putExtra("ID_COCHE", cochesList.get(i).getNum()); // Le pasamos el ID numérico
                            startActivity(intent);
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ListadoCochesActivity.this, "Fallo red/backend: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(request);
    }
}
