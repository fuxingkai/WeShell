package cn.jufuns.ws.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import cn.jufuns.ws.R;

/**
 * Created by zhangxg on 2016/8/10.
 */
public class QuestionAlertDialog extends AbstractDialog {

    private static final String TAG = "QuestionAlertDialog";

    private boolean mOutSideCancel = true;
    private boolean mCanCancelByUser = true;
    private boolean mHideWhenButtonClick = true;

    private CharSequence mTitleText;
    private CharSequence mContentText;
    private CharSequence mLeftBtnText;
    private CharSequence mRightBtnText;

    private boolean mDownAtDialogContent;

    protected TextView mTvTitle;
    protected View mViewTitleDivider;
    protected TextView mTvContent;
    protected Button mBtnLeft;
    protected View mViewBtnDivider;
    protected Button mBtnRight;

    private ViewGroup mDialogContentView;
    private Rect mDialogContentRect;
    private int[] mDialogContentLocation = new int[2];

    private int mContentViewIndex;

    private OnDialogButtonClickListener mListener;

    public QuestionAlertDialog(Context context) {
        super(context);
        mDialogContentRect = new Rect();

        mContentViewIndex = mDialogContentView.indexOfChild(mTvContent);
    }

    public QuestionAlertDialog setTouchOutsideCancel(boolean touchOutsideCancel) {
        mOutSideCancel = touchOutsideCancel;
        return this;
    }

    public QuestionAlertDialog setCanCancelByUser(boolean canCancelByUser) {
        mCanCancelByUser = canCancelByUser;
        return this;
    }

    public boolean getCanCancelByUser() {
        return mCanCancelByUser;
    }

    public QuestionAlertDialog setOnButtonClickListener(OnDialogButtonClickListener listener) {
        mListener = listener;
        return this;
    }

    public QuestionAlertDialog setHideWhenButtonClick(boolean hideWhenButtonClick) {
        mHideWhenButtonClick = hideWhenButtonClick;
        return this;
    }

    public QuestionAlertDialog setTitleText(CharSequence titleText) {
        mTitleText = titleText;
        if (TextUtils.isEmpty(mTitleText)) {
            mTvTitle.setVisibility(View.GONE);
            mViewTitleDivider.setVisibility(View.GONE);
        } else {
            mTvTitle.setText(mTitleText);
            mTvTitle.setVisibility(View.VISIBLE);
            mViewTitleDivider.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public QuestionAlertDialog setContent(CharSequence contentText) {
        mContentText = contentText;
        if (mTvContent.getParent() != null) {
            mTvContent.setText(TextUtils.isEmpty(mContentText) ? "" : mContentText);
        } else {
            Log.e(TAG, "text view content has been removed from the dialog");
        }
        return this;
    }

    public QuestionAlertDialog setLeftBtnVisible(boolean show) {
        if (show) {
            mBtnLeft.setVisibility(View.VISIBLE);
            mViewBtnDivider.setVisibility(View.VISIBLE);
        } else {
            mBtnLeft.setVisibility(View.GONE);
            mViewBtnDivider.setVisibility(View.GONE);
        }
        return this;
    }

    public QuestionAlertDialog setContent(View contentView) {
        if (contentView == null) {
            throw new IllegalArgumentException("content view is null");
        }

        if (contentView.getParent() != null) {
            throw new IllegalStateException("content view has been add to other layout");
        }

        mDialogContentView.removeViewAt(mContentViewIndex);
        mDialogContentView.addView(contentView, mContentViewIndex);

        return this;
    }

    public QuestionAlertDialog setLeftBtnText(CharSequence btnText) {
        mLeftBtnText = btnText;
        mBtnLeft.setText(TextUtils.isEmpty(mLeftBtnText) ? "" : mLeftBtnText);
        return this;
    }

    public QuestionAlertDialog setRightBtnText(CharSequence btnText) {
        mRightBtnText = btnText;
        mBtnRight.setText(TextUtils.isEmpty(mRightBtnText) ? "" : mRightBtnText);
        return this;
    }

    @Override
    protected View createContentView(Context context) {

        RelativeLayout layoutWindowContent = new RelativeLayout(context) {

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {

                if (onKeyEvent(event)) {
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {

                onTouch(ev);
                return super.dispatchTouchEvent(ev);
            }

        };
        layoutWindowContent.setBackgroundColor(Color.parseColor("#80000000"));

        LinearLayout layoutContent = new LinearLayout(context);
        layoutContent.setOrientation(LinearLayout.HORIZONTAL);
        layoutContent.setBackgroundResource(R.drawable.fillet_gray_white);
        RelativeLayout.LayoutParams lpContent = new RelativeLayout.LayoutParams(
                ((int) (context.getResources().getDisplayMetrics().widthPixels * 0.8)), RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpContent.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutWindowContent.addView(layoutContent, lpContent);

        Space spaceLeft = new Space(context);
        Space spaceRight = new Space(context);
        LinearLayout layoutDialogContent = new LinearLayout(context);

        mDialogContentView = layoutDialogContent;

        LinearLayout.LayoutParams lpSpace = new LinearLayout.LayoutParams(14, 1);
//        lpSpace.weight = 1f;
        LinearLayout.LayoutParams lpDialogContent = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpDialogContent.weight = 6f;


        layoutContent.addView(spaceLeft, lpSpace);
        layoutContent.addView(layoutDialogContent, lpDialogContent);
        layoutContent.addView(spaceRight, lpSpace);

        //the real dialog content
        GradientDrawable bgDrawable = new GradientDrawable();
//        bgDrawable.setCornerRadius(dp2px(8));
        bgDrawable.setColor(Color.WHITE);
        layoutDialogContent.setOrientation(LinearLayout.VERTICAL);
        if (Build.VERSION.SDK_INT >= 16) {
            layoutDialogContent.setBackground(bgDrawable);
        } else {
            layoutDialogContent.setBackgroundDrawable(bgDrawable);
        }

        //title
        int textColor = Color.parseColor("#333333");
        TextView tvTitle = new TextView(context);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvTitle.setTextColor(textColor);
        tvTitle.setSingleLine(true);
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams lpTvTitle = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int titleMargin = dp2px(10);
        lpTvTitle.setMargins(titleMargin, titleMargin, titleMargin, titleMargin);
        layoutDialogContent.addView(tvTitle, lpTvTitle);
        mTvTitle = tvTitle;
        mTvTitle.setVisibility(View.GONE);

        //divider
        View viewTitleDivider = new View(context);
        viewTitleDivider.setBackgroundColor(Color.parseColor("#ff5400"));
        layoutDialogContent.addView(viewTitleDivider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp2px(1)));
        mViewTitleDivider = viewTitleDivider;
        mViewTitleDivider.setVisibility(View.GONE);

        //content
        TextView tvContent = new TextView(context);
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17.45f);
        tvContent.setTextColor(textColor);
        LinearLayout.LayoutParams lpTvContent = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int tvContentMargin = dp2px(15);
        lpTvContent.setMargins(tvContentMargin, tvContentMargin, tvContentMargin, tvContentMargin);
        layoutDialogContent.addView(tvContent, lpTvContent);
        mTvContent = tvContent;

        //divider between content and button layout
        View viewBottomDivider = new View(context);
        viewBottomDivider.setBackgroundColor(Color.parseColor("#e6e6e6"));
        layoutDialogContent.addView(viewBottomDivider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));

        //button
        LinearLayout layoutBtn = new LinearLayout(context);
        layoutBtn.setOrientation(LinearLayout.HORIZONTAL);
        layoutDialogContent.addView(layoutBtn, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp2px(48)));

        //button selector
        StateListDrawable btnLeftSelectorDrawable = new StateListDrawable();
        btnLeftSelectorDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Color.parseColor("#e6e6e6")));
        btnLeftSelectorDrawable.addState(new int[]{-android.R.attr.state_enabled}, new ColorDrawable(Color.parseColor("#fefefe")));
        btnLeftSelectorDrawable.addState(new int[]{}, new ColorDrawable(Color.WHITE));
        StateListDrawable btnRightSelectorDrawable = new StateListDrawable();
        btnRightSelectorDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Color.parseColor("#e6e6e6")));
        btnRightSelectorDrawable.addState(new int[]{-android.R.attr.state_enabled}, new ColorDrawable(Color.parseColor("#fefefe")));
        btnRightSelectorDrawable.addState(new int[]{}, new ColorDrawable(Color.WHITE));

        Button btnLeft = new Button(context);
        btnLeft.setText("取消");
        btnLeft.setTextColor(textColor);
        btnLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        if (Build.VERSION.SDK_INT >= 16) {
            btnLeft.setBackground(btnLeftSelectorDrawable);
        } else {
            btnLeft.setBackgroundDrawable(btnLeftSelectorDrawable);
        }
        layoutBtn.addView(btnLeft, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        mBtnLeft = btnLeft;
        mBtnLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onBtnLeftClick();
            }
        });

        View viewBtnDivider = new View(context);
        viewBtnDivider.setBackgroundColor(Color.parseColor("#e6e6e6"));
        layoutBtn.addView(viewBtnDivider, new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT));
        mViewBtnDivider = viewBtnDivider;

        Button btnRight = new Button(context);
        btnRight.setText("确定");
        btnRight.setTextColor(textColor);
        btnRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        if (Build.VERSION.SDK_INT >= 16) {
            btnRight.setBackground(btnRightSelectorDrawable);
        } else {
            btnRight.setBackgroundDrawable(btnRightSelectorDrawable);
        }
        layoutBtn.addView(btnRight, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        mBtnRight = btnRight;
        mBtnRight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onBtnRightClick();
            }
        });

        return layoutWindowContent;
    }

    /* package */ boolean onKeyEvent(KeyEvent event) {
        if (!mCanCancelByUser) {
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            hide();
            return true;
        }

        return false;
    }

    /* package */ void onTouch(MotionEvent event) {

        if (!mCanCancelByUser) {
            return;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                mDialogContentView.getLocationOnScreen(mDialogContentLocation);
                mDialogContentRect.left = mDialogContentLocation[0];
                mDialogContentRect.top = mDialogContentLocation[1];
                mDialogContentRect.right = mDialogContentRect.left + mDialogContentView.getWidth();
                mDialogContentRect.bottom = mDialogContentRect.top + mDialogContentView.getHeight();

                mDownAtDialogContent = mDialogContentRect.contains((int) event.getRawX(), (int) event.getRawY());
                break;
            }

            case MotionEvent.ACTION_UP: {
                boolean upAtDialogContent = mDialogContentRect.contains((int) event.getRawX(), (int) event.getRawY());
                if (mOutSideCancel && !mDownAtDialogContent && !upAtDialogContent) {
                    hide();
                }
                break;
            }

            default: {
                break;
            }
        }
    }

    /* package */
    final void onBtnLeftClick() {
        if (mHideWhenButtonClick) {
            hide();
        }

        if (mListener != null) {
            mListener.onLeftButtonClick();
        }
    }

    /* package */
    final void onBtnRightClick() {
        if (mHideWhenButtonClick) {
            hide();
        }

        if (mListener != null) {
            mListener.onRightButtonClick();
        }
    }
}
