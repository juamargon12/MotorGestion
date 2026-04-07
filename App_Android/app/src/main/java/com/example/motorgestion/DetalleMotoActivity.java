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

public class DetalleMotoActivity extends AppCompatActivity {

    private long motoId;
    private RequestQueue queue;
    private static final String URL = "http://10.0.2.2:9000/api/motos/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_moto);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        queue = Volley.newRequestQueue(this);
        motoId = getIntent().getLongExtra("ID_MOTO", -1);

        Button btnEliminar = findViewById(R.id.btnEliminar);
        btnEliminar.setOnClickListener(view -> eliminarMotoRest());
    }

    private void eliminarMotoRest() {
        if(motoId == -1) return;

        String urlDelete = URL + motoId;

        StringRequest request = new StringRequest(Request.Method.DELETE, urlDelete,
                response -> {
                    Toast.makeText(this, "Moto borrada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Error al borrar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(request);
    }
}
