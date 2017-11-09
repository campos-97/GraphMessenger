package project.datos.tec.graphmessanger.gui.custom.swipapleviews;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by josea on 10/17/2016.
 */

public class CustomViewPager extends ViewPager {

    private boolean swipable;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.swipable = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.swipable) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.swipable) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.swipable = enabled;
    }
}
