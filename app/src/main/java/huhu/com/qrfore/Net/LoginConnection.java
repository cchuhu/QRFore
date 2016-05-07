package huhu.com.qrfore.Net;

import huhu.com.qrfore.Util.Config;
import huhu.com.qrfore.Util.HttpMethod;
import huhu.com.qrfore.Util.NetConnection;

/**
 * Created by Huhu on 5/6/16.
 * 登陆的接口
 */
public class LoginConnection {

    public LoginConnection(String spoint, String spass, final LoginSuccess loginSuccess, final LoginFailed loginFailed) {
        new NetConnection(Config.URL_LOGIN, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                loginSuccess.onSuccess(result);
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                loginFailed.onFailed();

            }
        }, "spoint", spoint, "spass", spass) {
        };

    }

    /**
     * 成功接口
     */
    public interface LoginSuccess {
        void onSuccess(String result);
    }

    /**
     * 失败接口
     */
    public interface LoginFailed {
        void onFailed();
    }

}
