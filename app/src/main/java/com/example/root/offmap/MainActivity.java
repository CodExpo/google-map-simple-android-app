package com.example.root.offmap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends Activity implements MapEventsReceiver {
    public final static String EXTRA_MESSAGE = "com.example.root.offmap.MESSAGE";
    public static final GeoPoint TEH = new GeoPoint(35.698745, 51.398764);
    private MyLocationOverlay myLocationoverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MapView mapView = (MapView) findViewById(R.id.mapview);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        mapView.getOverlays().add(0, mapEventsOverlay);

        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);
        mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);

        IMapController mapViewController = mapView.getController();
        mapViewController.setZoom(13);
        mapViewController.setCenter(TEH);

        ImageButton zoomIn = (ImageButton) findViewById(R.id.imageButton);
        ImageButton zoomOut = (ImageButton) findViewById(R.id.imageButton2);

        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.getController().zoomIn();
                checkzoom();
            }
        });

        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.getController().zoomOut();
                checkzoom();
            }
        });


        myLocationoverlay = new MyLocationOverlay(this, mapView);
        myLocationoverlay.enableMyLocation();
        myLocationoverlay.disableCompass();
        myLocationoverlay.enableFollowLocation();
        myLocationoverlay.setDrawAccuracyEnabled(true);
        myLocationoverlay.runOnFirstFix(new Runnable() {
            public void run() {
                if (myLocationoverlay.getMyLocation() != null) {
                    // go to MyLocation
                    mapView.getController().animateTo(myLocationoverlay
                            .getMyLocation());
                }
            }
        });

        final Marker startMarker = new Marker(mapView);
        //startMarker.setPosition(myLocationoverlay.getMyLocation());
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(startMarker);





/*
        InputStream in = null;
        in = getResources().openRawResource(R.raw.points);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line="";
        try {
            while ((line = br.readLine()) != null) {
                String[] point = line.split(" ");
                Marker m = new Marker(mapView);
                GeoPoint g = new GeoPoint(Double.parseDouble(point[1]),Double.parseDouble(point[0]));
                m.setPosition(g);
                poiMarkers.add(m);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        /*
        Log.d("sss", "dddddddddddddddddddddddddddddddddddddd");
        KmlDocument kmlDocument = new KmlDocument();
        String url = "http://farshadjafari.pythonanywhere.com/exp.kml";
        kmlDocument.parseKMLUrl(url);
        FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.mKmlRoot.buildOverlay(mapView, null, null, kmlDocument);
        mapView.getOverlays().add(kmlOverlay);
*/
        Button button= (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadWebPageTask(mapView,1).execute("http://farshadjafari.pythonanywhere.com/data.geojson");
                mapView.getOverlays().clear();
                mapView.invalidate();
            }
        });
        Button button2= (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadWebPageTask(mapView,2).execute("http://farshadjafari.pythonanywhere.com/data.geojson");
                mapView.getOverlays().clear();
                mapView.invalidate();
            }
        });
        Button button3= (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadWebPageTask(mapView,3).execute("http://farshadjafari.pythonanywhere.com/data.geojson");
                mapView.getOverlays().clear();
                mapView.invalidate();
            }
        });
        Button button4= (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadWebPageTask(mapView, 4).execute("http://farshadjafari.pythonanywhere.com/data.geojson");
                mapView.getOverlays().clear();
                mapView.invalidate();

            }
        });
        //new DownloadWebPageTask(mapView,fil).execute("http://farshadjafari.pythonanywhere.com/data.geojson");
        mapView.invalidate();

        // mapView.getOverlays().add(poiMarkers);

        Button reportButton = (Button) findViewById(R.id.button5);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Report(view,TEH);
            }
        });
    }



    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        private MapView mapView;
        private int filter;

        public DownloadWebPageTask(MapView mv,int f) {
            this.mapView= mv;
            this.filter = f;
        }
        @Override
        protected String doInBackground(String... params) {
            String url = "";
            switch (filter){
                case 1:
                    url = "http://farshadjafari.pythonanywhere.com/1/data.geojson";
                    break;
                case 2:
                    url = "http://farshadjafari.pythonanywhere.com/2/data.geojson";
                    break;
                case 3:
                    url = "http://farshadjafari.pythonanywhere.com/3/data.geojson";
                    break;
                case 4:
                    url = "http://farshadjafari.pythonanywhere.com/4/data.geojson";
                    break;
            }

            String response = "";

            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(String.valueOf(url));
            try {
                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();

                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            RadiusMarkerClusterer poiMarkers = new RadiusMarkerClusterer(mapView.getContext());
            Drawable clusterIconD = getResources().getDrawable(R.drawable.m1);
            Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
            poiMarkers.setIcon(clusterIcon);

            JSONObject jarr = null;
            try {
                jarr = new JSONObject(result);
                //JSONObject jobject = (JSONObject)jarr.getJSONArray("")[1];
                JSONArray st = jarr.getJSONArray("features");
                for(int i=0;i<st.length();i++)
                {
                    JSONObject entry = st.getJSONObject(i);
                    JSONObject geometry = entry.getJSONObject("geometry");
                    JSONArray coor = geometry.getJSONArray("coordinates");
                    Marker m = new Marker(mapView);
                    double lat = Double.parseDouble(coor.getString(1));
                    double lng = Double.parseDouble(coor.getString(0));
                    GeoPoint g = new GeoPoint(lat,lng);
                    m.setPosition(g);
                    poiMarkers.add(m);
                    // loop and add it to array or arraylist
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //KmlDocument kmlDocument = new KmlDocument();
           // kmlDocument.parseGeoJSON(result);
           // FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.mKmlRoot.buildOverlay(mapView, null, null, kmlDocument);

            mapView.getOverlays().add(poiMarkers);
            //mapView.getOverlays().add(kmlOverlay);
        }
    }
    public void changeback(View view) {
        String i = (String) view.getTag();
        switch (view.getId()) {
            case R.id.button:
                Button imb = (Button) findViewById(R.id.button);
                imb.setBackgroundResource(R.drawable.transition);
                TransitionDrawable trns = (TransitionDrawable) imb.getBackground();
                trns.startTransition(1000);
                trns.reverseTransition(1000);
                break;
            case R.id.button2:
                Button imb2 = (Button) findViewById(R.id.button2);
                imb2.setBackgroundResource(R.drawable.transition2);
                TransitionDrawable trns2 = (TransitionDrawable) imb2.getBackground();
                trns2.startTransition(1000);
                trns2.reverseTransition(1000);
                break;
            case R.id.button3:
                Button imb3 = (Button) findViewById(R.id.button3);
                imb3.setBackgroundResource(R.drawable.transition3);
                TransitionDrawable trns3 = (TransitionDrawable) imb3.getBackground();
                trns3.startTransition(1000);
                trns3.reverseTransition(1000);
                break;
            case R.id.button4:
                Button imb4 = (Button) findViewById(R.id.button4);
                imb4.setBackgroundResource(R.drawable.transition4);
                TransitionDrawable trns4 = (TransitionDrawable) imb4.getBackground();
                trns4.startTransition(1000);
                trns4.reverseTransition(1000);
                break;
        }
    }
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        Toast.makeText(this, "Tapped", Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        Toast.makeText(this, "Tap on ("+p.toString()+")", Toast.LENGTH_SHORT).show();
        return true;
    }

    public void checkzoom(){
        MapView mapView = (MapView)findViewById(R.id.mapview);
        ImageButton zoomIn = (ImageButton) findViewById(R.id.imageButton);
        ImageButton zoomOut = (ImageButton) findViewById(R.id.imageButton);
        if(mapView.getZoomLevel()==1) // dont know if 0 or 1, just wrote it
        {
            zoomOut.setEnabled(false);
            // or u can change drawable and disable click
        }
        if(mapView.getZoomLevel()==18) {
            zoomIn.setEnabled(false);
            // or u can change drawable and disable click
        }
    }
    public void Report(View view, GeoPoint g) {
        Intent intent = new Intent(this, PostReport.class);
        intent.putExtra(EXTRA_MESSAGE, g.toString());
        startActivity(intent);
    }
}
