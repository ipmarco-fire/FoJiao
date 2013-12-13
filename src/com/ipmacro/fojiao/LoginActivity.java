package com.ipmacro.fojiao;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ipmacro.app.Config;
import com.ipmacro.app.IpmacroApplication;
import com.ipmacro.download.DownloadUtil;
import com.ipmacro.ppcore.PPCore;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements OnItemClickListener{
    List<String> urlList;
    List<Integer> modeList;
    
    LinearLayout layoutLoading;
    ListView listView;
    
    IpmacroApplication mApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        mApp = (IpmacroApplication) getApplication();
        layoutLoading = (LinearLayout) findViewById(R.id.layout_loading);
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,getData()));
        listView.setOnItemClickListener(this);
        String autokey = mApp.getAutokey();
        if(autokey == null){   //还没有激活
            layoutLoading.setVisibility(View.VISIBLE);
            doActivate();
        }else{
            if(PPCore.getExpire()<0){
                Toast.makeText(this, R.string.expire, Toast.LENGTH_LONG).show();
            }else{
                listView.setVisibility(View.VISIBLE) ;
            }
        }
    }
    
    private List<String> getData(){
        
        urlList = new ArrayList<String>();
        modeList = new ArrayList<Integer>();
        urlList.add("http://114.80.187.213/m3u8/cctv1/desc.m3u8?tag=live&video_type=m3u8&stream_id=cctv1&useloc=0&clipsize=128&clipcount=10&f_ulrg=0&cmin=3&cmax=10&path=119.97.158.37,119.97.158.117&geo=CN-9-126-1&cipi=1031882284&tmn=1375856989&pnl=812,871,213&sign=live_ipad");
        modeList.add(DownloadUtil.TYPE_M3U8);  //TYPE_M3U8:m3u8 TYPE_FLV:flv  TYPE_TS:ts TYPE_FY:10
        
        urlList.add("http://ctstream.jstv.com/live/cctv2?fmt=x264_700k_flv");
        modeList.add(DownloadUtil.TYPE_FLV); 
        return urlList;
    }
    
    private void doActivate(){
        Build bd = new Build();
        String model = bd.MODEL;
        String pwd = PPCore.getPwd(); 
        String activatioUrl = Config.ACTIVATION_URL + "?customerId=" + Config.customerId
                + "&machineCode=" + pwd + "&os="
                + android.os.Build.VERSION.RELEASE + "&models="
                + model.replaceAll(" ", "");
        Log.i(Config.TAG,activatioUrl);
        ActivateTask at = new ActivateTask();
        at.execute(activatioUrl);
    }
    
    class ActivateTask extends AsyncTask<String, Integer, String >{
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            return  HTMLUtil.downloadHTML(url);
        }
        
        @Override
        protected void onPostExecute(String result) {
            if(result!=null){
                Boolean loginOk = PPCore.login(result);
                if(loginOk){
                    mApp.setAutokey(result);
                    layoutLoading.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.activate_fail)+":"+result, Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(LoginActivity.this, R.string.connect_fail, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String url = urlList.get(position);
        int mode = modeList.get(position);
        
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, PlayActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("mode", mode);
        startActivity(intent);
    }
}
