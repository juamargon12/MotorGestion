package com.example.motorgestion;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.motorgestion.model.Mantenimiento;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MantenimientoActivity extends AppCompatActivity {

    private ListView listView;
    private EditText etNuevaTarea, etBuscar;
    private TextView tvAvisoOffline;
    private Button btnAgregar;
    private LinearLayout layoutAgregar;
    private RequestQueue queue;
    private List<Mantenimiento> listaTareas = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private static final String URL = "http://10.0.2.2:9000/api/mantenimientos";

    private String rolUsuario;
    private boolean offlineMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mantenimiento);

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
        etNuevaTarea   = findViewById(R.id.etNuevaTarea);
        etBuscar       = findViewById(R.id.etBuscar);
        tvAvisoOffline = findViewById(R.id.tvAvisoOffline);
        btnAgregar     = findViewById(R.id.btnAgregarTarea);
        layoutAgregar  = findViewById(R.id.layoutAgregar);
        queue = Volley.newRequestQueue(this);

        // Buscador disponible para todos (Tema 03)
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAgregar.setOnClickListener(view -> {
            if (etNuevaTarea.getText().toString().trim().isEmpty()) {
                etNuevaTarea.setError("La descripción no puede estar vacía");
                return;
            }
            crearTareaRest();
        });

        actualizarInterfaz();

        // Al pulsar una tarea — AlertDialog con opciones: Toggle o Borrar
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (offlineMode) {
                Toast.makeText(this, "No se puede editar en modo offline", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Obtenemos el texto del item seleccionado (importante si está filtrado)
            String textoSeleccionado = (String) adapterView.getItemAtPosition(i);
            Mantenimiento tarea = null;
            for (Mantenimiento m : listaTareas) {
                String label = m.getTexto() + "  [" + (m.isRealizada() ? "✓ Hecho" : "✗ Pendiente") + "]";
                if (label.equals(textoSeleccionado)) {
                    tarea = m;
                    break;
                }
            }

            if (tarea == null) return;
            final Mantenimiento tareaFinal = tarea;
            String estadoActual = tareaFinal.isRealizada() ? "✓ Completada" : "✗ Pendiente";

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Tarea: " + tareaFinal.getTexto())
                    .setMessage("Estado actual: " + estadoActual + "\n\n¿Qué deseas hacer?")
                    .setPositiveButton(tareaFinal.isRealizada() ? "Marcar como PENDIENTE" : "Marcar como COMPLETADA",
                            (dialog, which) -> toggleTareaRest(tareaFinal.getNum()))
                    .setNeutralButton("Cancelar", null);

            // Solo el JEFE puede borrar tareas
            if ("JEFE".equals(rolUsuario)) {
                builder.setNegativeButton("🗑 Borrar tarea",
                        (dialog, which) -> eliminarTareaRest(tareaFinal.getNum()));
            }

            builder.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (offlineMode) {
            cargarDesdeCache();
        } else {
            cargarMantenimientos();
        }
    }

    private void actualizarInterfaz() {
        // Solo el JEFE puede ver el panel de agregar
        if ("EMPLEADO".equals(rolUsuario) || offlineMode) {
            layoutAgregar.setVisibility(View.GONE);
        } else {
            layoutAgregar.setVisibility(View.VISIBLE);
        }

        if (offlineMode) {
            tvAvisoOffline.setVisibility(View.VISIBLE);
        } else {
            tvAvisoOffline.setVisibility(View.GONE);
        }
    }

    // GET — carga todos los mantenimientos y los muestra en el ListView
    private void cargarMantenimientos() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                response -> {
                    offlineMode = false;
                    actualizarInterfaz();
                    listaTareas = new Gson().fromJson(response.toString(),
                            new TypeToken<List<Mantenimiento>>(){}.getType());
                    mostrarLista();
                },
                error -> {
                    Toast.makeText(this, "Sin conexión. Cargando caché.", Toast.LENGTH_SHORT).show();
                    offlineMode = true;
                    actualizarInterfaz();
                    cargarDesdeCache();
                }
        );
        queue.add(request);
    }

    private void cargarDesdeCache() {
        actualizarInterfaz();
        String json = SyncManager.getCache(this, "cache_mantenimientos");
        if (json != null) {
            listaTareas = new Gson().fromJson(json, new TypeToken<List<Mantenimiento>>(){}.getType());
            mostrarLista();
        } else {
            Toast.makeText(this, "Sin caché de mantenimientos disponible.", Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarLista() {
        List<String> textShow = new ArrayList<>();
        for (Mantenimiento m : listaTareas) {
            textShow.add(m.getTexto() + "  [" + (m.isRealizada() ? "✓ Hecho" : "✗ Pendiente") + "]");
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textShow);
        listView.setAdapter(adapter);

        // Mantener filtro si existe
        if (!etBuscar.getText().toString().isEmpty()) {
            adapter.getFilter().filter(etBuscar.getText().toString());
        }
    }

    // POST — crea una nueva tarea de mantenimiento
    private void crearTareaRest() {
        String descripcion = etNuevaTarea.getText().toString().trim();

        try {
            JSONObject json = new JSONObject();
            json.put("texto", descripcion);
            json.put("realizada", false);

            JsonObjectRequest postReq = new JsonObjectRequest(Request.Method.POST, URL, json,
                    response -> {
                        etNuevaTarea.setText("");
                        cargarMantenimientos();
                    },
                    error -> Toast.makeText(this, "Fallo enviando POST: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );
            queue.add(postReq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // PUT — toggle del estado realizada ↔ pendiente
    private void toggleTareaRest(long idTarea) {
        String urlPut = URL + "/" + idTarea;
        StringRequest putReq = new StringRequest(Request.Method.PUT, urlPut,
                response -> {
                    Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show();
                    cargarMantenimientos();
                },
                error -> Toast.makeText(this, "Error al actualizar: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );
        queue.add(putReq);
    }

    // DELETE — borra una tarea permanentemente
    private void eliminarTareaRest(long idTarea) {
        String urlDelete = URL + "/" + idTarea;
        StringRequest delReq = new StringRequest(Request.Method.DELETE, urlDelete,
                response -> {
                    Toast.makeText(this, "Tarea borrada", Toast.LENGTH_SHORT).show();
                    cargarMantenimientos();
                },
                error -> Toast.makeText(this, "Fallo enviando DELETE: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );
        queue.add(delReq);
    }
}
