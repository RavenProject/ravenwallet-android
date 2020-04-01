package com.ravenwallet.presenter.customviews;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;

import com.ravenwallet.R;
import com.ravenwallet.tools.util.Utils;

public abstract class IconButton extends ImageButton {


    protected abstract int getIconId();

    private static final String TAG = IconButton.class.getName();
    private static int ANIMATION_DURATION = 30;
    private Bitmap shadow;
    private Rect shadowRect;
    private RectF bRect;
    private int width;
    private int height;
    private int modifiedWidth;
    private int modifiedHeight;
    private Paint bPaint;
    private Paint bPaintStroke;
    private int type = 4;
    private static final float SHADOW_PRESSED = 0.88f;
    private static final float SHADOW_UNPRESSED = 0.95f;
    private float shadowOffSet = SHADOW_UNPRESSED;
    private static final int ROUND_PIXELS = 16;
    private boolean isBreadButton; //meaning is has the special animation and shadow
    private boolean hasShadow; // allows us to add/remove the drop shadow from the button without affecting the animation

    public IconButton(Context context) {
        super(context);
        init(context, null);
    }

    public IconButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        int iconId = getIconId();
        setImageResource(iconId);
    }

    public IconButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public IconButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attrs) {
        shadow = BitmapFactory.decodeResource(getResources(), R.drawable.shadow);
        bPaint = new Paint();
        bPaintStroke = new Paint();
        shadowRect = new Rect(0, 0, 100, 100);
        bRect = new RectF(0, 0, 100, 100);
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.BRButton);
        float px16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        //check attributes you need, for example all paddings
        int[] attributes = new int[]{android.R.attr.paddingStart, android.R.attr.paddingTop, android.R.attr.paddingEnd, android.R.attr.paddingBottom, R.attr.isBreadButton, R.attr.buttonType};
        //then obtain typed array
        TypedArray arr = ctx.obtainStyledAttributes(attrs, attributes);
        //You can check if attribute exists (in this example checking paddingRight)

        isBreadButton = a.getBoolean(R.styleable.BRButton_isBreadButton, true);
        int paddingLeft = arr.hasValue(0) ? arr.getDimensionPixelOffset(0, -1) : (int) px16;
        int paddingTop = arr.hasValue(1) ? arr.getDimensionPixelOffset(1, -1) : 0;
        int paddingRight = arr.hasValue(2) ? arr.getDimensionPixelOffset(2, -1) : (int) px16;
        int paddingBottom = arr.hasValue(3) ? arr.getDimensionPixelOffset(3, -1) + (isBreadButton ? (int) px16 : 0) : (isBreadButton ? (int) px16 : 0);
        hasShadow = a.getBoolean(R.styleable.BRButton_hasShadow, true);

        int type = a.getInteger(R.styleable.BRButton_buttonType, 0);
        setType(type);

        bPaint.setAntiAlias(true);
        bPaintStroke.setAntiAlias(true);

        if (isBreadButton) {
            setBackground(getContext().getDrawable(R.drawable.shadow_trans));
            setBackground(getContext().getDrawable(R.drawable.shadow_trans));
        }

        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
//        int padding = dpToPx(10);
//        setPadding(padding, padding, padding, padding);
        a.recycle();
        arr.recycle();
        final ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                correctTextSizeIfNeeded();
                correctTextBalance();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isBreadButton) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                if (type != 3)
                    press(ANIMATION_DURATION);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                unPress(ANIMATION_DURATION);
            }
        }

        return super.onTouchEvent(event);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

    }

    private void correctTextSizeIfNeeded() {
//        int limit = 100;
//        int lines = getLineCount();
//        float px = getTextSize();
//        while (lines > 1 && !getText().toString().contains("\n")) {
//            limit--;
//            px -= 1;
//            setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
//            lines = getLineCount();
//            if (limit <= 0) {
//                Log.e(TAG, "correctTextSizeIfNeeded: Failed to rescale, limit reached, final: " + px);
//                break;
//            }
//        }
    }

    private void correctTextBalance() {
//        Rect bounds = new Rect();
//        Paint textPaint = getPaint();
//        textPaint.getTextBounds(getText().toString(), 0, getText().toString().length(), bounds);
//        int height = bounds.height();
//        int width = bounds.width();

//        int paddingLeft = getPaddingLeft();
//        int paddingTop = getPaddingTop();
//        int paddingRight = getPaddingRight();
//        int paddingBottom = getPaddingBottom();
//
//        paddingTop = 5;
//        paddingBottom = height - 5 - modifiedHeight;
//
//        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
//        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isBreadButton) {
            if (hasShadow) {
                shadowRect.set(5, height / 4, width - 5, (int) (height * shadowOffSet));
                canvas.drawBitmap(shadow, null, shadowRect, null);

            }
            modifiedWidth = width - 10;
            modifiedHeight = height - height / 4 - 5;
            bRect.set(5, 5, modifiedWidth, modifiedHeight + 5);
            canvas.drawRoundRect(bRect, ROUND_PIXELS, ROUND_PIXELS, bPaint);
            if (type == 2 || type == 3)
                canvas.drawRoundRect(bRect, ROUND_PIXELS, ROUND_PIXELS, bPaintStroke);
        }
        super.onDraw(canvas);

    }

    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
        invalidate();
    }

    public void setColor(int color) {
        bPaint.setColor(color);

        invalidate();

    }

    public void setType(int type) {
        if (type == 3) press(1);
        this.type = type;

        if (type == 1) { //blue
            bPaint.setColor(getContext().getColor(R.color.primaryColor));
        } else if (type == 2) {
            bPaintStroke.setColor(getContext().getColor(R.color.secondaryColor));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Utils.getPixelsFromDps(getContext(), 1));
            bPaint.setColor(getContext().getColor(R.color.secondaryColor));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 3) {
            bPaintStroke.setColor(getContext().getColor(R.color.tetriaryColor));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Utils.getPixelsFromDps(getContext(), 1));
            bPaint.setColor(getContext().getColor(R.color.tetriaryColor));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 4) {
            bPaintStroke.setColor(getContext().getColor(R.color.tetriaryColor));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Utils.getPixelsFromDps(getContext(), 1));
            bPaint.setColor(getContext().getColor(R.color.tetriaryColor));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 5) {
            bPaintStroke.setColor(getContext().getColor(R.color.menu_asset_disabled_color));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Utils.getPixelsFromDps(getContext(), 1));
            bPaint.setColor(getContext().getColor(R.color.menu_asset_disabled_color));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 6) {
            bPaint.setColor(getContext().getColor(R.color.red));
        }
        invalidate();
    }

    private void press(int duration) {
        ScaleAnimation scaleAnim = new ScaleAnimation(
                1f, 0.96f,
                1f, 0.96f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 1f);
        scaleAnim.setDuration(duration);
        scaleAnim.setRepeatCount(0);
        scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnim.setFillAfter(true);
        scaleAnim.setFillBefore(true);
        scaleAnim.setFillEnabled(true);

        ValueAnimator shadowAnim = ValueAnimator.ofFloat(SHADOW_UNPRESSED, SHADOW_PRESSED);
        shadowAnim.setDuration(duration);
        shadowAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                shadowOffSet = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        startAnimation(scaleAnim);
        shadowAnim.start();

    }

    private void unPress(int duration) {
        ScaleAnimation scaleAnim = new ScaleAnimation(
                0.96f, 1f,
                0.96f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 1f);
        scaleAnim.setDuration(duration);
        scaleAnim.setRepeatCount(0);
        scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnim.setFillAfter(true);
        scaleAnim.setFillBefore(true);
        scaleAnim.setFillEnabled(true);

        ValueAnimator shadowAnim = ValueAnimator.ofFloat(SHADOW_PRESSED, SHADOW_UNPRESSED);
        shadowAnim.setDuration(duration);
        shadowAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                shadowOffSet = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        startAnimation(scaleAnim);
        shadowAnim.start();
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        int minWidth = dpToPx(55);
//        int padding = dpToPx(10);
//        setMinimumWidth(minWidth);
//        setPadding(padding, padding, padding, padding);
//    }
//    public static int dpToPx(int dp) {
//        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
//    }
}