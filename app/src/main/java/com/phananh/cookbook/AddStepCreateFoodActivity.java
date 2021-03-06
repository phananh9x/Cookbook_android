package com.phananh.cookbook;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.phananh.api.APIServices;
import com.phananh.api.ApiUtils;
import com.phananh.api.results.GetUploadResults;
import com.phananh.dialog.ImageAdapter;
import com.phananh.model.Content;
import com.phananh.model.Image;
import com.phananh.model.UploadImage;
import com.phananh.sqlite.SQLiteDatabaseHandler;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by thanh on 06/05/2018.
 */

public class AddStepCreateFoodActivity extends AppCompatActivity {

    public static final String CONTENT_ADD_STEP = "CONTENT_ADD_STEP";

    Toolbar toolbar;
    TextView tvCreate;
    TextInputLayout edtStep;
    ImageView ivAddImage;
    RecyclerView rcvImage;

    private List<Image> images;
    private String IMAGE_PATH;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_step_create_food);

        initData();
        initListener();
    }

    private void initData() {
        images = new ArrayList<>();

        toolbar = (Toolbar) findViewById(R.id.toolBarAddStep);
        tvCreate = (TextView) findViewById(R.id.tv_create_step);
        edtStep = (TextInputLayout) findViewById(R.id.edt_step_create_food);
        ivAddImage = (ImageView) findViewById(R.id.iv_add_image_step);
        rcvImage = (RecyclerView) findViewById(R.id.rcv_image_step_create_food);

        toolbar.setTitle("Create Step Food");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        rcvImage.setLayoutManager(new LinearLayoutManager(this));
        rcvImage.setAdapter(new ImageAdapter(this, images));
    }

    private void initListener() {
        ivAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (images.size() < 4){
                    openImageStorage();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddStepCreateFoodActivity.this);
                    builder.setMessage("Max size image = 4!");
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
            }
        });
        tvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Content content = new Content(edtStep.getEditText().getText().toString(), images);
                Intent intent = new Intent(AddStepCreateFoodActivity.this, CreateFoodActivity.class);
                intent.putExtra(CONTENT_ADD_STEP, content);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void openImageStorage() {
        Intent fintent = new Intent(Intent.ACTION_GET_CONTENT);
        fintent.setType("image/*");
        try {
            startActivityForResult(fintent, 100);
        } catch (ActivityNotFoundException e) {

        }
    }

    public void UploadImage(){
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading!!");
        progressDialog.show();

        APIServices service = ApiUtils.getAPIService();

        File file = new File(IMAGE_PATH);
        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

        String token = new SQLiteDatabaseHandler(this).getToken();

        Call<GetUploadResults> call = service.uploadImage(token,body);
        call.enqueue(new Callback<GetUploadResults>() {

            @Override
            public void onResponse(Call<GetUploadResults> call, Response<GetUploadResults> response) {
                UploadImage image = response.body().getUploadImage();
                progressDialog.dismiss();
                images.add(new Image(ApiUtils.BASE_URL+image.getPath()));
                rcvImage.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<GetUploadResults> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;
        switch (requestCode) {
            case 100:
                if (resultCode == RESULT_OK) {
//                    imgFood.setImageURI(data.getData());
                    Uri selectedImage = data.getData();
                    String wholeID = DocumentsContract.getDocumentId(selectedImage);

                    // Split at colon, use second item in the array
                    String id = wholeID.split(":")[1];

                    String[] column = { MediaStore.Images.Media.DATA };

                    // where id is equal to
                    String sel = MediaStore.Images.Media._ID + "=?";

                    Cursor cursor = getContentResolver().
                            query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    column, sel, new String[]{ id }, null);


                    int columnIndex = cursor.getColumnIndex(column[0]);

                    if (cursor.moveToFirst()) {
                        IMAGE_PATH = cursor.getString(columnIndex);
                    }
                    cursor.close();
                    UploadImage();
                }
        }
    }
}
