package com.example.motorgestion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class DetalleCocheActivity extends AppCompatActivity {

    private long cocheId;
    private RequestQueue queue;
    private static final String URL = "http://10.0.2.2:8080/api/coches/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_coche);

        // Soporte visual moderno (respeta barras del sistema)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configuración de la librería Volley
        queue = Volley.newRequestQueue(this);

        // Recogemos el ID del coche que el Listado nos ha enviado al hacer clic sobre él
        cocheId = getIntent().getLongExtra("ID_COCHE", -1);

        // Configuramos la acción del botón
        Button btnEliminar = findViewById(R.id.btnEliminar);
        btnEliminar.setOnClickListener(view -> eliminarCocheRest());
    }

    // Petición DELETE HTTP según Ejercicio 4
    private void eliminarCocheRest() {
        if(cocheId == -1) return;

        // La URL se queda así: http://10.0.2.2:8080/api/coches/5
        String urlDelete = URL + cocheId;

        StringRequest request = new StringRequest(Request.Method.DELETE, urlDelete,
                response -> {
                    Toast.makeText(this, "Coche borrado correctamente", Toast.LENGTH_SHORT).show();
                    // Con finish() cerramos el Detalle y el móvil vuelve automáticamente a la lista
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Error de red al borrar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(request);
    }
}
