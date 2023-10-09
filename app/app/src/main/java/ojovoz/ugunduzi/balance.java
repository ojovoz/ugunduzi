package ojovoz.ugunduzi;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Eugenio on 14/09/2018.
 */
public class balance extends AppCompatActivity {

    public String user;
    public String userPass;
    public int userId;
    public String farmName;
    public float farmSize;
    public int farmId;
    public int farmVersion;
    public int maxVersion;
    public int plot;
    public float plotSize;
    public String farmDate;

    public String cropNames;
    public String pestControlNames;
    public String soilManagementNames;

    public int displayWidth;
    public int displayHeight;

    public int from;

    public ArrayList<oLog> logList;
    oRecyclerViewAdapter recyclerViewAdapter;

    public Date date1;
    public Date date2;
    public Date minDate;
    public Date maxDate;
    public Button bDate1;
    public Button bDate2;

    public dateHelper dH;
    public preferenceManager prefs;
    public String server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        dH = new dateHelper();
        prefs = new preferenceManager(this);
        server = prefs.getPreference("server");

        user = getIntent().getExtras().getString("user");
        userPass = getIntent().getExtras().getString("userPass");
        userId = getIntent().getExtras().getInt("userId");
        farmName = getIntent().getExtras().getString("farmName");
        farmSize = getIntent().getExtras().getFloat("farmSize");
        farmId = getIntent().getExtras().getInt("farmId");
        farmVersion = getIntent().getExtras().getInt("farmVersion");
        maxVersion = getIntent().getExtras().getInt("maxVersion");
        plot = getIntent().getExtras().getInt("plot");
        plotSize = getIntent().getExtras().getFloat("plotSize");
        farmDate = getIntent().getExtras().getString("farmDate");

        cropNames = getIntent().getExtras().getString("cropNames");
        pestControlNames = getIntent().getExtras().getString("pestControlNames");
        soilManagementNames = getIntent().getExtras().getString("soilManagementNames");

        displayWidth = getIntent().getExtras().getInt("displayWidth");
        displayHeight = getIntent().getExtras().getInt("displayHeight");

        from = getIntent().getExtras().getInt("from");

        oLog l = new oLog(this);

        TextView tt = (TextView) findViewById(R.id.plotLabel);
        String title = "";

        if (plot != -1) {
            logList = l.createLog(farmId, farmVersion, plot, userId, 0);

            title = getString(R.string.cropsTitle) + ": " + cropNames + " (" + String.valueOf(plotSize) + " " + getString(R.string.acresWord) + ")";
            title += "\n";
            title += getString(R.string.pestControlTitle) + ": " + pestControlNames;
            title += "\n";
            title += getString(R.string.soilManagementTitle) + ": " + soilManagementNames;

            if (!pestControlNames.equals(getString(R.string.textNone)) && !soilManagementNames.equals(getString(R.string.textNone))) {
                tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagementAndPestControl));
            } else if (!pestControlNames.equals(getString(R.string.textNone)) && soilManagementNames.equals(getString(R.string.textNone))) {
                tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillPestControl));
            } else if (pestControlNames.equals(getString(R.string.textNone)) && !soilManagementNames.equals(getString(R.string.textNone))) {
                tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagement));
            } else {
                tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillDefault));
            }
        } else {
            logList = l.createLog(farmId, farmVersion, userId, 0);

            title = farmName + " (" + String.valueOf(farmSize) + " " + getString(R.string.acresWord) + ")";
            tt.setTextSize(18);
            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            tt.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        }

        if(logList.size()==0){
            Toast.makeText(this, R.string.farmHasNoRecordsMessage, Toast.LENGTH_SHORT).show();
            goBack();
        } else {

            tt.setText(title);

            findMaxMinDates();

            date1 = minDate;
            date2 = maxDate;

            bDate1 = (Button) findViewById(R.id.date1Button);
            bDate2 = (Button) findViewById(R.id.date2Button);

            bDate1.setText(dH.dateToString(date1));
            bDate2.setText(dH.dateToString(date2));

            bDate1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayDatePicker(1, view);
                }
            });

            bDate2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayDatePicker(2, view);
                }
            });

            if(plot==-1){
                logList = l.createLog(farmId, userId, 0);
                findMaxMinDates();
            }

            fillRecyclerView();
        }

    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        menu.clear();
        menu.add(0, 0, 0, R.string.opGoToWeb);
        menu.add(1, 1, 1, R.string.opGoBack);
        if(from==1){
            menu.add(2, 2, 2, R.string.opGoBackToFarm);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                goToWebPage();
                break;
            case 1:
                goBack();
                break;
            case 2:
                goToFarm();
        }
        return super.onOptionsItemSelected(item);
    }

    public void findMaxMinDates() {
        minDate = null;
        maxDate = null;

        Iterator<oLog> iterator = logList.iterator();
        while (iterator.hasNext()) {
            oLog l = iterator.next();
            if (minDate == null) {
                minDate = l.date;
            } else {
                if (minDate.after(l.date)) {
                    minDate = l.date;
                }
            }
            if (maxDate == null) {
                maxDate = l.date;
            } else {
                if (maxDate.before(l.date)) {
                    maxDate = l.date;
                }
            }
        }
    }

    public void displayDatePicker(int n, View view) {
        Calendar calMax;
        Calendar calMin;

        final int which = n;

        final Dialog dialogDate = new Dialog(view.getContext());
        dialogDate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDate.setContentView(R.layout.dialog_datepicker);

        DatePicker dp = (DatePicker) dialogDate.findViewById(R.id.datePicker);
        Calendar calActivity = Calendar.getInstance();
        if (which == 1) {
            calActivity.setTime(date1);
            calMax = Calendar.getInstance();
            calMax.setTime(date2);
            calMin = Calendar.getInstance();
            calMin.setTime(minDate);
        } else {
            calActivity.setTime(date2);
            calMax = Calendar.getInstance();
            calMax.setTime(maxDate);
            calMin = Calendar.getInstance();
            calMin.setTime(date1);
        }
        dp.init(calActivity.get(Calendar.YEAR), calActivity.get(Calendar.MONTH), calActivity.get(Calendar.DAY_OF_MONTH), null);
        dp.setMinDate(calMin.getTimeInMillis());
        dp.setMaxDate(calMax.getTimeInMillis());

        Button dialogButton = (Button) dialogDate.findViewById(R.id.okButton);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePicker dp = (DatePicker) dialogDate.findViewById(R.id.datePicker);
                int day = dp.getDayOfMonth();
                int month = dp.getMonth();
                int year = dp.getYear();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                Date nd = calendar.getTime();

                if (which == 1) {
                    date1 = nd;
                    bDate1.setText(dH.dateToString(nd));
                } else {
                    date2 = nd;
                    bDate2.setText(dH.dateToString(nd));
                }

                dialogDate.dismiss();
                recyclerViewAdapter.list = cardDataFromLog();
                recyclerViewAdapter.setList(recyclerViewAdapter.list);
                recyclerViewAdapter.notifyDataSetChanged();
            }
        });
        dialogDate.show();
    }

    public void fillRecyclerView() {
        List<oCardData> data = cardDataFromLog();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerViewAdapter = new oRecyclerViewAdapter(data, getApplication());
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public List<oCardData> cardDataFromLog() {
        List<oCardData> ret = new ArrayList<>();
        ArrayList<oBalance> balanceItems = new ArrayList<>();
        float total = 0.0f;
        Iterator<oLog> logIterator = logList.iterator();
        while (logIterator.hasNext()) {
            oBalance b;
            oLog l = logIterator.next();
            if ((l.date.equals(date1) || l.date.after(date1)) && (l.date.equals(date2) || l.date.before(date2))) {
                b = findBalanceItem(balanceItems, l.crop, l.treatmentIngredient, l.dataItem);
                if (b == null) {
                    b = new oBalance();
                    b.crop = l.crop;
                    b.treatmentIngredient = l.treatmentIngredient;
                    b.dataItem = l.dataItem;
                    balanceItems.add(b);
                }
                b.cost = (l.dataItem.type >= 3) ? b.cost + l.value : b.cost - l.value;
            }
        }

        ArrayList<oBalance> cropBalanceItems = new ArrayList<>();
        ArrayList<oBalance> treatmentBalanceItems = new ArrayList<>();
        ArrayList<oBalance> otherBalanceItems = new ArrayList<>();

        Iterator<oBalance> balanceIterator = balanceItems.iterator();
        while (balanceIterator.hasNext()) {
            oBalance b = balanceIterator.next();
            if (b.crop != null) {
                cropBalanceItems.add(b);
            } else if (b.treatmentIngredient != null) {
                treatmentBalanceItems.add(b);
            } else {
                otherBalanceItems.add(b);
            }
            total += b.cost;
        }

        Collections.sort(cropBalanceItems, new Comparator<oBalance>() {
            @Override
            public int compare(oBalance b1, oBalance b2) {
                return b1.crop.name.compareTo(b2.crop.name);
            }
        });

        Collections.sort(treatmentBalanceItems, new Comparator<oBalance>() {
            @Override
            public int compare(oBalance b1, oBalance b2) {
                return b1.treatmentIngredient.name.compareTo(b2.treatmentIngredient.name);
            }
        });

        Collections.sort(otherBalanceItems, new Comparator<oBalance>() {
            @Override
            public int compare(oBalance b1, oBalance b2) {
                return b1.dataItem.name.compareTo(b2.dataItem.name);
            }
        });

        TextView tv = (TextView)findViewById(R.id.totalLabel);
        tv.setText(getString(R.string.totalWord) + ": " + String.valueOf(total));

        int n=0;

        if (cropBalanceItems.size() > 0) {
            oCardData cCrops = new oCardData();
            cCrops.id = -1;
            cCrops.plotInfoColor = ContextCompat.getColor(this, R.color.colorPrimary);
            cCrops.info = getString(R.string.cropsTitle);
            cCrops.infoColor = ContextCompat.getColor(this, R.color.colorWhite);
            ret.add(cCrops);

            Iterator<oBalance> cropBalanceIterator = cropBalanceItems.iterator();
            while (cropBalanceIterator.hasNext()) {
                oBalance b = cropBalanceIterator.next();
                oCardData c = new oCardData();
                c.id = -1;
                c.info = b.crop.name + "\n" + getString(R.string.balanceWord) + ": " + b.cost + " " + getDefaultCostUnits();
                c.plotInfoColor = (n%2==0) ? ContextCompat.getColor(this,R.color.colorFillFaded) : ContextCompat.getColor(this,R.color.colorWhite);
                c.infoColor=ContextCompat.getColor(this, R.color.colorBlack);
                ret.add(c);
                n++;
            }
        }

        if(treatmentBalanceItems.size() > 0){
            oCardData cTreatments = new oCardData();
            cTreatments.id = -1;
            cTreatments.plotInfoColor = ContextCompat.getColor(this, R.color.colorPrimary);
            cTreatments.info = getString(R.string.treatmentIngredientsPhrase);
            cTreatments.infoColor = ContextCompat.getColor(this, R.color.colorWhite);
            ret.add(cTreatments);

            Iterator<oBalance> treatmentBalanceIterator = treatmentBalanceItems.iterator();
            while (treatmentBalanceIterator.hasNext()) {
                oBalance b = treatmentBalanceIterator.next();
                oCardData c = new oCardData();
                c.id = -1;
                c.info = b.treatmentIngredient.name + "\n" + getString(R.string.balanceWord) + ": " + b.cost + " " + getDefaultCostUnits();
                c.plotInfoColor = (b.treatmentIngredient.getTreatmentIngredientCategory(b.treatmentIngredient.id,this) == 0) ?
                        ContextCompat.getColor(this,R.color.colorFillPestControlFaded) :
                        (b.treatmentIngredient.getTreatmentIngredientCategory(b.treatmentIngredient.id,this) == 1) ?
                        ContextCompat.getColor(this,R.color.colorFillSoilManagementFaded) : ContextCompat.getColor(this,R.color.colorWhite);
                c.infoColor=ContextCompat.getColor(this, R.color.colorBlack);
                ret.add(c);
                n++;
            }
        }

        if(otherBalanceItems.size() > 0){
            oCardData cOther = new oCardData();
            cOther.id = -1;
            cOther.plotInfoColor = ContextCompat.getColor(this, R.color.colorPrimary);
            cOther.info = getString(R.string.otherWord);
            cOther.infoColor = ContextCompat.getColor(this, R.color.colorWhite);
            ret.add(cOther);

            Iterator<oBalance> otherBalanceIterator = otherBalanceItems.iterator();
            while (otherBalanceIterator.hasNext()) {
                oBalance b = otherBalanceIterator.next();
                oCardData c = new oCardData();
                c.id = -1;
                c.info = b.dataItem.name + "\n" + getString(R.string.balanceWord) + ": " + b.cost + " " + getDefaultCostUnits();
                c.plotInfoColor = (n%2==0) ? ContextCompat.getColor(this,R.color.colorFillFaded) : ContextCompat.getColor(this,R.color.colorWhite);
                c.infoColor=ContextCompat.getColor(this, R.color.colorBlack);
                ret.add(c);
                n++;
            }
        }

        return ret;
    }

    public oBalance findBalanceItem(ArrayList<oBalance> balanceItems, oCrop c, oTreatmentIngredient t, oDataItem d) {
        oBalance ret = null;
        Iterator<oBalance> iterator = balanceItems.iterator();
        while (iterator.hasNext()) {
            oBalance b = iterator.next();
            if (c != null && b.crop != null) {
                if (b.crop.id == c.id) {
                    ret = b;
                    break;
                }
            } else if (t != null && b.treatmentIngredient != null) {
                if (b.treatmentIngredient.id == t.id) {
                    ret = b;
                    break;
                }
            } else if (d != null && b.dataItem != null) {
                if (b.dataItem.id == d.id) {
                    ret = b;
                    break;
                }
            }
        }
        return ret;
    }

    public void goBack() {
        if(from==1) {
            Intent i = new Intent(this, records.class);
            i.putExtra("user", user);
            i.putExtra("userId", userId);
            i.putExtra("userPass", userPass);
            i.putExtra("farmName", farmName);
            i.putExtra("farmSize",farmSize);
            i.putExtra("farmId", farmId);
            i.putExtra("farmVersion", farmVersion);
            i.putExtra("maxVersion", maxVersion);
            i.putExtra("farmDate", farmDate);
            i.putExtra("plot", plot);
            i.putExtra("cropNames", cropNames);
            i.putExtra("pestControlNames", pestControlNames);
            i.putExtra("soilManagementNames", soilManagementNames);
            i.putExtra("plotSize", plotSize);
            i.putExtra("displayWidth", displayWidth);
            i.putExtra("displayHeight", displayHeight);
            startActivity(i);
            finish();
        } else {
            goToFarm();
        }
    }

    public void goToFarm(){
        Intent i = new Intent(this, farmInterface.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion", farmVersion);
        i.putExtra("newFarm", false);
        i.putExtra("firstFarm", false);
        startActivity(i);
        finish();
    }

    public CharSequence getDefaultCostUnits() {
        CharSequence ret = "";
        oUnit u = new oUnit(this);
        ret = u.getUnitNames(2).get(0);
        return ret;
    }

    public void goToWebPage() {
        if (isOnline()) {
            String webpage = server + "/mobile2web.php?user=" + user + "&pass=" + userPass;
            Intent i = new Intent(Intent.ACTION_VIEW);
            if (i.resolveActivity(getPackageManager()) != null) {
                i.setData(Uri.parse(webpage));
                startActivity(i);
            }
        } else {
            Toast.makeText(this, R.string.pleaseConnectMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public void editItem(View v){

    }

    private class oBalance {
        public oCrop crop;
        public oTreatmentIngredient treatmentIngredient;
        public oDataItem dataItem;
        public float cost;

        oBalance() {
            crop = null;
            treatmentIngredient = null;
            dataItem = null;
            cost = 0.0f;
        }
    }
}
