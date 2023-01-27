package com.example.forumlearning.ui;

import static com.example.forumlearning.MainActivity.MY_REQUEST_CODE;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.forumlearning.FirebaseHelper;
import com.example.forumlearning.MainActivity;
import com.example.forumlearning.MyQuestion;
import com.example.forumlearning.R;
import com.example.forumlearning.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import javax.annotation.Nullable;

public class MyProfileFragment extends Fragment {

    private View mView;
    private ImageView imgAvatar;
    private EditText edtFullName, edtEmail, edtPhone, edtAddress;
    private Button btnUpdateProfile;
    private Uri mUri;
    private MainActivity mainActivity;
    private ProgressDialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_my_profile, container, false);

        initUi();
        mainActivity = (MainActivity) getActivity();
        progressDialog = new ProgressDialog(getActivity());
        setUserInformation();
        initListener();

        return mView;
    }

    private void initUi() {
        imgAvatar = mView.findViewById(R.id.img_avatar);
        edtFullName = mView.findViewById(R.id.edt_fullname);
        edtEmail = mView.findViewById(R.id.edt_email);
        edtPhone = mView.findViewById(R.id.edt_phone_number);
        edtAddress = mView.findViewById(R.id.edt_address);
        btnUpdateProfile = mView.findViewById(R.id.btn_update_profile);
    }

    private void setUserInformation() {
        FirebaseUser user = FirebaseHelper.getCurrentUser();
        if (user == null) return;
        // get avatar image form DB
        StorageReference avatarsStorageRef = FirebaseHelper.mFStorage.child("assets/avatars/" + user.getUid());

        avatarsStorageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                try {
                    Glide.with(getActivity())
                            .load(task.getResult())
                            .error(R.drawable.ic_avatar_default)
                            .into(imgAvatar);
                } catch (Exception _) {
                }
            }
        });

        DatabaseReference myRef = FirebaseHelper.mDatabaseReference.child("users");
        Query query = myRef.child(user.getUid());
        query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    User userData = task.getResult().getValue(User.class);
                    if (userData != null) {
                        edtFullName.setText(userData.getFullname());
                        edtEmail.setText(userData.getEmail());
                        edtPhone.setText(userData.getPhoneNumber());
                        edtAddress.setText(userData.getAddress());
                    } else {
                        edtFullName.setText(user.getDisplayName());
                        edtEmail.setText(user.getEmail());
                    }
                }

            }
        });
        ValueEventListener userDataEventListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userData = snapshot.getValue(User.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initListener() {
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRequestPermission();
            }

        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickUpdateProfile();
            }
        });
    }

    private void onClickUpdateProfile() {
        FirebaseUser userFirebaseAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userFirebaseAuth == null) return;
        String strFullName = edtFullName.getText().toString().trim();
        String strEmail = edtEmail.getText().toString().trim();
        String strPhoneNumber = edtPhone.getText().toString().trim();
        String strAddress = edtAddress.getText().toString().trim();
        if (strFullName == null || strEmail == null) {
            Toast.makeText(getActivity(), "Xin hãy nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        // Update Email for userFirebaseAuth
        if (userFirebaseAuth.getEmail() != strEmail) {
            userFirebaseAuth.updateEmail(strEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mainActivity.showUserInformation();
                            }
                        }
                    });
        }
        // TODO: Update Phone for userFirebaseAuth (nice to have)

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(strFullName)
//                .setPhotoUri(mUri)
                .build();

        userFirebaseAuth.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mainActivity.showUserInformation();
                        }
                    }
                });

        // save new avatar to DB firebase
        if (mUri != null) {
            StorageReference avatarsStorageRef = FirebaseHelper.mFStorage.child("assets/avatars/" + userFirebaseAuth.getUid());
            avatarsStorageRef.putFile(mUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//                        Toast.makeText(getActivity(), "Cập nhập Profile thành công!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getActivity(), "Cập nhập Profile thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        User userTemp = new User(userFirebaseAuth.getUid(), strEmail, strFullName, strPhoneNumber, strAddress);
        DatabaseReference myRef = FirebaseHelper.mDatabaseReference.child("users");
        myRef.child(userFirebaseAuth.getUid()).updateChildren(userTemp.toMap(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                Toast.makeText(getActivity(), "Update Profile success!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void onClickRequestPermission() {
        if (mainActivity == null) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mainActivity.openGllery();
            return;
        }

        if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mainActivity.openGllery();
        } else {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            getActivity().requestPermissions(permissions, MY_REQUEST_CODE);
        }
    }

    public void setBitmapImageView(Bitmap bitmapImageView) {
        imgAvatar.setImageBitmap(bitmapImageView);
    }

    public void setUri(Uri mUri) {
        this.mUri = mUri;
    }
}