package com.greendot.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.greendot.R;
import com.greendot.model.RechargeType;

import java.util.List;


public class RechargeTypeListAdapter extends BaseAdapter {

    private Activity context;
    private List<RechargeType> rechargeTypeList;

    private int  selectItem = 0;

    public RechargeTypeListAdapter(Activity context, List<RechargeType> list) {
        this.context = context;
        this.rechargeTypeList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View itemView = inflater.inflate(R.layout.content_recharge_type_item, null);

        RechargeType rechargeTypeItem = rechargeTypeList.get(position);
        ImageView rechargeTypeIcon = (ImageView) itemView.findViewById(R.id.imgRechargeType);
        ImageView rechargeTypeChecked = (ImageView) itemView.findViewById(R.id.imgRechargeTypeChecked);
        TextView rechargeTypeName = (TextView) itemView.findViewById(R.id.txtRechargeTypeName);

        rechargeTypeName.setText(rechargeTypeItem.getName());
        int typeIconId = context.getResources().getIdentifier(rechargeTypeItem.getIcon(), "drawable", "com.greendot");
        rechargeTypeIcon.setImageResource(typeIconId);

        if (position == this.selectItem){
            rechargeTypeChecked.setImageResource(R.drawable.ic_checked);
            rechargeTypeChecked.setVisibility(View.VISIBLE);
            itemView.setBackgroundColor(Color.parseColor("#f2f2f2"));
        }

        return itemView;
    }

    public  void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    @Override
    public int getCount() {
        return rechargeTypeList.size();
    }

    @Override
    public Object getItem(int position) {
        return rechargeTypeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
