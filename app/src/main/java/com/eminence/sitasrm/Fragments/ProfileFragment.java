package com.eminence.sitasrm.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.eminence.sitasrm.Activity.LanguageActivity;
import com.eminence.sitasrm.Activity.OnBordingScreen;
import com.eminence.sitasrm.Activity.Profile.AboutActivity;
import com.eminence.sitasrm.Activity.Profile.Address;
import com.eminence.sitasrm.Activity.Profile.Contactus;
import com.eminence.sitasrm.Activity.Profile.EditProfile;
import com.eminence.sitasrm.Activity.Profile.Feedback;
import com.eminence.sitasrm.Activity.Profile.PrivacyPolicies;
import com.eminence.sitasrm.Activity.Profile.SendQuery;
import com.eminence.sitasrm.Activity.Profile.Termcondition;
import com.eminence.sitasrm.Models.CartResponse;
import com.eminence.sitasrm.R;
import com.eminence.sitasrm.Utils.DatabaseHandler;
import com.eminence.sitasrm.Utils.Helper;
import com.eminence.sitasrm.Utils.YourPreference;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static android.app.Activity.RESULT_OK;
import static com.eminence.sitasrm.Fragments.CartFragment.subStatus;
import static com.eminence.sitasrm.Utils.Baseurl.baseurl;
import static com.eminence.sitasrm.Utils.Baseurl.imagebaseurl;

public class ProfileFragment extends Fragment {

    LinearLayout addressLayout, languageLayout, feedbackLayout, contactLayout, sendqueryLayout,
            aboutusLayout, termAndConditionLayout, privacyLayout, editLayout;
    RelativeLayout logoutLayout,userProfileLayout;
    ImageView img_profilephoto;
    TextView txt_name, txt_number, txt_emial, txt_dateofb,langugetext;
    String convertedimage;
    ProgressBar mainProgressBar;
    static DatabaseHandler databaseHandler;
    List<CartResponse> alldata;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        editLayout = view.findViewById(R.id.editLayout);
        addressLayout = view.findViewById(R.id.addressLayout);
        languageLayout = view.findViewById(R.id.languageLayout);
        feedbackLayout = view.findViewById(R.id.feedbackLayout);
        contactLayout = view.findViewById(R.id.contactLayout);
        sendqueryLayout = view.findViewById(R.id.sendqueryLayout);
        aboutusLayout = view.findViewById(R.id.aboutusLayout);
        termAndConditionLayout = view.findViewById(R.id.termAndConditionLayout);
        privacyLayout = view.findViewById(R.id.privacyLayout);
        logoutLayout = view.findViewById(R.id.logoutLayout);
        userProfileLayout = view.findViewById(R.id.userProfileLayout);
        langugetext = view.findViewById(R.id.langugetext);
        img_profilephoto = view.findViewById(R.id.img_profilephoto);
        txt_name = view.findViewById(R.id.txt_name);
        txt_number = view.findViewById(R.id.txt_number);
        txt_emial = view.findViewById(R.id.txt_emial);
        txt_dateofb = view.findViewById(R.id.txt_dateofb);
        mainProgressBar = view.findViewById(R.id.mainProgressBar);

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subStatus = "0";
                SharedPreferences preferences = getActivity().getSharedPreferences("YourCustomNamedPreference", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(getActivity(), OnBordingScreen.class);
                startActivity(intent);
                getActivity().finish();

            }
        });


        setUpDB();
        addtocart();

        YourPreference yourPrefrence = YourPreference.getInstance(getActivity());
        String language = yourPrefrence.getData("language");
        if (language.equalsIgnoreCase("en")||language.equalsIgnoreCase("")) {
            langugetext.setText("English");
        } else {
            langugetext.setText("हिंदी");
        }

        userProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageDialog(0,95);
            }

        });

        editLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditProfile.class);
                intent.putExtra("name",txt_name.getText().toString());
                intent.putExtra("mobile",txt_number.getText().toString());
                intent.putExtra("email",txt_emial.getText().toString());
                intent.putExtra("dob",txt_dateofb.getText().toString());
                startActivity(intent);
            }
        });

        addressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Address.class));
            }
        });

        languageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LanguageActivity.class));
            }
        });

        feedbackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Feedback.class));
            }
        });

        contactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Contactus.class));
            }
        });

        sendqueryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SendQuery.class));
            }
        });

        aboutusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
            }
        });

        termAndConditionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Termcondition.class));
            }
        });

        privacyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PrivacyPolicies.class));
            }
        });

        return view;
    }

    private void openImageDialog(final int upload, final int take) {

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater inflater = getLayoutInflater();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View convertView = inflater.inflate(R.layout.camera, null);
        LinearLayout camera = convertView.findViewById(R.id.camera);
        LinearLayout gallery = convertView.findViewById(R.id.gallery);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(intent, take);
                }
                alertDialog.dismiss();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, upload);
                alertDialog.dismiss();

            }
        });

        alertDialog.setView(convertView);
        alertDialog.show();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (Helper.INSTANCE.isNetworkAvailable(getActivity())){
            getUserProfile();
        } else {
            Helper.INSTANCE.Error(getActivity(), getString(R.string.NOCONN));
        }

    }

    public void getUserProfile() {
        String url = baseurl + "user_profile";
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        YourPreference yourPrefrence = YourPreference.getInstance(getActivity());
        String id = yourPrefrence.getData("id");
        Map<String, String> params = new HashMap();
        params.put("user_id", id);
        mainProgressBar.setVisibility(View.VISIBLE);

        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mainProgressBar.setVisibility(View.GONE);
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");
                    if (r_code.equalsIgnoreCase("1")) {
                        JSONArray jsonArray = obj.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                            String name = jsonObject2.getString("name");
                            String mobile = jsonObject2.getString("mobile");
                            String email = jsonObject2.getString("email");
                            String dob = jsonObject2.getString("birth_date");
                            String profilephoto = jsonObject2.getString("profile_photo");

                            txt_name.setText(name);
                            txt_number.setText(mobile);
                            txt_emial.setText(email);
                            txt_dateofb.setText(dob);

                            Glide.with(getActivity()).load(imagebaseurl +profilephoto)
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.app_logo)
                                            .centerCrop()
                                            .error(R.drawable.app_logo))
                                    .into(img_profilephoto);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mainProgressBar.setVisibility(View.GONE);
            }
        }) {

        };
        requestQueue.add(stringRequest);
        stringRequest.setShouldCache(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Uri targetUri = data.getData();
                Bitmap bitmap;
                try {
                    bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(targetUri));
                    float aspectRatio = bitmap.getWidth() / (float) bitmap.getHeight();
                    int width = 280;
                    int height = Math.round(width / aspectRatio);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                    convertedimage = ConvertBitmapToString(resizedBitmap);


                    if (Helper.INSTANCE.isNetworkAvailable(getActivity())){
                        updateprofilephoto();
                    } else {
                        Helper.INSTANCE.Error(getActivity(), getString(R.string.NOCONN));
                    }

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        if (data != null && requestCode == 95) {
            if (resultCode == RESULT_OK) {
                try {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    float aspectRatio = bitmap.getWidth() /
                            (float) bitmap.getHeight();
                    int width = 280;
                    int height = Math.round(width / aspectRatio);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                    convertedimage = ConvertBitmapToString(resizedBitmap);
                    if (Helper.INSTANCE.isNetworkAvailable(getActivity())){
                        updateprofilephoto();
                    } else {
                        Helper.INSTANCE.Error(getActivity(), getString(R.string.NOCONN));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), ""+e, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String ConvertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        return base64String;
    }

    public void updateprofilephoto() {
        String url = baseurl + "user_photo_update";
        YourPreference yourPrefrence = YourPreference.getInstance(getActivity());
        String id = yourPrefrence.getData("id");
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        Map<String, String> params = new HashMap();
        params.put("user_id", id);
        params.put("profile_photo",convertedimage);

        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");
                    Toast.makeText(getActivity(), ""+obj.getString("message"), Toast.LENGTH_SHORT).show();
                    if (r_code.equalsIgnoreCase("1")) {
                        YourPreference yourPrefrence = YourPreference.getInstance(getActivity());
                        yourPrefrence.saveData("profile",obj.getString("profile_photo"));
                        Glide.with(getActivity()).load(imagebaseurl +obj.getString("profile_photo"))
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.loading)
                                        .centerCrop()
                                        .error(R.drawable.loading))
                                .into(img_profilephoto);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), ""+error, Toast.LENGTH_SHORT).show();
            }
        }) {

        };
        requestQueue.add(stringRequest);
        stringRequest.setShouldCache(false);
    }

    private void setUpDB() {
        databaseHandler = Room.databaseBuilder(getActivity(), DatabaseHandler.class, "cart").allowMainThreadQueries().build();
    }

    private void addtocart() {
        alldata = databaseHandler.cartInterface().getallcartdata();
        JSONArray jsonArray = null;
        String json = String.valueOf(new Gson().toJsonTree(alldata));
        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        YourPreference yourPrefrence = YourPreference.getInstance(getActivity());
        String user_id = yourPrefrence.getData("id");
        String url = baseurl + "add_to_cart";
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        JSONObject params = new JSONObject();

        try {
            params.put("user_id", user_id);
            params.put("products", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("My Cart Parameter",""+params);

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String status_code = obj.getString("status");
                    if (status_code.equalsIgnoreCase("1")) {

                    } else {

                        }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ABC", String.valueOf(error));
            }
        }) {

        };
        requestQueue.add(stringRequest);
        stringRequest.setShouldCache(false);
    }

}