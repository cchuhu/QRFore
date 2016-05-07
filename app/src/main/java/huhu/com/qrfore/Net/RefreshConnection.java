package huhu.com.qrfore.Net;

import huhu.com.qrfore.Util.Config;
import huhu.com.qrfore.Util.HttpMethod;
import huhu.com.qrfore.Util.NetConnection;

/**
 * Created by Huhu on 5/6/16.
 * 签到成功，显示人员信息
 */
public class RefreshConnection {
    public RefreshConnection(String pname, final RefreshSuccess showSuccess, final RefreshFailed showFailed) {
        new NetConnection(Config.URL_REFRESH, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                showSuccess.onSuccess(result);
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                showFailed.onFailed();
            }
        }, "spoint", Config.SING);
    }

    public interface RefreshSuccess {
        void onSuccess(String result);
    }

    public interface RefreshFailed {
        void onFailed();
    }
}
