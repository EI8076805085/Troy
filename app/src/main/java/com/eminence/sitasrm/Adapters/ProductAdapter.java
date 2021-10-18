package com.eminence.sitasrm.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.eminence.sitasrm.Activity.ProductDetails;
import com.eminence.sitasrm.Fragments.CartFragment;
import com.eminence.sitasrm.Interface.AddToCart;
import com.eminence.sitasrm.Interface.BadgingInterface;
import com.eminence.sitasrm.Interface.RemoveProduct;
import com.eminence.sitasrm.Interface.UpdateProduct;
import com.eminence.sitasrm.Models.CartResponse;
import com.eminence.sitasrm.Models.ProductModel;
import com.eminence.sitasrm.R;
import com.eminence.sitasrm.Utils.DatabaseHandler;
import com.eminence.sitasrm.Utils.YourPreference;

import java.util.ArrayList;

import static com.eminence.sitasrm.Utils.Baseurl.imagebaseurl;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> {
    Context context;
    ArrayList<ProductModel> subcat;
    String from;
    DatabaseHandler databaseHandler;
    BadgingInterface badgingInterface;

    public ProductAdapter(ArrayList<ProductModel> subcat, Context context, BadgingInterface badgingInterface, String from) {
        this.subcat = subcat;
        this.context = context;
        this.from = from;
        this.badgingInterface = badgingInterface;
        setUpDB();

    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        YourPreference yourPrefrence = YourPreference.getInstance(context);
        String language = yourPrefrence.getData("language");
        if (language.equalsIgnoreCase("en") || language.equalsIgnoreCase("")) {
            holder.txt_mrp.setText("MRP: ₹" + subcat.get(position).getPrice());
            holder.title.setText(subcat.get(position).getProduct_name() + " ₹" + subcat.get(position).getCaption_eng());
            holder.Discription.setText(subcat.get(position).getSingle_description_english());
            holder.txt_packofPouch.setText("Pack of" + "\n" + subcat.get(position).getPouch_quantity() + "\nPouches");
        } else {
            holder.txt_mrp.setText("एमआरपी: ₹" + subcat.get(position).getPrice());
            holder.title.setText(subcat.get(position).getP_name_hindi() + " ₹" + subcat.get(position).getCaption_eng());
            holder.Discription.setText(subcat.get(position).getSingle_description_hindi());
            holder.txt_packofPouch.setText(subcat.get(position).getPouch_quantity() + "\nपाउच \nका पैक");
        }

        if (from.equalsIgnoreCase("cart")) {
            holder.txt_plus.setVisibility(View.INVISIBLE);
            holder.txt_minus.setVisibility(View.INVISIBLE);
        }

        if (subcat.get(position).getCart_availability().equalsIgnoreCase("1")) {
            holder.inc_layout.setVisibility(View.VISIBLE);
            holder.addlayout.setVisibility(View.GONE);
            holder.txt_itemCount.setText(subcat.get(position).getQuantity());
        }

        holder.product_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (from.equalsIgnoreCase("cart")) {

                } else {
                    Intent intent = new Intent(context, ProductDetails.class);
                    intent.putExtra("productid", subcat.get(position).getProduct_id());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });

        Glide.with(context).load(imagebaseurl + subcat.get(position).getProduct_image())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.loading))
                .into(holder.product_image);


        holder.addlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.inc_layout.setVisibility(View.VISIBLE);
                holder.addlayout.setVisibility(View.GONE);
                holder.txt_itemCount.setText("1");
                CartResponse cartResponse = new CartResponse(subcat.get(position).getProduct_id(), "" + 1, subcat.get(position).getPrice());
                databaseHandler.cartInterface().addcart(cartResponse);
                badgingInterface.badgecount();
                if (from.equalsIgnoreCase("cartFragment")) {
                    CartFragment.getamount_from_adapter();
                }
            }
        });

        holder.txt_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String qty = holder.txt_itemCount.getText().toString();
                int qty2 = Integer.parseInt(qty) + 1;
                holder.txt_itemCount.setText("" + qty2);
                databaseHandler.cartInterface().setQty(subcat.get(position).getProduct_id(), "" + qty2);
                if (from.equalsIgnoreCase("cartFragment")) {
                    CartFragment.getamount_from_adapter();
                }
            }
        });

        holder.txt_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String qty = holder.txt_itemCount.getText().toString();
                if (qty.equals("0")) {
                    holder.addlayout.setVisibility(View.VISIBLE);
                    holder.inc_layout.setVisibility(View.GONE);
                } else {
                    int qty2 = Integer.parseInt(qty) - 1;
                    holder.txt_itemCount.setText("" + qty2);
                    databaseHandler.cartInterface().setQty(subcat.get(position).getProduct_id(), "" + qty2);
                    if (from.equalsIgnoreCase("cartFragment")) {
                        CartFragment.getamount_from_adapter();
                    }

                    if (qty2 == 0) {
                        if (from.equalsIgnoreCase("cartFragment")) {
                            holder.addlayout.setVisibility(View.VISIBLE);
                            holder.inc_layout.setVisibility(View.GONE);
                            databaseHandler.cartInterface().deletebyid(subcat.get(position).getProduct_id());
                            if (subcat.size() != 0) {
                                subcat.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, subcat.size());
                            }
                        } else {
                            holder.addlayout.setVisibility(View.VISIBLE);
                            holder.inc_layout.setVisibility(View.GONE);
                            databaseHandler.cartInterface().deletebyid(subcat.get(position).getProduct_id());
                        }
                        badgingInterface.badgecount();
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return subcat.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, title, Discription, txt_itemCount, txt_minus, txt_plus, txt_mrp, txt_packofPouch;
        ImageView product_image;
        LinearLayout product_layout, addlayout, inc_layout, contentLayout;

        public MyViewHolder(View view) {
            super(view);
            product_image = view.findViewById(R.id.product_image);
            title = view.findViewById(R.id.title);
            Discription = view.findViewById(R.id.Discription);
            product_layout = view.findViewById(R.id.product_layout);
            addlayout = view.findViewById(R.id.addlayout);
            inc_layout = view.findViewById(R.id.inc_layout);
            txt_itemCount = view.findViewById(R.id.txt_itemCount);
            txt_minus = view.findViewById(R.id.txt_minus);
            txt_plus = view.findViewById(R.id.txt_plus);
            contentLayout = view.findViewById(R.id.contentLayout);
            txt_mrp = view.findViewById(R.id.txt_mrp);
            txt_packofPouch = view.findViewById(R.id.txt_packofPouch);
        }
    }

    private void setUpDB() {
        databaseHandler = Room.databaseBuilder(context, DatabaseHandler.class, "cart").allowMainThreadQueries().build();
    }
}


