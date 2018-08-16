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
import android.util.DisplayMetrics;
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

    String photoFile="";
    String prevPhotoFile="";
    boolean photoDone;

    private audioRecorder soundRecorder;
    private Boolean recording;
    private boolean recordingDone;
    String soundFile="";
    String prevSoundFile="";

    Date messageDate;

    ArrayList<String> filesToDelete;

    public String user;
    public String userPass;
    public int userId;
    public String farmName;
    public int farmId;
    public int farmVersion;
    public int plot;

    String cropNames;
    String pestControlNames;
    String soilManagementNames;

    boolean bChanges=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_sound);

        createDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + getString(R.string.app_name) + File.separator);

        user = getIntent().getExtras().getString("user");
        userPass = getIntent().getExtras().getString("userPass");
        userId = getIntent().getExtras().getInt("userId");
        farmName = getIntent().getExtras().getString("farmName");
        farmId = getIntent().getExtras().getInt("farmId");
        farmVersion = getIntent().getExtras().getInt("farmVersion");
        plot = getIntent().getExtras().getInt("plot");

        cropNames = getIntent().getExtras().getString("cropNames");
        pestControlNames = getIntent().getExtras().getString("pestControlNames");
        soilManagementNames = getIntent().getExtras().getString("soilManagementNames");

        TextView tt = (TextView) findViewById(R.id.plotLabel);
        String title = "";

        title= getString(R.string.cropsTitle) + ": " + cropNames;
        title+="\n";
        title+=getString(R.string.pestControlTitle) + ": " + pestControlNames;
        title+="\n";
        title+=getString(R.string.soilManagementTitle) + ": " + soilManagementNames;

        if(!pestControlNames.equals(getString(R.string.textNone)) && !soilManagementNames.equals(getString(R.string.textNone))) {
            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagementAndPestControl));
        } else if(!pestControlNames.equals(getString(R.string.textNone)) && soilManagementNames.equals(getString(R.string.textNone))) {
            tt.setBackgroundColor(ContextCompat.getColor(this,R.color.colorFillPestControl));
        } else if(pestControlNames.equals(getString(R.string.textNone)) && !soilManagementNames.equals(getString(R.string.textNone))) {
            tt.setBackgroundColor(ContextCompat.getColor(this,R.color.colorFillSoilManagement));
        } else {
            tt.setBackgroundColor(ContextCompat.getColor(this,R.color.colorFillDefault));
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
        AlertDialog.Builder exitDialog = new AlertDialog.Builder(this);
        exitDialog.setMessage(R.string.pictureSoundNotSavedText);
        exitDialog.setNegativeButton(R.string.noButtonText, null);
        exitDialog.setPositiveButton(R.string.yesButtonText, new DialogInterface.OnClickListener() {
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
        exitDialog.create();
        exitDialog.show();
    }

    public void goBack(){
        Intent i = new Intent(this, farmInterface.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("farmId", farmId);
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
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion", farmVersion);
        i.putExtra("plot", plot);
        i.putExtra("cropNames",cropNames);
        i.putExtra("pestControlNames",pestControlNames);
        i.putExtra("soilManagementNames",soilManagementNames);
        startActivity(i);
        finish();
    }

    void goToManageRecords(){
        Intent i = new Intent(this, dataManager.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion", farmVersion);
        i.putExtra("plot", plot);
        i.putExtra("cropNames",cropNames);
        i.putExtra("pestControlNames",pestControlNames);
        i.putExtra("soilManagementNames",soilManagementNames);
        startActivity(i);
        finish();
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
        log.appendToLog(farmId,farmVersion,userId,plot,messageDate,null,0f,0f,null,null,null,0f,"",photoFile,soundFile);

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


