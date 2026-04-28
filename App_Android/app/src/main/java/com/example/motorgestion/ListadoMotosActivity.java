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
import com.example.motorgestion.model.Moto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class ListadoMotosActivity extends AppCompatActivity {

    private ListView listView;
    private EditText etBuscar;
    private TextView tvAvisoOffline;
    private RequestQueue queue;
    private List<Moto> listado = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private static final String URL = "http://10.0.2.2:9000/api/motos";

    private String rolUsuario;
    private boolean offlineMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listado_motos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rolUsuario  = getIntent().getStringExtra("ROL_USUARIO");
        offlineMode = getIntent().getBooleanExtra("OFFLINE_MODE", false);
        if (rolUsuario == null) rolUsuario = "EMPLEADO";

        listView       = findViewById(R.id.listaListView);
        etBuscar       = findViewById(R.id.etBuscar);
        tvAvisoOffline = findViewById(R.id.tvAvisoOffline);
        queue          = Volley.newRequestQueue(this);

        Button btnNuevo = findViewById(R.id.btnNuevaMoto);
        if ("EMPLEADO".equals(rolUsuario) || offlineMode) {
            btnNuevo.setVisibility(View.GONE);
        } else {
            btnNuevo.setOnClickListener(view -> {
                Intent intent = new Intent(ListadoMotosActivity.this, AnadirMotoActivity.class);
                startActivity(intent);
            });
        }

        // TextWatcher para filtrado en tiempo real (Tema 03)
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            String modeloSeleccionado = (String) adapterView.getItemAtPosition(i);
            for (Moto m : listado) {
                if (m.getModelo().equals(modeloSeleccionado)) {
                    Intent intent = new Intent(ListadoMotosActivity.this, DetalleMotoActivity.class);
                    intent.putExtra("ID_MOTO", m.getNum());
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
            cargarMotosHTTP();
        }
    }

    private void cargarMotosHTTP() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        tvAvisoOffline.setVisibility(View.GONE);
                        Gson gson = new Gson();
                        listado = gson.fromJson(response.toString(), new TypeToken<List<Moto>>(){}.getType());
                        mostrarLista();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ListadoMotosActivity.this, "Sin conexión. Mostrando datos en caché.", Toast.LENGTH_LONG).show();
                offlineMode = true;
                cargarDesdeCache();
            }
        });
        queue.add(request);
    }

    private void cargarDesdeCache() {
        tvAvisoOffline.setVisibility(View.VISIBLE);
        String json = SyncManager.getCache(this, "cache_motos");
        if (json != null) {
            Gson gson = new Gson();
            listado = gson.fromJson(json, new TypeToken<List<Moto>>(){}.getType());
            mostrarLista();
        } else {
            Toast.makeText(this, "Sin caché disponible. Conéctate al servidor al menos una vez.", Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarLista() {
        List<String> nombres = new ArrayList<>();
        for (Moto m : listado) nombres.add(m.getModelo());
        adapter = new ArrayAdapter<>(ListadoMotosActivity.this, android.R.layout.simple_list_item_1, nombres);
        listView.setAdapter(adapter);
    }
}
