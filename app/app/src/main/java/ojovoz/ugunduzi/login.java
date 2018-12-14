package ojovoz.ugunduzi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class login extends AppCompatActivity implements httpConnection.AsyncResponse {

    public String server = "";
    public String user = "";
    public String userPass = "";
    private promptDialog dlg = null;
    private preferenceManager prefs;
    private boolean dataDownloaded = false;

    private String uAS = "";
    private String uPS = "";

    private ArrayList<String> dataItems;
    private String lang;
    private int index;
    private ProgressDialog dialog;
    private int uploadIncrement = 1;
    private boolean bConnecting = false;
    private int connectionTask;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = new preferenceManager(this);

        dataDownloaded = prefs.getPreferenceBoolean("dataDownloaded");

        if (!prefs.preferenceExists("farmIdNumber")) {
            prefs.savePreferenceInt("farmIdNumber", 0);
        }

        server = prefs.getPreference("server");
        if (server.equals("")) {
            defineServer("");
        }

        user = prefs.getPreference("user");
        if (!user.equals("")) {
            if (dataDownloaded) {
                userId = prefs.getPreferenceInt("userId");
                userPass = prefs.getPreference("userPass");
                startNextActivity();
            } else {
                downloadData();
            }
        } else {
            updateAutocomplete();
        }
    }

    public void defineServer(String current) {
        dlg = new promptDialog(this, R.string.emptyString, R.string.defineServerLabel, current) {
            @Override
            public boolean onOkClicked(String input) {
                if (!input.startsWith("http://")) {
                    input = "http://" + input;
                }
                login.this.server = input;
                prefs.savePreference("server", input);
                downloadData();
                return true;
            }
        };
        dlg.show();
    }

    private void createNewUser(String uAS, String uPS) {
        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            CharSequence dialogTitle = getString(R.string.createNewUserLabel);

            dialog = new ProgressDialog(this);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage(dialogTitle);
            dialog.setIndeterminate(true);
            dialog.show();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface d) {
                    bConnecting = false;
                }
            });
            doCreateNewUser(uAS, uPS);
        } else {
            prefs.savePreferenceInt("userId", 0);
            prefs.savePreference("userPass", uPS);
        }
    }

    public void downloadData() {
        dataItems = new ArrayList<>();
        dataItems.add("parameters");
        dataItems.add("users");
        dataItems.add("crops");
        dataItems.add("treatments");
        dataItems.add("treatment_ingredients");
        dataItems.add("units");
        dataItems.add("data_items");

        lang = Locale.getDefault().getLanguage();

        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            index = 0;
            CharSequence dialogTitle = getString(R.string.downloadDataProgressDialogTitle) + " " + dataItems.get(index);

            dialog = new ProgressDialog(this);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage(dialogTitle);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.setMax(dataItems.size() - 1);
            dialog.show();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface d) {
                    bConnecting = false;
                    index = 0;
                    prefs.deletePreference("dataDownloaded");
                }
            });
            doDownload();
        } else {
            Toast.makeText(this, R.string.pleaseConnectMessage, Toast.LENGTH_SHORT).show();
            bConnecting = false;
        }
    }

    private void doDownload() {
        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            if (!bConnecting) {
                bConnecting = true;
                connectionTask = 0;
                String l = (lang.equals("en")) ? "?lang=en" : "";
                http.execute(server + "/mobile/get_" + dataItems.get(index) + ".php" + l, "csv");
            }
        } else {
            Toast.makeText(this, R.string.pleaseConnectMessage, Toast.LENGTH_SHORT).show();
            bConnecting = false;
        }
    }

    private void doCreateNewUser(String uAS, String uPS) {
        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            if (!bConnecting) {
                bConnecting = true;
                connectionTask = 1;
                try {
                    String arguments = "alias=" + URLEncoder.encode(uAS, "UTF-8") + "&pass=" + URLEncoder.encode(uPS, "UTF-8");
                    http.execute(server + "/mobile/create_new_user.php?" + arguments, "");
                } catch (IOException e) {

                }
            }
        }
    }

    public void updateAutocomplete() {
        AutoCompleteTextView a = (AutoCompleteTextView) findViewById(R.id.userAlias);
        oUser u = new oUser(this);
        String userList[] = u.getAllUserNames().split(",");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, userList);
        a.setAdapter(adapter);
        a.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    }

    public void validateUser(View v) {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        doValidateUser();
    }

    public void doValidateUser(){
        EditText uA = (EditText) findViewById(R.id.userAlias);
        uAS = uA.getText().toString().trim();
        EditText uP = (EditText) findViewById(R.id.userPassword);
        uPS = uP.getText().toString().trim();
        if (!uAS.equals("") && !uPS.equals("")) {
            if (uAS.equals("admin") && uPS.equals("admin")) {
                uAS="";
                uPS="";
                uA.setText(R.string.emptyString);
                uP.setText(R.string.emptyString);
                defineServer(server);
            } else if (uAS.equals("reset") && uPS.equals("reset")) {
                user = "";
                prefs.deletePreference("user");
                prefs.deletePreference("farm");
                prefs.deletePreference("dataDownloaded");
                uAS="";
                uPS="";
                uA.setText(R.string.emptyString);
                uP.setText(R.string.emptyString);
                downloadData();
            } else {
                oUser newUser = new oUser(this, uAS, uPS);
                userId = newUser.getUserIdFromAliasPass();
                if (userId > 0) {
                    // -1 = wrong password, 0 = new user, >0 known user
                    user = uAS;
                    userPass = uPS;
                    prefs.savePreference("user", uAS);
                    prefs.savePreference("userPass", uPS);
                    prefs.savePreferenceInt("userId", userId);
                    checkUserDataDownload();
                } else if (userId == 0) {
                    /*
                    if (dataDownloaded) {
                        connectionTask = 1;
                        createNewUser(uAS, uPS);
                    } else {
                        downloadData();
                    }
                    */
                    Toast.makeText(this, R.string.wrongPasswordLabel, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.wrongPasswordLabel, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void checkUserDataDownload() {
        oFarm f = new oFarm(this);
        if (f.hasFarms(userId) == 0) {
            doDownloadUserData();
        } else {
            startNextActivity();
        }
    }

    public void doDownloadUserData() {
        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            if (!bConnecting) {
                CharSequence dialogTitle = getString(R.string.downloadingUserData);

                dialog = new ProgressDialog(this);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setMessage(dialogTitle);
                dialog.setIndeterminate(true);
                dialog.show();
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface d) {
                        bConnecting = false;
                    }
                });
                bConnecting = true;
                connectionTask = 2;
                http.execute(server + "/mobile/get_user_farms.php?user=" + Integer.toString(userId), "");
            }
        } else {
            startNextActivity();
        }
    }

    @Override
    public void processFinish(String output) {
        if (bConnecting) {
            switch (connectionTask) {
                case 0:
                    bConnecting = false;
                    String[] nextLine;
                    CSVReader reader = new CSVReader(new StringReader(output), ',', '"');
                    deleteCatalog(dataItems.get(index));
                    File file = new File(this.getFilesDir(), dataItems.get(index));
                    try {
                        FileWriter w = new FileWriter(file);
                        CSVWriter writer = new CSVWriter(w, ',', '"');
                        while ((nextLine = reader.readNext()) != null) {
                            writer.writeNext(nextLine);
                        }
                        writer.close();
                        reader.close();
                    } catch (IOException e) {

                    }

                    index++;
                    if (index < dataItems.size()) {
                        progressHandler.sendMessage(progressHandler.obtainMessage());
                        doDownload();
                    } else {
                        bConnecting = false;
                        dialog.dismiss();
                        prefs.savePreferenceBoolean("dataDownloaded", true);
                        dataDownloaded = true;
                        if (!user.equals("")) {
                            startNextActivity();
                        } else {
                            updateAutocomplete();
                            doValidateUser();
                        }
                    }
                    break;
                case 1:
                    if (TextUtils.isEmpty(output)) {
                        bConnecting = false;
                        dialog.dismiss();
                        Toast.makeText(this, R.string.incorrectServerURLMessage, Toast.LENGTH_SHORT).show();
                        defineServer(server);
                    } else {
                        dialog.dismiss();
                        userId = Integer.parseInt(output);
                        if (userId != 0) {
                            if (userId > 0) {
                                prefs.savePreferenceInt("userId", userId);
                            } else if (userId < 0) {
                                userId *= -1;
                                prefs.savePreferenceInt("userId", userId);
                            }
                            prefs.savePreference("user", uAS);
                            prefs.savePreference("userPass", uPS);
                            user = uAS;
                            oUser newUser = new oUser(this);
                            newUser.addNewUser(userId, uAS, uPS);
                            startNextActivity();
                        } else {
                            Toast.makeText(this, R.string.wrongPasswordLabel, Toast.LENGTH_SHORT).show();
                            updateAutocomplete();
                        }
                    }
                    break;
                case 2:
                    if (!output.isEmpty()) {
                        createUserFarms(output);
                    } else {
                        dialog.dismiss();
                        startNextActivity();
                    }
                    break;
                case 3:
                    if(!output.isEmpty()){
                        createUserLog(output);
                    } else {
                        dialog.dismiss();
                        startNextActivity();
                    }
            }
        }
    }

    Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dialog.incrementProgressBy(uploadIncrement);
            CharSequence dialogTitle = getString(R.string.downloadDataProgressDialogTitle) + " " + dataItems.get(index);
            dialog.setMessage(dialogTitle);
        }
    };

    public void createUserFarms(String farms) {
        dateHelper dH = new dateHelper();
        int maxId = 0;
        ArrayList<oFarm> newFarms = new ArrayList<>();
        String[] lines = farms.split("\\*");
        for (int i = 0; i < lines.length; i++) {
            String[] thisFarm = lines[i].split(",");
            String farmName = thisFarm[0].replaceAll("\"", "");
            int farmId = Integer.parseInt(thisFarm[1]);
            int farmVersion = Integer.parseInt(thisFarm[2]);
            float farmSize = Float.parseFloat(thisFarm[3]);
            String date = thisFarm[4];
            String plotId = thisFarm[6];
            String plotX = thisFarm[7];
            String plotY = thisFarm[8];
            String plotW = thisFarm[9];
            String plotH = thisFarm[10];
            String plotSize = thisFarm[11];
            String cropsList = thisFarm[12];
            String pestControlList = thisFarm[13];
            String soilManagementList = thisFarm[14];

            String plotMatrix = plotId + ";" + plotX + ";" + plotY + ";" + plotW + ";" + plotH + ";" + plotSize + ";" + cropsList + ";" + pestControlList + ";" +
                    soilManagementList;

            Iterator<oFarm> iterator = newFarms.iterator();
            boolean bFound = false;
            while (iterator.hasNext()) {
                oFarm f = iterator.next();
                if (f.id == farmId && f.version == farmVersion) {
                    f.plotMatrix = f.plotMatrix + ";" + plotMatrix;
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                oFarm f = new oFarm(this);
                f.id = farmId;
                f.context = this;
                f.name = farmName;
                f.version = farmVersion;
                f.userId = userId;
                f.size = farmSize;
                f.dateCreated = dH.stringToDate(date);
                f.status = 2;
                f.plotMatrix = plotMatrix;
                newFarms.add(f);

                maxId = (farmId > maxId) ? farmId : maxId;
            }
        }

        Iterator<oFarm> iterator = newFarms.iterator();
        while (iterator.hasNext()) {
            oFarm f = iterator.next();
            f.addNewFarm(f.id, f.userId, f.name, f.size, f.dateCreated, f.plotMatrix, f.version, f.status);
        }

        if (maxId >= prefs.getPreferenceInt("farmIdNumber")) {
            prefs.savePreferenceInt("farmIdNumber", maxId + 1);
        }

        downloadUserLog();
    }

    public void downloadUserLog() {
        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {

            connectionTask = 3;
            http.execute(server + "/mobile/get_user_log.php?user=" + Integer.toString(userId), "");

        } else {
            dialog.dismiss();
            bConnecting=false;
            Toast.makeText(this, R.string.pleaseConnectMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void createUserLog(String log){
        dateHelper dH = new dateHelper();
        String[] lines = log.split("\\*");

        for(int i=0; i<lines.length; i++){
            oDataItem d = new oDataItem(this);
            oUnit u = new oUnit(this);
            oCrop c = new oCrop(this);
            oTreatmentIngredient t = new oTreatmentIngredient(this);
            oLog l = new oLog(this);
            String[] thisItem = lines[i].split(",");
            int farmId = Integer.parseInt(thisItem[0]);
            int farmVersion = Integer.parseInt(thisItem[1]);
            int plotId = Integer.parseInt(thisItem[2]);
            Date date = dH.stringToDate(thisItem[3]);
            int dataItemId = Integer.parseInt(thisItem[4]);
            d = d.getDataItemFromId(dataItemId);
            Float quantity = Float.parseFloat(thisItem[5]);
            Float value = Float.parseFloat(thisItem[6]);
            int unitsId = Integer.parseInt(thisItem[7]);
            u = u.getUnitFromId(unitsId);
            int cropId = Integer.parseInt(thisItem[8]);
            c = c.getCropFromId(cropId);
            int treatmentIngredientId = Integer.parseInt(thisItem[9]);
            t = t.getTreatmentIngredientFromId(treatmentIngredientId);
            String comments="";
            if(thisItem.length==11){
                try{
                    comments=URLDecoder.decode(thisItem[10].replaceAll("\"",""),"UTF-8");
                } catch (Exception e){
                //
                }
            }
            l.appendToLog(farmId,farmVersion,userId,plotId,date,d,value,quantity,u,c,t,0.0f,comments,"","");
        }

        dialog.dismiss();
        startNextActivity();
    }

    private void deleteCatalog(String filename) {
        this.deleteFile(filename);
    }

    private void startNextActivity() {

        boolean bProceed = true;
        final Context context = this;

        if (prefs.preferenceExists("farmId")) {

            int farmId = prefs.getPreferenceInt("farmId");
            if (farmId >= 0) {

                Intent i = new Intent(context, farmInterface.class);
                i.putExtra("user", user);
                i.putExtra("userId", userId);
                i.putExtra("userPass", userPass);
                i.putExtra("newFarm", false);
                i.putExtra("firstFarm", false);
                i.putExtra("farmId", farmId);
                i.putExtra("farmVersion", -1);
                startActivity(i);
                finish();
                bProceed = false;
            }

        }

        if (bProceed) {
            oFarm f = new oFarm(this);
            ArrayList<oFarm> userFarms = f.getActiveFarms(userId);
            if (userFarms.size() > 0) {
                if (userFarms.size() > 1) {
                    Intent i = new Intent(context, farmChooser.class);
                    i.putExtra("user", user);
                    i.putExtra("userId", userId);
                    i.putExtra("userPass", userPass);
                    startActivity(i);
                    finish();
                } else {
                    int farmId = userFarms.get(0).id;
                    prefs.savePreferenceInt("farmId", farmId);
                    Intent i = new Intent(context, farmInterface.class);
                    i.putExtra("user", user);
                    i.putExtra("userId", userId);
                    i.putExtra("userPass", userPass);
                    i.putExtra("newFarm", false);
                    i.putExtra("firstFarm", false);
                    i.putExtra("farmId", farmId);
                    i.putExtra("farmVersion", -1);
                    startActivity(i);
                    finish();
                }
            } else {
                Intent i = new Intent(context, farmInterface.class);
                i.putExtra("user", user);
                i.putExtra("userId", userId);
                i.putExtra("userPass", userPass);
                i.putExtra("newFarm", true);
                i.putExtra("firstFarm", true);
                i.putExtra("farmVersion", -1);
                startActivity(i);
                finish();
            }
        }
    }
}
