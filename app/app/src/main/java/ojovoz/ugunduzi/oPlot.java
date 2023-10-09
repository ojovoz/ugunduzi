package ojovoz.ugunduzi;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Eugenio on 15/03/2018.
 */
public class oPlot {

    public int id;

    public int x;
    public int y;
    public float w;
    public float h;
    public float size;

    public int state; // 0 = default; 1 = touched; 2 = moving; 3 = resizing; 4 = editing contents; 5 = choosing action

    public int iMoveX;
    public int iMoveY;
    public int iMoveW;
    public int iMoveH;
    public int iResizeX;
    public int iResizeY;
    public int iResizeW;
    public int iResizeH;
    public int iContentsX;
    public int iContentsY;
    public int iContentsW;
    public int iContentsH;
    public int iActionsX;
    public int iActionsY;
    public int iActionsW;
    public int iActionsH;

    public ArrayList<oCrop> crops;
    public ArrayList<oTreatmentIngredient> pestControlIngredients;
    public ArrayList<oTreatmentIngredient> soilManagementIngredients;

    oPlot(){
        state=0;
        size=0;

        crops = new ArrayList<>();
        pestControlIngredients = new ArrayList<>();
        soilManagementIngredients = new ArrayList<>();
    }

    oPlot(int rX, int rY, int rW, int rH){
        x=rX;
        y=rY;
        w=rW;
        h=rH;

        state=0;
        size=0;

        crops = new ArrayList<>();
        pestControlIngredients = new ArrayList<>();
        soilManagementIngredients = new ArrayList<>();
    }

    public void addAreas(int rIResizeW, int rIResizeH, int rIContentsW, int rIContentsH, int rIActionsW, int rIActionsH){
        iMoveW = (int)w;
        iMoveH = (int)h-(rIContentsH+rIResizeH);
        iResizeW = rIResizeW;
        iResizeH = rIResizeH;
        iContentsW = rIContentsW;
        iContentsH = rIContentsH;
        iActionsW = rIActionsW;
        iActionsH = rIActionsH;
        calculateAreasXY();
    }

    public void calculateAreasXY(){
        iMoveX = (int)(x+(w/2))-(iMoveW/2);
        iMoveY = (int)(y+(h/2)-(iMoveH/2));
        iResizeX = (int)((w+x)-iResizeW-2);
        iResizeY = (int)((h+y)-iResizeH-2);
        iContentsX = x+2;
        iContentsY = y+2;
        iActionsX = (int)(x+(w/2))-(iActionsW/2);
        iActionsY = y+2;
    }

    public String getCropNames(Context ctxt){
        String ret="";
        Iterator<oCrop> iterator = crops.iterator();
        while (iterator.hasNext()) {
            oCrop c = iterator.next();
            ret = (ret.isEmpty()) ? c.name : ret + ", " + c.name;
        }
        ret = (ret.isEmpty()) ? ctxt.getString(R.string.textNone) : ret;
        return ret;
    }

    public String getPestControlNames(Context ctxt){
        String ret="";
        Iterator<oTreatmentIngredient> iterator = pestControlIngredients.iterator();
        while (iterator.hasNext()) {
            oTreatmentIngredient ti = iterator.next();
            ret = (ret.isEmpty()) ? ti.name : ret + ", " + ti.name;
        }
        ret = (ret.isEmpty()) ? ctxt.getString(R.string.textNone) : ret;
        return ret;
    }

    public String getSoilManagementNames(Context ctxt){
        String ret="";
        Iterator<oTreatmentIngredient> iterator = soilManagementIngredients.iterator();
        while (iterator.hasNext()) {
            oTreatmentIngredient ti = iterator.next();
            ret = (ret.isEmpty()) ? ti.name : ret + ", " + ti.name;
        }
        ret = (ret.isEmpty()) ? ctxt.getString(R.string.textNone) : ret;
        return ret;
    }
}
