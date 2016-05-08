package huhu.com.qrfore.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import huhu.com.qrfore.Net.SignConnection;
import huhu.com.qrfore.R;
import huhu.com.qrfore.Util.Config;
import huhu.com.qrfore.Util.MyDBHelper;
import huhu.com.qrfore.Util.ToastBuilder;
import huhu.com.qrfore.Widget.PersonInfoWindow;
import zxing.AmbientLightManager;
import zxing.BeepManager;
import zxing.CameraManager;
import zxing.CaptureActivityHandler;
import zxing.FinishListener;
import zxing.InactivityTimer;
import zxing.ViewfinderView;


public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {
    //闪光灯开关、手动签到开关
    private ImageButton btn_torch, btn_handop;
    //闪光灯是否开启标志位
    private boolean isTorchOn = false;
    //相机管理器
    private CameraManager cameraManager;
    //清零和停止按钮
    private Button btn_clear, btn_stop;
    //显示人数的textView
    private TextView tv_num;
    //展示信息的popupwindow
    private PersonInfoWindow personInfoWindow;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            personInfoWindow.dismiss();

        }
    };
    SoundPool soundPool;
    int sourceid;
    //----------------------------------
    private CaptureActivityHandler handler;
    private Result savedResultToShow;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private AmbientLightManager ambientLightManager;

    //----------------------------------
    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.capture);
        initViews();
    }

    /**
     * 初始化资源
     */
    private void initViews() {
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_handop = (ImageButton) findViewById(R.id.btn_operation);
        btn_torch = (ImageButton) findViewById(R.id.btn_torch);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        ambientLightManager = new AmbientLightManager(this);
        //加载音频
        soundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        sourceid = soundPool.load(this, R.raw.sound, 0);
        //闪光灯按钮添加监听
        btn_torch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTorchOn) {
                    isTorchOn = false;
                    cameraManager.setTorch(false);
                } else {
                    isTorchOn = true;
                    cameraManager.setTorch(true);
                }
            }
        });
        //手动签到按钮添加监听
        btn_handop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CaptureActivity.this, HandOpActivity.class);
                startActivity(i);
            }
        });
        //结束签到添加监听
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                //记住开始时间
                Config.MEND = now.toString();
                Intent i = new Intent(CaptureActivity.this, EndActivity.class);
                startActivity(i);
                finish();
            }
        });

    }


    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        cameraManager = new CameraManager(getApplication());

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);

        handler = null;
        resetStatusView();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);

        inactivityTimer.onResume();

        decodeFormats = null;
        characterSet = null;
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        ambientLightManager.stop();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        viewfinderView.recycleLineDrawable();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA:// ���������
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * 处理识别结果
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) throws UnsupportedEncodingException {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();

        String msg = rawResult.getText();
        if (msg == null || "".equals(msg)) {
            msg = "�޷�ʶ��";
        }
        if (msg.equals("")) {
            Toast.makeText(CaptureActivity.this, R.string.not_scan, Toast.LENGTH_SHORT).show();
        } else {
            Log.e("msg", msg);
            final String finalMsg = URLDecoder.decode(msg, "utf-8");
            if (Config.isOnline == true) {

                new SignConnection(msg, Config.SING, Config.MID, new SignConnection.SignSuccess() {
                    @Override
                    public void onSuccess(String result) {
                        try {

                            switch (result) {
                                case "-1":
                                    ToastBuilder.Build("服务器错误", CaptureActivity.this);
                                    break;
                                case "1":
                                    ToastBuilder.Build("此人已经签过到", CaptureActivity.this);
                                    break;
                                case "2":
                                    ToastBuilder.Build("查无此人", CaptureActivity.this);
                                    break;
                                default:
                                    JSONObject obj = new JSONObject(result);
                                    String phone = obj.get("ptel").toString();
                                    String job = obj.get("pjob").toString();
                                    //展示人员信息
                                    showDetail(viewfinderView, finalMsg, phone, job);
                                    //将签到人数递增
                                    Config.hasSign++;
                                    soundPool.play(sourceid, 1, 1, 0, 0, 1);


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new SignConnection.SignFailed() {
                    @Override
                    public void onFailed() {
                        ToastBuilder.Build("签到失败，请重试", CaptureActivity.this);

                    }
                });
            } else {
                MyDBHelper myDBHelper = new MyDBHelper(CaptureActivity.this);
                SQLiteDatabase db = myDBHelper.getWritableDatabase();
                //插入数据库
                db.execSQL("insert into qrcode(name,sign,mid) values(?,?,?)", new String[]{msg, Config.SING, Config.MID});
                showDetail(viewfinderView, finalMsg, "", "");
            }

        }
        restartPreviewAfterDelay(1000);

    }

    /**
     * 显示弹出窗口
     */
    private void showDetail(View view, String name, String phone, String job) {
        //设置弹出窗口
        WindowManager wm = (WindowManager) CaptureActivity.this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels - 100;
        int height = outMetrics.heightPixels / 2;
        personInfoWindow = new PersonInfoWindow(name, phone, job, CaptureActivity.this, width, height);
        personInfoWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        personInfoWindow.setOutsideTouchable(true);
        //设置窗口2秒后自动消失
        mHandler.sendEmptyMessageDelayed(0, 2000);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            return;
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("����");
        builder.setMessage("��Ǹ������������⣬��������Ҫ�����豸");
        builder.setPositiveButton("ȷ��", new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    private void resetStatusView() {
        viewfinderView.setVisibility(View.VISIBLE);
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }
}
