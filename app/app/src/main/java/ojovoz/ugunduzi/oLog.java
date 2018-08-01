package ojovoz.ugunduzi;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Eugenio on 13/04/2018.
 */
public class oLog {

    public int line;

    public String farmName; //delete
    public int farmId;
    public int userId;

    public int plotId;
    public Date date;
    public oDataItem dataItem;
    public float quantity;
    public float value;
    public oUnit units;

    public oCrop crop;
    public oTreatment treatment;

    public String picture;
    public String sound;

    public boolean sent;

    private Context context;

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
                l.farmName = record[0];
                l.userId = Integer.parseInt(record[1]);
                l.plotId = Integer.parseInt(record[2]);
                l.date = dH.stringToDate(record[3]);
                oDataItem di = new oDataItem(context);
                l.dataItem = di.getDataItemFromId(Integer.parseInt(record[4]));
                switch(mode){
                    case 0:
                        if(l.dataItem!=null){
                            l.value = Float.parseFloat(record[5]);
                            oUnit u = new oUnit(context);
                            l.units = u.getUnitFromId(Integer.parseInt(record[6]));
                            oCrop c = new oCrop(context);
                            l.crop = c.getCropFromId(Integer.parseInt(record[7]));
                            oTreatment t = new oTreatment(context);
                            l.treatment = t.getTreatmentFromId(Integer.parseInt(record[8]));
                            l.sent = (record[1].equals("1"));
                            ret.add(l);
                        }
                        break;
                    case 1:
                        if(!record[9].isEmpty()){
                            l.picture = record[9];
                            l.sound = record[10];
                            l.sent = (record[1].equals("1"));
                            ret.add(l);
                        }
                        break;
                    case 2:
                        l.value = Float.parseFloat(record[5]);
                        oUnit u = new oUnit(context);
                        l.units = u.getUnitFromId(Integer.parseInt(record[6]));
                        oCrop c = new oCrop(context);
                        l.crop = c.getCropFromId(Integer.parseInt(record[7]));
                        oTreatment t = new oTreatment(context);
                        l.treatment = t.getTreatmentFromId(Integer.parseInt(record[8]));
                        l.picture = record[9];
                        l.sound = record[10];
                        l.sent = (record[11].equals("1"));
                        ret.add(l);
                }
                n++;
            }
        }
        return ret;
    }

    public ArrayList<oLog> createLog(int userId, int mode){
        ArrayList<oLog> ret = new ArrayList<>();
        csvFileManager log;

        log = new csvFileManager("log");
        List<String[]> logCSV = log.read(context);
        if(logCSV!=null) {
            Iterator<String[]> iterator = logCSV.iterator();
            int n=0;
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                if(Integer.parseInt(record[1])==userId) {
                    oLog l = new oLog();
                    l.line=n;
                    l.farmName = record[0];
                    l.userId = Integer.parseInt(record[1]);
                    l.plotId = Integer.parseInt(record[2]);
                    l.date = dH.stringToDate(record[3]);
                    oDataItem di = new oDataItem(context);
                    l.dataItem = di.getDataItemFromId(Integer.parseInt(record[4]));
                    switch(mode){
                        case 0:
                            if(l.dataItem!=null){
                                l.value = Float.parseFloat(record[5]);
                                oUnit u = new oUnit(context);
                                l.units = u.getUnitFromId(Integer.parseInt(record[6]));
                                oCrop c = new oCrop(context);
                                l.crop = c.getCropFromId(Integer.parseInt(record[7]));
                                oTreatment t = new oTreatment(context);
                                l.treatment = t.getTreatmentFromId(Integer.parseInt(record[8]));
                                l.sent = (record[11].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 1:
                            if(!record[9].isEmpty()){
                                l.picture = record[9];
                                l.sound = record[10];
                                l.sent = (record[11].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 2:
                            l.value = Float.parseFloat(record[5]);
                            oUnit u = new oUnit(context);
                            l.units = u.getUnitFromId(Integer.parseInt(record[6]));
                            oCrop c = new oCrop(context);
                            l.crop = c.getCropFromId(Integer.parseInt(record[7]));
                            oTreatment t = new oTreatment(context);
                            l.treatment = t.getTreatmentFromId(Integer.parseInt(record[8]));
                            l.picture = record[9];
                            l.sound = record[10];
                            l.sent = (record[11].equals("1"));
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
                if(Integer.parseInt(record[0])==(farmId) && (Integer.parseInt(record[1])==userId || userId==-1)) {
                    oLog l = new oLog();
                    l.line=n;
                    l.farmName = record[0];
                    l.userId = Integer.parseInt(record[1]);
                    l.plotId = Integer.parseInt(record[2]);
                    l.date = dH.stringToDate(record[3]);
                    oDataItem di = new oDataItem(context);
                    l.dataItem = di.getDataItemFromId(Integer.parseInt(record[4]));
                    switch(mode){
                        case 0:
                            if(l.dataItem!=null){
                                l.value = Float.parseFloat(record[5]);
                                oUnit u = new oUnit(context);
                                l.units = u.getUnitFromId(Integer.parseInt(record[6]));
                                oCrop c = new oCrop(context);
                                l.crop = c.getCropFromId(Integer.parseInt(record[7]));
                                oTreatment t = new oTreatment(context);
                                l.treatment = t.getTreatmentFromId(Integer.parseInt(record[8]));
                                l.sent = (record[11].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 1:
                            if(!record[9].isEmpty()){
                                l.picture = record[9];
                                l.sound = record[10];
                                l.sent = (record[11].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 2:
                            l.value = Float.parseFloat(record[5]);
                            oUnit u = new oUnit(context);
                            l.units = u.getUnitFromId(Integer.parseInt(record[6]));
                            oCrop c = new oCrop(context);
                            l.crop = c.getCropFromId(Integer.parseInt(record[7]));
                            oTreatment t = new oTreatment(context);
                            l.treatment = t.getTreatmentFromId(Integer.parseInt(record[8]));
                            l.picture = record[9];
                            l.sound = record[10];
                            l.sent = (record[11].equals("1"));
                            ret.add(l);
                    }
                }
                n++;
            }
        }
        return ret;
    }

    /*

    public ArrayList<oLog> createLog(String fName, int userId, int mode){
        ArrayList<oLog> ret = new ArrayList<>();
        csvFileManager log;

        log = new csvFileManager("log");
        List<String[]> logCSV = log.read(context);
        if(logCSV!=null) {
            Iterator<String[]> iterator = logCSV.iterator();
            int n=0;
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                if(record[0].equals(fName) && Integer.parseInt(record[1])==userId) {
                    oLog l = new oLog();
                    l.line=n;
                    l.farmName = record[0];
                    l.userId = Integer.parseInt(record[1]);
                    l.plotId = Integer.parseInt(record[2]);
                    l.date = dH.stringToDate(record[3]);
                    oDataItem di = new oDataItem(context);
                    l.dataItem = di.getDataItemFromId(Integer.parseInt(record[4]));
                    switch(mode){
                        case 0:
                            if(l.dataItem!=null){
                                l.value = Float.parseFloat(record[5]);
                                oUnit u = new oUnit(context);
                                l.units = u.getUnitFromId(Integer.parseInt(record[6]));
                                oCrop c = new oCrop(context);
                                l.crop = c.getCropFromId(Integer.parseInt(record[7]));
                                oTreatment t = new oTreatment(context);
                                l.treatment = t.getTreatmentFromId(Integer.parseInt(record[8]));
                                l.sent = (record[11].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 1:
                            if(!record[9].isEmpty()){
                                l.picture = record[9];
                                l.sound = record[10];
                                l.sent = (record[11].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 2:
                            l.value = Float.parseFloat(record[5]);
                            oUnit u = new oUnit(context);
                            l.units = u.getUnitFromId(Integer.parseInt(record[6]));
                            oCrop c = new oCrop(context);
                            l.crop = c.getCropFromId(Integer.parseInt(record[7]));
                            oTreatment t = new oTreatment(context);
                            l.treatment = t.getTreatmentFromId(Integer.parseInt(record[8]));
                            l.picture = record[9];
                            l.sound = record[10];
                            l.sent = (record[11].equals("1"));
                            ret.add(l);
                    }
                }
                n++;
            }
        }
        return ret;
    }

    */

    public ArrayList<oLog> createLog(String fName, int userId, int plot, int mode){

        //mode: 0=data only; 1=picture + sound only; 2=both

        ArrayList<oLog> ret = new ArrayList<>();
        csvFileManager log;

        log = new csvFileManager("log");
        List<String[]> logCSV = log.read(context);
        if(logCSV!=null) {
            Iterator<String[]> iterator = logCSV.iterator();
            int n=0;
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                if(record[0].equals(fName) && (Integer.parseInt(record[1])==userId) && (Integer.parseInt(record[2])==plot)) {
                    oLog l = new oLog();
                    l.line=n;
                    l.farmName = record[0];
                    l.userId = Integer.parseInt(record[1]);
                    l.plotId = Integer.parseInt(record[2]);
                    l.date = dH.stringToDate(record[3]);
                    oDataItem di = new oDataItem(context);
                    l.dataItem = di.getDataItemFromId(Integer.parseInt(record[4]));
                    switch(mode){
                        case 0:
                            if(l.dataItem!=null){
                                l.value = Float.parseFloat(record[5]);
                                oUnit u = new oUnit(context);
                                l.units = u.getUnitFromId(Integer.parseInt(record[6]));
                                oCrop c = new oCrop(context);
                                l.crop = c.getCropFromId(Integer.parseInt(record[7]));
                                oTreatment t = new oTreatment(context);
                                l.treatment = t.getTreatmentFromId(Integer.parseInt(record[8]));
                                l.sent = (record[11].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 1:
                            if(!record[9].isEmpty()){
                                l.picture = record[9];
                                l.sound = record[10];
                                l.sent = (record[11].equals("1"));
                                ret.add(l);
                            }
                            break;
                        case 2:
                            l.value = Float.parseFloat(record[5]);
                            oUnit u = new oUnit(context);
                            l.units = u.getUnitFromId(Integer.parseInt(record[6]));
                            oCrop c = new oCrop(context);
                            l.crop = c.getCropFromId(Integer.parseInt(record[7]));
                            oTreatment t = new oTreatment(context);
                            l.treatment = t.getTreatmentFromId(Integer.parseInt(record[8]));
                            l.picture = record[9];
                            l.sound = record[10];
                            l.sent = (record[11].equals("1"));
                            ret.add(l);
                    }
                }
                n++;
            }
        }
        return ret;
    }

    public void appendToLog(String farmName, int userId, int plot, Date date, oDataItem dataItem, float value, oUnit units, oCrop crop, oTreatment treatment, String picture, String sound){
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
        String[] newLine = {farmName, Integer.toString(userId), Integer.toString(plot), dH.dateToString(date), dataItemId, Float.toString(value), unitsId, cropId, treatmentId, picture, sound, "0"};

        log.append(context, newLine);
    }

    public void updateLogItem(int line, String farmName, int userId, int plot, Date date, oDataItem dataItem, float value, oUnit units, oCrop crop, oTreatment treatment){
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

        String[] newLine = {farmName, Integer.toString(userId), Integer.toString(plot), dH.dateToString(date), dataItemId, Float.toString(value), unitsId, cropId, treatmentId, "", "", "0"};
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
                delete[n]=n;
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
        ret = (treatment!=null) ? (ret.indexOf(treatmentMarker)>0) ? ret.replace(treatmentMarker,"("+treatment.name+")") : ret + " ("+treatment.name+")" : ret;
        return ret;
    }

    public String toString(String separator){
        String ret="";
        dateHelper dH = new dateHelper();
        int dataItemId = (dataItem==null) ? -1 : dataItem.id;
        int unitsId = (units==null) ? -1 : units.id;
        int cropId = (crop==null) ? -1 : crop.id;
        int treatmentId = (treatment==null) ? -1 : treatment.id;
        ret=farmName+separator+Integer.toString(userId)+separator+Integer.toString(plotId)+separator+dH.dateToString(date)+separator+
                Integer.toString(dataItemId)+separator+Float.toString(value)+separator+Integer.toString(unitsId)+separator+
                Integer.toString(cropId)+separator+Integer.toString(treatmentId);
        return ret;
    }

}
