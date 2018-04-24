package ojovoz.ugunduzi;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Eugenio on 19/04/2018.
 */
public class pictureSound extends AppCompatActivity {

    private int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    String photoFile;
    String prevPhotoFile="";
    boolean photoDone;

    private audioRecorder soundRecorder;
    private Boolean recording;
    private boolean recordingDone;
    String soundFile;
    String prevSoundFile="";

    Date messageDate;

    ArrayList<String> filesToDelete;

    public String user;
    public String userPass;
    public int userId;
    public String farmName;
    public int plot;

    boolean bChanges=false;

    oCrop crop1;
    oCrop crop2;
    oTreatment treatment1;
    oTreatment treatment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_sound);

        createDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + getString(R.string.app_name) + File.separator);

        crop1 = new oCrop(this);
        crop2 = new oCrop(this);
        treatment1 = new oTreatment(this);
        treatment2 = new oTreatment(this);

        user = getIntent().getExtras().getString("user");
        userPass = getIntent().getExtras().getString("userPass");
        userId = getIntent().getExtras().getInt("userId");
        farmName = getIntent().getExtras().getString("farmName");
        plot = getIntent().getExtras().getInt("plot");

        crop1 = (getIntent().getExtras().getInt("crop1") > 0) ? crop1.getCropFromId(getIntent().getExtras().getInt("crop1")) : null;
        crop2 = (getIntent().getExtras().getInt("crop2") > 0) ? crop2.getCropFromId(getIntent().getExtras().getInt("crop2")) : null;

        treatment1 = (getIntent().getExtras().getInt("treatment1") > 0) ? treatment1.getTreatmentFromId(getIntent().getExtras().getInt("treatment1")) : null;
        treatment2 = (getIntent().getExtras().getInt("treatment2") > 0) ? treatment2.getTreatmentFromId(getIntent().getExtras().getInt("treatment2")) : null;

        TextView tt = (TextView) findViewById(R.id.plotLabel);
        String title = "";

        if (crop1 == null && crop2 == null) {
            title = getString(R.string.plotCropLabel) + "s: " + getString(R.string.textNone);
        } else {
            if (crop1 != null && crop2 == null) {
                title = getString(R.string.plotCropLabel) + ": " + crop1.name;
            } else if (crop1 != null && crop2 != null) {
                title = getString(R.string.plotCropLabel) + "s: " + crop1.name + ", " + crop2.name;
            }
        }
        title += "\n";
        if (treatment1 == null && treatment2 == null) {
            title += getString(R.string.plotTreatmentLabel) + "s: " + getString(R.string.textNone);
            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillDefault));
        } else {
            if (treatment1 != null && treatment2 == null) {
                title += getString(R.string.plotTreatmentLabel) + ": " + treatment1.name;
                if (treatment1.category == 0) {
                    tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillPestControl));
                } else {
                    tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagement));
                }
            } else if (treatment1 != null && treatment2 != null) {
                title += getString(R.string.plotTreatmentLabel) + "s: " + treatment1.name + ", " + treatment2.name;
                if (treatment1.category != treatment2.category) {
                    tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagementAndPestControl));
                } else {
                    if (treatment1.category == 0) {
                        tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillPestControl));
                    } else {
                        tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagement));
                    }
                }
            }
        }
        tt.setText(title);

        photoDone=false;
        recording = false;
        recordingDone = false;

        filesToDelete = new ArrayList<>();

        messageDate = new Date();
    }

    @Override
    public void onBackPressed() {
        tryExit(2);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, R.string.opEnterData);
        menu.add(1, 1, 1, R.string.opManagePlotRecords);
        menu.add(2, 2, 2, R.string.opGoBack);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        tryExit(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    public void tryExit(int exitAction){
        if(bChanges) {
            confirmExit(exitAction);
        } else {
            switch (exitAction) {
                case 0:
                    goToEnterData();
                    break;
                case 1:
                    goToManageRecords();
                    break;
                case 2:
                    goBack();
            }
        }
    }

    public void confirmExit(int e) {
        final int exitAction = e;
        AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
        logoutDialog.setMessage(R.string.pictureSoundNotSavedText);
        logoutDialog.setNegativeButton(R.string.noButtonText, null);
        logoutDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(!photoFile.isEmpty()){
                    filesToDelete.add(photoFile);
                }
                if(!soundFile.isEmpty()){
                    filesToDelete.add(soundFile);
                }
                if(filesToDelete.size()>0){
                    deleteFiles();
                }
                switch (exitAction) {
                    case 0:
                        goToEnterData();
                        break;
                    case 1:
                        goToManageRecords();
                        break;
                    case 2:
                        goBack();
                }

            }
        });
        logoutDialog.create();
        logoutDialog.show();
    }

    public void goBack(){
        Intent i = new Intent(this, farmInterface.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("newFarm", false);
        i.putExtra("firstFarm", false);
        startActivity(i);
        finish();
    }

    public void goToEnterData(){
        Intent i = new Intent(this, enterData.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("plot", plot);
        if(crop1!=null) {
            i.putExtra("crop1", crop1.id);
        } else {
            i.putExtra("crop1", "-1");
        }
        if(crop2!=null) {
            i.putExtra("crop2", crop2.id);
        } else {
            i.putExtra("crop2", "-1");
        }
        if(treatment1!=null) {
            i.putExtra("treatment1", treatment1.id);
        } else {
            i.putExtra("treatment1", "-1");
        }
        if(treatment2!=null) {
            i.putExtra("treatment2", treatment2.id);
        } else {
            i.putExtra("treatment2", "-1");
        }
        startActivity(i);
        finish();
    }

    void goToManageRecords(){

    }

    public void createDir(String dir) {
        File folder = new File(dir);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public void startCamera(View v){
        showCamera();
    }

    private void showCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File filename = null;
            try {
                filename = createImageFile();
            } catch (IOException ex) {

            }
            if (filename != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filename));
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String dataPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + getString(R.string.app_name) + File.separator;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File image = new File(dataPath + "i" + timeStamp + ".jpg");

        photoFile = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap thumb = scaleBitmap(photoFile);
            if (thumb != null) {
                ImageView thumbnail = (ImageView) this.findViewById(R.id.thumbnail);
                thumbnail.setImageBitmap(thumb);
                thumbnail.invalidate();
            }

            photoDone = true;
            bChanges = true;
            if(!prevPhotoFile.isEmpty()){
                filesToDelete.add(prevPhotoFile);
            }
            prevPhotoFile = photoFile;

            if (recordingDone) {
                Button saveButton = (Button) findViewById(R.id.saveButton);
                saveButton.setVisibility(View.VISIBLE);

            }

        } else if(!prevPhotoFile.isEmpty()) {
            photoFile = prevPhotoFile;
        }
    }

    public Bitmap scaleBitmap(String path){
        Bitmap ret=null;
        final int IMAGE_MAX_SIZE = 1200000;
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

                try
                {
                    FileOutputStream out = new FileOutputStream(path);
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                    out.close();
                } catch (Exception e) {

                }

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

    public void recordSound(View v){
        if (!recording) {
            soundRecorder = new audioRecorder();
            if (!soundRecorder.getFilename().equals("null")) {
                deleteFile(soundRecorder.getFilename(), false);
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            soundFile=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + getString(R.string.app_name) + File.separator + "s" + timeStamp + ".amr";
            soundRecorder.modifyPath(soundFile);
            try {
                Button bRec = (Button) findViewById(R.id.soundButton);
                bRec.setBackgroundResource(R.drawable.button_background_rec);
                bRec.setText(R.string.soundButtonRecordingLabel);
                TextView textSoundRecorded = (TextView) findViewById(R.id.textSoundRecorded);
                textSoundRecorded.setText(R.string.emptyString);
                Button bSave = (Button) findViewById(R.id.saveButton);
                bSave.setVisibility(View.GONE);
                soundRecorder.start();
                recording = true;
            } catch (IOException e) {
                //
            }
        } else {
            if (soundRecorder != null) {
                try {
                    Button bRec = (Button) findViewById(R.id.soundButton);
                    bRec.setText(R.string.soundButtonLabelAgain);
                    bRec.setBackgroundResource(R.drawable.button_background);
                    soundRecorder.stop();
                    recording = false;
                    recordingDone = true;
                    bChanges = true;
                    if(!prevSoundFile.isEmpty()){
                        filesToDelete.add(prevSoundFile);
                    }
                    prevSoundFile=soundFile;
                    if (photoDone) {
                        Button saveButton = (Button) findViewById(R.id.saveButton);
                        saveButton.setVisibility(View.VISIBLE);
                    }
                    TextView textSoundRecorded = (TextView) findViewById(R.id.textSoundRecorded);
                    textSoundRecorded.setText(R.string.soundRecordedMessage);
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    public void deleteFiles(){
        Iterator<String> iterator = filesToDelete.iterator();
        while (iterator.hasNext()) {
            String f = iterator.next();
            deleteFile(f,f.contains(".jpg"));
        }
    }

    public void saveMessage(View v){
        if(filesToDelete.size()>0){
            deleteFiles();
        }

        oLog log = new oLog(this);
        log.appendToLog(farmName,userId,plot,messageDate,null,0f,null,null,null,photoFile,soundFile);

        Button bs = (Button)findViewById(R.id.soundButton);
        bs.setText(R.string.soundButtonLabel);
        TextView tv = (TextView)findViewById(R.id.textSoundRecorded);
        tv.setText(R.string.emptyString);
        ImageView it = (ImageView)findViewById(R.id.thumbnail);
        it.setImageResource(R.drawable.blank_image);
        bs = (Button)findViewById(R.id.saveButton);
        bs.setVisibility(View.GONE);

        filesToDelete = new ArrayList<>();

        messageDate = new Date();

        photoDone=false;
        photoFile="";
        prevPhotoFile="";

        soundRecorder.clear();
        recordingDone=false;
        soundFile="";
        prevSoundFile="";

        bChanges=false;

        Toast.makeText(this, R.string.pictureSoundSavedMessage, Toast.LENGTH_SHORT).show();
    }

    private void deleteFile(String f, boolean isImage) {
        File fileX = new File(f);
        long imgFileDate = fileX.lastModified();
        fileX.delete();
        if (isImage) {
            String defaultGalleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + "Camera";
            File imgs = new File(defaultGalleryPath);
            File imgsArray[] = imgs.listFiles();
            for (int i = 0; i < imgsArray.length; i++) {
                if (Math.abs(imgsArray[i].lastModified() - imgFileDate) <= 3000) {
                    imgsArray[i].delete();
                    break;
                }
            }
        }
    }

}


