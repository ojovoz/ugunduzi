package ojovoz.ugunduzi;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Eugenio on 30/07/2018.
 */
public class oFarm {

    public int userId;
    public String name;
    public float size;
    public Date dateCreated;
    public String plotMatrix;
    public int version;
    public int status; // 0 = not saved remotely, 1 = not deleted remotely, 2 = ok

    private dateHelper dH;
    private Context context;

    oFarm(){
        dH = new dateHelper();
        status=0;
    }

    oFarm(Context c){
        dH = new dateHelper();
        context=c;
        status=0;
    }

    public ArrayList<oFarm> getFarms(int userId, String farmName){
        ArrayList<oFarm> ret = new ArrayList<>();

        csvFileManager farms;

        farms = new csvFileManager("farms");
        List<String[]> farmsCSV = farms.read(context);
        if(farmsCSV!=null) {
            Iterator<String[]> iterator = farmsCSV.iterator();
            while (iterator.hasNext()) {
                String[] record = iterator.next();

                oFarm f = new oFarm();
                f.userId = Integer.parseInt(record[0]);
                f.name = record[1];
                f.size = Float.parseFloat(record[2]);
                f.dateCreated = dH.stringToDate(record[3]);
                f.plotMatrix = record[4];
                f.version = Integer.parseInt(record[5]);
                f.status = Integer.parseInt(record[6]);

                if((userId==-1 && farmName.isEmpty())||(f.userId==userId && f.name.equals(farmName))){
                    ret.add(f);
                }
            }
        }

        return ret;
    }

    public ArrayList<oFarm> getActiveFarms(int userId){
        ArrayList<oFarm> ret = new ArrayList<>();

        csvFileManager farms;

        farms = new csvFileManager("farms");
        List<String[]> farmsCSV = farms.read(context);
        if(farmsCSV!=null) {
            Iterator<String[]> iterator = farmsCSV.iterator();
            while (iterator.hasNext()) {
                String[] record = iterator.next();

                oFarm f = new oFarm();
                f.userId = Integer.parseInt(record[0]);
                f.name = record[1];
                f.size = Float.parseFloat(record[2]);
                f.dateCreated = dH.stringToDate(record[3]);
                f.plotMatrix = record[4];
                f.version = Integer.parseInt(record[5]);
                f.status = Integer.parseInt(record[6]);

                if(f.userId==userId &&f.status!=1){
                    ret.add(f);
                }
            }
        }

        return ret;
    }

    public oFarm getLatestActiveVersion(int userId, String farmName){
        oFarm ret = null;
        ArrayList<oFarm> farms = getFarms(userId, farmName);

        int maxVersion=-1;
        Iterator<oFarm> iterator = farms.iterator();
        while(iterator.hasNext()){
            oFarm f = iterator.next();
            if(f.version>maxVersion && f.status!=1){
                ret=f;
                maxVersion=f.version;
            }
        }
        return ret;
    }

    public void addNewFarm(int userId, String farmName, float size, Date date, String plotMatrix, int version){
        dateHelper dH = new dateHelper();

        csvFileManager farms = new csvFileManager("farms");
        String[] newLine = {Integer.toString(userId), farmName, Float.toString(size), dH.dateToString(date), plotMatrix, Integer.toString(version)};

        farms.append(context, newLine);
    }

    public int[] getFarmLineList(int userId, String farmName){
        ArrayList<oFarm> farms = getFarms(-1, "");
        int[] delete=new int[farms.size()];

        Iterator<oFarm> iterator = farms.iterator();
        int n=0;
        while(iterator.hasNext()){
            oFarm f = iterator.next();
            if(f.userId==userId && f.name.equals(farmName)){
                delete[n]=n;
            } else {
                delete[n]=-1;
            }
            n++;
        }
        return delete;
    }

    public void doDeleteFarm(int[] delete){
        csvFileManager farms = new csvFileManager("farms");
        farms.deleteLines(context, delete);
    }

    public void updateFarmstatus(int[] delete, int status) {
        dateHelper dH = new dateHelper();
        csvFileManager farms = new csvFileManager("farms");
        farms.updateStatus(context, delete, status);
    }

}
