package ojovoz.ugunduzi;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

    public oLog editingItem;
    public boolean itemChanges;

    public int nSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);

        context = this;
        nSelected=0;

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

        title = getString(R.string.cropsTitle) + ": " + cropNames;
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

        tt.setText(title);

        dataItemDate = new Date();
        unitsList = new ArrayList<>();
        currentPlot = getCurrentPlot();

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
        if(nSelected>0) {
            menu.add(0, 0, 0, R.string.opDeleteSelectedItems);
        }
        menu.add(1, 1, 1, R.string.opBalance);
        menu.add(2, 2, 2, R.string.opGoBack);
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
                goBack();
        }
        return super.onOptionsItemSelected(item);
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
        int[] delete = new int[recyclerViewAdapter.list.size()];
        List<oCardData> list = recyclerViewAdapter.list;
        Iterator<oCardData> iterator = list.iterator();
        int n = 0;
        while (iterator.hasNext()) {
            oCardData cd = iterator.next();
            if (cd.isSelected) {
                delete[n] = logList.get(cd.id).line;
            } else {
                delete[n] = -1;
            }
            n++;
        }
        oLog l = new oLog(this);
        l.deleteLogItems(delete);
        createLogList();
        recyclerViewAdapter.list = cardDataFromLog();
        recyclerViewAdapter.setList(recyclerViewAdapter.list);
        recyclerViewAdapter.notifyDataSetChanged();
        nSelected = 0;
        setTitle(getString(R.string.financeActivity));
    }

    public void addItem(View v) {

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
                if (i == keyEvent.KEYCODE_BACK && newItem.dataItem != null && itemChanges && !bCancellingData)
                {
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
                                        if ((int) tv.getTag() % 2 == 0) {
                                            tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillFaded));
                                        } else {
                                            tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorWhite));
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
                }else{
                    if (i == keyEvent.KEYCODE_BACK && editingItem != null) {
                        TextView tv = (TextView) editingView;
                        if ((int) tv.getTag() % 2 == 0) {
                            tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorFillFaded));
                        } else {
                            tv.setBackgroundColor(ContextCompat.getColor(tv.getContext(), R.color.colorWhite));
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
            if (currentPlot.crops.size() > 1 && editingItem.dataItem.isCropSpecific) {
                bCrop.setText(editingItem.crop.name);
            }
            if ((currentPlot.pestControlIngredients.size() > 0 || currentPlot.soilManagementIngredients.size() > 0) && editingItem.dataItem.isTreatmentSpecific) {
                bTreatment.setText(editingItem.treatmentIngredient.name);
            }

            if (editingItem.dataItem.type != 0) {
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
                        recyclerViewAdapter.list = cardDataFromLog();
                        recyclerViewAdapter.setList(recyclerViewAdapter.list);
                        recyclerViewAdapter.notifyDataSetChanged();
                    } else if(!itemChanges) {
                        dialog.dismiss();
                        if(editingItem!=null) {
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

                                        recyclerViewAdapter.list = cardDataFromLog();
                                        recyclerViewAdapter.setList(recyclerViewAdapter.list);
                                        recyclerViewAdapter.notifyDataSetChanged();
                                    } else if(!itemChanges) {
                                        dialog.dismiss();
                                        if(editingItem!=null) {
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
            }
        } else if (d.isTreatmentSpecific) {
            bTreatment.setVisibility(View.VISIBLE);
            if (currentPlot.pestControlIngredients.size() == 1) {
                bTreatment.setText(currentPlot.pestControlIngredients.get(0).name);
                newItem.treatmentIngredient = currentPlot.pestControlIngredients.get(0);
            } else {
                bTreatment.setText(currentPlot.soilManagementIngredients.get(0).name);
                newItem.treatmentIngredient = currentPlot.soilManagementIngredients.get(0);
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

        if (d.type != 0) {
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
                    if(!charSequence.toString().equals(String.valueOf(newItem.quantity))) {
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
                if(!charSequence.toString().equals(String.valueOf(newItem.value))) {
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
                if(!charSequence.toString().equals(newItem.comments)) {
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
        logList = (plot >= 0) ? log.sortLogByDate(log.createLog(farmId, farmVersion, plot, userId, 0), true, -1) :
                log.sortLogByDate(log.createLog(farmId, farmVersion, userId, 0), true, -1);
    }

    public List<oCardData> cardDataFromLog() {
        dateHelper dH = new dateHelper();
        List<oCardData> ret = new ArrayList<>();
        Iterator<oLog> logIterator = logList.iterator();
        int n = 0;
        while (logIterator.hasNext()) {
            oLog l = logIterator.next();
            if (l.dataItem != null) {
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
                c.info = (c.info.isEmpty()) ? getDataItemText(l) : c.info + "\n\n" + getDataItemText(l);
                c.isSelected = false;
                ret.add(c);
                n++;
            }
        }
        return ret;
    }

    public void getPlotInfo(oCardData c, oLog l) {
        oFarm f = new oFarm(this);
        f = f.getFarm(userId, farmId, farmVersion, this);
        oPlotMatrix pm = new oPlotMatrix();
        pm.fromString(this, f.plotMatrix, ";");
        oPlot p = pm.getPlotFromId(l.plotId);

        String title = getString(R.string.cropsTitle) + ": " + p.getCropNames(this);
        title += "\n";
        title += getString(R.string.pestControlTitle) + ": " + p.getPestControlNames(this);
        title += "\n";
        title += getString(R.string.soilManagementTitle) + ": " + p.getSoilManagementNames(this);

        if (p.pestControlIngredients.size() > 0 && p.soilManagementIngredients.size() > 0) {
            c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillSoilManagementAndPestControl);
        } else if (p.pestControlIngredients.size() > 0 && p.soilManagementIngredients.size() == 0) {
            c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillPestControl);
        } else if (p.pestControlIngredients.size() == 0 && p.soilManagementIngredients.size() > 0) {
            c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillSoilManagement);
        } else {
            c.plotInfoColor = ContextCompat.getColor(this, R.color.colorFillDefault);
        }

        c.info = title;
    }

    public String getDataItemText(oLog l) {
        dateHelper dH = new dateHelper();
        String ret = "";
        String dataItem = l.getDataItemName(this);
        String date = dH.dateToString(l.date);
        if (l.dataItem.type != 0) {
            String quantityUnits = String.valueOf(l.quantity) + " " + l.units.name;
            ret = date + "\n" + dataItem + ": " + quantityUnits + "\n";
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
            setTitle(getString(R.string.financeActivity) + ": " + String.valueOf(nSelected) + " " + getString(R.string.selected));
        } else {
            setTitle(getString(R.string.financeActivity));
        }

        invalidateOptionsMenu();
    }

    public void editItem(View v) {
        final int n = (int) v.getTag();
        if (n >= 0) {
            TextView tv = (TextView) v;
            tv.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
            editingItem = logList.get(n);
            addItem(v);
        }
    }

    public boolean checkFields() {
        boolean ret = true;
        if (newItem.dataItem.isCropSpecific && newItem.crop == null) {
            Toast.makeText(this, R.string.mustChooseCropMessage, Toast.LENGTH_SHORT).show();
        } else if (newItem.dataItem.isTreatmentSpecific && newItem.treatmentIngredient == null) {
            Toast.makeText(this, R.string.mustChooseTreatmentMessage, Toast.LENGTH_SHORT).show();
        } else if (newItem.dataItem.type != 0 && (etQuantity.getText().toString().isEmpty())) {
            Toast.makeText(this, R.string.quantityEmptyMessage, Toast.LENGTH_SHORT).show();
            etQuantity.requestFocus();
        } else if (etValue.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.valueEmptyMessage, Toast.LENGTH_SHORT).show();
            etValue.requestFocus();
        } else {
            ret = false;
            newItem.quantity = (newItem.dataItem.type != 0) ? Float.parseFloat(etQuantity.getText().toString()) : 0.0f;
            newItem.value = Float.parseFloat(etValue.getText().toString());
            newItem.comments = etComments.getText().toString().replaceAll(";", "");
            newItem.context = context;
            if (editingItem == null) {
                newItem.appendToLog(farmId, farmVersion, userId, plot, newItem.date, newItem.dataItem, newItem.value, newItem.quantity,
                        newItem.units, newItem.crop, newItem.treatmentIngredient, 0.0f, newItem.comments, "", "");
            } else {
                newItem.updateLogItem(newItem.line,farmId,farmVersion,userId,plot,newItem.date,newItem.dataItem,newItem.value,newItem.quantity,
                        newItem.units,newItem.crop,newItem.treatmentIngredient, 0.0f, newItem.comments);
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

                itemChanges=true;
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
                itemChanges=true;
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
                itemChanges=true;
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
                itemChanges=true;
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

    public oPlot getCurrentPlot() {
        oPlot ret;
        oFarm f = new oFarm(this);
        f = f.getVersion(userId, farmId, farmVersion, this);
        oPlotMatrix p = new oPlotMatrix();
        p.fromString(this, f.plotMatrix, ";");
        ret = p.plots.get(plot);
        return ret;
    }

    public void getDataItemsList() {
        oDataItem d = new oDataItem(this);
        boolean bExcludeCropSpecific = (currentPlot.crops.size() == 0) ? true : false;
        boolean bExcludeTreatmentSpecific = (currentPlot.pestControlIngredients.size() == 0 && currentPlot.soilManagementIngredients.size() == 0) ? true : false;
        dataItemsList = d.getDataItems(bExcludeCropSpecific, bExcludeTreatmentSpecific);
        dataItemsNamesArray = d.getDataItemNames(bExcludeCropSpecific, bExcludeTreatmentSpecific).toArray(new CharSequence[dataItemsList.size()]);
    }

    public void goBack() {
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

    public void goToBalance(){
        final Context context = this;
        Intent i = new Intent(context, balance.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName",farmName);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion", farmVersion);
        i.putExtra("plot",plot);
        i.putExtra("cropNames",cropNames);
        i.putExtra("pestControlNames",pestControlNames);
        i.putExtra("soilManagementNames",soilManagementNames);
        i.putExtra("displayWidth",displayWidth);
        i.putExtra("displayHeight",displayHeight);
        startActivity(i);
        finish();
    }

}
