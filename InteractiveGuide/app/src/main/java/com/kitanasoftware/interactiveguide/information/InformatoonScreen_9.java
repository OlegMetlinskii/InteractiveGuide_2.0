package com.kitanasoftware.interactiveguide.information;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.kitanasoftware.interactiveguide.R;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class  InformatoonScreen_9 extends AppCompatActivity {

    EditText etText;

    SharedPreferences sPref;

    final String GUIDE_NAME = "name";
    final String GUIDE_PHONE = "phone";
    final String TOUR = "tour";
    final String GOAL = "goal";
    final String COMPANY = "company";
    final String INFORMATION_ID = "id";

    String guideName;
    String guidePhone;
    String tour;
    String goal;
    String company;
    String inf_id;
    InformationAdapter adapter;
    ListView listView;
    ArrayList<Information> informList;
    Button butEdit;
    Button butSave;
    GuideInform guideInform;
    TourInform tourInform;
    AdditionalInform additionalInform;
    View edit ;
    View save;
    byte[] arrPhoto;
    Bitmap bitPhoto;
    private boolean isEdit;
    String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.informatoon_screen_9);

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#fdc68a"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        downloadFromParse();
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        informList = new ArrayList();
        adapter = new InformationAdapter(getApplicationContext(), informList);
        listView = (ListView) findViewById(R.id.lvInform);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    public void downloadFromParse() {
        ParseQuery<ParseObject> pq = ParseQuery.getQuery("Information");
        pq.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < list.size(); i++) {
                        guideName = list.get(i).getString("guide_full_name");
                        guidePhone = list.get(i).getString("guide_phone");
                        tour = list.get(i).getString("tour_inform_name");
                        goal = list.get(i).getString("tour_inform_goal");
                        company = list.get(i).getString("additional_inform_dev");
                        inf_id = list.get(i).getObjectId();
                        ParseFile pf = list.get(i).getParseFile("guide_photo");
                        pf.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] bytes, ParseException e) {
                                bitPhoto = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                savePhoto();
                                saveToPhone();

                                createInformArray();
                                //  listView = (ListView) findViewById(R.id.lvInform);
                                // adapter = new InformationAdapter(getApplicationContext(), informList);
                                invalidateLv();
                            }
                        });

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Exeption" + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(menu != null) {
            menu.clear();
        }
        if(isEdit==false){
            menu.setGroupVisible(R.menu.main, false);
            getMenuInflater().inflate(R.menu.main, menu);

        }
        else if(isEdit==true) {
            menu.setGroupVisible(R.menu.edit, false);
            getMenuInflater().inflate(R.menu.edit, menu);

        }

        return super.onPrepareOptionsMenu(menu);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        edit = findViewById(R.id.item1);
        save = findViewById(R.id.item2);
        switch (item.getItemId()) {
            case R.id.item1 :
                invalidateLv();
                adapter.startEdit();
                isEdit=true;
                invalidateLv();
                invalidateOptionsMenu();
                break;

            case R.id.item2 :

//                EditText etGuideName = (EditText) findViewById(R.id.etFullName);
//                EditText etGuidePhone = (EditText) findViewById(R.id.etPhone);
                EditText etTour = (EditText) findViewById(R.id.etTour);
                EditText etGoal = (EditText) findViewById(R.id.etTourGoal);

//                guideName = etGuideName.getText().toString();
//                guidePhone = etGuidePhone.getText().toString();
                tour = etTour.getText().toString();
                goal = etGoal.getText().toString();

                saveToPhone();
                saveToParse();
                adapter.stoptEdit();
                updateInformArray();
                isEdit = false;
                invalidateLv();

                break;

            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveToPhone() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(GUIDE_NAME, guideName);
        ed.putString(GUIDE_PHONE, guidePhone);
        ed.putString(TOUR, tour);
        ed.putString(GOAL, goal);
        ed.commit();
    }

    public void saveToParse() {
        ParseObject po = ParseObject.createWithoutData("Information", "Z9alPJeIal");//id put!!!
        //po.put("guide_photo", parseFile);
        po.put("guide_full_name", guideName);
        po.put("guide_phone", guidePhone);
        po.put("tour_inform_name", tour);
        po.put("tour_inform_goal", goal);
        po.saveInBackground();
    }

    public void invalidateLv() {
        adapter.setList(informList);
        listView.setAdapter(adapter);
        invalidateOptionsMenu();
        listView.invalidate();
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    // save foto to SD card
    public void savePhoto() {

        photoPath = Environment.getExternalStorageDirectory() + "/myPhoto" + System.currentTimeMillis() + ".jpg";
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(photoPath);
            bitPhoto.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void createInformArray() {
        //   bitPhoto=getPhotoFromGallery();
        guideInform = new GuideInform(Information.InformType.GUIDE,
                sPref.getString(GUIDE_NAME, ""), sPref.getString(GUIDE_PHONE, ""), bitPhoto);
        tourInform = new TourInform(Information.InformType.TOUR,
                sPref.getString(TOUR, ""), sPref.getString(GOAL, ""));
        additionalInform = new AdditionalInform(Information.InformType.ADD, sPref.getString(COMPANY, ""));

        informList.add(0, guideInform);
        informList.add(1, tourInform);
        informList.add(2, additionalInform);
    }

    public void updateInformArray(){
        guideInform.setFull_name(sPref.getString(GUIDE_NAME, ""));
        guideInform.setPhone(sPref.getString(GUIDE_PHONE, ""));
        tourInform.setName(sPref.getString(TOUR, ""));
        tourInform.setGoal(sPref.getString(GOAL, ""));
        informList.set(0, guideInform);
        informList.set(1, tourInform);
    }

    public Bitmap getPhotoFromGallery(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(photoPath, options);
    }
}




