package com.xw.sample.dashboardviewdemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * DashboardView style 1
 * Created by woxingxiao on 2016-11-19.
 */

public class DashboardView1 extends View {

    private int mRadius; // 扇形半径
    private int mStartAngle = 180; // 起始角度
    private int mSweepAngle = 180; // 绘制角度
    private int mMin = 0; // 最小值
    private int mMax = 100; // 最大值
    private int mSection = 10; // 值域（mMax-mMin）等分份数
    private int mPortion = 10; // 一个mSection等分份数
    private String mHeaderText = "℃"; // 表头
    private int mRealTimeValue = mMin; // 实时读数
    private boolean isShowValue = true; // 是否显示实时读数
    private int mStrokeWidth; // 画笔宽度
    private int mLength1; // 长刻度的相对圆弧的长度
    private int mLength2; // 刻度读数顶部的相对圆弧的长度
    private int mPLRadius; // 指针长半径
    private int mPSRadius; // 指针短半径

    private float mCenterX, mCenterY; // 圆心坐标
    private Paint mPaint;
    private PorterDuffXfermode mPDXfermode_SRC_OVER; // 画笔重合覆盖
    private PorterDuffXfermode mPDXfermode_XOR; // 画笔重合镂空
    private RectF mRectFArc;
    private Path mPath;
    private RectF mRectFInnerArc;
    private Rect mRectText;
    private String[] mTexts;

    public DashboardView1(Context context) {
        this(context, null);
    }

    public DashboardView1(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashboardView1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mStrokeWidth = dp2px(1);
        mLength1 = dp2px(10) + mStrokeWidth;
        mLength2 = dp2px(14) + mStrokeWidth;
        mPSRadius = dp2px(10);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mPDXfermode_SRC_OVER = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
        mPDXfermode_XOR = new PorterDuffXfermode(PorterDuff.Mode.XOR);

        mRectFArc = new RectF();
        mPath = new Path();
        mRectFInnerArc = new RectF();
        mRectText = new Rect();

        mTexts = new String[mSection + 1]; // 需要显示mSection + 1个刻度读数
        for (int i = 0; i < mTexts.length; i++) {
            int n = (mMax - mMin) / mSection;
            mTexts[i] = String.valueOf(i * n);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = resolveSize(dp2px(200), widthMeasureSpec);
        mRadius = (width - getPaddingLeft() - getPaddingRight() - mStrokeWidth * 2) / 2;

        mPaint.setTextSize(sp2px(16));
        if (isShowValue) { // 显示实时读数，View高度增加字体高度3倍
            mPaint.getTextBounds("x", 0, "x".length(), mRectText);
        } else {
            mPaint.getTextBounds("x", 0, 0, mRectText);
        }
        // View高度由宽度自动确定
        int height = mRadius + mStrokeWidth * 2 + mPSRadius + mRectText.height() * 3 +
                getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(width, height);

        mCenterX = mCenterY = getWidth() / 2f;
        mRectFArc.set(
                getPaddingLeft() + mStrokeWidth,
                getPaddingTop() + mStrokeWidth,
                getWidth() - getPaddingRight() - mStrokeWidth,
                getWidth() - getPaddingBottom() - mStrokeWidth
        );

        mPaint.setTextSize(sp2px(10));
        mPaint.getTextBounds("x", 0, "x".length(), mRectText);
        mRectFInnerArc.set(
                getPaddingLeft() + mLength2 + mRectText.height(),
                getPaddingTop() + mLength2 + mRectText.height(),
                getWidth() - getPaddingRight() - mLength2 - mRectText.height(),
                getWidth() - getPaddingBottom() - mLength2 - mRectText.height()
        );

        mPLRadius = mRadius - (mLength2 + mRectText.height() + dp2px(5));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 画圆弧
         */
        mPaint.setXfermode(mPDXfermode_SRC_OVER);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(Color.WHITE);
        canvas.drawArc(mRectFArc, mStartAngle, mSweepAngle, false, mPaint);

        /**
         * 画长刻度
         * 画好起始角度的一条刻度后通过canvas绕着原点旋转来画剩下的长刻度
         */
        double cos = Math.cos(Math.toRadians(mStartAngle - 180));
        double sin = Math.sin(Math.toRadians(mStartAngle - 180));
        float x0 = (float) (mRadius * (1 - cos) + mStrokeWidth + mStrokeWidth / 2f);
        float y0 = (float) (mRadius * (1 - sin) + mStrokeWidth);
        float x1 = (float) (mRadius - (mRadius - mLength1) * cos + mStrokeWidth);
        float y1 = (float) (mRadius - (mRadius - mLength1) * sin + mStrokeWidth);

        canvas.save();
        canvas.drawLine(x0, y0, x1, y1, mPaint);
        float angle = mSweepAngle * 1f / mSection;
        for (int i = 0; i < mSection; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            canvas.drawLine(x0, y0, x1, y1, mPaint);
        }
        canvas.restore();

        /**
         * 画短刻度
         * 同样采用canvas的旋转原理
         */
        canvas.save();
        mPaint.setStrokeWidth(1);
        float x2 = (float) (mRadius - (mRadius - mLength1 / 2f) * cos + mStrokeWidth + mStrokeWidth / 2f);
        float y2 = (float) (mRadius - (mRadius - mLength1 / 2f) * sin + mStrokeWidth);
        canvas.drawLine(x0, y0, x2, y2, mPaint);
        angle = mSweepAngle * 1f / (mSection * mPortion);
        for (int i = 1; i < mSection * mPortion; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            if (i % mSection == 0) { // 避免与长刻度画重合
                continue;
            }
            canvas.drawLine(x0, y0, x2, y2, mPaint);
        }
        canvas.restore();

        /**
         * 画长刻度读数
         * 添加一个圆弧path，文字沿着path绘制
         */
        mPaint.setTextSize(sp2px(10));
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < mTexts.length; i++) {
            mPaint.getTextBounds(mTexts[i], 0, mTexts[i].length(), mRectText);
            // 粗略把文字的宽度视为圆心角2*θ对应的弧长，利用弧长公式得到θ，下面用于修正角度
            float θ = (float) (180 * mRectText.width() / 2 /
                    (Math.PI * (mRadius - mLength2 - mRectText.height())));

            mPath.reset();
            mPath.addArc(
                    mRectFInnerArc,
                    mStartAngle + i * (mSweepAngle / mSection) - θ, // 正起始角度减去θ使文字居中对准长刻度
                    mSweepAngle
            );
            canvas.drawTextOnPath(mTexts[i], mPath, 0, 0, mPaint);
        }

        /**
         * 画表头
         * 没有表头就不画
         */
        if (!TextUtils.isEmpty(mHeaderText)) {
            mPaint.setTextSize(sp2px(14));
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.getTextBounds(mHeaderText, 0, mHeaderText.length(), mRectText);
            canvas.drawText(mHeaderText, mCenterX, mCenterY / 2f + mRectText.height(), mPaint);
        }

        /**
         * 画指针
         */
        float θ = mStartAngle + mSweepAngle * (mRealTimeValue - mMin) / (mMax - mMin); // 指针与水平线夹角
        int d = dp2px(5); // 指针由两个等腰三角形构成，d为共底边长的一半
        mPath.reset();
        float[] p1 = getCoordinatePoint(d, θ - 90);
        mPath.moveTo(p1[0], p1[1]);
        float[] p2 = getCoordinatePoint(mPLRadius, θ);
        mPath.lineTo(p2[0], p2[1]);
        float[] p3 = getCoordinatePoint(d, θ + 90);
        mPath.lineTo(p3[0], p3[1]);
        float[] p4 = getCoordinatePoint(mPSRadius, θ - 180);
        mPath.lineTo(p4[0], p4[1]);
        mPath.close();
        canvas.drawPath(mPath, mPaint);

        /**
         * 画指针围绕的镂空圆心
         */
        mPaint.setXfermode(mPDXfermode_XOR);
        canvas.drawCircle(mCenterX, mCenterY, dp2px(2), mPaint);

        /**
         * 画实时度数值
         */
        if (isShowValue) {
            mPaint.setXfermode(mPDXfermode_SRC_OVER);
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setTextSize(sp2px(16));
            String value = String.valueOf(mRealTimeValue);
            mPaint.getTextBounds(value, 0, value.length(), mRectText);
            canvas.drawText(value, mCenterX, mCenterY + mPSRadius + mRectText.height() * 2, mPaint);
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }

    public float[] getCoordinatePoint(int radius, float angle) {
        float[] point = new float[2];

        double arcAngle = Math.toRadians(angle); //将角度转换为弧度
        if (angle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (angle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }

    public int getRealTimeValue() {
        return mRealTimeValue;
    }

    public void setRealTimeValue(int realTimeValue) {
        if (mRealTimeValue == realTimeValue || realTimeValue < mMin || realTimeValue > mMax) {
            return;
        }

        mRealTimeValue = realTimeValue;
        postInvalidate();
    }
}
