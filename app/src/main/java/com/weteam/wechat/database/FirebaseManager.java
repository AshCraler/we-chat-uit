package com.weteam.wechat.database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.weteam.wechat.models.Conversation;
import com.weteam.wechat.models.User;

import java.util.ArrayList;
import java.util.HashMap;

///Class
public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;

    //    private static final String USER_AVATAR_STORAGE = "User_Avatar";
//    private static final String USER_DATABASE = "users";
//    private static final String STATUS_DATABASE = "status";
    private static final String STATUS_DATABASE_ONLINE = "online";
    private static final String STATUS_DATABASE_OFFLINE = "offline";
    private static final String TOKEN = "token";

    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private static final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private FirebaseManager() {
    }

    //singleton
    public static FirebaseManager getInstance() {
        if (instance == null) {
            synchronized (FirebaseManager.class) {
                if (instance == null) {
                    instance = new FirebaseManager();
                }
            }
        }
        return instance;
    }


    public interface GetUserAvatarListener {
        void getUserAvatarListener(String avatar);
    }

    private GetUserAvatarListener getUserAvatarListener;

    public void setReadUserAvatar(GetUserAvatarListener getUserAvatarListener) {
        this.getUserAvatarListener = getUserAvatarListener;
    }

    public interface GetUserInformationListener {
        void getUserInformationListener(User user);
    }

    private GetUserInformationListener getUserInformationListener;

    public void setReadUserInformation(GetUserInformationListener getUserInformationListener) {
        this.getUserInformationListener = getUserInformationListener;
    }

    public interface GetUserNameListener {
        void getUserNameListener(String name);
    }

    private GetUserNameListener getUserNameListener;

    public void setReadUserName(GetUserNameListener getUserNameListener) {
        this.getUserNameListener = getUserNameListener;
    }

    public void getUserAvatar(String uid) {
        firebaseDatabase.getReference().child("users/" + uid.trim() + "/avatar").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    getUserAvatarListener.getUserAvatarListener(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("firebase", error.getMessage().trim());
            }
        });
    }

    public void getUserInfo(String uid) {
        firebaseDatabase.getReference().child("users/" + uid.trim()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    getUserInformationListener.getUserInformationListener(snapshot.getValue(User.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("firebase", error.getMessage().trim());
            }
        });
    }

    public void getUserName(String uid) {
        firebaseDatabase.getReference().child("users/" + uid.trim() + "/name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    getUserNameListener.getUserNameListener(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("firebase", error.getMessage().trim());
            }
        });
    }

    public void setStatusOnline(String uid) {
        firebaseDatabase.getReference().child("users/" + uid.trim() + "/status").setValue(STATUS_DATABASE_ONLINE.trim());
    }

    public void setStatusOffline(String uid) {
        firebaseDatabase.getReference().child("users/" + uid.trim() + "/status").setValue(STATUS_DATABASE_OFFLINE.trim());
    }

    public void setUserToken(String uid, String token) {
        HashMap<String, Object> setToken = new HashMap<>();
        setToken.put(TOKEN, token);
        firebaseDatabase.getReference().child("users/" + uid.trim()).updateChildren(setToken);

        Log.d(TAG, token.trim());
    }

    public void getConversation(String id) {
        firebaseFirestore.collection("/conversation")
                .document("/" + id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot queryDocumentSnapshots) {
                Log.e("document cvs01", queryDocumentSnapshots.get("messages", ArrayList.class ).toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("document cvs01", e.toString());
                    }
                });
    }
}
