package ojovoz.ugunduzi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Eugenio on 24/04/2018.
 */
public class dataManager extends AppCompatActivity {

    public oCrop crop1;
    public oCrop crop2;
    public oTreatment treatment1;
    public oTreatment treatment2;

    public String user;
    public String userPass;
    public int userId;
    public String farmName;
    public int plot;

    public ArrayList<oLog> logList;

    public preferenceManager prefs;

    oRecyclerViewAdapter adapter;

    boolean soundPlaying;
    MediaPlayer soundPlayer;

    oLog editing;
    oUnit editedUnits;

    int nSelected=0;
    String activityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_manager);

        prefs = new preferenceManager(this);

        crop1 = new oCrop(this);
        crop2 = new oCrop(this);
        treatment1 = new oTreatment(this);
        treatment2 = new oTreatment(this);

        user = getIntent().getExtras().getString("user");
        userPass = getIntent().getExtras().getString("userPass");
        userId = getIntent().getExtras().getInt("userId");
        farmName = getIntent().getExtras().getString("farmName");
        plot = getIntent().getExtras().getInt("plot");

        crop1 = (getIntent().getExtras().getInt("crop1") > 0) ? crop1.getCropFromId(getIntent().getExtras().getInt("crop1")) : null;
        crop2 = (getIntent().getExtras().getInt("crop2") > 0) ? crop2.getCropFromId(getIntent().getExtras().getInt("crop2")) : null;

        treatment1 = (getIntent().getExtras().getInt("treatment1") > 0) ? treatment1.getTreatmentFromId(getIntent().getExtras().getInt("treatment1")) : null;
        treatment2 = (getIntent().getExtras().getInt("treatment2") > 0) ? treatment2.getTreatmentFromId(getIntent().getExtras().getInt("treatment2")) : null;

        TextView tt = (TextView) findViewById(R.id.plotLabel);
        String title = "";

        activityTitle = getTitle().toString();
        if (plot >= 0) {
            activityTitle = activityTitle.replace("X", getString(R.string.plotWord));

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
        } else {
            activityTitle = activityTitle.replace("X", getString(R.string.farmWord));
            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            tt.setText(farmName);
            tt.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        }
        setTitle(activityTitle);

        oLog log = new oLog(this);
        logList = (plot >= 0) ? log.sortLogByDate(log.createLog(farmName, userId, plot, 2), true, -1) : log.sortLogByDate(log.createLog(farmName, userId, 2), true, -1);

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
        if (plot >= 0) {
            menu.add(0, 0, 0, R.string.opPictureSound);
            menu.add(1, 1, 1, R.string.opEnterData);
        }
        menu.add(2, 2, 2, R.string.opGoBack);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                goToPictureSound();
                break;
            case 1:
                goToEnterData();
                break;
            case 2:
                goBack();
        }
        return super.onOptionsItemSelected(item);
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
            i.putExtra("crop1", "-1");
        }
        if (crop2 != null) {
            i.putExtra("crop2", crop2.id);
        } else {
            i.putExtra("crop2", "-1");
        }
        if (treatment1 != null) {
            i.putExtra("treatment1", treatment1.id);
        } else {
            i.putExtra("treatment1", "-1");
        }
        if (treatment2 != null) {
            i.putExtra("treatment2", treatment2.id);
        } else {
            i.putExtra("treatment2", "-1");
        }
        startActivity(i);
        finish();
    }

    public void goToEnterData() {
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
            i.putExtra("crop1", "-1");
        }
        if (crop2 != null) {
            i.putExtra("crop2", crop2.id);
        } else {
            i.putExtra("crop2", "-1");
        }
        if (treatment1 != null) {
            i.putExtra("treatment1", treatment1.id);
        } else {
            i.putExtra("treatment1", "-1");
        }
        if (treatment2 != null) {
            i.putExtra("treatment2", treatment2.id);
        } else {
            i.putExtra("treatment2", "-1");
        }
        startActivity(i);
        finish();
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
            c.isSelected=false;
            ret.add(c);
            n++;
        }
        return ret;
    }

    public String getDataItemText(oLog l) {
        dateHelper dH = new dateHelper();
        String ret = "";
        String dataItem = l.getDataItemName();
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
        pm.fromString(this, prefs.getPreference(user + "_" + farmName.replaceAll(" ", "_")), ";");

        oPlot p = pm.getPlotFromId(l.plotId);
        oCrop c1 = p.crop1;
        oCrop c2 = p.crop2;
        oTreatment t1 = p.treatment1;
        oTreatment t2 = p.treatment2;

        String title = "";

        if (c1 == null && c1 == null) {
            title = getString(R.string.plotWord) + ": " + getString(R.string.textNone);
        } else {
            if (c1 != null && c1 == null) {
                title = getString(R.string.plotWord) + ": " + c1.name;
            } else if (c1 != null && c1 != null) {
                title = getString(R.string.plotWord) + ": " + c1.name + ", " + c2.name;
            }
        }
        title += "\n";
        if (t1 == null && t2 == null) {
            title += getString(R.string.textNone);
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

        nSelected = (cb.isChecked()) ? nSelected+1 : nSelected-1;
        if(nSelected>0){
            setTitle(activityTitle+": "+String.valueOf(nSelected)+" "+getString(R.string.selected));
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
            tv.setText(editing.getDataItemName());

            final EditText ev = (EditText)dialog.findViewById(R.id.dataItemValue);
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

            Button bs = (Button)dialog.findViewById(R.id.saveButton);
            bs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tv = ev.getText().toString();
                    if(!tv.isEmpty() || editing.dataItem.type==1){
                        float editedValue = (tv.isEmpty()) ? 0 : Float.parseFloat(tv);
                        if((editedValue>=0.0f && (editing.dataItem.type==0 || editing.dataItem.type==2)) || editing.dataItem.type==1){
                            editing.date=dH.stringToDate(db.getText().toString());
                            editing.value=editedValue;
                            editing.units=editedUnits;
                            oLog l = new oLog();
                            logList=l.sortLogByDate(logList,true,-1);
                            adapter.list=cardDataFromLog();
                            adapter.setList(adapter.list);
                            adapter.notifyDataSetChanged();
                            //TODO: update log file
                            dialog.dismiss();
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
}
