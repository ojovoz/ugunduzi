package ojovoz.ugunduzi;

import android.content.Context;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Eugenio on 13/04/2018.
 */
public class oLog {

    public int line;

    public String farmName; //delete
    public int farmId;
    public int farmVersion;
    public int userId;

    public int plotId;
    public Date date;
    public oDataItem dataItem;
    public float quantity;
    public float value;
    public oUnit units;
    public float cost;
    public String comments;

    public oCrop crop;
    public oTreatmentIngredient treatmentIngredient;

    public String picture;
    public String sound;

    public boolean sent;

    public Context context;

    private dateHelper dH;

    oLog(){
        dH = new dateHelper();
    }

    oLog(Context c){
        dH = new dateHelper();
        context=c;
    }

    public ArrayList<oLog> createLog(int mode){
        ArrayList<oLog> ret = new ArrayList<>();
        csvFileManager log;

        log = new csvFileManager("log");
        List<String[]> logCSV = log.read(context);
        if(logCSV!=null) {
            Iterator<String[]> iterator = logCSV.iterator();
            int n=0;
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                oLog l = new oLog();
                l.line=n;
                l.farmId = Integer.parseInt(record[0]);
                l.farmVersion = Integer.parseInt(record[1]);
                l.userId = Integer.parseInt(record[2]);
                l.plotId = Integer.parseInt(record[3]);
                l.date = dH.stringToDate(record[4]);
                oDataItem di = new oDataItem(context);
                l.dataItem = di.getDataItemFromId(Integer.parseInt(record[5]));
                switch(mode){
                    case 0:
                        if(l.dataItem!=null){
                            l.value = Float.parseFloat(record[6]);
                            oUnit u = new oUnit(context);
                            l.quantity = Float.parseFloat(record[7]);
                            l.units = u.getUnitFromId(Integer.parseInt(record[8]));
                            oCrop c = new oCrop(context);
                            l.crop = c.getCropFromId(Integer.parseInt(record[9]));
                            oTreatmentIngredient t = new oTreatmentIngredient(context);
                            l.treatmentIngredient = t.getTreatmentIngredientFromId(Integer.parseInt(record[10]));
                            l.cost = Float.parseFloat(record[11]);
                            l.comments = record[12];
                            l.sent = (record[15].equals("1"));
                            ret.add(l);
                        }
                        break;
                    case 1:
                        if(!record[13].isEmpty()){
                            l.picture = record[13];
                            l.sound = record[14];
                            l.sent = (record[15].equals("1"));
                            ret.add(l);
                        }
                        break;
                    case 2:
                        l.value = Float.parseFloat(record[6]);
                        oUnit u = new oUnit(context);
                        l.quantity = Float.parseFloat(record[7]);
                        l.units = u.getUnitFromId(Integer.parseInt(record[8]));
                        oCrop c = new oCrop(context);
                        l.crop = c.getCropFromId(Integer.parseInt(record[9]));
                        oTreatmentIngredient t = new oTreatmentIngredient(context);
                        l.treatmentIngredient = t.getTreatmentIngredientFromId(Integer.parseInt(record[10]));
                        l.cost = Float.parseFloat(record[11]);
                        l.comments = record[12];
                        l.picture = record[13];
                        l.sound = record[14];
                        l.sent = (record[15].equals("1"));
                        ret.add(l);
                }
                n++;
            }
        }
        return ret;
    }

    public ArrayList<oLog> createLog(int farmId, int version, int userId, int mode){
        ArrayList<oLog> ret = new ArrayList<>();
        csvFileManager log;

        log = new csvFileManager("log");
        List<String[]> logCSV = log.read(context);
        if(logCSV!=null) {
            Iterator<String[]> iterator = logCSV.iterator();
            int n=0;
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                if(Integer.parseInt(record[0])==farmId && Integer.parseInt(record[1])<=version && (Integer.parseInt(record[2])==userId || userId==-1)) {
                    oLog l = new oLog();
                    l.line=n;
                    l.farmId = Integer.parseInt(record[0]);
                    l.farmVersion = Integer.parseInt(record[1]);
                    l.userId = Integer.parseInt(record[2]);
                    l.plotId = Integer.parseInt(record[3]);
                    l.date = dH.stringToDate(record[4]);
                    oDataItem di = new oDataItem(context);
                    l.dataItem = di.getDataItemFromId(Integer.parseInt(record[5]));
                    switch(mode){
                        case 0:
                            if(l.dataItem!=null){
                                l.value = Float.parseFloat(record[6]);
                                oUnit u = new oUnit(context);
                                l.quantity = Float.parseFloat(record[7]);
                                l.units = u.getUnitFromId(Integer.parseInt(record[8]));
                                oCrop c = new oCrop(context);
                                l.crop = c.getCropFromId(Integer.parseInt(record[9]));
                                oTreatmentIngredient t = new oTreatmentIngredient(context);
                                l.treatmentIngredient = t.getTreatmentIngredientFromId(Integer.parseInt(record[10]));
                                l.cost = Float.parseFloat(record[11]);
                                l.comments = record[12];
                                l.sent = (record[15].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 1:
                            if(!record[13].isEmpty()){
                                l.picture = record[13];
                                l.sound = record[14];
                                l.sent = (record[15].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 2:
                            l.value = Float.parseFloat(record[6]);
                            oUnit u = new oUnit(context);
                            l.quantity = Float.parseFloat(record[7]);
                            l.units = u.getUnitFromId(Integer.parseInt(record[8]));
                            oCrop c = new oCrop(context);
                            l.crop = c.getCropFromId(Integer.parseInt(record[9]));
                            oTreatmentIngredient t = new oTreatmentIngredient(context);
                            l.treatmentIngredient = t.getTreatmentIngredientFromId(Integer.parseInt(record[10]));
                            l.cost = Float.parseFloat(record[11]);
                            l.comments = record[12];
                            l.picture = record[13];
                            l.sound = record[14];
                            l.sent = (record[15].equals("1"));
                            ret.add(l);
                    }
                }
                n++;
            }
        }
        return ret;
    }

    public ArrayList<oLog> createLog(int farmId, int userId, int mode){
        ArrayList<oLog> ret = new ArrayList<>();
        csvFileManager log;

        log = new csvFileManager("log");
        List<String[]> logCSV = log.read(context);
        if(logCSV!=null) {
            Iterator<String[]> iterator = logCSV.iterator();
            int n=0;
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                if(Integer.parseInt(record[0])==farmId && (Integer.parseInt(record[2])==userId || userId==-1)) {
                    oLog l = new oLog();
                    l.line=n;
                    l.farmId = Integer.parseInt(record[0]);
                    l.farmVersion = Integer.parseInt(record[1]);
                    l.userId = Integer.parseInt(record[2]);
                    l.plotId = Integer.parseInt(record[3]);
                    l.date = dH.stringToDate(record[4]);
                    oDataItem di = new oDataItem(context);
                    l.dataItem = di.getDataItemFromId(Integer.parseInt(record[5]));
                    switch(mode){
                        case 0:
                            if(l.dataItem!=null){
                                l.value = Float.parseFloat(record[6]);
                                oUnit u = new oUnit(context);
                                l.quantity = Float.parseFloat(record[7]);
                                l.units = u.getUnitFromId(Integer.parseInt(record[8]));
                                oCrop c = new oCrop(context);
                                l.crop = c.getCropFromId(Integer.parseInt(record[9]));
                                oTreatmentIngredient t = new oTreatmentIngredient(context);
                                l.treatmentIngredient = t.getTreatmentIngredientFromId(Integer.parseInt(record[10]));
                                l.cost = Float.parseFloat(record[11]);
                                l.comments = record[12];
                                l.sent = (record[15].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 1:
                            if(!record[13].isEmpty()){
                                l.picture = record[13];
                                l.sound = record[14];
                                l.sent = (record[15].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 2:
                            l.value = Float.parseFloat(record[6]);
                            oUnit u = new oUnit(context);
                            l.quantity = Float.parseFloat(record[7]);
                            l.units = u.getUnitFromId(Integer.parseInt(record[8]));
                            oCrop c = new oCrop(context);
                            l.crop = c.getCropFromId(Integer.parseInt(record[9]));
                            oTreatmentIngredient t = new oTreatmentIngredient(context);
                            l.treatmentIngredient = t.getTreatmentIngredientFromId(Integer.parseInt(record[10]));
                            l.cost = Float.parseFloat(record[11]);
                            l.comments = record[12];
                            l.picture = record[13];
                            l.sound = record[14];
                            l.sent = (record[15].equals("1"));
                            ret.add(l);
                    }
                }
                n++;
            }
        }
        return ret;
    }

    public ArrayList<oLog> createLog(int farmId, int version, int plot, int userId, int mode){
        ArrayList<oLog> ret = new ArrayList<>();
        csvFileManager log;

        log = new csvFileManager("log");
        List<String[]> logCSV = log.read(context);
        if(logCSV!=null) {
            Iterator<String[]> iterator = logCSV.iterator();
            int n=0;
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                if(Integer.parseInt(record[0])==farmId && Integer.parseInt(record[1])==version && (Integer.parseInt(record[2])==userId || userId==-1)
                        && (Integer.parseInt(record[3])==plot)) {
                    oLog l = new oLog();
                    l.line=n;
                    l.farmId = Integer.parseInt(record[0]);
                    l.farmVersion = Integer.parseInt(record[1]);
                    l.userId = Integer.parseInt(record[2]);
                    l.plotId = Integer.parseInt(record[3]);
                    l.date = dH.stringToDate(record[4]);
                    oDataItem di = new oDataItem(context);
                    l.dataItem = di.getDataItemFromId(Integer.parseInt(record[5]));
                    switch(mode){
                        case 0:
                            if(l.dataItem!=null){
                                l.value = Float.parseFloat(record[6]);
                                oUnit u = new oUnit(context);
                                l.quantity = Float.parseFloat(record[7]);
                                l.units = u.getUnitFromId(Integer.parseInt(record[8]));
                                oCrop c = new oCrop(context);
                                l.crop = c.getCropFromId(Integer.parseInt(record[9]));
                                oTreatmentIngredient t = new oTreatmentIngredient(context);
                                l.treatmentIngredient = t.getTreatmentIngredientFromId(Integer.parseInt(record[10]));
                                l.cost = Float.parseFloat(record[11]);
                                l.comments = record[12];
                                l.sent = (record[15].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 1:
                            if(!record[13].isEmpty()){
                                l.picture = record[13];
                                l.sound = record[14];
                                l.sent = (record[15].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 2:
                            l.value = Float.parseFloat(record[6]);
                            oUnit u = new oUnit(context);
                            l.quantity = Float.parseFloat(record[7]);
                            l.units = u.getUnitFromId(Integer.parseInt(record[8]));
                            oCrop c = new oCrop(context);
                            l.crop = c.getCropFromId(Integer.parseInt(record[9]));
                            oTreatmentIngredient t = new oTreatmentIngredient(context);
                            l.treatmentIngredient = t.getTreatmentIngredientFromId(Integer.parseInt(record[10]));
                            l.cost = Float.parseFloat(record[11]);
                            l.comments = record[12];
                            l.picture = record[13];
                            l.sound = record[14];
                            l.sent = (record[15].equals("1"));
                            ret.add(l);
                    }
                }
                n++;
            }
        }
        return ret;
    }

    public void appendToLog(int farmId, int farmVersion, int userId, int plot, Date date, oDataItem dataItem, float value, float quantity, oUnit units, oCrop crop, oTreatmentIngredient treatmentIngredient, float cost, String comments, String picture, String sound){
        dateHelper dH = new dateHelper();

        csvFileManager log = new csvFileManager("log");
        String dataItemId;
        String unitsId;
        String cropId;
        String treatmentId;
        dataItemId = (dataItem == null) ? "0" : Integer.toString(dataItem.id);
        unitsId = (units == null) ? "0" : Integer.toString(units.id);
        cropId = (crop == null) ? "0" : Integer.toString(crop.id);
        treatmentId = (treatmentIngredient == null) ? "0" : Integer.toString(treatmentIngredient.id);
        String[] newLine = {Integer.toString(farmId), Integer.toString(farmVersion), Integer.toString(userId), Integer.toString(plot), dH.dateToString(date), dataItemId, Float.toString(value), Float.toString(quantity), unitsId, cropId, treatmentId, Float.toString(cost), comments, picture, sound, "0"};

        log.append(context, newLine);
    }

    public void updateLogItem(int line, int farmId, int farmVersion, int userId, int plot, Date date, oDataItem dataItem, float value, float quantity, oUnit units, oCrop crop, oTreatmentIngredient treatment, float cost, String comments){
        dateHelper dH = new dateHelper();

        csvFileManager log = new csvFileManager("log");
        String dataItemId;
        String unitsId;
        String cropId;
        String treatmentId;
        dataItemId = (dataItem == null) ? "0" : Integer.toString(dataItem.id);
        unitsId = (units == null) ? "0" : Integer.toString(units.id);
        cropId = (crop == null) ? "0" : Integer.toString(crop.id);
        treatmentId = (treatment == null) ? "0" : Integer.toString(treatment.id);

        String[] newLine = {Integer.toString(farmId), Integer.toString(farmVersion), Integer.toString(userId), Integer.toString(plot), dH.dateToString(date), dataItemId, Float.toString(value), Float.toString(quantity), unitsId, cropId, treatmentId, Float.toString(cost), comments, "", "", "0"};
        log.update(context, newLine, line);
    }

    public void setSent(Context c){
        csvFileManager log = new csvFileManager("log");
        log.sent(c,line);
    }

    public ArrayList<String> deleteFarmItems(int[] farmList){
        ArrayList<String> ret = new ArrayList<>();
        ArrayList<oLog> log = createLog(2);
        int[] delete=new int[log.size()];
        Iterator<oLog> iterator = log.iterator();
        int n=0;
        while (iterator.hasNext()) {
            oLog l = iterator.next();
            boolean bFound=false;
            for(int i=0;i<farmList.length;i++){
                if(l.farmId==farmList[i]){
                    bFound=true;
                    ret.add(l.picture);
                    ret.add(l.sound);
                    break;
                }
            }
            if(bFound){
                delete[n]=l.line;
            } else {
                delete[n]=-1;
            }
            n++;
        }
        deleteLogItems(delete);
        return ret;
    }

    public void deleteLogItems(int[] delete){
        csvFileManager log = new csvFileManager("log");
        log.deleteLines(context, delete);
    }

    public ArrayList<oLog> sortLogByDate(ArrayList<oLog> sortedLog, boolean reverse, int limit){
        Collections.sort(sortedLog, new Comparator<oLog>() {
            @Override
            public int compare(oLog l1, oLog l2) {
                return l1.date.compareTo(l2.date);
            }
        });

        if(reverse){
            Collections.reverse(sortedLog);
        }

        if(limit>0 && limit<sortedLog.size()){
            sortedLog.subList(0,limit);
        }

        return sortedLog;
    }

    public String getDataItemName(Context c){
        String cropMarker = c.getString(R.string.cropMarker);
        String treatmentMarker = c.getString(R.string.treatmentMarker);
        String ret = (crop!=null) ? (dataItem.name.indexOf(cropMarker)>0) ? dataItem.name.replace(cropMarker,"("+crop.name+")") : dataItem.name + " ("+crop.name+")" : dataItem.name;
        ret = (treatmentIngredient !=null) ? (ret.indexOf(treatmentMarker)>0) ? ret.replace(treatmentMarker,"("+ treatmentIngredient.name+")") : ret + " ("+ treatmentIngredient.name+")" : ret;
        return ret;
    }

    public String toString(String separator, boolean isData){
        String ret="";
        dateHelper dH = new dateHelper();

        if(isData) {
            int dataItemId = (dataItem == null) ? -1 : dataItem.id;
            int unitsId = (units == null) ? -1 : units.id;
            int cropId = (crop == null) ? -1 : crop.id;
            int treatmentId = (treatmentIngredient == null) ? -1 : treatmentIngredient.id;
            String sendComments = (comments == null) ? "" : comments.replaceAll(separator, "_");

            try {
                sendComments = URLEncoder.encode(sendComments, "UTF-8");
            } catch (Exception e) {

            }
            ret = Integer.toString(farmId) + separator + Integer.toString(farmVersion) + separator + Integer.toString(userId) + separator + Integer.toString(plotId) + separator + dH.dateToString(date) + separator +
                    Integer.toString(dataItemId) + separator + Float.toString(value) + separator + Float.toString(quantity) + separator + Integer.toString(unitsId) + separator +
                    Integer.toString(cropId) + separator + Integer.toString(treatmentId) + separator + Float.toString(cost) + separator + sendComments;
        } else {
            ret = Integer.toString(farmId) + separator + Integer.toString(farmVersion) + separator + Integer.toString(userId) + separator + Integer.toString(plotId) + separator + dH.dateToString(date);
        }
        return ret;
    }

}
