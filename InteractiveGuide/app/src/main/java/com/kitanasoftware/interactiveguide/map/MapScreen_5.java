package com.kitanasoftware.interactiveguide.map;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kitanasoftware.interactiveguide.R;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class MapScreen_5 extends AppCompatActivity implements IRegisterReceiver, EditGeo {

    public static Boolean addingMode = false;
    public static Boolean editingMode = false;

    private Geopoint editedGeo;
    private int editedGeoPosition;

    private HybridMap mapView;
    private Button creationGeoOk;
    private Button creationGeoCancel;
    private Button addGeo;
    private Button editGeo;
    private Button delGeo;
    private ArrayList<OverlayItem> items;
    private MyItemizedOverlay myItemizedOverlay = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_screen_5);

        //map View
        mapView = (HybridMap) findViewById(R.id.hybridMap);

        //add geopoints on map
        createOverlay();

        //buttons
        Button zoomIn = (Button) findViewById(R.id.zoomIn);
        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.zoomIn();
            }
        });
        Button zoomOut = (Button) findViewById(R.id.zoomOut);
        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.zoomOut();
            }
        });
        creationGeoOk = (Button) findViewById(R.id.creationGeoOk);
        creationGeoCancel = (Button) findViewById(R.id.creationGeoCancel);
        addGeo = (Button) findViewById(R.id.addGeopoint);
        editGeo = (Button) findViewById(R.id.editGeopoints);
        delGeo = (Button) findViewById(R.id.delGeopoints);

    }


    public void onButtonClicked(View v) {

        if (v.getId() == R.id.addGeopoint) {
            clickedAddGeo();

        } else if (v.getId() == R.id.editGeopoints) {
            addingMode = false;
            EditingDialog editDialog = new EditingDialog();
            Bundle args = new Bundle();
            args.putString("clicked", "edit");
            editDialog.setArguments(args);
            editDialog.show(getFragmentManager(), "editGeo");

        } else if (v.getId() == R.id.creationGeoOk) {

            clickedCreationGeoOk();

        } else if (v.getId() == R.id.creationGeoCancel) {

            mainMode();
            //if geopoint was not accepted
            if (items.get(items.size() - 1).getTitle().equals("NewPoint")) {
                items.remove(items.size() - 1);
                createOverlay();
            }
        } else if (v.getId() == R.id.delGeopoints) {
            deleteGeopointDialog();
        }
    }


    private void clickedCreationGeoOk() {
        delGeo.setVisibility(View.GONE);
        AddingDialog addingDialog = new AddingDialog();
        Bundle args = new Bundle();

        if (items.get(items.size() - 1).getTitle().equals("NewPoint")) {
            double latitude = items.get(items.size() - 1).getPoint().getLatitude();
            double longitude = items.get(items.size() - 1).getPoint().getLongitude();
            args.putDoubleArray("coordinates", new double[]{latitude, longitude});
            if (editingMode) {
                args.putString("name", editedGeo.getName());
                args.putString("type", editedGeo.getType());
                args.putInt("color", editedGeo.getColor());
                args.putInt("position", editedGeoPosition);
            }
            addingDialog.setArguments(args);
            addingDialog.show(getFragmentManager(), "addGeo");
            addingMode = false;
        } else {
            Toast.makeText(MapScreen_5.this, "Tap anywhere to put a geopoint", Toast.LENGTH_SHORT).show();
        }
    }

    private void clickedAddGeo() {
        editingMode = false;
        addingMode = true;
        creationGeoOk.setVisibility(View.VISIBLE);
        creationGeoCancel.setVisibility(View.VISIBLE);
        addGeo.setVisibility(View.GONE);
        editGeo.setVisibility(View.GONE);
    }

    private void mainMode() {
        delGeo.setVisibility(View.GONE);
        creationGeoOk.setVisibility(View.GONE);
        creationGeoCancel.setVisibility(View.GONE);
        addGeo.setVisibility(View.VISIBLE);
        editGeo.setVisibility(View.VISIBLE);
        addingMode = false;
        editingMode = false;
    }

    public void createOverlay() {
        items = new ArrayList<>();
        ArrayList<Geopoint> geopoints = GeopointsData.getInstance().getGeopoints();

        for (int i = 0; i < geopoints.size(); i++) {
            OverlayItem newItem = new OverlayItem(geopoints.get(i).getName(),
                    geopoints.get(i).getType(),
                    new GeoPoint(geopoints.get(i).getCoordinates()[0],
                            geopoints.get(i).getCoordinates()[1]));
            Drawable marker = getResources().getDrawable(
                    geopoints.get(i).getColor());
            newItem.setMarker(marker);
            items.add(newItem);
        }
        myItemizedOverlay = new MyItemizedOverlay(this, items);
        mapView.setOverlay(myItemizedOverlay);
    }

    private void deleteGeopointDialog() {
        new AlertDialog.Builder(this)
                .setTitle(editedGeo.getName() + " " + editedGeo.getType())
                .setMessage("Do you really want to delete this geopoint?")
                .setIcon(editedGeo.getColor())
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        ArrayList<Geopoint> geopoints = GeopointsData.getInstance().getGeopoints();
                        geopoints.remove(editedGeoPosition);
                        GeopointsData.getInstance().setGeopoints(geopoints);
                        createOverlay();
                        editedGeo = null;
                        editedGeoPosition = 0;

                        mainMode();

                        Toast.makeText(MapScreen_5.this, "Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    //getting info from fragments
    @Override
    public void setEditableGeo(Geopoint geopoint) {
        this.editedGeo = geopoint;
    }

    @Override
    public Geopoint getEditableGeo() {
        return editedGeo;
    }

    @Override
    public int getEditedPositionInList() {
        return editedGeoPosition;
    }

    @Override
    public void setEditedPositionInList(int editedPositionInList) {
        this.editedGeoPosition = editedPositionInList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.allPoints) {
            EditingDialog editDialog = new EditingDialog();
            Bundle args = new Bundle();
            args.putString("clicked", "allPoints");
            editDialog.setArguments(args);
            editDialog.show(getFragmentManager(), "allPoints");
        }
        return super.onOptionsItemSelected(item);
    }

}