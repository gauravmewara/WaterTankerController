package com.example.watertankercontroller.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watertankercontroller.Activity.NotificationActivity;
import com.example.watertankercontroller.Modal.NotificationModal;
import com.example.watertankercontroller.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    ArrayList<NotificationModal> notificationlist;
    Context context;

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private boolean isLoadingAdded = false;

    public NotificationAdapter(Context context){
        this.context = context;
        this.notificationlist = new ArrayList<>();
    }
    public NotificationAdapter(Context context,ArrayList<NotificationModal> notificationlist){
        this.context = context;
        this.notificationlist = notificationlist;
    }
    protected class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RelativeLayout notificationlayout;
        TextView notificationheading,notificationmsg;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationlayout = (RelativeLayout)itemView.findViewById(R.id.rl_notificationitem_layout);
            notificationheading = (TextView)itemView.findViewById(R.id.tv_notificationitem_heading);
            notificationmsg = (TextView)itemView.findViewById(R.id.tv_notificationitem_data);
            notificationlayout.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.rl_notificationitem_layout:
                    notificationlayout.setClickable(false);
                    if(context instanceof NotificationActivity)
                        ((NotificationActivity)context).readNotificationApiCall(notificationlist.get(getAdapterPosition()).getNotifiactionid());
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return notificationlist == null ? 0 : notificationlist.size();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM:
                viewHolder = new NotificationViewHolder(inflater.inflate(R.layout.notification_single_item,parent,false));
                break;
            case LOADING:
                View v2 = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingViewHolder(v2);
                break;
        }
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM:
                final NotificationViewHolder mVH = (NotificationViewHolder) holder;
                mVH.notificationheading.setText(notificationlist.get(position).getTitle());
                mVH.notificationmsg.setText(notificationlist.get(position).getText());
                if(notificationlist.get(position).getIsread().equals("0"))
                    mVH.notificationlayout.setBackground(context.getDrawable(R.drawable.notification_read_background));
                else
                    mVH.notificationlayout.setBackground(context.getDrawable(R.drawable.notification_unread_background));
            break;
            case LOADING:
                break;
        }
    }


    @Override
    public int getItemViewType(int position) {
        return (position == notificationlist.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }


    public void add(NotificationModal r) {
        notificationlist.add(r);
        notifyItemInserted(notificationlist.size() - 1);
    }

    public void addAll(List<NotificationModal> moveResults) {
        for (NotificationModal result : moveResults) {
            add(result);
        }
    }

    public void remove(NotificationModal r) {
        int position = notificationlist.indexOf(r);
        if (position > -1) {
            notificationlist.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new NotificationModal());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;
        int position = notificationlist.size() - 1;
        NotificationModal result = getItem(position);
        if (result != null) {
            notificationlist.remove(position);
            notifyItemRemoved(position);
        }
    }

    public NotificationModal getItem(int position) {
        return notificationlist.get(position);
    }



    public void clearNotifications(){
        notificationlist.clear();
        notifyDataSetChanged();
    }
}
