package ojovoz.ugunduzi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by Eugenio on 27/03/2018.
 */
public class farmChooser extends AppCompatActivity implements httpConnection.AsyncResponse {

    String user;
    String userPass;
    int userId;
    String server;

    preferenceManager prefs;

    ArrayList<CheckBox> checkboxes;
    ArrayList<oFarm> farms;

    String deleteList;

    boolean bConnecting = false;

    ProgressDialog deletingFarmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_chooser);

        user = getIntent().getExtras().getString("user");
        userPass = getIntent().getExtras().getString("userPass");
        userId = getIntent().getExtras().getInt("userId");

        prefs = new preferenceManager(this);
        server = prefs.getPreference("server");

        prefs.deletePreference("farmId");

        fillTable();

    }

    void fillTable() {

        oFarm f = new oFarm(this);

        TableLayout farmTable = (TableLayout)findViewById(R.id.chooserTable);
        farmTable.removeAllViews();
        checkboxes = new ArrayList<>();

        farms = f.getActiveFarms(userId);

        if(farms !=null){

            Collections.sort(farms, new Comparator<oFarm>() {
                @Override
                public int compare(oFarm f1, oFarm f2) {
                    return f1.name.compareTo(f2.name);
                }
            });

            int n=0;
            Iterator<oFarm> iterator = farms.iterator();
            while (iterator.hasNext()) {
                oFarm farm = iterator.next();
                final TableRow trow = new TableRow(farmChooser.this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f);
                lp.setMargins(10, 10, 0, 10);

                if (n % 2 == 0) {
                    trow.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillFaded));
                } else {
                    trow.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite));
                }
                CheckBox cb = new CheckBox(farmChooser.this);
                cb.setButtonDrawable(R.drawable.custom_checkbox);
                cb.setId(n);
                cb.setPadding(4, 4, 4, 4);
                cb.setChecked(false);
                cb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        invalidateOptionsMenu();
                    }
                });
                checkboxes.add(cb);
                trow.addView(cb, lp);

                TextView tv = new TextView(farmChooser.this);
                tv.setId(n);
                tv.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f);
                tv.setText(farm.name);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                tv.setPadding(0, 10, 0, 10);
                tv.setMaxWidth(350);
                tv.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        goToFarm(v);
                    }

                });
                trow.addView(tv, lp);

                trow.setGravity(Gravity.CENTER_VERTICAL);
                farmTable.addView(trow, lp);

                n++;
            }
            if(n==0){
                goToFarm(null);
            }
        } else {
            goToLogin();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, 0, 0, R.string.opCreateNewFarm);
        if(anyCheckboxChecked()) {
            menu.add(1, 1, 1, R.string.opDeleteSelectedFarms);
        }
        menu.add(2, 2, 2, R.string.opSwitchUser);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                goToFarm(null);
                break;
            case 1:
                deleteSelectedFarms();
                break;
            case 2:
                confirmExit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean anyCheckboxChecked(){
        boolean ret=false;

        Iterator<CheckBox> checkBoxIterator = checkboxes.iterator();
        while (checkBoxIterator.hasNext()) {
            CheckBox cb = checkBoxIterator.next();
            if (cb.isChecked()) {
                ret=true;
                break;
            }
        }

        return ret;
    }

    public void deleteSelectedFarms(){
        deleteList="";
        Iterator<CheckBox> checkBoxIterator = checkboxes.iterator();
        while (checkBoxIterator.hasNext()) {
            CheckBox cb = checkBoxIterator.next();
            if(cb.isChecked()){
                deleteList = (deleteList.isEmpty()) ? Integer.toString(farms.get(cb.getId()).id) : deleteList + ";" + Integer.toString(farms.get(cb.getId()).id);
            }
        }
        if(!deleteList.isEmpty()){
            AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
            logoutDialog.setMessage(R.string.deleteFarmConfirmMessage);
            logoutDialog.setNegativeButton(R.string.noButtonText,null);
            logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doDeleteFarms();
                }
            });
            logoutDialog.create();
            logoutDialog.show();

        } else {
            Toast.makeText(this, R.string.noFarmsSelectedMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void doDeleteFarms(){
        httpConnection http = new httpConnection(this, this);
        if (http.isOnline()) {
            if (!bConnecting) {
                bConnecting = true;
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
                http.execute(server + "/mobile/delete_farm.php?user=" + userId + "&farm=" + deleteList, "");
            }
        } else {
            markDeletedFarms();
            fillTable();
        }
    }

    public void markDeletedFarms(){
        oFarm f = new oFarm(this);
        String[] ids = deleteList.split(";");
        int [] farmIds = new int[ids.length];
        for(int i=0;i<ids.length;i++){
            int[] idsToDelete = f.getFarmLineList(userId,Integer.valueOf(ids[i]));
            f.updateFarmStatus(idsToDelete,1);
            farmIds[i]=Integer.valueOf(ids[i]);
        }
        deleteFarmLogs(farmIds);
    }

    public void executeDeleteFarms(){
        oFarm f = new oFarm(this);
        String[] ids = deleteList.split(";");
        int [] farmIds = new int[ids.length];
        for(int i=0;i<ids.length;i++){
            int[] idsToDelete = f.getFarmLineList(userId,Integer.valueOf(ids[i]));
            f.doDeleteFarm(idsToDelete);
            farmIds[i]=Integer.valueOf(ids[i]);
        }
        deleteFarmLogs(farmIds);
    }

    public void deleteFarmLogs(int[] ids){
        oLog l = new oLog(this);
        ArrayList<String> deleteFiles = l.deleteFarmItems(ids);
        deleteImgSndFiles(deleteFiles);
    }

    public void deleteImgSndFiles(ArrayList<String> deleteFiles){
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
        final Context context = this;
        Intent i = new Intent(context, login.class);
        startActivity(i);
        finish();
    }

    public void goToFarm(View v){
        boolean newFarm=false;
        int farmId;
        if(v!=null) {
            TextView tv = (TextView) v;
            tv.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
            int n = v.getId();
            farmId = farms.get(n).id;
            prefs.savePreferenceInt("farmId",farmId);
        } else {
            newFarm=true;
            farmId=-1;
        }
        final Context context = this;
        Intent i = new Intent(context, farmInterface.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("newFarm", newFarm);
        i.putExtra("firstFarm",false);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion", -1);
        startActivity(i);
        finish();
    }

    @Override
    public void processFinish(String output) {
        bConnecting=false;
        deletingFarmDialog.dismiss();
        if(output.equals("ok")){
            executeDeleteFarms();
        } else {
            markDeletedFarms();
        }
        fillTable();
    }

}
