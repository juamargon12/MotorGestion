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
import com.example.motorgestion.model.Coche;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class DetalleCocheActivity extends AppCompatActivity {

    private long cocheId;
    private RequestQueue queue;
    private static final String URL_BASE = "http://10.0.2.2:9000/api/coches/";

    private EditText etModelo, etBastidor, etMatricula, etAnio, etZona;
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
        setContentView(R.layout.activity_detalle_coche);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        queue = Volley.newRequestQueue(this);
        cocheId     = getIntent().getLongExtra("ID_COCHE", -1);
        rolUsuario  = getIntent().getStringExtra("ROL_USUARIO");
        offlineMode = getIntent().getBooleanExtra("OFFLINE_MODE", false);
        if (rolUsuario == null) rolUsuario = "EMPLEADO";

        imgFoto     = findViewById(R.id.imgFoto);
        etModelo    = findViewById(R.id.etModelo);
        etBastidor  = findViewById(R.id.etBastidor);
        etMatricula = findViewById(R.id.etMatricula);
        etAnio      = findViewById(R.id.etAnio);
        etZona      = findViewById(R.id.etZona);

        Button btnFoto     = findViewById(R.id.btnFoto);
        Button btnGuardar  = findViewById(R.id.btnGuardar);
        Button btnEliminar = findViewById(R.id.btnEliminar);

        // Lógica de roles (Tema 02): EMPLEADO solo puede ver, no editar
        if ("EMPLEADO".equals(rolUsuario) || offlineMode) {
            etModelo.setEnabled(false);
            etBastidor.setEnabled(false);
            etMatricula.setEnabled(false);
            etAnio.setEnabled(false);
            etZona.setEnabled(false);
            btnFoto.setVisibility(android.view.View.GONE);
            btnGuardar.setVisibility(android.view.View.GONE);
            btnEliminar.setVisibility(android.view.View.GONE);
        } else {
            // Botón cámara — solicita permiso si es necesario y abre la cámara
            btnFoto.setOnClickListener(view -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    camaraLauncher.launch(null);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA}, 100);
                }
            });
            btnGuardar.setOnClickListener(view -> actualizarCocheRest());
            btnEliminar.setOnClickListener(view -> eliminarCocheRest());
        }

        cargarDatosCoche();
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

    // GET — carga los datos del coche desde el backend y los muestra
    private void cargarDatosCoche() {
        if (cocheId <= 0) {
            Toast.makeText(this, "ID de coche no válido: " + cocheId, Toast.LENGTH_SHORT).show();
            return;
        }
        
        String urlGet = URL_BASE + cocheId;
        android.util.Log.d("REST_DEBUG", "Cargando datos desde: " + urlGet);

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, urlGet, null,
                response -> {
                    Gson gson = new Gson();
                    Coche coche = gson.fromJson(response.toString(), Coche.class);
                    etModelo.setText(coche.getModelo());
                    etBastidor.setText(coche.getNBastidor());
                    etMatricula.setText(coche.getMatricula());
                    etAnio.setText(coche.getAnioFabricacion());
                    etZona.setText(coche.getZona());

                    // Mostrar foto si existe (Base64 → Bitmap → ImageView)
                    if (coche.getFoto() != null && !coche.getFoto().isEmpty()) {
                        fotoBase64 = coche.getFoto();
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
                    } else if (error.getMessage() != null) {
                        msg += ": " + error.getMessage();
                        android.util.Log.e("REST_DEBUG", "Error de red: " + error.getMessage());
                    } else {
                        msg += ": sin conexión con el servidor";
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        );
        queue.add(getRequest);
    }

    // PUT — actualiza todos los campos del coche en el backend
    private void actualizarCocheRest() {
        if (cocheId == -1) return;

        // Validación básica antes de enviar
        if (etModelo.getText().toString().trim().isEmpty()) {
            etModelo.setError("El modelo no puede estar vacío");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("modelo", etModelo.getText().toString().trim());
            json.put("n_bastidor", etBastidor.getText().toString().trim());
            json.put("matricula", etMatricula.getText().toString().trim());
            json.put("anio_fabricacion", etAnio.getText().toString().trim());
            json.put("zona", etZona.getText().toString().trim());
            json.put("foto", fotoBase64); // Incluye foto en Base64 (puede estar vacía)

            String urlPut = URL_BASE + cocheId;
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, urlPut, json,
                    response -> Toast.makeText(this, "Coche actualizado correctamente", Toast.LENGTH_SHORT).show(),
                    error -> Toast.makeText(this, "Error al actualizar: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );
            queue.add(putRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // DELETE — borra el coche y vuelve al listado
    private void eliminarCocheRest() {
        if (cocheId == -1) return;

        String urlDelete = URL_BASE + cocheId;
        StringRequest request = new StringRequest(Request.Method.DELETE, urlDelete,
                response -> {
                    Toast.makeText(this, "Coche borrado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Error de red al borrar", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }
}
