package com.ros.tango.peanut;

import org.ros.message.Time;

/**
 * Created by mallik on 8/12/14.
 */
public interface RGBInterface {
    void onNewRawImage(byte[] data, int width, int height, Time currentTime);
    void onNewCord(short x, short y,short leftTopX,short leftTopY, short leftBotX, short leftBotY,
                   short rightTopX, short rightTopY,short rightBotX, short rightBotY );
    void onNewCommand(String cmnd);
    void onPlayCommand(Boolean data);
}
