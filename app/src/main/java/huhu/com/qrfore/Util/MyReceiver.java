package huhu.com.qrfore.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import huhu.com.qrfore.Net.SignConnection;

/**
 * Created by Huhu on 5/8/16.
 * 监听网络连接的类
 */
public class MyReceiver extends BroadcastReceiver {
    public void onReceive(final Context context, Intent intent) {
        NetworkInfo.State wifiState = null;
        NetworkInfo.State mobileState = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED == mobileState) {
            // 手机网络连接成功
            Config.isOnline = true;
        } else if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED != mobileState) {
            // 手机没有任何的网络,将标志位设为false
            Config.isOnline = false;
        } else if (wifiState != null && NetworkInfo.State.CONNECTED == wifiState) {
            //无线网络连接成功
            Config.isOnline = true;
            //开始同步数据
            MyDBHelper myDBHelper = new MyDBHelper(context);
            SQLiteDatabase db = myDBHelper.getWritableDatabase();
            //查询数据
            Cursor cursor = db.rawQuery("select * from qrcode", null);
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String sign = cursor.getString(cursor.getColumnIndex("sign"));
                String mid = cursor.getString(cursor.getColumnIndex("mid"));
                //上传数据
                new SignConnection(name, sign, mid, new SignConnection.SignSuccess() {
                    @Override
                    public void onSuccess(String result) {
                        ToastBuilder.Build("同步成功", context);

                    }
                }, new SignConnection.SignFailed() {
                    @Override
                    public void onFailed() {

                    }
                });
            }
            //同步完成后删除数据表
            //db.execSQL("drop table if exists qrcode");
        }

    }
}
