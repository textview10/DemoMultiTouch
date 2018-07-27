package com.demo.demomultitouch.bean;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by xu.wang
 * Date on 2017/1/19 15:00
 * 绘画的每一条直线的对象
 */
public class LineInfo implements Parcelable {
    private int color;
    private int strokeWidth = 8;
    private int index;  //正在绘制的第一条线
    private ArrayList<PointF> pointLists = new ArrayList<>();

    public LineInfo() {
    }

    protected LineInfo(Parcel in) {
        color = in.readInt();
        strokeWidth = in.readInt();
        index = in.readInt();
        pointLists = in.createTypedArrayList(PointF.CREATOR);
    }

    public static final Creator<LineInfo> CREATOR = new Creator<LineInfo>() {
        @Override
        public LineInfo createFromParcel(Parcel in) {
            return new LineInfo(in);
        }

        @Override
        public LineInfo[] newArray(int size) {
            return new LineInfo[size];
        }
    };

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ArrayList<PointF> getPointLists() {
        return pointLists;
    }

    public void setPointLists(ArrayList<PointF> pointLists) {
        this.pointLists = pointLists;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(color);
        dest.writeInt(strokeWidth);
        dest.writeInt(index);
        dest.writeTypedList(pointLists);
    }
}
