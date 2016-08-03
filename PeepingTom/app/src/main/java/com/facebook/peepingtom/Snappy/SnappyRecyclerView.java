package com.facebook.peepingtom.Snappy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.facebook.peepingtom.Adapters.FeedRecyclerAdapter;

/**
 * Created by sophiehouser on 7/18/16.
 */
public class SnappyRecyclerView extends RecyclerView {

    int savedPostion = 0;

    public SnappyRecyclerView(Context context) {
        super(context);
    }

    public SnappyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SnappyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // interface to find current position you're scrolled to
    public interface ScrollPosition {
        public void scrollPositionListener(int position);
    }

    public FeedRecyclerAdapter.FeedViewHolder feedViewHolder;

    // makes sure the scroll lands on a specific item and changes the nav dot colors accordingly
    @Override
    public boolean fling(int velocityX, int velocityY) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        // views on the screen
        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
        View lastView = linearLayoutManager.findViewByPosition(lastVisibleItemPosition);
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        View firstView = linearLayoutManager.findViewByPosition(firstVisibleItemPosition);

        // distance we need to scroll
        int leftEdge = lastView.getLeft();
        int rightEdge = firstView.getRight();
        if (Math.abs(velocityX) < 1000) {
            // The fling is slow -> stay at the current page if we are less than half through,
            // or go to the next page if more than half through
            if (leftEdge > screenWidth / 2) {
                // go to next page
                savedPostion = firstVisibleItemPosition;
            } else if (rightEdge < screenWidth / 2)
                // go to next page
                savedPostion = lastVisibleItemPosition;
            else {
                // stay at current page
                if (velocityX > 0) savedPostion = firstVisibleItemPosition;
                else savedPostion = lastVisibleItemPosition;
            }
        } else {
            // The fling is fast -> go to next page
            if (velocityX > 0) savedPostion = lastVisibleItemPosition;
            else savedPostion = firstVisibleItemPosition;
        }
        changeBackground(savedPostion);
        // passing the feed view holder the position of the story you flung to
        feedViewHolder.scrollPositionListener(savedPostion);
        super.smoothScrollToPosition(savedPostion);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // We want the parent to handle all touch events--there's a lot going on there,
        // and there is no reason to overwrite that functionality--bad things will happen.
        final boolean ret = super.onTouchEvent(e);
        final RecyclerView.LayoutManager lm = getLayoutManager();

        if (lm instanceof ISnappyLayoutManager
                && (e.getAction() == MotionEvent.ACTION_UP ||
                e.getAction() == MotionEvent.ACTION_CANCEL)
                && getScrollState() == SCROLL_STATE_IDLE) {
            // The layout manager is a SnappyLayoutManager, which means that the
            // children should be snapped to a grid at the end of a drag or
            // fling. The motion event is either a user lifting their finger or
            // the cancellation of a motion events, so this is the time to take
            // over the scrolling to perform our own functionality.
            // Finally, the scroll state is idle--meaning that the resultant
            // velocity after the user's gesture was below the threshold, and
            // no fling was performed, so the view may be in an unaligned state
            // and will not be flung to a proper state.
            smoothScrollToPosition(((ISnappyLayoutManager) lm).getFixScrollPos());
        }

        return ret;
    }

    // makes background color of rv white if the story position is greater than 0
    // hides question in the background of feed while you scroll through stories
    public void changeBackground(int position){
        if (position > 0) {
            setBackgroundColor(Color.WHITE);
        } else {
            setBackgroundColor(Color.TRANSPARENT);
        }
    }
}


