package huhu.com.qrfore.Net;

import huhu.com.qrfore.Util.Config;
import huhu.com.qrfore.Util.HttpMethod;
import huhu.com.qrfore.Util.NetConnection;

/**
 * Created by Huhu on 5/8/16.
 * 注册接口
 */
public class RegisterConnection {
    public RegisterConnection(String spoint, String spass, final RegistSuccess registSuccess, final RegistFailed registFailed) {
        new NetConnection(Config.URL_REGISTER, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                registSuccess.onSuccess(result);
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                registFailed.onFailed();
            }
        }, "spoint", spoint, "spass", spass);
    }

    public interface RegistSuccess {
        void onSuccess(String result);
    }

    public interface RegistFailed {
        void onFailed();
    }
}
