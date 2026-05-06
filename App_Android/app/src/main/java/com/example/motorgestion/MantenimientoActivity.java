package com.example.motorgestion;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.example.motorgestion.model.Coche;
import com.example.motorgestion.model.Furgoneta;
import com.example.motorgestion.model.Mantenimiento;
import com.example.motorgestion.model.Moto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MantenimientoActivity extends AppCompatActivity {

    private ListView listView;
    private EditText etNuevaTarea, etBuscar;
    private TextView tvAvisoOffline;
    private Button btnAgregar;
    private LinearLayout layoutAgregar;
    private Spinner spinnerTipoVehiculo, spinnerVehiculo;
    private RequestQueue queue;
    private List<Mantenimiento> listaTareas = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private static final String URL_BASE      = "http://10.0.2.2:9000/api";
    private static final String URL_MANT      = URL_BASE + "/mantenimientos";
    private static final String URL_COCHES    = URL_BASE + "/coches";
    private static final String URL_MOTOS     = URL_BASE + "/motos";
    private static final String URL_FURGONETAS = URL_BASE + "/furgonetas";

    private static final List<String> TIPOS = Arrays.asList("(sin asignar)", "COCHE", "MOTO", "FURGONETA");

    // Listas de vehículos cargados de la API
    private List<Coche>      listaCoches     = new ArrayList<>();
    private List<Moto>       listaMotos      = new ArrayList<>();
    private List<Furgoneta>  listaFurgonetas = new ArrayList<>();

    // Vehículo seleccionado actualmente en el spinner
    private String tipoSeleccionado  = null;
    private Long   vehiculoSeleccionado = null;

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

        listView            = findViewById(R.id.listaListView);
        etNuevaTarea        = findViewById(R.id.etNuevaTarea);
        etBuscar            = findViewById(R.id.etBuscar);
        tvAvisoOffline      = findViewById(R.id.tvAvisoOffline);
        btnAgregar          = findViewById(R.id.btnAgregarTarea);
        layoutAgregar       = findViewById(R.id.layoutAgregar);
        spinnerTipoVehiculo = findViewById(R.id.spinnerTipoVehiculo);
        spinnerVehiculo     = findViewById(R.id.spinnerVehiculo);
        queue = Volley.newRequestQueue(this);

        // Configurar spinner de tipo de vehículo
        ArrayAdapter<String> tiposAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, TIPOS);
        tiposAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoVehiculo.setAdapter(tiposAdapter);

        spinnerTipoVehiculo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipo = TIPOS.get(position);
                if ("(sin asignar)".equals(tipo)) {
                    tipoSeleccionado = null;
                    vehiculoSeleccionado = null;
                    actualizarSpinnerVehiculos(null);
                } else {
                    tipoSeleccionado = tipo;
                    actualizarSpinnerVehiculos(tipo);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerVehiculo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vehiculoSeleccionado = getVehiculoNumAt(tipoSeleccionado, position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { vehiculoSeleccionado = null; }
        });

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

            String textoSeleccionado = (String) adapterView.getItemAtPosition(i);
            Mantenimiento tarea = null;
            for (Mantenimiento m : listaTareas) {
                String label = buildLabel(m);
                if (label.equals(textoSeleccionado)) {
                    tarea = m;
                    break;
                }
            }

            if (tarea == null) return;
            final Mantenimiento tareaFinal = tarea;
            String estadoActual = tareaFinal.isRealizada() ? "✓ Completada" : "✗ Pendiente";
            String vehiculoInfo = getVehiculoLabel(tareaFinal);

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Tarea: " + tareaFinal.getTexto())
                    .setMessage("Estado: " + estadoActual + "\nVehículo: " + vehiculoInfo + "\n\n¿Qué deseas hacer?")
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

        // Cargar vehículos de la API (para el formulario de alta)
        if (!offlineMode) {
            cargarVehiculos();
        }
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

    // Carga todos los vehículos de la API para poblar los spinners
    private void cargarVehiculos() {
        // Coches
        JsonArrayRequest reqCoches = new JsonArrayRequest(Request.Method.GET, URL_COCHES, null,
                response -> {
                    listaCoches = new Gson().fromJson(response.toString(),
                            new TypeToken<List<Coche>>(){}.getType());
                    if ("COCHE".equals(tipoSeleccionado)) actualizarSpinnerVehiculos("COCHE");
                },
                error -> { /* silencioso */ });
        queue.add(reqCoches);

        // Motos
        JsonArrayRequest reqMotos = new JsonArrayRequest(Request.Method.GET, URL_MOTOS, null,
                response -> {
                    listaMotos = new Gson().fromJson(response.toString(),
                            new TypeToken<List<Moto>>(){}.getType());
                    if ("MOTO".equals(tipoSeleccionado)) actualizarSpinnerVehiculos("MOTO");
                },
                error -> { /* silencioso */ });
        queue.add(reqMotos);

        // Furgonetas
        JsonArrayRequest reqFurg = new JsonArrayRequest(Request.Method.GET, URL_FURGONETAS, null,
                response -> {
                    listaFurgonetas = new Gson().fromJson(response.toString(),
                            new TypeToken<List<Furgoneta>>(){}.getType());
                    if ("FURGONETA".equals(tipoSeleccionado)) actualizarSpinnerVehiculos("FURGONETA");
                },
                error -> { /* silencioso */ });
        queue.add(reqFurg);
    }

    // Actualiza el spinner de vehículos concretos según el tipo elegido
    private void actualizarSpinnerVehiculos(String tipo) {
        List<String> labels = new ArrayList<>();
        if (tipo == null) {
            labels.add("—");
        } else if ("COCHE".equals(tipo)) {
            for (Coche c : listaCoches) labels.add(c.getModelo() + " (" + c.getMatricula() + ")");
        } else if ("MOTO".equals(tipo)) {
            for (Moto m : listaMotos) labels.add(m.getModelo() + " (" + m.getMatricula() + ")");
        } else if ("FURGONETA".equals(tipo)) {
            for (Furgoneta f : listaFurgonetas) labels.add(f.getModelo() + " (" + f.getMatricula() + ")");
        }

        ArrayAdapter<String> vAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehiculo.setAdapter(vAdapter);
        vehiculoSeleccionado = getVehiculoNumAt(tipo, 0);
    }

    // Obtiene el num del vehículo en la posición dada según el tipo
    private Long getVehiculoNumAt(String tipo, int position) {
        if (tipo == null) return null;
        try {
            if ("COCHE".equals(tipo) && position < listaCoches.size())
                return listaCoches.get(position).getNum();
            if ("MOTO".equals(tipo) && position < listaMotos.size())
                return listaMotos.get(position).getNum();
            if ("FURGONETA".equals(tipo) && position < listaFurgonetas.size())
                return listaFurgonetas.get(position).getNum();
        } catch (Exception e) { /* índice fuera de rango */ }
        return null;
    }

    // GET — carga todos los mantenimientos y los muestra en el ListView
    private void cargarMantenimientos() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL_MANT, null,
                response -> {
                    offlineMode = false;
                    actualizarInterfaz();
                    listaTareas = new Gson().fromJson(response.toString(),
                            new TypeToken<List<Mantenimiento>>(){}.getType());
                    // Guardar en caché
                    SyncManager.saveCache(this, "cache_mantenimientos", response.toString());
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

    // Construye la etiqueta visible en el ListView para una tarea
    private String buildLabel(Mantenimiento m) {
        String vehiculoInfo = getVehiculoLabel(m);
        return m.getTexto()
                + "  [" + (m.isRealizada() ? "✓ Hecho" : "✗ Pendiente") + "]"
                + vehiculoInfo;
    }

    // Texto legible del vehículo asignado — busca en las listas cargadas para mostrar modelo y matrícula
    private String getVehiculoLabel(Mantenimiento m) {
        if (m.getTipoVehiculo() == null || m.getVehiculoNum() == null) return "Sin vehículo";
        long id = m.getVehiculoNum();
        switch (m.getTipoVehiculo()) {
            case "COCHE":
                for (Coche c : listaCoches)
                    if (c.getNum() == id) return c.getModelo() + " (" + c.getMatricula() + ")";
                break;
            case "MOTO":
                for (Moto mo : listaMotos)
                    if (mo.getNum() == id) return mo.getModelo() + " (" + mo.getMatricula() + ")";
                break;
            case "FURGONETA":
                for (Furgoneta f : listaFurgonetas)
                    if (f.getNum() == id) return f.getModelo() + " (" + f.getMatricula() + ")";
                break;
        }
        // Fallback si las listas aún no se han cargado
        return m.getTipoVehiculo() + " #" + id;
    }

    private void mostrarLista() {
        List<String> textShow = new ArrayList<>();
        for (Mantenimiento m : listaTareas) {
            textShow.add(buildLabel(m));
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textShow);
        listView.setAdapter(adapter);

        // Mantener filtro si existe
        if (!etBuscar.getText().toString().isEmpty()) {
            adapter.getFilter().filter(etBuscar.getText().toString());
        }
    }

    // POST — crea una nueva tarea de mantenimiento con el vehículo seleccionado
    private void crearTareaRest() {
        String descripcion = etNuevaTarea.getText().toString().trim();

        try {
            JSONObject json = new JSONObject();
            json.put("texto", descripcion);
            json.put("realizada", false);
            if (tipoSeleccionado != null) {
                json.put("tipoVehiculo", tipoSeleccionado);
                if (vehiculoSeleccionado != null) {
                    json.put("vehiculoNum", vehiculoSeleccionado);
                }
            }

            JsonObjectRequest postReq = new JsonObjectRequest(Request.Method.POST, URL_MANT, json,
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
        String urlPut = URL_MANT + "/" + idTarea;
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
        String urlDelete = URL_MANT + "/" + idTarea;
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
