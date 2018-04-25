package ojovoz.ugunduzi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Eugenio on 24/04/2018.
 */
public class dataManager extends AppCompatActivity {

    public oCrop crop1;
    public oCrop crop2;
    public oTreatment treatment1;
    public oTreatment treatment2;

    public String user;
    public String userPass;
    public int userId;
    public String farmName;
    public int plot;

    public ArrayList<oLog> logList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_manager);

        crop1 = new oCrop(this);
        crop2 = new oCrop(this);
        treatment1 = new oTreatment(this);
        treatment2 = new oTreatment(this);

        user = getIntent().getExtras().getString("user");
        userPass = getIntent().getExtras().getString("userPass");
        userId = getIntent().getExtras().getInt("userId");
        farmName = getIntent().getExtras().getString("farmName");
        plot = getIntent().getExtras().getInt("plot");

        crop1 = (getIntent().getExtras().getInt("crop1") > 0) ? crop1.getCropFromId(getIntent().getExtras().getInt("crop1")) : null;
        crop2 = (getIntent().getExtras().getInt("crop2") > 0) ? crop2.getCropFromId(getIntent().getExtras().getInt("crop2")) : null;

        treatment1 = (getIntent().getExtras().getInt("treatment1") > 0) ? treatment1.getTreatmentFromId(getIntent().getExtras().getInt("treatment1")) : null;
        treatment2 = (getIntent().getExtras().getInt("treatment2") > 0) ? treatment2.getTreatmentFromId(getIntent().getExtras().getInt("treatment2")) : null;

        TextView tt = (TextView) findViewById(R.id.plotLabel);
        String title = "";

        String activityTitle = getTitle().toString();
        if (plot >= 0) {
            activityTitle = activityTitle.replace("X", getString(R.string.plotWord));

            if (crop1 == null && crop2 == null) {
                title = getString(R.string.plotCropLabel) + "s: " + getString(R.string.textNone);
            } else {
                if (crop1 != null && crop2 == null) {
                    title = getString(R.string.plotCropLabel) + ": " + crop1.name;
                } else if (crop1 != null && crop2 != null) {
                    title = getString(R.string.plotCropLabel) + "s: " + crop1.name + ", " + crop2.name;
                }
            }
            title += "\n";
            if (treatment1 == null && treatment2 == null) {
                title += getString(R.string.plotTreatmentLabel) + "s: " + getString(R.string.textNone);
                tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillDefault));
            } else {
                if (treatment1 != null && treatment2 == null) {
                    title += getString(R.string.plotTreatmentLabel) + ": " + treatment1.name;
                    if (treatment1.category == 0) {
                        tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillPestControl));
                    } else {
                        tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagement));
                    }
                } else if (treatment1 != null && treatment2 != null) {
                    title += getString(R.string.plotTreatmentLabel) + "s: " + treatment1.name + ", " + treatment2.name;
                    if (treatment1.category != treatment2.category) {
                        tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagementAndPestControl));
                    } else {
                        if (treatment1.category == 0) {
                            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillPestControl));
                        } else {
                            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagement));
                        }
                    }
                }
            }
            tt.setText(title);
        } else {
            activityTitle = activityTitle.replace("X", getString(R.string.farmWord));
            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            tt.setText(farmName);
            tt.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        }
        setTitle(activityTitle);

        oLog log = new oLog(this);
        logList = (plot >= 0) ? log.createLog(farmName, userId, plot, 2) : log.createLog(farmName, userId, 2);

        fillRecyclerView();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, R.string.opPictureSound);
        menu.add(1, 1, 1, R.string.opEnterData);
        menu.add(2, 2, 2, R.string.opGoBack);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                goToPictureSound();
                break;
            case 1:
                goToEnterData();
                break;
            case 2:
                goBack();
        }
        return super.onOptionsItemSelected(item);
    }

    public void goToPictureSound() {
        Intent i = new Intent(this, pictureSound.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("plot", plot);
        if (crop1 != null) {
            i.putExtra("crop1", crop1.id);
        } else {
            i.putExtra("crop1", "-1");
        }
        if (crop2 != null) {
            i.putExtra("crop2", crop2.id);
        } else {
            i.putExtra("crop2", "-1");
        }
        if (treatment1 != null) {
            i.putExtra("treatment1", treatment1.id);
        } else {
            i.putExtra("treatment1", "-1");
        }
        if (treatment2 != null) {
            i.putExtra("treatment2", treatment2.id);
        } else {
            i.putExtra("treatment2", "-1");
        }
        startActivity(i);
        finish();
    }

    public void goToEnterData() {
        Intent i = new Intent(this, enterData.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("plot", plot);
        if (crop1 != null) {
            i.putExtra("crop1", crop1.id);
        } else {
            i.putExtra("crop1", "-1");
        }
        if (crop2 != null) {
            i.putExtra("crop2", crop2.id);
        } else {
            i.putExtra("crop2", "-1");
        }
        if (treatment1 != null) {
            i.putExtra("treatment1", treatment1.id);
        } else {
            i.putExtra("treatment1", "-1");
        }
        if (treatment2 != null) {
            i.putExtra("treatment2", treatment2.id);
        } else {
            i.putExtra("treatment2", "-1");
        }
        startActivity(i);
        finish();
    }

    public void goBack() {
        Intent i = new Intent(this, farmInterface.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("newFarm", false);
        i.putExtra("firstFarm", false);
        startActivity(i);
        finish();
    }

    public void fillRecyclerView() {
        List<oCardData> data = cardDataFromLog();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        oRecyclerViewAdapter adapter = new oRecyclerViewAdapter(data, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public List<oCardData> cardDataFromLog(){
        dateHelper dH = new dateHelper();
        List<oCardData> ret = new ArrayList<>();
        Iterator<oLog> logIterator = logList.iterator();
        int n=0;
        while (logIterator.hasNext()) {
            oLog l = logIterator.next();
            oCardData c = new oCardData();
            c.id = n;
            if(l.dataItem==null){
                c.info = dH.dateToString(l.date);
                c.imgFile = l.picture;
                c.sndFile = l.sound;
            } else {
                c.info = getDataItemText(l);
            }
            ret.add(c);
            n++;
        }
        return ret;
    }

    public String getDataItemText(oLog l){
        dateHelper dH = new dateHelper();
        String ret="";
        String dataItem = l.getDataItemName();
        String date = dH.dateToString(l.date);
        if(l.dataItem.type==1){
            ret=date+"\n"+dataItem;
        } else if(l.dataItem.type==0 || l.dataItem.type==2){
            String valueUnits = String.valueOf(l.value) + " " + l.units.name;
            ret=date+"\n"+dataItem+": "+valueUnits;
        }
        return ret;
    }

    public void playStop(View v){
        int n = v.getId();
    }
}
