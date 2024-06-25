package com.example.offlinemap;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.mapsforge.BuildConfig;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.GEMFFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class MainActivity extends AppCompatActivity implements IRegisterReceiver {

    private static final String TAG = "OfflineMap";
    private MapView mapView;
    private Button btnShowMap;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Copy GEMF file from assets to internal storage
        copyGemfFile();

        // Initialize OSMDroid configuration
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        mapView = findViewById(R.id.mapView);
        btnShowMap = findViewById(R.id.btnShowMap);

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Map is now visible", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the GEMF file
        showMsg("Started");
        File gemfFile = new File(getFilesDir(), "Offline_Map.gemf");
        if (gemfFile.exists()) {
            Log.d(TAG, "GEMF file exists: " + gemfFile.getAbsolutePath());
//            showMsg(gemfFile.getAbsolutePath());
            try {
                GEMFFileArchive gemfArchive = GEMFFileArchive.getGEMFFileArchive(gemfFile);
                Log.d(TAG, "GEMF file loaded successfully.");
//                showMsg("GEMF file loaded successfully.");

                // Set up the tile source
                XYTileSource tileSource = new XYTileSource("GEMF", 0, 18, 256, ".png", new String[]{});

                // Set up the tile provider to use the GEMF file
                MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
                tileProvider.setTileSource(tileSource);

                // Create the MapTileFileArchiveProvider correctly
//                showMsg("try 5");
                MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(
                        this, // Use the context
                        tileSource,
                        new GEMFFileArchive[]{gemfArchive}
                );
//                showMsg("try 6");

                MapTileProviderArray providerArray = new MapTileProviderArray(tileSource, null, new MapTileModuleProviderBase[]{archiveProvider});
                providerArray.getTileRequestCompleteHandlers().add(new SimpleInvalidationHandler(mapView));

                // Set the tile provider to the map view
                mapView.setTileProvider(providerArray);
                mapView.setTileSource(tileSource);
                Log.d(TAG, "Tile provider and tile source set successfully.");

                // Set the default zoom level and center point
                mapView.getController().setZoom(15.0);
                GeoPoint startPoint = new GeoPoint(34.36059872403241, 73.47664953453541); // Example coordinates
                mapView.getController().setCenter(startPoint);

                // Add markers
                addMarker(startPoint, "Jalalabad Garden Muzafarabad");

                GeoPoint secondPoint = new GeoPoint(34.35369441962124, 73.47796217922111);
                addMarker(secondPoint, "Muzafarabad Cricket Stadium");

                GeoPoint thirdPoint = new GeoPoint(34.35841600879067, 73.47018190008589);
                addMarker(thirdPoint, "Aqua Arena Swimming Pool");

                Log.d(TAG, "Markers added successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error loading GEMF file.", e);
                showMsg("Error loading GEMF file: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "GEMF file does not exist.");
            showMsg("GEMF file does not exist.");
        }
    }

    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);

        mapView.getOverlays().add(marker);
    }

    private void copyGemfFile() {
        try {
//            showMsg("inside copyGemfFile");
            Log.d(TAG, "inside copyGemfFile");
            InputStream inputStream = getAssets().open("Offline_Map.gemf");
//            showMsg("getting assets");
            Log.d(TAG, "getting assets");
            File outFile = new File(getFilesDir(), "Offline_Map.gemf");
//            showMsg("outfile created");
            Log.d(TAG, "outfile created: " + outFile.getAbsolutePath());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try (OutputStream outputStream = Files.newOutputStream(outFile.toPath())) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.flush();
//                    showMsg("GEMF file copied successfully");
                    Log.d(TAG, "GEMF file copied successfully");
                }
            }
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error copying GEMF file.", e);
            showMsg("Error copying GEMF file: " + e.getMessage());
        }
    }

    void showMsg(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void destroy() {

    }
}
