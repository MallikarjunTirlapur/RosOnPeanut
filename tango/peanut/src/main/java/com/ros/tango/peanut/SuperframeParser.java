package com.ros.tango.peanut;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.ros.message.Time;
import org.ros.node.ConnectedNode;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by mallik on 5/12/14.
 */

public class SuperframeParser extends SurfaceView implements Callback,
        PreviewCallback, View.OnTouchListener {

    private double[] D = {0.2104473, -0.5854902, 0.4575633, 0.0, 0.0};
    private double[] K = {236.546, 0.0, 158.472, 0.0, 236.637, 88.3187, 0.0, 0.0, 1.0};
    private double[] R = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    private double[] P = {234.941, 0.0, 157.622, 0.0, 0.0, 234.877, 88.2387, 0.0, 0.0, 0.0, 1.0, 0.0};

    /////////////////////////////////////////Touch Variables///////////////////////////////////////
    private Drawable mLeftTopIcon;
    private Drawable mRightTopIcon;
    private Drawable mLeftBottomIcon;
    private Drawable mRightBottomIcon;

    private boolean mLeftTopBool = false;
    private boolean mRightTopBool = false;
    private boolean mLeftBottomBool = false;
    private boolean mRightBottomBool = false;

    // Starting positions of the bounding box

    private float mLeftTopPosX = 565;
    private float mLeftTopPosY = 260;

    private float mRightTopPosX = 715;
    private float mRightTopPosY = 260;

    private float mLeftBottomPosX = 565;
    private float mLeftBottomPosY = 460;

    private float mRightBottomPosX = 715;
    private float mRightBottomPosY = 460;
    private float mPosX;
    private float mPosY;

    private float mLastTouchX;
    private float mLastTouchY;

    private Paint topLine;
    private Paint bottomLine;
    private Paint leftLine;
    private Paint rightLine;

    private Rect buttonRec;

    private int mCenter;

    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    // you can ignore this
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
/////////////////////////////////////////////////////////////////////////////////////////////////////

    // Camera and surfaces.
    private Camera mCamera = null;
    private SurfaceHolder mSurfaceHolder = null;
    Camera.Parameters parameters;
    // This bitmap has color data, color is based on depth buffer values.
    private int[] mDepthColorPixels = null;

    private ConnectedNode rgb_connectedNode;
    private ConnectedNode depth_connectedNode;

    private DepthInterface rawImageListener;
    private RGBInterface rawRgbListener;


    private short[] rawDepthData = new short[57600];
    private byte[] rawRgbData = new byte[1280 * 1304];
    private int[] rawRgbBigData = new int[1280 * 1304];
    private byte[] rawSmallData = new byte[640 * 480];
    private Paint pnt = new Paint();
    private Paint pntX = new Paint();
    private Paint pntY = new Paint();
    private Paint pntX1 = new Paint();
    private Paint pntY1 = new Paint();
    private Paint pntText = new Paint();
    private boolean skip_frame = false;
    private byte[] fromBmp = new byte[320 * 180];
    private Canvas cnv = new Canvas();

    private SensorManager sensorManager;
    private Sensor accelSensor;
    private Sensor gyroSensor;
    private Sensor quatSensor;


    private RosSurface mDv;
    private TextView ltv1, ltv2;
    private Activity lact;
    private Button lpick_bt, lplace_bt, lplay_bt;
    Context cnt;


    long prevTime;
    long depthPubTime;
    int i, j;
    long timeinMs;
    Time currentTime;
    ImageView vw;
    short x, y;

   
    private void init() {
        setWillNotDraw(false); // Enable view to draw.
        mSurfaceHolder = getHolder();

    }

    public SuperframeParser(Context context) {
        super(context);
        setWillNotDraw(false);
        cnt = context;
        init();

    }

    public SuperframeParser(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        cnt = context;
        init();
    }


    public void setDepthListener(DepthInterface rawImageListener, ConnectedNode node) {
        this.rawImageListener = rawImageListener;
        this.depth_connectedNode = node;
    }

    public void setRGBListener(RGBInterface rawImageListener, ConnectedNode node) {
        this.rawRgbListener = rawImageListener;
        this.rgb_connectedNode = node;
    }

    /**
     * Set camera mode then start the camera preview.
     */
    public void startCamera() {
        mSurfaceHolder.addCallback(this);
        try {
            mCamera = Camera.open();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        parameters = mCamera.getParameters();
        // Note: sf modes are "all", "big-rgb", "small-rgb", "depth", "ir".
        parameters.set("sf-mode", "big-rgb");  // Show the RGB image.
        //parameters.setZoom(15);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    public void stopCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mSurfaceHolder.removeCallback(this);
            mCamera.release();
            mCamera = null;


        }
    }


    private byte[] mBuffer;

    public Bitmap getPic(int x, int y, int width, int height) {
        System.gc();
        Bitmap b = null;
        Camera.Size s = parameters.getPreviewSize();

        YuvImage yuvimage = new YuvImage(mBuffer, ImageFormat.NV21, s.width, s.height, null);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(x, y, width, height), 100, outStream); // make JPG
        b = BitmapFactory.decodeByteArray(outStream.toByteArray(), 0, outStream.size()); // decode JPG
        if (b != null) {
            //Log.i(TAG, "getPic() WxH:" + b.getWidth() + "x" + b.getHeight());
        } else {
            //Log.i(TAG, "getPic(): Bitmap is null..");
        }
        yuvimage = null;
        outStream = null;
        System.gc();
        return b;
    }

    private void updateBufferSize() {
        mBuffer = null;
        System.gc();
        // prepare a buffer for copying preview data to
        int h = mCamera.getParameters().getPreviewSize().height;
        int w = mCamera.getParameters().getPreviewSize().width;
        int bitsPerPixel = ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat());
        mBuffer = new byte[w * h * bitsPerPixel / 8];
        //Log.i("surfaceCreated", "buffer length is " + mBuffer.length + " bytes");
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        //Log.i(TAG,"SurfaceDestroyed being called");
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    /**
     * Change preview size when width and height change. This can occur when the
     * device orientation changes (user rotates phone).
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

        if (mCamera != null && mSurfaceHolder.getSurface() != null) {
            try {
                mCamera.setPreviewCallback(this);
            } catch (Throwable t) {
            }
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (Exception e) {
                return;
            }
            // Set the preview size.
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height,
                    parameters.getSupportedPreviewSizes());
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                mCamera.setParameters(parameters);
            }
            mCamera.startPreview();
        }
    }

    public Camera.Parameters getCameraParameters() {
        return mCamera.getParameters();
    }


    public void setFlash(boolean flash) {
        if (flash) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
        } else {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        }
    }

    public void setViews(RosSurface mDepthView, TextView tv1, TextView tv2, Button bt1, Button bt2, Button bt3,
                         Activity act, Drawable mLeftT, Drawable mLeftBot,
                         Drawable mRightT, Drawable mRightBot) {

        mDv = mDepthView;
        ltv1 = tv1;
        ltv2 = tv2;
        lpick_bt = bt1;
        lplace_bt = bt2;
        lplay_bt = bt3;
        lact = act;
        mLeftTopIcon = mLeftT;
        mLeftBottomIcon = mLeftBot;
        mRightTopIcon = mRightT;
        mRightBottomIcon = mRightBot;
        mDv.setOnTouchListener(this);
        
        initializeBoundingBoxParameters();
    }

    private void initializeBoundingBoxParameters() {

        // I need to create lines for the bouding box to connect

        topLine = new Paint();
        bottomLine = new Paint();
        leftLine = new Paint();
        rightLine = new Paint();

        setLineParameters(Color.WHITE, 2);

        // Here I grab the image that will work as the corners of the bounding
        // box and set their positions.


        mCenter = mLeftTopIcon.getMinimumHeight() / 2;
        mLeftTopIcon.setBounds((int) mLeftTopPosX, (int) mLeftTopPosY,
                mLeftTopIcon.getIntrinsicWidth() + (int) mLeftTopPosX,
                mLeftTopIcon.getIntrinsicHeight() + (int) mLeftTopPosY);


        mRightTopIcon.setBounds((int) mRightTopPosX, (int) mRightTopPosY,
                mRightTopIcon.getIntrinsicWidth() + (int) mRightTopPosX,
                mRightTopIcon.getIntrinsicHeight() + (int) mRightTopPosY);


        mLeftBottomIcon.setBounds((int) mLeftBottomPosX, (int) mLeftBottomPosY,
                mLeftBottomIcon.getIntrinsicWidth() + (int) mLeftBottomPosX,
                mLeftBottomIcon.getIntrinsicHeight() + (int) mLeftBottomPosY);


        mRightBottomIcon.setBounds((int) mRightBottomPosX, (int) mRightBottomPosY,
                mRightBottomIcon.getIntrinsicWidth() + (int) mRightBottomPosX,
                mRightBottomIcon.getIntrinsicHeight() + (int) mRightBottomPosY);
        // Create our ScaleGestureDetector
        mScaleDetector = new ScaleGestureDetector(cnt, new ScaleListener());

    }

    private void setLineParameters(int color, float width) {

        topLine.setColor(Color.RED);
        topLine.setStrokeWidth(width);

        bottomLine.setColor(Color.RED);
        bottomLine.setStrokeWidth(width);

        leftLine.setColor(Color.RED);
        leftLine.setStrokeWidth(width);

        rightLine.setColor(Color.RED);
        rightLine.setStrokeWidth(width);

    }

    //parse superframe for depth and rgb images
    @Override
    public void onPreviewFrame(final byte[] data, Camera cameraPreFrame) {
        try {
            currentTime = rgb_connectedNode.getCurrentTime();
        } catch (Exception e) {
            e.printStackTrace();
            currentTime = Time.fromMillis(System.currentTimeMillis());
        }
        long sysTime = System.currentTimeMillis();
        long curTime = sysTime;
        long timeTaken = curTime - prevTime;
        prevTime = curTime;

        if (depthPubTime > 1000) {
            depthPubTime = 0;
        }
        depthPubTime += timeTaken;

        int depthBufferByteIndex = Superframe.SF_START_INDEX_DEPTH;
        for (int bitmapIndex = 0; bitmapIndex < Superframe.DB_SIZE; ++bitmapIndex) {
            int pixDepthMm = ((((int) data[depthBufferByteIndex + 1]) << 8) & 0xff00)
                    | (((int) data[depthBufferByteIndex]) & 0x00ff);

            rawDepthData[bitmapIndex] = (short) pixDepthMm;

            depthBufferByteIndex += 2;  // Increment the depth index to next Int.


        }
        rawImageListener.onNewDepthImage(rawDepthData, Superframe.DB_WIDTH, Superframe.DB_HEIGHT, currentTime);
        depthPubTime = 0;

        int rgbBigArrayIndex = Superframe.SF_START_INDEX_BIGIMAGEY;
        byte b;

        for (int j = 0; rgbBigArrayIndex < (1280 * 1752); j++) {
            b = data[rgbBigArrayIndex++];
            if (rgbBigArrayIndex == 1495041) {
                rgbBigArrayIndex = 1781759;
                continue;
            }
            rawRgbData[j] = b;
        }
        rawRgbListener.onNewRawImage(rawRgbData, Superframe.RGB_WIDTH, Superframe.RGB_HEIGHT, currentTime);

        try {
            Thread.sleep(200);

        } catch (Exception e) {
            e.printStackTrace();
        }


        skip_frame = !skip_frame;


    }


    float[] delta_angls = new float[3];
    float[] crnt_rot = new float[9];
    float[] prev_rot = new float[9];
    float[] rot = new float[9];
   /* @Override
    public boolean onTouch(View v, MotionEvent event) {

        prev_rot = rot;
        //   System.out.println("hello");
        ltv1.setText(String.valueOf(event.getX()));
        x = (short) event.getX();
        y = (short) event.getY();
        //     tch_X.setText(String.valueOf(viewCoords[0]));
        ltv2.setText(String.valueOf(event.getY()));
        //     tch_Y.setText(String.valueOf(viewCoords[1]));


        compRgbListener.onNewCord(x, y);
        mDv.invalidate();





        return true;
    }*/

    public void getCenterPointOnTheBox() {

        x = (short) (mLeftTopPosX + ((mRightTopPosX - mLeftTopPosX) / 2));
        y = (short) (mLeftTopPosY + ((mLeftBottomPosY - mLeftTopPosY) / 2));

        ltv1.setText(String.valueOf(x));
        ltv2.setText(String.valueOf(y));
        commandRobo("pick");
        rawRgbListener.onNewCord(x, y, (short) mLeftTopPosX, (short) mLeftTopPosY, (short) mLeftBottomPosX, (short) mLeftBottomPosY,
                (short) mRightTopPosX, (short) mRightTopPosY, (short) mRightBottomPosX, (short) mRightBottomPosY);
        Toast.makeText(cnt, "Pick the object", Toast.LENGTH_SHORT).show();

    }

    public void commandRobo(String str) {

        rawRgbListener.onNewCommand(str);
    }

    public void playTheRobot(){
        rawRgbListener.onPlayCommand(true);
    }

    public int convertYUVtoRGB(int y, int u, int v) {

        int r, g, b;

        r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
        g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
        b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

        //r = y + (int)1.370705*v;
        //g = y - (int)(0.698001*v - 0.337633*u);
        //b = y + (int)1.732446*u;

        r = r > 255 ? 255 : r < 0 ? 0 : r;
        g = g > 255 ? 255 : g < 0 ? 0 : g;
        b = b > 255 ? 255 : b < 0 ? 0 : b;

        return 0xff000000 | (r << 16) | (g << 8) | b;

        //  return 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);

    }


    private Paint pick_pntText = new Paint();
    private Paint place_pntText = new Paint();
    float mLastTouchCordX, mLastTouchCordY;
    float xCord, yCord;
    int[] cord = new int[3];

    @Override
    public void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        //   System.out.println("hello");

        pnt.setColor(Color.BLUE);
        pnt.setStrokeWidth(3);

        pntX.setStrokeWidth(3);
        pntX.setColor(Color.RED);
        Path pt = new Path();

        pntY.setStrokeWidth(3);
        pntY.setColor(Color.GREEN);

        pntX1.setStrokeWidth(3);
        pntX1.setColor(Color.RED);

        pntY1.setStrokeWidth(3);
        pntY1.setColor(Color.GREEN);

        //     pntText.setStrokeWidth(12);
        pntText.setTextSize(40);
        pntText.setColor(Color.BLACK);

        pick_pntText.setTextSize(40);
        pick_pntText.setColor(Color.CYAN);

        place_pntText.setTextSize(40);
        place_pntText.setColor(Color.MAGENTA);

        float dist;
        int index = ((y / 4) * 320 + (x / 4));

        dist = rawDepthData[index];
        dist = dist * 0.001f;

        float mRealCordA_X, mRealCordA_Y, mRealCordB_X, mRealCordB_Y;


        x = (short) (mLeftTopPosX + ((mRightTopPosX - mLeftTopPosX) / 2));
        y = (short) (mLeftTopPosY + ((mLeftBottomPosY - mLeftTopPosY) / 2));
        canvas.drawText("Pick", x, y, pick_pntText);
        canvas.drawLine(x, y, x + 60, y, pntX);
        canvas.drawLine(x, y, x, y + 60, pntY);


        canvas.drawLine(mLeftTopPosX + mCenter, mLeftTopPosY + mCenter,
                mRightTopPosX + mCenter, mRightTopPosY + mCenter, topLine);
        canvas.drawLine(mLeftBottomPosX + mCenter, mLeftBottomPosY + mCenter,
                mRightBottomPosX + mCenter, mRightBottomPosY + mCenter, bottomLine);
        canvas.drawLine(mLeftTopPosX + mCenter, mLeftTopPosY + mCenter,
                mLeftBottomPosX + mCenter, mLeftBottomPosY + mCenter, leftLine);
        canvas.drawLine(mRightTopPosX + mCenter, mRightTopPosY + mCenter,
                mRightBottomPosX + mCenter, mRightBottomPosY + mCenter, rightLine);


        mLeftTopIcon.setBounds((int) mLeftTopPosX, (int) mLeftTopPosY,
                mLeftTopIcon.getIntrinsicWidth() + (int) mLeftTopPosX,
                mLeftTopIcon.getIntrinsicHeight() + (int) mLeftTopPosY);

        mRightTopIcon.setBounds((int) mRightTopPosX, (int) mRightTopPosY,
                mRightTopIcon.getIntrinsicWidth() + (int) mRightTopPosX,
                mRightTopIcon.getIntrinsicHeight() + (int) mRightTopPosY);

        mLeftBottomIcon.setBounds((int) mLeftBottomPosX, (int) mLeftBottomPosY,
                mLeftBottomIcon.getIntrinsicWidth() + (int) mLeftBottomPosX,
                mLeftBottomIcon.getIntrinsicHeight() + (int) mLeftBottomPosY);

        mRightBottomIcon.setBounds((int) mRightBottomPosX, (int) mRightBottomPosY,
                mRightBottomIcon.getIntrinsicWidth() + (int) mRightBottomPosX,
                mRightBottomIcon.getIntrinsicHeight() + (int) mRightBottomPosY);


        mLeftTopIcon.draw(canvas);
        mRightTopIcon.draw(canvas);
        mLeftBottomIcon.draw(canvas);
        mRightBottomIcon.draw(canvas);
        canvas.restore();

    }


    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        final int action = ev.getAction();
        boolean intercept = true;

        switch (action) {

            case MotionEvent.ACTION_DOWN: {

                final float x = ev.getX();
                final float y = ev.getY();

                // in CameraPreview we have Rect rec. This is passed here to return
                // a false when the camera button is pressed so that this view ignores
                // the touch event.
                if ((x >= buttonRec.left) && (x <= buttonRec.right) && (y >= buttonRec.top) && (y <= buttonRec.bottom)) {
                    intercept = false;
                    break;
                }

                // is explained below, when we get to this method.
                manhattanDistance(x, y);

                // Remember where we started
                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {

                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX();
                final float y = ev.getY();
                //Log.i(TAG,"x: "+x);
                //Log.i(TAG,"y: "+y);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                // but we ignore here because we are not using ScaleGestureDetector.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    invalidate();
                }

                // Calculate the distance moved
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;


                // Move the object
                if (mPosX >= 0 && mPosX <= 800) {
                    mPosX += dx;
                }
                if (mPosY >= 0 && mPosY <= 480) {
                    mPosY += dy;
                }

                // while its being pressed n it does not overlap the bottom line or right line
                if (mLeftTopBool && ((y + mCenter * 2) < mLeftBottomPosY) && ((x + mCenter * 2) < mRightTopPosX)) {
                    if (dy != 0) {
                        mRightTopPosY = y;
                    }
                    if (dx != 0) {
                        mLeftBottomPosX = x;
                    }
                    mLeftTopPosX = x;//mPosX;
                    mLeftTopPosY = y;//mPosY;
                }
                if (mRightTopBool && ((y + mCenter * 2) < mRightBottomPosY) && (x > (mLeftTopPosX + mCenter * 2))) {
                    if (dy != 0) {
                        mLeftTopPosY = y;
                    }
                    if (dx != 0) {
                        mRightBottomPosX = x;
                    }
                    mRightTopPosX = x;//mPosX;
                    mRightTopPosY = y;//mPosY;
                }
                if (mLeftBottomBool && (y > (mLeftTopPosY + mCenter * 2)) && ((x + mCenter * 2) < mRightBottomPosX)) {
                    if (dx != 0) {
                        mLeftTopPosX = x;
                    }
                    if (dy != 0) {
                        mRightBottomPosY = y;
                    }
                    mLeftBottomPosX = x;
                    mLeftBottomPosY = y;
                }
                if (mRightBottomBool && (y > (mLeftTopPosY + mCenter * 2)) && (x > (mLeftBottomPosX + mCenter * 2))) {
                    if (dx != 0) {
                        mRightTopPosX = x;
                    }
                    if (dy != 0) {
                        mLeftBottomPosY = y;
                    }
                    mRightBottomPosX = x;
                    mRightBottomPosY = y;
                }

                // Remember this touch position for the next move event
                mLastTouchX = x;
                mLastTouchY = y;

                // Invalidate to request a redraw
                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                // when one of these is true, that means it can move when onDraw is called
                mLeftTopBool = false;
                mRightTopBool = false;
                mLeftBottomBool = false;
                mRightBottomBool = false;
                //mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                // Extract the index of the pointer that left the touch sensor
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return intercept;
    }

    // Where the screen is pressed, calculate the distance closest to one of the 4 corners
    // so that it can get the pressed and moved. Only 1 at a time can be moved.
    private void manhattanDistance(float x, float y) {

        double leftTopMan = Math.sqrt(Math.pow((Math.abs((double) x - (double) mLeftTopPosX)), 2)
                + Math.pow((Math.abs((double) y - (double) mLeftTopPosY)), 2));

        double rightTopMan = Math.sqrt(Math.pow((Math.abs((double) x - (double) mRightTopPosX)), 2)
                + Math.pow((Math.abs((double) y - (double) mRightTopPosY)), 2));

        double leftBottomMan = Math.sqrt(Math.pow((Math.abs((double) x - (double) mLeftBottomPosX)), 2)
                + Math.pow((Math.abs((double) y - (double) mLeftBottomPosY)), 2));

        double rightBottomMan = Math.sqrt(Math.pow((Math.abs((double) x - (double) mRightBottomPosX)), 2)
                + Math.pow((Math.abs((double) y - (double) mRightBottomPosY)), 2));

        //Log.i(TAG,"leftTopMan: "+leftTopMan);
        //Log.i(TAG,"RightTopMan: "+rightTopMan);

        if (leftTopMan < 50) {
            mLeftTopBool = true;
            mRightTopBool = false;
            mLeftBottomBool = false;
            mRightBottomBool = false;
        } else if (rightTopMan < 50) {
            mLeftTopBool = false;
            mRightTopBool = true;
            mLeftBottomBool = false;
            mRightBottomBool = false;
        } else if (leftBottomMan < 50) {
            mLeftTopBool = false;
            mRightTopBool = false;
            mLeftBottomBool = true;
            mRightBottomBool = false;
        } else if (rightBottomMan < 50) {
            mLeftTopBool = false;
            mRightTopBool = false;
            mLeftBottomBool = false;
            mRightBottomBool = true;
        }

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }

    public float getmLeftTopPosX() {
        return mLeftTopPosX;
    }

    public float getmLeftTopPosY() {
        return mLeftTopPosY;
    }

    public float getmRightTopPosX() {
        return mRightTopPosX;
    }

    public float getmRightTopPosY() {
        return mRightTopPosY;
    }

    public float getmLeftBottomPosX() {
        return mLeftBottomPosX;
    }

    public float getmLeftBottomPosY() {
        return mLeftBottomPosY;
    }

    public float getmRightBottomPosY() {
        return mRightBottomPosY;
    }

    public float getmRightBottomPosX() {
        return mRightBottomPosX;
    }

    public void setRec(Rect rec) {
        this.buttonRec = rec;
    }

    // calls the onDraw method, I used it in my app Translanguage OCR
    // because I have a thread that needs to invalidate, or redraw
    // you cannot call onDraw from a thread not the UI thread.
    public void setInvalidate() {
        invalidate();

    }


    public void getRealXYCord(float x, float y, float dist) {

        float fx = (float) K[0];
        float fy = (float) K[4];
        float cx = (float) K[2];
        float cy = (float) K[5];

        xCord = ((x / 4) - cx) * (dist / fx);
        yCord = ((y / 4) - cy) * (dist / fy);

    }


    public float[] getTheRotationMatrix(float[] angls) {
        float A, B, C;
        float[] rotMat = new float[9];
        A = angls[0];
        B = angls[2];
        C = angls[1];

        rotMat[0] = (float) (Math.cos(A) * Math.cos(B));
        rotMat[1] = (float) ((Math.sin(B) * Math.sin(C) * Math.cos(A)) - (Math.sin(A) * Math.cos(C)));
        rotMat[2] = (float) ((Math.sin(B) * Math.sin(C) * Math.cos(A)) + (Math.sin(A) * Math.sin(C)));

        rotMat[3] = (float) (Math.sin(A) * Math.cos(B));
        rotMat[4] = (float) ((Math.sin(B) * Math.sin(C) * Math.sin(A)) + (Math.cos(A) * Math.cos(C)));
        rotMat[5] = (float) ((Math.sin(B) * Math.cos(C) * Math.sin(A)) - (Math.cos(A) * Math.sin(C)));

        rotMat[6] = (float) (-Math.sin(B));
        rotMat[7] = (float) (Math.cos(B) * Math.sin(C));
        rotMat[8] = (float) (Math.cos(B) * Math.cos(C));

        return rotMat;


    }


    /**
     * Helper method to select the best preview size from a list of choices.
     *
     * @param width  Desired width
     * @param height Desired height
     * @param sizes  List of possible preview sizes
     * @return Best preview size
     */
    private Camera.Size getBestPreviewSize(int width, int height,
                                           List<Camera.Size> sizes) {
        Camera.Size result = null;
        for (Camera.Size size : sizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return (result);
    }


}
