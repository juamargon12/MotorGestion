package com.example.motorgestion;

import android.os.Bundle;
import android.widget.Button;
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

public class DetalleFurgonetaActivity extends AppCompatActivity {

    private long furgonetaId;
    private RequestQueue queue;
    private static final String URL = "http://10.0.2.2:9000/api/furgonetas/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_furgoneta);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        queue = Volley.newRequestQueue(this);
        furgonetaId = getIntent().getLongExtra("ID_FURGONETA", -1);

        Button btnEliminar = findViewById(R.id.btnEliminar);
        btnEliminar.setOnClickListener(view -> eliminarFurgonetaRest());
    }

    private void eliminarFurgonetaRest() {
        if(furgonetaId == -1) return;

        String urlDelete = URL + furgonetaId;

        StringRequest request = new StringRequest(Request.Method.DELETE, urlDelete,
                response -> {
                    Toast.makeText(this, "Furgoneta borrada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Error al borrar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(request);
    }
}
