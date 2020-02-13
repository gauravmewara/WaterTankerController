package com.example.watertankercontroller.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.watertankercontroller.Modal.PickupPlaceModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class PickUpLocations extends AppCompatActivity {
    ArrayList<PickupPlaceModal> placelist;
    PickUpAdapter adapter;
    EditText pickuplocations;
    RecyclerView pickuplistview;
    ProgressBar progressbar;
    TextView nodata;
    TextWatcher t1;
    //static boolean contactclicked = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_up_locations);
        Intent intent = getIntent();
        placelist = intent.getParcelableArrayListExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE);
        pickuplistview = (RecyclerView)findViewById(R.id.rv_pickuplocation_list);
        pickuplistview.setVisibility(View.GONE);
        progressbar = (ProgressBar)findViewById(R.id.pg_pickuplocation_list);
        nodata = (TextView)findViewById(R.id.tv_pickuplocation_nodata);
        nodata.setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);
        pickuplocations = (EditText)findViewById(R.id.et_pickuploaction_search);
        t1 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
                pickuplocations.setCursorVisible(true);
            }
        };
        pickuplocations.addTextChangedListener(t1);
        setRecyclerView();
    }

    public class PickUpAdapter extends RecyclerView.Adapter<PickUpAdapter.PickUpViewHolder>{
        Context context;
        ArrayList<PickupPlaceModal> adapterlist;
        public PickUpAdapter(Context context,ArrayList<PickupPlaceModal> list){
            this.context = context;
            this.adapterlist = list;
        }
        @NonNull
        @Override
        public PickUpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pickup_location_single_item,parent,false);
            return new PickUpViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PickUpViewHolder holder, int position) {
            holder.location.setText(adapterlist.get(position).getLocationname()+", "+adapterlist.get(position).getLocationaddress());
        }

        @Override
        public int getItemCount() {
            return (adapterlist!=null?adapterlist.size():0);
        }

        public class PickUpViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public TextView location;
            public RelativeLayout locationlayout;
            public PickUpViewHolder(@NonNull View itemView) {
                super(itemView);
                location = itemView.findViewById(R.id.tv_pickupitem_location);
                locationlayout = itemView.findViewById(R.id.rl_pickup_item_location);
                locationlayout.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case R.id.rl_pickup_item_location:
                        //setContactClick();
                        pickuplocations.removeTextChangedListener(t1);
                        pickuplocations.setText(placelist.get(getAdapterPosition()).getLocationname()+", "+placelist.get(getAdapterPosition()).getLocationaddress());
                        PickupPlaceModal obj1 = placelist.get(getAdapterPosition());
                        Intent intent = new Intent();
                        intent.putExtra(Constants.PICKUP_LOCATION_INTENT_DATA_TITLE,obj1);
                        setResult(Activity.RESULT_OK,intent);
                        finish();
                        break;
                }
            }
        }

        public void filterList(ArrayList<PickupPlaceModal> filteredNames){
            adapterlist = filteredNames;
            notifyDataSetChanged();
        }
    }

    private void filter(String text){
        ArrayList<PickupPlaceModal> filterNames = new ArrayList<>();
        for(int i=0;i<placelist.size();i++){
            String location = placelist.get(i).getLocationname()+", "+placelist.get(i).getLocationaddress();
            if(location.toLowerCase().contains(text.toLowerCase())){
                filterNames.add(placelist.get(i));
            }
        }
        //if(contactclicked)
            adapter.filterList(filterNames);
    }

    /*public static void setContactClick(){
        contactclicked = true;
    }*/



    public void setRecyclerView(){
        if(placelist==null){
            progressbar.setVisibility(View.GONE);
            nodata.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.GONE);
        }else{
            progressbar.setVisibility(View.GONE);
            nodata.setVisibility(View.GONE);
            pickuplistview.setVisibility(View.VISIBLE);
            adapter = new PickUpAdapter(this, placelist);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            pickuplistview.setLayoutManager(mLayoutManager);
            pickuplistview.setAdapter(adapter);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED,intent);
        finish();
    }
}
