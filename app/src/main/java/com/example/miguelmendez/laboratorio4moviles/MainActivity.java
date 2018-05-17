package com.example.miguelmendez.laboratorio4moviles;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
    private Boolean existUser = false;
    public Boolean errorAuth = false;
    ImageView imagen;

    String globalName,globalEmail, globalPassword;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

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
        imagen = findViewById(R.id.image);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("User");
        validateUserExistingDatabase();

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
                    validateUserExistingDatabase();
                    if(existUser){
                        dialogShow();
                    }
                    else{
                        newUser(globalEmail,globalPassword);
                    }

                }
                existUser = false;
            }
        });
    }



    public void saveInformation(){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("User");
        User user = new User();
        user.setNombre(globalName);
        user.setCorreo(globalEmail);
        user.setContrasenna(globalPassword);
        uploadImage();
        myRef.child(String.valueOf(globalName)).setValue(user);
        name.setText("");
        email.setText("");
        password.setText("");
        Toast.makeText(MainActivity.this, "Successfuly.", Toast.LENGTH_SHORT).show();
    }

    public void validateUserExistingDatabase(){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    User user = postSnapshot.getValue(User.class);
                    if(user.nombre.equals(name.getText().toString())){
                        existUser = true;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    public void newUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveInformation();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed. Email must be similar to (miguel@gmail.com)",
                                    Toast.LENGTH_SHORT).show();

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
                            errorAuth = false;
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            errorAuth = true;
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed. This email is already on use in auth",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void dialogShow(){
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("This user is already exist. Do you want to continue anyway?")
                .setTitle("Confirmation")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()  {
                    public void onClick(DialogInterface dialog, int id) {
                        newUser(globalEmail,globalPassword);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).create().show();
    }


    public boolean validateFields(){
        globalName = name.getText().toString().trim();
        globalEmail = email.getText().toString().trim();
        globalPassword = password.getText().toString();

        if(globalName.isEmpty()||globalEmail.isEmpty()||globalPassword.isEmpty()){
            Toast.makeText (this , "You need to fill all the fields" , Toast . LENGTH_LONG ) . show () ;
            return false;
        }
        else if(!existUri){
            Toast.makeText (this ,"You need to take a photo" , Toast . LENGTH_LONG ) . show () ;
            return false;
        }

        else{
            if(password.getText().length()<6){
                Toast.makeText (this ,"In the password field you must type more than 6 characters" , Toast . LENGTH_LONG ) . show () ;
                return false;
            }
            return true;
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
                existUri = true;
                imagen.setImageURI(uriImage);
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
