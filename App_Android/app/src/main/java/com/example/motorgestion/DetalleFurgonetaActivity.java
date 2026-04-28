package com.example.motorgestion;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.motorgestion.model.Furgoneta;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class DetalleFurgonetaActivity extends AppCompatActivity {

    private long furgonetaId;
    private RequestQueue queue;
    private static final String URL_BASE = "http://10.0.2.2:9000/api/furgonetas/";

    private EditText etModelo, etMatricula, etCombustible, etCarga, etZona;
    private ImageView imgFoto;
    private String fotoBase64 = ""; // Base64 de la foto actual

    // Extras de sesión recibidos desde el Listado
    private String rolUsuario;
    private boolean offlineMode;

    // ActivityResultLauncher para la cámara (Tema 05 — captura de foto con cámara)
    private final ActivityResultLauncher<Void> camaraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    imgFoto.setImageBitmap(bitmap);
                    // Convertir Bitmap a Base64 para enviar al backend
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    fotoBase64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
                }
            });

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
        rolUsuario  = getIntent().getStringExtra("ROL_USUARIO");
        offlineMode = getIntent().getBooleanExtra("OFFLINE_MODE", false);
        if (rolUsuario == null) rolUsuario = "EMPLEADO";

        imgFoto       = findViewById(R.id.imgFoto);
        etModelo      = findViewById(R.id.etModelo);
        etMatricula   = findViewById(R.id.etMatricula);
        etCombustible = findViewById(R.id.etCombustible);
        etCarga       = findViewById(R.id.etCarga);
        etZona        = findViewById(R.id.etZona);

        Button btnFoto     = findViewById(R.id.btnFoto);
        Button btnGuardar  = findViewById(R.id.btnGuardar);
        Button btnEliminar = findViewById(R.id.btnEliminar);

        // Lógica de roles (Tema 02): EMPLEADO solo puede ver, no editar
        if ("EMPLEADO".equals(rolUsuario) || offlineMode) {
            etModelo.setEnabled(false);
            etMatricula.setEnabled(false);
            etCombustible.setEnabled(false);
            etCarga.setEnabled(false);
            etZona.setEnabled(false);
            btnFoto.setVisibility(android.view.View.GONE);
            btnGuardar.setVisibility(android.view.View.GONE);
            btnEliminar.setVisibility(android.view.View.GONE);
        } else {
            btnFoto.setOnClickListener(view -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    camaraLauncher.launch(null);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA}, 100);
                }
            });
            btnGuardar.setOnClickListener(view -> actualizarFurgonetaRest());
            btnEliminar.setOnClickListener(view -> eliminarFurgonetaRest());
        }

        cargarDatosFurgoneta();
    }

    // Al conceder el permiso en tiempo de ejecución, lanza la cámara
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            camaraLauncher.launch(null);
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
        }
    }

    // GET — carga los datos de la furgoneta desde el backend y los muestra
    private void cargarDatosFurgoneta() {
        if (furgonetaId <= 0) {
            Toast.makeText(this, "ID de furgoneta no válido: " + furgonetaId, Toast.LENGTH_SHORT).show();
            return;
        }
        
        String urlGet = URL_BASE + furgonetaId;
        android.util.Log.d("REST_DEBUG", "Cargando datos desde: " + urlGet);

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, urlGet, null,
                response -> {
                    Gson gson = new Gson();
                    Furgoneta f = gson.fromJson(response.toString(), Furgoneta.class);
                    etModelo.setText(f.getModelo());
                    etMatricula.setText(f.getMatricula());
                    etCombustible.setText(f.getCombustible());
                    etCarga.setText(String.valueOf(f.getCargaMaxima()));
                    etZona.setText(f.getZona());

                    // Mostrar foto si existe (Base64 → Bitmap → ImageView)
                    if (f.getFoto() != null && !f.getFoto().isEmpty()) {
                        fotoBase64 = f.getFoto();
                        byte[] decoded = Base64.decode(fotoBase64, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        imgFoto.setImageBitmap(bmp);
                    }
                },
                error -> {
                    String msg = "Error al cargar datos";
                    if (error.networkResponse != null) {
                        msg += " (HTTP " + error.networkResponse.statusCode + ")";
                        android.util.Log.e("REST_DEBUG", "Error HTTP: " + error.networkResponse.statusCode);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(getRequest);
    }

    // PUT — actualiza todos los campos de la furgoneta en el backend
    private void actualizarFurgonetaRest() {
        if (furgonetaId == -1) return;

        // Validación básica antes de enviar
        if (etModelo.getText().toString().trim().isEmpty()) {
            etModelo.setError("El modelo no puede estar vacío");
            return;
        }
        String combustible = etCombustible.getText().toString().trim();
        if (!combustible.equalsIgnoreCase("Diesel") && !combustible.equalsIgnoreCase("Gasolina")
                && !combustible.equalsIgnoreCase("Electrico") && !combustible.equalsIgnoreCase("Hibrido")) {
            etCombustible.setError("Debe ser: Diesel, Gasolina, Electrico o Hibrido");
            return;
        }

        try {
            double carga = Double.parseDouble(etCarga.getText().toString().trim());
            JSONObject json = new JSONObject();
            json.put("modelo", etModelo.getText().toString().trim());
            json.put("matricula", etMatricula.getText().toString().trim());
            json.put("combustible", combustible);
            json.put("carga_maxima", carga);
            json.put("zona", etZona.getText().toString().trim());
            json.put("foto", fotoBase64);

            String urlPut = URL_BASE + furgonetaId;
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, urlPut, json,
                    response -> Toast.makeText(this, "Furgoneta actualizada correctamente", Toast.LENGTH_SHORT).show(),
                    error -> Toast.makeText(this, "Error al actualizar: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );
            queue.add(putRequest);
        } catch (NumberFormatException e) {
            etCarga.setError("Debe ser un número válido");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // DELETE — borra la furgoneta y vuelve al listado
    private void eliminarFurgonetaRest() {
        if (furgonetaId == -1) return;

        String urlDelete = URL_BASE + furgonetaId;
        StringRequest request = new StringRequest(Request.Method.DELETE, urlDelete,
                response -> {
                    Toast.makeText(this, "Furgoneta borrada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Error al borrar", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }
}
