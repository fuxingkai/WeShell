package cn.jufuns.ws.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Vector;

import cn.jufuns.ws.R;
import cn.zxing.camera.CameraManager;
import cn.zxing.decoding.CaptureActivityHandler;
import cn.zxing.decoding.InactivityTimer;
import cn.zxing.view.ViewfinderView;

public class ScanningActivity extends BaseAppActivity implements OnClickListener,
        Callback {

    private Context mContext;

    TextView tv_title_name;
    ImageView img_back;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        mContext = this;
        init();
    }

    @Override
    public void permissionSuccess(int requestCode) {
    }

    @Override
    public void permissionFail(int requestCode) {
    }

    public void init() {

        requestPermission(new String[]{Manifest.permission.CAMERA});

        setScanWidthHeight();
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        /*
         * switch (v.getId()) { case R.id.img_back: this.finish();
		 * overridePendingTransition(0, R.anim.out_to_right); break; // case
		 * R.id.tv_jump: // Intent intent = new Intent(getApplicationContext(),
		 * // DetailedInformationActivity.class); // startActivity(intent); //
		 * overridePendingTransition(R.anim.default_anim_in, //
		 * R.anim.default_anim_out); // break;
		 * 
		 * default: break; }
		 */
    }

    public void handleDecode(Result result, Bitmap barcode) {
        // inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        // FIXME
        if (resultString.equals("")) {
            Toast.makeText(ScanningActivity.this, "Scan failed!",
                    Toast.LENGTH_SHORT).show();
        } else {
//			Toast.makeText(getApplicationContext(), "" + resultString,
//					Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.putExtra("resultString", resultString);

            setResult(RESULT_OK, intent);
            this.finish();

            // inactivityTimer.onActivity();

            // Intent intent=new Intent(this,DetailedInformationActivity.class);
            // intent.putExtra("member_id", resultString);
            // startActivity(intent);
            // this.finish();

            // System.out.println("Result:"+resultString);
            // Intent resultIntent = new Intent();
            // Bundle bundle = new Bundle();
            // bundle.putString("result", resultString);
            // resultIntent.putExtras(bundle);
            // this.setResult(RESULT_OK, resultIntent);
        }

        // ScanningActivity.this.finish();

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                //用户没有授予相机权限会报的异常
                if (null != e && "Fail to connect to camera service".equals(e.getMessage())) {
                    //Toast.makeText(mContext,"无法获取摄像头数据，请检查是否已经打开摄像头权限",Toast.LENGTH_LONG).show();
                    showTipsDialog();
                }
            }
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

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

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

			/*
             * AssetFileDescriptor file = getResources().openRawResourceFd(
			 * R.raw.beep); try {
			 * mediaPlayer.setDataSource(file.getFileDescriptor(),
			 * file.getStartOffset(), file.getLength()); file.close();
			 * mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			 * mediaPlayer.prepare(); } catch (IOException e) { mediaPlayer =
			 * null; }
			 */
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    private void setScanWidthHeight() {
        // 设置扫描的大�?
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        int width = widthPixels < heightPixels ? widthPixels : heightPixels;
        if (width <= 0)
            width = 320;
        CameraManager.MIN_FRAME_WIDTH = (int) (width * 3 / 5);
        CameraManager.MIN_FRAME_HEIGHT = (int) (width * 3 / 5);
        CameraManager.MAX_FRAME_WIDTH = (int) (width * 3 / 5);// (int)(width*2/3);
        CameraManager.MAX_FRAME_HEIGHT = (int) (width * 3 / 5);
    }

}
