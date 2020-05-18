package com.agobal.DoctorAid.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.agobal.DoctorAid.AccountActivity.ProfileEdit;
import com.agobal.DoctorAid.Entities.UserData;
import com.agobal.DoctorAid.R;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private StorageReference mImageStorage;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mUserRequestsDatabase;

    private static final int GALLERY_PICK = 1;

    //private ArrayList<Books> BookList = new ArrayList<>();
    private final FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final String current_uid = Objects.requireNonNull(mCurrentUser).getUid();


    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String address;



    private String userID;
    private TextView tvEmpty;
    ProgressBar spinner;



    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        spinner = v.findViewById(R.id.progressBar);


        spinner.setVisibility(View.VISIBLE);

        ViewCompat.setNestedScrollingEnabled(v, true);

        final TextView T_firstAndLastName = v.findViewById(R.id.p_firstAndLastName);
        final TextView T_Desc = v.findViewById(R.id.p_email);
        final TextView T_UserName = v.findViewById(R.id.p_username);
        final TextView T_Address = v.findViewById(R.id.p_address);
        final CircleImageView ProfilePic = v.findViewById(R.id.profilePic);
        final TextView T_RequestCount = v.findViewById(R.id.p_requestCount);
        ImageButton BtnProfileEdit = v.findViewById(R.id.profileEditBtn);


        mImageStorage = FirebaseStorage.getInstance().getReference();
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = Objects.requireNonNull(mCurrentUser).getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("Requests").child(current_uid);


        ProfilePic.setOnClickListener(view -> {
            //Profile photo button
            showFileChooser();
        });

        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("userName").getValue(String.class);
                email = dataSnapshot.child("email").getValue(String.class);
                firstName = dataSnapshot.child("firstName").getValue(String.class);
                lastName = dataSnapshot.child("lastName").getValue(String.class);
                address = dataSnapshot.child("address").getValue(String.class);
                //String thumb_image = dataSnapshot.child("thumb_image").getValue(String.class);
                final String image = dataSnapshot.child("image").getValue(String.class);

                assert image != null;
                if(!image.equals("default")){
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.unknown_profile_pic)
                            .into(ProfilePic, new Callback() {
                                @Override
                                public void onSuccess() {
                                    spinner.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(image).placeholder(R.drawable.unknown_profile_pic).into(ProfilePic);
                                }

                            });

                }


                UserData userData = new UserData();

                userData.setFirstName(firstName);
                userData.setLastName(lastName);
                userData.setEmail(email);
                userData.setUserName(userName);
                userData.setAddress(address);

                T_firstAndLastName.setText(userData.firstName+" "+userData.lastName);
                T_Desc.setText(userData.email);
                T_UserName.setText(userName);
                T_Address.setText(address);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }) ;

        mUserRequestsDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                long RequestsCount = dataSnapshot.getChildrenCount();
                String RequestCountText ="Įvykdytų užklausų skaičius: " + RequestsCount;
                T_RequestCount.setText(RequestCountText);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }) ;


        BtnProfileEdit.setOnClickListener(view -> {
            //Profile edit button
            Intent intent = new Intent(getActivity(), ProfileEdit.class);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("email", email);
            intent.putExtra("userName", userName);
            intent.putExtra("address", address);
            startActivity(intent);
        });

        return v;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            //start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(Objects.requireNonNull(getActivity()),this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {


                spinner.setVisibility(View.VISIBLE);


                Uri resultUri = result.getUri();

                File thumb_filePath = new File(Objects.requireNonNull(resultUri.getPath()));

                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                String current_user_id = Objects.requireNonNull(user).getUid();

                Bitmap thumb_bitmap = null;

                try {
                    thumb_bitmap = new Compressor(Objects.requireNonNull(getContext()).getApplicationContext())
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (thumb_bitmap != null) {
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                }
                else
                    Log.d(TAG, "THUMB NEPAVYKO");

                //final byte[] thumb_byte = baos.toByteArray();

                final StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_user_id + ".jpg");

                filepath.putFile(resultUri).addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl().addOnSuccessListener(uri -> {

                    String download_url = uri.toString();
                    mUserDatabase.child("image").setValue(download_url);
                    spinner.setVisibility(View.GONE);
                }));

                thumb_filepath.putFile(resultUri).addOnSuccessListener(taskSnapshot -> thumb_filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                    String thumb_download_url = uri.toString();
                    mUserDatabase.child("thumb_image").setValue(thumb_download_url);
                    spinner.setVisibility(View.GONE);

                }));

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

            }
        }
    }

    private void showFileChooser() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(galleryIntent, "Pasirinkite nuotrauką"), GALLERY_PICK);
    }



}
