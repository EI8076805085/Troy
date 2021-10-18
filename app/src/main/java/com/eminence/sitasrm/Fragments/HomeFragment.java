package com.eminence.sitasrm.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.eminence.sitasrm.Activity.AllProduct;
import com.eminence.sitasrm.Activity.Help;
import com.eminence.sitasrm.Activity.Notification;
import com.eminence.sitasrm.Activity.OTPScreen;
import com.eminence.sitasrm.Activity.OrderDetails;
import com.eminence.sitasrm.Activity.PaymentSuccessfullActivity;
import com.eminence.sitasrm.Activity.Profile.Feedback;
import com.eminence.sitasrm.Activity.SignUpActivity;
import com.eminence.sitasrm.Activity.Profile.Address;
import com.eminence.sitasrm.Activity.SubscriptionPlanActivity;
import com.eminence.sitasrm.Activity.SuccesfullSubscribe;
import com.eminence.sitasrm.Adapters.AddressAdapter;
import com.eminence.sitasrm.Adapters.FeedbackSliderAdapter;
import com.eminence.sitasrm.Adapters.ImageSlideAdapter;
import com.eminence.sitasrm.Adapters.ProductAdapter;
import com.eminence.sitasrm.Interface.AddToCart;
import com.eminence.sitasrm.Interface.RemoveProduct;
import com.eminence.sitasrm.Interface.UpdateProduct;
import com.eminence.sitasrm.MainActivity;
import com.eminence.sitasrm.Interface.BadgingInterface;
import com.eminence.sitasrm.Models.AddressModel;
import com.eminence.sitasrm.Models.CartModel;
import com.eminence.sitasrm.Models.CartResponse;
import com.eminence.sitasrm.Models.Feedbackmodel;
import com.eminence.sitasrm.Models.Images;
import com.eminence.sitasrm.Models.ProductModel;
import com.eminence.sitasrm.R;
import com.eminence.sitasrm.Utils.DatabaseHandler;
import com.eminence.sitasrm.Utils.Helper;
import com.eminence.sitasrm.Utils.YourPreference;
import com.github.demono.AutoScrollViewPager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.eminence.sitasrm.MainActivity.badgecountmain;
import static com.eminence.sitasrm.Utils.Baseurl.baseurl;

public class HomeFragment extends Fragment implements BadgingInterface {

    private AutoScrollViewPager viewPager, recipiviewpager;
    TextView location_pincode, txt_seeAll, txt_memberShipPlan, txt_dis_coupon, txt_discriptionDiscount;
    RecyclerView product_recycle;
    FrameLayout notificationLayout;
    ImageView img_help;
    YourPreference yourPrefrence;
    TextView become_subs_btn, txtFeedBack;
    TextView lead_badge;
    CardView become_subscriber_layout, prime_member_layout;
    ArrayList<ProductModel> offerlist = new ArrayList<>();
    ArrayList<CartModel> cartList = new ArrayList<>();
    ArrayList<Feedbackmodel> Feedbacklist = new ArrayList<>();
    LinearLayout feedBackLayout, supportLayout, addressLayout;
    DatabaseHandler databaseHandler;
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

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        location_pincode = view.findViewById(R.id.location_pincode);
        product_recycle = view.findViewById(R.id.product_recycle);
        txt_seeAll = view.findViewById(R.id.txt_seeAll);
        notificationLayout = view.findViewById(R.id.notificationLayout);
        txt_memberShipPlan = view.findViewById(R.id.txt_memberShipPlan);
        become_subscriber_layout = view.findViewById(R.id.become_subscriber_layout);
        prime_member_layout = view.findViewById(R.id.prime_member_layout);
        become_subs_btn = view.findViewById(R.id.become_subs_btn);
        img_help = view.findViewById(R.id.img_help);
        feedBackLayout = view.findViewById(R.id.feedBackLayout);
        supportLayout = view.findViewById(R.id.supportLayout);
        recipiviewpager = view.findViewById(R.id.recipiviewpager);
        txtFeedBack = view.findViewById(R.id.txtFeedBack);
        txt_dis_coupon = view.findViewById(R.id.txt_dis_coupon);
        addressLayout = view.findViewById(R.id.addressLayout);
        lead_badge = view.findViewById(R.id.lead_badge);
        txt_discriptionDiscount = view.findViewById(R.id.txt_discriptionDiscount);
        viewPager.setCycle(true);
        setUpDB();

        if (Helper.INSTANCE.isNetworkAvailable(getActivity())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    GetBanner();
                    appfeedback();
                    getUtility();
                    check_subscribe_status();
                    addtocart();

                }
            }, 1000);

        } else {
            Helper.INSTANCE.Error(getActivity(), getString(R.string.NOCONN));
        }

        yourPrefrence = YourPreference.getInstance(getActivity());
        String badgevalue = yourPrefrence.getData("badge");
        if (badgevalue.equalsIgnoreCase("")) {
            badgevalue = "0";
        }

        if (badgevalue.equalsIgnoreCase("0")) {
            lead_badge.setVisibility(View.GONE);
        }

        lead_badge.setText(badgevalue);

        become_subs_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                become_subs_apicall();
            }
        });

        img_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Help.class);
                startActivity(intent);
            }
        });

        addressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Address.class);
                startActivity(intent);
            }
        });

        location_pincode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Address.class);
                startActivity(intent);
            }
        });

        txt_memberShipPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SubscriptionPlanActivity.class);
                startActivity(intent);
            }
        });

        txt_seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AllProduct.class);
                startActivity(intent);
            }
        });

        notificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Notification.class);
                startActivity(intent);
            }
        });

        feedBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Feedback.class));
            }
        });

        supportLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), Help.class));
            }
        });

        return view;

    }

    public void appfeedback() {
        Feedbacklist.clear();
        String url = baseurl + "app_feedback_list";
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String r_code = obj.getString("status");
                    if (r_code.equalsIgnoreCase("1")) {
                        JSONArray jsonArray = obj.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {

                            Feedbackmodel feedbackmodel = new Feedbackmodel();
                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                            String name = jsonObject2.getString("name");
                            String profile_photo = jsonObject2.getString("profile_photo");
                            String star_rate = jsonObject2.getString("star_rate");
                            String feedback = jsonObject2.getString("feedback");

                            feedbackmodel.setName(name);
                            feedbackmodel.setProfilephoto(profile_photo);
                            feedbackmodel.setStarrate(star_rate);
                            feedbackmodel.setFeedback(feedback);
                            Feedbacklist.add(feedbackmodel);

                        }

                        final FeedbackSliderAdapter feedbackAdapter = new FeedbackSliderAdapter(Feedbacklist, getActivity());
                        recipiviewpager.setAdapter(feedbackAdapter);
                        recipiviewpager.startAutoScroll();
                        recipiviewpager.setSlideDuration(3 * 1000);
                        recipiviewpager.setSlideInterval(4 * 1000);
                        feedbackAdapter.notifyDataSetChanged();
                        recipiviewpager.setVisibility(View.VISIBLE);
                        recipiviewpager.setVisibility(View.VISIBLE);
                    } else {

                        recipiviewpager.setVisibility(View.GONE);
                        txtFeedBack.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //    Toast.makeText(MainActivity.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }) {
        };
        requestQueue.add(stringRequest);

        stringRequest.setShouldCache(false);

    }

    public void GetBanner() {
        String url = baseurl + "banner_list";
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //   Toast.makeText(login.this, ""+response, Toast.LENGTH_SHORT).show();
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));

                    String r_code = obj.getString("status");
                    // Toast.makeText(MainActivity.this, ""+response, Toast.LENGTH_SHORT).show();

                    if (r_code.equalsIgnoreCase("1")) {

                        JSONArray jsonArray = obj.getJSONArray("data");
                        List<Images> images = new GsonBuilder().create().fromJson(obj.getJSONArray("data").toString(), new TypeToken<List<Images>>() {
                        }.getType());
                        viewPager.setVisibility(View.VISIBLE);

                        if (images != null && images.size() > 0) {
                            viewPager.setAdapter((new ImageSlideAdapter(images, getActivity())));
                            viewPager.startAutoScroll();
                            viewPager.setSlideDuration(2 * 1000);
                            viewPager.setSlideInterval(3 * 1000);
                        } else {
                            viewPager.setVisibility(View.GONE);
                        }
                    } else {
                        viewPager.setVisibility(View.GONE);
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

                    Log.i("p_response", "" + response);
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

                            offerlist.add(productModel);
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
        ProductAdapter offerlistAdapter = new ProductAdapter(offerlist, getActivity(), this,  "home");
        product_recycle.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        product_recycle.setAdapter(offerlistAdapter);
        offerlistAdapter.notifyDataSetChanged();

    }

    public void address() {
        String url = baseurl + "user_address_list";
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
                        JSONArray jsonArray = obj.getJSONArray("data");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            AddressModel addressModel = new AddressModel();
                            JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                            String state = jsonObject2.getString("state");
                            String pincode = jsonObject2.getString("pincode");
                            String defaults = jsonObject2.getString("defaults");

                            if (defaults.equalsIgnoreCase("1")) {
                                addressModel.setState(state);
                                addressModel.setPincode(pincode);
                                // location_pincode.setText("Delivered to " +state + " " +pincode );
                                location_pincode.setText(state + " " + pincode);
                            }
                        }
                    } else {
                        location_pincode.setText(R.string.address2);
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

    public void getUtility() {
        String url = baseurl + "utility";
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String description = "";
                    String status_code = obj.getString("status");

                    if (status_code.equalsIgnoreCase("1")) {
                        YourPreference yourPrefrence = YourPreference.getInstance(getActivity());
                        String language = yourPrefrence.getData("language");
                        if (language.equalsIgnoreCase("en") || language.equalsIgnoreCase("")) {
                            description = obj.getString("description");
                        } else {
                            description = obj.getString("description_hindi");
                        }
                    }

                    txt_dis_coupon.setText(Html.fromHtml(description));
                    txt_discriptionDiscount.setText(Html.fromHtml(description));
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

    @Override
    public void onResume() {
        super.onResume();

        setUpDB();
        address();
        addtocart();

        yourPrefrence = YourPreference.getInstance(getActivity());
        String badgevalue = yourPrefrence.getData("badge");
        if (badgevalue.equalsIgnoreCase("")) {
            badgevalue = "0";
        }

        if (badgevalue.equalsIgnoreCase("0")) {
            lead_badge.setVisibility(View.GONE);
        } else {
            lead_badge.setVisibility(View.VISIBLE);
        }
        lead_badge.setText(badgevalue);
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

    public void become_subs_apicall() {
        String url = baseurl + "user_subscribe";
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        YourPreference yourPrefrence = YourPreference.getInstance(getActivity());
        String id = yourPrefrence.getData("id");

        Map<String, String> params = new HashMap();
        params.put("user_id", id);
        JSONObject parameters = new JSONObject(params);


        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                // Toast.makeText(HotelMain.this, ""+response, Toast.LENGTH_SHORT).show();
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));

                    String status_code = obj.getString("status");
                    // Toast.makeText(getActivity(), "" + obj.getString("message"), Toast.LENGTH_SHORT).show();

                    if (status_code.equalsIgnoreCase("1")) {
                        Intent intent = new Intent(getActivity(), SuccesfullSubscribe.class);
                        startActivity(intent);
                    } else {

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

    public void check_subscribe_status() {
        String url = baseurl + "user_subscribe_check";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //  String username = sharedPreferences.getString("username", null);
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        YourPreference yourPrefrence = YourPreference.getInstance(getActivity());
        String id = yourPrefrence.getData("id");

        Map<String, String> params = new HashMap();
        params.put("user_id", id);
        JSONObject parameters = new JSONObject(params);


        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                // Toast.makeText(HotelMain.this, ""+response, Toast.LENGTH_SHORT).show();
                try {
                    JSONObject obj = new JSONObject(String.valueOf(response));

                    String status_code = obj.getString("status");
                    // Toast.makeText(getActivity(), "" + obj.getString("message"), Toast.LENGTH_SHORT).show();

                    if (status_code.equalsIgnoreCase("1")) {
                        become_subscriber_layout.setVisibility(View.GONE);
                        prime_member_layout.setVisibility(View.VISIBLE);
                    } else {
                        become_subscriber_layout.setVisibility(View.VISIBLE);
                        prime_member_layout.setVisibility(View.GONE);
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

    @Override
    public void badgecount() {
        badgecountmain(databaseHandler.cartInterface().getallcartdata().size());
    }

    private void setUpDB() {
        databaseHandler = Room.databaseBuilder(getActivity(), DatabaseHandler.class, "cart").allowMainThreadQueries().build();
    }

    @Override
    public void onStop() {
        setUpDB();
        addtocart();
        super.onStop();

    }

}