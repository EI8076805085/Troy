package com.eminence.sitasrm.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.eminence.sitasrm.Activity.Profile.AddAdress;
import com.eminence.sitasrm.Activity.Profile.Address;
import com.eminence.sitasrm.Adapters.AddressAdapter;
import com.eminence.sitasrm.Adapters.ProductAdapter;
import com.eminence.sitasrm.Interface.AddToCart;
import com.eminence.sitasrm.Interface.BadgingInterface;
import com.eminence.sitasrm.Interface.CartInterface;
import com.eminence.sitasrm.Interface.RemoveProduct;
import com.eminence.sitasrm.Interface.UpdateProduct;
import com.eminence.sitasrm.MainActivity;
import com.eminence.sitasrm.Models.AddressModel;
import com.eminence.sitasrm.Models.CartModel;
import com.eminence.sitasrm.Models.CartResponse;
import com.eminence.sitasrm.Models.ProductModel;
import com.eminence.sitasrm.R;
import com.eminence.sitasrm.Utils.DatabaseHandler;
import com.eminence.sitasrm.Utils.Helper;
import com.eminence.sitasrm.Utils.YourPreference;
import com.google.gson.Gson;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.eminence.sitasrm.Fragments.CartFragment.totalprice;
import static com.eminence.sitasrm.MainActivity.badgecountmain;
import static com.eminence.sitasrm.Utils.Baseurl.baseurl;
import static com.eminence.sitasrm.Utils.Baseurl.imagebaseurl;

public class CheckOutActivity extends AppCompatActivity implements PaymentResultListener, BadgingInterface {

    RecyclerView product_recycle;
    ArrayList<ProductModel> offerlist = new ArrayList<>();
    DatabaseHandler databaseHandler;
    LinearLayout calculation_layout, checkOutLayout, addressFoundLayout, addressNotFoundLayout;
    List<CartResponse> alldata;
    TextView address_name, address, state_pincode, mobile, txt_totalprice,
            dis_amt_txt, payble_amt_txt, net_amt_long_txt, payble_amt_main, txt_userWalletAmount,
            txt_walletDiductAmount, txt_rupeeSign;
    String default_address_id;
    String email,hfnum, addresss, landmark, state, pincode, mobile1, mobile2, defaults, created_at,name;
    int total_price = 0;
    boolean isaddressavailable = false;
    int  net_amt = 0;
    String discount_amount, payble_amount, total_amount;
    String cart_id="";
    CheckBox cb_userWallet;
    ArrayList<CartModel> cartList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        product_recycle = findViewById(R.id.cartrecycleview);
        calculation_layout = findViewById(R.id.calculation_layout);
        checkOutLayout = findViewById(R.id.checkOutLayout);
        addressFoundLayout = findViewById(R.id.addressFoundLayout);
        addressNotFoundLayout = findViewById(R.id.addressNotFoundLayout);
        address_name = findViewById(R.id.address_name);
        address = findViewById(R.id.address);
        mobile = findViewById(R.id.mobile);
        txt_totalprice = findViewById(R.id.totalprice);
        dis_amt_txt = findViewById(R.id.dis_amt_txt);
        payble_amt_main = findViewById(R.id.payble_amt_main);
        payble_amt_txt = findViewById(R.id.payble_amt_txt);
        state_pincode = findViewById(R.id.state_pincode);
        net_amt_long_txt = findViewById(R.id.net_amt_long_txt);
        txt_userWalletAmount = findViewById(R.id.txt_userWalletAmount);
        txt_walletDiductAmount = findViewById(R.id.txt_walletDiductAmount);
        cb_userWallet = findViewById(R.id.cb_userWallet);
        txt_rupeeSign = findViewById(R.id.txt_rupeeSign);

        setUpDB();

        Intent intent = getIntent();
        payble_amount = intent.getStringExtra("payble_amount");
        discount_amount = intent.getStringExtra("discount_amount");
        total_amount = intent.getStringExtra("total_amount");

        dis_amt_txt.setText("₹" + discount_amount);
        payble_amt_txt.setText("₹" + payble_amount);
        payble_amt_main.setText(payble_amount);
        txt_totalprice.setText("₹" + total_amount);

        YourPreference yourPrefrence = YourPreference.getInstance(CheckOutActivity.this);
        String language = yourPrefrence.getData("language");
        if (language.equalsIgnoreCase("en") || language.equalsIgnoreCase("")) {
            net_amt_long_txt.setText("You will save ₹" + discount_amount + " on this order");
        } else {
            net_amt_long_txt.setText("आप इस ऑर्डर पर ₹" + discount_amount + " की बचत करेंगे");
        }

        alldata = databaseHandler.cartInterface().getallcartdata();
        if (Helper.INSTANCE.isNetworkAvailable(CheckOutActivity.this)) {

            addtocart();
            CartList();
            getamount();
            getUserProfile();

        } else {
            Helper.INSTANCE.Error(CheckOutActivity.this, getString(R.string.NOCONN));
        }

        checkOutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isaddressavailable) {
                    if(email.equalsIgnoreCase("") || email.equalsIgnoreCase("null")){
                        Toast.makeText(CheckOutActivity.this, "Please Update Your Profile", Toast.LENGTH_SHORT).show();
                    }else {

                        if (cart_id.equalsIgnoreCase(""))
                        {

                            Toast.makeText(CheckOutActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Log.i("cart_id",cart_id);
                            startPayment(payble_amt_main.getText().toString());
                        }

                    }

                } else {
                    Toast.makeText(CheckOutActivity.this, "Please Add Address", Toast.LENGTH_SHORT).show();
                }
            }
        });


        cb_userWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb_userWallet.isChecked()) {
                    YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
                    String walletAmount = txt_userWalletAmount.getText().toString();
                    String nextAmount = payble_amt_main.getText().toString();

                    if (Integer.parseInt(walletAmount) > Integer.parseInt(nextAmount)) {
                        int safaAmount = Integer.parseInt(walletAmount) - Integer.parseInt(nextAmount);
                        txt_walletDiductAmount.setText("" + nextAmount);
                        txt_rupeeSign.setVisibility(View.VISIBLE);
                        txt_userWalletAmount.setText("" + safaAmount);
                        payble_amt_main.setText("0");
                        yourPrefrence.saveData("wallet_diduct_amount", nextAmount);

                    } else {
                        int safaAmount = Integer.parseInt(nextAmount) - Integer.parseInt(walletAmount);
                        txt_walletDiductAmount.setText("" + walletAmount);
                        txt_rupeeSign.setVisibility(View.VISIBLE);
                        payble_amt_main.setText("" + safaAmount);
                        txt_userWalletAmount.setText("0");
                        yourPrefrence.saveData("wallet_diduct_amount", walletAmount);
                    }

                } else {
                    YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
                    String nextAmount = payble_amt_main.getText().toString();
                    String walletDiductAmount = yourPrefrence.getData("wallet_diduct_amount");
                    int safaAmount = Integer.parseInt(nextAmount) + Integer.parseInt(walletDiductAmount);
                    int updateWallet = Integer.parseInt(txt_userWalletAmount.getText().toString());
                    int newUpdateAmount = updateWallet + Integer.parseInt(walletDiductAmount);
                    txt_userWalletAmount.setText("" + newUpdateAmount);
                    txt_walletDiductAmount.setText("");
                    txt_rupeeSign.setVisibility(View.GONE);
                    payble_amt_main.setText("" + safaAmount);
                    yourPrefrence.saveData("wallet_diduct_amount", nextAmount);

                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        address();

    }

    public void getamount() {
        YourPreference yourPrefrence = YourPreference.getInstance(CheckOutActivity.this);
        String user_id = yourPrefrence.getData("id");
        String url = baseurl + "wallet_amount";
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(CheckOutActivity.this);

        Map<String, String> params = new HashMap();
        params.put("user_id", user_id);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");

                    if (r_code.equalsIgnoreCase("1")) {
                        String amount = obj.getString("balance");
                        txt_userWalletAmount.setText(amount);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckOutActivity.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }) {

        };
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);


    }

    public void Productlist() {
        offerlist.clear();
        String url = baseurl + "product_list";
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
        String id = yourPrefrence.getData("id");
        Map<String, String> params = new HashMap();
        params.put("user_id", id);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");
                    if (r_code.equalsIgnoreCase("1")) {
                        JSONArray jsonArray = obj.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            ProductModel productModel = new ProductModel();
                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                            String product_id = jsonObject2.getString("product_id");
                            String category_id = jsonObject2.getString("category_id");
                            String product_name = jsonObject2.getString("product_name");
                            String price = jsonObject2.getString("price");
                            String caption_eng = jsonObject2.getString("caption_eng");
                            String caption_hindi = jsonObject2.getString("caption_hindi");
                            String pouch_quantity = jsonObject2.getString("pouch_quantity");
                            String product_image = jsonObject2.getString("product_image");
                            String created_at = jsonObject2.getString("created_at");
                            String p_name_hindi = jsonObject2.getString("product_name_hindi");
                            String description_hindi = jsonObject2.getString("description_hindi");
                            String cart_availability = jsonObject2.getString("cart_availability");
                            String quantity = jsonObject2.getString("quantity");
                            String single_description_english = jsonObject2.getString("single_description_english");
                            String single_description_hindi = jsonObject2.getString("single_description_hindi");

                            productModel.setCategory_id(category_id);
                            productModel.setProduct_id(product_id);
                            productModel.setProduct_name(product_name);
                            productModel.setPrice(price);
                            productModel.setProduct_image(product_image);
                            productModel.setPouch_quantity(pouch_quantity);
                            productModel.setCreated_at(created_at);
                            productModel.setP_name_hindi(p_name_hindi);
                            productModel.setDescription_hindi(description_hindi);
                            productModel.setCaption_eng(caption_eng);
                            productModel.setCaption_hindi(caption_hindi);
                            productModel.setCart_availability(cart_availability);
                            productModel.setQuantity(quantity);
                            productModel.setSingle_description_english(single_description_english);
                            productModel.setSingle_description_hindi(single_description_hindi);

                            if (cart_availability.equalsIgnoreCase("1")) {
                                offerlist.add(productModel);
                                //total_price = total_price + Integer.parseInt(price);
                            }
                        }

//                        int total_dis = total_price * 7 / 100;
//
//                        net_amt = total_price - total_dis;

                        bindadapter();

                    } else if (r_code.equalsIgnoreCase("100")) {
                        Toast.makeText(getApplicationContext(), "Your Account has been Terminated!!", Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences preferences = getApplicationContext().getSharedPreferences("YourCustomNamedPreference", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.apply();
                                finishAffinity();
                            }
                        }, 1500);
                    } else {
                        //offerlayoput.setVisibility(View.GONE);
                        product_recycle.setVisibility(View.GONE);
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

    private void bindadapter() {
        ProductAdapter offerlistAdapter = new ProductAdapter(offerlist, getApplicationContext(), this,"cart");
        product_recycle.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        product_recycle.setAdapter(offerlistAdapter);
        offerlistAdapter.notifyDataSetChanged();
    }

    public void address() {
        String url = baseurl + "user_address_list";
        RequestQueue requestQueue = Volley.newRequestQueue(CheckOutActivity.this);
        YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
        String id = yourPrefrence.getData("id");
        Map<String, String> params = new HashMap();
        params.put("user_id", id);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");
                    if (r_code.equalsIgnoreCase("1")) {
                        addressFoundLayout.setVisibility(View.VISIBLE);
                        addressNotFoundLayout.setVisibility(View.GONE);
                        JSONArray jsonArray = obj.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            AddressModel addressModel = new AddressModel();
                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                            String id = jsonObject2.getString("id");
                            String type = jsonObject2.getString("type");

                            hfnum = jsonObject2.getString("hf_number");
                            addresss = jsonObject2.getString("address");
                            landmark = jsonObject2.getString("landmark");
                            state = jsonObject2.getString("state");
                            pincode = jsonObject2.getString("pincode");
                            created_at = jsonObject2.getString("created_at");
                            name = jsonObject2.getString("name");
                            mobile1 = jsonObject2.getString("mobile1");
                            mobile2 = jsonObject2.getString("mobile2");
                            defaults = jsonObject2.getString("defaults");

                            if (defaults.equalsIgnoreCase("1")) {

                                isaddressavailable = true;
                                address_name.setText(name);
                                address.setText(hfnum + " " + addresss + " near " + landmark);
                                state_pincode.setText(state + " " + pincode);
                                mobile.setText(mobile1 + "," + mobile2);
                                default_address_id = id;
                                if (type.equalsIgnoreCase("Home")) {
                                } else if (type.equalsIgnoreCase("Work")) {
                                    address_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_work_with_background, 0);

                                } else {
                                    address_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_other_with_background, 0);
                                }
                            }
                        }
                    } else {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(CheckOutActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckOutActivity.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }) {
        };
        requestQueue.add(stringRequest);
    }

    public void startPayment(String amount) {

        if (cb_userWallet.isChecked() && payble_amt_main.getText().toString().equalsIgnoreCase("0")) {


            make_order_request("");

        } else {

            final Activity activity = this;
            final Checkout co = new Checkout();
            try {
                YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
                String email = yourPrefrence.getData("email");
                String mobile = yourPrefrence.getData("mobile");
                JSONObject options = new JSONObject();
                options.put("name", "Razorpay Corp");
                options.put("description", "Wallet recharge");
                //You can omit the image option to fetch the image from dashboard
                options.put("image", "https://rzp-mobile.s3.amazonaws.com/images/rzp.png");
                options.put("currency", "INR");
                String payment = amount;
                double total = Double.parseDouble(payment);
                total = total * 100;
                options.put("amount", total);
                JSONObject preFill = new JSONObject();
                preFill.put("email", email);
                preFill.put("contact", mobile);
                options.put("prefill", preFill);
                co.open(activity, options);

            } catch (Exception e) {
                Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    }

    private void orderPlaced() {
        Toast.makeText(CheckOutActivity.this, "Order Id not Found", Toast.LENGTH_SHORT).show();
    }

    private void setUpDB() {
        databaseHandler = Room.databaseBuilder(CheckOutActivity.this, DatabaseHandler.class, "cart").allowMainThreadQueries().build();
    }

    public void back(View view) {
        onBackPressed();
    }

    @Override
    public void onPaymentSuccess(String s) {

            make_order_request(s);



    }

    @Override
    public void onPaymentError(int i, String s) {
    }

    private void make_order_request(String Transaction_id) {
        JSONArray jsonArray = null;
        String json = String.valueOf(new Gson().toJsonTree(alldata));
        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
        String user_id = yourPrefrence.getData("id");
         String url = baseurl + "place_order";
        RequestQueue requestQueue = Volley.newRequestQueue(CheckOutActivity.this);

        String walletAmount = "0";

        if (txt_walletDiductAmount.getText().toString().equalsIgnoreCase("") || txt_walletDiductAmount.getText().toString().equalsIgnoreCase("null")) {
            walletAmount = "0";
        } else {
            walletAmount = txt_walletDiductAmount.getText().toString();
        }

        JSONObject params = new JSONObject();

        try {
            params.put("user_id", user_id);
            params.put("cart_id", cart_id);
            params.put("address_id", default_address_id);
            params.put("transaction_id", Transaction_id);
            params.put("total_amount", total_amount);
            params.put("wallet_amount", walletAmount);
            params.put("discount_amount", "0");
            params.put("net_amount", payble_amount);
            params.put("products", jsonArray);
            params.put("cashback", discount_amount);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("paramenter", String.valueOf(params));
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    Toast.makeText(CheckOutActivity.this, "" + obj.getString("message"), Toast.LENGTH_SHORT).show();
                    String r_code = obj.getString("status");

                    if (r_code.equalsIgnoreCase("1")) {
                        databaseHandler.cartInterface().deleteall();
                        totalprice = 0;

                      // make_order_request_shiprocket(user_id,payble_amount,discount_amount);

                        Intent intent = new Intent(CheckOutActivity.this, PaymentSuccessfullActivity.class);
                        intent.putExtra("dis_amount", "" + discount_amount);
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

        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    public void changeaddress(View view) {
        Intent intent = new Intent(CheckOutActivity.this, Address.class);
        startActivity(intent);

    }

    @Override
    public void badgecount() {
        badgecountmain(databaseHandler.cartInterface().getallcartdata().size());
    }

    public void getUserProfile() {
        String url = baseurl + "user_profile";
        RequestQueue requestQueue = Volley.newRequestQueue(CheckOutActivity.this);
        YourPreference yourPrefrence = YourPreference.getInstance(CheckOutActivity.this);
        String id = yourPrefrence.getData("id");
        Map<String, String> params = new HashMap();
        params.put("user_id", id);

        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");
                    if (r_code.equalsIgnoreCase("1")) {
                        JSONArray jsonArray = obj.getJSONArray("data");
                        for(int i = 0; i < jsonArray.length(); i++){
                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                            String name = jsonObject2.getString("name");
                            String mobile = jsonObject2.getString("mobile");
                            email = jsonObject2.getString("email");
                            String dob = jsonObject2.getString("birth_date");
                            String profilephoto = jsonObject2.getString("profile_photo");

                            if(email.equalsIgnoreCase("") || email.equalsIgnoreCase("null")){
                                Toast.makeText(CheckOutActivity.this, "Please Update your profile", Toast.LENGTH_SHORT).show();
                            }
                        }
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

    private void make_order_request_shiprocket(String user_id, String payble_amount, String discount_amount) {
        String url = "https://apiv2.shiprocket.in/v1/external/orders/create/adhoc";

        RequestQueue requestQueue = Volley.newRequestQueue(CheckOutActivity.this);
        JSONObject params = new JSONObject();
        try {
        params.put("order_id", "072");
        params.put("order_date", created_at);
        params.put("pickup_location", addresss);
        params.put("channel_id", "");
        params.put("comment", "This order is teststing purpose");
        params.put("billing_customer_name", name);
        params.put("billing_last_name", "");
        params.put("billing_address", addresss);
        params.put("billing_address_2", "");
        params.put("billing_city", state);
        params.put("billing_pincode", pincode);
        params.put("billing_state", state);
        params.put("billing_country", "India");
        params.put("billing_email", email);
        params.put("billing_phone", mobile1);
        params.put("shipping_is_billing", true);
        params.put("shipping_customer_name", name);
        params.put("shipping_last_name", "");
        params.put("shipping_address", addresss);
        params.put("shipping_address_2", "");
        params.put("shipping_city", state);
        params.put("shipping_pincode", pincode);
        params.put("shipping_country", "India");
        params.put("shipping_state", state);
        params.put("shipping_email", email);
        params.put("shipping_phone", mobile1);
        params.put("payment_method", "Prepaid");
        params.put("shipping_charges", "");
        params.put("giftwrap_charges", "");
        params.put("transaction_charges", "");
        params.put("total_discount", discount_amount);
        params.put("sub_total", payble_amount);
        params.put("length", "2");
        params.put("breadth", "2");
        params.put("height", "2");
        params.put("weight", "2");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("parameter shiprocket",String.valueOf(params));

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");
                    if (r_code.equalsIgnoreCase("1")) {
                        Toast.makeText(CheckOutActivity.this, "Order is placed succefully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CheckOutActivity.this, PaymentSuccessfullActivity.class);
                        intent.putExtra("dis_amount", "" + discount_amount);
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
                Toast.makeText(CheckOutActivity.this, ""+error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Host", "<calculated when request is sent>");
                params.put("Authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOjE4NDg3MTksImlzcyI6Imh0dHBzOi8vYXBpdjIuc2hpcHJvY2tldC5pbi92MS9leHRlcm5hbC9hdXRoL2xvZ2luIiwiaWF0IjoxNjMyOTEwNDQ4LCJleHAiOjE2MzM3NzQ0NDgsIm5iZiI6MTYzMjkxMDQ0OCwianRpIjoiT2ttVERMaWNmem1zTDR2ayJ9.NbQXq6fs9znWfdPzXY4GnUHGlMTcPTBIqxrhaBtc1hs");
                return params;
            }
        };

        requestQueue.add(stringRequest);

    }

    private void addtocart() {
        alldata = databaseHandler.cartInterface().getallcartdata();
        Log.i("AllDATA",""+alldata);
        JSONArray jsonArray = null;
        String json = String.valueOf(new Gson().toJsonTree(alldata));
        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
        String user_id = yourPrefrence.getData("id");
        String url = baseurl + "add_to_cart";
        RequestQueue requestQueue = Volley.newRequestQueue(CheckOutActivity.this);


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
                    Productlist();

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
                //      Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                Log.d("ABC", String.valueOf(error));
            }
        }) {

        };
        requestQueue.add(stringRequest);
        stringRequest.setShouldCache(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        addtocart();
    }



    private void CartList() {
        databaseHandler.cartInterface().deleteall();
        cartList.clear();
        String url = baseurl + "cart_list";
        RequestQueue requestQueue = Volley.newRequestQueue(CheckOutActivity.this);
        YourPreference yourPrefrence = YourPreference.getInstance(CheckOutActivity.this);
        String id = yourPrefrence.getData("id");
        Map<String, String> params = new HashMap();
        params.put("user_id", id);

        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");

                    Log.i("cart_list_response",String.valueOf(response));
                    if (r_code.equalsIgnoreCase("1")) {
                          cart_id = obj.getString("cart_id");

                        cartList.clear();
                        JSONArray jsonArray = obj.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            CartModel productModel = new CartModel();
                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                            String product_id = jsonObject2.getString("product_id");
                            String product_price = jsonObject2.getString("price");
                            String quantity = jsonObject2.getString("quantity");
                            productModel.setProduct_id(product_id);
                            cartList.add(productModel);

                            CartResponse cartResponse = new CartResponse(product_id, quantity, product_price);
                            databaseHandler.cartInterface().addcart(cartResponse);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("App",""+error);
            }
        }) {

        };

        requestQueue.add(stringRequest);
        stringRequest.setShouldCache(false);

    }

}