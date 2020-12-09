package com.identidad.semaforoapp;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    public LatLng ll, llmed;
    Double longitudOrigen = 0.00, latitudOrigen = 0.00, latitudDestino = 0.00, longitudDestino = 0.00, latm, longm;
    JSONObject jso;

    String ruta_id, ruta_nombre, lat_inicio, long_inicio, lat_fin, long_fin, estado, rutas_alternas;

    EditText etSearchMap;
    Button btnSearchMapa;
    TextView txtEstado, txtRutaAlterna, txtRutaAlternaTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etSearchMap = findViewById(R.id.etSearchMap);
        btnSearchMapa = findViewById(R.id.btnSearchMapa);
        txtEstado = findViewById(R.id.txtEstado);
        txtRutaAlterna = findViewById(R.id.txtRutaAlterna);
        txtRutaAlternaTitle = findViewById(R.id.txtRutaAlternaTitle);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        btnSearchMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchmap = etSearchMap.getText().toString();
                if(searchmap.isEmpty()) {
                    etSearchMap.setError("Campo vacio");
                }else{
                    leerDatos();
                }
            }
        });


    }

    private void leerDatos() {
        //String url = "http://192.168.1.2/semaforo/Semaforo/search_ruta.php?name=" + etSearchMap.getText().toString();
        String url = "http://smartcityhyo.tk/api/Semaforo/search_ruta.php?name=" + etSearchMap.getText().toString();


        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("ruta");

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject a = array.getJSONObject(i);

                        ruta_id = a.getString("ruta_id");
                        ruta_nombre = a.getString("ruta_nombre");
                        lat_inicio = a.getString("lat_inicio");
                        long_inicio = a.getString("long_inicio");
                        lat_fin = a.getString("lat_fin");
                        long_fin = a.getString("long_fin");
                        estado = a.getString("estado");
                        rutas_alternas = a.getString("rutas_alternas");

                        guardarPreferenciaMapas(lat_inicio, long_inicio, lat_fin, long_fin, estado, rutas_alternas);

                        //guardarPreferenciaMapas("", "", "", "", "Vacio", "Vacio");

                        /*if(lat_inicio=="null" || long_inicio=="null" || lat_fin=="null" || long_fin=="null"){
                            guardarPreferenciaMapas("", "", "", "", "Vacio", "Vacio");
                        }else{
                            guardarPreferenciaMapas(lat_inicio, long_inicio, lat_fin, long_fin, estado, rutas_alternas);
                        }*/

                        finish();
                        startActivity(getIntent());

                    }

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    txtEstado.setText("Estado ...");
                    txtRutaAlterna.setText("Rutas alterna ...");
                    guardarPreferenciaMapas("", "", "", "", "", "");
                    finish();
                    startActivity(getIntent());
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Lista Vacía."+error.toString(), Toast.LENGTH_SHORT).show();
                        txtEstado.setText("Estado ...");
                        txtRutaAlterna.setText("Rutas alterna ...");
                        guardarPreferenciaMapas("", "", "", "", "", "");
                        finish();
                        startActivity(getIntent());
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }


    public void onMapReady(GoogleMap googleMap) {

        SharedPreferences preferences = getSharedPreferences("preferenciasMapa", Context.MODE_PRIVATE);
        String lat_inicio_s = preferences.getString("lat_inicio_p", "");
        String long_inicio_s = preferences.getString("long_inicio_p", "");
        String lat_fin_s = preferences.getString("lat_fin_p", "");
        String long_fin_s = preferences.getString("long_fin_p", "");
        String estado_s = preferences.getString("estado_p", "Vacio");
        String rutas_alternas_s = preferences.getString("rutas_alternar_p", "Vacio");

        String lat_inicio_replace = lat_inicio_s.replaceAll(" ", "");
        String long_inicio_replace = long_inicio_s.replaceAll(" ", "");
        String lat_fin_replace = lat_fin_s.replaceAll(" ", "");
        String long_fin_replace = long_fin_s.replaceAll(" ", "");

        if (lat_inicio_replace.isEmpty() || long_inicio_replace.isEmpty() || lat_fin_replace.isEmpty() || long_fin_replace.isEmpty() || lat_inicio_replace.equals("null") || long_inicio_replace.equals("null") || lat_fin_replace.equals("null") || long_fin_replace.equals("null") ) {
            latitudOrigen = 0.00;
            longitudOrigen = 0.00;
            latitudDestino = 0.00;
            longitudDestino = 0.00;
        } else {
            latitudOrigen = Double.parseDouble(lat_inicio_replace);
            longitudOrigen = Double.parseDouble(long_inicio_replace);
            latitudDestino = Double.parseDouble(lat_fin_replace);
            longitudDestino = Double.parseDouble(long_fin_replace);
        }


        if (latitudOrigen == 0.00 || latitudDestino == 0.00 || longitudOrigen == 0.00 || longitudDestino == 0.00) {
            latitudOrigen = -12.061193;
            latitudDestino = -75.2148308;
            mMap = googleMap;
            ll = new LatLng(latitudOrigen, longitudOrigen);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.addMarker(new MarkerOptions().position(ll).title("Mapa Vacio").snippet("Consulte una ubicacion"));
            txtEstado.setText("Estado ...");
            txtRutaAlterna.setText("Rutas alterna ...");
            Toast.makeText(this, "Campos vacios, intente con otra ubicación.", Toast.LENGTH_SHORT).show();
        } else {

            txtEstado.setText(estado_s);
            txtRutaAlterna.setText(rutas_alternas_s);
            if (estado_s.equals("Libre")) {
                txtRutaAlterna.setVisibility(View.INVISIBLE);
                txtRutaAlternaTitle.setVisibility(View.INVISIBLE);
            } else {

                txtRutaAlterna.setVisibility(View.VISIBLE);
                txtRutaAlternaTitle.setVisibility(View.VISIBLE);
            }

            mMap = googleMap;
            ll = new LatLng(latitudOrigen, longitudOrigen);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.addMarker(new MarkerOptions().position(ll).title("Origen").snippet("Origen ver"));


            ll = new LatLng(latitudDestino, longitudDestino);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.addMarker(new MarkerOptions().position(ll).title("Destino").snippet("Destino ver"));

            latm = (latitudOrigen + latitudDestino) / 2;
            longm = (longitudOrigen + longitudDestino) / 2;

            llmed = new LatLng(latm, longm);


            CameraPosition cameraPosition = CameraPosition.builder().target(llmed).zoom(18).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            String key = "AIzaSyBwINK2wOmTj0NyOqo4nd7lYYGR6M1tNV8";
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + latitudOrigen + "%2C" + longitudOrigen + "&destination=" + latitudDestino + "%2C" + longitudDestino + "&key=" + key;
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {


                    try {
                        jso = new JSONObject(response);
                        trazarRuta(jso);
                        Log.i("jsonRuta: ", "" + response);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(stringRequest);
        }
    }

    private void trazarRuta(JSONObject jso) {
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {
            jRoutes = jso.getJSONArray("routes");
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) (jRoutes.get(i))).getJSONArray("legs");
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "" + ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        Log.i("end", "" + polyline);
                        List<LatLng> list = PolyUtil.decode(polyline);
                        mMap.addPolyline(new PolylineOptions().addAll(list).color(Color.GRAY).width(5));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void guardarPreferenciaMapas(String lat_inicio_p, String long_inicio_p, String lat_fin_p, String long_fin_p, String estado_p, String rutas_alternar_p) {
        SharedPreferences preferences = getSharedPreferences("preferenciasMapa", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lat_inicio_p", lat_inicio_p);
        editor.putString("long_inicio_p", long_inicio_p);
        editor.putString("lat_fin_p", lat_fin_p);
        editor.putString("long_fin_p", long_fin_p);
        editor.putString("estado_p", estado_p);
        editor.putString("rutas_alternar_p", rutas_alternar_p);
        editor.commit();
    }
}