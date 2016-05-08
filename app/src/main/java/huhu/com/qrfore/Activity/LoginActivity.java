package huhu.com.qrfore.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import huhu.com.qrfore.Net.LoginConnection;
import huhu.com.qrfore.R;
import huhu.com.qrfore.Util.Config;
import huhu.com.qrfore.Util.ToastBuilder;

/**
 * 登陆界面的Activity
 */
public class LoginActivity extends Activity {
    //账号密码输入框
    private EditText edt_account, edt_password;
    //登陆按钮
    private Button btn_login,btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    /**
     * 初始化布局资源以及添加监听器
     */
    private void init() {
        edt_account = (EditText) findViewById(R.id.edt_account);
        edt_password = (EditText) findViewById(R.id.edt_password);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_register=(Button)findViewById(R.id.btn_register);
        //注册按钮添加监听
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
        //联网设置监听
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String spoint = edt_account.getText().toString().trim();
                String spass = edt_password.getText().toString().trim();
                new LoginConnection(spoint, spass, new LoginConnection.LoginSuccess() {
                    @Override
                    public void onSuccess(String result) {
                        switch (result) {
                            case "-1":
                                ToastBuilder.Build("服务器错误", LoginActivity.this);
                                break;
                            case "1":
                                ToastBuilder.Build("密码错误", LoginActivity.this);
                                break;
                            case "2":
                                Intent i = new Intent(LoginActivity.this, MeetActivity.class);
                                startActivity(i);
                                Config.hasMeeting = false;
                                Config.SING = spoint;
                                LoginActivity.this.finish();
                                break;
                            default:
                                //带着会议信息跳转
                                Intent intent = new Intent(LoginActivity.this, MeetActivity.class);
                                intent.putExtra("detail", result);
                                Config.hasMeeting = true;
                                Config.SING = spoint;
                                startActivity(intent);
                                LoginActivity.this.finish();


                        }

                    }
                }, new LoginConnection.LoginFailed() {
                    @Override
                    public void onFailed() {
                        ToastBuilder.Build("登录失败", LoginActivity.this);

                    }
                });


            }
        });
    }
}
