package com.example.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllAppActivity extends AppCompatActivity implements View.OnClickListener{


    public static final int FILTER_ALL_APP = 0; // 所有应用程序
    public static final int FILTER_SYSTEM_APP = 1; // 系统程序
    public static final int FILTER_THIRD_APP = 2; // 第三方应用程序
    public static final int FILTER_SDCARD_APP = 3; // 安装在SDCard的应用程序

    private Button btnAllApp;
    private Button btnSystemApp;
    private Button btnThirdApp;
    private Button btnSdApp;

    private ApplicationInfoAdapter appAdapter;

    private ListView listview = null;
    private int filter =FILTER_THIRD_APP ;
    private PackageManager pm;
    private List<AppInfo> mlistAppInfo = null;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_app);
        mContext =AllAppActivity.this;
        initView();
//        mlistAppInfo = new ArrayList<AppInfo>();
//        queryAppInfo(); // 查询所有应用程序信息
        bindData(filter);

    }

    public void initView(){

        btnAllApp = (Button) findViewById(R.id.btn_allApp);
        btnSystemApp = (Button) findViewById(R.id.btn_systemApp);
        btnThirdApp = (Button) findViewById(R.id.btn_thirdApp);
        btnSdApp = (Button) findViewById(R.id.btn_sdApp);
        listview = (ListView) findViewById(R.id.listviewApp);

        btnAllApp.setOnClickListener(this);
        btnSystemApp.setOnClickListener(this);
        btnThirdApp.setOnClickListener(this);
        btnSdApp.setOnClickListener(this);

    }

    public void bindData(int filter){

        mlistAppInfo =queryFilterAppInfo(filter);
        appAdapter= new ApplicationInfoAdapter(
                this, mlistAppInfo);

        listview.setAdapter(appAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                doStartApplicationWithPackageName(mlistAppInfo.get(position).getPkgName());
            }
        });


    }

    // 获得所有启动Activity的信息，类似于Launch界面
    public void queryAppInfo() {
        PackageManager pm = this.getPackageManager(); // 获得PackageManager对象
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm
                .queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);
        // 调用系统排序 ， 根据name排序
        // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
        Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
        if (mlistAppInfo != null) {
            mlistAppInfo.clear();
            for (ResolveInfo reInfo : resolveInfos) {
                String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
                String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
                String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
                Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
                // 为应用程序的启动Activity 准备Intent
                Intent launchIntent = new Intent();
                launchIntent.setComponent(new ComponentName(pkgName,
                        activityName));
                // 创建一个AppInfo对象，并赋值
                AppInfo appInfo = new AppInfo();
                appInfo.setAppLabel(appLabel);
                appInfo.setPkgName(pkgName);
                appInfo.setAppIcon(icon);
                appInfo.setIntent(launchIntent);
                mlistAppInfo.add(appInfo); // 添加至列表中
               Log.i("cx",appLabel + " activityName---" + activityName
                        + " pkgName---" + pkgName);
            }
        }
    }

    private List<AppInfo> queryFilterAppInfo(int filter){
        pm =this.getPackageManager();
        //查询所有已经安装的应用程序
        List<ApplicationInfo> list=pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(list,new ApplicationInfo.DisplayNameComparator(pm)); //排序
        List<AppInfo> appInfos =new ArrayList<>();// 保存过滤查到的结果


        //根据条件来过滤
        switch (filter){
            case FILTER_ALL_APP:
                appInfos.clear();
                for(ApplicationInfo app:list){
                    appInfos.add(getAppInfo(app));
                }
                return appInfos;

            case FILTER_SYSTEM_APP:
                appInfos.clear();
                for(ApplicationInfo app:list){
                    if((app.flags & ApplicationInfo.FLAG_SYSTEM)!=0){
                        appInfos.add(getAppInfo(app));
                    }
                }
                return appInfos;

            case FILTER_THIRD_APP:
                appInfos.clear();
                for(ApplicationInfo app:list){
                    //第三方程序，非系统程序
                    if((app.flags & ApplicationInfo.FLAG_SYSTEM) <=0){
                        appInfos.add(getAppInfo(app));
                    }else if((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
                        appInfos.add(getAppInfo(app));
                    }
                }
                break;
            case FILTER_SDCARD_APP:
                appInfos.clear();
                for (ApplicationInfo app : list) {
                    if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                        appInfos.add(getAppInfo(app));
                    }
                }
                return appInfos;
            default:
                return null;
        }
        return appInfos;

    }

    // 构造一个AppInfo对象，并赋值
    private AppInfo getAppInfo(ApplicationInfo app){
        AppInfo appInfo =new AppInfo();
        appInfo.setAppLabel((String) app.loadLabel(pm));
        appInfo.setAppIcon(app.loadIcon(pm));
        appInfo.setPkgName(app.packageName);
        return appInfo;
    }

    private void doStartApplicationWithPackageName(String packageName){
        //通过包名获取此App详细信息
        PackageInfo packageInfo =null;
        try {
            packageInfo =getPackageManager().getPackageInfo(packageName,0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(packageInfo ==null){
            return ;
        }

        //创建一个类别为CATRGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent =new Intent(Intent.ACTION_MAIN,null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageInfo.packageName);

        //通过getPackageManager()的queryIntentActivitires方法去遍历
        List<ResolveInfo> resolveInfos =getPackageManager().queryIntentActivities(resolveIntent,0);
        for(ResolveInfo resolveInfo:resolveInfos){
            if(resolveInfo!=null){
                String pkgName =resolveInfo.activityInfo.name;
                String className =resolveInfo.activityInfo.name;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                //设置CompoentName
                ComponentName cn =new ComponentName(packageName,className);
                intent.setComponent(cn);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_allApp:
                filter =FILTER_ALL_APP;
                bindData(filter);
                break;
            case R.id.btn_systemApp:
                filter=FILTER_SYSTEM_APP;
                bindData(filter);
                break;
            case R.id.btn_thirdApp:
                filter=FILTER_THIRD_APP;
                bindData(filter);
                break;
            case R.id.btn_sdApp:
                filter =FILTER_SDCARD_APP;
                bindData(filter);
                break;
            default:
                break;
        }
    }
}
