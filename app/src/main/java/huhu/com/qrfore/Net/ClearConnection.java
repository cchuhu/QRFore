package huhu.com.qrfore.Net;

import huhu.com.qrfore.Util.Config;
import huhu.com.qrfore.Util.HttpMethod;
import huhu.com.qrfore.Util.NetConnection;

/**
 * Created by Huhu on 5/8/16.
 * 签到清零的接口
 */
public class ClearConnection {
    public ClearConnection(String spoint, String mid, final ClearSuccess clearSuccess, final ClearFailed clearFailed) {
        new NetConnection(Config.URL_CLEAR, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                clearSuccess.onSuccess(result);
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                clearFailed.onFailed();
            }
        }, "spoint", spoint, "mid", mid);
    }

    public interface ClearSuccess {
        void onSuccess(String result);
    }

    public interface ClearFailed {
        void onFailed();
    }
}
