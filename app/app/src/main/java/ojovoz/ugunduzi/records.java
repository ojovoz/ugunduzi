package ojovoz.ugunduzi;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Created by Eugenio on 20/08/2018.
 */
public class records extends AppCompatActivity implements httpConnection.AsyncResponse {

    public oLog newItem;

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

    public oPlot currentPlot;

    ArrayList<oDataItem> dataItemsList;
    public CharSequence dataItemsNamesArray[];

    public Date dataItemDate;

    public Button bDate;
    public Button bCrop;
    public Button bTreatment;
    public Button bUnits;
    public Button bSave;

    public EditText etValue;
    public EditText etComments;
    public EditText etQuantity;

    public TableLayout tlQuantityUnits;

    public ArrayList<oUnit> unitsList;
    public Context context;
    public boolean bCancellingData;

    oRecyclerViewAdapter recyclerViewAdapter;
    public ArrayList<oLog> logList;

    boolean soundPlaying;
    MediaPlayer soundPlayer;

    public oLog editingItem;
    public boolean itemChanges;

    public int nSelected;

    public preferenceManager prefs;
    public String server;

    int connectionTask;
    boolean bConnecting;
    ArrayList<oFarm> farmsPendingDelete;
    ArrayList<oFarm> farmsPendingSave;
    ProgressDialog deletingFarmDialog;
    ProgressDialog savingFarmsDialog;
    ProgressDialog downloadingParamsDialog;
    private ProgressDialog sendingDataDialog;
    private ProgressDialog sendingMultimediaDialog;
    private Thread uploadMultimedia;
    private ArrayList<oLog> multimediaSentItems;
    private int[] multimediaCleanUpList;

    String ugunduziEmail = "";
    String ugunduziPass = "";
    String dataSubject = "";
    String multimediaSubject = "";
    String smtpServer = "";
    String smtpPort = "";

    private Context recordsContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        context = this;
        nSelected = 0;
        bConnecting = false;

        prefs = new preferenceManager(this);
        server = prefs.getPreference("server");

        recordsContext = this;

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

        setTitle(getTitle()+ ": " + farmName);

        TextView tt = (TextView) findViewById(R.id.plotLabel);
        String title = "";

        if(plot>=0) {
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
            title = farmName + " (" + String.valueOf(farmSize) + " " + getString(R.string.acresWord) + ")";
            if(farmVersion<maxVersion){
                title += ": " + farmDate;
            }
            tt.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary));
            tt.setTextColor(ContextCompat.getColor(this,R.color.colorWhite));

            tt.setTextSize(18);
        }

        tt.setText(title);

        dataItemDate = new Date();
        unitsList = new ArrayList<>();
        if(plot>=0) {
            currentPlot = getCurrentPlot(plot);
        } else {
            currentPlot = null;
        }
        getDataItemsList();

        fillRecyclerView();

    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        menu.clear();
        if (nSelected > 0) {
            menu.add(0, 0, 0, R.string.opDeleteSelectedItems);
        }
        if(logList.size()>0) {
            menu.add(1, 1, 1, R.string.opBalance);
        }
        menu.add(2, 2, 2, R.string.opUploadRecords);
        menu.add(3, 3, 3, R.string.opGoToWeb);
        menu.add(4, 4, 4, R.string.opGoBackToFarm);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                tryDeleteSelectedItems();
                break;
            case 1:
                goToBalance();
                break;
            case 2:
                uploadRecords();
                break;
            case 3:
                goToWebPage();
                break;
            case 4:
                goBack();
        }
        return super.onOptionsItemSelected(item);
    }

    public void uploadRecords(){
        if (!bConnecting) {
            httpConnection http = new httpConnection(this, this);
            if (http.isOnline()) {
                bConnecting = true;
                oFarm f = new oFarm(this);
                farmsPendingDelete = f.getFarmsPendingDelete(userId);
                if (farmsPendingDelete.size() > 0) {
                    connectionTask = 0;
                    doDeleteFarms();
                } else {
                    farmsPendingSave = f.getFarmsPendingSave(userId);
                    if (farmsPendingSave.size() > 0) {
                        connectionTask = 1;
                        doSaveFarms();
                    } else {
                        sendMessages();
                    }
                }
            } else {
                Toast.makeText(this, R.string.pleaseConnectMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void doDeleteFarms(){
        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            CharSequence dialogTitle = getString(R.string.deletingFarmsLabel);
            deletingFarmDialog = new ProgressDialog(this);
            deletingFarmDialog.setCancelable(true);
            deletingFarmDialog.setCanceledOnTouchOutside(false);
            deletingFarmDialog.setMessage(dialogTitle);
            deletingFarmDialog.setIndeterminate(true);
            deletingFarmDialog.show();
            deletingFarmDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface d) {
                    bConnecting = false;
                    deletingFarmDialog.dismiss();
                }
            });
            String deleteList="";
            Iterator<oFarm> iterator = farmsPendingDelete.iterator();
            while(iterator.hasNext()){
                oFarm f = iterator.next();
                deleteList = (deleteList.isEmpty()) ? Integer.toString(f.id) : deleteList + ";" + Integer.toString(f.id);
            }

            http.execute(server + "/mobile/delete_farm.php?user=" + userId + "&farm=" + deleteList, "");
        } else {
            Toast.makeText(this, R.string.pleaseConnectMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void doSaveFarms(){
        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            dateHelper dH = new dateHelper();
            CharSequence dialogTitle = getString(R.string.savingFarmsLabel);
            savingFarmsDialog = new ProgressDialog(this);
            savingFarmsDialog.setCancelable(true);
            savingFarmsDialog.setCanceledOnTouchOutside(false);
            savingFarmsDialog.setMessage(dialogTitle);
            savingFarmsDialog.setIndeterminate(true);
            savingFarmsDialog.show();
            savingFarmsDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface d) {
                    bConnecting = false;
                    savingFarmsDialog.dismiss();
                }
            });
            String saveString="";
            Iterator<oFarm> iterator = farmsPendingSave.iterator();
            while(iterator.hasNext()){
                oFarm f = iterator.next();
                saveString = (saveString.isEmpty()) ? f.name.replaceAll(" ", "_") + ";" + String.valueOf(f.size) + ";" + dH.dateToString(f.dateCreated) +
                        ";" + String.valueOf(f.id) + ";" + String.valueOf(f.version) + ";" + f.plotMatrix.toString() : saveString + "*" + f.name.replaceAll(" ", "_")
                        + ";" + String.valueOf(f.size) + ";" + dH.dateToString(f.dateCreated) +
                        ";" + String.valueOf(f.id) + ";" + String.valueOf(f.version) + ";" + f.plotMatrix.toString();
            }
            http.execute(server + "/mobile/create_new_farms.php?user=" + userId + "&farms=" + saveString, "");
        } else {
            Toast.makeText(this, R.string.pleaseConnectMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void sendMessages(){
        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            if (getEmailParams()) {
                doSendMessages();
            } else {
                CharSequence dialogTitle = getString(R.string.downloadingParametersMessage);
                downloadingParamsDialog = new ProgressDialog(this);
                downloadingParamsDialog.setCancelable(true);
                downloadingParamsDialog.setCanceledOnTouchOutside(false);
                downloadingParamsDialog.setMessage(dialogTitle);
                downloadingParamsDialog.setIndeterminate(true);
                downloadingParamsDialog.show();
                downloadingParamsDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface d) {
                        bConnecting = false;
                        downloadingParamsDialog.dismiss();
                    }
                });
                connectionTask = 2;
                http.execute(server + "/mobile/get_parameters.php?", "");
            }

        } else {
            Toast.makeText(this, R.string.pleaseConnectMessage, Toast.LENGTH_SHORT).show();
            bConnecting=false;
        }
    }

    public void doSendMessages(){
        String b = "";

        ArrayList<oLog> allRecords;
        oLog l = new oLog(this);
        allRecords = l.createLog(farmId,userId,0);

        Iterator<oLog> iterator = allRecords.iterator();
        while (iterator.hasNext()) {
            oLog thisRecord = iterator.next();
            if (thisRecord.dataItem != null) {
                b = (b.isEmpty()) ? thisRecord.toString(";",true) : b + "*" + thisRecord.toString(";",true);
            }
        }

        if (!b.isEmpty()) {
            final String emailBody=b;
            httpConnection http = new httpConnection(this, this);
            if (http.isOnline()) {
                if(!ugunduziEmail.isEmpty() && !ugunduziPass.isEmpty() && !smtpServer.isEmpty() && !smtpPort.isEmpty()){
                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected void onPreExecute() {
                            sendingDataDialog = new ProgressDialog(recordsContext);
                            CharSequence dialogTitle = getString(R.string.sendingRecordsMessage);
                            sendingDataDialog.setMessage(dialogTitle);
                            sendingDataDialog.setCancelable(true);
                            sendingDataDialog.setCanceledOnTouchOutside(false);
                            sendingDataDialog.setIndeterminate(true);
                            sendingDataDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface d) {
                                    bConnecting = false;
                                    sendingDataDialog.dismiss();
                                }
                            });
                            sendingDataDialog.show();
                        }

                        @Override
                        protected Void doInBackground(Void... arg0) {
                            try {
                                Mail m = new Mail(ugunduziEmail, ugunduziPass, smtpServer, smtpPort);
                                String[] toArr = {ugunduziEmail};
                                m.setTo(toArr);
                                m.setFrom(ugunduziEmail);
                                m.setSubject(dataSubject);
                                m.setBody(emailBody);
                                try {
                                    if(m.send()){

                                    } else {
                                        Toast.makeText(recordsContext, R.string.incorrectInternetParamsMessage, Toast.LENGTH_SHORT).show();
                                        if (sendingDataDialog != null) {
                                            sendingDataDialog.dismiss();
                                        }
                                        bConnecting=false;
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(recordsContext, R.string.incorrectInternetParamsMessage, Toast.LENGTH_SHORT).show();
                                    if (sendingDataDialog != null) {
                                        sendingDataDialog.dismiss();
                                    }
                                    bConnecting=false;
                                }
                            } catch (Exception e) {
                                Toast.makeText(recordsContext, R.string.incorrectInternetParamsMessage, Toast.LENGTH_SHORT).show();
                                if (sendingDataDialog != null) {
                                    sendingDataDialog.dismiss();
                                }
                                bConnecting=false;
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            if (sendingDataDialog!=null) {
                                sendingDataDialog.dismiss();
                            }
                            if(bConnecting) {
                                doSendMultimediaMessages();
                            }
                        }
                    };
                    task.execute((Void[])null);
                } else {
                    Toast.makeText(this, R.string.incorrectInternetParamsMessage, Toast.LENGTH_SHORT).show();
                    bConnecting=false;
                }
            } else {
                Toast.makeText(this, R.string.pleaseConnectMessage, Toast.LENGTH_SHORT).show();
                bConnecting=false;
            }
        } else {
            doSendMultimediaMessages();
        }
    }

    public void doSendMultimediaMessages(){

        ArrayList<String> b = new ArrayList<>();
        ArrayList<oLog> multimediaLog;
        final ArrayList<oLog> attachments = new ArrayList<>();

        oLog l = new oLog(this);
        multimediaLog = l.createLog(farmId,userId,1);

        Iterator<oLog> iterator = multimediaLog.iterator();
        while (iterator.hasNext()) {
            oLog thisRecord = iterator.next();
            if (!thisRecord.sent) {
                b.add(thisRecord.toString(";",false));
                attachments.add(thisRecord);
            }
        }

        if(!b.isEmpty()){
            final ArrayList<String> emailBody=b;
            multimediaCleanUpList = new int[attachments.size()];
            multimediaSentItems = new ArrayList<>();
            httpConnection http = new httpConnection(this, this);
            if (http.isOnline()) {
                sendingMultimediaDialog = new ProgressDialog(this);
                sendingMultimediaDialog.setCancelable(true);
                sendingMultimediaDialog.setCanceledOnTouchOutside(false);
                CharSequence dialogTitle = getString(R.string.sendingMultimediaRecordsMessage);
                sendingMultimediaDialog.setMessage(dialogTitle);
                sendingMultimediaDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                sendingMultimediaDialog.setProgress(0);
                int dialogMax=attachments.size();
                sendingMultimediaDialog.setMax(dialogMax);
                sendingMultimediaDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface d) {
                        bConnecting=false;
                        uploadMultimedia.interrupt();
                        sendingMultimediaDialog.dismiss();
                        createLogList();
                        recyclerViewAdapter.list = cardDataFromLog();
                        recyclerViewAdapter.setList(recyclerViewAdapter.list);
                        recyclerViewAdapter.notifyDataSetChanged();
                        nSelected = 0;
                    }
                });
                sendingMultimediaDialog.show();

                uploadMultimedia = new Thread(new Runnable() {
                    public void run() {

                        int n=0;
                        Iterator<oLog> iterator = attachments.iterator();
                        while (iterator.hasNext() && bConnecting) {
                            oLog item = iterator.next();
                            String body = emailBody.get(n);

                            Mail m = new Mail(ugunduziEmail, ugunduziPass, smtpServer, smtpPort);
                            String[] toArr = {ugunduziEmail};
                            m.setTo(toArr);
                            m.setFrom(ugunduziEmail);
                            m.setSubject(multimediaSubject);
                            m.setBody(body);
                            boolean proceed=true;

                            try{
                                File f1 = new File(item.picture);
                                if(f1.exists()){
                                    m.addAttachment(item.picture);
                                } else {
                                    proceed=false;
                                }
                                File f2 = new File(item.sound);
                                if(f2.exists()){
                                    m.addAttachment(item.sound);
                                } else {
                                    proceed=false;
                                }
                            } catch (Exception e){
                                proceed=false;
                            }

                            if(proceed){
                                try{
                                    if(m.send()){
                                        multimediaCleanUpList[n]=-1;
                                        multimediaSentItems.add(item);
                                    }
                                } catch (Exception e){

                                }
                            } else {
                                multimediaCleanUpList[n] = item.line;
                            }
                            progressHandler.sendMessage(progressHandler.obtainMessage());

                            n++;

                        }
                    }
                });
                uploadMultimedia.start();

            } else {
                Toast.makeText(this, R.string.pleaseConnectMessage, Toast.LENGTH_SHORT).show();
                bConnecting=false;
            }
        } else {
            bConnecting=false;
        }
    }

    Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            sendingMultimediaDialog.incrementProgressBy(1);
            if (sendingMultimediaDialog.getProgress() == sendingMultimediaDialog.getMax()) {
                bConnecting=false;
                sendingMultimediaDialog.dismiss();
                uploadMultimedia.interrupt();

                Iterator<oLog> iterator = multimediaSentItems.iterator();
                while (iterator.hasNext()) {
                    oLog item = iterator.next();
                    item.setSent(recordsContext);
                }

                oLog l = new oLog(recordsContext);
                l.deleteLogItems(multimediaCleanUpList);

                createLogList();
                recyclerViewAdapter.list = cardDataFromLog();
                recyclerViewAdapter.setList(recyclerViewAdapter.list);
                recyclerViewAdapter.notifyDataSetChanged();
                nSelected = 0;
                setTitle(getString(R.string.recordsActivity));
            }
        }
    };

    public boolean getEmailParams(){
        boolean ret = false;
        csvFileManager paramList;

        paramList = new csvFileManager("parameters");
        List<String[]> paramCSV = paramList.read(this);
        if (paramCSV != null) {
            Iterator<String[]> iterator = paramCSV.iterator();
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                if (record.length == 6) {
                    ugunduziEmail = record[0];
                    ugunduziPass = record[1];
                    dataSubject = record[2];
                    multimediaSubject = record[3];
                    smtpServer = record[4];
                    smtpPort = record[5];
                    ret = true;
                }
            }
        }
        return ret;
    }

    public void tryDeleteSelectedItems() {
        stopSoundPlayer();
        if (nSelected > 0) {
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
            confirmDialog.setMessage(R.string.deleteItemsConfirmMessage);
            confirmDialog.setNegativeButton(R.string.noButtonText, null);
            confirmDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteSelectedItems();

                }
            });
            confirmDialog.create();
            confirmDialog.show();
        } else {
            Toast.makeText(this, R.string.noItemsSelectedMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteSelectedItems() {
        int[] delete = new int[recyclerViewAdapter.list.size()];
        ArrayList<String> deleteFiles = new ArrayList<>();
        List<oCardData> list = recyclerViewAdapter.list;
        Iterator<oCardData> iterator = list.iterator();
        int n = 0;
        while (iterator.hasNext()) {
            oCardData cd = iterator.next();
            if (cd.isSelected) {
                delete[n] = logList.get(cd.id).line;
                if (!cd.imgFile.isEmpty()) {
                    deleteFiles.add(cd.imgFile);
                }
                if (!cd.sndFile.isEmpty()) {
                    deleteFiles.add(cd.sndFile);
                }
            } else {
                delete[n] = -1;
            }
            n++;
        }
        oLog l = new oLog(this);
        l.deleteLogItems(delete);
        deleteImgSndFiles(deleteFiles);
        createLogList();
        recyclerViewAdapter.list = cardDataFromLog();
        recyclerViewAdapter.setList(recyclerViewAdapter.list);
        recyclerViewAdapter.notifyDataSetChanged();
        nSelected = 0;
        setTitle(getString(R.string.recordsActivity) + ": " + farmName);
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

    public void goToPictureSound(View v) {
        stopSoundPlayer();
        final Context context = this;
        Intent i = new Intent(context, pictureSound.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("farmSize",farmSize);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion", farmVersion);
        i.putExtra("maxVersion", maxVersion);
        i.putExtra("plot", plot);
        i.putExtra("farmDate", farmDate);
        i.putExtra("cropNames", cropNames);
        i.putExtra("pestControlNames", pestControlNames);
        i.putExtra("soilManagementNames", soilManagementNames);
        i.putExtra("plotSize", plotSize);
        i.putExtra("displayWidth", displayWidth);
        i.putExtra("displayHeight", displayHeight);
        startActivity(i);
        finish();
    }

    public void addItem(View v) {
        stopSoundPlayer();
        final View editingView = v;

        itemChanges = true;

        if (editingItem == null) {
            newItem = new oLog();
            newItem.date = new Date();
        } else {
            newItem = editingItem;
            itemChanges = false;
        }

        bCancellingData = false;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_edit_data_item);
        dialog.getWindow().setLayout(displayWidth - 10, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK && newItem.dataItem != null && itemChanges && !bCancellingData) {
                    bCancellingData = true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.dataNotSavedText)
                            .setCancelable(false)
                            .setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dlg, int id) {
                                    dlg.dismiss();
                                    dialog.dismiss();
                                    if (editingItem != null) {
                                        TextView tv = (TextView) editingView;
                                        if(plot>=0) {
                                            if ((int) tv.getTag() % 2 == 0) {
                                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillFaded));
                                            } else {
                                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorWhite));
                                            }
                                        } else if(currentPlot==null){
                                            tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillFaded));
                                        } else {
                                            if (currentPlot.pestControlIngredients.size() > 0 && currentPlot.soilManagementIngredients.size() > 0) {
                                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillSoilManagementAndPestControlFaded));
                                            } else if (currentPlot.pestControlIngredients.size() > 0 && currentPlot.soilManagementIngredients.size() == 0) {
                                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillPestControlFaded));
                                            } else if (currentPlot.pestControlIngredients.size() == 0 && currentPlot.soilManagementIngredients.size() > 0) {
                                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillSoilManagementFaded));
                                            } else {
                                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillFaded));
                                            }
                                        }
                                        editingItem = null;
                                    }
                                }
                            })
                            .setNegativeButton(R.string.noButtonText, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dlg, int id) {
                                    dlg.dismiss();
                                    bCancellingData = false;
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return true;
                } else {
                    if (i == KeyEvent.KEYCODE_BACK && editingItem != null) {
                        TextView tv = (TextView) editingView;
                        if(plot>=0) {
                            if ((int) tv.getTag() % 2 == 0) {
                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillFaded));
                            } else {
                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorWhite));
                            }
                        } else if(currentPlot==null){
                            tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillFaded));
                        } else {
                            if (currentPlot.pestControlIngredients.size() > 0 && currentPlot.soilManagementIngredients.size() > 0) {
                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillSoilManagementAndPestControlFaded));
                            } else if (currentPlot.pestControlIngredients.size() > 0 && currentPlot.soilManagementIngredients.size() == 0) {
                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillPestControlFaded));
                            } else if (currentPlot.pestControlIngredients.size() == 0 && currentPlot.soilManagementIngredients.size() > 0) {
                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillSoilManagementFaded));
                            } else {
                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillFaded));
                            }
                        }
                        editingItem = null;
                    }
                    return false;
                }
            }
        });

        final Button bDataItem = (Button) dialog.findViewById(R.id.dataItemButton);
        bDate = (Button) dialog.findViewById(R.id.dateButton);
        bCrop = (Button) dialog.findViewById(R.id.cropButton);
        bTreatment = (Button) dialog.findViewById(R.id.treatmentButton);
        bUnits = (Button) dialog.findViewById(R.id.dataItemUnits);
        etValue = (EditText) dialog.findViewById(R.id.dataItemValue);
        etQuantity = (EditText) dialog.findViewById(R.id.dataItemQuantity);
        etComments = (EditText) dialog.findViewById(R.id.dataItemComments);
        bSave = (Button) dialog.findViewById(R.id.saveButton);

        tlQuantityUnits = (TableLayout) dialog.findViewById(R.id.quantityUnitsTable);

        if (editingItem != null) {
            bDataItem.setText(editingItem.dataItem.name);
            displayFields(editingItem.dataItem);
            if(currentPlot!=null) {
                if (currentPlot.crops.size() > 1 && editingItem.dataItem.isCropSpecific) {
                    bCrop.setText(editingItem.crop.name);
                }
                if ((currentPlot.pestControlIngredients.size() > 0 || currentPlot.soilManagementIngredients.size() > 0) && editingItem.dataItem.isTreatmentSpecific) {
                    bTreatment.setText(editingItem.treatmentIngredient.name);
                }
            }

            if (editingItem.dataItem.type > 0 && editingItem.dataItem.type < 4) {
                bUnits.setText(editingItem.units.name);
                etQuantity.setText(Float.toString(editingItem.quantity));
            }

            etValue.setText(Float.toString(editingItem.value));
            etComments.setText(editingItem.comments);

            bSave.setVisibility(View.VISIBLE);
            bSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemChanges && !checkFields()) {
                        dialog.dismiss();
                        createLogList();
                        invalidateOptionsMenu();
                        recyclerViewAdapter.list = cardDataFromLog();
                        recyclerViewAdapter.setList(recyclerViewAdapter.list);
                        recyclerViewAdapter.notifyDataSetChanged();
                    } else if (!itemChanges) {
                        dialog.dismiss();
                        if (editingItem != null) {
                            TextView tv = (TextView) editingView;
                            if ((int) tv.getTag() % 2 == 0) {
                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillFaded));
                            } else {
                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorWhite));
                            }
                        }
                    }
                }
            });
        }

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

                            resetFields();

                            newItem.dataItem = d;
                            bDataItem.setText(d.name);

                            displayFields(d);

                            bSave.setVisibility(View.VISIBLE);
                            bSave.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (itemChanges && !checkFields()) {
                                        dialog.dismiss();
                                        createLogList();
                                        invalidateOptionsMenu();
                                        recyclerViewAdapter.list = cardDataFromLog();
                                        recyclerViewAdapter.setList(recyclerViewAdapter.list);
                                        recyclerViewAdapter.notifyDataSetChanged();
                                    } else if (!itemChanges) {
                                        dialog.dismiss();
                                        if (editingItem != null) {
                                            TextView tv = (TextView) editingView;
                                            if ((int) tv.getTag() % 2 == 0) {
                                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillFaded));
                                            } else {
                                                tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorWhite));
                                            }
                                        }
                                    }
                                }
                            });

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

    public void displayFields(oDataItem d) {
        dateHelper dH = new dateHelper();
        if(currentPlot!=null) {
            if (currentPlot.crops.size() > 1 && d.isCropSpecific) {
                bCrop.setVisibility(View.VISIBLE);
                bCrop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        displayCropPicker();
                    }
                });
            } else if (d.isCropSpecific) {
                bCrop.setVisibility(View.VISIBLE);
                bCrop.setText(currentPlot.crops.get(0).name);
                newItem.crop = currentPlot.crops.get(0);
            } else {
                bCrop.setVisibility(View.GONE);
            }
            if ((currentPlot.pestControlIngredients.size() > 0 || currentPlot.soilManagementIngredients.size() > 0) && d.isTreatmentSpecific) {
                bTreatment.setVisibility(View.VISIBLE);
                if ((currentPlot.pestControlIngredients.size() + currentPlot.soilManagementIngredients.size()) > 1) {
                    bTreatment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            displayTreatmentIngredientPicker();
                        }
                    });
                } else if (currentPlot.pestControlIngredients.size() == 1) {
                    bTreatment.setText(currentPlot.pestControlIngredients.get(0).name);
                    newItem.treatmentIngredient = currentPlot.pestControlIngredients.get(0);
                } else if (currentPlot.soilManagementIngredients.size() == 1) {
                    bTreatment.setText(currentPlot.soilManagementIngredients.get(0).name);
                    newItem.treatmentIngredient = currentPlot.soilManagementIngredients.get(0);
                }
            } else {
                bTreatment.setVisibility(View.GONE);
            }
        } else {
            bCrop.setVisibility(View.GONE);
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

        if (d.type > 0 && d.type < 4) {
            tlQuantityUnits.setVisibility(View.VISIBLE);
            newItem.units = d.defaultUnits;
            bUnits.setText(newItem.units.name);
            bUnits.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayUnitsPicker();
                }
            });
            etQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!charSequence.toString().equals(String.valueOf(newItem.quantity))) {
                        itemChanges = true;
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        } else {
            tlQuantityUnits.setVisibility(View.GONE);
        }

        etValue.setVisibility(View.VISIBLE);
        etValue.setHint(getDefaultCostUnits());
        etValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(String.valueOf(newItem.value))) {
                    itemChanges = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etComments.setVisibility(View.VISIBLE);
        etComments.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(newItem.comments)) {
                    itemChanges = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void fillRecyclerView() {
        createLogList();
        List<oCardData> data = cardDataFromLog();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerViewAdapter = new oRecyclerViewAdapter(data, getApplication());
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void createLogList() {
        oLog log = new oLog(this);
        logList = (plot >= 0) ? log.sortLogByDate(log.createLog(farmId, farmVersion, plot, userId, 2), true, -1) :
                log.sortLogByDate(log.createLog(farmId, farmVersion, userId, 2), true, -1);
    }

    public List<oCardData> cardDataFromLog() {
        dateHelper dH = new dateHelper();
        List<oCardData> ret = new ArrayList<>();
        Iterator<oLog> logIterator = logList.iterator();
        int n = 0;
        while (logIterator.hasNext()) {
            oLog l = logIterator.next();
            oCardData c = new oCardData();
            c.id = n;
            if (plot < 0) {
                getPlotInfo(c, l);
            } else {
                if (n % 2 == 0) {
                    c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillFaded);
                } else {
                    c.plotInfoColor = ContextCompat.getColor(this, R.color.colorWhite);
                }
            }
            if (l.dataItem == null) {
                c.info = (c.info.isEmpty()) ? dH.dateToString(l.date) : c.info + "\n\n" + dH.dateToString(l.date);
                c.imgFile = l.picture;
                c.sndFile = l.sound;
            } else {
                c.info = (c.info.isEmpty()) ? getDataItemText(l) : c.info + "\n\n" + getDataItemText(l);

            }
            c.isSelected = false;

            ret.add(c);
            n++;
        }
        return ret;
    }

    public void viewImage(View v) {
        int n = (int) v.getTag();
        oLog l = logList.get(n);

        Bitmap picture = scaleBitmap(l.picture);

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_view_picturesound);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        ImageView i = (ImageView) dialog.findViewById(R.id.imageView);
        i.setMaxWidth((int)(displayWidth*.8f));
        i.setMaxHeight((int)(displayHeight*.8f));
        i.setImageBitmap(picture);

        final String s = l.sound;

        ImageView player = (ImageView) dialog.findViewById(R.id.playStopButton);
        player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (soundPlaying && soundPlayer != null) {
                    soundPlayer.stop();
                    soundPlayer.release();
                    ImageView player = (ImageView) view;
                    player.setImageResource(R.drawable.play);
                    player.invalidate();
                    soundPlaying = !soundPlaying;
                } else {
                    final ImageView player = (ImageView) view;
                    player.setImageResource(R.drawable.stop);
                    player.invalidate();
                    try {
                        soundPlayer = new MediaPlayer();
                        soundPlayer.setDataSource(s);
                        soundPlayer.prepare();
                        soundPlayer.start();
                        soundPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer m) {
                                if (soundPlayer != null) {
                                    soundPlayer.stop();
                                    soundPlayer.release();
                                    player.setImageResource(R.drawable.play);
                                    player.invalidate();
                                    soundPlaying = false;
                                }
                            }
                        });
                        soundPlaying = !soundPlaying;
                    } catch (IOException e) {

                    }
                }

            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                stopSoundPlayer();
            }
        });

        dialog.show();
    }

    public Bitmap scaleBitmap(String path) {
        Bitmap ret = null;
        final int IMAGE_MAX_SIZE = 400000;
        try {
            InputStream in = null;
            in = new FileInputStream(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();

            int scale = 1;
            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }

            in = new FileInputStream(path);

            if (scale > 1) {
                scale--;
                options = new BitmapFactory.Options();
                options.inSampleSize = scale;
                ret = BitmapFactory.decodeStream(in, null, options);
                in.close();

                int height = ret.getHeight();
                int width = ret.getWidth();

                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(ret, (int) x, (int) y, true);

                ret.recycle();
                ret = scaledBitmap;
                return ret;

            } else {
                ret = BitmapFactory.decodeStream(in);
                in.close();
                return ret;
            }


        } catch (IOException e) {

        }
        return ret;
    }

    void stopSoundPlayer() {
        if (soundPlaying && soundPlayer != null) {
            soundPlayer.stop();
            soundPlayer.release();
            soundPlaying = false;
        }
    }

    public void getPlotInfo(oCardData c, oLog l) {
        oFarm f = new oFarm(this);
        f = f.getFarm(userId, farmId, l.farmVersion, this);
        oPlotMatrix pm = new oPlotMatrix();
        pm.fromString(this, f.plotMatrix, ";");
        oPlot p = (l.plotId!=-1) ? pm.getPlotFromId(l.plotId) : null;

        if(p!=null) {
            String title = getString(R.string.plotWord) + ": " + p.getCropNames(this);
            String treatments = "";
            if (p.pestControlIngredients.size() > 0 && p.soilManagementIngredients.size() > 0) {
                c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillSoilManagementAndPestControlFaded);
                treatments = "\n" + getString(R.string.soilManagementTitle) + ", " + getString(R.string.pestControlTitle);
            } else if (p.pestControlIngredients.size() > 0 && p.soilManagementIngredients.size() == 0) {
                c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillPestControlFaded);
                treatments = "\n" + getString(R.string.pestControlTitle);
            } else if (p.pestControlIngredients.size() == 0 && p.soilManagementIngredients.size() > 0) {
                c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillSoilManagementFaded);
                treatments = "\n" + getString(R.string.soilManagementTitle);
            } else {
                c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillFaded);
            }
            c.info = title + treatments;
        } else {
            c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillFaded);
            c.info = farmName;
        }


    }

    public String getDataItemText(oLog l) {
        dateHelper dH = new dateHelper();
        String ret = "";
        String dataItem = l.getDataItemName(this);
        String date = dH.dateToString(l.date);
        if (l.dataItem.type > 0 && l.dataItem.type < 4) {
            String quantityUnits = String.valueOf(l.quantity) + " " + l.units.name;
            ret = date + "\n" + dataItem + ", " + quantityUnits + "\n";
        } else {
            ret = date + "\n" + dataItem + ": ";
        }
        ret += String.valueOf(l.value) + " " + getDefaultCostUnits();
        if (!l.comments.isEmpty()) {
            ret += "\n\n" + l.comments;
        }
        return ret;
    }

    public void selectItem(View v) {
        int n = (int) v.getTag();
        CheckBox cb = (CheckBox) v;
        recyclerViewAdapter.list.get(n).isSelected = cb.isChecked();

        nSelected = (cb.isChecked()) ? nSelected + 1 : nSelected - 1;
        if (nSelected > 0) {
            setTitle(getString(R.string.recordsActivity) + ": " + String.valueOf(nSelected) + " " + getString(R.string.selected));
        } else {
            setTitle(getString(R.string.recordsActivity) + ": " + farmName);
        }

        invalidateOptionsMenu();
    }

    public void editItem(View v) {
        if (farmVersion == maxVersion) {
            final int n = (int) v.getTag();
            if (n >= 0) {
                editingItem = logList.get(n);
                if(editingItem.farmVersion==maxVersion) {
                    TextView tv = (TextView) v;
                    tv.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
                    if (plot == -1) {
                        currentPlot = (editingItem.plotId != -1) ? getCurrentPlot(editingItem.plotId) : null;
                        getDataItemsList();
                    }
                    addItem(v);
                } else {
                    editingItem=null;
                }
            }
        }
    }

    public boolean checkFields() {
        boolean ret = true;
        if (newItem.dataItem.isCropSpecific && newItem.crop == null) {
            Toast.makeText(this, R.string.mustChooseCropMessage, Toast.LENGTH_SHORT).show();
        } else if (newItem.dataItem.isTreatmentSpecific && newItem.treatmentIngredient == null) {
            Toast.makeText(this, R.string.mustChooseTreatmentMessage, Toast.LENGTH_SHORT).show();
        } else if ((newItem.dataItem.type > 0 && newItem.dataItem.type <4) && (etQuantity.getText().toString().isEmpty())) {
            Toast.makeText(this, R.string.quantityEmptyMessage, Toast.LENGTH_SHORT).show();
            etQuantity.requestFocus();
        } else if (etValue.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.valueEmptyMessage, Toast.LENGTH_SHORT).show();
            etValue.requestFocus();
        } else {
            ret = false;
            newItem.quantity = (newItem.dataItem.type > 0 && newItem.dataItem.type < 4) ? Float.parseFloat(etQuantity.getText().toString()) : 0.0f;
            newItem.value = Float.parseFloat(etValue.getText().toString());
            newItem.comments = etComments.getText().toString().replaceAll(";", "");
            newItem.comments = newItem.comments.replaceAll("\\*", "");
            newItem.comments = newItem.comments.replaceAll("\\|", "");
            newItem.context = context;
            if (editingItem == null) {
                newItem.appendToLog(farmId, farmVersion, userId, plot, newItem.date, newItem.dataItem, newItem.value, newItem.quantity,
                        newItem.units, newItem.crop, newItem.treatmentIngredient, 0.0f, newItem.comments, "", "");
            } else {
                int thisPlot = (plot >= 0) ? plot : editingItem.plotId;
                newItem.updateLogItem(editingItem.line, farmId, farmVersion, userId, thisPlot, newItem.date, newItem.dataItem, newItem.value, newItem.quantity,
                        newItem.units, newItem.crop, newItem.treatmentIngredient, 0.0f, newItem.comments);
            }
            editingItem = null;
        }

        return ret;
    }

    public void resetFields() {
        etValue.setText(R.string.emptyString);
        etQuantity.setText(R.string.emptyString);
        etComments.setText(R.string.emptyString);
        bCrop.setText(R.string.chooseCropButtonLabel);
        bTreatment.setText(R.string.chooseTreatmentButtonLabel);
    }

    public void displayDatePicker(View view) {
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

                newItem.date = nd;

                bDate.setText(dH.dateToString(nd));
                dialogDate.dismiss();

                itemChanges = true;
            }
        });
        dialogDate.show();
    }

    public void displayCropPicker() {
        ArrayList<String> cropNames = new ArrayList<>();
        Iterator<oCrop> iterator = currentPlot.crops.iterator();
        while (iterator.hasNext()) {
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
                newItem.crop = currentPlot.crops.get(i);
                bCrop.setText(newItem.crop.name);
                itemChanges = true;
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialogCrops = builder.create();
        dialogCrops.show();
    }

    public void displayUnitsPicker() {
        ArrayList<String> unitNames = new ArrayList<>();
        oUnit u = new oUnit(this);
        final ArrayList<oUnit> unitList = u.getUnits(0);
        Iterator<oUnit> iteratorUnits = unitList.iterator();
        while (iteratorUnits.hasNext()) {
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
                newItem.units = unitList.get(i);
                bUnits.setText(newItem.units.name);
                itemChanges = true;
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialogUnits = builder.create();
        dialogUnits.show();

    }

    public void displayTreatmentIngredientPicker() {
        final ArrayList<oTreatmentIngredient> treatmentIngredients = new ArrayList<>();
        ArrayList<String> treatmentIngredientNames = new ArrayList<>();
        Iterator<oTreatmentIngredient> iteratorPestControl = currentPlot.pestControlIngredients.iterator();
        while (iteratorPestControl.hasNext()) {
            oTreatmentIngredient t = iteratorPestControl.next();
            treatmentIngredientNames.add(t.name);
            treatmentIngredients.add(t);
        }
        Iterator<oTreatmentIngredient> iteratorSoilManagement = currentPlot.soilManagementIngredients.iterator();
        while (iteratorSoilManagement.hasNext()) {
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
                newItem.treatmentIngredient = treatmentIngredients.get(i);
                bTreatment.setText(newItem.treatmentIngredient.name);
                itemChanges = true;
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialogCrops = builder.create();
        dialogCrops.show();
    }

    public CharSequence getDefaultCostUnits() {
        CharSequence ret = "";
        oUnit u = new oUnit(this);
        ret = u.getUnitNames(2).get(0);
        return ret;
    }

    public oPlot getCurrentPlot(int thisPlot) {
        oPlot ret;
        oFarm f = new oFarm(this);
        f = f.getVersion(userId, farmId, farmVersion, this);
        oPlotMatrix p = new oPlotMatrix();
        p.fromString(this, f.plotMatrix, ";");
        ret = p.getPlotFromId(thisPlot);
        return ret;
    }

    public void getDataItemsList() {
        oDataItem d = new oDataItem(this);
        boolean bExcludeCropSpecific = (currentPlot != null) ? (currentPlot.crops.size() == 0) ? true : false : true;
        boolean bExcludeTreatmentSpecific = (currentPlot != null) ? (currentPlot.pestControlIngredients.size() == 0 && currentPlot.soilManagementIngredients.size() == 0) ? true : false : true;
        boolean bOnlyRetroactive = (farmVersion < maxVersion) ? true : false;
        dataItemsList = d.getDataItems(bExcludeCropSpecific, bExcludeTreatmentSpecific, bOnlyRetroactive);
        dataItemsNamesArray = d.getDataItemNames(bExcludeCropSpecific, bExcludeTreatmentSpecific, bOnlyRetroactive).toArray(new CharSequence[dataItemsList.size()]);
    }

    public void goBack() {
        stopSoundPlayer();
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

    public void goToBalance() {
        stopSoundPlayer();
        final Context context = this;
        Intent i = new Intent(context, balance.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("farmSize",farmSize);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion", farmVersion);
        i.putExtra("maxVersion", maxVersion);
        i.putExtra("plot", plot);
        i.putExtra("farmDate", farmDate);
        i.putExtra("cropNames", cropNames);
        i.putExtra("pestControlNames", pestControlNames);
        i.putExtra("soilManagementNames", soilManagementNames);
        i.putExtra("plotSize", plotSize);
        i.putExtra("displayWidth", displayWidth);
        i.putExtra("displayHeight", displayHeight);
        i.putExtra("from",1);
        startActivity(i);
        finish();
    }

    public void localDeleteFarms(){
        Iterator<oFarm> iterator = farmsPendingDelete.iterator();
        while(iterator.hasNext()){
            oFarm f = iterator.next();
            f.context = this;
            int[] linesToDelete = f.getFarmLineList(userId,f.id);
            f.doDeleteFarm(linesToDelete);
        }
    }

    public void localMarkFarmsAsSaved(){
        Iterator<oFarm> iterator = farmsPendingSave.iterator();
        while(iterator.hasNext()){
            oFarm f = iterator.next();
            f.context = this;
            int[] idsToUpdate = f.getFarmLineList(userId, f.id);
            f.updateFarmStatus(idsToUpdate, 2);
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
        switch(connectionTask){
            case 0:
                deletingFarmDialog.dismiss();
                if (output.equals("ok")) {
                    localDeleteFarms();
                }
                oFarm f = new oFarm(this);
                farmsPendingSave = f.getFarmsPendingSave(userId);
                if (farmsPendingSave.size() > 0) {
                    connectionTask = 1;
                    doSaveFarms();
                } else {
                    sendMessages();
                }
                break;
            case 1:
                savingFarmsDialog.dismiss();
                if (output.equals("ok")){
                    localMarkFarmsAsSaved();
                }
                sendMessages();
                break;
            case 2:
                downloadingParamsDialog.dismiss();
                String[] nextLine;
                CSVReader reader = new CSVReader(new StringReader(output), ',', '"');
                File file = new File(this.getFilesDir(), "parameters");
                try {
                    FileWriter w = new FileWriter(file);
                    CSVWriter writer = new CSVWriter(w, ',', '"');
                    while ((nextLine = reader.readNext()) != null) {
                        writer.writeNext(nextLine);
                    }
                    writer.close();
                    reader.close();
                    if (getEmailParams()) {
                        doSendMessages();
                    } else {
                        Toast.makeText(this, R.string.incorrectInternetParamsMessage, Toast.LENGTH_SHORT).show();
                        bConnecting=false;
                    }
                } catch (IOException e) {
                    Toast.makeText(this, R.string.incorrectInternetParamsMessage, Toast.LENGTH_SHORT).show();
                    bConnecting=false;
                }
        }
    }

}
