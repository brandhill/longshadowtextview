package com.shamanland.longshadowtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
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
 * If you want to assign custom value, first add to following code snippet to your .xml
 * xmlns:ifTextView="http://schemas.android.com/apk/res/com.cleanmaster.security"
 * <p/>
 * then assign value, for example : "ifTextView:bgColor="@color/intl_scanresult_item_circle_fb_bg"
 * <p/>
 * we provide following custom attrs :
 * ifTextView:bgColor
 * ifTextView:strokeColor
 * ifTextView:strokeWidth
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

    // internal data
    private Bitmap mBitmap;
    private Paint mPaint;
    private Rect mTextBounds;
    private Rect mSrc;
    private RectF mDst;

    public static final float DEFAULT_TEXT_SIZE = 20;
    public static final int DEFAULT_SHADOW_COLOR = Color.DKGRAY;
    public static final int DEFAULT_TEXT_COLOR = Color.DKGRAY;

    // configurable fields
    private String mText;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private int mTextColor = DEFAULT_TEXT_COLOR;
    private int mShadowColor = DEFAULT_SHADOW_COLOR;

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
            if (mShapeType == 0) {
                bgShapeColor = a.getColor(R.styleable.IconFontTextView_bgColor, defaultBgColor);
                ShapeDrawable background = new ShapeDrawable(new OvalShape());
                background.getPaint().setColor(bgShapeColor);
                background.getPaint().setAntiAlias(true);
                this.setBackgroundDrawable(background);

            } else if (mShapeType == 1) {
                int r = DimenUtils.dp2px(5);
                float[] outerR = new float[]{r, r, r, r, r, r, r, r};

                bgShapeColor = a.getColor(R.styleable.IconFontTextView_bgColor, defaultBgColor);
                ShapeDrawable background = new ShapeDrawable(new RoundRectShape(outerR, null, null));
                background.getPaint().setColor(bgShapeColor);
                background.getPaint().setAntiAlias(true);
                this.setBackgroundDrawable(background);
            }

            setTypeface();


            mPaint = new Paint();
            mPaint.setTypeface(getTypeface());
            if (getText() != null) {
                mTextSize = getTextSize();
                mText = getText().toString();
                refresh();
            }


            StringBuilder sb = new StringBuilder();
            sb.append(", bgShape : ").append(mShapeType)
                    .append(", strokeColor : ").append(mStrokeColor)
                    .append(", strokeWidth : ").append(mStrokeWidth);
            Log.d(TAG, "IconFontTextView attrs : " + sb.toString());


        } finally {
            a.recycle();
        }

    }

    public void setBackgroundColorResource(int resID) {
        if (mShapeType >= 0) {

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


    public void refresh() {

        if (TextUtils.isEmpty(mText)) {
            return;
        }

        mSrc = new Rect();
        mDst = new RectF();

        mPaint.setAntiAlias(true);
        mPaint.setColor(mShadowColor);
        mPaint.setTextSize(mTextSize);

        if (mTextBounds == null) {
            mTextBounds = new Rect();
        }

        mPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
        Log.d(TAG, "mTextBounds.width() : " + mTextBounds.width());
        Log.d(TAG, "mTextBounds.height() : "+mTextBounds.height());

        int w = mTextBounds.width();
        int h = mTextBounds.height();

        Bitmap bitmap = Bitmap.createBitmap(w + 2 * h, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawText(mText, 0, h, mPaint);

        Rect src = new Rect();
        RectF dst = new RectF();

        src.left = 0;
        src.right = w;

        for (int i = 0; i < h; ++i) {
            src.top = i;
            src.bottom = i + 1;

            dst.left = 1;
            dst.top = i + 1;
            dst.right = 1 + w;
            dst.bottom = i + 2;

            canvas.drawBitmap(bitmap, src, dst, null);
        }

        mBitmap = bitmap;
    }


    @Override
    protected void onDraw(Canvas canvas) {


        if (getText() != null) {

            if (mTextBounds.width() <= 0 || mTextBounds.height() <= 0) {
                return;
            }

            Log.d(TAG, "canvas.getWidth() : " + canvas.getWidth());
            Log.d(TAG, "canvas.getHeight() : " + canvas.getHeight());

            float offsetX = (canvas.getWidth() - mTextBounds.width()) / 2f;
            float offsetY = (canvas.getHeight() - mTextBounds.height()) / 2f;

            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, offsetX - 10, offsetY + 10, null);

                mSrc.right = mBitmap.getWidth();
                mSrc.top = mBitmap.getHeight() - 1;
                mSrc.bottom = mBitmap.getHeight();

                for (int x = (int) offsetX - 10, y = (int) (offsetY + mBitmap.getHeight() + 10), h = canvas.getHeight(); y < h; ++x, ++y) {
                    mDst.left = x;
                    mDst.right = x + mBitmap.getWidth();
                    mDst.top = y;
                    mDst.bottom = y + 1;

                    canvas.drawBitmap(mBitmap, mSrc, mDst, null);
                }
            }

            //ignore
            mPaint.setColor(mTextColor);
            canvas.drawText(mText, offsetX, offsetY - mTextBounds.top, mPaint);
        }


        canvas.drawText(getText().toString(), (getWidth() - mStrokePaint.measureText(getText().toString())) / 2, getBaseline(), mStrokePaint);
        super.onDraw(canvas);
    }
}
