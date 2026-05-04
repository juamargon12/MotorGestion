package com.example.motorgestion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etPassword;
    private Button btnLogin, btnOffline;
    private TextView tvStatusServer;

    private static final String LOGIN_URL = "http://10.0.2.2:9000/api/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsuario      = findViewById(R.id.etUsuario);
        etPassword     = findViewById(R.id.etPassword);
        btnLogin       = findViewById(R.id.btnLogin);
        btnOffline     = findViewById(R.id.btnOffline);
        tvStatusServer = findViewById(R.id.tvStatusServer);

        btnLogin.setOnClickListener(v -> login());

        // Acceso sin conexión: entra como EMPLEADO en modo sólo lectura (Tema 04 — Persistencia)
        btnOffline.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("ROL_USUARIO", "EMPLEADO");
            intent.putExtra("OFFLINE_MODE", true);
            startActivity(intent);
            finish();
        });
    }

    private void login() {
        String user = etUsuario.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Llamada en hilo secundario — Android no permite operaciones de red en el hilo principal
        new Thread(() -> {
            try {
                URL url = new URL(LOGIN_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(5000);
                conn.setDoOutput(true);

                // Construimos el JSON manualmente (Tema 05 — Servicios Web REST)
                String jsonInputString = "{\"usuario\": \"" + user + "\", \"password\": \"" + pass + "\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 200 OK → leemos el rol devuelto por el servidor ("JEFE" o "EMPLEADO")
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String rol = in.readLine().trim();

                    runOnUiThread(() -> {
                        tvStatusServer.setVisibility(View.GONE);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("ROL_USUARIO", rol);
                        intent.putExtra("OFFLINE_MODE", false);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    // 401 Unauthorized u otro error
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show());
                }

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    tvStatusServer.setVisibility(View.VISIBLE);
                    Toast.makeText(LoginActivity.this, "Error conectando al servidor", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}