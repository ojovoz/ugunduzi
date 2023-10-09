package ojovoz.ugunduzi;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Eugenio on 13/04/2018.
 */
public class oDataItem {

    public int id;
    public String name;
    public oUnit defaultUnits;
    public int type; //0=activity (cost), 1=activity (cost with quantity), 2=input (cost), 3=output (sale)
    public boolean isCropSpecific;
    public boolean isTreatmentSpecific;
    public boolean isRetroactive;

    private Context context;

    oDataItem(){

    }

    oDataItem(Context c){
        context=c;
    }

    public ArrayList<oDataItem> getDataItems(boolean bExcludeCropSpecific, boolean bExcludeTreatmentSpecific, boolean bOnlyRetroactive){
        ArrayList<oDataItem> ret = new ArrayList<>();
        csvFileManager dataItemList;

        dataItemList = new csvFileManager("data_items");
        List<String[]> dataItemCSV = dataItemList.read(context);
        if(dataItemCSV!=null) {
            Iterator<String[]> iterator = dataItemCSV.iterator();
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                if(!((record[4].equals("1") && bExcludeCropSpecific) || (record[5].equals("1") && bExcludeTreatmentSpecific) || (record[6].equals("0") && bOnlyRetroactive))) {
                    oUnit u = new oUnit(context);
                    oDataItem d = new oDataItem();
                    d.id = Integer.parseInt(record[0]);
                    d.name = record[1];
                    d.defaultUnits = u.getUnitFromId(Integer.parseInt(record[2]));
                    d.type = Integer.parseInt(record[3]);
                    d.isCropSpecific = (record[4].equals("1"));
                    d.isTreatmentSpecific = (record[5].equals("1"));
                    ret.add(d);
                }
            }
        }
        return ret;
    }

    public ArrayList<String> getDataItemNames(boolean bExcludeCropSpecific, boolean bExcludeTreatmentSpecific, boolean bOnlyRetroactive){
        ArrayList<String> ret = new ArrayList<>();
        ArrayList<oDataItem> dataItemList = getDataItems(bExcludeCropSpecific, bExcludeTreatmentSpecific, bOnlyRetroactive);

        Iterator<oDataItem> iterator = dataItemList.iterator();
        while(iterator.hasNext()){
            oDataItem d = iterator.next();
            ret.add(d.name);
        }
        return ret;
    }

    public oDataItem getDataItemFromId(int id){
        oDataItem ret = new oDataItem();
        if(id==0){
            ret=null;
        } else {
            ArrayList<oDataItem> dataItemList = getDataItems(false,false,false);
            Iterator<oDataItem> iterator = dataItemList.iterator();
            while (iterator.hasNext()) {
                oDataItem d = iterator.next();
                if (d.id == id) {
                    ret = d;
                    break;
                }
            }
        }
        return ret;
    }
}
