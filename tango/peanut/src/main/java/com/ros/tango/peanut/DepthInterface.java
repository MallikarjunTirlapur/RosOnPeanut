package com.ros.tango.peanut;

import org.ros.message.Time;

/**
 * Created by mallik on 6/13/14.
 */
public interface DepthInterface {
    void onNewDepthImage(short[] data, int width, int height,Time currentTime);
}
