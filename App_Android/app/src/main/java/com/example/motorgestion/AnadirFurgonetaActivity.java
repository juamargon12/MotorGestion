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

public class AnadirFurgonetaActivity extends AppCompatActivity {

    private EditText etModelo, etMatricula, etCombustible, etCarga, etZona;
    private RequestQueue queue;
    private static final String URL = "http://10.0.2.2:9000/api/furgonetas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anadir_furgoneta);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        queue = Volley.newRequestQueue(this);

        etModelo      = findViewById(R.id.etModelo);
        etMatricula   = findViewById(R.id.etMatricula);
        etCombustible = findViewById(R.id.etCombustible);
        etCarga       = findViewById(R.id.etCarga);
        etZona        = findViewById(R.id.etZona);

        Button btnAgregar = findViewById(R.id.btnAgregar);
        btnAgregar.setOnClickListener(view -> {
            // Validar antes de enviar (Tema 03 — IU / validación de formularios)
            if (validarFormulario()) {
                insertarFurgonetaRest();
            }
        });
    }

    /**
     * Valida todos los campos obligatorios, incluyendo:
     * - combustible: debe ser uno de los valores aceptados por la BD
     * - carga_maxima: debe ser un número positivo
     * @return true si el formulario es válido
     */
    private boolean validarFormulario() {
        boolean valido = true;

        if (etModelo.getText().toString().trim().isEmpty()) {
            etModelo.setError("El modelo es obligatorio");
            valido = false;
        } else {
            etModelo.setError(null);
        }

        if (etMatricula.getText().toString().trim().isEmpty()) {
            etMatricula.setError("La matrícula es obligatoria");
            valido = false;
        } else {
            etMatricula.setError(null);
        }

        // Validar combustible contra los valores del CHECK constraint de la BD
        String combustible = etCombustible.getText().toString().trim();
        if (combustible.isEmpty()) {
            etCombustible.setError("El combustible es obligatorio");
            valido = false;
        } else if (!combustible.equalsIgnoreCase("Diesel")
                && !combustible.equalsIgnoreCase("Gasolina")
                && !combustible.equalsIgnoreCase("Electrico")
                && !combustible.equalsIgnoreCase("Hibrido")) {
            etCombustible.setError("Debe ser: Diesel, Gasolina, Electrico o Hibrido");
            valido = false;
        } else {
            etCombustible.setError(null);
        }

        // Validar que carga_maxima es un número positivo
        String cargaStr = etCarga.getText().toString().trim();
        if (cargaStr.isEmpty()) {
            etCarga.setError("La carga máxima es obligatoria");
            valido = false;
        } else {
            try {
                double carga = Double.parseDouble(cargaStr);
                if (carga <= 0) {
                    etCarga.setError("La carga debe ser un número positivo");
                    valido = false;
                } else {
                    etCarga.setError(null);
                }
            } catch (NumberFormatException e) {
                etCarga.setError("Debe ser un número válido (ej: 800.5)");
                valido = false;
            }
        }

        if (etZona.getText().toString().trim().isEmpty()) {
            etZona.setError("La zona es obligatoria");
            valido = false;
        } else {
            etZona.setError(null);
        }

        return valido;
    }

    private void insertarFurgonetaRest() {
        try {
            JSONObject parametroJSON = new JSONObject();
            parametroJSON.put("modelo", etModelo.getText().toString().trim());
            parametroJSON.put("matricula", etMatricula.getText().toString().trim());
            parametroJSON.put("combustible", etCombustible.getText().toString().trim());
            parametroJSON.put("carga_maxima", Double.parseDouble(etCarga.getText().toString().trim()));
            parametroJSON.put("zona", etZona.getText().toString().trim());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, parametroJSON,
                    response -> {
                        Toast.makeText(this, "Furgoneta creada con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> Toast.makeText(this, "Error al crear: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );
            queue.add(request);
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
