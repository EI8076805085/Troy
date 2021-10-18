package com.eminence.sitasrm.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.eminence.sitasrm.MainActivity;
import com.eminence.sitasrm.R;
import com.eminence.sitasrm.Utils.Helper;
import com.eminence.sitasrm.Utils.YourPreference;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static com.eminence.sitasrm.Utils.Baseurl.baseurl;

public class OTPScreen extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout otpverify_Layout;
    LinearLayout resendOTPLayout;
    String token;
    EditText ed1, ed2, ed3, ed4;/*ed5,ed6*/
    String number, otp, name, type, otpNote;
    TextView numbertext,txt_timeCounter;
    ProgressBar mainProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpscreen);
        intialize_views();

        number=getIntent().getExtras().getString("number");
        name=getIntent().getExtras().getString("name");
        type=getIntent().getExtras().getString("type");

        YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
        String language = yourPrefrence.getData("language");

        if (language.equalsIgnoreCase("hi")) {
            otpNote ="कृपया अपना मोबाइल नंबर जांचें "+ number +" \nलॉग इन के लिए जारी रखें";
        } else {
            otpNote ="Please check your mobile number "+ number+" \ncontinue to Login";
        }

        numbertext.setText(otpNote);

        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                txt_timeCounter.setText(millisUntilFinished / 1000 + " Sec");
            }

            public void onFinish() {
                txt_timeCounter.setVisibility(View.GONE);
                resendOTPLayout.setVisibility(View.VISIBLE);
            }

        }.start();

        // Get Firebase Token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            token = task.getResult().getToken();
                        } else {
                            token = "";
                        }

                    }
                });

        ed1.addTextChangedListener(new GenericTextWatcher(ed2, ed1));
        ed2.addTextChangedListener(new GenericTextWatcher(ed3, ed1));
        ed3.addTextChangedListener(new GenericTextWatcher(ed4, ed2));
        ed4.addTextChangedListener(new GenericTextWatcher(ed4, ed3));

    }

    public class GenericTextWatcher implements TextWatcher {
        private EditText etPrev;
        private EditText etNext;

        public GenericTextWatcher(EditText etNext, EditText etPrev) {
            this.etPrev = etPrev;
            this.etNext = etNext;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            if (text.length() == 1)
                etNext.requestFocus();
            else if (text.length() == 0)
                etPrev.requestFocus();
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }
    }


    private void intialize_views() {
        ed1 = findViewById(R.id.ed1);
        ed2 = findViewById(R.id.ed2);
        ed3 = findViewById(R.id.ed3);
        ed4 = findViewById(R.id.ed4);

        numbertext = findViewById(R.id.numbertext);
        otpverify_Layout = findViewById(R.id.otpverify_Layout);
        resendOTPLayout = findViewById(R.id.resendOTPLayout);
        txt_timeCounter = findViewById(R.id.txt_timeCounter);
        mainProgressBar = findViewById(R.id.mainProgressBar);

        otpverify_Layout.setOnClickListener(this);
        resendOTPLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Helper.INSTANCE.isNetworkAvailable(OTPScreen.this)){
                    resendOTP();
                } else {
                    Helper.INSTANCE.Error(OTPScreen.this, getString(R.string.NOCONN));
                }
            }
        });
    }

    private void Firebase_token_init(final String type) {

        if (token != null && !token.equalsIgnoreCase("")) {
            if (Helper.INSTANCE.isNetworkAvailable(OTPScreen.this)){
               otpverify_apicall();
            } else {
                Helper.INSTANCE.Error(OTPScreen.this, getString(R.string.NOCONN));
            }

        } else {
            //Generating new token

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                token = task.getResult().getToken();
                                Firebase_token_init(type);
                            } else {
                                token = "";
                            }
                        }
                    });
        }
    }

    public void otpverify_apicall() {

        String url = baseurl + "user_otp_verify";
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(OTPScreen.this);

        RequestQueue requestQueue = Volley.newRequestQueue(OTPScreen.this);

         Map<String,String> params = new HashMap();
        params.put("name", name);
        params.put("mobile",number);
        params.put("otp",otp);
        params.put("device_id",token);
        params.put("state", "");
        mainProgressBar.setVisibility(View.VISIBLE);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    mainProgressBar.setVisibility(View.GONE);
                    String r_code = obj.getString("status");
                    Toast.makeText(OTPScreen.this, ""+obj.getString("message"), Toast.LENGTH_SHORT).show();

                    if (r_code.equalsIgnoreCase("1")) {

                        String name=obj.getString("name");
                        String id=obj.getString("id");
                        String email=obj.getString("email");
                        String mobile=obj.getString("mobile");
                        String birth_date=obj.getString("birth_date");
                        String aadhaar_number=obj.getString("aadhaar_number");
                        String aadhaar_photo=obj.getString("aadhaar_photo");
                        String profile_photo=obj.getString("profile_photo");

                        YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
                        yourPrefrence.saveData("name",name);
                        yourPrefrence.saveData("id",id);
                        yourPrefrence.saveData("email",email);
                        yourPrefrence.saveData("mobile",mobile);
                        yourPrefrence.saveData("dob",birth_date);
                        yourPrefrence.saveData("aadhaar_number",aadhaar_number);
                        yourPrefrence.saveData("profile",profile_photo);
                        yourPrefrence.saveData("adharphoto",aadhaar_photo);
                        yourPrefrence.saveData("logintype",type);


                        if (type.equalsIgnoreCase("login")) {
                            Intent intent = new Intent(OTPScreen.this, MainActivity.class);
                            intent.putExtra("goto","home");
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(OTPScreen.this, IntroSliderActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    } else {
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.otpverify_Layout:
                otp = ed1.getText().toString()+ed2.getText().toString()+ed3.getText().toString()+ed4.getText().toString();
                if (otp.length()!=4) {
                    Toast.makeText(OTPScreen.this, "Required 4 Digit OTP", Toast.LENGTH_SHORT).show();
                } else {
                    Firebase_token_init(type);
                }
        }
    }

    public void resendOTP() {
        String url = baseurl + "user_resend_otp";
        RequestQueue requestQueue = Volley.newRequestQueue(OTPScreen.this);
        Map<String, String> params = new HashMap();
        params.put("mobile", number);
        JSONObject parameters = new JSONObject(params);
        resendOTPLayout.setVisibility(View.GONE);
        txt_timeCounter.setVisibility(View.VISIBLE);
        mainProgressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");
                    mainProgressBar.setVisibility(View.GONE);
                    if (r_code.equalsIgnoreCase("1")) {
                        Toast.makeText(OTPScreen.this, ""+obj.getString("message"), Toast.LENGTH_SHORT).show();
                        new CountDownTimer(60000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                txt_timeCounter.setText(millisUntilFinished / 1000 + " Sec");
                            }
                            public void onFinish() {
                                txt_timeCounter.setVisibility(View.GONE);
                                resendOTPLayout.setVisibility(View.VISIBLE);
                            }
                        }.start();
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
    }
}