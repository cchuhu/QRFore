package huhu.com.qrfore.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import huhu.com.qrfore.Net.SignConnection;
import huhu.com.qrfore.R;
import huhu.com.qrfore.Util.Config;
import huhu.com.qrfore.Util.ToastBuilder;
import huhu.com.qrfore.Widget.PersonInfoWindow;

/**
 * 手动签到页面
 */
public class HandOpActivity extends Activity {
    //转换为二维码签到的按钮
    private ImageButton btn_qr;
    //签到人员的姓名
    private EditText edt_name;
    //签到按钮
    private Button btn_sign;
    //屏幕窗口宽高
    private int width, height;
    //弹出窗口
    private PersonInfoWindow personInfoWindow;
    private String name, job, phone;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            personInfoWindow.dismiss();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand_op);
        initViews();
    }

    /**
     * 初始化视图资源
     */
    private void initViews() {
        btn_qr = (ImageButton) findViewById(R.id.btn_qr);
        edt_name = (EditText) findViewById(R.id.edt_name);
        btn_sign = (Button) findViewById(R.id.btn_handsign);
        btn_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HandOpActivity.this, CaptureActivity.class);
                startActivity(i);
            }
        });


        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                name = edt_name.getText().toString();
                if (name.equals("")) {
                    ToastBuilder.Build("请输入姓名", HandOpActivity.this);
                } else {

                    new SignConnection(name, Config.SING, Config.MID, new SignConnection.SignSuccess() {
                        @Override
                        public void onSuccess(String result) {
                            try {
                                System.out.print(name + Config.SING + Config.MID);
                                switch (result) {
                                    case "-1":
                                        ToastBuilder.Build("服务器错误", HandOpActivity.this);
                                        break;
                                    case "1":
                                        ToastBuilder.Build("此人已经签过到", HandOpActivity.this);
                                        break;
                                    case "2":
                                        ToastBuilder.Build("查无此人", HandOpActivity.this);
                                        break;
                                    default:
                                        JSONObject obj = new JSONObject(result);
                                        phone = obj.get("ptel").toString();
                                        job = obj.get("pjob").toString();
                                        //展示人员信息
                                        showDetail(view, name, phone, job);
                                        //将签到人数递增
                                        Config.hasSign++;
                                        //将签到框清空

                                }
                                edt_name.setText("");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    }, new SignConnection.SignFailed() {
                        @Override
                        public void onFailed() {
                            ToastBuilder.Build("签到失败，请重试", HandOpActivity.this);

                        }
                    });
                }

            }
        });
    }


    /**
     * 显示弹出窗口
     */
    private void showDetail(View view, String name, String phone, String job) {
        //设置弹出窗口
        WindowManager wm = (WindowManager) HandOpActivity.this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels - 100;
        height = outMetrics.heightPixels / 2;
        personInfoWindow = new PersonInfoWindow(name, phone, job, HandOpActivity.this, width, height);
        personInfoWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        personInfoWindow.setOutsideTouchable(true);
        //设置窗口3秒后自动消失
        handler.sendEmptyMessageDelayed(0, 2000);
    }
}
