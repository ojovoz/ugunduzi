package ojovoz.ugunduzi;

import android.content.Context;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Eugenio on 08/03/2018.
 */
public class oUser {
    public String userAlias;
    public String userPassword;

    private csvFileManager userList;
    private Context context;

    oUser(Context rContext, String rUserAlias, String rUserPassword){
        userAlias=rUserAlias;
        userPassword=rUserPassword;

        context=rContext;

        userList = new csvFileManager("users");
    }

    oUser(Context rContext){
        context=rContext;
        userList = new csvFileManager("users");
    }

    public int getUserIdFromAliasPass(){
        int ret=0;
        List<String[]> usersCSV = userList.read(context);
        if(usersCSV!=null) {
            Iterator<String[]> iterator = usersCSV.iterator();
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                if(userAlias.equals(record[1]) && userPassword.equals(record[2])){
                    ret=Integer.parseInt(record[0]);
                    break;
                } else if(userAlias.equals(record[1]) && !userPassword.equals(record[2])){
                    ret=-1;
                    break;
                }
            }
        }
        return ret;
    }

    public String getAllUserNames(){
        String ret="";
        List<String[]> usersCSV = userList.read(context);
        if(usersCSV!=null) {
            Iterator<String[]> iterator = usersCSV.iterator();
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                if(ret.isEmpty()){
                    ret=record[1];
                } else {
                    ret+=","+record[1];
                }
            }
        }
        return ret;
    }

    public void addNewUser(int rId, String rUserAlias, String rUserPassword){
        String[] newLine = {Integer.toString(rId), rUserAlias, rUserPassword};
        userList.append(context, newLine);
    }

}
