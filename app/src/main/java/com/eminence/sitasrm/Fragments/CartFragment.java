package com.eminence.sitasrm.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.eminence.sitasrm.Activity.CheckOutActivity;
import com.eminence.sitasrm.Activity.Profile.Address;
import com.eminence.sitasrm.Activity.SignUpActivity;
import com.eminence.sitasrm.Adapters.ProductAdapter;
import com.eminence.sitasrm.Interface.AddToCart;
import com.eminence.sitasrm.Interface.BadgingInterface;
import com.eminence.sitasrm.Interface.Calculationinterface;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.eminence.sitasrm.MainActivity.badgecountmain;
import static com.eminence.sitasrm.Utils.Baseurl.baseurl;

public class CartFragment extends Fragment implements BadgingInterface{

    RecyclerView product_recycle;
    ArrayList<ProductModel> offerlist = new ArrayList<>();
    static DatabaseHandler databaseHandler;
    LinearLayout checkOutLayout;
    static LinearLayout bottomLayout,cartEmptyLayout;
    static TextView idtotal_amount,txt_defaultAddress;
    static TextView dis_amt,txt_discount;
    static TextView payble_amt;
    public static int disamt;
    public static int payble_amount;
    public static int totalprice=0;
    static int discount=0 ;
    LinearLayout addressLayout;
    static String tvDiscount;
    static ImageView img_imptyCart;
    static YourPreference yourPrefrence;
    public static String subscriberCashback="0",cashback="0",subStatus="0";
    static List<CartResponse> cartResponseslist;
    ArrayList<CartModel> cartList = new ArrayList<>();
    List<CartResponse> alldata;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        product_recycle = view.findViewById(R.id.cartrecycleview);
        checkOutLayout = view.findViewById(R.id.checkOutLayout);
        bottomLayout = view.findViewById(R.id.bottomLayout);
        idtotal_amount = view.findViewById(R.id.idtotal_amount);
        dis_amt = view.findViewById(R.id.dis_amt);
        payble_amt = view.findViewById(R.id.payble_amt);
        txt_defaultAddress = view.findViewById(R.id.txt_defaultAddress);
        addressLayout = view.findViewById(R.id.addressLayout);
        txt_discount = view.findViewById(R.id.txt_discount);
        cartEmptyLayout = view.findViewById(R.id.cartEmptyLayout);
        img_imptyCart = view.findViewById(R.id.img_imptyCart);
        yourPrefrence = YourPreference.getInstance(getActivity());

        if (Helper.INSTANCE.isNetworkAvailable(getActivity())) {
            getUtility();
            setUpDB();
            addtocart();
        } else {
            Helper.INSTANCE.Error(getActivity(), getString(R.string.NOCONN));
        }

        checkOutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CheckOutActivity.class);
                intent.putExtra("total_amount",totalprice+"");
                intent.putExtra("discount_amount",disamt+"");
                intent.putExtra("payble_amount",payble_amount+"");
                startActivity(intent);

            }});

        addressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Address.class);
                startActivity(intent);
            }
        });


        img_imptyCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                   getActivity().onBackPressed();

//                Intent intent = new Intent(getActivity(), MainActivity.class);
//                intent.putExtra("goto","home");
//                startActivity(intent);

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        address();
    }

    public void Productlist() {
        offerlist.clear();
        String url = baseurl + "product_list";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(getActivity());
        YourPreference yourPrefrence = YourPreference.getInstance(getActivity());
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
                            productModel.setQuantity(quantity);
                            productModel.setCart_availability(cart_availability);
                            productModel.setSingle_description_english(single_description_english);
                            productModel.setSingle_description_hindi(single_description_hindi);

                            //offerlist.add(productModel);

                            if (cart_availability.equalsIgnoreCase("1")) {
                               offerlist.add(productModel);
                            }
                        }

                        if (offerlist.size() == 0) {
                            bottomLayout.setVisibility(View.GONE);
                            cartEmptyLayout.setVisibility(View.VISIBLE);
                        } else {
                            bottomLayout.setVisibility(View.VISIBLE);
                            cartEmptyLayout.setVisibility(View.GONE);
                        }

                        bindadapter(offerlist);

                    } else if (r_code.equalsIgnoreCase("100")) {
                        Toast.makeText(getActivity(), "Your Account has been Terminated!!", Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences preferences = getActivity().getSharedPreferences("YourCustomNamedPreference", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.apply();
                                getActivity().finishAffinity();

                            }
                        }, 1500);


                    } else {

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

    @Override
    public void badgecount() {
        badgecountmain(databaseHandler.cartInterface().getallcartdata().size());

        if(databaseHandler.cartInterface().getallcartdata().size()==0){
            bottomLayout.setVisibility(View.GONE);
            cartEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            bottomLayout.setVisibility(View.VISIBLE);
            cartEmptyLayout.setVisibility(View.GONE);
        }
    }

    private void bindadapter(ArrayList<ProductModel> offerlist) {
            ProductAdapter offerlistAdapter = new ProductAdapter(offerlist, getActivity(), this,"cartFragment");
            product_recycle.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            product_recycle.setAdapter(offerlistAdapter);
            offerlistAdapter.notifyDataSetChanged();
    }

    private void setUpDB() {
        databaseHandler = Room.databaseBuilder(getActivity(), DatabaseHandler.class, "cart").allowMainThreadQueries().build();
    }

    public static void getamount_from_adapter() {
        totalprice=0;
        cartResponseslist = databaseHandler.cartInterface().getallcartdata();
        for(int i=0;i<cartResponseslist.size();i++) {
          int  princeinto= Integer.parseInt(cartResponseslist.get(i).getPrice())*Integer.parseInt(cartResponseslist.get(i).getQty());
          totalprice= totalprice+princeinto;

        }

         if(subStatus.equalsIgnoreCase("1")) {
             if(totalprice>5000){
                 discount = Integer.parseInt(subscriberCashback);
                 String language = yourPrefrence.getData("language");
                 if (language.equalsIgnoreCase("hi")) {
                     tvDiscount = subscriberCashback+"% छूट";
                 } else {
                     tvDiscount = subscriberCashback+"% Discount";
                 }
                 txt_discount.setText(tvDiscount);
             } else {
                 discount = Integer.parseInt(cashback);
                 String language = yourPrefrence.getData("language");
                 if (language.equalsIgnoreCase("hi")) {
                     tvDiscount = cashback+"% छूट";
                 } else {
                     tvDiscount = cashback+"% Discount";
                 }
                 txt_discount.setText(tvDiscount);
             }
         } else {
             discount = Integer.parseInt(cashback);
             String language = yourPrefrence.getData("language");
             if (language.equalsIgnoreCase("hi")) {
                 tvDiscount = cashback+"% छूट";
             } else {
                 tvDiscount = cashback+"% Discount";
             }
             txt_discount.setText(tvDiscount);
         }
            idtotal_amount.setText(""+totalprice);
            disamt=totalprice*discount/100;
            dis_amt.setText(""+disamt);
            payble_amount=totalprice-disamt;
            payble_amt.setText(""+payble_amount);

        if (totalprice==0) {
            bottomLayout.setVisibility(View.GONE);
            cartEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            bottomLayout.setVisibility(View.VISIBLE);
            cartEmptyLayout.setVisibility(View.GONE);
        }
     }

    public void address() {
        String url = baseurl + "user_address_list";
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        YourPreference yourPrefrence = YourPreference.getInstance(getActivity());
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
                            AddressModel addressModel = new AddressModel();
                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                            String state = jsonObject2.getString("state");
                            String pincode = jsonObject2.getString("pincode");
                            String defaults = jsonObject2.getString("defaults");

                            if(defaults.equalsIgnoreCase("1")) {
                                addressModel.setState(state);
                                addressModel.setPincode(pincode);
                                // txt_defaultAddress.setText("Delivered to " +state + " " +pincode );
                                txt_defaultAddress.setText(state + " " +pincode );
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
                Toast.makeText(getActivity(), "" + error, Toast.LENGTH_SHORT).show();
            }
        }) {

        };

        requestQueue.add(stringRequest);
    }

    public void check_subscribe_status() {
        String url = baseurl + "user_subscribe_check";
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        YourPreference yourPrefrence = YourPreference.getInstance(getActivity());
        String id = yourPrefrence.getData("id");

        Map<String, String> params = new HashMap();
        params.put("user_id", id);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String status_code = obj.getString("status");
                    if (status_code.equalsIgnoreCase("1")) {
                        subStatus = obj.getString("status");
                        getamount_from_adapter();
                    } else {
                        subStatus = "0";
                        getamount_from_adapter();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "" + error, Toast.LENGTH_SHORT).show();
            }
        }) {

        };

        requestQueue.add(stringRequest);
        stringRequest.setShouldCache(false);

    }

    public void getUtility() {
        String url = baseurl + "utility";
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");
                     if (r_code.equalsIgnoreCase("1")) {
                         check_subscribe_status();
                         subscriberCashback = obj.getString("subscriber_cashback");
                         cashback = obj.getString("cashback");
                     }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "" + error, Toast.LENGTH_SHORT).show();
            }
        }) {

        };

        requestQueue.add(stringRequest);
        stringRequest.setShouldCache(false);
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
    public void onStop() {
        setUpDB();
        addtocart();
        super.onStop();
    }
}