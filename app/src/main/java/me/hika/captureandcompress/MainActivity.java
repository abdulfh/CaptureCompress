package me.hika.captureandcompress;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;

import id.zelory.compressor.Compressor;
import me.hika.captureandcompress.utility.FileUtility;
import me.hika.captureandcompress.utility.Utility;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_CODE = 1000;
    Button btnCapture,btnCompress;
    File actualImage,compressedImage;
    TextView actualSizeTextView,compressedSizeTextView;
    ImageView actualImageView,compressedImageView;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        if (ContextCompat.checkSelfPermission( MainActivity.this,Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {
            String[] permission = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions( MainActivity.this, permission, PERMISSION_CODE );
        }

        actualImageView = findViewById( R.id.actualImageView );
        compressedImageView = findViewById( R.id.compressedImageView );

        actualSizeTextView = findViewById( R.id.actualSizeTextView );
        compressedSizeTextView = findViewById( R.id.compressedSizeTextView );

        btnCapture = findViewById( R.id.chooseImageButton );
        btnCapture.setOnClickListener( this );

        btnCompress = findViewById( R.id.compressImageButton );
        btnCompress.setOnClickListener( this );

        actualImageView.setBackgroundColor(getRandomColor());
        clearImage();
    }

    @Override
    public void onClick( View view ) {
        switch (view.getId()) {
            case R.id.chooseImageButton:
                captureImage();
                break;
            case R.id.compressImageButton:
                compressImage();
                break;
        }
    }

    private void compressImage() {
        if (actualImage == null) {
            Toast.makeText( MainActivity.this,"Please choose an image!", Toast.LENGTH_LONG ).show();
        } else {

            // Compress image in main thread
            compressedImage = Compressor.getDefault(this).compressToFile(actualImage);
            setCompressedImage();

            // Compress image to bitmap in main thread
            /*compressedImageView.setImageBitmap(Compressor.getDefault(this).compressToBitmap(actualImage));*/

            // Compress image using RxJava in background thread
//            Compressor.getDefault(this)
//                    .compressToFileAsObservable(actualImage)
//                    .subscribeOn( Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Action1<File>() {
//                        @Override
//                        public void call(File file) {
//                            compressedImage = file;
//                            setCompressedImage();
//                        }
//                    }, new Action1<Throwable>() {
//                        @Override
//                        public void call(Throwable throwable) {
//                            showError(throwable.getMessage());
//                        }
//                    });
        }
    }

    private void captureImage() {
        Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        startActivityForResult( intent,PERMISSION_CODE );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, @Nullable Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == PERMISSION_CODE) {
            try {

                Bitmap bitmap = (Bitmap) data.getExtras().get( "data" );
                Uri tempUri = Utility.getImageUri(MainActivity.this, bitmap );

                actualImage = FileUtility.from(MainActivity.this, tempUri);
                actualImageView.setImageBitmap( BitmapFactory.decodeFile(actualImage.getAbsolutePath()) );
                actualSizeTextView.setText(String.format("Size : %s", Utility.getReadableFileSize(actualImage.length())));

                clearImage();

            }catch ( Exception e ) {

                Toast.makeText( MainActivity.this, "Error " + e.getMessage() , Toast.LENGTH_SHORT ).show();

            }
        }
    }

    private void clearImage() {
        actualImageView.setBackgroundColor(getRandomColor());
        compressedImageView.setImageDrawable(null);
        compressedImageView.setBackgroundColor(getRandomColor());
        compressedSizeTextView.setText("Size : -");
    }

    private int getRandomColor() {
        Random rand = new Random();
        return Color.argb(100, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private void setCompressedImage() {
        compressedImageView.setImageBitmap(BitmapFactory.decodeFile(compressedImage.getAbsolutePath()));
        compressedSizeTextView.setText(String.format("Size : %s", Utility.getReadableFileSize(compressedImage.length())));

        Toast.makeText(this, "Compressed image save in " + compressedImage.getPath(), Toast.LENGTH_LONG).show();
        Log.d("Compressor", "Compressed image save in " + compressedImage.getPath());
    }

}