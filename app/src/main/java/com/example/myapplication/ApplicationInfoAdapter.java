package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2017/10/21.
 */

public class ApplicationInfoAdapter extends BaseAdapter{

    private List<AppInfo> mListAppInfo =null;
    LayoutInflater inflater =null;

    public ApplicationInfoAdapter(Context context, List<AppInfo> mListAppInfo) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mListAppInfo = mListAppInfo;

    }

    @Override
    public int getCount() {
        Log.i("cx","size:"+mListAppInfo.size());
        return mListAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mListAppInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("cx","getView at " + position);
        View view = null;
        ViewHolder holder = null;
        if (convertView == null || convertView.getTag() == null) {
            view = inflater.inflate(R.layout.browse_app_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        else{
            view = convertView ;
            holder = (ViewHolder) convertView.getTag() ;
        }
        AppInfo appInfo = (AppInfo) getItem(position);
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        holder.tvAppLabel.setText(appInfo.getAppLabel());
        holder.tvPkgName.setText(appInfo.getPkgName());
        return view;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;
        TextView tvPkgName;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.imgApp);
            this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);
            this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
        }
    }
}
