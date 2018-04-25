package ojovoz.ugunduzi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Eugenio on 16/04/2018.
 */
public class enterData extends AppCompatActivity {

    public String user;
    public String userPass;
    public int userId;
    public String farmName;
    public int plot;

    public oCrop crop1;
    public oCrop crop2;
    public oTreatment treatment1;
    public oTreatment treatment2;

    public Date dataItemDate;

    public ArrayList<oLog> plotLog;

    boolean bChanges = false;
    private dateHelper dH;

    ArrayList<oDataItem> dataItemsList;
    public CharSequence dataItemsNamesArray[];

    public oDataItem chosenDataItem;
    public oCrop chosenCrop;
    public oTreatment chosenTreatment;
    public oUnit chosenUnits;

    ArrayList<oUnit> unitsList;
    public CharSequence unitsNamesArray[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_data);

        dH = new dateHelper();

        oCrop seed = new oCrop(this);
        oTreatment action = new oTreatment(this);
        oLog log = new oLog(this);

        user = getIntent().getExtras().getString("user");
        userPass = getIntent().getExtras().getString("userPass");
        userId = getIntent().getExtras().getInt("userId");
        farmName = getIntent().getExtras().getString("farmName");
        plot = getIntent().getExtras().getInt("plot");

        crop1 = (getIntent().getExtras().getInt("crop1") > 0) ? seed.getCropFromId(getIntent().getExtras().getInt("crop1")) : null;
        crop2 = (getIntent().getExtras().getInt("crop2") > 0) ? seed.getCropFromId(getIntent().getExtras().getInt("crop2")) : null;

        treatment1 = (getIntent().getExtras().getInt("treatment1") > 0) ? action.getTreatmentFromId(getIntent().getExtras().getInt("treatment1")) : null;
        treatment2 = (getIntent().getExtras().getInt("treatment2") > 0) ? action.getTreatmentFromId(getIntent().getExtras().getInt("treatment2")) : null;

        TextView tt = (TextView) findViewById(R.id.plotLabel);
        String title = "";

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

        dataItemDate = new Date();

        unitsList = new ArrayList<>();

        oDataItem d = new oDataItem(this);
        boolean bExcludeCropSpecific = (crop1==null && crop2==null) ? true : false;
        boolean bExcludeTreatmentSpecific = (treatment1==null && treatment1==null) ? true : false;
        dataItemsList = d.getDataItems(bExcludeCropSpecific, bExcludeTreatmentSpecific);
        dataItemsNamesArray = d.getDataItemNames(bExcludeCropSpecific, bExcludeTreatmentSpecific).toArray(new CharSequence[dataItemsList.size()]);

        plotLog = log.createLog(farmName, userId, plot, 0);
        if (plotLog.size() == 0) {
            TextView tv = (TextView) findViewById(R.id.previousDataItems);
            tv.setVisibility(View.GONE);
            TableLayout tl = (TableLayout) findViewById(R.id.dataItems);
            tl.setVisibility(View.GONE);
        } else {
            plotLog = log.sortLogByDate(plotLog,true,10);
            fillTable();
        }
    }

    @Override
    public void onBackPressed() {
        tryExit(2);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, R.string.opPictureSound);
        menu.add(1, 1, 1, R.string.opManagePlotRecords);
        menu.add(2, 2, 2, R.string.opGoBack);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        tryExit(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    public void tryExit(int exitAction){
        if(bChanges) {
            confirmExit(exitAction);
        } else {
            switch (exitAction) {
                case 0:
                    goToPictureSound();
                    break;
                case 1:
                    goToManageRecords();
                    break;
                case 2:
                    goBack();
            }
        }
    }

    public void confirmExit(int e) {
        final int exitAction = e;
        AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
        logoutDialog.setMessage(R.string.dataNotSavedText);
        logoutDialog.setNegativeButton(R.string.noButtonText, null);
        logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch (exitAction) {
                    case 0:
                        goToPictureSound();
                        break;
                    case 1:
                        goToManageRecords();
                        break;
                    case 2:
                        goBack();
                }

            }
        });
        logoutDialog.create();
        logoutDialog.show();
    }

    public void goBack(){
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

    public void goToPictureSound(){
        Intent i = new Intent(this, pictureSound.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("plot", plot);
        if(crop1!=null) {
            i.putExtra("crop1", crop1.id);
        } else {
            i.putExtra("crop1", "-1");
        }
        if(crop2!=null) {
            i.putExtra("crop2", crop2.id);
        } else {
            i.putExtra("crop2", "-1");
        }
        if(treatment1!=null) {
            i.putExtra("treatment1", treatment1.id);
        } else {
            i.putExtra("treatment1", "-1");
        }
        if(treatment2!=null) {
            i.putExtra("treatment2", treatment2.id);
        } else {
            i.putExtra("treatment2", "-1");
        }
        startActivity(i);
        finish();
    }

    void goToManageRecords(){
        Intent i = new Intent(this, dataManager.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("plot", plot);
        if(crop1!=null) {
            i.putExtra("crop1", crop1.id);
        } else {
            i.putExtra("crop1", "-1");
        }
        if(crop2!=null) {
            i.putExtra("crop2", crop2.id);
        } else {
            i.putExtra("crop2", "-1");
        }
        if(treatment1!=null) {
            i.putExtra("treatment1", treatment1.id);
        } else {
            i.putExtra("treatment1", "-1");
        }
        if(treatment2!=null) {
            i.putExtra("treatment2", treatment2.id);
        } else {
            i.putExtra("treatment2", "-1");
        }
        startActivity(i);
        finish();
    }

    public void showDataItemsSelector(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancelButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final ListAdapter adapter = new ArrayAdapter<>(this,R.layout.checked_list_template,dataItemsNamesArray);
        builder.setSingleChoiceItems(adapter,-1,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i>=0) {
                    String chosenDataItem = dataItemsNamesArray[i].toString();
                    Button b = (Button)findViewById(R.id.enterDataButton);
                    b.setText(chosenDataItem);
                    showFields(i);
                }
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialogDataItems = builder.create();
        dialogDataItems.show();
    }

    public void showFields(int i){
        chosenDataItem = dataItemsList.get(i);

        bChanges=true;

        Button bc = (Button)findViewById(R.id.enterCropButton);
        Button bt = (Button)findViewById(R.id.enterTreatmentButton);
        TextView td = (TextView)findViewById(R.id.enterDateText);
        Button bd = (Button)findViewById(R.id.dateButton);
        TextView tv = (TextView)findViewById(R.id.enterValueText);
        EditText ev = (EditText)findViewById(R.id.dataItemValue);
        TextView tu = (TextView)findViewById(R.id.enterUnitsText);
        Button bu = (Button)findViewById(R.id.dataItemUnits);
        Button bs = (Button)findViewById(R.id.saveButton);

        chosenCrop=null;
        chosenTreatment=null;
        chosenUnits=null;

        if(chosenDataItem.isCropSpecific && (crop1!=null && crop2!=null)){
            bc.setVisibility(View.VISIBLE);
        } else {
            bc.setVisibility(View.GONE);
        }

        if(chosenDataItem.isTreatmentSpecific && (treatment1!=null && treatment2!=null)){
            bt.setVisibility(View.VISIBLE);
        } else {
            bt.setVisibility(View.GONE);
        }

        td.setVisibility(View.VISIBLE);
        bd.setVisibility(View.VISIBLE);
        bd.setText(dH.dateToString(dataItemDate));

        switch(chosenDataItem.type){
            case 0:
                //number
                tv.setVisibility(View.VISIBLE);
                ev.setVisibility(View.VISIBLE);
                tu.setVisibility(View.VISIBLE);

                prepareUnits(bu,tu,tv);

                break;
            case 1:
                //date
                tv.setVisibility(View.GONE);
                ev.setVisibility(View.GONE);
                tu.setVisibility(View.GONE);
                bu.setVisibility(View.GONE);
                chosenUnits=null;
                unitsList.clear();
                break;
            case 2:
                //cost
                tv.setVisibility(View.VISIBLE);
                ev.setVisibility(View.VISIBLE);
                tu.setVisibility(View.VISIBLE);
                bu.setVisibility(View.VISIBLE);

                prepareUnits(bu,tu,tv);
        }
        bs.setVisibility(View.VISIBLE);
    }

    public void prepareUnits(Button bu, TextView tu, TextView tv){
        oUnit u = new oUnit(this);
        unitsList = u.getUnits(chosenDataItem.type);
        if(unitsList.size()==1) {
            String units = unitsList.get(0).name;
            chosenUnits = unitsList.get(0);
            tv.setText(tu.getText()+" ("+units+")");
            tu.setVisibility(View.GONE);
            bu.setVisibility(View.GONE);
        } else {
            tu.setVisibility(View.VISIBLE);
            bu.setVisibility(View.VISIBLE);
            unitsNamesArray = u.getUnitNames(chosenDataItem.type).toArray(new CharSequence[unitsList.size()]);
            bu.setText(chosenDataItem.defaultUnits.name);
            tv.setText(R.string.valueLabel);
        }
    }

    public void displayDatePicker(View v){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_datepicker);

        DatePicker dp = (DatePicker) dialog.findViewById(R.id.datePicker);
        Calendar calActivity = Calendar.getInstance();
        calActivity.setTime(dataItemDate);
        dp.init(calActivity.get(Calendar.YEAR), calActivity.get(Calendar.MONTH), calActivity.get(Calendar.DAY_OF_MONTH), null);

        Calendar calMax = Calendar.getInstance();
        calMax.setTime(new Date());

        dp.setMaxDate(calMax.getTimeInMillis());

        Button dialogButton = (Button) dialog.findViewById(R.id.okButton);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bChanges=true;

                DatePicker dp = (DatePicker) dialog.findViewById(R.id.datePicker);
                int day = dp.getDayOfMonth();
                int month = dp.getMonth();
                int year = dp.getYear();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);

                dataItemDate = calendar.getTime();

                Button cb = (Button) findViewById(R.id.dateButton);
                cb.setText(dH.dateToString(dataItemDate));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void showUnitsSelector(View v){

        if(unitsList.size()>1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setNegativeButton(R.string.cancelButtonText, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final ListAdapter adapter = new ArrayAdapter<>(this, R.layout.checked_list_template, unitsNamesArray);
            builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i >= 0) {
                        bChanges=true;
                        chosenUnits = unitsList.get(i);
                        String chosenUnitsName = unitsNamesArray[i].toString();
                        Button b = (Button) findViewById(R.id.dataItemUnits);
                        b.setText(chosenUnitsName);
                    }
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialogUnits = builder.create();
            dialogUnits.show();
        }
    }

    public void showCropSelector(View v){
        ArrayList<String> cropNames = new ArrayList<>();
        cropNames.add(crop1.name);
        cropNames.add(crop2.name);
        CharSequence cropNamesArray[] = cropNames.toArray(new CharSequence[2]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancelButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final ListAdapter adapter = new ArrayAdapter<>(this, R.layout.checked_list_template, cropNamesArray);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                bChanges=true;
                String chosenCropName="";
                switch(i){
                    case 0:
                        chosenCrop=crop1;
                        chosenCropName=crop1.name;
                        break;
                    case 1:
                        chosenCrop=crop2;
                        chosenCropName=crop2.name;
                }
                Button b = (Button) findViewById(R.id.enterCropButton);
                b.setText(chosenCropName);

                dialogInterface.dismiss();
            }
        });
        AlertDialog dialogCrops = builder.create();
        dialogCrops.show();
    }

    public void showTreatmentSelector(View v){
        ArrayList<String> treatmentNames = new ArrayList<>();
        treatmentNames.add(treatment1.name);
        treatmentNames.add(treatment2.name);
        CharSequence treatmentNamesArray[] = treatmentNames.toArray(new CharSequence[2]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancelButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final ListAdapter adapter = new ArrayAdapter<>(this, R.layout.checked_list_template, treatmentNamesArray);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                bChanges=true;
                String chosenTreatmentName="";
                switch(i){
                    case 0:
                        chosenTreatment=treatment1;
                        chosenTreatmentName=treatment1.name;
                        break;
                    case 1:
                        chosenTreatment=treatment2;
                        chosenTreatmentName=treatment2.name;
                }
                Button b = (Button) findViewById(R.id.enterTreatmentButton);
                b.setText(chosenTreatmentName);

                dialogInterface.dismiss();
            }
        });
        AlertDialog dialogTreatments = builder.create();
        dialogTreatments.show();
    }

    public void fillTable() {

        if(plotLog.size()>0) {

            dateHelper dH = new dateHelper();

            TableLayout tl0 = (TableLayout) findViewById(R.id.dataItems);
            tl0.setVisibility(View.VISIBLE);

            TableLayout tl = (TableLayout) findViewById(R.id.dataItemsTable);
            tl.removeAllViews();
            Iterator<oLog> logIterator = plotLog.iterator();
            int n=0;
            while (logIterator.hasNext()) {
                oLog l = logIterator.next();

                final TableRow trow = new TableRow(enterData.this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f);
                lp.setMargins(10, 10, 0, 10);

                if (n % 2 == 0) {
                    trow.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillFaded));
                } else {
                    trow.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite));
                }

                TextView t1 = new TextView(enterData.this);
                t1.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                t1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f);
                String dataItem = l.getDataItemName();
                t1.setText(dataItem);
                t1.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                t1.setPadding(0, 10, 0, 10);
                t1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        findViewById(R.id.childScrollView).getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });
                trow.addView(t1, lp);

                TextView t2 = new TextView(enterData.this);
                t2.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                t2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f);
                t2.setText(dH.dateToString(l.date));
                t2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                t2.setPadding(0, 10, 0, 10);
                trow.addView(t2, lp);
                t2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        findViewById(R.id.childScrollView).getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });

                trow.setGravity(Gravity.CENTER_VERTICAL);
                tl.addView(trow, lp);

                n++;
            }
        }
    }

    public void saveData(View v){
        EditText ev = (EditText)findViewById(R.id.dataItemValue);
        String tv = ev.getText().toString();
        if(!tv.isEmpty() || chosenDataItem.type==1){
            float chosenValue = (tv.isEmpty()) ? 0 : Float.parseFloat(tv);
            if((chosenValue>=0.0f && (chosenDataItem.type==0 || chosenDataItem.type==2)) || chosenDataItem.type==1){
                if(!(crop1!=null && crop2!=null && chosenDataItem.isCropSpecific && chosenCrop==null)){
                    if(!(treatment1!=null && treatment2!=null && chosenDataItem.isTreatmentSpecific && chosenTreatment==null)){
                        if(!(unitsList.size()>1 && chosenUnits==null)){
                            oLog l = new oLog(this);
                            l.appendToLog(farmName,userId,plot,dataItemDate,chosenDataItem,chosenValue,chosenUnits,chosenCrop,chosenTreatment,"","");
                            resetFields();
                            resetLogList();
                            fillTable();
                        } else {
                            Toast.makeText(this, R.string.mustChooseUnitsMessage, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, R.string.mustChooseTreatmentMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.mustChooseCropMessage, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.valueNegativeMessage, Toast.LENGTH_SHORT).show();
                ev.requestFocus();
            }
        } else {
            Toast.makeText(this, R.string.valueEmptyMessage, Toast.LENGTH_SHORT).show();
            ev.requestFocus();
        }
    }

    public void resetLogList(){
        oLog l = new oLog(this);
        plotLog = l.createLog(farmName, userId, plot, 0);
        plotLog = l.sortLogByDate(plotLog,true,10);
    }

    public void resetFields(){
        bChanges=false;
        chosenDataItem=null;
        chosenCrop=null;
        chosenTreatment=null;
        chosenUnits=null;
        dataItemDate=new Date();

        Button bi = (Button)findViewById(R.id.enterDataButton);
        bi.setText(R.string.enterNewDataItemButtonLabel);

        Button bc = (Button)findViewById(R.id.enterCropButton);
        bc.setText(R.string.enterCropLabel);
        bc.setVisibility(View.GONE);
        Button bt = (Button)findViewById(R.id.enterTreatmentButton);
        bt.setText(R.string.enterTreatmentLabel);
        bt.setVisibility(View.GONE);
        TextView td = (TextView)findViewById(R.id.enterDateText);
        td.setVisibility(View.GONE);
        Button bd = (Button)findViewById(R.id.dateButton);
        bd.setVisibility(View.GONE);
        TextView tv = (TextView)findViewById(R.id.enterValueText);
        tv.setVisibility(View.GONE);
        EditText ev = (EditText)findViewById(R.id.dataItemValue);
        ev.setText(R.string.emptyString);
        ev.setVisibility(View.GONE);
        TextView tu = (TextView)findViewById(R.id.enterUnitsText);
        tu.setVisibility(View.GONE);
        Button bu = (Button)findViewById(R.id.dataItemUnits);
        bu.setVisibility(View.GONE);
        Button bs = (Button)findViewById(R.id.saveButton);
        bs.setVisibility(View.GONE);

    }
}
