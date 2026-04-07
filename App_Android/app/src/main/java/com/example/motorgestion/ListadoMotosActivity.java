package com.example.motorgestion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.motorgestion.model.Moto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class ListadoMotosActivity extends AppCompatActivity {

    private ListView listView;
    private RequestQueue queue;
    private List<Moto> listado = new ArrayList<>();
    private static final String URL = "http://10.0.2.2:9000/api/motos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listado_motos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listaListView);
        queue = Volley.newRequestQueue(this);

        cargarMotosHTTP();
    }

    private void cargarMotosHTTP() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        listado = gson.fromJson(response.toString(), new TypeToken<List<Moto>>(){}.getType());

                        List<String> nombres = new ArrayList<>();
                        for(Moto m : listado) {
                            nombres.add(m.getModelo());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ListadoMotosActivity.this,
                                android.R.layout.simple_list_item_1, nombres);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            Intent intent = new Intent(ListadoMotosActivity.this, DetalleMotoActivity.class);
                            intent.putExtra("ID_MOTO", listado.get(i).getNum());
                            startActivity(intent);
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ListadoMotosActivity.this, "Error de red: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(request);
    }
}
