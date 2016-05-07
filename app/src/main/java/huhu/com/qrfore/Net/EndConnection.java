package huhu.com.qrfore.Net;

import huhu.com.qrfore.Util.Config;
import huhu.com.qrfore.Util.HttpMethod;
import huhu.com.qrfore.Util.NetConnection;

/**
 * Created by Huhu on 5/6/16.
 * 结束签到接口
 */
public class EndConnection {
    public EndConnection(String spoint, String mid, String sstarttime, String sendtime, final EndSuccess endSuccess, final EndFailed endFailed) {
        new NetConnection(Config.URL_CUT, HttpMethod.POST, new NetConnection.SuccessCallback() {
            @Override
            public void onSuccess(String result) {
                endSuccess.onSuccess(result);
            }
        }, new NetConnection.FailCallback() {
            @Override
            public void onFail() {
                endFailed.onFailed();
            }
        }, "spoint", spoint, "mid", mid, "sstarttime", sstarttime, "sendtime", sendtime);
    }

    public interface EndSuccess {
        void onSuccess(String result);
    }

    public interface EndFailed {
        void onFailed();
    }
}
