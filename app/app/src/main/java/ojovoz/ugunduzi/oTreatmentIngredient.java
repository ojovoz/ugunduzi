package ojovoz.ugunduzi;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Eugenio on 23/07/2018.
 */
public class oTreatmentIngredient {

    public int id;
    public String name;

    private Context context;

    oTreatmentIngredient(){

    }

    oTreatmentIngredient(Context c){
        context = c;
    }

    public ArrayList<oTreatmentIngredient> getTreatmentIngredients(int category){
        //category: -1=all, 0=pest control, 1=soil management
        ArrayList<oTreatmentIngredient> ret = new ArrayList<>();
        csvFileManager treatmentIngredientList;

        treatmentIngredientList = new csvFileManager("treatment_ingredients");
        List<String[]> treatmentIngredientsCSV = treatmentIngredientList.read(context);
        if(treatmentIngredientsCSV!=null) {
            Iterator<String[]> iterator = treatmentIngredientsCSV.iterator();
            while (iterator.hasNext()) {
                String[] record = iterator.next();

                oTreatmentIngredient ti = new oTreatmentIngredient();
                oTreatment t = new oTreatment(context);
                int treatmentId = Integer.parseInt(record[1]);

                if(category==-1 || t.getTreatmentCategoryFromId(treatmentId)==category){
                    ti.id = Integer.parseInt(record[0]);
                    ti.name = record[2];
                    ret.add(ti);
                }
            }
        }
        return ret;
    }

    public ArrayList<String> getTreatmentIngredientNames(ArrayList<oTreatmentIngredient> list){
        ArrayList<String> ret = new ArrayList<>();

        Iterator<oTreatmentIngredient> iterator = list.iterator();
        while(iterator.hasNext()){
            oTreatmentIngredient t = iterator.next();
            ret.add(t.name);
        }
        return ret;
    }

    public oTreatmentIngredient getTreatmentIngredientFromId(int id){
        oTreatmentIngredient ret = new oTreatmentIngredient();
        if(id==0){
            ret=null;
        } else {
            ArrayList<oTreatmentIngredient> ingredientList = getTreatmentIngredients(-1);
            Iterator<oTreatmentIngredient> iterator = ingredientList.iterator();
            while (iterator.hasNext()) {
                oTreatmentIngredient t = iterator.next();
                if (t.id == id) {
                    ret = t;
                    break;
                }
            }
        }
        return ret;
    }
}
