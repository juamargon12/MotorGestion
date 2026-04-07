package com.example.motorgestion;

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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.motorgestion.model.Mantenimiento;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class MantenimientoActivity extends AppCompatActivity {

    private ListView listView;
    private static final String URL = "http://10.0.2.2:8080/api/mantenimientos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mantenimiento);

        // Soporte visual moderno (respeta barras del sistema)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listaListView);
        cargarMantenimientos();
    }

    private void cargarMantenimientos() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                response -> {
                    List<Mantenimiento> tareas = new Gson().fromJson(response.toString(), new TypeToken<List<Mantenimiento>>(){}.getType());
                    List<String> listado = new ArrayList<>();

                    // Recorremos la tabla generada por Eclipse/Spring Boot y comprobamos
                    // si la BD manda TRUE o FALSE para pintar un tick o una X visual.
                    for(Mantenimiento m : tareas) {
                        listado.add(m.getTexto() + " - [" + (m.isRealizada() ? "✓" : " X ") + "]");
                    }
                    listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listado));
                },
                error -> Toast.makeText(this, "Error cargando tareas de la BD", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }
}
