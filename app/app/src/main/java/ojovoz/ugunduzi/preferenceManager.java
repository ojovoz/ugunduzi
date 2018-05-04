package ojovoz.ugunduzi;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Eugenio on 08/03/2018.
 */
public class preferenceManager {

    Context context;

    preferenceManager(Context c){
        context=c;
    }

    public String getPreference(String keyName) {
        String value = "";
        SharedPreferences ugunduziPrefs = context.getSharedPreferences("ugunduziPrefs", Context.MODE_PRIVATE);
        value = ugunduziPrefs.getString(keyName, "");
        return value;
    }

    public int getPreferenceInt(String keyName) {
        int value = -1;
        SharedPreferences ugunduziPrefs = context.getSharedPreferences("ugunduziPrefs", Context.MODE_PRIVATE);
        value = ugunduziPrefs.getInt(keyName, -1);
        return value;
    }

    public boolean preferenceExists(String keyName){
        boolean ret=false;
        SharedPreferences ugunduziPrefs = context.getSharedPreferences("ugunduziPrefs", Context.MODE_PRIVATE);
        ret = ugunduziPrefs.contains(keyName);
        return ret;
    }

    public boolean getPreferenceBoolean(String keyName) {
        boolean value;
        SharedPreferences ugunduziPrefs = context.getSharedPreferences("ugunduziPrefs", Context.MODE_PRIVATE);
        value = ugunduziPrefs.getBoolean(keyName, false);
        return value;
    }

    public ArrayList<String> getPreferenceAsArrayList(String keyName, String separator, String prefixExcluded) {
        ArrayList<String> ret = new ArrayList<>();
        String list = getPreference(keyName);
        if(!list.isEmpty()) {
            String valuesArray[] = list.split(separator);
            for (int i = 0; i < valuesArray.length; i++) {
                if (!prefixExcluded.isEmpty()) {
                    if (prefixExcluded.charAt(0) != valuesArray[i].charAt(0)) {
                        ret.add(valuesArray[i]);
                    }
                } else {
                    ret.add(valuesArray[i]);
                }
            }
        }
        return ret;
    }

    public String getFarmDate(String keyName, String separator){
        String ret="";
        ArrayList<String> farmList = getPreferenceAsArrayList(keyName,separator,"");
        if(farmList!=null){
            if(farmList.size()>1){
                ret=farmList.get(1);
            }
        }
        return ret;
    }

    public void savePreference(String keyName, String keyValue) {
        SharedPreferences ugunduziPrefs = context.getSharedPreferences("ugunduziPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = ugunduziPrefs.edit();
        prefEditor.putString(keyName, keyValue);
        prefEditor.apply();
    }

    public void savePreferenceBoolean(String keyName, boolean keyValue){
        SharedPreferences ugunduziPrefs = context.getSharedPreferences("ugunduziPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = ugunduziPrefs.edit();
        prefEditor.putBoolean(keyName, keyValue);
        prefEditor.apply();
    }

    public void savePreferenceInt(String keyName, int keyValue){
        SharedPreferences ugunduziPrefs = context.getSharedPreferences("ugunduziPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = ugunduziPrefs.edit();
        prefEditor.putInt(keyName, keyValue);
        prefEditor.apply();
    }

    public boolean valueExistsInList(String keyName, String value, String separator){
        boolean ret=false;
        String allValues=getPreference(keyName);
        if(!allValues.isEmpty()) {
            String valuesArray[] = allValues.split(separator);
            for(int i=0;i<valuesArray.length;i++) {
                if(valuesArray[i].equals(value)){
                    ret=true;
                    break;
                }
            }
        }
        return ret;
    }

    public void appendIfNewValue(String keyName, String value, String separator){
        String allValues=getPreference(keyName);
        if(!allValues.isEmpty()) {
            if(!valueExistsInList(keyName, value, separator)){
                String newValues = allValues + separator + value;
                savePreference(keyName, newValues);
            }
        } else {
            savePreference(keyName, value);
        }
    }

    public int getNumberOfValueItems(String keyName, String separator){
        int ret=0;
        String allValues=getPreference(keyName);
        if(!allValues.isEmpty()) {
            ret = allValues.split(separator).length;
        }
        return ret;
    }

    public boolean farmExists(String keyName, String value, String separator){
        boolean ret=false;
        if(valueExistsInList(keyName,value,separator) || valueExistsInList(keyName,"*"+value,separator)){
            ret=true;
        }
        return ret;
    }

    public void deletePreference(String keyName){
        SharedPreferences ugunduziPrefs = context.getSharedPreferences("ugunduziPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = ugunduziPrefs.edit();
        prefEditor.remove(keyName);
        prefEditor.apply();
    }

    public void markFarmsAsDeleted(String user, String farmsCSV, String separator){
        String[] deleteFarmList = farmsCSV.split(";");
        String[] currentFarmList = getPreference(user + "_farms").split(separator);
        String newFarms="";
        for(int i=0;i<currentFarmList.length;i++) {
            boolean bFound=false;
            for(int j=0;j<deleteFarmList.length;j++){
                String deleteFarm = deleteFarmList[j];
                String currentFarm = currentFarmList[i];
                if(currentFarm.equals(deleteFarm)){
                    bFound=true;
                    break;
                }
            }
            if(!bFound){
                newFarms = (newFarms.isEmpty()) ? currentFarmList[i] : newFarms + separator + currentFarmList[i];
            } else {
                newFarms = (newFarms.isEmpty()) ? "-" + currentFarmList[i] : newFarms + separator + "-" + currentFarmList[i];
                deletePreference(user + "_" + currentFarmList[i]);
            }
        }
        savePreference(user + "_farms", newFarms);
    }

    public void deleteFarms(String user, String farmsCSV, String separator){
        String[] deleteFarmList = farmsCSV.split(";");
        String[] currentFarmList = getPreference(user + "_farms").split(separator);
        String newFarms="";
        for(int i=0;i<currentFarmList.length;i++) {
            boolean bFound=false;
            for(int j=0;j<deleteFarmList.length;j++){
                String deleteFarm = deleteFarmList[j];
                if(currentFarmList[i].equals(deleteFarm)){
                    bFound=true;
                    break;
                }
            }
            if(!bFound){
                newFarms = (newFarms.isEmpty()) ? currentFarmList[i] : newFarms + separator + currentFarmList[i];
            } else {
                deletePreference(user + "_" + currentFarmList[i]);
            }
        }
        savePreference(user + "_farms", newFarms);
    }

    public String getActiveFarms(String user, String separator){
        String ret="";
        if(preferenceExists(user + "_farms")){
            String[] userFarms = getPreference(user + "_farms").split(separator);
            for(int i=0;i<userFarms.length;i++){
                if(!userFarms[i].startsWith("-")){
                    ret = (ret.isEmpty()) ? userFarms[i] : ret + separator + userFarms[i];
                }
            }
        }
        return ret;
    }

    public int getNumberOfActiveFarms(String user, String separator){
        int ret = getActiveFarms(user,separator).split(separator).length;
        return ret;
    }

    public ArrayList<String> getFarmsPendingSave(String keyName, String separator){
        ArrayList<String> ret = new ArrayList<>();
        ArrayList<String> current = getPreferenceAsArrayList(keyName, separator, "");
        Iterator<String> farmIterator = current.iterator();
        while (farmIterator.hasNext()) {
            String farmName = farmIterator.next();
            if(farmName.startsWith("*")){
                ret.add(farmName.substring(1));
            }
        }
        return ret;
    }

    public void updateSavedFarm(String updateFarmName, String keyName, String separator){
        String farms="";
        ArrayList<String> current = getPreferenceAsArrayList(keyName, separator, "");
        Iterator<String> farmIterator = current.iterator();
        while (farmIterator.hasNext()) {
            String farmName = farmIterator.next();
            if(farmName.startsWith("*") && updateFarmName.equals(farmName.substring(1))){
                farms = (farms.isEmpty()) ? updateFarmName : farms + separator + updateFarmName;
            } else {
                farms = (farms.isEmpty()) ? farmName : farms + separator + farmName;
            }
        }
        savePreference(keyName, farms);
    }

    public String getFarmsPendingDelete(String keyName, String separator){
        String farms="";
        String ret = "";
        ArrayList<String> current = getPreferenceAsArrayList(keyName, separator, "");
        Iterator<String> farmIterator = current.iterator();
        while (farmIterator.hasNext()) {
            String farmName = farmIterator.next();
            if(farmName.startsWith("-*")){
                //ignore
            } else {
                if (farmName.startsWith("-")) {
                    ret = (ret.isEmpty()) ? farmName.substring(1) : ret + separator + farmName.substring(1);
                }
                farms = (farms.isEmpty()) ? farmName : farms + separator + farmName;
            }
        }
        savePreference(keyName, farms);
        return ret;
    }

    public void updateDeletedFarms(String updateFarmNames, String keyName, String separator){
        String farms="";
        String[] updateFarmNamesList = updateFarmNames.split(separator);
        ArrayList<String> current = getPreferenceAsArrayList(keyName, separator, "");
        Iterator<String> farmIterator = current.iterator();
        while (farmIterator.hasNext()) {
            String farmName = farmIterator.next();
            String updateFarm="";
            for(int i=0; i<updateFarmNamesList.length;i++) {
                if (farmName.startsWith("-") && updateFarmNamesList[i].equals(farmName.substring(1))) {
                    updateFarm = updateFarmNamesList[i];
                    break;
                }
            }
            if(!updateFarm.isEmpty()){
                farms = (farms.isEmpty()) ? updateFarm : farms + separator + updateFarm;
            } else {
                farms = (farms.isEmpty()) ? farmName : farms + separator + farmName;
            }
        }
        savePreference(keyName, farms);
    }
}