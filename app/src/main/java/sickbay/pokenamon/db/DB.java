package sickbay.pokenamon.db;

import android.app.Activity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.model.User;

public class DB {
    static FirebaseAuth firebaseAuth;
    static FirebaseDatabase firebaseDatabase;
    static Activity activity;

    public static synchronized DB getAuthInstance(Activity activity) {
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();
            DB.activity = activity;
        }

        return new DB();
    }

    public static synchronized DB getDatabaseInstance() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
        }

        return new DB();
    }

    public DatabaseReference getUsersReference() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
        }

        return firebaseDatabase.getReference("users");
    }

    public DatabaseReference getUsersInventoryReference() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
        }

        return firebaseDatabase.getReference("user_inventory");
    }

    public DatabaseReference getGachaMetadataReference() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
        }

        return firebaseDatabase.getReference("gacha_metadata");
    }

    public DatabaseReference getGachaMetadataTierReference(String tier) {
        return getGachaMetadataReference().child(tier);
    }

    public DatabaseReference getUserReference(String uid) {
        return getUsersReference().child(uid);
    }

    public DatabaseReference getUserInventoryReference(String uid) {
        return getUsersInventoryReference().child(uid);
    }
    
    public FirebaseUser getAuthUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void createAuthUser(String email, String password, OnCompleteListener<AuthResult> completeListener, OnFailureListener failureListener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, completeListener)
                .addOnFailureListener(activity, failureListener);
    }

    public void createUser(String uid, User user, OnCompleteListener<Void> completeListener, OnFailureListener failureListener) {
        getUserReference(uid).setValue(user)
                .addOnCompleteListener(activity, completeListener)
                .addOnFailureListener(activity, failureListener);
    }

    public void signInAuthUser(String email, String password, OnCompleteListener<AuthResult> completeListener, OnFailureListener failureListener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, completeListener)
                .addOnFailureListener(activity, failureListener);
    }

    public void signOutAuthUser() {
        firebaseAuth.signOut();
        UserManager.getInstance().setUser(null);
    }

    public void deleteUserPokemon(String uid, String collectionId) {
        getUserInventoryReference(uid).child(collectionId).removeValue();
    }
}
