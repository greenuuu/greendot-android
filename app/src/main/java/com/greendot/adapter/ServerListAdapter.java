package com.greendot.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.greendot.R;
import com.greendot.model.Server;

import java.util.List;


public class ServerListAdapter extends BaseAdapter {
    private Activity context;
    private List<Server> serverList;

    private int  selectItem = 0;

    public ServerListAdapter(Activity context, List<Server> list) {
        this.context = context;
        this.serverList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View itemView = inflater.inflate(R.layout.content_server_item, null);

        Server serverItem = serverList.get(position);

        ImageView serverIcon = (ImageView) itemView.findViewById(R.id.imgServerIcon);
        ImageView serverChecked = (ImageView) itemView.findViewById(R.id.imgServerChecked);
        TextView serverName = (TextView) itemView.findViewById(R.id.txtServerName);
        TextView serverHost = (TextView) itemView.findViewById(R.id.txtServerHost);

        serverName.setText(serverItem.getName());
        serverHost.setText(serverItem.getHost());

        if (position == this.selectItem){
            serverChecked.setImageResource(R.drawable.ic_dot);
            serverChecked.setVisibility(View.VISIBLE);
            itemView.setBackgroundColor(Color.parseColor("#f2f2f2"));
        }

        int serverIconId = context.getResources().getIdentifier(serverItem.getIcon(), "drawable", "com.greendot");
        serverIcon.setImageResource(serverIconId);

        return itemView;
    }

    public  void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    @Override
    public int getCount() {
        return serverList.size();
    }

    @Override
    public Object getItem(int position) {
        return serverList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
