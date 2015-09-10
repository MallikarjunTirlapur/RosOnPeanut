package com.ros.tango.peanut;


/**
 * Created by mallik on 11/8/14.
 */
public class Superframe {

    public final static int SF_SIZE = 1752;

    // The Superframe's width is 1280 pixels, where each pixel is a byte.
    public final static int SF_WIDTH = 1280;
    // The Superframe's height is 1168 pixels (height can be thought of as
    //   pixels or lines).
    public final static int SF_HEIGHT = 1168;
    // Header data is stored in first 16 lines of a Superframe.
    public final static int SF_LINES_HEADER = 16;
    // A Y plane of wide angle lens (Y contains the luminance of a YUV image)
    // is stored in next 240 lines.
    public final static int SF_LINES_SMALLIMAGE = 240;
    //Mallik
    public final static int SF_START_INDEX_SMALLIMAGEY = 16* SF_WIDTH;

    // An Image pyramid is stored in next 96 lines, currently a placeholder.
    public final static int SF_LINES_PYRAMID = 96;
    // A Depth buffer is stored in next 96 lines.
    public final static int SF_LINES_DEPTH = 96;
    // The Y plane of the 4 MP standard field of view camera YUV image
    //   is stored in next 720 lines.
    public final static int SF_LINES_BIGIMAGE = 720;



    public final static int SF_START_LINE_DEPTH = SF_LINES_HEADER
            + SF_LINES_SMALLIMAGE + SF_LINES_PYRAMID;
    public final static int SF_START_INDEX_DEPTH = SF_START_LINE_DEPTH
            * SF_WIDTH;


    public final static int SF_START_LINE_BIGIMAGEY = SF_START_LINE_DEPTH
            + SF_LINES_DEPTH;
    //Mallik
    public final static int SF_START_INDEX_BIGIMAGEY = SF_START_LINE_BIGIMAGEY
            * SF_WIDTH;

    // Size of the Y portion of the 4 MP standard field YUV image.
    public final static int SF_BIG_SIZEY = SF_WIDTH * SF_LINES_BIGIMAGE;
    // Number of bytes in YUV bitmap.
    public final static int SF_BIG_SIZEYUV = SF_BIG_SIZEY + (SF_BIG_SIZEY / 2);

    // The depth buffer is contained in a Superframe as a 320x180 array
    //   of 16 bit (2 contiguous bytes) values.
    public final static int DB_WIDTH = 320;
    public final static int DB_HEIGHT = 180;
    // DB_SIZE is the number of elements, not the number of bytes, in the depth
    //   buffer.
    // An element can be though of as a pixel, where the value is the depth.
    // A single element is a 2 byte int in the Superframe.
    public final static int DB_SIZE = DB_WIDTH * DB_HEIGHT;


    //Mallik, for color images
    public final static int RGB_WIDTH = 1280;
    public final static int RGB_HEIGHT = 720;

    public final static int RGB_SIZE = RGB_WIDTH * RGB_HEIGHT;

    public final static int SMALL_WIDTH = 640;
    public final static int SMALL_HEIGHT = 480;

    public final static int SMALL_SIZE = SMALL_WIDTH * SMALL_HEIGHT;

    public final static int YUV_HEIGHT = RGB_HEIGHT+(RGB_HEIGHT/2);
    public final static int YUV_WIDTH = RGB_WIDTH;
    public final static int YUV_SIZE = RGB_SIZE+(RGB_SIZE/2);

    public final static int rgbBigArray_skipStartIndex = 1495041;
    public final static int rgbBigArray_skipEndIndex = 1781759;

}