package huhu.com.qrfore.Net;

import huhu.com.qrfore.Util.Config;
import huhu.com.qrfore.Util.HttpMethod;
import huhu.com.qrfore.Util.NetConnection;

/**
 * Created by Huhu on 5/6/16.
 * 扫描出信息，签到记录
 */
public class SignConnection {
    public SignConnection(String pname, String spoint, String mid, final SignSuccess signSuccess, final SignFailed signFailed) {
        new NetConnection(Config.URL_SIGN, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                signSuccess.onSuccess(result);
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                signFailed.onFailed();
            }
        }, "pname", pname, "spoint", spoint, "mid", mid);
    }

    public interface SignSuccess {
        void onSuccess(String result);
    }

    public interface SignFailed {
        void onFailed();
    }
}
