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
import android.widget.TextView;

public class IconFontLongShadowTextView extends TextView {
    private static final String TAG = IconFontLongShadowTextView.class.getSimpleName();
    public static final float DEFAULT_TEXT_SIZE = 20;
    public static final int DEFAULT_SHADOW_COLOR = Color.BLACK;
    public static final int DEFAULT_TEXT_COLOR = Color.GRAY;
    private final int defaultBgColor = Color.parseColor("#dc552c");

    // configurable fields
    private String mText;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private int mTextColor = DEFAULT_TEXT_COLOR;
    private int mShadowColor = DEFAULT_SHADOW_COLOR;

    // internal data
    private Bitmap mBitmap;
    private Paint mPaint;
    private Rect mTextBounds;
    private Rect mSrc;
    private RectF mDst;
    private Paint mTransparentPaint;

    private float mCircleMaskRadius ;


    private static final String DEFAULT_FONT = "CMS_IconFonts.ttf";

    public IconFontLongShadowTextView(Context context) {
        this(context, null, 0);
    }

    public IconFontLongShadowTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconFontLongShadowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {

        // 若不設定則透明的部分會變成黑色
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LongShadowTextView, defStyleAttr, 0);

        try {
            if (attrs != null) {

                mTextSize = (int) a.getDimension(R.styleable.LongShadowTextView_text_size, DEFAULT_TEXT_SIZE);
                mTextColor = a.getColor(R.styleable.LongShadowTextView_text_color, DEFAULT_TEXT_COLOR);
                mShadowColor = a.getColor(R.styleable.LongShadowTextView_shadow_color, DEFAULT_SHADOW_COLOR);
                mText = a.getString(R.styleable.LongShadowTextView_text);
            }

            mCircleMaskRadius = 60f;
            setTypeface();

            initFontShadow();

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

    public String getText() {
        return mText;
    }

    public void setText(String value) {
        boolean changed = mText == null && value != null || mText != null && !mText.equals(value);

        mText = value;

        if (changed) {
            initFontShadow();
        }
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float value) {
        boolean changed = mTextSize != value;

        mTextSize = value;

        if (changed) {
            initFontShadow();
        }
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int value) {
        boolean changed = mTextColor != value;

        mTextColor = value;

        if (changed) {
            initFontShadow();
        }
    }

    public int getShadowColor() {
        return mShadowColor;
    }

    public void setShadowColor(int value) {
        boolean changed = mShadowColor != value;

        mShadowColor = value;

        if (changed) {
            initFontShadow();
        }
    }

    public void initFontShadow() {
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

        canvas.drawText(mText, 0, mTextBounds.height() +12, mPaint);

        Rect src = new Rect();
        RectF dst = new RectF();

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        src.left = 0;
        src.right = w;

        for (int i = 0; i < h; i++) {
            src.top = i;
            src.bottom = i + 1;

            dst.left = 1;
            dst.top = i + 1;
            dst.right = 1 + w;
            dst.bottom = i + 2;

            canvas.drawBitmap(bitmap, src, dst, null);
        }

        mBitmap = bitmap;

        if (mTransparentPaint == null) {
            mTransparentPaint = new Paint();
            mTransparentPaint.setColor(Color.WHITE);
            mTransparentPaint.setXfermode(null);
            mTransparentPaint.setAntiAlias(true);
        }

        int size = 0;
        if(mTextBounds.width() > mTextBounds.height()){
            size = mTextBounds.width();
        }else{
            size = mTextBounds.height();
        }

        maskBitmap = Bitmap.createBitmap(size * 4, size * 4, Bitmap.Config.ARGB_8888);
        maskCanvas = new Canvas(maskBitmap);
        maskCanvas.drawColor(Color.TRANSPARENT);
        maskCanvas.drawCircle(size, size, size, mTransparentPaint);

    }

    Canvas maskCanvas ;
    Bitmap maskBitmap;

    @Override
    public void onDraw(Canvas canvas) {
        if (mText == null) {
            return;
        }

        float offsetX = (canvas.getWidth() - mTextBounds.width()) / 2f;
        float offsetY = (canvas.getHeight() - mTextBounds.height()) / 2f;

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
        canvas.drawBitmap(maskBitmap, canvas.getWidth() / 2  - mTextBounds.width() / 2, canvas.getHeight() / 2 -mTextBounds.height(), mTransparentPaint);

    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
}
