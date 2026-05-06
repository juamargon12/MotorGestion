package com.example.motorgestion;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

/**
 * SyncManager — Gestiona la caché local (Tema 04 — Persistencia de datos: SharedPreferences)
 *
 * Descarga todos los listados del servidor y los guarda en SharedPreferences.
 * Cuando no hay conexión, las actividades pueden recuperar estos datos para mostrarlos
 * en modo sólo lectura (OFFLINE_MODE).
 */
public class SyncManager {

    private static final String PREFS_CACHE = "MotorGestionCache";
    private static final String BASE_URL    = "http://10.0.2.2:9000/api";

    // Endpoints y sus claves de caché correspondientes
    private static final String[] ENDPOINTS  = {"coches", "motos", "furgonetas", "mantenimientos"};
    private static final String[] CACHE_KEYS = {"cache_coches", "cache_motos", "cache_furgonetas", "cache_mantenimientos"};

    /**
     * Sincroniza todos los listados del servidor en background y los almacena en SharedPreferences.
     * Se llama desde MainActivity nada más iniciar sesión.
     */
    public static void syncAll(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE);

        for (int i = 0; i < ENDPOINTS.length; i++) {
            final String key = CACHE_KEYS[i];
            String url = BASE_URL + "/" + ENDPOINTS[i];

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        // Guardamos el JSON completo en SharedPreferences como String
                        prefs.edit().putString(key, response.toString()).apply();
                        Log.d("SyncManager", "Caché actualizada: " + key);
                    },
                    error -> Log.e("SyncManager", "Error sincronizando " + key + ": " + error.getMessage())
            );
            queue.add(request);
        }
    }

    /** Devuelve el JSON en caché para una clave dada, o null si no hay caché. */
    public static String getCache(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    /** Guarda un JSON en caché para una clave dada. */
    public static void saveCache(Context context, String key, String json) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE);
        prefs.edit().putString(key, json).apply();
        Log.d("SyncManager", "Caché guardada manualmente: " + key);
    }
}
