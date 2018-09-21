package ojovoz.ugunduzi;

import android.graphics.Color;

/**
 * Created by Eugenio on 25/04/2018.
 */
public class oCardData {
    public int id;
    public boolean isSelected;
    public int plotInfoColor;
    public int infoColor;
    public String info;
    public String imgFile;
    public String sndFile;

    oCardData(){
        isSelected=false;
        info="";
        infoColor= Color.BLACK;
        imgFile="";
        sndFile="";
    }
}
