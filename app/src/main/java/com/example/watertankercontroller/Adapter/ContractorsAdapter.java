package com.example.watertankercontroller.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.watertankercontroller.Modal.ContractorModal;
import com.example.watertankercontroller.R;

import java.util.ArrayList;

public class ContractorsAdapter extends BaseAdapter {

    Context context;
    ArrayList<ContractorModal>list;
    LayoutInflater inflator;

    public ContractorsAdapter(Context context, ArrayList<ContractorModal> list) {
        this.context = context;
        this.list = list;
        this.inflator =(LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflator.inflate(R.layout.layout_item_contractor_spinner, null);
        TextView names = (TextView) view.findViewById(R.id.tvContractorName);
        names.setText(list.get(position).getContracator_id());


        return view;
    }

}
