package ojovoz.ugunduzi;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Eugenio on 14/09/2018.
 */
public class balance extends AppCompatActivity {

    public String user;
    public String userPass;
    public int userId;
    public String farmName;
    public int farmId;
    public int farmVersion;
    public int plot;

    public String cropNames;
    public String pestControlNames;
    public String soilManagementNames;

    public int displayWidth;
    public int displayHeight;

    public ArrayList<oLog> logList;

    public Date date1;
    public Date date2;
    public Date minDate;
    public Date maxDate;
    public Button bDate1;
    public Button bDate2;

    public dateHelper dH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        dH = new dateHelper();

        user = getIntent().getExtras().getString("user");
        userPass = getIntent().getExtras().getString("userPass");
        userId = getIntent().getExtras().getInt("userId");
        farmName = getIntent().getExtras().getString("farmName");
        farmId = getIntent().getExtras().getInt("farmId");
        farmVersion = getIntent().getExtras().getInt("farmVersion");
        plot = getIntent().getExtras().getInt("plot");

        cropNames = getIntent().getExtras().getString("cropNames");
        pestControlNames = getIntent().getExtras().getString("pestControlNames");
        soilManagementNames = getIntent().getExtras().getString("soilManagementNames");

        displayWidth = getIntent().getExtras().getInt("displayWidth");
        displayHeight = getIntent().getExtras().getInt("displayHeight");

        oLog l = new oLog(this);

        TextView tt = (TextView) findViewById(R.id.plotLabel);
        String title = "";

        if (plot != -1) {
            logList = l.createLog(farmId, farmVersion, plot, userId, 0);

            title = getString(R.string.cropsTitle) + ": " + cropNames;
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

            title = farmName;
            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillDefault));
        }

        tt.setText(title);

        findMaxMinDates();

        date1 = minDate;
        date2 = maxDate;

        bDate1 = (Button)findViewById(R.id.date1Button);
        bDate2 = (Button)findViewById(R.id.date2Button);

        bDate1.setText(dH.dateToString(date1));
        bDate2.setText(dH.dateToString(date2));

        bDate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDatePicker(1,view);
            }
        });

        bDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDatePicker(2,view);
            }
        });

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

    public void displayDatePicker(int n, View view){
        Calendar calMax;
        Calendar calMin;

        final int which = n;

        final Dialog dialogDate = new Dialog(view.getContext());
        dialogDate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDate.setContentView(R.layout.dialog_datepicker);

        DatePicker dp = (DatePicker) dialogDate.findViewById(R.id.datePicker);
        Calendar calActivity = Calendar.getInstance();
        if(which==1) {
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
                calendar.set(year, month, day);

                Date nd = calendar.getTime();

                if(which==1){
                    date1=nd;
                    bDate1.setText(dH.dateToString(nd));
                } else {
                    date2=nd;
                    bDate2.setText(dH.dateToString(nd));
                }


                dialogDate.dismiss();
            }
        });
        dialogDate.show();
    }
}
