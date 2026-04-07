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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Código por defecto de Android Studio para ajustar la vista a la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ejercicio 2 de Arquitectura: LogCat del ciclo de vida
        Log.d("CICLO_VIDA", "La app ha pasado a: onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Ejercicio 2 de Arquitectura: LogCat del ciclo de vida
        Log.d("CICLO_VIDA", "La app ha pasado a: onStart");
    }

    // Funciones que se ejecutan al pulsar cada botón (onClick) en el XML
    public void irACoches(View view){
        Intent intent = new Intent(this, ListadoCochesActivity.class);
        startActivity(intent);
    }

    public void irAMotos(View view){
        Intent intent = new Intent(this, ListadoMotosActivity.class);
        startActivity(intent);
    }

    public void irAFurgonetas(View view){
        Intent intent = new Intent(this, ListadoFurgonetasActivity.class);
        startActivity(intent);
    }

    public void irAMantenimientos(View view){
        Intent intent = new Intent(this, MantenimientoActivity.class);
        startActivity(intent);
    }
}
