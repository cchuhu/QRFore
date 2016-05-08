package huhu.com.qrfore.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import huhu.com.qrfore.Net.RegisterConnection;
import huhu.com.qrfore.R;
import huhu.com.qrfore.Util.ToastBuilder;

public class RegisterActivity extends AppCompatActivity {
    Button btn_register;
    EditText edt_account, edt_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btn_register = (Button) findViewById(R.id.btn_register);
        edt_account = (EditText) findViewById(R.id.edt_acc);
        edt_pass = (EditText) findViewById(R.id.edt_pass);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String acc = edt_account.getText().toString();
                String pass = edt_pass.getText().toString();
                if (acc.equals("") || pass.equals("")) {
                    ToastBuilder.Build("请输入有效信息", RegisterActivity.this);
                } else {
                    new RegisterConnection(acc, pass, new RegisterConnection.RegistSuccess() {
                        @Override
                        public void onSuccess(String result) {
                            if (result.equals("1")) {
                                ToastBuilder.Build("注册成功", RegisterActivity.this);
                                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                ToastBuilder.Build("请重试", RegisterActivity.this);
                            }
                        }
                    }, new RegisterConnection.RegistFailed() {
                        @Override
                        public void onFailed() {

                        }
                    });
                }

            }
        });

    }
}
