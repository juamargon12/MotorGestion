package com.example.motorgestion;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
    private TextView tvAvisoOffline;
    private RequestQueue queue;
    private List<Coche> cochesList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private static final String URL = "http://10.0.2.2:9000/api/coches";

    private String rolUsuario;
    private boolean offlineMode;

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

        // Recibir rol y modo offline del Intent
        rolUsuario  = getIntent().getStringExtra("ROL_USUARIO");
        offlineMode = getIntent().getBooleanExtra("OFFLINE_MODE", false);
        if (rolUsuario == null) rolUsuario = "EMPLEADO";

        listView       = findViewById(R.id.listaListView);
        etBuscar       = findViewById(R.id.etBuscar);
        tvAvisoOffline = findViewById(R.id.tvAvisoOffline);
        queue          = Volley.newRequestQueue(this);

        Button btnNuevo = findViewById(R.id.btnNuevoCoche);
        // Solo el JEFE puede añadir vehículos (Tema 02 — Roles)
        if ("EMPLEADO".equals(rolUsuario) || offlineMode) {
            btnNuevo.setVisibility(View.GONE);
        } else {
            btnNuevo.setOnClickListener(view -> {
                Intent intent = new Intent(ListadoCochesActivity.this, AnadirCocheActivity.class);
                startActivity(intent);
            });
        }

        // TextWatcher para filtrado en tiempo real (Tema 03 — Interfaz de Usuario)
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Listener registrado una sola vez en onCreate
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            String modeloSeleccionado = (String) adapterView.getItemAtPosition(i);
            for (Coche c : cochesList) {
                if (c.getModelo().equals(modeloSeleccionado)) {
                    Intent intent = new Intent(ListadoCochesActivity.this, DetalleCocheActivity.class);
                    intent.putExtra("ID_COCHE", c.getNum());
                    intent.putExtra("ROL_USUARIO", rolUsuario);
                    intent.putExtra("OFFLINE_MODE", offlineMode);
                    startActivity(intent);
                    break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (offlineMode) {
            cargarDesdeCache();
        } else {
            cargarCochesHTTP();
        }
    }

    private void cargarCochesHTTP() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        tvAvisoOffline.setVisibility(View.GONE);
                        Gson gson = new Gson();
                        cochesList = gson.fromJson(response.toString(), new TypeToken<List<Coche>>(){}.getType());
                        mostrarLista();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Sin conexión → modo offline automático (Tema 04 — Persistencia)
                Toast.makeText(ListadoCochesActivity.this, "Sin conexión. Mostrando datos en caché.", Toast.LENGTH_LONG).show();
                offlineMode = true;
                cargarDesdeCache();
            }
        });
        queue.add(request);
    }

    /** Carga los datos desde SharedPreferences cuando no hay red (Tema 04) */
    private void cargarDesdeCache() {
        tvAvisoOffline.setVisibility(View.VISIBLE);
        String json = SyncManager.getCache(this, "cache_coches");
        if (json != null) {
            Gson gson = new Gson();
            cochesList = gson.fromJson(json, new TypeToken<List<Coche>>(){}.getType());
            mostrarLista();
        } else {
            Toast.makeText(this, "Sin caché disponible. Conéctate al servidor al menos una vez.", Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarLista() {
        List<String> nombres = new ArrayList<>();
        for (Coche c : cochesList) nombres.add(c.getModelo());
        adapter = new ArrayAdapter<>(ListadoCochesActivity.this, android.R.layout.simple_list_item_1, nombres);
        listView.setAdapter(adapter);
    }
}
