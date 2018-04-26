package ojovoz.ugunduzi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Created by Eugenio on 25/04/2018.
 */
public class oRecyclerViewAdapter extends RecyclerView.Adapter<oCardViewHolder> {

    List<oCardData> list = Collections.emptyList();
    Context context;

    public oRecyclerViewAdapter(List<oCardData> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public oCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        oCardViewHolder holder = new oCardViewHolder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(oCardViewHolder holder, int position) {

        holder.cb.setId(position);
        holder.cb.setChecked(list.get(position).isSelected);
        holder.info.setText(list.get(position).info);
        if(list.get(position).plotInfoColor==-1){
            if (position % 2 == 0) {
                holder.cv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFillFaded));
            } else {
                holder.cv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));
            }
        } else {
            holder.cv.setBackgroundColor(list.get(position).plotInfoColor);
        }

        if(!list.get(position).imgFile.isEmpty()){
            holder.image.setVisibility(View.VISIBLE);
            holder.image.setImageBitmap(scaleBitmap(list.get(position).imgFile));
            holder.image.setId(position);
        } else {
            holder.image.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insert(int position, oCardData data) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    public void remove(oCardData data) {
        int position = list.indexOf(data);
        list.remove(position);
        notifyItemRemoved(position);
    }

    public Bitmap scaleBitmap(String path){
        Bitmap ret=null;
        final int IMAGE_MAX_SIZE = 100000;
        try{
            InputStream in = null;
            in = new FileInputStream(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();

            int scale = 1;
            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }

            in = new FileInputStream(path);

            if (scale > 1) {
                scale--;
                options = new BitmapFactory.Options();
                options.inSampleSize = scale;
                ret = BitmapFactory.decodeStream(in, null, options);
                in.close();

                int height = ret.getHeight();
                int width = ret.getWidth();

                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(ret, (int) x, (int) y, true);

                ret.recycle();
                ret = scaledBitmap;
                return ret;

            } else {
                ret = BitmapFactory.decodeStream(in);
                in.close();
                return ret;
            }


        } catch (IOException e){

        }
        return ret;
    }
}
