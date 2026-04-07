package com.example.motorgestion;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
    private EditText etBuscar;
    private RequestQueue queue;
    private List<Coche> cochesList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private static final String URL = "http://10.0.2.2:9000/api/coches";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listado_coches);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listaListView);
        etBuscar = findViewById(R.id.etBuscar);
        queue = Volley.newRequestQueue(this);

        Button btnNuevo = findViewById(R.id.btnNuevoCoche);
        btnNuevo.setOnClickListener(view -> {
            Intent intent = new Intent(ListadoCochesActivity.this, AnadirCocheActivity.class);
            startActivity(intent);
        });

        // TextWatcher para filtrado en tiempo real (Tema 03 — Interfaz de Usuario)
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s); // Filtra el ArrayAdapter con el texto introducido
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCochesHTTP();
    }

    private void cargarCochesHTTP() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        cochesList = gson.fromJson(response.toString(), new TypeToken<List<Coche>>(){}.getType());

                        List<String> nombres = new ArrayList<>();
                        for (Coche c : cochesList) {
                            nombres.add(c.getModelo());
                        }

                        adapter = new ArrayAdapter<>(ListadoCochesActivity.this,
                                android.R.layout.simple_list_item_1, nombres);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            // Obtenemos el índice real del elemento en la lista filtrada
                            String modeloSeleccionado = adapter.getItem(i);
                            for (Coche c : cochesList) {
                                if (c.getModelo().equals(modeloSeleccionado)) {
                                    Intent intent = new Intent(ListadoCochesActivity.this, DetalleCocheActivity.class);
                                    intent.putExtra("ID_COCHE", c.getNum());
                                    startActivity(intent);
                                    break;
                                }
                            }
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ListadoCochesActivity.this, "Fallo red/backend", Toast.LENGTH_LONG).show();
            }
        });

        queue.add(request);
    }
}
