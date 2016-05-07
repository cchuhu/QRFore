package huhu.com.qrfore.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

import huhu.com.qrfore.Net.RefreshConnection;
import huhu.com.qrfore.R;
import huhu.com.qrfore.Util.Config;
import huhu.com.qrfore.Util.DateUtil;
import huhu.com.qrfore.Util.ToastBuilder;

/**
 * 显示即将进行签到的会议具体信息
 */
public class MeetActivity extends Activity {
    //会议名称
    private TextView tv_meetname, tv_content, tv_num, tv_begintime, tv_endtime;
    //提示会议未开始的图片
    private ImageView img_hint;
    //刷新按钮
    private ImageButton btn_refresh;
    //开始签到的按钮
    private Button btn_sign;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);

        initView();
        if (Config.hasMeeting == false) {
            relativeLayout.setVisibility(View.GONE);
            img_hint.setVisibility(View.VISIBLE);
        } else {
            img_hint.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
            String detail = getIntent().getExtras().get("detail").toString();
            try {
                setData(detail);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 初始化视图资源
     */
    private void initView() {
        tv_meetname = (TextView) findViewById(R.id.tv_meetname);
        tv_content = (TextView) findViewById(R.id.tv_content);
        tv_num = (TextView) findViewById(R.id.tv_num);
        tv_begintime = (TextView) findViewById(R.id.tv_begintime);
        tv_endtime = (TextView) findViewById(R.id.tv_endtime);
        img_hint = (ImageView) findViewById(R.id.img_hint);
        btn_refresh = (ImageButton) findViewById(R.id.btn_refresh);
        btn_sign = (Button) findViewById(R.id.btn_beginsign);
        relativeLayout = (RelativeLayout) findViewById(R.id.layout_hasdata);
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                //记住开始时间
                Config.MSTART = now.toString();
                Intent i = new Intent(MeetActivity.this, CaptureActivity.class);
                startActivityForResult(i, 0);

            }
        });
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RefreshConnection(Config.SING, new RefreshConnection.RefreshSuccess() {
                    @Override
                    public void onSuccess(String result) {
                        switch (result) {
                            case "-1":
                                ToastBuilder.Build("服务器错误", MeetActivity.this);
                                break;
                            case "2":
                                ToastBuilder.Build("暂无会议", MeetActivity.this);
                                break;
                            default:
                                try {
                                    setData(result);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                }, new RefreshConnection.RefreshFailed() {
                    @Override
                    public void onFailed() {

                    }
                });
            }
        });
    }

    /**
     * 将数据设置到组件上
     */
    private void setData(String detail) throws JSONException {
        JSONObject obj = new JSONObject(detail);
        Config.MID = obj.get("mid").toString();
        Config.MNAME = obj.get("mname").toString();
        Config.MSTARTTIME = DateUtil.getDateToString(DateUtil.getStringToDate(obj.get("mstarttime").toString()));
        Config.MENDTIME = DateUtil.getDateToString(DateUtil.getStringToDate(obj.get("mendtime").toString()));
        Config.MCONTENT = obj.get("mcontent").toString();
        Config.MCOUNT = obj.get("mcount").toString();
        img_hint.setVisibility(View.GONE);
        relativeLayout.setVisibility(View.VISIBLE);
        tv_meetname.setText(Config.MNAME);
        tv_content.setText(Config.MCONTENT);
        tv_begintime.setText(Config.MSTARTTIME);
        tv_endtime.setText(Config.MENDTIME);
        tv_num.setText(Config.MCOUNT);
    }


}
