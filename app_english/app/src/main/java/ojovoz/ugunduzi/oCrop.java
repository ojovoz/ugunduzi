package ojovoz.ugunduzi;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Eugenio on 21/03/2018.
 */
public class oCrop {

    public int id;
    public String name;
    public String variety;

    private Context context;

    oCrop(){

    }

    oCrop(Context c){
        context = c;
    }

    public ArrayList<oCrop> getCrops(){
        ArrayList<oCrop> ret = new ArrayList<>();
        csvFileManager cropList;

        cropList = new csvFileManager("crops");
        List<String[]> cropsCSV = cropList.read(context);
        if(cropsCSV!=null) {
            Iterator<String[]> iterator = cropsCSV.iterator();
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                oCrop c = new oCrop();
                c.id = Integer.parseInt(record[0]);
                c.name = record[1];
                ret.add(c);
            }
        }
        return ret;
    }

    public ArrayList<String> getCropNames(boolean bAddNone){
        ArrayList<String> ret = new ArrayList<>();
        ArrayList<oCrop> cropList = getCrops();

        if(bAddNone){
            ret.add(context.getString(R.string.textNone));
        }

        Iterator<oCrop> iterator = cropList.iterator();
        while(iterator.hasNext()){
            oCrop c = iterator.next();
            ret.add(c.name);
        }
        return ret;
    }

    public oCrop getCropFromId(int id){
        oCrop ret = new oCrop();
        if(id==0){
            ret=null;
        } else {
            ArrayList<oCrop> cropList = getCrops();
            Iterator<oCrop> iterator = cropList.iterator();
            while (iterator.hasNext()) {
                oCrop c = iterator.next();
                if (c.id == id) {
                    ret = c;
                    break;
                }
            }
        }
        return ret;
    }
}
