package com.eminence.sitasrm.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.eminence.sitasrm.Activity.Profile.SendQuery;
import com.eminence.sitasrm.Adapters.ProductAdapter;
import com.eminence.sitasrm.Interface.AddToCart;
import com.eminence.sitasrm.Interface.BadgingInterface;
import com.eminence.sitasrm.Interface.RemoveProduct;
import com.eminence.sitasrm.Interface.UpdateProduct;
import com.eminence.sitasrm.MainActivity;
import com.eminence.sitasrm.Models.CartModel;
import com.eminence.sitasrm.Models.CartResponse;
import com.eminence.sitasrm.Models.ProductModel;
import com.eminence.sitasrm.R;
import com.eminence.sitasrm.Utils.DatabaseHandler;
import com.eminence.sitasrm.Utils.Helper;
import com.eminence.sitasrm.Utils.YourPreference;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.eminence.sitasrm.MainActivity.badgecountmain;
import static com.eminence.sitasrm.Utils.Baseurl.baseurl;

public class AllProduct extends AppCompatActivity implements BadgingInterface {

    ArrayList<ProductModel> offerlist = new ArrayList<>();
    RecyclerView product_recycle;
    LinearLayout cart;
    TextView lead_badge;
    DatabaseHandler databaseHandler;
    ArrayList<CartModel> cartList = new ArrayList<>();
    List<CartResponse> alldata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_product);
        product_recycle = findViewById(R.id.product_recycle);
        cart = findViewById(R.id.cart);
        lead_badge = findViewById(R.id.lead_badge);

        setUpDB();

        if(databaseHandler.cartInterface().getallcartdata().size() == 0){
            lead_badge.setVisibility(View.GONE);
          }


        lead_badge.setText(databaseHandler.cartInterface().getallcartdata().size()+"");

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addtocart("cart");
            }
        });

        if (Helper.INSTANCE.isNetworkAvailable(AllProduct.this)){
            addtocart("");
        } else {
            Helper.INSTANCE.Error(AllProduct.this, getString(R.string.NOCONN));
        }
    }

    public void back(View view) {
        onBackPressed();
    }

    public void cart(View view) {

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
                        offerlist.clear();
                        JSONArray jsonArray = obj.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            ProductModel productModel = new ProductModel();
                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                            String product_id = jsonObject2.getString("product_id");
                            String category_id = jsonObject2.getString("category_id");
                            String product_name = jsonObject2.getString("product_name");
                            String caption_eng = jsonObject2.getString("caption_eng");
                            String caption_hindi = jsonObject2.getString("caption_hindi");
                            String price = jsonObject2.getString("price");
                            String pouch_quantity = jsonObject2.getString("pouch_quantity");
                            String product_image = jsonObject2.getString("product_image");
                            String created_at = jsonObject2.getString("created_at");
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
                            productModel.setCaption_eng(caption_eng);
                            productModel.setCaption_hindi(caption_hindi);
                            productModel.setCart_availability(cart_availability);
                            productModel.setQuantity(quantity);
                            productModel.setSingle_description_english(single_description_english);
                            productModel.setSingle_description_hindi(single_description_hindi);

                            offerlist.add(productModel);

                        }

                        bindadapter(offerlist);

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
                //Toast.makeText(MainActivity.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }) {
        };

        requestQueue.add(stringRequest);
        stringRequest.setShouldCache(false);
    }

    private void bindadapter(ArrayList<ProductModel> offerlist) {
        ProductAdapter offerlistAdapter = new ProductAdapter(offerlist, getApplicationContext(), this,"home");
        product_recycle.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        product_recycle.setAdapter(offerlistAdapter);
        offerlistAdapter.notifyDataSetChanged();

    }

    @Override
    public void badgecount() {
        badgecountmain(databaseHandler.cartInterface().getallcartdata().size());
        if (databaseHandler.cartInterface().getallcartdata().size() == 0) {
            lead_badge.setVisibility(View.GONE);
        } else {
            lead_badge.setVisibility(View.VISIBLE);
        }

        lead_badge.setText("" + databaseHandler.cartInterface().getallcartdata().size()+"");

    }

    private void setUpDB() {
        databaseHandler = Room.databaseBuilder(AllProduct.this, DatabaseHandler.class, "cart").allowMainThreadQueries().build();
    }

    private void addtocart(String cart) {
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
        RequestQueue requestQueue = Volley.newRequestQueue(AllProduct.this);
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

                        if (cart.equalsIgnoreCase("cart")) {

                            Intent intent = new Intent(AllProduct.this, MainActivity.class);
                            intent.putExtra("goto", "cart");
                            startActivity(intent);

                        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setUpDB();
        addtocart("");
    }

    @Override
    protected void onStop() {
        setUpDB();
        addtocart("");
        super.onStop();
    }
}