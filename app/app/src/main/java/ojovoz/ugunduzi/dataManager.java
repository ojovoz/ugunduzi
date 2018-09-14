package ojovoz.ugunduzi;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
 * Created by Eugenio on 24/04/2018.
 */
public class dataManager extends AppCompatActivity implements httpConnection.AsyncResponse {

    public oCrop crop1;
    public oCrop crop2;
    public oTreatment treatment1;
    public oTreatment treatment2;

    public String user;
    public String userPass;
    public int userId;
    public String farmName;
    public int farmId;
    public int plot;

    public ArrayList<oLog> logList;

    public preferenceManager prefs;

    oRecyclerViewAdapter adapter;

    boolean soundPlaying;
    MediaPlayer soundPlayer;

    oLog editing;
    oUnit editedUnits;

    int nSelected = 0;
    String activityTitle;

    public String server;
    String farmsPendingDelete;
    ArrayList<String> farmsPendingSave;
    int farmSaveIndex;
    int connectionTask; // 0=delete pending farms, 1=send pending farms, 2=send items, 3=downloading parameters
    boolean bConnecting = false;
    ProgressDialog deletingFarmDialog;
    ProgressDialog savingFarmsDialog;
    ProgressDialog downloadingParamsDialog;

    public String ugunduziEmail = "";
    public String ugunduziPass = "";
    public String dataSubject = "";
    public String multimediaSubject = "";
    public String smtpServer = "";
    public String smtpPort = "";

    private ProgressDialog sendingDataDialog;
    private ProgressDialog sendingMultimediaDialog;
    private Thread uploadMultimedia;
    private int[] multimediaCleanUpList;
    private ArrayList<oLog> multimediaSentItems;
    private Context dataManagerContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_manager);

        dataManagerContext = this;

        prefs = new preferenceManager(this);
        server = prefs.getPreference("server");

        crop1 = new oCrop(this);
        crop2 = new oCrop(this);
        treatment1 = new oTreatment(this);
        treatment2 = new oTreatment(this);

        user = getIntent().getExtras().getString("user");
        userPass = getIntent().getExtras().getString("userPass");
        userId = getIntent().getExtras().getInt("userId");
        farmName = getIntent().getExtras().getString("farmName");
        plot = getIntent().getExtras().getInt("plot");

        int crop1Id = getIntent().getExtras().getInt("crop1");
        int crop2Id = getIntent().getExtras().getInt("crop2");
        int treatment1Id = getIntent().getExtras().getInt("treatment1");
        int treatment2Id = getIntent().getExtras().getInt("treatment2");

        crop1 = (crop1Id > 0) ? crop1.getCropFromId(crop1Id) : null;
        crop2 = (crop2Id > 0) ? crop2.getCropFromId(crop2Id) : null;

        treatment1 = (treatment1Id > 0) ? treatment1.getTreatmentFromId(treatment1Id) : null;
        treatment2 = (treatment2Id > 0) ? treatment2.getTreatmentFromId(treatment2Id) : null;

        TextView tt = (TextView) findViewById(R.id.plotLabel);
        String title = "";

        activityTitle = getTitle().toString();
        if (plot >= 0) {
            activityTitle = activityTitle.replace("X", getString(R.string.plotWord));

            if (crop1 == null && crop2 == null) {
                title = getString(R.string.plotCropLabel) + " " + getString(R.string.textNone);
            } else {
                if (crop1 != null && crop2 == null) {
                    title = getString(R.string.plotCropLabel) + ": " + crop1.name;
                } else if (crop1 != null && crop2 != null) {
                    title = getString(R.string.plotCropLabel) + " " + crop1.name + ", " + crop2.name;
                }
            }
            title += "\n";
            if (treatment1 == null && treatment2 == null) {
                title += getString(R.string.plotTreatmentLabel) + " " + getString(R.string.textNone);
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
                    title += getString(R.string.plotTreatmentLabel) + " " + treatment1.name + ", " + treatment2.name;
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
        } else {
            activityTitle = activityTitle.replace("X", getString(R.string.farmWord));
            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            tt.setText(farmName);
            tt.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        }
        setTitle(activityTitle);

        oLog log = new oLog(this);
        //logList = (plot >= 0) ? log.sortLogByDate(log.createLog(farmName, userId, plot, 2), true, -1) : log.sortLogByDate(log.createLog(farmId, userId, 2), true, -1);

        soundPlaying = false;
        soundPlayer = new MediaPlayer();

        fillRecyclerView();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, R.string.opSendSelectedItems);
        menu.add(1, 1, 1, R.string.opDeleteSelectedItems);
        if (plot >= 0) {
            menu.add(2, 2, 2, R.string.opPictureSound);
            menu.add(3, 3, 3, R.string.opEnterData);
        }
        menu.add(4, 4, 4, R.string.opGoBackToFarm);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                sendSelectedItems();
                break;
            case 1:
                tryDeleteSelectedItems();
                break;
            case 2:
                goToPictureSound();
                break;
            case 3:
                goToEnterData();
                break;
            case 4:
                goBack();
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendSelectedItems() {
        if (!bConnecting) {
            httpConnection http = new httpConnection(this, this);
            if (http.isOnline()) {
                bConnecting = true;
                farmsPendingDelete = prefs.getFarmsPendingDelete(user + "_farms", ";");
                if (!farmsPendingDelete.isEmpty()) {
                    connectionTask = 0;
                    doDeleteFarms();
                } else {
                    farmsPendingSave = prefs.getFarmsPendingSave(user + "_farms", ";");
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

    public void doDeleteFarms() {
        httpConnection http = new httpConnection(this, this);
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
        http.execute(server + "/mobile/delete_farm.php?user=" + userId + "&farm=" + farmsPendingDelete.replaceAll(" ", "_"), "");
    }

    public void doSaveFarms() {
        CharSequence dialogTitle = getString(R.string.createNewFarmLabel);
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
        farmSaveIndex = 0;
        executeSaveFarm();
    }

    public void executeSaveFarm() {
        httpConnection http = new httpConnection(this, this);
        String saveString = user + ";" + userPass + ";" + farmsPendingSave.get(farmSaveIndex) + ";" + prefs.getPreference(user + "_" + farmsPendingSave.get(farmSaveIndex));
        http.execute(server + "/mobile/create_new_farm.php?farm=" + saveString.replaceAll(" ", "_"), "");
    }

    public void goToPictureSound() {
        stopSoundPlayer();
        Intent i = new Intent(this, pictureSound.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("plot", plot);
        if (crop1 != null) {
            i.putExtra("crop1", crop1.id);
        } else {
            i.putExtra("crop1", -1);
        }
        if (crop2 != null) {
            i.putExtra("crop2", crop2.id);
        } else {
            i.putExtra("crop2", -1);
        }
        if (treatment1 != null) {
            i.putExtra("treatment1", treatment1.id);
        } else {
            i.putExtra("treatment1", -1);
        }
        if (treatment2 != null) {
            i.putExtra("treatment2", treatment2.id);
        } else {
            i.putExtra("treatment2", -1);
        }
        startActivity(i);
        finish();
    }

    public void goToEnterData() {
        /*
        stopSoundPlayer();
        Intent i = new Intent(this, enterData.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("plot", plot);
        if (crop1 != null) {
            i.putExtra("crop1", crop1.id);
        } else {
            i.putExtra("crop1", -1);
        }
        if (crop2 != null) {
            i.putExtra("crop2", crop2.id);
        } else {
            i.putExtra("crop2", -1);
        }
        if (treatment1 != null) {
            i.putExtra("treatment1", treatment1.id);
        } else {
            i.putExtra("treatment1", -1);
        }
        if (treatment2 != null) {
            i.putExtra("treatment2", treatment2.id);
        } else {
            i.putExtra("treatment2", -1);
        }
        startActivity(i);
        finish();
        */
    }

    public void goBack() {
        stopSoundPlayer();
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

    void stopSoundPlayer() {
        if (soundPlaying && soundPlayer != null) {
            soundPlayer.stop();
            soundPlayer.release();
            soundPlaying = false;
        }
    }
    public void fillRecyclerView() {
        List<oCardData> data = cardDataFromLog();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new oRecyclerViewAdapter(data, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    public String getDataItemText(oLog l) {
        dateHelper dH = new dateHelper();
        String ret = "";
        String dataItem = l.getDataItemName(this);
        String date = dH.dateToString(l.date);
        if (l.dataItem.type == 1) {
            ret = date + "\n" + dataItem;
        } else if (l.dataItem.type == 0 || l.dataItem.type == 2) {
            String valueUnits = String.valueOf(l.value) + " " + l.units.name;
            ret = date + "\n" + dataItem + ": " + valueUnits;
        }
        return ret;
    }

    public void getPlotInfo(oCardData c, oLog l) {
        oPlotMatrix pm = new oPlotMatrix();
        String farmString = prefs.getPreference(user + "_" + farmName);
        pm.fromString(this, farmString, ";");

        oPlot p = pm.getPlotFromId(l.plotId);
        oCrop c1 = p.crop1;
        oCrop c2 = p.crop2;
        oTreatment t1 = p.treatment1;
        oTreatment t2 = p.treatment2;

        String title = "";

        if (c1 == null && c2 == null) {
            title = getString(R.string.plotWord) + ": " + getString(R.string.textNoCrops);
        } else {
            if (c1 != null && c2 == null) {
                title = getString(R.string.plotWord) + ": " + c1.name;
            } else if (c1 != null && c2 != null) {
                title = getString(R.string.plotWord).toString() + ": " + c1.name + ", " + c2.name;
            }
        }
        title += "\n";
        if (t1 == null && t2 == null) {
            title += getString(R.string.textNoTreatments);
            c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillFaded);
        } else {
            if (t1 != null && t2 == null) {
                title += t1.name;
                if (t1.category == 0) {
                    c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillPestControlFaded);
                } else {
                    c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillSoilManagementFaded);
                }
            } else if (t1 != null && t2 != null) {
                title += t1.name + ", " + t2.name;
                if (t1.category != t2.category) {
                    c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillSoilManagementAndPestControlFaded);
                } else {
                    if (t1.category == 0) {
                        c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillPestControlFaded);
                    } else {
                        c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillSoilManagementFaded);
                    }
                }
            }
        }
        c.info = title;
    }

    public void selectItem(View v) {
        int n = (int) v.getTag();
        CheckBox cb = (CheckBox) v;
        adapter.list.get(n).isSelected = cb.isChecked();

        nSelected = (cb.isChecked()) ? nSelected + 1 : nSelected - 1;
        if (nSelected > 0) {
            setTitle(activityTitle + ": " + String.valueOf(nSelected) + " " + getString(R.string.selected));
        } else {
            setTitle(activityTitle);
        }
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

    public void editItem(View v) {
        final int position = (int) v.getTag();

        if (position >= 0) {

            final dateHelper dH = new dateHelper();

            editing = logList.get(position);
            editedUnits = editing.units;

            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_edit_data);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);

            TextView tv = (TextView) dialog.findViewById(R.id.dataItem);
            tv.setText(editing.getDataItemName(this));

            final EditText ev = (EditText) dialog.findViewById(R.id.dataItemValue);
            final TextView ut = (TextView) dialog.findViewById(R.id.unitsText);
            final Button bu = (Button) dialog.findViewById(R.id.dataItemUnits);

            final Button db = (Button) dialog.findViewById(R.id.dateButton);
            db.setText(dH.dateToString(editing.date));
            db.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialogDate = new Dialog(view.getContext());
                    dialogDate.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogDate.setContentView(R.layout.dialog_datepicker);

                    DatePicker dp = (DatePicker) dialogDate.findViewById(R.id.datePicker);
                    Calendar calActivity = Calendar.getInstance();
                    calActivity.setTime(editing.date);
                    dp.init(calActivity.get(Calendar.YEAR), calActivity.get(Calendar.MONTH), calActivity.get(Calendar.DAY_OF_MONTH), null);

                    Calendar calMax = Calendar.getInstance();
                    calMax.setTime(new Date());

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

                            db.setText(dH.dateToString(nd));
                            dialogDate.dismiss();
                        }
                    });
                    dialogDate.show();
                }
            });

            if (editing.dataItem.type != 1) {

                ev.setText(String.valueOf(editing.value));

                oUnit u = new oUnit(this);
                final ArrayList<oUnit> unitsList = u.getUnits(editing.dataItem.type);

                if (unitsList.size() == 1) {
                    String units = unitsList.get(0).name;
                    ut.setText(units);
                    bu.setVisibility(View.GONE);
                } else {
                    ut.setVisibility(View.GONE);
                    final CharSequence unitsNamesArray[] = u.getUnitNames(editing.dataItem.type).toArray(new CharSequence[unitsList.size()]);
                    bu.setText(editing.units.name);
                    bu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                            builder.setCancelable(true);
                            builder.setNegativeButton(R.string.cancelButtonText, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogUnits, int which) {
                                    dialogUnits.dismiss();
                                }
                            });
                            final ListAdapter adapter = new ArrayAdapter<>(view.getContext(), R.layout.checked_list_template, unitsNamesArray);
                            builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i >= 0) {
                                        editedUnits = unitsList.get(i);
                                        String chosenUnitsName = unitsNamesArray[i].toString();
                                        bu.setText(chosenUnitsName);
                                    }
                                    dialogInterface.dismiss();
                                }
                            });
                            AlertDialog dialogUnits = builder.create();
                            dialogUnits.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialogUnits.show();
                        }
                    });
                }
            } else {
                ut.setVisibility(View.GONE);
                ev.setVisibility(View.GONE);
                bu.setVisibility(View.GONE);
            }

            Button bs = (Button) dialog.findViewById(R.id.saveButton);
            bs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tv = ev.getText().toString();
                    if (!tv.isEmpty() || editing.dataItem.type == 1) {
                        float editedValue = (tv.isEmpty()) ? 0 : Float.parseFloat(tv);
                        if ((editedValue >= 0.0f && (editing.dataItem.type == 0 || editing.dataItem.type == 2)) || editing.dataItem.type == 1) {
                            editing.date = dH.stringToDate(db.getText().toString());
                            editing.value = editedValue;
                            editing.units = editedUnits;
                            oLog l = new oLog(dialog.getContext());
                            logList = l.sortLogByDate(logList, true, -1);
                            adapter.list = cardDataFromLog();
                            adapter.setList(adapter.list);
                            adapter.notifyDataSetChanged();
                            //l.updateLogItem(editing.line, editing.farmName, editing.userId, editing.plotId, editing.date, editing.dataItem, editing.value, editing.units, editing.crop, editing.treatmentIngredient);
                            dialog.dismiss();
                            nSelected = 0;
                            setTitle(activityTitle);
                        } else {
                            Toast.makeText(dialog.getContext(), R.string.valueNegativeMessage, Toast.LENGTH_SHORT).show();
                            ev.requestFocus();
                        }
                    } else {
                        Toast.makeText(dialog.getContext(), R.string.valueEmptyMessage, Toast.LENGTH_SHORT).show();
                        ev.requestFocus();
                    }

                }
            });

            dialog.show();

        }

    }

    public void tryDeleteSelectedItems() {
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
        int[] delete = new int[adapter.list.size()];
        ArrayList<String> deleteFiles = new ArrayList<>();
        List<oCardData> list = adapter.list;
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
        //logList = (plot >= 0) ? l.sortLogByDate(l.createLog(farmName, userId, plot, 2), true, -1) : l.sortLogByDate(l.createLog(farmId, userId, 2), true, -1);
        adapter.list = cardDataFromLog();
        adapter.setList(adapter.list);
        adapter.notifyDataSetChanged();
        nSelected = 0;
        setTitle(activityTitle);
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

    public Bitmap scaleBitmap(String path) {
        Bitmap ret = null;
        final int IMAGE_MAX_SIZE = 200000;
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

    public boolean getEmailParams() {
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

    public void sendMessages() {
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
                connectionTask = 3;
                http.execute(server + "/mobile/get_parameters.php?", "");
            }

        } else {
            Toast.makeText(this, R.string.pleaseConnectMessage, Toast.LENGTH_SHORT).show();
            bConnecting=false;
        }
    }

    public void doSendMessages() {
        String b = "";
        Iterator<oLog> iterator = logList.iterator();
        while (iterator.hasNext()) {
            oLog l = iterator.next();
            if (l.dataItem != null) {
                b = (b.isEmpty()) ? l.toString(";") : b + "*" + l.toString(";");
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
                            sendingDataDialog = new ProgressDialog(dataManagerContext);
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
                                        Toast.makeText(dataManagerContext, R.string.incorrectInternetParamsMessage, Toast.LENGTH_SHORT).show();
                                        if (sendingDataDialog != null) {
                                            sendingDataDialog.dismiss();
                                        }
                                        bConnecting=false;
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(dataManagerContext, R.string.incorrectInternetParamsMessage, Toast.LENGTH_SHORT).show();
                                    if (sendingDataDialog != null) {
                                        sendingDataDialog.dismiss();
                                    }
                                    bConnecting=false;
                                }
                            } catch (Exception e) {
                                Toast.makeText(dataManagerContext, R.string.incorrectInternetParamsMessage, Toast.LENGTH_SHORT).show();
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
        ArrayList<oLog> multimediaLog = new ArrayList<>();
        Iterator<oLog> iterator = logList.iterator();
        while (iterator.hasNext()) {
            oLog l = iterator.next();
            if (l.dataItem==null && !l.sent) {
                b.add(l.toString(";"));
                multimediaLog.add(l);
            }
        }

        if(!b.isEmpty()){
            final ArrayList<String> emailBody=b;
            final ArrayList<oLog> attachments = multimediaLog;
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
                        oLog l = new oLog(dataManagerContext);
                        logList = l.sortLogByDate(logList, true, -1);
                        adapter.list = cardDataFromLog();
                        adapter.setList(adapter.list);
                        adapter.notifyDataSetChanged();
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
                    item.setSent(dataManagerContext);
                }

                oLog l = new oLog(dataManagerContext);

                l.deleteLogItems(multimediaCleanUpList);

                //logList = (plot >= 0) ? l.sortLogByDate(l.createLog(farmName, userId, plot, 2), true, -1) : l.sortLogByDate(l.createLog(farmId, userId, 2), true, -1);
                adapter.list = cardDataFromLog();
                adapter.setList(adapter.list);
                adapter.notifyDataSetChanged();
                nSelected = 0;
                setTitle(activityTitle);
            }
        }
    };

    @Override
    public void processFinish(String output) {
        switch (connectionTask) {
            case 0:
                deletingFarmDialog.dismiss();
                if (output.equals("ok")) {
                    prefs.updateDeletedFarms(farmsPendingDelete, user + "_farms", ";");
                }
                farmsPendingSave = prefs.getFarmsPendingSave(user + "_farms", ";");
                if (farmsPendingSave.size() > 0) {
                    connectionTask = 1;
                    doSaveFarms();
                } else {
                    sendMessages();
                }
                break;
            case 1:
                if (!output.equals("ko")) {
                    prefs.updateSavedFarm(farmsPendingSave.get(farmSaveIndex), user + "_farms", ";");
                }
                farmSaveIndex++;
                if (farmSaveIndex < farmsPendingSave.size()) {
                    executeSaveFarm();
                } else {
                    savingFarmsDialog.dismiss();
                    sendMessages();
                }
                break;
            case 3:
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
