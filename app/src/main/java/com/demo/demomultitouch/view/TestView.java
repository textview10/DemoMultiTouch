package com.demo.demomultitouch.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;

import com.demo.demomultitouch.bean.LineInfo;
import com.demo.demomultitouch.util.PaintMathUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by xu.wang
 * Date on  2018/7/27 14:52:04.
 *
 * @Desc
 */

public class TestView extends TextureView implements TextureView.SurfaceTextureListener, Runnable {
    private final static String TAG = "TestView";
    private boolean mLoop = false;
    private int mColor = Color.WHITE;
    private ArrayList<LineInfo> mLists;
    private ArrayList<LineInfo> mMoveingList;   //正在移动绘制的线条的触摸事件Index ,第一笔,就记录在第一个位置,第二笔记录在第二个位置

    public TestView(Context context) {
        this(context, null);
    }

    public TestView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialView();
    }

    private void initialView() {
        this.setSurfaceTextureListener(this);
        mLists = new ArrayList<>();
        mMoveingList = new ArrayList<>();
        Collections.synchronizedList(mLists);
        Collections.synchronizedList(mMoveingList);
    }

    //      event.getPointerCount()：触控点的个数
//    getPointerId(int pointerIndex)：pointerIndex从0到getPointerCount-1,返回一个触摸点的标示
//    getX(int pointerIndex)：通过标示来得到X坐标
//    getY(int pointerIndex)：通过标示来得到Y坐标
//    MotionEvent.ACTION_POINTER_1_DOWN：第一个触摸点点击事件
//    MotionEvent.ACTION_POINTER_2_DOWN：第二个触摸点点击事件
//    MotionEvent.ACTION_POINTER_1_UP：第一个触摸点松开事件
//    MotionEvent.ACTION_POINTER_2_UP：第二个触摸点松开事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN://第一个触控点按下
                addLineInfo(0, Color.RED, pointerCount, event.getX(0), event.getY(0));
                break;
            case MotionEvent.ACTION_POINTER_DOWN://第一个之后的触控点按下
                addLineInfo(pointerCount, Color.GREEN, pointerCount, event.getX(pointerCount - 1), event.getY(pointerCount - 1));
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerCount > 1) {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < pointerCount; i++) {
                        sb.append("第" + i + "个点" + "x = " + event.getX(i) + " y = " + event.getY(i) +
                                "actionId = " + event.getActionIndex()
                                + " id = " + event.getPointerId(i)).append("   ");
                    }
                    Log.e(TAG, "" + sb.toString());
                }
                for (int i = 0; i < pointerCount; i++) {
                    if (mMoveingList.size() < i) continue;  //异常
                    LineInfo lineInfo = mMoveingList.get(i);
                    if (lineInfo == null || lineInfo.getPointLists() == null) continue;    //异常
                    lineInfo.getPointLists().add(new PointF(event.getX(i), event.getY(i)));
                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_UP:
                mLists.addAll(mMoveingList);
                mMoveingList.clear();
                break;
            case MotionEvent.ACTION_CANCEL:
                return true;
        }
        return true;
    }

    /**
     * 增加记录线条
     *
     * @param pos
     * @param color
     * @param pointerCount
     * @param x
     * @param y
     */
    private void addLineInfo(int pos, int color, int pointerCount, float x, float y) {
        LineInfo lineInfo = new LineInfo();
        lineInfo.setColor(color);
        lineInfo.setStrokeWidth(6);
        lineInfo.setIndex(pointerCount);//记录是第几条线
        ArrayList<PointF> tempMore = new ArrayList<>();
        tempMore.add(new PointF(x, y));
        lineInfo.setPointLists(tempMore);
        if (pos < mMoveingList.size()) {
            mMoveingList.set(pos, lineInfo);
        } else {
            mMoveingList.add(lineInfo);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mLoop = true;
        new Thread(this).start();
        drawCanvas();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mLoop = false;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void drawCanvas() {
        Canvas canvas = lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "canvas == null");
            return;
        }
        canvas.drawColor(mColor);
        drawLineInfoList(canvas, mLists);    //绘制历史线条
        drawLineInfoList(canvas, mMoveingList);   //绘制正在移动的线条
        unlockCanvasAndPost(canvas);
    }

    private void drawLineInfoList(Canvas canvas, ArrayList<LineInfo> lists) {
        for (int i = 0; i < lists.size(); i++) {
            LineInfo lineInfo = lists.get(i);
            Path path = new Path();
            Paint paint = new Paint();
            paint.setColor(lineInfo.getColor());
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(lineInfo.getStrokeWidth());
            for (int j = 0; j < lineInfo.getPointLists().size(); j++) {
                if (j == 0) {
                    PointF pointF = lineInfo.getPointLists().get(j);
                    path.moveTo(pointF.x, pointF.y);
                } else {
                    PointF prePointF = lineInfo.getPointLists().get(j - 1);
                    PointF pointF = lineInfo.getPointLists().get(j);
                    PointF besPoint = PaintMathUtils.getBesPoint(prePointF, pointF);
                    path.quadTo(prePointF.x, prePointF.y, besPoint.x, besPoint.y);
//                    path.lineTo(pointF.x, pointF.y);
                }
            }
            canvas.drawPath(path, paint);
        }
    }


    @Override
    public void run() {
        while (mLoop) {
            long start = System.currentTimeMillis();
            drawCanvas();
            long end = System.currentTimeMillis();
//            Log.e(TAG, "绘制时间" + (end - start));
            if (end - start < 50) {
                SystemClock.sleep(50 - (end - start));
            }
        }
    }
}
