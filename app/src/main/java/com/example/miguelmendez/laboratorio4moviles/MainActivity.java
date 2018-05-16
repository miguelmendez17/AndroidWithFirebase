package com.example.miguelmendez.laboratorio4moviles;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class MainActivity extends AppCompatActivity {
    private static final int IMAGE_CAPTURE = 101;
    Uri uriImage;
    Button uploadImage,save;
    EditText name, email, password;
    Boolean existUri = false;
    ImageView imagen;

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private static final String TAG = "Auth";

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState );
        setContentView (R.layout.activity_main ) ;
        uploadImage = findViewById(R.id.recordButton);
        save = findViewById(R.id.save);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        imagen = (ImageView)findViewById(R.id.image);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
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

                if(validateFields()){
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("User");
                    String nombre = name.getText().toString();
                    String correo = email.getText().toString();
                    String contr = password.getText().toString();
                    uploadImage();
                    User user=new User();
                    user.setNombre(nombre);
                    user.setCorreo(correo);
                    user.setContrasenna(contr);
                    myRef.child(String.valueOf(nombre)).setValue(user);
                    name.setText("");
                    email.setText("");
                    password.setText("");
                    Toast.makeText(MainActivity.this, "Successfuly.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


  /*  public void validateUserExistingDatabase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("User");

        for(DataSnapshot postSnapshot: myRef.getChildren()){
            User user = postSnapshot.getValue(User.class);
            if(user.name.equals(name)){
                editTextEmail.setText(user.email);
                editTextMobile.setText(user.mobile);
                Toast.makeText(getApplicationContext(), user.email, Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    public void newUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //  updateUI(null);
                        }
                    }
                });
    }

    public void uploadImage(){
        String nombreUsuario = name.getText().toString();
        StorageReference riversRef = mStorageRef.child(nombreUsuario+".jpg");
        riversRef.putFile(uriImage)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });

    }

    public void existingUser(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public boolean validateFields(){
        if(name.getText().toString().isEmpty()||email.getText().toString().isEmpty()||password.getText().toString().isEmpty()){
            Toast.makeText (this , "You need to fill all the fields" , Toast . LENGTH_LONG ) . show () ;
            return false;
        }
        if(!existUri){
            Toast.makeText (this ,"You need to take a photo" , Toast . LENGTH_LONG ) . show () ;
            return false;
        }

        else{
            if(password.getText().length()<6){
                Toast.makeText (this ,"In the password field you must type more than 6 characters" , Toast . LENGTH_LONG ) . show () ;
                return false;
            }
            else{
            newUser(email.getText().toString(), password.getText().toString());
            existingUser(email.getText().toString(),password.getText().toString());
            return true;
            }
        }
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
                Toast.makeText (this , "Photo saved to :\n" +
                        uriImage , Toast . LENGTH_LONG ) . show () ;
                existUri = true;
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
