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

    public int id;
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

    public ArrayList<oFarm> getFarms(int userId, int id){
        ArrayList<oFarm> ret = new ArrayList<>();

        csvFileManager farms;

        farms = new csvFileManager("farms");
        List<String[]> farmsCSV = farms.read(context);
        if(farmsCSV!=null) {
            Iterator<String[]> iterator = farmsCSV.iterator();
            while (iterator.hasNext()) {
                String[] record = iterator.next();

                oFarm f = new oFarm();
                f.id = Integer.parseInt(record[0]);
                f.userId = Integer.parseInt(record[1]);
                f.name = record[2];
                f.size = Float.parseFloat(record[3]);
                f.dateCreated = dH.stringToDate(record[4]);
                f.plotMatrix = record[5];
                f.version = Integer.parseInt(record[6]);
                f.status = Integer.parseInt(record[7]);

                if((userId==-1 && id==-1)||(f.userId==userId && id==-1)||(userId==-1 && f.id==id)||(f.userId==userId && f.id==id)){
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
                f.id = Integer.parseInt(record[0]);
                f.userId = Integer.parseInt(record[1]);
                f.name = record[2];
                f.size = Float.parseFloat(record[3]);
                f.dateCreated = dH.stringToDate(record[4]);
                f.plotMatrix = record[5];
                f.version = Integer.parseInt(record[6]);
                f.status = Integer.parseInt(record[7]);

                if(f.userId==userId &&f.status!=1){
                    ret.add(f);
                }
            }
        }

        return ret;
    }

    public ArrayList<String> getActiveFarmNames(int userId){
        ArrayList<String> ret = new ArrayList<>();
        ArrayList<oFarm> fList = getActiveFarms(userId);
        Iterator<oFarm> iterator = fList.iterator();
        while(iterator.hasNext()){
            oFarm f = iterator.next();
            ret.add(f.name);
        }
        return ret;
    }

    public boolean farmNameExists(int userId, String farmName){
        ArrayList<String> fNames = getActiveFarmNames(userId);
        return fNames.contains(farmName);
    }

    public int getFarmIdFromNameUser(int userId, String farmName){
        int ret=-1;
        int maxId=-1;
        ArrayList<oFarm> fList = getActiveFarms(userId);
        Iterator<oFarm> iterator = fList.iterator();
        while(iterator.hasNext()){
            oFarm f = iterator.next();
            if(f.name.equals(farmName)){
                ret=f.id;
                break;
            } else {
                if(f.id>maxId){
                    maxId=f.id;
                }
            }
        }
        if(ret==-1) ret=maxId+1;
        return ret;
    }

    public oFarm getLatestActiveVersion(int userId, int id, Context c){
        oFarm ret = null;
        ArrayList<oFarm> farms = getFarms(userId, id);

        int maxVersion=-1;
        Iterator<oFarm> iterator = farms.iterator();
        while(iterator.hasNext()){
            oFarm f = iterator.next();
            if(f.version>maxVersion && f.status!=1){
                ret=f;
                maxVersion=f.version;
            }
        }

        if(ret!=null){
            ret.context=c;
        }
        return ret;
    }

    public int getNumberOfFarms(int userId){
        ArrayList<oFarm> farms = getActiveFarms(userId);
        return farms.size();
    }

    public void addNewFarm(int id, int userId, String farmName, float size, Date date, String plotMatrix, int version, int status){
        dateHelper dH = new dateHelper();

        csvFileManager farms = new csvFileManager("farms");
        String[] newLine = {Integer.toString(id), Integer.toString(userId), farmName, Float.toString(size), dH.dateToString(date), plotMatrix, Integer.toString(version), Integer.toString(status)};

        farms.append(context, newLine);
    }

    public void updateFarm(int id, int userId, String farmName, float size, Date date, String plotMatrix, int version, int status){
        dateHelper dH = new dateHelper();

        csvFileManager farms = new csvFileManager("farms");
        List<String[]> lines = farms.read(context);

        if(lines!=null) {
            Iterator<String[]> iterator = lines.iterator();
            int line = 0;
            while (iterator.hasNext()) {
                String[] thisLine = iterator.next();
                if(Integer.valueOf(thisLine[0])==id && Integer.valueOf(thisLine[6])==version){
                    break;
                }
                line++;
            }

            String[] newLine = {Integer.toString(id), Integer.toString(userId), farmName, Float.toString(size), dH.dateToString(date), plotMatrix, Integer.toString(version), Integer.toString(status)};
            farms.update(context, newLine, line);
        }
    }

    public int[] getFarmLineList(int userId, int id){
        ArrayList<oFarm> farms = getFarms(-1, -1);
        int[] delete=new int[farms.size()];

        Iterator<oFarm> iterator = farms.iterator();
        int n=0;
        while(iterator.hasNext()){
            oFarm f = iterator.next();
            if(f.userId==userId && f.id==id){
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
        csvFileManager farms = new csvFileManager("farms");
        farms.updateStatus(context, delete, status);
    }

    public boolean hasRecords(){
        oLog l = new oLog(context);
        ArrayList<oLog> logList = l.createLog(id,-1,0);
        return (logList.size()>0);
    }

}
