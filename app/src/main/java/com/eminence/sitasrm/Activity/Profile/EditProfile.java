package com.eminence.sitasrm.Activity.Profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.eminence.sitasrm.Activity.Help;
import com.eminence.sitasrm.MainActivity;
import com.eminence.sitasrm.R;
import com.eminence.sitasrm.Utils.Helper;
import com.eminence.sitasrm.Utils.YourPreference;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.eminence.sitasrm.Utils.Baseurl.baseurl;

public class EditProfile extends AppCompatActivity {
    LinearLayout updateprofileLayout;
    EditText txt_name,txt_email,txt_dob;
    TextView txt_mobile;
    Calendar myCalendar;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String mobile = intent.getStringExtra("mobile");
        String email = intent.getStringExtra("email");
        String dob = intent.getStringExtra("dob");

        txt_name = findViewById(R.id.txt_name);
        txt_mobile = findViewById(R.id.txt_mobile);
        txt_email = findViewById(R.id.txt_email);
        txt_dob = findViewById(R.id.txt_dob);
        myCalendar = Calendar.getInstance();

        updateprofileLayout = findViewById(R.id.updateprofileLayout);

        if(name.equalsIgnoreCase("") || name.equalsIgnoreCase("null")){
        } else {
            txt_name.setText(name);
        }

        if(mobile.equalsIgnoreCase("") || mobile.equalsIgnoreCase("null")){
        } else {
            txt_mobile.setText(mobile);
        }

        if(email.equalsIgnoreCase("") || email.equalsIgnoreCase("null")){
        } else {
            txt_email.setText(email);
        }

        if(dob.equalsIgnoreCase("") || dob.equalsIgnoreCase("null")){
        } else {
            txt_dob.setText(dob);
        }

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        txt_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(EditProfile.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        updateprofileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = txt_name.getText().toString();
                String mobile = txt_mobile.getText().toString();
                String email = txt_email.getText().toString();
                String dob = txt_dob.getText().toString();
                YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
                String language = yourPrefrence.getData("language");

                if(name.equalsIgnoreCase("") || name.equalsIgnoreCase("null")){
                    if (language.equalsIgnoreCase("en")||language.equalsIgnoreCase("")) {
                        txt_name.setError("Field is required");
                    } else {
                        txt_name.setError("फील्ड अनिवार्य है");
                    }
                } else if(email.equalsIgnoreCase("") || email.equalsIgnoreCase("null")){
                    if (language.equalsIgnoreCase("en")||language.equalsIgnoreCase("")) {
                        txt_email.setError("Field is required");
                    } else {
                        txt_email.setError("फील्ड अनिवार्य है");
                    }
                } else if(!email.matches(emailPattern)){
                    if (language.equalsIgnoreCase("en")||language.equalsIgnoreCase("")) {
                        txt_email.setError("Please Enter Valid Email");
                    } else {
                        txt_email.setError("कृपया वैलिड ईमेल दर्ज़ करें");
                    }
                } else if(dob.equalsIgnoreCase("") || dob.equalsIgnoreCase("null")){
                    if (language.equalsIgnoreCase("en")||language.equalsIgnoreCase("")) {
                        txt_dob.setError("Field is required");
                    } else {
                        txt_dob.setError("फील्ड अनिवार्य है");
                    }
                } else {
                    if (Helper.INSTANCE.isNetworkAvailable(EditProfile.this)){
                        updateProfileDetails(name,mobile,email,dob);
                    } else {
                        Helper.INSTANCE.Error(EditProfile.this, getString(R.string.NOCONN));
                    }
                }
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        txt_dob.setText(sdf.format(myCalendar.getTime()));
    }

    private void updateProfileDetails(String name, String mobile, String email, String dob) {
        String url = baseurl + "user_profile_update";
        YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
        String id = yourPrefrence.getData("id");
        RequestQueue requestQueue = Volley.newRequestQueue(EditProfile.this);
        Map<String, String> params = new HashMap();
        params.put("user_id", id);
        params.put("name", name);
        params.put("email", email);
        params.put("mobile", mobile);
        params.put("birth_date", dob);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");
                    if (r_code.equalsIgnoreCase("1")) {
                        YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
                        yourPrefrence.saveData("name", name);
                        yourPrefrence.saveData("email", email);
                        yourPrefrence.saveData("mobile", mobile);
                        yourPrefrence.saveData("dob", dob);

                        Intent intent = new Intent(EditProfile.this, MainActivity.class);
                        intent.putExtra("goto","home");
                        startActivity(intent);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

        };
        requestQueue.add(stringRequest);
        stringRequest.setShouldCache(false);

    }

    public void help(View view) {
        startActivity(new Intent(getApplicationContext(), Help.class));
    }

    public void back(View view) {
        onBackPressed();
    }

}