package com.example.watertankercontroller.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watertankercontroller.Modal.TankerModal;
import com.example.watertankercontroller.R;

import java.util.ArrayList;

public class TankerAdapter extends RecyclerView.Adapter<TankerAdapter.TankerViewHolder> {
    ArrayList<TankerModal> tankerlist;
    Context context;

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private boolean isLoadingAdded = false;

    public TankerAdapter(Context context, ArrayList<TankerModal> tankerlist){
        this.context = context;
        this.tankerlist = tankerlist;
    }
    @NonNull
    @Override
    public TankerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tanker_single_item,parent,false);
        return new TankerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TankerViewHolder holder, int position) {
        holder.drivername.setText(tankerlist.get(position).getDrivername());
        holder.drivermobile.setText(tankerlist.get(position).getDrivermobile());
        holder.contractorname.setText(tankerlist.get(position).getContractorname());
        holder.tankermodel.setText(tankerlist.get(position).getTankerModelNo());
        holder.tankercapacity.setText(tankerlist.get(position).getTankercapacity());
        if(tankerlist.get(position).getOngoing().equals("0")){
            holder.tankerstatus.setImageResource(R.drawable.tanker_offline_icon);
        }else{
            holder.tankerstatus.setImageResource(R.drawable.tanker_online_icon);
        }
    }

    @Override
    public int getItemCount() {
        return tankerlist.size();
    }

    public class TankerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView tankerimg,tankerstatus;
        TextView drivername,drivermobile,contractorname,tankermodel,tankercapacity;
        ConstraintLayout tankeritem;
        public TankerViewHolder(@NonNull View itemView) {
            super(itemView);
            drivername = (TextView)itemView.findViewById(R.id.tv_tankeritem_drivername);
            drivermobile = (TextView)itemView.findViewById(R.id.tv_tankeritem_drivermobile);
            contractorname = (TextView)itemView.findViewById(R.id.tv_tankeritem_contractorname);
            tankermodel = (TextView)itemView.findViewById(R.id.tv_tankeritem_tankermodel);
            tankercapacity = (TextView)itemView.findViewById(R.id.tv_tankeritem_tankercapacity);
            tankerimg = (ImageView)itemView.findViewById(R.id.iv_tankeritem_tanker);
            tankerstatus = (ImageView)itemView.findViewById(R.id.iv_tankeritem_tankeronline);
            tankeritem = (ConstraintLayout)itemView.findViewById(R.id.cl_tankeritem);
        }

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.cl_tankeritem:
                    break;
            }
        }
    }
}
