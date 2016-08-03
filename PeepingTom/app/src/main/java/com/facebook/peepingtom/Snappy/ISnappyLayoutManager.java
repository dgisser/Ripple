package com.facebook.peepingtom.Snappy;

/**
 * Created by sophiehouser on 7/18/16.
 * http://stackoverflow.com/questions/26370289/snappy-scrolling-in-recyclerview
 */
public interface ISnappyLayoutManager {

    /**
     * @param velocityX
     * @param velocityY
     * @return the resultant position from a fling of the given velocity.
     */
    int getPositionForVelocity(int velocityX, int velocityY);

    /**
     * @return the position this list must scroll to to fix a state where the
     * views are not snapped to grid.
     */
    int getFixScrollPos();

}
