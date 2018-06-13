package cn.zxing.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;

import cn.jufuns.ws.R;
import cn.zxing.camera.CameraManager;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 * 
 */
public final class ViewfinderView extends View {

	 private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
     private static final long ANIMATION_DELAY = 80L;
     private static final int MAX_RESULT_POINTS = 20;
     private static final int OPAQUE = 0xA0;
     private CameraManager cameraManager;
     private final Paint paint;
     private Bitmap resultBitmap;        
     private final int maskColor;
     private final int resultColor;
     
//     private final int frameColor;
//     private final int laserColor;
//     private final int resultPointColor;
     
     private int scannerAlpha;
     private List<ResultPoint> possibleResultPoints;
     private Collection<ResultPoint> lastPossibleResultPoints;
     
     private int i = 0;// 
     private Rect mRect;// 
     private GradientDrawable mDrawable;// 
     private Drawable lineDrawable;// 

     // This constructor is used when the class is built from an XML resource.
     public ViewfinderView(Context context, AttributeSet attrs) {
             super(context, attrs);

             // Initialize these once for performance rather than calling them every
             // time in onDraw().
             paint = new Paint(Paint.ANTI_ALIAS_FLAG);
             Resources resources = getResources();
             maskColor = resources.getColor(R.color.viewfinder_mask);
             resultColor = resources.getColor(R.color.result_view);
             
//             frameColor = resources.getColor(R.color.viewfinder_frame);
//             laserColor = resources.getColor(R.color.viewfinder_laser);
//             resultPointColor = resources.getColor(R.color.possible_result_points);
             mRect = new Rect();
             int left = getResources().getColor(R.color.lightgreen);
             int center = getResources().getColor(R.color.green);
             int right = getResources().getColor(R.color.lightgreen);
             lineDrawable = getResources().getDrawable(R.drawable.qrcode_scan_line);
             mDrawable = new GradientDrawable(
                             GradientDrawable.Orientation.LEFT_RIGHT, new int[] { left,
                                             left, center, right, right });
             
             scannerAlpha = 0;
             possibleResultPoints = new ArrayList<ResultPoint>(5);
     }

     @Override
     public void onDraw(Canvas canvas) {
             Rect frame = CameraManager.get().getFramingRect();
             if (frame == null) {
                     return;
             }
             int width = canvas.getWidth();
             int height = canvas.getHeight();

             // Draw the exterior (i.e. outside the framing rect) darkened
             paint.setColor(resultBitmap != null ? resultColor : maskColor);
             canvas.drawRect(0, 0, width, frame.top, paint);
             canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
             canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
             canvas.drawRect(0, frame.bottom, width, height, paint);

             if (resultBitmap != null) {
                     // Draw the opaque result bitmap over the scanning rectangle
                     paint.setAlpha(OPAQUE);
                     canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
             } else {

                     // Draw a two pixel solid black border inside the framing rect
                     paint.setColor(getResources().getColor(R.color.green));
                     //
                     canvas.drawRect(frame.left, frame.top, frame.left + 30,
                                     frame.top + 5, paint);
                     canvas.drawRect(frame.left, frame.top, frame.left + 5,
                                     frame.top + 30, paint);
                     // 
                     canvas.drawRect(frame.right - 30, frame.top, frame.right,
                                     frame.top + 5, paint);
                     canvas.drawRect(frame.right - 5, frame.top, frame.right,
                                     frame.top + 30, paint);
                     // 
                     canvas.drawRect(frame.left, frame.bottom - 5, frame.left + 30,
                                     frame.bottom, paint);
                     canvas.drawRect(frame.left, frame.bottom - 30, frame.left + 5,
                                     frame.bottom, paint);
                     // 
                     canvas.drawRect(frame.right - 30, frame.bottom - 5, frame.right,
                                     frame.bottom, paint);
                     canvas.drawRect(frame.right - 5, frame.bottom - 30, frame.right,
                                     frame.bottom, paint);

                     // Draw a red "laser scanner" line through the middle to show
                     // decoding is active
                     paint.setColor(getResources().getColor(R.color.green));
                     paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
                     scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
                     
//                     int middle = frame.height() / 2 + frame.top;
//                     canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);
//
//                     Collection<ResultPoint> currentPossible = possibleResultPoints;
//                     Collection<ResultPoint> currentLast = lastPossibleResultPoints;
                     if ((i += 5) < frame.bottom - frame.top) {
                             mRect.set(frame.left - 6, frame.top + i - 6, frame.right + 6,
                                             frame.top + 6 + i);
                             lineDrawable.setBounds(mRect);
                             lineDrawable.draw(canvas);
                             invalidate();
                     } else {
                             i = 0;
                     }
//                     if (currentLast != null) {
//                             paint.setAlpha(OPAQUE / 2);
//                             paint.setColor(resultPointColor);
//                             for (ResultPoint point : currentLast) {
//                                     canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
//                             }
//                     }

                     // Request another update at the animation interval, but only
                     // repaint the laser line,
                     // not the entire viewfinder mask.
                     postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
             }
     }

     public void drawViewfinder() {
             Bitmap resultBitmap = this.resultBitmap;
             this.resultBitmap = null;
             if (resultBitmap != null) {
                     resultBitmap.recycle();
             }
             invalidate();
     }

     /**
      * Draw a bitmap with the result points highlighted instead of the live
      * scanning display.
      * 
      * @param barcode
      *            An image of the decoded barcode.
      */
     public void drawResultBitmap(Bitmap barcode) {
             resultBitmap = barcode;
             invalidate();
     }

     public void addPossibleResultPoint(ResultPoint point) {
             List<ResultPoint> points = possibleResultPoints;
             synchronized (points) {
                     points.add(point);
                     int size = points.size();
                     if (size > MAX_RESULT_POINTS) {
                             points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
                     }
             }
     }

}

