package com.example.motorgestion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private String rolUsuario;
    private boolean offlineMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Recibir el rol y el modo offline del Intent (enviados desde LoginActivity)
        rolUsuario  = getIntent().getStringExtra("ROL_USUARIO");
        offlineMode = getIntent().getBooleanExtra("OFFLINE_MODE", false);

        if (rolUsuario == null) rolUsuario = "EMPLEADO"; // Valor por defecto por seguridad

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d("CICLO_VIDA", "onCreate — ROL: " + rolUsuario + " | OFFLINE: " + offlineMode);

        // Sincronizar caché si hay conexión (Tema 04 — SharedPreferences)
        if (!offlineMode) {
            SyncManager.syncAll(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("CICLO_VIDA", "La app ha pasado a: onStart");
    }

    /** Cierra la sesión y vuelve al Login */
    public void cerrarSesion(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Funciones que se ejecutan al pulsar cada botón (onClick) en el XML
    public void irACoches(View view) {
        Intent intent = new Intent(this, ListadoCochesActivity.class);
        intent.putExtra("ROL_USUARIO", rolUsuario);
        intent.putExtra("OFFLINE_MODE", offlineMode);
        startActivity(intent);
    }

    public void irAMotos(View view) {
        Intent intent = new Intent(this, ListadoMotosActivity.class);
        intent.putExtra("ROL_USUARIO", rolUsuario);
        intent.putExtra("OFFLINE_MODE", offlineMode);
        startActivity(intent);
    }

    public void irAFurgonetas(View view) {
        Intent intent = new Intent(this, ListadoFurgonetasActivity.class);
        intent.putExtra("ROL_USUARIO", rolUsuario);
        intent.putExtra("OFFLINE_MODE", offlineMode);
        startActivity(intent);
    }

    public void irAMantenimientos(View view) {
        Intent intent = new Intent(this, MantenimientoActivity.class);
        intent.putExtra("ROL_USUARIO", rolUsuario);
        intent.putExtra("OFFLINE_MODE", offlineMode);
        startActivity(intent);
    }
}
