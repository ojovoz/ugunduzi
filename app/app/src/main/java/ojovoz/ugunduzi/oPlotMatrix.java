package ojovoz.ugunduzi;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Eugenio on 16/03/2018.
 */
public class oPlotMatrix {

    public ArrayList<oPlot> plots;
    public oPlot currentPlot;

    matrixContent[][] matrix;
    int displayWidth;
    int displayHeight;

    oPlot ghostPlot;
    int offsetX;
    int offsetY;
    int startX;
    int startY;

    int plotIndex=0;

    boolean bGoNext;
    boolean bGoPrev;

    oPlotMatrix() {
        plots = new ArrayList<>();
        matrix = new matrixContent[4][4];
        currentPlot = new oPlot();
        bGoNext = bGoPrev = false;
    }

    public void createMatrix(int w, int h) {
        displayWidth = w;
        displayHeight = h;
        int x;
        int y;
        for (int i = 0; i < 4; i++) {
            y = (h / 4) * i;
            for (int j = 0; j < 4; j++) {
                x = (w / 4) * j;
                matrix[j][i] = new matrixContent(null, new Point(x, y));
            }
        }
    }

    public void fromString(Context c, String matrixString, String separator, int iResizeW, int iResizeH, int iContentsW, int iContentsH, int iActionsW, int iActionsH){
        String matrixItems[] = matrixString.split(separator);
        oPlot plot;
        oCrop crop = new oCrop(c);
        oTreatmentIngredient treatmentIngredient = new oTreatmentIngredient(c);
        int inc = (matrixItems.length%8==0) ? (matrixItems.length%9==0) ? 9 : 8 : 9;
        for(int i=0;i<matrixItems.length;i+=inc){
            plot=new oPlot();
            plot.id=Integer.parseInt(matrixItems[i]);
            plot.x=Integer.parseInt(matrixItems[i+1])* displayWidth/4;
            plot.y=Integer.parseInt(matrixItems[i+2])* displayHeight/4;
            plot.w=Integer.parseInt(matrixItems[i+3])* displayWidth/4;
            plot.h=Integer.parseInt(matrixItems[i+4])* displayHeight/4;
            plot.size = (inc==9) ? Float.parseFloat(matrixItems[i + 5]) : 0;
            String crops=matrixItems[i+inc-3];
            if(!crops.equals("-1")) {
                String plotCropsList[] = crops.split("\\|");
                for (int j = 0; j < plotCropsList.length; j++) {
                    oCrop pc = crop.getCropFromId(Integer.parseInt(plotCropsList[j]));
                    plot.crops.add(pc);
                }
            }
            String pestControl=matrixItems[i+inc-2];
            if(!pestControl.equals("-1")) {
                String plotPestControlList[] = pestControl.split("\\|");
                for (int j = 0; j < plotPestControlList.length; j++) {
                    oTreatmentIngredient ti = treatmentIngredient.getTreatmentIngredientFromId(Integer.parseInt(plotPestControlList[j]));
                    plot.pestControlIngredients.add(ti);
                }
            }
            String soilManagement=matrixItems[i+inc-1];
            if(!soilManagement.equals("-1")) {
                String plotSoilManagementList[] = soilManagement.split("\\|");
                for (int j = 0; j < plotSoilManagementList.length; j++) {
                    oTreatmentIngredient ti = treatmentIngredient.getTreatmentIngredientFromId(Integer.parseInt(plotSoilManagementList[j]));
                    plot.soilManagementIngredients.add(ti);
                }
            }
            plot.addAreas(iResizeW, iResizeH, iContentsW, iContentsH, iActionsW, iActionsH);
            plots.add(plot);
            addPlotToMatrix(plot, plot.x/(displayWidth/4), plot.y/(displayHeight/4),(int)(plot.w/(displayWidth/4))+ plot.x/(displayWidth/4),(int)(plot.h/(displayHeight/4))+ plot.y/(displayHeight/4));
        }
    }

    public void fromString(Context c,String matrixString, String separator){
        String matrixItems[] = matrixString.split(separator);
        oCrop crop = new oCrop(c);
        oTreatmentIngredient treatmentIngredient = new oTreatmentIngredient(c);
        int inc = (matrixItems.length%8==0) ? 8 : 9;
        for(int i=0;i<matrixItems.length;i+=inc){
            oPlot plot=new oPlot();
            plot.id=Integer.parseInt(matrixItems[i]);
            String crops=matrixItems[i+inc-3];
            if(!crops.equals("-1")) {
                String plotCropsList[] = crops.split("\\|");
                for (int j = 0; j < plotCropsList.length; j++) {
                    oCrop pc = crop.getCropFromId(Integer.parseInt(plotCropsList[j]));
                    plot.crops.add(pc);
                }
            }
            String pestControl=matrixItems[i+inc-2];
            if(!pestControl.equals("-1")) {
                String plotPestControlList[] = pestControl.split("\\|");
                for (int j = 0; j < plotPestControlList.length; j++) {
                    oTreatmentIngredient ti = treatmentIngredient.getTreatmentIngredientFromId(Integer.parseInt(plotPestControlList[j]));
                    plot.pestControlIngredients.add(ti);
                }
            }
            String soilManagement=matrixItems[i+inc-1];
            if(!soilManagement.equals("-1")) {
                String plotSoilManagementList[] = soilManagement.split("\\|");
                for (int j = 0; j < plotSoilManagementList.length; j++) {
                    oTreatmentIngredient ti = treatmentIngredient.getTreatmentIngredientFromId(Integer.parseInt(plotSoilManagementList[j]));
                    plot.soilManagementIngredients.add(ti);
                }
            }
            plots.add(plot);
        }
    }

    public void setCurrentPlot(oPlot p) {
        currentPlot = p;
    }

    public boolean addPlot(int iResizeW, int iResizeH, int iContentsW, int iContentsH, int iActionsW, int iActionsH) {
        boolean ret;
        matrixContent cell = findFirstAvailablePosition();
        if (cell.point != null) {
            oPlot p = new oPlot(cell.point.x, cell.point.y, displayWidth / 4, displayHeight / 4);
            p.addAreas(iResizeW, iResizeH, iContentsW, iContentsH, iActionsW, iActionsH);
            setCurrentPlot(p);
            getPlotIndex();
            p.id=plotIndex;
            plots.add(p);
            cell.plot = p;
            ret=true;
        } else {
            ret=false;
        }
        return ret;
    }

    public void getPlotIndex(){
        int index=-1;
        Iterator<oPlot> iterator = plots.iterator();
        while(iterator.hasNext()){
            oPlot p = iterator.next();
            if(p.id>index){
                index=p.id;
            }
        }
        plotIndex=index+1;
    }

    public matrixContent findFirstAvailablePosition() {
        matrixContent ret = new matrixContent(null, null);
        boolean bFound = false;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrixContent cell = matrix[j][i];
                if (cell.plot == null) {
                    ret = cell;
                    bFound = true;
                    break;
                }
            }
            if (bFound) {
                break;
            }
        }
        return ret;
    }

    public boolean passEvent(MotionEvent e, int state) {
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
            currentPlot = getTouchedPlot((int) e.getX(), (int) e.getY(), state);
            startX = (int) e.getX();
            startY = (int) e.getY();
            if (currentPlot != null) {
                if (currentPlot.state == 2 || currentPlot.state == 3) {
                    ghostPlot = new oPlot(currentPlot.x, currentPlot.y, (int) currentPlot.w, (int) currentPlot.h);
                    offsetX = (int) (e.getX() - ghostPlot.x);
                    offsetY = (int) (e.getY() - ghostPlot.y);
                    return true;
                } else {
                    ghostPlot = null;
                    return state == 1 && currentPlot.state == 1;
                }
            } else {
                ghostPlot = null;
                return state == 1;
            }

        } else if (e.getActionMasked() == MotionEvent.ACTION_UP) {
            if (currentPlot != null) {
                if (ghostPlot != null) {
                    snapToGrid();
                    currentPlot.state = 0;
                    ghostPlot = null;
                } else {
                    calculateXDelta((int)e.getX(),(int)e.getY());
                }
                return true;
            } else {
                if(state==1){
                    calculateXDelta((int)e.getX(),(int)e.getY());
                    return true;
                } else {
                    return false;
                }
            }
        } else if (e.getActionMasked() == MotionEvent.ACTION_MOVE && (state==0 || state==2)) {
            if (currentPlot != null && ghostPlot != null) {
                if (currentPlot.state == 2) {
                    moveGhostPlot((int) e.getX(), (int) e.getY(), (int) ghostPlot.w, (int) ghostPlot.h);
                } else if (currentPlot.state == 3) {
                    resizeGhostPlot((int) e.getX(), (int) e.getY(), (int) ghostPlot.w, (int) ghostPlot.h);
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public oPlot getTouchedPlot(int x, int y, int state) {
        oPlot ret = null;
        if (currentPlot != null) {
            currentPlot.state = 0;
        }
        Iterator<oPlot> iterator = plots.iterator();
        while (iterator.hasNext()) {
            oPlot plot = iterator.next();
            if (isWithin(plot, x, y)) {
                if(state==0 || state==2) {
                    if (isMoving(plot, x, y)) {
                        plot.state = 2;
                    } else if (isResizing(plot, x, y)) {
                        plot.state = 3;
                    } else if (isEditing(plot, x, y)) {
                        plot.state = 4;
                    } else {
                        plot.state = 1;
                    }
                } else {
                    if(actionChooser(plot, x, y)){
                        plot.state = 5;
                    } else {
                        plot.state = 1;
                    }
                }
                ret = plot;
                break;
            }
        }
        return ret;
    }

    public boolean isWithin(oPlot p, int x, int y) {
        boolean ret = false;
        if (x > p.x && x < (p.x + p.w) && y > p.y && y < (p.y + p.h)) {
            ret = true;
        }
        return ret;
    }

    public boolean isMoving(oPlot p, int x, int y) {
        boolean ret = false;
        if (x > p.iMoveX && x < (p.iMoveX + p.iMoveW) && y > p.iMoveY && y < (p.iMoveY + p.iMoveH)) {
            ret = true;
        }
        return ret;
    }

    public boolean isResizing(oPlot p, int x, int y) {
        boolean ret = false;
        if (x > p.iResizeX && x < (p.iResizeX + p.iResizeW) && y > p.iResizeY && y < (p.iResizeY + p.iResizeH)) {
            ret = true;
        }
        return ret;
    }

    public boolean isEditing(oPlot p, int x, int y){
        boolean ret = false;
        if (x > p.iContentsX && x < (p.iContentsX + p.iContentsW) && y > p.iContentsY && y < (p.iContentsY + p.iContentsH)) {
            ret = true;
        }
        return ret;
    }

    public boolean actionChooser(oPlot p, int x, int y){
        boolean ret = false;
        if (x > p.iContentsX && x < (p.iActionsX + p.iActionsW) && y > p.iActionsY && y < (p.iActionsY + p.iActionsH)) {
            ret = true;
        }
        return ret;
    }

    public void moveGhostPlot(int x, int y, int w, int h) {
        if ((x - offsetX) >= 0 && ((x - offsetX) + w) < displayWidth) {
            ghostPlot.x = x - offsetX;
        } else {
            offsetX = x - ghostPlot.x;
        }
        if ((y - offsetY) >= 0 && ((y - offsetY) + h) < displayHeight) {
            ghostPlot.y = y - offsetY;
        } else {
            offsetY = y - ghostPlot.y;
        }
        if (!isWithin(ghostPlot, x, y)) {
            currentPlot.state = 0;
            ghostPlot = null;
        }
    }

    public void resizeGhostPlot(int x, int y, int w, int h) {
        int varX = startX - x;
        int varY = startY - y;
        if ((w - varX) >= (displayWidth / 4) && (w - varX) < displayWidth) {
            ghostPlot.w = w - varX;
            startX = x;
        }
        if ((h - varY) >= (displayHeight / 4) && (h - varY) < displayHeight) {
            ghostPlot.h = h - varY;
            startY = y;
        }
    }

    public void snapToGrid() {
        float destX = ghostPlot.x;
        float destY = ghostPlot.y;
        float destW = ghostPlot.w;
        float destH = ghostPlot.h;

        int closestX = 0;
        int matrixX = 0;
        int matrixY = 0;
        int closestY = 0;
        double minDist = 9999;

        for (int ix = 0; ix < displayWidth; ix += (displayWidth / 4)) {
            for (int iy = 0; iy < displayHeight; iy += (displayHeight / 4)) {
                double dist = Math.hypot(destX - ix, destY - iy);
                if (dist < minDist) {
                    minDist = dist;
                    closestX = ix;
                    closestY = iy;
                    matrixX = Math.round(ix / (displayWidth / 4));
                    matrixY = Math.round(iy / (displayHeight / 4));
                }
            }
        }

        if (fitInMatrix(matrixX, matrixY, destW, destH)) {
            currentPlot.x = closestX;
            currentPlot.y = closestY;
            currentPlot.w = Math.round(destW / (displayWidth / 4)) * (displayWidth / 4);
            currentPlot.h = Math.round(destH / (displayHeight / 4)) * (displayHeight / 4);
            currentPlot.iMoveW=(int)currentPlot.w;
            currentPlot.iMoveH=(int)currentPlot.h-(currentPlot.iContentsH+currentPlot.iResizeH);
            currentPlot.calculateAreasXY();
        }

    }

    public boolean fitInMatrix(int matrixX, int matrixY, float destW, float destH) {
        boolean ret = true;
        int matrixX2 = matrixX + Math.round(destW / (displayWidth / 4));
        int matrixY2 = matrixY + Math.round(destH / (displayHeight / 4));
        for (int y = matrixY; y < matrixY2; y++) {
            if (ret) {
                for (int x = matrixX; x < matrixX2; x++) {
                    matrixContent mc = matrix[x][y];
                    if (mc.plot != null && mc.plot != currentPlot) {
                        ret = false;
                        break;
                    }
                }
            }
        }
        if (ret) {
            deletePlotFromMatrix(currentPlot);
            addPlotToMatrix(currentPlot, matrixX, matrixY, matrixX2, matrixY2);
        }
        return ret;
    }

    public void deletePlotFromMatrix(oPlot p) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                matrixContent mc = matrix[x][y];
                if (mc.plot == p) {
                    mc.plot = null;
                }
            }
        }
    }

    public void addPlotToMatrix(oPlot p, int x1, int y1, int x2, int y2) {
        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                matrixContent mc = matrix[x][y];
                mc.plot = p;
            }
        }
    }

    public boolean deletePlot(){
        boolean ret=true;
        if(plots.size()>1){
            plots.remove(currentPlot);
            deletePlotFromMatrix(currentPlot);
            currentPlot=null;
        } else {
            ret=false;
        }
        return ret;
    }

    public ArrayList<oPlot> getPlots() {
        return plots;
    }

    public void calculateXDelta(int x, int y){
        double xDelta = startX - x;
        double yDelta = startY - y;
        if(Math.abs(xDelta)>=(displayWidth/2) && (Math.abs(yDelta)<Math.abs(xDelta))){
            bGoPrev = (xDelta<0);
            bGoNext = !bGoPrev;
        } else {
            bGoPrev = bGoNext = false;
        }
    }

    public oPlot getPlotFromId(int id){
        oPlot ret = new oPlot();
        Iterator<oPlot> iterator = plots.iterator();
        while (iterator.hasNext()) {
            oPlot plot = iterator.next();
            if(plot.id==id){
                ret=plot;
                break;
            }
        }
        return ret;
    }

    private class matrixContent {
        public oPlot plot;
        public Point point;

        matrixContent(oPlot rP, Point rPoint) {
            plot = rP;
            point = rPoint;
        }
    }

    public String toString(){
        String ret="";
        boolean bFound;
        Iterator<oPlot> iterator = plots.iterator();
        while (iterator.hasNext()) {
            oPlot plot = iterator.next();
            String plotString=String.valueOf(plot.id) + ";";
            bFound=false;
            for (int y = 0; y < 4; y++) {
                if(!bFound) {
                    for (int x = 0; x < 4; x++) {
                        matrixContent mc = matrix[x][y];
                        if (mc.plot == plot) {
                            plotString = plotString + String.valueOf(x) + ";" + String.valueOf(y) + ";";
                            bFound=true;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
            plotString = plotString + String.valueOf(Math.round(plot.w/(displayWidth/4))) + ";" + String.valueOf(Math.round(plot.h/(displayHeight/4))) + ";" + String.valueOf(plot.size) + ";";
            String crops="-1";
            Iterator<oCrop> iteratorCrops = plot.crops.iterator();
            while (iteratorCrops.hasNext()) {
                oCrop crop = iteratorCrops.next();
                crops = (crops.equals("-1")) ? String.valueOf(crop.id) : crops + "|" + String.valueOf(crop.id);
            }
            String pestControl="-1";
            Iterator<oTreatmentIngredient> iteratorPestControl = plot.pestControlIngredients.iterator();
            while (iteratorPestControl.hasNext()) {
                oTreatmentIngredient ti = iteratorPestControl.next();
                pestControl = (pestControl.equals("-1")) ? String.valueOf(ti.id) : pestControl + "|" + String.valueOf(ti.id);
            }
            String soilManagement="-1";
            Iterator<oTreatmentIngredient> iteratorSoilManagement = plot.soilManagementIngredients.iterator();
            while (iteratorSoilManagement.hasNext()) {
                oTreatmentIngredient ti = iteratorSoilManagement.next();
                soilManagement = (soilManagement.equals("-1")) ? String.valueOf(ti.id) : soilManagement + "|" + String.valueOf(ti.id);
            }
            plotString = plotString + crops + ";" + pestControl + ";" + soilManagement;
            if(ret.isEmpty()){
                ret=plotString;
            } else {
                ret=ret + ";" + plotString;
            }
        }
        return ret;
    }
}
