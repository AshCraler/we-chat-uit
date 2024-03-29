package com.weteam.wechat.activities;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
//import com.squareup;
import com.squareup.picasso.Picasso;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StringeeConnectionListener;
import com.weteam.wechat.R;
import com.weteam.wechat.adapters.ViewPagerAdapter;
import com.weteam.wechat.animations.AnimationScale;
import com.weteam.wechat.animations.ZoomOutPageTransformer;
import com.weteam.wechat.database.FirebaseManager;
import com.weteam.wechat.databinding.ActivityMainBinding;
import com.weteam.wechat.utils.Common;
import com.weteam.wechat.utils.MyToast;

import org.json.JSONObject;

public class MainActivity extends AppCompat {
    private ActivityMainBinding activityMainBinding;
    private static final String TAG = "MainActivity";
    private static final String STRINGEE = "Stringee";

    private FirebaseManager firebaseManager;

    private ViewPagerAdapter viewPagerAdapter;
    private FirebaseAuth firebaseAuth;

    private AnimationScale animationScale;

    private Toast myToast;
    private long BACK_PRESS_TIME = 0;

//    // Real device
    private final String token1 = "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTSy4wLjZleGxPUnpLcEJYbGtqRUtCRXBlVDlaU3ZtUGFsZ00tMTY3NzMwNTc4NyIsImlzcyI6IlNLLjAuNmV4bE9SektwQlhsa2pFS0JFcGVUOVpTdm1QYWxnTSIsImV4cCI6MTY3OTg5Nzc4NywidXNlcklkIjoicXF6Z1NKY1VKTU1xV2tuNkJFbmxNakNFenY3MyJ9.Mi1iZTrlBTGsUJV3ZO1dXFM29lYWjiMqI_M0YDgCup8";
//
//    // Virtual device
    private final String token2 = "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTSy4wLjZleGxPUnpLcEJYbGtqRUtCRXBlVDlaU3ZtUGFsZ00tMTY3NzMwNTgxMSIsImlzcyI6IlNLLjAuNmV4bE9SektwQlhsa2pFS0JFcGVUOVpTdm1QYWxnTSIsImV4cCI6MTY3OTg5NzgxMSwidXNlcklkIjoiNFFIUVFidXh4TGNIUGpqVWVOT1MwYUxsQk9KMyJ9.OGI8SCAanP4gnGt2OolLPmm3TD7kLvfFYfKdRxezb9U";
    public static StringeeClient stringeeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        firebaseManager = FirebaseManager.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        animationScale = AnimationScale.getInstance();

        initializeViews();
        listeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatusOnline();
    }

    @Override
    protected void onDestroy() {
        setStatusOffline();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (BACK_PRESS_TIME + 2000 > System.currentTimeMillis()) {
            myToast.cancel();
            super.onBackPressed();
            return;
        } else {
            myToast = MyToast.makeText(MainActivity.this, MyToast.INFORMATION, getString(R.string.toast13), MyToast.SHORT);
            myToast.show();
        }
        BACK_PRESS_TIME = System.currentTimeMillis();
    }

    private void initializeViews() {
        //Animation scale
        animationScale.eventCircleImageView(this, activityMainBinding.civAvatar);

        //Set Pager adapter as bottom nav bar
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        activityMainBinding.vp2ViewPager2.setAdapter(viewPagerAdapter);
        activityMainBinding.vp2ViewPager2.setCurrentItem(0); // Set default fragment
        activityMainBinding.vp2ViewPager2.setOffscreenPageLimit(2);
        activityMainBinding.vp2ViewPager2.setPageTransformer(new ZoomOutPageTransformer()); // Set animation change page

        //Get user from Firebse

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Log.e("firebaseeeeeeeeeee", "not null");
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String token) {
                    if (token != null && !token.isEmpty()) {
                        FirebaseManager.getInstance().setUserToken(currentUser.getUid().trim(), token);
                    }
                }
            });

            firebaseManager.getUserAvatar(currentUser.getUid().trim());
            firebaseManager.setReadUserAvatar(new FirebaseManager.GetUserAvatarListener() {
                @Override
                public void getUserAvatarListener(String avatar) {
                    Picasso.get()
                            .load(avatar)
                            .placeholder(R.drawable.ic_user_avatar)
                            .error(R.drawable.ic_user_avatar)
                            .into(activityMainBinding.civAvatar);
                }
            });

            initAndConnectStringee();
        } else {
            Log.e("firebaseeeeeeeeeee", " null");

            FirebaseMessaging.getInstance().getToken().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    private void initAndConnectStringee() {
        stringeeClient = new StringeeClient(this);
        stringeeClient.setConnectionListener(new StringeeConnectionListener() {
            @Override
            public void onConnectionConnected(StringeeClient stringeeClient, boolean b) {
                Log.d(STRINGEE, "Connect successfully");
            }

            @Override
            public void onConnectionDisconnected(StringeeClient stringeeClient, boolean b) {
                Log.d(STRINGEE, "Disconnected Connect");
            }

            @Override
            public void onIncomingCall(StringeeCall stringeeCall) {
                Log.d(STRINGEE, "onIncommingCall");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Common.isInCall) {
                            stringeeCall.reject();
                        } else {
                            Common.callsMap.put(stringeeCall.getCallId(), stringeeCall);
                            Intent intent = new Intent(MainActivity.this, InComingCallActivity.class);
                            intent.putExtra("CALL_ID", stringeeCall.getCallId());
                            startActivity(intent);
                        }
                    }
                });
            }

            @Override
            public void onIncomingCall2(StringeeCall2 stringeeCall2) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (Common.isInCall) {
//                            stringeeCall2.reject();
//                        } else {
//                            Common.calls2Map.put(stringeeCall2.getCallId(), stringeeCall2);
//                            Intent intent = new Intent(MainActivity.this, IncomingCall2Activity.class);
//                            intent.putExtra("call_id", stringeeCall2.getCallId());
//                            startActivity(intent);
//                        }
//                    }
//                });
            }

            @Override
            public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
                Log.d("Stringee", "StringeeClient fails to connect: " + stringeeError.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Common.reportMessage(MainActivity.this, "StringeeClient fails to connect: " + stringeeError.getMessage());
                    }
                });
            }

            @Override
            public void onRequestNewToken(StringeeClient stringeeClient) {
            }

            @Override
            public void onCustomMessage(String s, JSONObject jsonObject) {
            }

            @Override
            public void onTopicMessage(String s, JSONObject jsonObject) {
            }
        });

        stringeeClient.connect(token1); //Important
    }

    private void listeners() {
        activityMainBinding.civAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });

        activityMainBinding.bnvNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menuActionChat) {
                    activityMainBinding.vp2ViewPager2.setCurrentItem(0);
                } else if (item.getItemId() == R.id.menuActionPeople) {
                    activityMainBinding.vp2ViewPager2.setCurrentItem(1);
                } else if (item.getItemId() == R.id.menuActionSetting) {
                    activityMainBinding.vp2ViewPager2.setCurrentItem(2);
                }
                return false;
            }
        });

        activityMainBinding.vp2ViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                switch (position) {
                    case 0: {
                        activityMainBinding.bnvNav.getMenu().findItem(R.id.menuActionChat).setChecked(true);
                        break;
                    }
                    case 1: {
                        activityMainBinding.bnvNav.getMenu().findItem(R.id.menuActionPeople).setChecked(true);
                        break;
                    }
                    case 2: {
                        activityMainBinding.bnvNav.getMenu().findItem(R.id.menuActionSetting).setChecked(true);
                        break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    private void setStatusOnline() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseManager.setStatusOnline(currentUser.getUid().trim());
        }
    }

    private void setStatusOffline() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseManager.setStatusOffline(currentUser.getUid().trim());
        }
    }
}