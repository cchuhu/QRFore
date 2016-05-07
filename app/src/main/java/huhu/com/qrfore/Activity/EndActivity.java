package huhu.com.qrfore.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import huhu.com.qrfore.Net.EndConnection;
import huhu.com.qrfore.R;
import huhu.com.qrfore.Util.Config;

/**
 * 签到结束页面
 */
public class EndActivity extends AppCompatActivity {
    private ImageButton btn_back;
    private TextView tv_signnum, tv_starttime, tv_endtime;
    private ListView lv_name;
    private ArrayList<String> list = new ArrayList<>();
    private String count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        initViews();
    }

    private void initViews() {
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        tv_signnum = (TextView) findViewById(R.id.tv_signnum);
        tv_starttime = (TextView) findViewById(R.id.tv_signstarttime);
        tv_endtime = (TextView) findViewById(R.id.tv_signendtime);
        lv_name = (ListView) findViewById(R.id.lv_pername);
        setData();
    }

    /**
     * 设置数据
     */
    private void setData() {
        tv_starttime.setText(Config.MSTART);
        tv_endtime.setText(Config.MEND);
        new EndConnection(Config.SING, Config.MID, Config.MSTART, Config.MEND, new EndConnection.EndSuccess() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONArray array = new JSONArray(result);
                    for (int i = 0; i < array.length() - 1; i++) {
                        JSONObject obj = array.getJSONObject(i);
                        list.add(obj.get("pname").toString());
                    }
                    JSONObject jcount = array.getJSONObject(array.length() - 1);
                    count = jcount.get("scount").toString();
                    tv_signnum.setText(count);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                lv_name.setAdapter(new ArrayAdapter<>(EndActivity.this, android.R.layout.simple_expandable_list_item_1, list));

            }
        }, new EndConnection.EndFailed() {
            @Override
            public void onFailed() {

            }
        });
    }
}
