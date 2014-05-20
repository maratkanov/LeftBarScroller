package com.example.appScroller;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;


public class MyViewGroup extends ViewGroup {

    private int viewWidth;
    private int viewHeight;
    private Handler handler;
    private Scroller scroller;

    private static final int SLICE_LEFT = 0;
    private static final int SLICE_RIGHT = 1;

    private static final int LEFT_VIEW = 1;
    private static final int RIGHT_VIEW = 0;

    public MyViewGroup(Context context) {
        this(context, null);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        View leftView = new View(getContext());
        leftView.setBackgroundResource(R.drawable.two);
        View centerView = new View(getContext());
        centerView.setBackgroundResource(R.drawable.one);

        addView(centerView);
        addView(leftView);

        scroller = new Scroller(getContext());  // TODO: possibly here
        handler = new MyHandler(scroller, leftView);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChild(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(viewWidth, viewHeight);
    }

    private void measureChild(int screenWidth, int screenHeight) {
        for (int i=0; i<getChildCount(); i++) {
            View currentView = getChildAt(i);
            currentView.measure(screenWidth, screenHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        getChildAt(LEFT_VIEW).layout(-viewWidth, 0, 0, viewHeight);  //  left view not visible at first
        getChildAt(RIGHT_VIEW).layout(0, 0, viewWidth, viewHeight);  // center view visible at first
    }

    // handle touch events

    private float oldX;
    private float oldY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldX = x;
                oldY = y;
                return false;
            case MotionEvent.ACTION_MOVE:
                float dX = x - oldX;
                float dY = y - oldY;
                if ( dX > 0 && dX >= ViewConfiguration.get(getContext()).getScaledTouchSlop() && (Math.abs(dX) - Math.abs(dY) > 0)) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        int dX = Math.round(x - oldX);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                if (dX < viewWidth/2) {
                    if (Math.abs(dX) <= viewWidth / 4) {
                        scroller.startScroll(dX, 0, -dX, 0);
                        handler.sendEmptyMessage(SLICE_LEFT);
                    }
                    if (dX > viewWidth / 4) {
                        scroller.startScroll(dX, 0, viewWidth / 2 - dX, 0);
                        handler.sendEmptyMessage(SLICE_RIGHT);
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dX < viewWidth/2) {
                    getChildAt(LEFT_VIEW).layout(-viewWidth + dX, 0, dX, viewHeight);
//                    getChildAt(RIGHT_VIEW).layout(dX, 0, viewWidth, viewHeight);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private static class MyHandler extends Handler {
        private Scroller scroller;
        private View view;

        private MyHandler(Scroller scroller, View view) {
            this.scroller = scroller;
            this.view = view;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SLICE_LEFT:
                    if (scroller.computeScrollOffset()) {
                        int x = scroller.getCurrX();
                        view.offsetLeftAndRight(-(view.getRight() - x));
                        sendEmptyMessage(SLICE_LEFT);
                    }
                    break;
                case SLICE_RIGHT:
                    if (scroller.computeScrollOffset()) {
                        int x = scroller.getCurrX();
                        view.offsetLeftAndRight(x - view.getRight());
                        sendEmptyMessage(SLICE_RIGHT);
                    }
                    break;
            }
        }
    }
}
