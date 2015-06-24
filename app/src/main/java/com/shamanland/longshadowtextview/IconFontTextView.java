package com.shamanland.longshadowtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;



/**
 *  If you want to assign custom value, first add to following code snippet to your .xml
 *    xmlns:ifTextView="http://schemas.android.com/apk/res/com.cleanmaster.security"
 *
 *    then assign value, for example : "ifTextView:bgColor="@color/intl_scanresult_item_circle_fb_bg"
 *
 *    we provide following custom attrs :
 *      ifTextView:bgColor
 *      ifTextView:strokeColor
 *      ifTextView:strokeWidth
 *
 */
public class IconFontTextView extends TextView {

    private static final String TAG = IconFontTextView.class.getSimpleName();
    private static final String DEFAULT_FONT = "CMS_IconFonts.ttf";
    private final int defaultBgColor = Color.parseColor("#dc552c");
    private final int defaultStrokeColor = Color.parseColor("#00000000");
    private final float defaultStrokeWidth = 0;

    private int mShapeType;
    private int mStrokeColor;
    private float mStrokeWidth;
    private TextPaint mStrokePaint;

    private String mFontName;

    public IconFontTextView(Context context) {
        this(context, null);
    }

    public IconFontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconFontTextView, defStyle, 0);

        try {

            mFontName = DEFAULT_FONT;

            // setup stroke -- begin
            mStrokeColor = a.getColor(R.styleable.IconFontTextView_strokeColor, defaultStrokeColor);
            mStrokeWidth = a.getFloat(R.styleable.IconFontTextView_strokeWidth, defaultStrokeWidth);
            mStrokePaint = new TextPaint();

            // copy
            mStrokePaint.setTextSize(getTextSize());
            mStrokePaint.setTypeface(getTypeface());
            mStrokePaint.setFlags(getPaintFlags());

            Log.d(TAG, "getTextSize() : " + getTextSize());

            // custom
            try {
                mStrokePaint.setStyle(Paint.Style.STROKE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mStrokePaint.setColor(mStrokeColor);
            mStrokePaint.setStrokeWidth(mStrokeWidth);
            // setup stroke -- end

            int bgShapeColor;
            mShapeType = a.getInt(R.styleable.IconFontTextView_bgShape, -1);
            if (mShapeType == 0){
                bgShapeColor = a.getColor(R.styleable.IconFontTextView_bgColor, defaultBgColor);
                ShapeDrawable background = new ShapeDrawable(new OvalShape());
                background.getPaint().setColor(bgShapeColor);
                background.getPaint().setAntiAlias(true);
                this.setBackgroundDrawable(background);

            }else if (mShapeType == 1){
                int r =  DimenUtils.dp2px(5);
                float[] outerR = new float[] {r, r, r, r, r, r, r, r};

                bgShapeColor = a.getColor(R.styleable.IconFontTextView_bgColor, defaultBgColor);
                ShapeDrawable background = new ShapeDrawable(new RoundRectShape(outerR, null, null));
                background.getPaint().setColor(bgShapeColor);
                background.getPaint().setAntiAlias(true);
                this.setBackgroundDrawable(background);
            }

            setTypeface();

        } finally {
            a.recycle();
        }

    }

    public void setBackgroundColorResource(int resID){
        if (mShapeType >= 0){

            //TODO : add rect-style shape 
            ShapeDrawable background = new ShapeDrawable(new OvalShape());
            background.getPaint().setColor(getResources().getColor(resID));
            background.getPaint().setAntiAlias(true);
            this.setBackgroundDrawable(background);
        }
    }

    public void setStrokeColor(int color) {
        mStrokeColor = color;
    }

    public void setStrokeWidth(float width) {
        mStrokeWidth = width;
    }


    private void setTypeface() {
        if (!TextUtils.isEmpty(mFontName)) {
            try {
                Typeface font = FontUitls.getFont(getContext(), mFontName);
                if (font != null) {
                    setTypeface(font);
                }
            } catch (Exception e) {
            }
        }
    }

    public void refreshTypeface() {
        setTypeface();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(getText().toString(), (getWidth() - mStrokePaint.measureText(getText().toString())) / 2, getBaseline(), mStrokePaint);
        super.onDraw(canvas);
    }
}
