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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class AnadirMotoActivity extends AppCompatActivity {

    private EditText etModelo, etBastidor, etMatricula, etAnio, etZona;
    private RequestQueue queue;
    private static final String URL = "http://10.0.2.2:9000/api/motos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anadir_moto);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        queue = Volley.newRequestQueue(this);

        etModelo    = findViewById(R.id.etModelo);
        etBastidor  = findViewById(R.id.etBastidor);
        etMatricula = findViewById(R.id.etMatricula);
        etAnio      = findViewById(R.id.etAnio);
        etZona      = findViewById(R.id.etZona);

        Button btnAgregar = findViewById(R.id.btnAgregar);
        btnAgregar.setOnClickListener(view -> {
            // Validar antes de enviar (Tema 03 — IU / validación de formularios)
            if (validarFormulario()) {
                insertarMotoRest();
            }
        });
    }

    /**
     * Valida que todos los campos obligatorios estén rellenos.
     * Usa setError() para marcar el campo en rojo con un mensaje descriptivo.
     * @return true si el formulario es válido, false si hay algún error
     */
    private boolean validarFormulario() {
        boolean valido = true;

        if (etModelo.getText().toString().trim().isEmpty()) {
            etModelo.setError("El modelo es obligatorio");
            valido = false;
        } else {
            etModelo.setError(null);
        }

        if (etBastidor.getText().toString().trim().isEmpty()) {
            etBastidor.setError("El número de bastidor es obligatorio");
            valido = false;
        } else {
            etBastidor.setError(null);
        }

        if (etMatricula.getText().toString().trim().isEmpty()) {
            etMatricula.setError("La matrícula es obligatoria");
            valido = false;
        } else {
            etMatricula.setError(null);
        }

        String anio = etAnio.getText().toString().trim();
        if (anio.isEmpty()) {
            etAnio.setError("El año de fabricación es obligatorio");
            valido = false;
        } else if (anio.length() != 4) {
            etAnio.setError("El año debe tener 4 dígitos");
            valido = false;
        } else {
            etAnio.setError(null);
        }

        if (etZona.getText().toString().trim().isEmpty()) {
            etZona.setError("La zona es obligatoria");
            valido = false;
        } else {
            etZona.setError(null);
        }

        return valido;
    }

    private void insertarMotoRest() {
        try {
            JSONObject parametroJSON = new JSONObject();
            parametroJSON.put("modelo", etModelo.getText().toString().trim());
            parametroJSON.put("n_bastidor", etBastidor.getText().toString().trim());
            parametroJSON.put("matricula", etMatricula.getText().toString().trim());
            parametroJSON.put("anio_fabricacion", etAnio.getText().toString().trim());
            parametroJSON.put("zona", etZona.getText().toString().trim());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, parametroJSON,
                    response -> {
                        Toast.makeText(this, "Moto creada con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> Toast.makeText(this, "Error al crear: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
