package com.example.motorgestion;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
    private EditText etNuevaTarea;
    private RequestQueue queue;
    private List<Mantenimiento> listaTareas = new ArrayList<>();
    private static final String URL = "http://10.0.2.2:9000/api/mantenimientos";

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

        listView      = findViewById(R.id.listaListView);
        etNuevaTarea  = findViewById(R.id.etNuevaTarea);
        queue = Volley.newRequestQueue(this);

        Button btnAgregar = findViewById(R.id.btnAgregarTarea);
        btnAgregar.setOnClickListener(view -> {
            // Validación: la descripción de la tarea no puede estar vacía
            if (etNuevaTarea.getText().toString().trim().isEmpty()) {
                etNuevaTarea.setError("La descripción no puede estar vacía");
                return;
            }
            crearTareaRest();
        });

        // Al pulsar una tarea — AlertDialog con opciones: Toggle o Borrar
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            Mantenimiento tarea = listaTareas.get(i);
            String estadoActual = tarea.isRealizada() ? "✓ Completada" : "✗ Pendiente";

            // AlertDialog con 2 opciones (Tema 03 — IU: diálogo de confirmación)
            new AlertDialog.Builder(this)
                    .setTitle("Tarea: " + tarea.getTexto())
                    .setMessage("Estado actual: " + estadoActual + "\n\n¿Qué deseas hacer?")
                    .setPositiveButton(tarea.isRealizada() ? "Marcar como PENDIENTE" : "Marcar como COMPLETADA",
                            (dialog, which) -> toggleTareaRest(tarea.getNum()))
                    .setNegativeButton("🗑 Borrar tarea",
                            (dialog, which) -> eliminarTareaRest(tarea.getNum()))
                    .setNeutralButton("Cancelar", null)
                    .show();
        });

        cargarMantenimientos();
    }

    // GET — carga todos los mantenimientos y los muestra en el ListView
    private void cargarMantenimientos() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                response -> {
                    listaTareas = new Gson().fromJson(response.toString(),
                            new TypeToken<List<Mantenimiento>>(){}.getType());

                    List<String> textShow = new ArrayList<>();
                    for (Mantenimiento m : listaTareas) {
                        // Muestra el estado booleano de forma clara con iconos
                        textShow.add(m.getTexto() + "  [" + (m.isRealizada() ? "✓ Hecho" : "✗ Pendiente") + "]");
                    }
                    listView.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, textShow));
                },
                error -> Toast.makeText(this, "Error cargando BD: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
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

    // PUT — toggle del estado realizada ↔ pendiente (llama al endpoint toggle del backend)
    private void toggleTareaRest(long idTarea) {
        String urlPut = URL + "/" + idTarea;
        // StringRequest con método PUT (el backend hace el toggle internamente)
        StringRequest putReq = new StringRequest(Request.Method.PUT, urlPut,
                response -> {
                    Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show();
                    cargarMantenimientos(); // Recarga la lista para reflejar el cambio
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
