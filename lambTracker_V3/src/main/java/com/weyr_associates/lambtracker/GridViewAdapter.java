package com.weyr_associates.lambtracker;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class GridViewAdapter extends ArrayAdapter<Item>
{
    Context mContext;
    int resourceId;
    ArrayList<Item> data = new ArrayList<Item>();

    public GridViewAdapter(Context context, int layoutResourceId, ArrayList<Item> data)
    {
        super(context, layoutResourceId, data);
        this.mContext = context;
        this.resourceId = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View itemView = convertView;
        ViewHolder holder = null;

        if (itemView == null)
        {
            final LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = layoutInflater.inflate(resourceId, parent, false);

            holder = new ViewHolder();
            
            holder.rb1_lbl = (TextView) itemView.findViewById(R.id.rb1_lbl);
            holder.ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            
//            holder.imgItem = (ImageView) itemView.findViewById(R.id.imgItem);
//            holder.txtItem = (TextView) itemView.findViewById(R.id.txtItem);
            itemView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) itemView.getTag();
        }

        Item item = getItem(position);
//        holder.imgItem.setImageDrawable(item.getImage());
//        holder.txtItem.setText(item.getTitle());
        
        holder.rb1_lbl.setText(item.getTitle());
//        holder.ratingBar(item.setBar());
        return itemView;
    }

    static class ViewHolder
    {
//        ImageView imgItem;
        TextView rb1_lbl;
        RatingBar ratingBar;
    }

}

