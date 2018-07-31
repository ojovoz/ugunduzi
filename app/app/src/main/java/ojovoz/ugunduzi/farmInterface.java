package ojovoz.ugunduzi;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Eugenio on 13/03/2018.
 */
public class farmInterface extends AppCompatActivity implements httpConnection.AsyncResponse {

    RelativeLayout relativeLayout;
    Paint paint;
    TextPaint textPaint;
    View canvasView;
    Bitmap bitmap;
    Canvas canvas;

    int displayWidth;
    int displayHeight;
    Bitmap iconMove;
    Bitmap iconMoveFaded;
    Bitmap iconMoveActive;
    Bitmap iconResize;
    Bitmap iconResizeFaded;
    Bitmap iconResizeActive;
    Bitmap iconContents;
    Bitmap iconContentsFaded;
    Bitmap iconContentsActive;
    Bitmap iconActions;
    Bitmap iconActionsFaded;
    Bitmap iconActionsActive;

    String user;
    String userPass;
    int userId;
    boolean newFarm;
    boolean firstFarm;
    boolean bFarmSaved;

    int farmId=-1;
    String farmName="";
    float farmSize;
    String farmDateString;

    oFarm currentFarm;

    int state; //0 = new farm; 1 = actions; 2 = edit farm

    int farmVersion;

    oPlotMatrix plotMatrix;
    String sMatrix;

    ArrayList<oCrop> cropList;
    public CharSequence cropNamesArray[];

    ArrayList<oTreatmentIngredient> pestControlList;
    public CharSequence pestControlNamesArray[];
    ArrayList<oTreatmentIngredient> soilManagementList;
    public CharSequence soilManagementNamesArray[];

    preferenceManager prefs;

    boolean bConnecting=false;
    String server;
    ProgressDialog createFarmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_interface);

        final Context ctxt=this;

        currentFarm=new oFarm(this);

        user=getIntent().getExtras().getString("user");
        userPass=getIntent().getExtras().getString("userPass");
        userId=getIntent().getExtras().getInt("userId");
        newFarm = getIntent().getExtras().getBoolean("newFarm");
        firstFarm = getIntent().getExtras().getBoolean("firstFarm");

        prefs = new preferenceManager(this);
        server = prefs.getPreference("server");

        oCrop seed = new oCrop(this);
        cropList = seed.getCrops();
        cropNamesArray = seed.getCropNames(false).toArray(new CharSequence[cropList.size()]);

        oTreatmentIngredient start = new oTreatmentIngredient(this);
        pestControlList = start.getTreatmentIngredients(0);
        pestControlNamesArray = start.getTreatmentIngredientNames(pestControlList).toArray(new CharSequence[pestControlList.size()]);

        soilManagementList = start.getTreatmentIngredients(1);
        soilManagementNamesArray = start.getTreatmentIngredientNames(soilManagementList).toArray(new CharSequence[soilManagementList.size()]);

        if(newFarm){
            farmId=-1;
            bFarmSaved=false;
            state=0;
            this.setTitle(R.string.drawNewFarmTitle);
            farmName = getDefaultFarmName();
            defineFarmNameAcres(true,false);
        } else {
            state=1;
            farmId=getIntent().getExtras().getInt("farmId");
            if(farmId==-1){
                prefs.deletePreference("farmId");
                prefs.deletePreference("user");
                goToLogin();
            } else {
                currentFarm=currentFarm.getLatestActiveVersion(userId,farmId,this);
                farmName=currentFarm.name;
            }
            this.setTitle(farmName);
        }

        iconMove=BitmapFactory.decodeResource(this.getResources(),R.drawable.move);
        iconResize=BitmapFactory.decodeResource(this.getResources(),R.drawable.resize);
        iconContents=BitmapFactory.decodeResource(this.getResources(),R.drawable.contents);
        iconActions=BitmapFactory.decodeResource(this.getResources(),R.drawable.actions);
        iconMoveFaded=BitmapFactory.decodeResource(this.getResources(),R.drawable.move_faded);
        iconResizeFaded=BitmapFactory.decodeResource(this.getResources(),R.drawable.resize_faded);
        iconContentsFaded=BitmapFactory.decodeResource(this.getResources(),R.drawable.contents_faded);
        iconActionsFaded=BitmapFactory.decodeResource(this.getResources(),R.drawable.actions_faded);
        iconMoveActive=BitmapFactory.decodeResource(this.getResources(),R.drawable.move_active);
        iconResizeActive=BitmapFactory.decodeResource(this.getResources(),R.drawable.resize_active);
        iconContentsActive=BitmapFactory.decodeResource(this.getResources(),R.drawable.contents_active);
        iconActionsActive=BitmapFactory.decodeResource(this.getResources(),R.drawable.actions_active);

        plotMatrix = new oPlotMatrix();

        LinearLayout root = (LinearLayout) findViewById(R.id.mainRoot);
        root.post(new Runnable() {
            @Override
            public void run() {
                Rect rect = new Rect();
                Window win = getWindow();
                win.getDecorView().getWindowVisibleDisplayFrame(rect);
                int contentViewTop = win.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                displayWidth = metrics.widthPixels;
                displayHeight = (int)(metrics.heightPixels-(contentViewTop*metrics.density));

                relativeLayout = (RelativeLayout)findViewById(R.id.drawingCanvas);
                canvasView = new SketchSheetView(farmInterface.this,displayWidth,displayHeight);
                paint = new Paint();
                relativeLayout.addView(canvasView, new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                paint.setDither(true);
                paint.setColor(ContextCompat.getColor(farmInterface.this, R.color.colorDraw));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeWidth(4);

                textPaint = new TextPaint();
                textPaint.setTextSize(21);
                textPaint.setAntiAlias(true);
                textPaint.setTextAlign(Paint.Align.LEFT);
                textPaint.setColor(ContextCompat.getColor(ctxt, R.color.colorBlack));
                textPaint.setTypeface(Typeface.create("Arial", Typeface.NORMAL));

                plotMatrix.createMatrix(displayWidth,displayHeight);
                createFarm();

            }
        });
    }

    public void createFarm(){
        int mw=iconMove.getWidth();
        int mh=iconMove.getHeight();
        int rw=iconResize.getWidth();
        int rh=iconResize.getHeight();
        int cw=iconContents.getWidth();
        int ch=iconContents.getHeight();
        int aw=iconActions.getWidth();
        int ah=iconActions.getHeight();

        if(newFarm){
            plotMatrix.addPlot(mw, mh, rw, rh, cw, ch, aw, ah);
        } else if(state==1){
            plotMatrix.fromString(this,currentFarm.plotMatrix,";",mw,mh,rw,rh,cw,ch,aw,ah);
        }
    }

    @Override
    public void onBackPressed () {
        if(!bFarmSaved && state==0) {
            AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
            logoutDialog.setMessage(R.string.farmHasNotBeenSavedMessage);
            logoutDialog.setNegativeButton(R.string.noButtonText,null);
            logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            logoutDialog.create();
            logoutDialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.clear();

        if(state==0) {
            menu.add(0, 0, 0, R.string.opAddPlot);
            menu.add(1, 1, 1, R.string.opDeletePlot);
            menu.add(2, 2, 2, R.string.opEditFarmNameSize);
            menu.add(3, 3, 3, R.string.opSaveFarm);
            if(!firstFarm) {
                menu.add(4, 4, 4, R.string.opCancelNewFarm);
            }
        } else if(state==1){
            menu.add(0, 0, 0, R.string.opManageFarmRecords);
            menu.add(1, 1, 1, R.string.opEditFarm);
            menu.add(2, 2, 2, R.string.opCreateNewFarm);
            if(prefs.getNumberOfActiveFarms(user,";")>1) {
                menu.add(3, 3, 3, R.string.opGoToOtherFarm);
            }
            menu.add(4, 4, 4, R.string.opSwitchUser);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(state==0) {
            switch (item.getItemId()) {
                case 0:
                    addPlot();
                    canvasView.invalidate();
                    break;
                case 1:
                    deleteSelectedPlot();
                    canvasView.invalidate();
                    break;
                case 2:
                    defineFarmNameAcres(true,false);
                    break;
                case 3:
                    saveFarm();
                    break;
                case 4:
                    cancelNewFarm();
            }
        } else if (state==1){
            switch(item.getItemId()){
                case 0:
                    goToRecords(true);
                    break;
                case 1:
                    break;
                case 2:
                    createNewFarm();
                    break;
                case 3:
                    goToFarmChooser();
                    break;
                case 4:
                    confirmExit();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void confirmExit(){
        AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);

        logoutDialog.setMessage(getString(R.string.logoutConfirmMessage));
        logoutDialog.setNegativeButton(R.string.noButtonText,null);
        logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                goToLogin();
            }
        });
        logoutDialog.create();
        logoutDialog.show();
    }

    public void goToLogin(){
        prefs.savePreference("user","");
        prefs.deletePreference("farmId");
        final Context context = this;
        Intent i = new Intent(context, login.class);
        startActivity(i);
        finish();
    }

    public void goToFarmChooser(){
        final Context context = this;
        Intent i = new Intent(context, farmChooser.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        startActivity(i);
        finish();
    }

    public String getDefaultFarmName(){
        String ret = "";

        if(firstFarm){
            ret = getString(R.string.defaultFarmNamePrefix)+" 1";
        } else {
            int n=1;
            ret = getString(R.string.defaultFarmNamePrefix)+" "+Integer.toString(n);
            do{
                if(!currentFarm.farmNameExists(userId,ret)){
                    break;
                } else{
                    ret=getString(R.string.defaultFarmNamePrefix)+" "+Integer.toString(n);
                    n++;
                }
            } while(true);
        }
        return ret;
    }

    public void createNewFarm(){
        plotMatrix=new oPlotMatrix();
        plotMatrix.createMatrix(displayWidth,displayHeight);
        plotMatrix.addPlot(iconMove.getWidth(), iconMove.getHeight(), iconResize.getWidth(), iconResize.getHeight(), iconContents.getWidth(), iconContents.getHeight(), iconActions.getWidth(), iconActions.getHeight());
        state=0;
        bFarmSaved=false;
        canvasView.invalidate();
        farmId=-1;
        farmName = getDefaultFarmName();
        defineFarmNameAcres(true,false);
    }

    public void cancelNewFarm(){
        AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
        logoutDialog.setMessage(R.string.cancelNewFarmMessage);
        logoutDialog.setNegativeButton(R.string.noButtonText,null);
        logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doCancelNewFarm();
            }
        });
        logoutDialog.create();
        logoutDialog.show();
    }

    public void doCancelNewFarm(){
        if(prefs.preferenceExists("farmId")){
            farmId=prefs.getPreferenceInt("farmId");
            if(farmId>=0){
                plotMatrix = new oPlotMatrix();
                plotMatrix.createMatrix(displayWidth,displayHeight);
                currentFarm = currentFarm.getLatestActiveVersion(userId,farmId,this);
                plotMatrix.fromString(this,currentFarm.plotMatrix,";",iconMove.getWidth(), iconMove.getHeight(), iconResize.getWidth(), iconResize.getHeight(), iconContents.getWidth(), iconContents.getHeight(), iconActions.getWidth(), iconActions.getHeight());
                state=1;
                this.setTitle(farmName);
                canvasView.invalidate();
            } else {
                goToFarmChooser();
            }
        } else {
            goToFarmChooser();
        }
    }

    public void addPlot(){
        if(!plotMatrix.addPlot(iconMove.getWidth(), iconMove.getHeight(), iconResize.getWidth(), iconResize.getHeight(), iconContents.getWidth(), iconContents.getHeight(), iconActions.getWidth(), iconActions.getHeight())){
            Toast.makeText(this, R.string.noSpaceForNewPlotMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteSelectedPlot(){
        if(plotMatrix.currentPlot!=null){
            AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
            logoutDialog.setMessage(R.string.deletePlotConfirmMessage);
            logoutDialog.setNegativeButton(R.string.noButtonText,null);
            logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(!plotMatrix.deletePlot()){
                        Toast.makeText(farmInterface.this, R.string.farmMustHavePlotMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        canvasView.invalidate();
                    }
                }
            });
            logoutDialog.create();
            logoutDialog.show();
        } else {
            Toast.makeText(this, R.string.selectPlotMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void definePlotContents(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_define_plot_contents);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                plotMatrix.currentPlot.state=1;
                dialog.dismiss();
                canvasView.invalidate();
            }
        });

        Button okButton = (Button)dialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                switch (v.getId()) {
                    case R.id.okButton:
                        plotMatrix.currentPlot.state=1;
                        dialog.dismiss();
                        canvasView.invalidate();
                        break;
                    default:
                        break;
                }
            }
        });

        Button crops = (Button)dialog.findViewById(R.id.cropButton);
        crops.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.cropButton:
                        showCropSelector();
                        break;
                    default:
                        break;
                }
            }
        });

        Button pestControl = (Button)dialog.findViewById(R.id.pestControlButton);
        pestControl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.pestControlButton:
                        showPestControlIngredientSelector();
                        break;
                    default:
                        break;
                }
            }
        });

        Button soilManagement = (Button)dialog.findViewById(R.id.soilManagementButton);
        soilManagement.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.soilManagementButton:
                        showSoilManagementIngredientSelector();
                        break;
                    default:
                        break;
                }
            }
        });

        dialog.show();
    }

    public void showCropSelector(){
        boolean[] checkedCrops = new boolean[cropNamesArray.length];
        for(int i=0;i<cropNamesArray.length;i++){
            checkedCrops[i]=(plotMatrix.currentPlot.crops.contains(cropList.get(i)));
        }

        DialogInterface.OnMultiChoiceClickListener cropsDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    plotMatrix.currentPlot.crops.add(cropList.get(which));
                } else {
                    plotMatrix.currentPlot.crops.remove(cropList.get(which));
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.selectCropsTitle);
        builder.setMultiChoiceItems(cropNamesArray, checkedCrops, cropsDialogListener);
        builder.setPositiveButton(R.string.okButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                plotMatrix.currentPlot.state=1;
                canvasView.invalidate();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void showPestControlIngredientSelector(){
        boolean[] checkedIngredients = new boolean[pestControlNamesArray.length];
        for(int i=0;i<pestControlNamesArray.length;i++){
            checkedIngredients[i]=(plotMatrix.currentPlot.pestControlIngredients.contains(pestControlList.get(i)));
        }

        DialogInterface.OnMultiChoiceClickListener ingredientsDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    plotMatrix.currentPlot.pestControlIngredients.add(pestControlList.get(which));
                } else {
                    plotMatrix.currentPlot.pestControlIngredients.remove(pestControlList.get(which));
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.selectPestControlIngredientsTitle);
        builder.setMultiChoiceItems(pestControlNamesArray, checkedIngredients, ingredientsDialogListener);
        builder.setPositiveButton(R.string.okButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                plotMatrix.currentPlot.state=1;
                canvasView.invalidate();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void showSoilManagementIngredientSelector(){
        boolean[] checkedIngredients = new boolean[soilManagementNamesArray.length];
        for(int i=0;i<soilManagementNamesArray.length;i++){
            checkedIngredients[i]=(plotMatrix.currentPlot.soilManagementIngredients.contains(soilManagementList.get(i)));
        }

        DialogInterface.OnMultiChoiceClickListener ingredientsDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    plotMatrix.currentPlot.soilManagementIngredients.add(soilManagementList.get(which));
                } else {
                    plotMatrix.currentPlot.soilManagementIngredients.remove(soilManagementList.get(which));
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.selectSoilManagementIngredientsTitle);
        builder.setMultiChoiceItems(soilManagementNamesArray, checkedIngredients, ingredientsDialogListener);
        builder.setPositiveButton(R.string.okButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                plotMatrix.currentPlot.state=1;
                canvasView.invalidate();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void defineFarmNameAcres(boolean cancellable, final boolean isSaving){

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_define_new_farm);
        dialog.setCanceledOnTouchOutside(cancellable);
        dialog.setCancelable(cancellable);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                farmName="";
            }
        });

        EditText et = (EditText)dialog.findViewById(R.id.newFarm);
        String defaultFarmName = farmName;

        et.setText(defaultFarmName);

        EditText fSize = (EditText)dialog.findViewById(R.id.acres);
        if(farmSize>0){
            fSize.setText(Float.toString(farmSize));
        } else {
            fSize.setText(Float.toString(1f));
        }

        Button button = (Button)dialog.findViewById(R.id.okButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = (EditText)dialog.findViewById(R.id.newFarm);
                String fName = et.getText().toString();
                if(fName.isEmpty()){
                    Toast.makeText(view.getContext(), R.string.farmNameCannotBeEmptyMessage, Toast.LENGTH_SHORT).show();
                } else {
                    if(currentFarm.farmNameExists(userId,fName)){
                        Toast.makeText(view.getContext(), R.string.farmNameRepeated, Toast.LENGTH_SHORT).show();
                    } else {
                        EditText fSize = (EditText) dialog.findViewById(R.id.acres);
                        float farmSize = Float.parseFloat(fSize.getText().toString());
                        if (farmSize <= 0) {
                            Toast.makeText(view.getContext(), R.string.farmSizeMustBeAboveZero, Toast.LENGTH_SHORT).show();
                        } else {
                            updateFarmData(fName, farmSize);
                            dialog.dismiss();
                            if(isSaving){
                                saveFarm();
                            }
                        }
                    }
                }
            }
        });

        dialog.show();
    }

    public void showActionChooser(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_action_chooser);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        TextView tt = (TextView)dialog.findViewById(R.id.plotLabel);

        String title= getString(R.string.cropsTitle) + ": " + plotMatrix.currentPlot.getCropNames(this);
        title+="\n";
        title+=getString(R.string.pestControlTitle) + ": " + plotMatrix.currentPlot.getPestControlNames(this);
        title+="\n";
        title+=getString(R.string.soilManagementTitle) + ": " + plotMatrix.currentPlot.getSoilManagementNames(this);

        if(plotMatrix.currentPlot.pestControlIngredients.size()>0 && plotMatrix.currentPlot.soilManagementIngredients.size()>0) {
            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagementAndPestControl));
        } else if(plotMatrix.currentPlot.pestControlIngredients.size()>0 && plotMatrix.currentPlot.soilManagementIngredients.size()==0) {
            tt.setBackgroundColor(ContextCompat.getColor(this,R.color.colorFillPestControl));
        } else if(plotMatrix.currentPlot.pestControlIngredients.size()==0 && plotMatrix.currentPlot.soilManagementIngredients.size()>0) {
            tt.setBackgroundColor(ContextCompat.getColor(this,R.color.colorFillSoilManagement));
        } else {
            tt.setBackgroundColor(ContextCompat.getColor(this,R.color.colorFillDefault));
        }

        tt.setText(title);

        Button dataButton = (Button)dialog.findViewById(R.id.dataButton);
        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToDataEntry();
            }
        });

        Button psButton = (Button)dialog.findViewById(R.id.pictureSoundButton);
        psButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPictureSound();
            }
        });

        Button recordsButton = (Button)dialog.findViewById(R.id.manageRecordsButton);
        recordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRecords(false);
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                plotMatrix.currentPlot.state=1;
                canvasView.invalidate();
            }
        });

        dialog.show();
    }

    public void goToDataEntry(){
        final Context context = this;
        Intent i = new Intent(context, enterData.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName",farmName);
        i.putExtra("plot",plotMatrix.currentPlot.id);
        if(plotMatrix.currentPlot.crop1!=null) {
            i.putExtra("crop1", plotMatrix.currentPlot.crop1.id);
        } else {
            i.putExtra("crop1", -1);
        }
        if(plotMatrix.currentPlot.crop2!=null) {
            i.putExtra("crop2", plotMatrix.currentPlot.crop2.id);
        } else {
            i.putExtra("crop2", -1);
        }
        if(plotMatrix.currentPlot.treatment1!=null) {
            i.putExtra("treatment1", plotMatrix.currentPlot.treatment1.id);
        } else {
            i.putExtra("treatment1", -1);
        }
        if(plotMatrix.currentPlot.treatment2!=null) {
            i.putExtra("treatment2", plotMatrix.currentPlot.treatment2.id);
        } else {
            i.putExtra("treatment2", -1);
        }
        startActivity(i);
        finish();
    }

    public void goToPictureSound(){
        final Context context = this;
        Intent i = new Intent(context, pictureSound.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName",farmName);
        i.putExtra("plot",plotMatrix.currentPlot.id);
        if(plotMatrix.currentPlot.crop1!=null) {
            i.putExtra("crop1", plotMatrix.currentPlot.crop1.id);
        } else {
            i.putExtra("crop1", -1);
        }
        if(plotMatrix.currentPlot.crop2!=null) {
            i.putExtra("crop2", plotMatrix.currentPlot.crop2.id);
        } else {
            i.putExtra("crop2", -1);
        }
        if(plotMatrix.currentPlot.treatment1!=null) {
            i.putExtra("treatment1", plotMatrix.currentPlot.treatment1.id);
        } else {
            i.putExtra("treatment1", -1);
        }
        if(plotMatrix.currentPlot.treatment2!=null) {
            i.putExtra("treatment2", plotMatrix.currentPlot.treatment2.id);
        } else {
            i.putExtra("treatment2", -1);
        }
        startActivity(i);
        finish();
    }

    public void goToRecords(boolean bFarm){
        final Context context = this;
        Intent i = new Intent(context, dataManager.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName",farmName);
        if(bFarm){
            i.putExtra("plot", -1);
        } else {
            i.putExtra("plot", plotMatrix.currentPlot.id);
        }
        if(plotMatrix.currentPlot.crop1!=null) {
            i.putExtra("crop1", plotMatrix.currentPlot.crop1.id);
        } else {
            i.putExtra("crop1", -1);
        }
        if(plotMatrix.currentPlot.crop2!=null) {
            i.putExtra("crop2", plotMatrix.currentPlot.crop2.id);
        } else {
            i.putExtra("crop2", -1);
        }
        if(plotMatrix.currentPlot.treatment1!=null) {
            i.putExtra("treatment1", plotMatrix.currentPlot.treatment1.id);
        } else {
            i.putExtra("treatment1", -1);
        }
        if(plotMatrix.currentPlot.treatment2!=null) {
            i.putExtra("treatment2", plotMatrix.currentPlot.treatment2.id);
        } else {
            i.putExtra("treatment2", -1);
        }
        startActivity(i);
        finish();
    }

    public void updateFarmData(String fName, float fSize){
        fName = fName.replaceAll("[^A-Za-z0-9 ]", "");
        fName = fName.trim();
        this.setTitle(getString(R.string.drawNewFarmTitle)+ ": " + fName);
        farmName = fName;
        farmSize = fSize;
    }

    public void saveFarm(){

        dateHelper dH = new dateHelper();

        sMatrix = plotMatrix.toString();

        if(farmName.isEmpty()){
            farmName=getDefaultFarmName();
            defineFarmNameAcres(false,true);
        } else {

            Date farmDate = new Date();
            farmDateString = dH.dateToString(farmDate);

            if(farmId==-1){
                farmId=currentFarm.getFarmIdFromNameUser(userId, farmName);
            }

            farmVersion = (newFarm) ? 0 : currentFarm.version+1;

            String saveString = user + ";" + userPass + ";" + farmName + ";" + String.valueOf(farmSize) + ";" + farmDateString + ";" + sMatrix + ";" + farmId;
            httpConnection http = new httpConnection(this, this);
            if (http.isOnline()) {
                CharSequence dialogTitle = getString(R.string.createNewFarmLabel);

                createFarmDialog = new ProgressDialog(this);
                createFarmDialog.setCancelable(true);
                createFarmDialog.setCanceledOnTouchOutside(false);
                createFarmDialog.setMessage(dialogTitle);
                createFarmDialog.setIndeterminate(true);
                createFarmDialog.show();
                createFarmDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface d) {
                        bConnecting = false;
                        createFarmDialog.dismiss();
                    }
                });
                doCreateNewFarm(saveString);
            } else {
                currentFarm.addNewFarm(farmId,userId,farmName,farmSize,farmDate,sMatrix,farmVersion,0);
                prefs.savePreferenceInt("farmId", farmId);
                Toast.makeText(this, R.string.farmSavedMessage, Toast.LENGTH_SHORT).show();
                state = 1;
                canvasView.invalidate();
                firstFarm = false;
                this.setTitle(farmName);
                currentFarm = currentFarm.getLatestActiveVersion(userId,farmId,this);
            }
        }
    }

    public void doCreateNewFarm(String s){
        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            if (!bConnecting) {
                bConnecting = true;
                http.execute(server + "/mobile/create_new_farm.php?farm=" + s.replaceAll(" ","_"), "");
            }
        }
    }

    @Override
    public void processFinish(String output) {
        bConnecting=false;
        createFarmDialog.dismiss();
        Date farmDate = new Date();
        if(!output.equals("ko") && !output.isEmpty()){
            currentFarm.addNewFarm(farmId,userId,farmName,farmSize,farmDate,sMatrix,farmVersion,2);
        } else {
            currentFarm.addNewFarm(farmId,userId,farmName,farmSize,farmDate,sMatrix,farmVersion,0);
        }
        prefs.savePreferenceInt("farmId", farmId);
        currentFarm = currentFarm.getLatestActiveVersion(userId,farmId,this);
        if(state==0){
            state=1;
            firstFarm=false;
            canvasView.invalidate();
            this.setTitle(farmName);
        }
        Toast.makeText(this, R.string.farmSavedMessage, Toast.LENGTH_SHORT).show();
    }

    class SketchSheetView extends View {

        Context context;

        public SketchSheetView(Context c, int w, int h) {

            super(c);
            context=c;
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(bitmap);
            this.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_MOVE || event.getActionMasked() == MotionEvent.ACTION_UP) {
                invalidate();
                boolean b = plotMatrix.passEvent(event,state);
                if(plotMatrix.currentPlot!=null) {
                    if (plotMatrix.currentPlot.state == 4 && state==0) {
                        definePlotContents();
                    } else if (plotMatrix.currentPlot.state == 5 && state==1){
                        showActionChooser();
                    }
                }
                return b;
            } else {
                invalidate();
                return true;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int fillColor;
            int borderColor;

            Bitmap iMove;
            Bitmap iResize;
            Bitmap iContents;
            Bitmap iActions;

            Iterator<oPlot> iterator = plotMatrix.getPlots().iterator();
            while (iterator.hasNext()) {
                oPlot plot = iterator.next();
                fillColor=getFillColor(plot,plot==plotMatrix.currentPlot);
                borderColor= (plot==plotMatrix.currentPlot) ? ContextCompat.getColor(context, R.color.colorDraw) : ContextCompat.getColor(context, R.color.colorDrawFaded);
                if(state==0) {
                    iMove = (plot == plotMatrix.currentPlot) ? (plotMatrix.currentPlot.state == 2) ? iconMoveActive : iconMove : iconMoveFaded;
                    iResize = (plot == plotMatrix.currentPlot) ? (plotMatrix.currentPlot.state == 3) ? iconResizeActive : iconResize : iconResizeFaded;
                    iContents = (plot == plotMatrix.currentPlot) ? (plotMatrix.currentPlot.state == 4) ? iconContentsActive : iconContents : iconContentsFaded;
                    drawPlot(canvas, plot, borderColor, fillColor, iMove, iResize, iContents);
                } else if(state==1){
                    iActions = (plot == plotMatrix.currentPlot) ? (plotMatrix.currentPlot.state == 5) ? iconActionsActive : iconActions : iconActionsFaded;
                    drawPlot(canvas, plot, borderColor, fillColor, iActions);
                }
            }

            if(plotMatrix.ghostPlot !=null && state==0){
                drawGhostRectangle(canvas, plotMatrix.ghostPlot, ContextCompat.getColor(context, R.color.colorDrawGhostRectangle));
            }
        }

        public int getFillColor(oPlot p, boolean strong){
            int ret;
            if(p.pestControlIngredients.size()>0 && p.soilManagementIngredients.size()>0) {
                ret = (strong) ? ContextCompat.getColor(context, R.color.colorFillSoilManagementAndPestControl) : ContextCompat.getColor(context, R.color.colorFillSoilManagementAndPestControlFaded);
            } else if(p.pestControlIngredients.size()>0 && p.soilManagementIngredients.size()==0) {
                ret = (strong) ? ContextCompat.getColor(context,R.color.colorFillPestControl) : ContextCompat.getColor(context,R.color.colorFillPestControlFaded);
            } else if(p.pestControlIngredients.size()==0 && p.soilManagementIngredients.size()>0) {
                ret = (strong) ? ContextCompat.getColor(context,R.color.colorFillSoilManagement) : ContextCompat.getColor(context,R.color.colorFillSoilManagementFaded);
            } else {
                ret = (strong) ? ContextCompat.getColor(context,R.color.colorFillDefault) : ContextCompat.getColor(context,R.color.colorFillFaded);
            }
            return ret;
        }

        private void drawPlot(Canvas canvas, oPlot p, int border, int fill, Bitmap iMove, Bitmap iResize, Bitmap iContents){
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(fill);
            canvas.drawRect(p.x,p.y,p.x+p.w,p.y+p.h,paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(border);
            canvas.drawRect(p.x,p.y,p.x+p.w,p.y+p.h,paint);
            canvas.drawBitmap(iResize,p.iResizeX,p.iResizeY,paint);
            canvas.drawBitmap(iContents,p.iContentsX,p.iContentsY,paint);
            float yOffset = (((p.h/(displayHeight/4))-1)*30)+(20-p.crops.size());
            drawPlotCropLabels(canvas, p, p.iContentsY+p.iContentsH+yOffset);
        }

        private void drawPlot(Canvas canvas, oPlot p, int border, int fill, Bitmap iActions){
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(fill);
            canvas.drawRect(p.x,p.y,p.x+p.w,p.y+p.h,paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(border);
            canvas.drawRect(p.x,p.y,p.x+p.w,p.y+p.h,paint);
            canvas.drawBitmap(iActions,p.iActionsX,p.iActionsY,paint);
            float yOffset = (((p.h/(displayHeight/4))-1)*30)+(20-p.crops.size());
            drawPlotCropLabels(canvas, p, p.iActionsY+p.iActionsH+yOffset);
        }

        private void drawPlotCropLabels(Canvas canvas, oPlot p, float txtY){
            Rect txtBounds = new Rect();
            float txtX;

            float fontSize = ((((p.w/(displayWidth/4)+(p.h/(displayHeight/4)))-2)*10)/14)+21;

            textPaint.setTextSize(fontSize);

            Iterator<oCrop> iterator = p.crops.iterator();
            while (iterator.hasNext()) {
                oCrop c = iterator.next();
                textPaint.getTextBounds(c.name, 0, c.name.length(), txtBounds);

                int n=c.name.length();
                while(txtBounds.width()>(p.w)){
                    n--;
                    textPaint.getTextBounds(c.name, 0, n, txtBounds);
                }

                txtX = ((p.w - txtBounds.width()) / 2) + p.x;
                canvas.drawText(c.name.substring(0,n), (int) txtX, (int) txtY, textPaint);

                txtY+=(((p.h/(displayHeight/4))-1)*3)+24;

            }
        }

        private void drawGhostRectangle(Canvas canvas, oPlot gR, int border){
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(border);
            canvas.drawRect(gR.x,gR.y,gR.x+gR.w+1,gR.y+gR.h+1,paint);
        }
    }
}
