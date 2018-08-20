package ojovoz.ugunduzi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

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
        newItem = new oLog(this);

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_edit_data_item);
        dialog.getWindow().setLayout(displayWidth-10,displayHeight-10);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        final Button bDataItem = (Button) dialog.findViewById(R.id.dataItemButton);
        final Button bDate = (Button) dialog.findViewById(R.id.dateButton);
        final Button bCrop = (Button) dialog.findViewById(R.id.cropButton);
        final Button bTreatment = (Button) dialog.findViewById(R.id.treatmentButton);

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
                            newItem.dataItem=d;
                            bDataItem.setText(d.name);
                            if(currentPlot.crops.size()>1 && d.isCropSpecific){
                                bCrop.setVisibility(View.VISIBLE);
                            }
                            if((currentPlot.pestControlIngredients.size()>0 || currentPlot.soilManagementIngredients.size()>0) && d.isTreatmentSpecific){
                                bTreatment.setVisibility(View.VISIBLE);
                            }
                            bDate.setVisibility(View.VISIBLE);
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
