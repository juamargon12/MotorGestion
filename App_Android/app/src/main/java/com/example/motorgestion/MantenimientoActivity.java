package com.example.motorgestion;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MantenimientoActivity extends AppCompatActivity {

    private ListView listView;
    private EditText etBuscar;
    private TextView tvAvisoOffline;
    private FloatingActionButton fabAgregar;
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

    // Variables temporales para el diálogo de creación
    private String tipoSeleccionadoDialog  = null;
    private Long   vehiculoSeleccionadoDialog = null;

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

        rolUsuario  = getIntent().getStringExtra("ROL_USUARIO");
        offlineMode = getIntent().getBooleanExtra("OFFLINE_MODE", false);
        if (rolUsuario == null) rolUsuario = "EMPLEADO";

        listView       = findViewById(R.id.listaListView);
        etBuscar       = findViewById(R.id.etBuscar);
        tvAvisoOffline = findViewById(R.id.tvAvisoOffline);
        fabAgregar     = findViewById(R.id.fabAgregar);
        queue = Volley.newRequestQueue(this);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabAgregar.setOnClickListener(v -> mostrarDialogoNuevaTarea());

        actualizarInterfaz();

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (offlineMode) {
                Toast.makeText(this, "No se puede editar en modo offline", Toast.LENGTH_SHORT).show();
                return;
            }

            String textoSeleccionado = (String) adapterView.getItemAtPosition(i);
            Mantenimiento tarea = null;
            for (Mantenimiento m : listaTareas) {
                if (buildLabel(m).equals(textoSeleccionado)) {
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

            if ("JEFE".equals(rolUsuario)) {
                builder.setNegativeButton("🗑 Borrar tarea",
                        (dialog, which) -> eliminarTareaRest(tareaFinal.getNum()));
            }
            builder.show();
        });

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
        if ("JEFE".equals(rolUsuario) && !offlineMode) {
            fabAgregar.setVisibility(View.VISIBLE);
        } else {
            fabAgregar.setVisibility(View.GONE);
        }

        tvAvisoOffline.setVisibility(offlineMode ? View.VISIBLE : View.GONE);
    }

    private void mostrarDialogoNuevaTarea() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_anadir_tarea, null);
        EditText etTarea = dialogView.findViewById(R.id.etNuevaTareaDialog);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipoVehiculoDialog);
        Spinner spinnerVeh = dialogView.findViewById(R.id.spinnerVehiculoDialog);

        ArrayAdapter<String> tiposAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TIPOS);
        tiposAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(tiposAdapter);

        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipo = TIPOS.get(position);
                tipoSeleccionadoDialog = "(sin asignar)".equals(tipo) ? null : tipo;
                actualizarSpinnerVehiculosDialog(spinnerVeh, tipoSeleccionadoDialog);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerVeh.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vehiculoSeleccionadoDialog = getVehiculoNumAt(tipoSeleccionadoDialog, position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { vehiculoSeleccionadoDialog = null; }
        });

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String desc = etTarea.getText().toString().trim();
                    if (desc.isEmpty()) {
                        Toast.makeText(this, "La descripción es obligatoria", Toast.LENGTH_SHORT).show();
                    } else {
                        crearTareaRest(desc, tipoSeleccionadoDialog, vehiculoSeleccionadoDialog);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarSpinnerVehiculosDialog(Spinner spinner, String tipo) {
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

        ArrayAdapter<String> vAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(vAdapter);
        vehiculoSeleccionadoDialog = getVehiculoNumAt(tipo, 0);
    }

    private void cargarVehiculos() {
        JsonArrayRequest reqCoches = new JsonArrayRequest(Request.Method.GET, URL_COCHES, null,
                response -> {
                    listaCoches = new Gson().fromJson(response.toString(), new TypeToken<List<Coche>>(){}.getType());
                    mostrarLista(); // Refrescar para ver nombres en vez de IDs
                }, null);
        queue.add(reqCoches);

        JsonArrayRequest reqMotos = new JsonArrayRequest(Request.Method.GET, URL_MOTOS, null,
                response -> {
                    listaMotos = new Gson().fromJson(response.toString(), new TypeToken<List<Moto>>(){}.getType());
                    mostrarLista();
                }, null);
        queue.add(reqMotos);

        JsonArrayRequest reqFurg = new JsonArrayRequest(Request.Method.GET, URL_FURGONETAS, null,
                response -> {
                    listaFurgonetas = new Gson().fromJson(response.toString(), new TypeToken<List<Furgoneta>>(){}.getType());
                    mostrarLista();
                }, null);
        queue.add(reqFurg);
    }

    private Long getVehiculoNumAt(String tipo, int position) {
        if (tipo == null) return null;
        try {
            if ("COCHE".equals(tipo) && position < listaCoches.size())
                return listaCoches.get(position).getNum();
            if ("MOTO".equals(tipo) && position < listaMotos.size())
                return listaMotos.get(position).getNum();
            if ("FURGONETA".equals(tipo) && position < listaFurgonetas.size())
                return listaFurgonetas.get(position).getNum();
        } catch (Exception ignored) {}
        return null;
    }

    private void cargarMantenimientos() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL_MANT, null,
                response -> {
                    offlineMode = false;
                    actualizarInterfaz();
                    listaTareas = new Gson().fromJson(response.toString(), new TypeToken<List<Mantenimiento>>(){}.getType());
                    SyncManager.saveCache(this, "cache_mantenimientos", response.toString());
                    mostrarLista();
                },
                error -> {
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
        }
    }

    private String buildLabel(Mantenimiento m) {
        String vehiculoInfo = getVehiculoLabel(m);
        // La etiqueta incluye tarea, estado y vehículo para que el buscador filtre por todo
        return m.getTexto() + " [" + (m.isRealizada() ? "HECHO" : "PENDIENTE") + "] - " + vehiculoInfo;
    }

    private String getVehiculoLabel(Mantenimiento m) {
        if (m.getTipoVehiculo() == null || m.getVehiculoNum() == null) return "Sin vehículo";
        long id = m.getVehiculoNum();
        switch (m.getTipoVehiculo()) {
            case "COCHE":
                for (Coche c : listaCoches)
                    if (c.getNum() == id)
                        return c.getModelo() + " (" + c.getMatricula() + ")";
                break;
            case "MOTO":
                for (Moto mo : listaMotos)
                    if (mo.getNum() == id)
                        return mo.getModelo() + " (" + mo.getMatricula() + ")";
                break;
            case "FURGONETA":
                for (Furgoneta f : listaFurgonetas)
                    if (f.getNum() == id)
                        return f.getModelo() + " (" + f.getMatricula() + ")";
                break;
        }
        return m.getTipoVehiculo() + " #" + id;
    }

    private void mostrarLista() {
        List<String> textShow = new ArrayList<>();
        for (Mantenimiento m : listaTareas) {
            textShow.add(buildLabel(m));
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textShow);
        listView.setAdapter(adapter);

        if (!etBuscar.getText().toString().isEmpty()) {
            adapter.getFilter().filter(etBuscar.getText().toString());
        }
    }

    private void crearTareaRest(String descripcion, String tipo, Long vehiculoId) {
        try {
            JSONObject json = new JSONObject();
            json.put("texto", descripcion);
            json.put("realizada", false);
            if (tipo != null) {
                json.put("tipoVehiculo", tipo);
                if (vehiculoId != null) json.put("vehiculoNum", vehiculoId);
            }

            JsonObjectRequest postReq = new JsonObjectRequest(Request.Method.POST, URL_MANT, json,
                    response -> cargarMantenimientos(),
                    error -> Toast.makeText(this, "Error al crear: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );
            queue.add(postReq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleTareaRest(long idTarea) {
        String urlPut = URL_MANT + "/" + idTarea;
        StringRequest putReq = new StringRequest(Request.Method.PUT, urlPut,
                response -> cargarMantenimientos(),
                error -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
        );
        queue.add(putReq);
    }

    private void eliminarTareaRest(long idTarea) {
        String urlDelete = URL_MANT + "/" + idTarea;
        StringRequest delReq = new StringRequest(Request.Method.DELETE, urlDelete,
                response -> cargarMantenimientos(),
                error -> Toast.makeText(this, "Error al borrar", Toast.LENGTH_SHORT).show()
        );
        queue.add(delReq);
    }
}
