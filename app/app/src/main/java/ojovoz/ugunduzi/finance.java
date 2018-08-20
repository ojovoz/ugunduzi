package ojovoz.ugunduzi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Eugenio on 20/08/2018.
 */
public class finance extends AppCompatActivity {

    public oLog newItem;

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

    public oPlot currentPlot;

    ArrayList<oDataItem> dataItemsList;
    public CharSequence dataItemsNamesArray[];
    public oDataItem chosenDataItem;

    public Date dataItemDate;

    public Button bDate;
    public Button bCrop;
    public Button bTreatment;
    public Button bUnits;

    public EditText etValue;
    public EditText etComments;
    public EditText etQuantity;

    public ArrayList<oUnit> unitsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);

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

        TextView tt = (TextView) findViewById(R.id.plotLabel);
        String title = "";

        title= getString(R.string.cropsTitle) + ": " + cropNames;
        title+="\n";
        title+=getString(R.string.pestControlTitle) + ": " + pestControlNames;
        title+="\n";
        title+=getString(R.string.soilManagementTitle) + ": " + soilManagementNames;

        if(!pestControlNames.equals(getString(R.string.textNone)) && !soilManagementNames.equals(getString(R.string.textNone))) {
            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagementAndPestControl));
        } else if(!pestControlNames.equals(getString(R.string.textNone)) && soilManagementNames.equals(getString(R.string.textNone))) {
            tt.setBackgroundColor(ContextCompat.getColor(this,R.color.colorFillPestControl));
        } else if(pestControlNames.equals(getString(R.string.textNone)) && !soilManagementNames.equals(getString(R.string.textNone))) {
            tt.setBackgroundColor(ContextCompat.getColor(this,R.color.colorFillSoilManagement));
        } else {
            tt.setBackgroundColor(ContextCompat.getColor(this,R.color.colorFillDefault));
        }

        tt.setText(title);

        dataItemDate = new Date();
        unitsList = new ArrayList<>();
        currentPlot = getCurrentPlot();

        getDataItemsList();

    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    public void addItem(View v){
        final dateHelper dH = new dateHelper();
        newItem = new oLog();

        newItem.date = new Date();

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_edit_data_item);
        dialog.getWindow().setLayout(displayWidth-10, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        final Button bDataItem = (Button) dialog.findViewById(R.id.dataItemButton);
        bDate = (Button) dialog.findViewById(R.id.dateButton);
        bCrop = (Button) dialog.findViewById(R.id.cropButton);
        bTreatment = (Button) dialog.findViewById(R.id.treatmentButton);
        bUnits = (Button) dialog.findViewById(R.id.dataItemUnits);

        final TableLayout tlQuantityUnits = (TableLayout) dialog.findViewById(R.id.quantityUnitsTable);

        bDataItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setCancelable(true);
                builder.setNegativeButton(R.string.cancelButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogDataItems, int which) {
                        dialogDataItems.dismiss();
                    }
                });
                final ListAdapter adapter = new ArrayAdapter<>(view.getContext(), R.layout.checked_list_template, dataItemsNamesArray);
                builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i >= 0) {
                            oDataItem d = dataItemsList.get(i);

                            Date dd = newItem.date;
                            newItem = new oLog();
                            newItem.date = dd;

                            //TODO: reset fields

                            newItem.dataItem=d;
                            bDataItem.setText(d.name);
                            if(currentPlot.crops.size()>1 && d.isCropSpecific){
                                bCrop.setVisibility(View.VISIBLE);
                                bCrop.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        displayCropPicker();
                                    }
                                });
                            } else if(d.isCropSpecific){
                                bCrop.setVisibility(View.VISIBLE);
                                bCrop.setText(currentPlot.crops.get(0).name);
                                newItem.crop=currentPlot.crops.get(0);
                            } else {
                                bCrop.setVisibility(View.GONE);
                            }
                            if((currentPlot.pestControlIngredients.size()>0 || currentPlot.soilManagementIngredients.size()>0) && d.isTreatmentSpecific){
                                bTreatment.setVisibility(View.VISIBLE);
                                if((currentPlot.pestControlIngredients.size() + currentPlot.soilManagementIngredients.size())>1){
                                    bTreatment.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            displayTreatmentIngredientPicker();
                                        }
                                    });
                                }
                            } else if(d.isTreatmentSpecific) {
                                bTreatment.setVisibility(View.VISIBLE);
                                if(currentPlot.pestControlIngredients.size()==1){
                                    bTreatment.setText(currentPlot.pestControlIngredients.get(0).name);
                                    newItem.treatmentIngredient=currentPlot.pestControlIngredients.get(0);
                                } else {
                                    bTreatment.setText(currentPlot.soilManagementIngredients.get(0).name);
                                    newItem.treatmentIngredient=currentPlot.soilManagementIngredients.get(0);
                                }
                            } else {
                                bTreatment.setVisibility(View.GONE);
                            }
                            bDate.setVisibility(View.VISIBLE);
                            bDate.setText(dH.dateToString(newItem.date));

                            bDate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    displayDatePicker(view);
                                }
                            });

                            if(d.type!=0) {
                                tlQuantityUnits.setVisibility(View.VISIBLE);
                                newItem.units = d.defaultUnits;
                                bUnits.setText(newItem.units.name);
                                bUnits.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        displayUnitsPicker();
                                    }
                                });
                            } else {
                                tlQuantityUnits.setVisibility(View.GONE);
                            }

                            etValue = (EditText) dialog.findViewById(R.id.dataItemValue);
                            etValue.setVisibility(View.VISIBLE);
                            etValue.setHint(getDefaultCostUnits());

                            etComments = (EditText) dialog.findViewById(R.id.dataItemComments);
                            etComments.setVisibility(View.VISIBLE);

                            Button bSave = (Button) dialog.findViewById(R.id.saveButton);
                            bSave.setVisibility(View.VISIBLE);

                        }
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialogDataItems = builder.create();
                dialogDataItems.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogDataItems.show();
            }
        });

        dialog.show();
    }

    public void displayDatePicker(View view){
        final Dialog dialogDate = new Dialog(view.getContext());
        dialogDate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDate.setContentView(R.layout.dialog_datepicker);

        DatePicker dp = (DatePicker) dialogDate.findViewById(R.id.datePicker);
        Calendar calActivity = Calendar.getInstance();
        calActivity.setTime(newItem.date);
        dp.init(calActivity.get(Calendar.YEAR), calActivity.get(Calendar.MONTH), calActivity.get(Calendar.DAY_OF_MONTH), null);

        Calendar calMax = Calendar.getInstance();
        calMax.setTime(new Date());

        dp.setMaxDate(calMax.getTimeInMillis());

        Button dialogButton = (Button) dialogDate.findViewById(R.id.okButton);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dateHelper dH = new dateHelper();

                DatePicker dp = (DatePicker) dialogDate.findViewById(R.id.datePicker);
                int day = dp.getDayOfMonth();
                int month = dp.getMonth();
                int year = dp.getYear();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);

                Date nd = calendar.getTime();

                newItem.date=nd;

                bDate.setText(dH.dateToString(nd));
                dialogDate.dismiss();
            }
        });
        dialogDate.show();
    }

    public void displayCropPicker(){
        ArrayList<String> cropNames = new ArrayList<>();
        Iterator<oCrop> iterator =  currentPlot.crops.iterator();
        while(iterator.hasNext()){
            oCrop c = iterator.next();
            cropNames.add(c.name);
        }
        CharSequence cropNamesArray[] = cropNames.toArray(new CharSequence[cropNames.size()]);
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
                newItem.crop=currentPlot.crops.get(i);
                bCrop.setText(newItem.crop.name);

                dialogInterface.dismiss();
            }
        });
        AlertDialog dialogCrops = builder.create();
        dialogCrops.show();
    }

    public void displayUnitsPicker(){
        ArrayList<String> unitNames = new ArrayList<>();
        oUnit u = new oUnit(this);
        final ArrayList<oUnit> unitList = u.getUnits(0);
        Iterator<oUnit> iteratorUnits = unitList.iterator();
        while(iteratorUnits.hasNext()){
            u = iteratorUnits.next();
            unitNames.add(u.name);
        }

        CharSequence unitNamesArray[] = unitNames.toArray(new CharSequence[unitNames.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancelButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final ListAdapter adapter = new ArrayAdapter<>(this, R.layout.checked_list_template, unitNamesArray);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                newItem.units=unitList.get(i);
                bUnits.setText(newItem.units.name);
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialogUnits = builder.create();
        dialogUnits.show();

    }

    public void displayTreatmentIngredientPicker(){
        final ArrayList<oTreatmentIngredient> treatmentIngredients = new ArrayList<>();
        ArrayList<String> treatmentIngredientNames = new ArrayList<>();
        Iterator<oTreatmentIngredient> iteratorPestControl =  currentPlot.pestControlIngredients.iterator();
        while(iteratorPestControl.hasNext()){
            oTreatmentIngredient t = iteratorPestControl.next();
            treatmentIngredientNames.add(t.name);
            treatmentIngredients.add(t);
        }
        Iterator<oTreatmentIngredient> iteratorSoilManagement =  currentPlot.soilManagementIngredients.iterator();
        while(iteratorSoilManagement.hasNext()){
            oTreatmentIngredient t = iteratorSoilManagement.next();
            treatmentIngredientNames.add(t.name);
            treatmentIngredients.add(t);
        }
        CharSequence treatmentIngredientNamesArray[] = treatmentIngredientNames.toArray(new CharSequence[treatmentIngredientNames.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancelButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final ListAdapter adapter = new ArrayAdapter<>(this, R.layout.checked_list_template, treatmentIngredientNamesArray);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                newItem.treatmentIngredient =treatmentIngredients.get(i);
                bTreatment.setText(newItem.treatmentIngredient.name);

                dialogInterface.dismiss();
            }
        });
        AlertDialog dialogCrops = builder.create();
        dialogCrops.show();
    }

    public CharSequence getDefaultCostUnits(){
        CharSequence ret="";
        oUnit u = new oUnit(this);
        ret = u.getUnitNames(2).get(0);
        return ret;
    }

    public oPlot getCurrentPlot(){
        oPlot ret;
        oFarm f = new oFarm(this);
        f = f.getVersion(userId,farmId,farmVersion,this);
        oPlotMatrix p = new oPlotMatrix();
        p.fromString(this,f.plotMatrix,";");
        ret=p.plots.get(plot);
        return ret;
    }

    public void getDataItemsList(){
        oDataItem d = new oDataItem(this);
        boolean bExcludeCropSpecific = (currentPlot.crops.size()==0) ? true : false;
        boolean bExcludeTreatmentSpecific = (currentPlot.pestControlIngredients.size()==0 && currentPlot.soilManagementIngredients.size()==0) ? true : false;
        dataItemsList = d.getDataItems(bExcludeCropSpecific, bExcludeTreatmentSpecific);
        dataItemsNamesArray = d.getDataItemNames(bExcludeCropSpecific, bExcludeTreatmentSpecific).toArray(new CharSequence[dataItemsList.size()]);
    }

    public void goBack(){
        Intent i = new Intent(this, farmInterface.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion",farmVersion);
        i.putExtra("newFarm", false);
        i.putExtra("firstFarm", false);
        startActivity(i);
        finish();
    }

}
