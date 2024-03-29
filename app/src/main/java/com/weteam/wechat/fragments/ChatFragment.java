package com.weteam.wechat.fragments;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.squareup.picasso.Picasso;
import com.weteam.wechat.R;
import com.weteam.wechat.adapters.StoryAdapter;
import com.weteam.wechat.adapters.UserAdapter;
import com.weteam.wechat.animations.AnimationScale;
import com.weteam.wechat.database.FirebaseManager;
import com.weteam.wechat.databinding.FragmentChatBinding;
import com.weteam.wechat.models.Story;
import com.weteam.wechat.models.User;
import com.weteam.wechat.models.UserStory;
import com.weteam.wechat.utils.LoadingDialog;
import com.weteam.wechat.utils.MyToast;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    private FragmentChatBinding fragmentChatBinding;

    private static final String USER_STORY_STORAGE = "USER_STORY/";
    private static final String USER_DATABASE = "users";
    private static final String STORY_DATABASE = "users_stories";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private FirebaseManager firebaseManager;

    private List<User> userList;
    private UserAdapter userAdapter;

    private StoryAdapter storyAdapter;
    private List<UserStory> userStoryList;

    private AnimationScale animationScale;
    private LoadingDialog loadingDialog;

    private User mUser;
    private long userStoryCount;

    public ChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentChatBinding = FragmentChatBinding.inflate(inflater, container, false);
        return fragmentChatBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        animationScale = AnimationScale.getInstance();
        loadingDialog = LoadingDialog.getInstance();

        firebaseManager = FirebaseManager.getInstance();

        initializeViews();
//        listeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentChatBinding = null;
    }

    private void initializeViews() {
//        scale
        animationScale.eventConstraintLayout(getContext(), fragmentChatBinding.cslStory);

//        get user info
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            //get conversations from firebase
            userList = new ArrayList<>();
            userAdapter = new UserAdapter(getActivity(), userList);
            fragmentChatBinding.rvChat.setAdapter(userAdapter);

            firebaseDatabase.getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        userList.add(user);
                    }
                    userAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            //Get stories
            FirebaseManager.getInstance().setReadUserInformation(new FirebaseManager.GetUserInformationListener() {
                @Override
                public void getUserInformationListener(User user) {
                    if (user != null) {
//                        mUser = new User();
                        mUser = user;

                        Picasso.get()
                                .load(user.getAvatar())
                                .placeholder(R.drawable.ic_user_avatar)
                                .error(R.drawable.ic_user_avatar)
                                .into(fragmentChatBinding.civStory);

                        fragmentChatBinding.cvUpper.setVisibility(View.VISIBLE);
                        listeners();
                    }
                }
            });
            FirebaseManager.getInstance().getUserInfo(currentUser.getUid().trim());


            firebaseDatabase.getReference().child(STORY_DATABASE.trim()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userStoryList = new ArrayList<>();
                        userStoryCount =  snapshot.getChildrenCount();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            UserStory userStory = new UserStory();
                            userStory.setName(dataSnapshot.child("name").getValue(String.class));
                            userStory.setAvatar(dataSnapshot.child("avatar").getValue(String.class));
                            userStory.setLastUpdated(dataSnapshot.child("last_updated").getValue(String.class));

                            List<Story> storyList = new ArrayList<>();
                            try{
                                DataSnapshot child = dataSnapshot.child("story_list");
                                Map<String, Map<String, String>> data = (Map<String, Map<String, String>>)child.getValue();

                                if(data != null) {
                                    for (Map.Entry<String, Map<String, String>> map : data.entrySet()) {
                                        Story story = new Story();
                                        story.setImage(map.getValue().get("image"));
                                        story.setTime(map.getValue().get("time"));
                                        storyList.add(story);
                                    }
                                    userStory.setStoryList(storyList);
                                    userStoryList.add(userStory);
                                }
                            } catch (Error error){
                                Log.e("ERROR ", error.toString());
                            }
                        }

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
                        fragmentChatBinding.rvStory.setLayoutManager(linearLayoutManager);
                        storyAdapter = new StoryAdapter(getContext(), userStoryList);
                        fragmentChatBinding.rvStory.setHasFixedSize(true);
                        fragmentChatBinding.rvStory.setAdapter(storyAdapter);

                        fragmentChatBinding.sflItemStory.setVisibility(View.GONE);
                        fragmentChatBinding.rvStory.setVisibility(View.VISIBLE);
                    } else {
                        fragmentChatBinding.sflItemStory.setVisibility(View.GONE);
                        fragmentChatBinding.rvStory.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void listeners() {
        fragmentChatBinding.cslStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionListener permissionlistener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        selectImageFromGallery();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        MyToast.makeText(getContext(), MyToast.WARNING, getString(R.string.toast7) + deniedPermissions.toString(), MyToast.SHORT).show();
                    }
                };

                TedPermission.with(getContext())
                        .setPermissionListener(permissionlistener)
                        .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                        .check();
            }
        });
    }

    private void selectImageFromGallery() {
        TedBottomPicker.with((FragmentActivity) getContext())
                .setPeekHeight(1600)
                .showTitle(false)
                .setSelectMaxCount(5)
                .setSelectMaxCountErrorText(getString(R.string.tedBottomPicker3))
                .setCompleteButtonText(getString(R.string.tedBottomPicker1))
                .setEmptySelectionText(getString(R.string.tedBottomPicker2))
//                .setSelectedUriList(selectedUriList)
                .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
                    @Override
                    public void onImagesSelected(List<Uri> uriList) {
                        if (uriList != null && !uriList.isEmpty()) {
                            addDataToFirebase(uriList);
                        }
                    }
                });
    }

    private void addDataToFirebase(List<Uri> uriList) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            loadingDialog.startLoading(getContext(), false);

            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            String time = dateFormat.format(date);

            StorageReference storageReference = firebaseStorage.getReference()
                    .child(USER_STORY_STORAGE.trim() + currentUser.getUid().trim() + "/" + time.trim());
            for (int i = 0; i < uriList.size(); i++) {
                Uri individualImage = uriList.get(i);
                StorageReference imageName = storageReference.child(individualImage.getLastPathSegment().trim());
                imageName.putFile(individualImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String storyImage = uri.toString().trim();

                                    UserStory userStory = new UserStory();
                                    userStory.setName(mUser.getName());
                                    userStory.setAvatar(mUser.getAvatar());
                                    userStory.setLastUpdated(time.trim());

                                    HashMap<String, Object> userStoryObj = new HashMap<>();
                                    userStoryObj.put("name", userStory.getName());
                                    userStoryObj.put("avatar", userStory.getAvatar());
                                    userStoryObj.put("last_updated", userStory.getLastUpdated());

                                    Story story = new Story(storyImage, userStory.getLastUpdated());

                                    firebaseDatabase.getReference()
                                            .child(STORY_DATABASE.trim())
                                            .child(String.valueOf(userStoryCount))
                                            .updateChildren(userStoryObj);

                                    firebaseDatabase.getReference()
                                            .child(STORY_DATABASE.trim())
                                            .child(String.valueOf(userStoryCount))
                                            .child("story_list")
                                            .push()
                                            .setValue(story);

                                    loadingDialog.cancelLoading();
                                    MyToast.makeText(getContext(), MyToast.SUCCESS, getString(R.string.toast9), MyToast.SHORT).show();
                                }
                            });
                        } else {
                            MyToast.makeText(getContext(), MyToast.ERROR, getString(R.string.toast8), MyToast.SHORT).show();
                        }
                    }
                });
            }
        }
    }

//    ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
//            new ActivityResultContracts.GetContent(),
//            new ActivityResultCallback<Uri>() {
//                @Override
//                public void onActivityResult(Uri result) {
//                    if (result != null) {
//                    }
//                }
//            });
}