package cn.jufuns.ws.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.jufuns.ws.R;


public class LoadingDialog {
	
	/**
	 * 弹出框显示时，拦截返回键
	 */
	private static OnKeyListener keylistener = new OnKeyListener(){
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode== KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0)
            {
             return true;
            }
            else
            {
             return false;
            }
        }
    };
	
	public static Dialog createLoadingDialog(Context context)
    {  
        LayoutInflater mInflater = LayoutInflater.from(context);
        //加载布局
        
        LinearLayout layout = (LinearLayout) mInflater.inflate(R.layout.view_loading_dialog, null);
        //FrameLayout layout = (FrameLayout) mInflater.inflate(R.layout.view_loading_dialog, null);
        //加载需要进行动画效果的图片
        ImageView image = (ImageView) layout.findViewById(R.id.iv_loadingDialog_bg);
        //创建动画
        Animation animation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator interpolator = new LinearInterpolator(); //匀速旋转
        animation.setInterpolator(interpolator);  
        animation.setDuration(2000); //一次动画耗时2000ms  
        animation.setRepeatCount(-1); //重复播放动画  
        //显示动画  
        image.startAnimation(animation);  
        //创建对话框  
        Dialog loadingDialog = new Dialog(context, R.style.loadingDialog);
        loadingDialog.setContentView(layout);  
        loadingDialog.setOnKeyListener(keylistener);
        loadingDialog.setCanceledOnTouchOutside(false);
        return loadingDialog;
    }  
	
	
    
	
}
