package com.example.watertankercontroller.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watertankercontroller.Activity.BookingDetails;
import com.example.watertankercontroller.Modal.BookingModal;
import com.example.watertankercontroller.Modal.PickupPlaceModal;
import com.example.watertankercontroller.R;
import com.example.watertankercontroller.Utils.Constants;
import com.example.watertankercontroller.Utils.FetchDataListener;
import com.example.watertankercontroller.Utils.HeadersUtil;
import com.example.watertankercontroller.Utils.POSTAPIRequest;
import com.example.watertankercontroller.Utils.SessionManagement;
import com.example.watertankercontroller.Utils.URLs;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BookingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<BookingModal> bookinglist;
    Context context;
    String init_type;
    BottomSheetDialog abortsheet;
    TextView abort_delete,abort_cancel;
    int removedPosition=-1;

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private boolean isLoadingAdded = false;

    public BookingListAdapter(Context context,String init_type){
        this.context = context;
        this.init_type = init_type;
        this.bookinglist = new ArrayList<>();
    }
    public BookingListAdapter(Context context,ArrayList<BookingModal> bookinglist,String init_type){
        this.bookinglist = bookinglist;
        this.context = context;
        this.init_type = init_type;
    }

    protected class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class BookingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView bookingid,distance,fromlocation,fromtime,tolocation,totime,bookingactiontext;
        RelativeLayout ongoingView,ongoingAbort,bookingview;
        ConstraintLayout itemview;
        LinearLayout ongoingaction;

        public BookingViewHolder(View view){
            super(view);
            bookingid = (TextView)view.findViewById(R.id.tv_bookingitem_bookingid);
            distance = (TextView)view.findViewById(R.id.tv_bookingitem_distance);
            fromlocation = (TextView)view.findViewById(R.id.tv_bookingitem_fromlocation);
            fromtime = (TextView)view.findViewById(R.id.tv_bookingitem_fromtime);
            tolocation = (TextView)view.findViewById(R.id.tv_bookingitem_tolocation);
            totime = (TextView)view.findViewById(R.id.tv_bookingitem_totime);
            bookingactiontext = (TextView)view.findViewById(R.id.tv_bookingitem_viewaction);
            ongoingaction = (LinearLayout)view.findViewById(R.id.ll_bookingitem_action_ongoing);
            ongoingView = (RelativeLayout)view.findViewById(R.id.rl_bookingitem_ongoing_view);
            ongoingView.setOnClickListener(this);
            ongoingAbort = (RelativeLayout)view.findViewById(R.id.rl_bookingitem_ongoing_abort);
            ongoingAbort.setOnClickListener(this);
            bookingview = (RelativeLayout)view.findViewById(R.id.rl_bookingitem_view);
            bookingview.setOnClickListener(this);
            itemview = (ConstraintLayout)view.findViewById(R.id.cl_bookingitem_itemview);
            itemview.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.rl_bookingitem_ongoing_view:
                    break;
                case R.id.rl_bookingitem_ongoing_abort:
                    abortsheet = new BottomSheetDialog(context);
                    View sheetView = LayoutInflater.from(context).inflate(R.layout.activity_abort_dialog,null);
                    abortsheet.setContentView(sheetView);
                    abort_delete = (TextView)sheetView.findViewById(R.id.tv_abortdialog_delete);
                    abort_cancel = (TextView)sheetView.findViewById(R.id.tv_abortdialog_cancel);
                    abort_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            abort_delete.setClickable(false);
                            removedPosition = getAdapterPosition();
                            abortBooking(bookinglist.get(getAdapterPosition()).getBookingid());
                        }
                    });
                    abort_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            abortsheet.dismiss();
                        }
                    });
                    abortsheet.show();
                    break;
                case R.id.rl_bookingitem_view:
                    if(init_type.equals(Constants.PENDING_CALL)){
                        abortsheet = new BottomSheetDialog(context);
                        View sheetView2 = LayoutInflater.from(context).inflate(R.layout.activity_abort_dialog,null);
                        abortsheet.setContentView(sheetView2);
                        abort_delete = (TextView)sheetView2.findViewById(R.id.tv_abortdialog_delete);
                        abort_cancel = (TextView)sheetView2.findViewById(R.id.tv_abortdialog_cancel);
                        abort_delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                abort_delete.setClickable(false);
                                removedPosition = getAdapterPosition();
                                abortBooking(bookinglist.get(getAdapterPosition()).getBookingid());
                            }
                        });
                        abort_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                abortsheet.dismiss();
                            }
                        });

                        abortsheet.show();
                    }else{
                        intent = new Intent(context, BookingDetails.class);
                        intent.putExtra("init_type",init_type);
                        intent.putExtra("booking_id",bookinglist.get(getAdapterPosition()).getBookingid());
                        context.startActivity(intent);
                    }
                    break;
                case R.id.cl_bookingitem_itemview:
                    intent = new Intent(context, BookingDetails.class);
                    intent.putExtra("init_type",init_type);
                    intent.putExtra("booking_id",bookinglist.get(getAdapterPosition()).getBookingid());
                    context.startActivity(intent);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return bookinglist == null ? 0 : bookinglist.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM:
                viewHolder = new BookingViewHolder(inflater.inflate(R.layout.bookinglist_single_item,parent,false));
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

        switch (getItemViewType(position)){
            case ITEM:
                final BookingViewHolder mVH = (BookingViewHolder) holder;
                if(init_type.equals(Constants.ONGOING_CALL)){
                    mVH.ongoingaction.setVisibility(View.VISIBLE);
                    mVH.bookingview.setVisibility(View.GONE);
                }else{
                    if(init_type.equals(Constants.PENDING_CALL)){
                        mVH.bookingactiontext.setText("Abort");
                    }else{
                        mVH.bookingactiontext.setText("View");
                    }
                    mVH.ongoingaction.setVisibility(View.GONE);
                    mVH.bookingview.setVisibility(View.VISIBLE);
                }
                mVH.bookingid.setText(bookinglist.get(position).getBookingid());
                mVH.distance.setText(bookinglist.get(position).getDistance());
                mVH.fromtime.setText(bookinglist.get(position).getFromtime());
                mVH.totime.setText(bookinglist.get(position).getTotime());
                mVH.fromlocation.setText(styleLocation(bookinglist.get(position).getFromlocation()));
                mVH.tolocation.setText(styleLocation(bookinglist.get(position).getTolocation()));
                break;
            case LOADING:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == bookinglist.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }


    public void add(BookingModal r) {
        bookinglist.add(r);
        notifyItemInserted(bookinglist.size() - 1);
    }

    public void addAll(List<BookingModal> moveResults) {
        for (BookingModal result : moveResults) {
            add(result);
        }
    }

    public void remove(BookingModal r) {
        int position = bookinglist.indexOf(r);
        if (position > -1) {
            bookinglist.remove(position);
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
        add(new BookingModal());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;
        int position = bookinglist.size() - 1;
        BookingModal result = getItem(position);
        if (result != null) {
            bookinglist.remove(position);
            notifyItemRemoved(position);
        }
    }

    public BookingModal getItem(int position) {
        return bookinglist.get(position);
    }


    private SpannableStringBuilder styleLocation(String location){
        int index = location.indexOf(",");
        if(index==-1){
            index = location.indexOf(" ");
        }
        final SpannableStringBuilder sb = new SpannableStringBuilder(location);
        final StyleSpan bss = new StyleSpan(Typeface.BOLD);
        if(index!=-1) {
            sb.setSpan(new AbsoluteSizeSpan(12, true), 0, index, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(new AbsoluteSizeSpan(10, true), index + 1, location.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(bss, 0, index, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }else{
            sb.setSpan(new AbsoluteSizeSpan(12, true), 0, location.length()-1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(bss, 0, location.length()-1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return sb;
    }

    public void abortBooking(String bookingidval){
        try{
            JSONObject jsonBody = new JSONObject();
            POSTAPIRequest getapiRequest=new POSTAPIRequest();
            String url = URLs.BASE_URL+URLs.ABORT_BOOKING+bookingidval;
            Log.i("url", String.valueOf(url));
            String token = SessionManagement.getUserToken(context);
            HeadersUtil headparam = new HeadersUtil(token);
            getapiRequest.request(context.getApplicationContext(),abortBookingListener,url,headparam,jsonBody);
        }catch (Exception e){
            e.printStackTrace();
            abort_delete.setClickable(true);
            abortsheet.dismiss();
        }
    }

    FetchDataListener abortBookingListener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject data) {
            try{
                if(data!=null){
                    if(data.getInt("error")==0){
                        Toast.makeText(context,data.getString("message"),Toast.LENGTH_LONG).show();
                        abort_delete.setClickable(true);
                        abortsheet.dismiss();
                        if(removedPosition!=-1){
                            bookinglist.remove(removedPosition);
                            removedPosition = -1;
                            notifyDataSetChanged();
                        }
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
                abort_delete.setClickable(true);
                abortsheet.dismiss();
            }
        }

        @Override
        public void onFetchFailure(String msg) {
            abort_delete.setClickable(true);
            abortsheet.dismiss();
        }

        @Override
        public void onFetchStart() {

        }
    };

}
