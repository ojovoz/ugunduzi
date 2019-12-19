package ojovoz.ugunduzi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import java.util.jar.Manifest;

/**
 * Created by Eugenio on 19/04/2018.
 */
public class pictureSound extends AppCompatActivity {

    private int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private int CAMERA_PERMISSION = 232323;
    private int AUDIO_PERMISSION = 23232323;

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
    public float farmSize;
    public int farmId;
    public int farmVersion;
    public int maxVersion;
    public int plot;
    public float plotSize;
    public String farmDate;

    String cropNames;
    String pestControlNames;
    String soilManagementNames;

    boolean bChanges=false;

    int displayWidth;
    int displayHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_sound);

        createDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + getString(R.string.app_name) + File.separator);

        user = getIntent().getExtras().getString("user");
        userPass = getIntent().getExtras().getString("userPass");
        userId = getIntent().getExtras().getInt("userId");
        farmName = getIntent().getExtras().getString("farmName");
        farmSize = getIntent().getExtras().getFloat("farmSize");
        farmId = getIntent().getExtras().getInt("farmId");
        farmVersion = getIntent().getExtras().getInt("farmVersion");
        maxVersion = getIntent().getExtras().getInt("maxVersion");
        plot = getIntent().getExtras().getInt("plot");
        plotSize = getIntent().getExtras().getFloat("plotSize");
        farmDate = getIntent().getExtras().getString("farmDate");

        cropNames = getIntent().getExtras().getString("cropNames");
        pestControlNames = getIntent().getExtras().getString("pestControlNames");
        soilManagementNames = getIntent().getExtras().getString("soilManagementNames");

        displayWidth = getIntent().getExtras().getInt("displayWidth");
        displayHeight = getIntent().getExtras().getInt("displayHeight");

        TextView tt = (TextView) findViewById(R.id.plotLabel);
        String title = "";

        if(plot>=0) {

            title = getString(R.string.cropsTitle) + ": " + cropNames + " (" + String.valueOf(plotSize) + " " + getString(R.string.acresWord) + ")";
            title += "\n";
            title += getString(R.string.pestControlTitle) + ": " + pestControlNames;
            title += "\n";
            title += getString(R.string.soilManagementTitle) + ": " + soilManagementNames;

            if (!pestControlNames.equals(getString(R.string.textNone)) && !soilManagementNames.equals(getString(R.string.textNone))) {
                tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagementAndPestControl));
            } else if (!pestControlNames.equals(getString(R.string.textNone)) && soilManagementNames.equals(getString(R.string.textNone))) {
                tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillPestControl));
            } else if (pestControlNames.equals(getString(R.string.textNone)) && !soilManagementNames.equals(getString(R.string.textNone))) {
                tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillSoilManagement));
            } else {
                tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFillDefault));
            }
        } else {
            title = farmName + " (" + String.valueOf(farmSize) + " " + getString(R.string.acresWord) + ")";
            tt.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            tt.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        }

        tt.setText(title);

        ImageView i = (ImageView)findViewById(R.id.thumbnail);
        int w = (displayWidth*400)/540;
        i.getLayoutParams().width = w;
        i.getLayoutParams().height = (int)(w/1.5);
        i.requestLayout();

        photoDone=false;
        recording = false;
        recordingDone = false;

        filesToDelete = new ArrayList<>();

        messageDate = new Date();

        setTitle(getTitle() + ": " + farmName);
    }

    @Override
    public void onBackPressed() {
        tryExit(1);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, R.string.opGoBackToFarm);
        menu.add(1, 1, 1, R.string.opGoBack);
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
                    goBackToFarm();
                    break;
                case 1:
                    goBack();
                    break;
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
                        goBackToFarm();
                        break;
                    case 1:
                        goBack();
                }

            }
        });
        exitDialog.create();
        exitDialog.show();
    }

    public void goBackToFarm(){
        Intent i = new Intent(this, farmInterface.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName", farmName);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion",farmVersion);
        i.putExtra("newFarm", false);
        i.putExtra("firstFarm", false);
        startActivity(i);
        finish();
    }

    public void goBack(){
        final Context context = this;
        Intent i = new Intent(context, records.class);
        i.putExtra("user", user);
        i.putExtra("userId", userId);
        i.putExtra("userPass", userPass);
        i.putExtra("farmName",farmName);
        i.putExtra("farmSize",farmSize);
        i.putExtra("farmId", farmId);
        i.putExtra("farmVersion", farmVersion);
        i.putExtra("maxVersion", maxVersion);
        i.putExtra("plot",plot);
        i.putExtra("farmDate", farmDate);
        i.putExtra("cropNames",cropNames);
        i.putExtra("pestControlNames",pestControlNames);
        i.putExtra("soilManagementNames",soilManagementNames);
        i.putExtra("plotSize",plotSize);
        i.putExtra("displayWidth",displayWidth);
        i.putExtra("displayHeight",displayHeight);
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                showCamera();
            } else {
                String[] permissionRequest = {android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissionRequest, CAMERA_PERMISSION);
            }
        } else {
            showCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==CAMERA_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                showCamera();
            }
        } else if (requestCode==AUDIO_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                doRecordSound();
            }
        }
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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(pictureSound.this, BuildConfig.APPLICATION_ID + ".provider", filename));
                } else {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filename));
                }
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

            } else {
                Toast.makeText(this, R.string.photoFailedTryAgain, Toast.LENGTH_SHORT).show();
                photoDone=false;
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                doRecordSound();
            } else {
                String[] permissionRequest = {android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissionRequest, AUDIO_PERMISSION);
            }
        } else {
            doRecordSound();
        }
    }

    private void doRecordSound() {
        if (!recording) {
            soundRecorder = new audioRecorder();
            if (!soundRecorder.getFilename().equals("null")) {
                deleteFile(soundRecorder.getFilename(), false);
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            soundFile= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + getString(R.string.app_name) + File.separator + "s" + timeStamp + ".amr";
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


