package com.greendot.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.greendot.R;
import com.greendot.model.Recharge;
import java.util.List;


public class RechargeListAdapter extends BaseAdapter {

    private Activity context;
    private List<Recharge> rechargeList;

    private int selectItem = 0;

    public RechargeListAdapter(Activity context, List<Recharge> list) {
        this.context = context;
        this.rechargeList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View itemView = inflater.inflate(R.layout.content_recharge_item, null);

        Recharge rechargeItem = rechargeList.get(position);
        ImageView rechargeChecked = (ImageView) itemView.findViewById(R.id.imgRechargeChecked);
        TextView rechargeCash = (TextView) itemView.findViewById(R.id.txtRechargeCash);
        TextView rechargeMonth = (TextView) itemView.findViewById(R.id.txtRechargeMonth);
        TextView rechargeDiscount = (TextView) itemView.findViewById(R.id.txtRechargeDiscount);

        rechargeMonth.setText(String.valueOf(rechargeItem.getMonth()));
        rechargeCash.setText(rechargeItem.getDiscountcash());

        if (null!=rechargeItem.getDiscount() && !"10".equals(rechargeItem.getDiscount())){
            rechargeDiscount.setText(rechargeItem.getDiscount() + "折优惠");
            rechargeDiscount.setVisibility(View.VISIBLE);
        }

        if (position == this.selectItem){
            rechargeChecked.setImageResource(R.drawable.ic_checked);
            rechargeChecked.setVisibility(View.VISIBLE);
            itemView.setBackgroundColor(Color.parseColor("#f2f2f2"));
        }

        return itemView;
    }

    public  void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    @Override
    public int getCount() {
        return rechargeList.size();
    }

    @Override
    public Object getItem(int position) {
        return rechargeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
