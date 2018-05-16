package com.example.miguelmendez.laboratorio4moviles;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int IMAGE_CAPTURE = 101;
    Uri uriImage;
    Button uploadImage,save;
    EditText name, email, password;

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState );
        setContentView (R.layout.activity_main ) ;
        uploadImage = findViewById(R.id.recordButton);
        save = findViewById(R.id.save);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        if (!hasCamera())
            uploadImage.setEnabled( false ) ;

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording(v);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateEmptyFields();
            }
        });
    }

    public void validateEmptyFields(){
        if(name.getText().toString().isEmpty()||email.getText().toString().isEmpty()||password.getText().toString().isEmpty()){
            Toast.makeText (this , "You need to fill all the fields" , Toast . LENGTH_LONG ) . show () ; }
        else{
            Toast.makeText (this , "Ok" , Toast . LENGTH_LONG ) . show () ; }
    }

    private boolean hasCamera () {
        return ( getPackageManager () . hasSystemFeature (
                PackageManager. FEATURE_CAMERA_ANY ) ) ;
    }

    public void startRecording (View view)
    {
        Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE ) ;
        startActivityForResult (intent,IMAGE_CAPTURE ) ;
    }
    protected void onActivityResult ( int requestCode , int resultCode , Intent data ) {
        uriImage = data.getData () ;
        if ( requestCode == IMAGE_CAPTURE ) {
            if ( resultCode == RESULT_OK ) {
                Toast.makeText (this , " Photo saved to :\n" +
                        uriImage , Toast . LENGTH_LONG ) . show () ;
            } else if ( resultCode == RESULT_CANCELED ) {
                Toast.makeText (this , " Cancelled .",
                        Toast . LENGTH_LONG ) . show () ;
            } else {
                Toast . makeText (this , " Failed to take photo ",
                        Toast . LENGTH_LONG ) . show () ;
            }
        }
    }
}
