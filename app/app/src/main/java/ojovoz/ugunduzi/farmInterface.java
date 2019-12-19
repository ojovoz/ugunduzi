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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.math.BigDecimal;
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

    Bitmap historyLeft;
    Bitmap historyRight;
    int arrowShowing; // -1 = none, 0 = left, 1 = right

    String user;
    String userPass;
    int userId;
    boolean newFarm;
    boolean firstFarm;
    boolean bFarmSaved;

    int baseTextSize;

    boolean bSaveEditedFarmAsNew;

    int farmId = -1;
    String farmName = "";
    float farmSize = 1;
    String farmDateString;

    oFarm currentFarm;

    int state; //0 = new farm; 1 = actions; 2 = edit farm

    int farmVersion;
    int maxVersion;

    oPlotMatrix plotMatrix;
    String sMatrix;

    ArrayList<oCrop> cropList;
    public CharSequence cropNamesArray[];

    ArrayList<oTreatmentIngredient> pestControlList;
    public CharSequence pestControlNamesArray[];
    ArrayList<oTreatmentIngredient> soilManagementList;
    public CharSequence soilManagementNamesArray[];

    oPlot tempPlot;

    preferenceManager prefs;

    boolean bConnecting = false;
    int connectionTask; //0=create new farm, 1=delete farm
    String server;
    ProgressDialog createFarmDialog;
    ProgressDialog deleteFarmDialog;

    private Handler historyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            arrowShowing = -1;
            canvasView.invalidate();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_interface);

        final Context ctxt = this;

        currentFarm = new oFarm(this);

        user = getIntent().getExtras().getString("user");
        userPass = getIntent().getExtras().getString("userPass");
        userId = getIntent().getExtras().getInt("userId");
        newFarm = getIntent().getExtras().getBoolean("newFarm");
        firstFarm = getIntent().getExtras().getBoolean("firstFarm");
        int initialVersion = getIntent().getExtras().getInt("farmVersion");

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

        iconMove = BitmapFactory.decodeResource(this.getResources(), R.drawable.move);
        iconResize = BitmapFactory.decodeResource(this.getResources(), R.drawable.resize);
        iconContents = BitmapFactory.decodeResource(this.getResources(), R.drawable.contents);
        iconActions = BitmapFactory.decodeResource(this.getResources(), R.drawable.actions);
        iconMoveFaded = BitmapFactory.decodeResource(this.getResources(), R.drawable.move_faded);
        iconResizeFaded = BitmapFactory.decodeResource(this.getResources(), R.drawable.resize_faded);
        iconContentsFaded = BitmapFactory.decodeResource(this.getResources(), R.drawable.contents_faded);
        iconActionsFaded = BitmapFactory.decodeResource(this.getResources(), R.drawable.actions_faded);
        iconMoveActive = BitmapFactory.decodeResource(this.getResources(), R.drawable.move_active);
        iconResizeActive = BitmapFactory.decodeResource(this.getResources(), R.drawable.resize_active);
        iconContentsActive = BitmapFactory.decodeResource(this.getResources(), R.drawable.contents_active);
        iconActionsActive = BitmapFactory.decodeResource(this.getResources(), R.drawable.actions_active);

        historyLeft = BitmapFactory.decodeResource(this.getResources(), R.drawable.history_left);
        historyRight = BitmapFactory.decodeResource(this.getResources(), R.drawable.history_right);
        arrowShowing = -1;

        plotMatrix = new oPlotMatrix();

        if (newFarm) {
            state = 0;
            farmId = -1;
            bFarmSaved = false;
            this.setTitle(R.string.drawNewFarmTitle);
            farmName = getDefaultFarmName();
        } else {
            state = 1;
            farmId = getIntent().getExtras().getInt("farmId");
            if (farmId == -1) {
                prefs.deletePreference("farmId");
                prefs.deletePreference("user");
                goToLogin();
            } else {
                if (initialVersion == -1) {
                    currentFarm = currentFarm.getLatestActiveVersion(userId, farmId, this);
                    maxVersion = farmVersion = currentFarm.version;
                    this.setTitle(currentFarm.name + " (" + user + ")");
                } else {
                    currentFarm = currentFarm.getVersion(userId, farmId, initialVersion, this);
                    maxVersion = currentFarm.getMaxVersionNumber(userId, farmId, this);
                    farmVersion = initialVersion;
                    if (farmVersion == maxVersion) {
                        this.setTitle(currentFarm.name + " (" + user + ")");
                    } else {
                        dateHelper dH = new dateHelper();
                        this.setTitle(currentFarm.name + ": " + dH.dateToString(currentFarm.dateCreated) + " (" + user + ")");
                    }
                }
                farmName = currentFarm.name;
                farmSize = currentFarm.size;
            }
        }

        final LinearLayout root = (LinearLayout) findViewById(R.id.mainRoot);
        ViewTreeObserver o = root.getViewTreeObserver();
        o.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                displayWidth = root.getWidth();
                displayHeight = root.getHeight();

                if(displayHeight>0) {

                    relativeLayout = (RelativeLayout) findViewById(R.id.drawingCanvas);
                    canvasView = new SketchSheetView(farmInterface.this, displayWidth, displayHeight);
                    paint = new Paint();
                    relativeLayout.addView(canvasView, new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                    paint.setDither(true);
                    paint.setColor(ContextCompat.getColor(farmInterface.this, R.color.colorDraw));
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    paint.setStrokeWidth(2 * getResources().getDisplayMetrics().scaledDensity);

                    textPaint = new TextPaint();
                    baseTextSize = (int)(13 * getResources().getDisplayMetrics().scaledDensity);
                    textPaint.setTextSize(baseTextSize);
                    textPaint.setAntiAlias(true);
                    textPaint.setTextAlign(Paint.Align.LEFT);
                    textPaint.setColor(ContextCompat.getColor(ctxt, R.color.colorBlack));
                    textPaint.setTypeface(Typeface.create("Arial", Typeface.NORMAL));

                    plotMatrix.createMatrix(displayWidth, displayHeight);
                    if (newFarm) {
                        defineFarmNameAcres(true, false);
                    }
                    createFarm();

                    relativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }


    public void createFarm() {
        int rw = iconResize.getWidth();
        int rh = iconResize.getHeight();
        int cw = iconContents.getWidth();
        int ch = iconContents.getHeight();
        int aw = iconActions.getWidth();
        int ah = iconActions.getHeight();

        if (newFarm) {
            plotMatrix.addPlot(rw, rh, cw, ch, aw, ah);
        } else if (state == 1 || state == 2) {
            plotMatrix.fromString(this, currentFarm.plotMatrix, ";", rw, rh, cw, ch, aw, ah);
        }
    }

    @Override
    public void onBackPressed() {
        boolean bProceed = true;
        if (!bFarmSaved && (state == 0 || state == 2)) {
            if (state == 2) {
                bProceed = farmHasBeenEdited();
            }
            if (bProceed) {
                AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
                logoutDialog.setMessage(R.string.farmHasNotBeenSavedMessage);
                logoutDialog.setNegativeButton(R.string.noButtonText, null);
                logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(state==2) {
                            doCancelEditedFarm();
                        } else {
                            finish();
                        }
                    }
                });
                logoutDialog.create();
                logoutDialog.show();
            } else {
                doCancelEditedFarm();
            }
        } else if (farmVersion != maxVersion) {
            goToLatestFarm();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.clear();

        if (state == 0 || state == 2) {
            menu.add(0, 0, 0, R.string.opAddPlot);
            menu.add(1, 1, 1, R.string.opDeletePlot);
            menu.add(2, 2, 2, R.string.opEditFarmNameSize);
            menu.add(3, 3, 3, R.string.opSaveFarm);
            if (!firstFarm && state == 0) {
                menu.add(4, 4, 4, R.string.opCancelNewFarm);
            } else if (state == 2) {
                menu.add(4, 4, 4, R.string.opCancelEditing);
            }
        } else if (state == 1) {
            menu.add(0, 0, 0, R.string.opManageFarmRecords);
            menu.add(1, 1, 1, R.string.opFarmBalance);
            if (farmVersion == maxVersion) {
                menu.add(2, 2, 2, R.string.opEditFarm);
            } else {
                menu.add(2, 2, 2, R.string.opGoToCurrentState);
            }
            if (farmVersion == maxVersion) {
                menu.add(3, 3, 3, R.string.opDeleteFarm);
                menu.add(4, 4, 4, R.string.opCreateNewFarm);
                if (currentFarm.getNumberOfFarms(userId) > 1) {
                    menu.add(5, 5, 5, R.string.opGoToOtherFarm);
                }
                menu.add(6, 6, 6, R.string.opGoToWeb);
                menu.add(7, 7, 7, R.string.opSwitchUser);
            }

        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (state == 0 || state == 2) {
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
                    defineFarmNameAcres(true, false);
                    break;
                case 3:
                    saveFarm();
                    break;
                case 4:
                    if (state == 0) {
                        cancelNewFarm();
                    } else {
                        cancelEditedFarm();
                    }
            }
        } else if (state == 1) {
            switch (item.getItemId()) {
                case 0:
                    goToFarmRecords();
                    break;
                case 1:
                    goToFarmBalance();
                    break;
                case 2:
                    if (farmVersion == maxVersion) {
                        startEditFarm();
                    } else {
                        goToLatestFarm();
                    }
                    break;
                case 3:
                    deleteFarm();
                    break;
                case 4:
                    createNewFarm();
                    break;
                case 5:
                    goToFarmChooser();
                    break;
                case 6:
                    goToWebPage();
                    break;
                case 7:
                    confirmExit();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void confirmExit() {
        AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);

        logoutDialog.setMessage(getString(R.string.logoutConfirmMessage));
        logoutDialog.setNegativeButton(R.string.noButtonText, null);
        logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                goToLogin();
            }
        });
        logoutDialog.create();
        logoutDialog.show();
    }

    public void deleteFarm() {
        AlertDialog.Builder deleteFarmDialog = new AlertDialog.Builder(this);

        deleteFarmDialog.setMessage(getString(R.string.deleteThisFarmConfirmMessage));
        deleteFarmDialog.setNegativeButton(R.string.noButtonText, null);
        deleteFarmDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doDeleteFarm();
            }
        });
        deleteFarmDialog.create();
        deleteFarmDialog.show();
    }

    public void doDeleteFarm() {

        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            bConnecting = true;
            connectionTask = 1;
            CharSequence dialogTitle = getString(R.string.deletingFarmLabel);

            deleteFarmDialog = new ProgressDialog(this);
            deleteFarmDialog.setCancelable(true);
            deleteFarmDialog.setCanceledOnTouchOutside(false);
            deleteFarmDialog.setMessage(dialogTitle);
            deleteFarmDialog.setIndeterminate(true);
            deleteFarmDialog.show();
            deleteFarmDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface d) {
                    bConnecting = false;
                    deleteFarmDialog.dismiss();
                }
            });
            http.execute(server + "/mobile/delete_farm.php?user=" + userId + "&farm=" + Integer.toString(farmId), "");
        } else {
            markDeletedFarm();
            afterFarmDeletion();
        }
    }

    public void markDeletedFarm() {
        oFarm f = new oFarm(this);
        int[] idsToDelete = f.getFarmLineList(userId, farmId);
        f.updateFarmStatus(idsToDelete, 1);
        deleteFarmLogs(farmId);
    }

    public void deleteFarmLogs(int id) {
        oLog l = new oLog(this);
        int[] ids = new int[1];
        ids[0] = id;
        ArrayList<String> deleteFiles = l.deleteFarmItems(ids);
        deleteImgSndFiles(deleteFiles);
    }

    public void deleteImgSndFiles(ArrayList<String> deleteFiles) {
        Iterator<String> iterator = deleteFiles.iterator();
        while (iterator.hasNext()) {
            String f = iterator.next();
            File fileX = new File(f);
            long imgFileDate = fileX.lastModified();
            fileX.delete();
            if (f.contains("jpg")) {
                String defaultGalleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + "Camera";
                File imgs = new File(defaultGalleryPath);
                File imgsArray[] = imgs.listFiles();
                for (int i = 0; i < imgsArray.length; i++) {
                    if (Math.abs(imgsArray[i].lastModified() - imgFileDate) <= 3000) {
                        imgsArray[i].delete();
                        break;
                    }
                }
            }
        }
    }

    public void executeDeleteFarm() {
        oFarm f = new oFarm(this);
        int[] idsToDelete = f.getFarmLineList(userId, farmId);
        f.doDeleteFarm(idsToDelete);
        deleteFarmLogs(farmId);
    }

    public void afterFarmDeletion() {
        oFarm f = new oFarm(this);
        ArrayList<oFarm> userFarms = f.getActiveFarms(userId);
        if (userFarms.size() > 0) {
            if (userFarms.size() > 1) {
                goToFarmChooser();
            } else {
                currentFarm = userFarms.get(0);
                currentFarm.context = this;
                farmId = currentFarm.id;
                prefs.savePreferenceInt("farmId", farmId);
                farmName = currentFarm.name;
                maxVersion = farmVersion = currentFarm.version;
                this.setTitle(farmName + " (" + user + ")");
                state = 1;
                firstFarm = false;
                newFarm = false;
                bFarmSaved = true;
                plotMatrix = new oPlotMatrix();
                plotMatrix.createMatrix(displayWidth, displayHeight);
                createFarm();
                canvasView.invalidate();
            }
        } else {
            prefs.deletePreference("farmId");
            firstFarm = true;
            createNewFarm();
        }
    }

    public void goToLogin() {
        prefs.savePreference("user", "");
        prefs.deletePreference("farmId");
        final Context context = this;
        Intent i = new Intent(context, login.class);
        startActivity(i);
        finish();
    }

    public void goToFarmChooser() {
        final Context context = this;
        Intent i = new Intent(context, farmChooser.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        startActivity(i);
        finish();
    }

    public String getDefaultFarmName() {
        String ret = "";

        if (firstFarm) {
            ret = getString(R.string.defaultFarmNamePrefix) + " 1";
        } else {
            int n = 1;
            ret = getString(R.string.defaultFarmNamePrefix) + " " + Integer.toString(n);
            do {
                if (!currentFarm.farmNameExists(userId, farmId, ret)) {
                    break;
                } else {
                    ret = getString(R.string.defaultFarmNamePrefix) + " " + Integer.toString(n);
                    n++;
                }
            } while (true);
        }
        return ret;
    }

    public void createNewFarm() {
        plotMatrix = new oPlotMatrix();
        plotMatrix.createMatrix(displayWidth, displayHeight);
        plotMatrix.addPlot(iconResize.getWidth(), iconResize.getHeight(), iconContents.getWidth(), iconContents.getHeight(), iconActions.getWidth(), iconActions.getHeight());
        state = 0;
        bFarmSaved = false;
        canvasView.invalidate();
        newFarm = true;
        farmId = -1;
        farmName = getDefaultFarmName();
        defineFarmNameAcres(true, false);
    }

    public void cancelNewFarm() {
        AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
        logoutDialog.setMessage(R.string.cancelNewFarmMessage);
        logoutDialog.setNegativeButton(R.string.noButtonText, null);
        logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doCancelNewFarm();
            }
        });
        logoutDialog.create();
        logoutDialog.show();
    }

    public void cancelEditedFarm() {
        if (farmHasBeenEdited()) {
            AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
            logoutDialog.setMessage(R.string.cancelEditedFarmMessage);
            logoutDialog.setNegativeButton(R.string.noButtonText, null);
            logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doCancelEditedFarm();
                }
            });
            logoutDialog.create();
            logoutDialog.show();
        } else {
            doCancelEditedFarm();
        }
    }

    public void doCancelEditedFarm() {
        currentFarm = currentFarm.getLatestActiveVersion(userId, farmId, this);
        farmName = currentFarm.name;
        farmSize = currentFarm.size;
        plotMatrix = new oPlotMatrix();
        plotMatrix.createMatrix(displayWidth, displayHeight);
        createFarm();
        state = 1;
        bFarmSaved = true;
        this.setTitle(farmName + " (" + user + ")");
        canvasView.invalidate();
    }

    public void doCancelNewFarm() {
        if (prefs.preferenceExists("farmId")) {
            farmId = prefs.getPreferenceInt("farmId");
            if (farmId >= 0) {
                plotMatrix = new oPlotMatrix();
                plotMatrix.createMatrix(displayWidth, displayHeight);
                currentFarm = currentFarm.getLatestActiveVersion(userId, farmId, this);
                plotMatrix.fromString(this, currentFarm.plotMatrix, ";", iconResize.getWidth(), iconResize.getHeight(), iconContents.getWidth(), iconContents.getHeight(), iconActions.getWidth(), iconActions.getHeight());
                state = 1;
                this.setTitle(farmName + " (" + user + ")");
                canvasView.invalidate();
            } else {
                goToFarmChooser();
            }
        } else {
            goToFarmChooser();
        }
    }

    public void startEditFarm() {
        state = 2;
        newFarm = false;
        bSaveEditedFarmAsNew = currentFarm.hasRecords();
        plotMatrix = new oPlotMatrix();
        plotMatrix.createMatrix(displayWidth, displayHeight);
        createFarm();
        bFarmSaved = false;
        canvasView.invalidate();
    }

    public void goToLatestFarm() {
        currentFarm = currentFarm.getLatestActiveVersion(userId, farmId, this);
        farmName = currentFarm.name;
        farmSize = currentFarm.size;
        farmVersion = currentFarm.version;
        plotMatrix = new oPlotMatrix();
        plotMatrix.createMatrix(displayWidth, displayHeight);
        createFarm();
        this.setTitle(farmName + " (" + user + ")");
        canvasView.invalidate();
        invalidateOptionsMenu();
    }

    public void addPlot() {
        if (!plotMatrix.addPlot(iconResize.getWidth(), iconResize.getHeight(), iconContents.getWidth(), iconContents.getHeight(), iconActions.getWidth(), iconActions.getHeight())) {
            Toast.makeText(this, R.string.noSpaceForNewPlotMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteSelectedPlot() {
        if (plotMatrix.currentPlot != null) {
            AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
            logoutDialog.setMessage(R.string.deletePlotConfirmMessage);
            logoutDialog.setNegativeButton(R.string.noButtonText, null);
            logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (!plotMatrix.deletePlot()) {
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

    public void definePlotContents() {

        tempPlot = new oPlot();
        copyContentsToTempPlot();

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_define_plot_contents);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                plotMatrix.currentPlot.state = 1;
                dialog.dismiss();
                canvasView.invalidate();
            }
        });

        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.okButton:
                        EditText plotSize = (EditText) dialog.findViewById(R.id.plotSize);
                        if(verifyPlotSize(plotSize.getText().toString())) {
                            plotMatrix.currentPlot.state = 1;
                            plotMatrix.currentPlot.size = Float.valueOf(plotSize.getText().toString());
                            copyContentsFromTempPlot();
                            dialog.dismiss();
                            canvasView.invalidate();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        EditText plotSize = (EditText) dialog.findViewById(R.id.plotSize);
        if(plotMatrix.currentPlot.size>0){
            plotSize.setText(String.valueOf(plotMatrix.currentPlot.size));
        } else {
            plotSize.setText(String.valueOf(calculatePlotSize()));
        }

        Button crops = (Button) dialog.findViewById(R.id.cropButton);
        crops.setOnClickListener(new View.OnClickListener() {
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

        Button pestControl = (Button) dialog.findViewById(R.id.pestControlButton);
        pestControl.setOnClickListener(new View.OnClickListener() {
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

        Button soilManagement = (Button) dialog.findViewById(R.id.soilManagementButton);
        soilManagement.setOnClickListener(new View.OnClickListener() {
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

    BigDecimal calculatePlotSize(){
        float fs = (currentFarm.size>0) ? currentFarm.size : farmSize;
        float pw = Math.round(plotMatrix.currentPlot.w/(displayWidth/4));
        float ph = Math.round(plotMatrix.currentPlot.h/(displayHeight/4));
        float pa = pw*ph;
        float proportion = pa/16;
        float size = fs*proportion;
        BigDecimal bd = new BigDecimal(Float.toString(size));
        BigDecimal ret = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return ret;
    }

    public boolean verifyPlotSize(String size){
        boolean ret=true;
        if(size.isEmpty()){
            Toast.makeText(this, R.string.plotSizeCannotBeEmpty, Toast.LENGTH_SHORT).show();
            ret=false;
        } else {
            float plotSize = Float.valueOf(size);
            if(plotSize==0f){
                Toast.makeText(this, R.string.plotSizeCannotBeZero, Toast.LENGTH_SHORT).show();
                ret=false;
            }
        }
        return ret;
    }

    void copyContentsToTempPlot() {
        Iterator<oCrop> iteratorCrops = plotMatrix.currentPlot.crops.iterator();
        while (iteratorCrops.hasNext()) {
            oCrop c = iteratorCrops.next();
            tempPlot.crops.add(c);
        }
        Iterator<oTreatmentIngredient> iteratorPC = plotMatrix.currentPlot.pestControlIngredients.iterator();
        while (iteratorPC.hasNext()) {
            oTreatmentIngredient pc = iteratorPC.next();
            tempPlot.pestControlIngredients.add(pc);
        }
        Iterator<oTreatmentIngredient> iteratorSM = plotMatrix.currentPlot.soilManagementIngredients.iterator();
        while (iteratorSM.hasNext()) {
            oTreatmentIngredient sm = iteratorSM.next();
            tempPlot.soilManagementIngredients.add(sm);
        }
    }

    void copyContentsFromTempPlot() {
        plotMatrix.currentPlot.crops.clear();
        Iterator<oCrop> iteratorCrops = tempPlot.crops.iterator();
        while (iteratorCrops.hasNext()) {
            oCrop c = iteratorCrops.next();
            plotMatrix.currentPlot.crops.add(c);
        }
        plotMatrix.currentPlot.pestControlIngredients.clear();
        Iterator<oTreatmentIngredient> iteratorPC = tempPlot.pestControlIngredients.iterator();
        while (iteratorPC.hasNext()) {
            oTreatmentIngredient pc = iteratorPC.next();
            plotMatrix.currentPlot.pestControlIngredients.add(pc);
        }
        plotMatrix.currentPlot.soilManagementIngredients.clear();
        Iterator<oTreatmentIngredient> iteratorSM = tempPlot.soilManagementIngredients.iterator();
        while (iteratorSM.hasNext()) {
            oTreatmentIngredient sm = iteratorSM.next();
            plotMatrix.currentPlot.soilManagementIngredients.add(sm);
        }
    }

    public void showCropSelector() {
        boolean[] checkedCrops = new boolean[cropNamesArray.length];
        for (int i = 0; i < cropNamesArray.length; i++) {
            Iterator<oCrop> iterator = tempPlot.crops.iterator();
            while (iterator.hasNext()) {
                oCrop pc = iterator.next();
                if (pc.name.equals(cropNamesArray[i])) {
                    checkedCrops[i] = true;
                    break;
                }
            }
        }

        DialogInterface.OnMultiChoiceClickListener cropsDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    tempPlot.crops.add(cropList.get(which));
                } else {
                    oCrop removeCrop = cropList.get(which);
                    Iterator<oCrop> iterator = tempPlot.crops.iterator();
                    int index = 0;
                    while (iterator.hasNext()) {
                        oCrop c = iterator.next();
                        if (c.id == removeCrop.id) {
                            break;
                        }
                        index++;
                    }
                    tempPlot.crops.remove(index);
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.selectCropsTitle);
        builder.setMultiChoiceItems(cropNamesArray, checkedCrops, cropsDialogListener);
        builder.setPositiveButton(R.string.okButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                plotMatrix.currentPlot.state = 1;
                canvasView.invalidate();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void showPestControlIngredientSelector() {
        boolean[] checkedIngredients = new boolean[pestControlNamesArray.length];
        for (int i = 0; i < pestControlNamesArray.length; i++) {
            Iterator<oTreatmentIngredient> iterator = tempPlot.pestControlIngredients.iterator();
            while (iterator.hasNext()) {
                oTreatmentIngredient pt = iterator.next();
                if (pt.name.equals(pestControlNamesArray[i])) {
                    checkedIngredients[i] = true;
                    break;
                }
            }
        }

        DialogInterface.OnMultiChoiceClickListener ingredientsDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    tempPlot.pestControlIngredients.add(pestControlList.get(which));
                } else {
                    oTreatmentIngredient removeIngredient = pestControlList.get(which);
                    Iterator<oTreatmentIngredient> iterator = tempPlot.pestControlIngredients.iterator();
                    int index = 0;
                    while (iterator.hasNext()) {
                        oTreatmentIngredient tp = iterator.next();
                        if (tp.id == removeIngredient.id) {
                            break;
                        }
                        index++;
                    }
                    tempPlot.pestControlIngredients.remove(index);
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.selectPestControlIngredientsTitle);
        builder.setMultiChoiceItems(pestControlNamesArray, checkedIngredients, ingredientsDialogListener);
        builder.setPositiveButton(R.string.okButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                plotMatrix.currentPlot.state = 1;
                canvasView.invalidate();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void showSoilManagementIngredientSelector() {
        boolean[] checkedIngredients = new boolean[soilManagementNamesArray.length];
        for (int i = 0; i < soilManagementNamesArray.length; i++) {
            Iterator<oTreatmentIngredient> iterator = tempPlot.soilManagementIngredients.iterator();
            while (iterator.hasNext()) {
                oTreatmentIngredient pt = iterator.next();
                if (pt.name.equals(soilManagementNamesArray[i])) {
                    checkedIngredients[i] = true;
                    break;
                }
            }
        }

        DialogInterface.OnMultiChoiceClickListener ingredientsDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    tempPlot.soilManagementIngredients.add(soilManagementList.get(which));
                } else {
                    oTreatmentIngredient removeIngredient = soilManagementList.get(which);
                    Iterator<oTreatmentIngredient> iterator = tempPlot.soilManagementIngredients.iterator();
                    int index = 0;
                    while (iterator.hasNext()) {
                        oTreatmentIngredient tp = iterator.next();
                        if (tp.id == removeIngredient.id) {
                            break;
                        }
                        index++;
                    }
                    tempPlot.soilManagementIngredients.remove(index);
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.selectSoilManagementIngredientsTitle);
        builder.setMultiChoiceItems(soilManagementNamesArray, checkedIngredients, ingredientsDialogListener);
        builder.setPositiveButton(R.string.okButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                plotMatrix.currentPlot.state = 1;
                canvasView.invalidate();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void defineFarmNameAcres(boolean cancellable, final boolean isSaving) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_define_new_farm);
        dialog.setCanceledOnTouchOutside(cancellable);
        dialog.setCancelable(cancellable);

        EditText et = (EditText) dialog.findViewById(R.id.newFarm);
        String defaultFarmName = farmName;

        et.setText(defaultFarmName);

        EditText fSize = (EditText) dialog.findViewById(R.id.acres);
        if (farmSize > 0) {
            if(farmSize<getPlotSum()){
                fSize.setText(Float.toString(getPlotSum()));
            } else {
                fSize.setText(Float.toString(farmSize));
            }
        } else {
            fSize.setText(Float.toString(1f));
        }

        Button button = (Button) dialog.findViewById(R.id.okButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = (EditText) dialog.findViewById(R.id.newFarm);
                String fName = et.getText().toString();
                if (fName.isEmpty()) {
                    Toast.makeText(view.getContext(), R.string.farmNameCannotBeEmptyMessage, Toast.LENGTH_SHORT).show();
                } else {
                    if (currentFarm.farmNameExists(userId, farmId, fName)) {
                        Toast.makeText(view.getContext(), R.string.farmNameRepeated, Toast.LENGTH_SHORT).show();
                    } else {
                        EditText fSize = (EditText) dialog.findViewById(R.id.acres);
                        float farmSize = Float.parseFloat(fSize.getText().toString());
                        if (farmSize <= 0) {
                            Toast.makeText(view.getContext(), R.string.farmSizeMustBeAboveZero, Toast.LENGTH_SHORT).show();
                        } else {
                            float sum = getPlotSum();
                            if(sum>farmSize){
                                Toast.makeText(view.getContext(), R.string.sizeOfPlotsGreaterThanFarmSize, Toast.LENGTH_SHORT).show();
                                fSize.setText(String.valueOf(sum));
                            } else {
                                updateFarmData(fName, farmSize);
                                dialog.dismiss();
                                if (isSaving) {
                                    saveFarm();
                                }
                            }
                        }
                    }
                }
            }
        });

        dialog.show();
    }

    public float getPlotSum() {
        Iterator<oPlot> iterator = plotMatrix.plots.iterator();
        float sum=0f;
        while(iterator.hasNext()){
            oPlot p = iterator.next();
            sum+=p.size;
        }
        return sum;
    }

    public void farmHistory(boolean bNext, boolean bPrev) {
        dateHelper dH = new dateHelper();
        boolean bChange = false;
        plotMatrix.bGoPrev = plotMatrix.bGoNext = false;
        if (bNext && farmVersion < maxVersion) {
            farmVersion++;
            bChange = true;
            arrowShowing = 1;
        } else if (bPrev && farmVersion > 0) {
            farmVersion--;
            bChange = true;
            arrowShowing = 0;
        }
        if (bChange) {
            historyHandler.sendEmptyMessageDelayed(-1, 800);
            currentFarm = currentFarm.getVersion(userId, farmId, farmVersion, this);
            String date = dH.dateToString(currentFarm.dateCreated);
            farmName = currentFarm.name;
            farmSize = currentFarm.size;
            plotMatrix = new oPlotMatrix();
            plotMatrix.createMatrix(displayWidth, displayHeight);
            createFarm();
            if (farmVersion < maxVersion) {
                this.setTitle(farmName + ": " + date + " (" + user + ")");
            } else {
                this.setTitle(farmName + " (" + user + ")");
            }
            invalidateOptionsMenu();
        }
    }

    public void goToPlotRecords() {
        dateHelper dH = new dateHelper();
        final Context context = this;
        Intent i = new Intent(context, records.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("farmSize",farmSize);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion", currentFarm.version);
        i.putExtra("maxVersion", maxVersion);
        i.putExtra("plot", plotMatrix.currentPlot.id);
        i.putExtra("farmDate", dH.dateToString(currentFarm.dateCreated));
        i.putExtra("cropNames", plotMatrix.currentPlot.getCropNames(this));
        i.putExtra("pestControlNames", plotMatrix.currentPlot.getPestControlNames(this));
        i.putExtra("soilManagementNames", plotMatrix.currentPlot.getSoilManagementNames(this));
        i.putExtra("plotSize", plotMatrix.currentPlot.size);
        i.putExtra("displayWidth", displayWidth);
        i.putExtra("displayHeight", displayHeight);
        startActivity(i);
        finish();
    }

    public void goToFarmRecords() {
        dateHelper dH = new dateHelper();
        final Context context = this;
        Intent i = new Intent(context, records.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("farmSize",farmSize);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion", currentFarm.version);
        i.putExtra("maxVersion", maxVersion);
        i.putExtra("plot", -1);
        i.putExtra("farmDate", dH.dateToString(currentFarm.dateCreated));
        i.putExtra("cropNames", plotMatrix.currentPlot.getCropNames(this));
        i.putExtra("pestControlNames", plotMatrix.currentPlot.getPestControlNames(this));
        i.putExtra("soilManagementNames", plotMatrix.currentPlot.getSoilManagementNames(this));
        i.putExtra("plotSize", plotMatrix.currentPlot.size);
        i.putExtra("displayWidth", displayWidth);
        i.putExtra("displayHeight", displayHeight);
        startActivity(i);
        finish();
    }

    public void goToFarmBalance() {
        dateHelper dH = new dateHelper();
        final Context context = this;
        Intent i = new Intent(context, balance.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("farmSize",farmSize);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion", currentFarm.version);
        i.putExtra("maxVersion", maxVersion);
        i.putExtra("plot", -1);
        i.putExtra("farmDate", dH.dateToString(currentFarm.dateCreated));
        i.putExtra("cropNames", plotMatrix.currentPlot.getCropNames(this));
        i.putExtra("pestControlNames", plotMatrix.currentPlot.getPestControlNames(this));
        i.putExtra("soilManagementNames", plotMatrix.currentPlot.getSoilManagementNames(this));
        i.putExtra("plotSize", plotMatrix.currentPlot.size);
        i.putExtra("displayWidth", displayWidth);
        i.putExtra("displayHeight", displayHeight);
        i.putExtra("from", 0);
        startActivity(i);
        finish();
    }

    public void updateFarmData(String fName, float fSize) {
        fName = fName.replaceAll("[^A-Za-z0-9 ]", "");
        fName = fName.trim();
        this.setTitle(fName + " (" + user + ")");
        farmName = fName;
        farmSize = fSize;
    }

    public boolean farmHasBeenEdited() {
        boolean ret = true;
        oFarm pastFarm = new oFarm(this);
        pastFarm = pastFarm.getLatestActiveVersion(userId, farmId, this);
        String newMatrix = plotMatrix.toString();
        if (pastFarm.name.equals(farmName) && pastFarm.size == farmSize && pastFarm.plotMatrix.equals(newMatrix)) {
            ret = false;
        }
        return ret;
    }

    public void saveFarm() {

        boolean bChangesMade;
        dateHelper dH = new dateHelper();
        sMatrix = plotMatrix.toString();

        if (farmName.isEmpty()) {
            farmName = getDefaultFarmName();
            defineFarmNameAcres(false, true);
        } else {
            float sum=getPlotSum();
            farmSize=(farmSize<sum) ? sum : farmSize;
            Date farmDate = new Date();
            farmDateString = dH.dateToString(farmDate);

            if (farmId == -1) {
                farmId = prefs.getPreferenceInt("farmIdNumber");
                if (state != 2) {
                    prefs.savePreferenceInt("farmIdNumber", farmId + 1);
                }
            }

            bChangesMade = (state == 2) ? farmHasBeenEdited() : true;

            if (bChangesMade) {

                maxVersion = farmVersion = (state == 0) ? 0 : (bSaveEditedFarmAsNew) ? currentFarm.version + 1 : currentFarm.version;

                String saveString = user + ";" + userPass + ";" + farmName + ";" + String.valueOf(farmSize) + ";" + farmDateString + ";" + String.valueOf(farmId) + ";" + String.valueOf(farmVersion) + ";" + sMatrix;
                httpConnection http = new httpConnection(this, this);
                if (http.isOnline()) {

                    CharSequence dialogTitle;

                    if (state == 0) {
                        dialogTitle = getString(R.string.createNewFarmLabel);
                    } else {
                        dialogTitle = getString(R.string.updatingFarmLabel);
                    }

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
                    doCreateUpdateFarm(saveString);
                } else {
                    if (state == 0) {
                        currentFarm.addNewFarm(farmId, userId, farmName, farmSize, farmDate, sMatrix, farmVersion, 0);
                        Toast.makeText(this, R.string.farmSavedMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        if (bSaveEditedFarmAsNew) {
                            currentFarm.addNewFarm(farmId, userId, farmName, farmSize, farmDate, sMatrix, farmVersion, 0);
                        } else {
                            currentFarm.updateFarm(farmId, userId, farmName, farmSize, farmDate, sMatrix, farmVersion, 0);
                        }
                        Toast.makeText(this, R.string.farmEditedMessage, Toast.LENGTH_SHORT).show();
                    }
                    prefs.savePreferenceInt("farmId", farmId);
                    state = 1;
                    canvasView.invalidate();
                    firstFarm = false;
                    newFarm = false;
                    bFarmSaved = true;
                    this.setTitle(farmName + " (" + user + ")");
                    currentFarm = currentFarm.getLatestActiveVersion(userId, farmId, this);
                }
            } else {
                state = 1;
                canvasView.invalidate();
                firstFarm = false;
                newFarm = false;
                bFarmSaved = true;
                this.setTitle(farmName + " (" + user + ")");
                currentFarm = currentFarm.getLatestActiveVersion(userId, farmId, this);
            }
        }
    }

    public void doCreateUpdateFarm(String s) {
        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            if (!bConnecting) {
                bConnecting = true;
                connectionTask = 0;
                if (bSaveEditedFarmAsNew || state == 0) {
                    http.execute(server + "/mobile/create_new_farm.php?farm=" + s.replaceAll(" ", "_"), "");
                } else {
                    http.execute(server + "/mobile/update_farm.php?farm=" + s.replaceAll(" ", "_"), "");
                }
            }
        }
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

    @Override
    public void processFinish(String output) {
        switch (connectionTask) {
            case 0:
                bConnecting = false;
                createFarmDialog.dismiss();
                Date farmDate = new Date();
                int status = (!output.equals("ko") && !output.isEmpty()) ? 2 : 0;
                if (state == 0) {
                    currentFarm.addNewFarm(farmId, userId, farmName, farmSize, farmDate, sMatrix, farmVersion, status);
                    Toast.makeText(this, R.string.farmSavedMessage, Toast.LENGTH_SHORT).show();
                } else {
                    if (bSaveEditedFarmAsNew) {
                        currentFarm.addNewFarm(farmId, userId, farmName, farmSize, farmDate, sMatrix, farmVersion, status);
                    } else {
                        currentFarm.updateFarm(farmId, userId, farmName, farmSize, farmDate, sMatrix, farmVersion, status);
                    }
                    Toast.makeText(this, R.string.farmEditedMessage, Toast.LENGTH_SHORT).show();
                }
                prefs.savePreferenceInt("farmId", farmId);
                currentFarm = currentFarm.getLatestActiveVersion(userId, farmId, this);

                state = 1;
                firstFarm = false;
                newFarm = false;
                bFarmSaved = true;
                canvasView.invalidate();
                this.setTitle(farmName + " (" + user + ")");
                break;

            case 1:
                bConnecting = false;
                deleteFarmDialog.dismiss();

                if (output.equals("ok")) {
                    executeDeleteFarm();
                } else {
                    markDeletedFarm();
                }
                afterFarmDeletion();
                break;
        }
    }

    class SketchSheetView extends View {

        Context context;

        public SketchSheetView(Context c, int w, int h) {

            super(c);
            context = c;
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(bitmap);
            this.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_MOVE || event.getActionMasked() == MotionEvent.ACTION_UP) {
                invalidate();
                boolean b = plotMatrix.passEvent(event, state);
                if (plotMatrix.currentPlot != null) {
                    if (plotMatrix.currentPlot.state == 4 && (state == 0 || state == 2)) {
                        definePlotContents();
                    } else if (plotMatrix.currentPlot.state == 5 && state == 1) {
                        goToPlotRecords();
                    }
                }
                if (state == 1 && (plotMatrix.bGoNext || plotMatrix.bGoPrev)) {
                    farmHistory(plotMatrix.bGoNext, plotMatrix.bGoPrev);
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
                fillColor = getFillColor(plot, plot == plotMatrix.currentPlot);
                borderColor = (plot == plotMatrix.currentPlot) ? ContextCompat.getColor(context, R.color.colorDraw) : ContextCompat.getColor(context, R.color.colorDrawFaded);
                if (state == 0 || state == 2) {
                    iMove = (plot == plotMatrix.currentPlot) ? (plotMatrix.currentPlot.state == 2) ? iconMoveActive : iconMove : iconMoveFaded;
                    iResize = (plot == plotMatrix.currentPlot) ? (plotMatrix.currentPlot.state == 3) ? iconResizeActive : iconResize : iconResizeFaded;
                    iContents = (plot == plotMatrix.currentPlot) ? (plotMatrix.currentPlot.state == 4) ? iconContentsActive : iconContents : iconContentsFaded;
                    drawPlot(canvas, plot, borderColor, fillColor, iMove, iResize, iContents);
                } else if (state == 1) {
                    iActions = (plot == plotMatrix.currentPlot) ? (plotMatrix.currentPlot.state == 5) ? iconActionsActive : iconActions : iconActionsFaded;
                    drawPlot(canvas, plot, borderColor, fillColor, iActions);
                }
            }

            if (plotMatrix.ghostPlot != null && (state == 0 || state == 2)) {
                drawGhostRectangle(canvas, plotMatrix.ghostPlot, ContextCompat.getColor(context, R.color.colorDrawGhostRectangle));
            }

            if (arrowShowing == 0) {
                canvas.drawBitmap(historyLeft, (displayWidth / 4) - (historyLeft.getWidth() / 2), (displayHeight / 2) - (historyLeft.getHeight() / 2), paint);
            } else if (arrowShowing == 1) {
                canvas.drawBitmap(historyRight, ((displayWidth / 4) * 3) - (historyRight.getWidth() / 2), (displayHeight / 2) - (historyRight.getHeight() / 2), paint);
            }
        }

        public int getFillColor(oPlot p, boolean strong) {
            int ret;
            if (p.pestControlIngredients.size() > 0 && p.soilManagementIngredients.size() > 0) {
                ret = (strong) ? ContextCompat.getColor(context, R.color.colorFillSoilManagementAndPestControl) : ContextCompat.getColor(context, R.color.colorFillSoilManagementAndPestControlFaded);
            } else if (p.pestControlIngredients.size() > 0 && p.soilManagementIngredients.size() == 0) {
                ret = (strong) ? ContextCompat.getColor(context, R.color.colorFillPestControl) : ContextCompat.getColor(context, R.color.colorFillPestControlFaded);
            } else if (p.pestControlIngredients.size() == 0 && p.soilManagementIngredients.size() > 0) {
                ret = (strong) ? ContextCompat.getColor(context, R.color.colorFillSoilManagement) : ContextCompat.getColor(context, R.color.colorFillSoilManagementFaded);
            } else {
                ret = (strong) ? ContextCompat.getColor(context, R.color.colorFillDefault) : ContextCompat.getColor(context, R.color.colorFillFaded);
            }
            return ret;
        }

        private void drawPlot(Canvas canvas, oPlot p, int border, int fill, Bitmap iMove, Bitmap iResize, Bitmap iContents) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(fill);
            canvas.drawRect(p.x, p.y, p.x + p.w, p.y + p.h, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(border);
            canvas.drawRect(p.x, p.y, p.x + p.w, p.y + p.h, paint);
            canvas.drawBitmap(iResize, p.iResizeX, p.iResizeY, paint);
            canvas.drawBitmap(iContents, p.iContentsX, p.iContentsY, paint);
            int added = (state == 1) ? 15 : 0;
            float yOffset = (((p.h / (displayHeight / 4)) - 1) * 30) + (baseTextSize - p.crops.size()) + added;
            drawPlotCropLabels(canvas, p, p.iContentsY + p.iContentsH + yOffset);
        }

        private void drawPlot(Canvas canvas, oPlot p, int border, int fill, Bitmap iActions) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(fill);
            canvas.drawRect(p.x, p.y, p.x + p.w, p.y + p.h, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(border);
            canvas.drawRect(p.x, p.y, p.x + p.w, p.y + p.h, paint);
            canvas.drawBitmap(iActions, p.iActionsX, p.iActionsY, paint);
            int added = (state == 1) ? 15 : 0;
            float yOffset = (((p.h / (displayHeight / 4)) - 1) * 30) + (baseTextSize - p.crops.size()) + added;
            drawPlotCropLabels(canvas, p, p.iActionsY + p.iActionsH + yOffset);
        }

        private void drawPlotCropLabels(Canvas canvas, oPlot p, float txtY) {
            Rect txtBounds = new Rect();
            float txtX;

            float fontSize = ((((p.w / (displayWidth / 4) + (p.h / (displayHeight / 4))) - 2) * 10) / 14) + baseTextSize;

            textPaint.setTextSize(fontSize);

            Iterator<oCrop> iterator = p.crops.iterator();
            while (iterator.hasNext()) {
                oCrop c = iterator.next();
                textPaint.getTextBounds(c.name, 0, c.name.length(), txtBounds);

                int n = c.name.length();
                while (txtBounds.width() > (p.w)) {
                    n--;
                    textPaint.getTextBounds(c.name, 0, n, txtBounds);
                }

                txtX = ((p.w - txtBounds.width()) / 2) + p.x;
                canvas.drawText(c.name.substring(0, n), (int) txtX, (int) txtY, textPaint);

                txtY += (((p.h / (displayHeight / 4)) - 1) * 3) + baseTextSize;

            }
        }

        private void drawGhostRectangle(Canvas canvas, oPlot gR, int border) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(border);
            canvas.drawRect(gR.x, gR.y, gR.x + gR.w + 1, gR.y + gR.h + 1, paint);
        }
    }
}
