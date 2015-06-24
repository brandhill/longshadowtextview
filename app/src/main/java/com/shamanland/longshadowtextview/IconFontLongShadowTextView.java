package com.shamanland.longshadowtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class IconFontLongShadowTextView extends TextView {
    private static final String TAG = IconFontLongShadowTextView.class.getSimpleName();
    public static final float DEFAULT_TEXT_SIZE = 20;
    public static final float DEFAULT_CLIP_SIZE = 20;
    public static final int DEFAULT_SHADOW_COLOR = Color.BLACK;
    public static final int DEFAULT_TEXT_COLOR = Color.GRAY;

    // configurable fields
    private String mText;
    private float mTextSize ;
    private int mTextColor = DEFAULT_TEXT_COLOR;
    private int mShadowColor = DEFAULT_SHADOW_COLOR;

    // internal data
    private Bitmap mBitmap;
    private Paint mPaint;
    private Rect mTextBounds;
    private Rect mSrc;
    private RectF mDst;
    private Paint mTransparentPaint;

    private float mClipRadius;
    Canvas mMaskCanvas;
    Bitmap mMaskBitmap;


    private static final String DEFAULT_FONT = "CMS_IconFonts.ttf";

    public IconFontLongShadowTextView(Context context) {
        this(context, null, 0);
    }

    public IconFontLongShadowTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconFontLongShadowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 若不設定則透明的部分會變成黑色
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LongShadowTextView, defStyleAttr, 0);

        try {

            mTextSize = getTextSize();
            mTextColor = getCurrentTextColor();
            mShadowColor = a.getColor(R.styleable.LongShadowTextView_shadow_color, DEFAULT_SHADOW_COLOR);
            mText = a.getString(R.styleable.LongShadowTextView_text);
            mClipRadius = a.getDimension(R.styleable.LongShadowTextView_clipRadius, DEFAULT_CLIP_SIZE);

            Log.d(TAG, "mClipRadius : "+mClipRadius);

            setTypeface();

            prepareFontShadow();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a.recycle();
        }

    }


    private void setTypeface() {
        if (!TextUtils.isEmpty(DEFAULT_FONT)) {
            try {
                Typeface font = FontUitls.getFont(getContext(), DEFAULT_FONT);
                if (font != null) {
                    setTypeface(font);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void setText(String value) {
        boolean changed = mText == null && value != null || mText != null && !mText.equals(value);

        mText = value;

        if (changed) {
            prepareFontShadow();
        }
    }

    public void setTextSize(float value) {
        boolean changed = mTextSize != value;

        mTextSize = value;

        if (changed) {
            prepareFontShadow();
        }
    }


    public void setTextColor(int value) {
        boolean changed = mTextColor != value;

        mTextColor = value;

        if (changed) {
            prepareFontShadow();
        }
    }



    public void setShadowColor(int value) {
        boolean changed = mShadowColor != value;

        mShadowColor = value;

        if (changed) {
            prepareFontShadow();
        }
    }

    public void prepareFontShadow() {
        if (mText == null) {
            return;
        }

        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);

            mSrc = new Rect();
            mDst = new RectF();
        }

        mPaint.setTypeface(getTypeface());
        mPaint.setColor(mShadowColor);
        mPaint.setTextSize(mTextSize);

        if (mTextBounds == null) {
            mTextBounds = new Rect();
        }

        mPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);

        Bitmap bitmap = Bitmap.createBitmap(mTextBounds.width() + 2 * mTextBounds.height(), mTextBounds.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawText(mText, 0, mTextBounds.height() * 1.1f, mPaint);

        Log.d(TAG, "mTextBounds.width() : " + mTextBounds.width());
        Log.d(TAG, "mTextBounds.height() : " + mTextBounds.height());

        Rect src = new Rect();
        RectF dst = new RectF();

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        src.left = 0;
        src.right = w;

        for (int i = 0; i < h; i++) {
            src.top = i;
            src.bottom = i + 1;

            dst.top = i + 1;
            dst.bottom = i + 2;
            dst.left = 1;
            dst.right = 1 + w;

            canvas.drawBitmap(bitmap, src, dst, null);
        }

        mBitmap = bitmap;

        if (mTransparentPaint == null) {
            mTransparentPaint = new Paint();
            mTransparentPaint.setColor(Color.WHITE);
            mTransparentPaint.setXfermode(null);
            mTransparentPaint.setAntiAlias(true);
        }


        // init circle clipping
        int size = 0;
        if (mTextBounds.width() > mTextBounds.height()) {
            size = mTextBounds.width();
        } else {
            size = mTextBounds.height();
        }

        // TODO refine bitmap size
        size = (int) mClipRadius * 4;

        mMaskBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        mMaskCanvas = new Canvas(mMaskBitmap);
        mMaskCanvas.drawColor(Color.TRANSPARENT);
        mMaskCanvas.drawCircle(mClipRadius, mClipRadius, mClipRadius, mTransparentPaint);

    }


    @Override
    public void onDraw(Canvas canvas) {
        if (mText == null) {
            return;
        }

        float offsetX = (canvas.getWidth() - mTextBounds.width()) / 2f;
        float offsetY = (canvas.getHeight() - mTextBounds.height()) / 2f;


        Log.d(TAG, "canvas.getWidth() : " + canvas.getWidth());
        Log.d(TAG, "canvas.getHeight() : " + canvas.getHeight());

        if (mBitmap != null) {
            // 上方陰影
            canvas.drawBitmap(mBitmap, offsetX, offsetY, null);

            mSrc.right = mBitmap.getWidth();
            mSrc.top = mBitmap.getHeight() - 1;
            mSrc.bottom = mBitmap.getHeight();

            // 下方陰影
            for (int x = (int) offsetX + 1, y = (int) (offsetY + mBitmap.getHeight()), h = canvas.getHeight(); y < h; ++x, ++y) {
                mDst.left = x;
                mDst.right = x + mBitmap.getWidth();
                mDst.top = y;
                mDst.bottom = y + 1;

                canvas.drawBitmap(mBitmap, mSrc, mDst, null);
            }
        }

        mPaint.setColor(mTextColor);
        canvas.drawText(mText, offsetX, offsetY - mTextBounds.top, mPaint);


        // circle clipping
        mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(mMaskBitmap, canvas.getWidth() / 2 - mTextBounds.width() / 2, canvas.getHeight() / 2 - mTextBounds.height(), mTransparentPaint);

    }


}
