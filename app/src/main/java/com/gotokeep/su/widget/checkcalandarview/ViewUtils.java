package com.gotokeep.su.widget.checkcalandarview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;

/**
 * Utils to operate android views
 *
 * @author chentian
 */
public class ViewUtils {

    private static final long SMALL_SCREEN_HEIGHT_DP = 570;

    private ViewUtils() {
    }

    /**
     * Creates a view.
     *
     * @param parent parent view
     * @param resId  resource id
     * @return view
     */
    public static View newInstance(ViewGroup parent, @LayoutRes int resId) {
        return LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
    }

    /**
     * Creates a view.
     *
     * @param parent parent view
     * @param resId  resource id
     * @return view
     */
    public static View newInstance(ViewGroup parent, @LayoutRes int resId, boolean attachToRoot) {
        return LayoutInflater.from(parent.getContext()).inflate(resId, parent, attachToRoot);
    }

    /**
     * Creates a view.
     * <p>
     * - 建议使用带 parent 的形式 { @see newInstance(ViewGroup parent, @LayoutRes int resId) }
     *
     * @param context context
     * @param resId   resource id
     * @return view
     */
    public static View newInstance(Context context, @LayoutRes int resId) {
        return LayoutInflater.from(context).inflate(resId, null);
    }

    public static void setTextWhenNoNull(TextView textView, String text) {
        if (textView != null && !TextUtils.isEmpty(text)) {
            textView.setText(text);
        }
    }

    /**
     * 获取屏幕的高度，px
     */
    public static int getScreenHeightPx(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取屏幕的高度减去 status bar，px
     */
    public static int getScreenHeightWithoutStatusBar(Context context) {
        return getScreenHeightPx(context) - getStatusBarHeight(context);
    }

    /**
     * 获取屏幕的高度，dp
     */
    public static int getScreenHeightDp(Context context) {
        int heightPx = getScreenHeightPx(context);
        return (int) pxToDp(context, heightPx);
    }

    /**
     * 获取屏幕的宽度，px
     */
    public static int getScreenWidthPx(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕的宽度，dp
     */
    public static int getScreenWidthDp(Context context) {
        int widthPx = getScreenWidthPx(context);
        return (int) pxToDp(context, widthPx);
    }

    /**
     * 将dip或dp值转换为px值
     */
    public static int dpToPx(Context context, float dipValue) {
        if (context == null) {
            return 0;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将sp值转换为px值
     */
    public static int spToPx(int spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue,
                Resources.getSystem().getDisplayMetrics());
    }

    public static int getScreenMinWidth(Context context) {
        int height = getScreenHeightPx(context);
        int width = getScreenWidthPx(context);
        return height < width ? height : width;
    }

    public static void setViewWidthDp(View view, int widthDp) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = dpToPx(view.getContext(), widthDp);
        view.setLayoutParams(layoutParams);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static float getScale(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * for old scale , just return half of density
     */
    public static float getScaleForOldData(Context context) {
        return getScale(context) / 2;
    }

    public static boolean isTv(Context context) {
        return false;
    }

    public static void setBackgroundResourceAndKeepPadding(View view, int resId) {
        Drawable backgroundDrawable = ContextCompat.getDrawable(view.getContext(), resId);
        Rect drawablePadding = new Rect();
        backgroundDrawable.getPadding(drawablePadding);
        int top = view.getPaddingTop() + drawablePadding.top;
        int left = view.getPaddingLeft() + drawablePadding.left;
        int right = view.getPaddingRight() + drawablePadding.right;
        int bottom = view.getPaddingBottom() + drawablePadding.bottom;

        view.setBackgroundResource(resId);
        view.setPadding(left, top, right, bottom);
    }

    public static int getDimenPx(Context context, @DimenRes int dimenResId) {
        return context.getResources().getDimensionPixelSize(dimenResId);
    }

    public static boolean isSmallScreen(Context context, boolean isPortrait) {
        if (isPortrait) {
            return getScreenHeightDp(context) <= SMALL_SCREEN_HEIGHT_DP;
        } else {
            return getScreenWidthDp(context) <= SMALL_SCREEN_HEIGHT_DP;
        }
    }

    public static boolean isSmallScreen(Context context) {
        return getScreenHeightDp(context) <= SMALL_SCREEN_HEIGHT_DP;
    }

    public static boolean hasVirtualKey(Activity activity) {
        return activity != null && getScreenOriginalHeight(activity) > 0
                && getScreenHeightPx(activity) - getScreenOriginalHeight(activity) != 0;
    }

    public static int getVirtualKeyHeight(Activity activity) {
        return getScreenOriginalHeight(activity) - getScreenHeightPx(activity);
    }

    /**
     * Converts dp unit to equivalent pixels, depending on device density.
     */
    public static float pxToDp(Context context, float px) {
        if (context == null) {
            return 0;
        }
        return px / getScale(context);
    }

    public static void viewFadeInFromY(View view, long duration, int translateFromYDp, long delay) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator totalTranslateYAnimator =
                ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, dpToPx(view.getContext(), translateFromYDp), 0);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1);
        animatorSet.play(totalTranslateYAnimator).with(alphaAnimator);
        animatorSet.setDuration(duration);
        animatorSet.setStartDelay(delay);
        animatorSet.start();
    }

    public static void viewFadeOutFromY(View view, long duration, int translateFromYDp, long delay) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator totalTranslateYAnimator =
                ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, dpToPx(view.getContext(), translateFromYDp));
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0);
        animatorSet.play(totalTranslateYAnimator).with(alphaAnimator);
        animatorSet.setDuration(duration);
        animatorSet.setStartDelay(delay);
        animatorSet.start();
    }

    public static int getScreenOriginalHeight(Activity activity) {
        return getScreenOriginalHeight(activity.getWindowManager());
    }

    public static int getScreenOriginalHeight(WindowManager windowManager) {
        Display defaultDisplay = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        try {
            Class<?> clazz = Class.forName("android.view.Display");
            Method method = clazz.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(defaultDisplay, metrics);
            return metrics.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static int getScreenOriginalWidth(Activity activity) {
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        try {
            Class<?> clazz = Class.forName("android.view.Display");
            Method method = clazz.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(defaultDisplay, metrics);
            return metrics.widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setRoundRectangleBackground(View view, int radius, int color) {
        ShapeDrawable background = new ShapeDrawable();
        // The corners are ordered top-left, top-right, bottom-right,
        // bottom-left. For each corner, the array contains 2 values, [X_radius,
        // Y_radius]
        float[] radii = new float[8];
        radii[0] = radius;
        radii[1] = radius;
        radii[2] = radius;
        radii[3] = radius;
        radii[4] = radius;
        radii[5] = radius;
        radii[6] = radius;
        radii[7] = radius;
        background.setShape(new RoundRectShape(radii, null, null));
        background.getPaint().setColor(color);
        view.setBackgroundDrawable(background);
    }

    public static Rect getImageViewInsideSize(ImageView imageView) {
        if (imageView == null || imageView.getDrawable() == null ||
                (imageView.getScaleType() != ImageView.ScaleType.CENTER_INSIDE &&
                        imageView.getScaleType() != ImageView.ScaleType.CENTER)) {
            return null;
        }
        Drawable drawable = imageView.getDrawable();
        int imgW = drawable.getIntrinsicWidth();
        int imgH = drawable.getIntrinsicHeight();
        float imgRatio = imgH != 0 ? (float) imgW / (float) imgH : 0;
        int viewW = imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
        int viewH = imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
        float viewRatio = viewH != 0 ? (float) viewW / (float) viewH : 0;
        int finalW, finalH;
        if (imgRatio > viewRatio) {
            finalW = Math.min(imgW, viewW);
            finalH = imgRatio != 0 ? (int) (((float) finalW) / imgRatio) : 0;
        } else {
            finalH = Math.min(imgH, viewH);
            finalW = (int) (imgRatio * finalH);
        }

        int horizontalPadding = (viewW - finalW) / 2;
        int verticalPadding = (viewH - finalH) / 2;
        return new Rect(horizontalPadding + imageView.getPaddingLeft(),
                verticalPadding + imageView.getPaddingTop(),
                viewW - horizontalPadding - imageView.getPaddingBottom(),
                viewH - verticalPadding - imageView.getPaddingRight());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isFitsSystemWindows(final Activity activity) {
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ViewGroup contentView = activity.findViewById(android.R.id.content);
            if (contentView == null) {
                return false;
            }
            View firstChild = contentView.getChildAt(0);
            if (firstChild == null) {
                return false;
            }
            return firstChild.getFitsSystemWindows();
        }
        return false;
    }

    /**
     * 判断view 是否可见，
     *
     * @param ratio 主观认为显示在屏幕内的比例，算作可见
     */
    public static boolean isViewVisibleFromWindow(float ratio, View view, @LinearLayoutCompat.OrientationMode int orientation) {
        Rect localRect = new Rect();
        view.getLocalVisibleRect(localRect);
        boolean visible = false;
        if (localRect.height() == 0 || localRect.width() == 0) {
            visible = false;
        } else if (LinearLayoutCompat.VERTICAL == orientation) {
            //左上角出现在屏幕中 top = 0（向上滚动）; 右下角出现在屏幕中 bottom = height，（向下滚动）
            if (localRect.top == 0 || localRect.bottom == view.getHeight()) {
                visible = localRect.height() >= view.getHeight() * ratio;
            }
        } else if (LinearLayout.HORIZONTAL == orientation) {
            visible = localRect.width() >= view.getWidth() * ratio;
        }

        return visible;
    }

    /**
     * 监听view处理绘制
     */
    public static void addOnGlobalLayoutListener(final View view, final Runnable runnable) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                runnable.run();
            }
        });
    }

    public static Bitmap convertViewToBitmap(View view) {
        if (view.getLayoutParams() == null) {
            view.setLayoutParams(
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    /**
     * 设置状态栏背景色为透明，不隐藏虚拟导航栏
     *
     * <href ="https://blog.csdn.net/guolin_blog/article/details/51763825"/>
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void transparentActionBar(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        View decorView = activity.getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            option |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(option);
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        activity.getWindow().setStatusBarColor(color);
    }

    /**
     * 将 Activity 全屏（隐藏系统状态栏、导航栏等）
     * 在 {@link Activity#onWindowFocusChanged} 函数中调用
     *
     * <href ="https://blog.csdn.net/guolin_blog/article/details/51763825"/>
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void fullScreenActivity(Activity activity, boolean hasFocus) {
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * 取 子 View 在 父 View 中的位置
     */
    public static int getChildIndex(ViewGroup parent, View child) {
        if (parent == null || parent.getChildCount() == 0) {
            return -1;
        }
        return parent.indexOfChild(child);
    }

    /**
     * 需要对屏幕宽高比大于 1.9 的手机做适配，以 S9 Plus 为标准
     */
    public static boolean isLargeScreen(Context context) {
        int height = getScreenHeightPx(context);
        int width = getScreenWidthPx(context);
        return ((double) height / width) > 1.9f;
    }

    /**
     * 判断是否是平板
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * 距离视窗顶部高度
     */
    public static int getLocationYOnWindowPx(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return location[1];
    }
}
