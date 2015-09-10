package com.ros.tango.peanut;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * DepthScanActivity is the launch activity of the PeanutDepthScan app.
 * PeanutDepthScan is a real-time camera app that scans objects by drawing a
 * 'Depth Line' over the top of them. The 'Depth Line' is based on the actual
 * depth of a pixel, this depth data comes from the Peanut's depth buffer. Upon
 * launch, the app starts a camera object and sets a surface for it to draw on.
 * The app then reads the depth buffer during the camera preview. It then draws
 * a 'false color' overlay on top the camera image, the color is based on the
 * depth buffer.
 * Note: The depth buffer is a 2 dimensional array of ints that corresponds to
 * the images the camera captures.
 *
 * @author Michael F. Winter (robotmikew@gmail.com)
 */

public class Peanut extends RosActivity{
    RosSurface mDepthView = null;  //Surface for drawing on.
    private TextView tv1,tv2;
    private Button pick_bt,place_bt,play_bt;
    private FrameLayout flout;
    //////////////////////////////////////////////////////////////////////////

    // private TouchView mView;
    private ImageView mTakePicture;

    private Drawable mLeftTopIcon;
    private Drawable mRightTopIcon;
    private Drawable mLeftBottomIcon;
    private Drawable mRightBottomIcon;


    private boolean mAutoFocus = true;

    private boolean mFlashBoolean = false;

    private SensorManager mSensorManager;
    private Sensor mAccel;
    private boolean mInitialized = false;
    private float mLastX = 0;
    private float mLastY = 0;
    private float mLastZ = 0;
    private Rect rec = new Rect();

    private int mScreenHeight;
    private int mScreenWidth;

    private boolean mInvalidate = false;

    private File mLocation = new File(Environment.
            getExternalStorageDirectory(),"test.jpg");
/////////////////////////////////////////////////////////////////////////////////////////////////

    public Peanut() {
        // The RosActivity constructor configures the notification title and ticker
        // messages.
        super("Pubsub Tutorial", "Pubsub Tutorial");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set window to full screen view with no title.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);


        tv1 = (TextView)findViewById(R.id.pos_x);
        tv2 = (TextView)findViewById(R.id.pos_y);

        pick_bt = (Button)findViewById(R.id.but_pick);
        place_bt = (Button)findViewById(R.id.but_place);
        play_bt = (Button)findViewById(R.id.play);

       /////////////////////////////////////////////////////////////////////////////////////////////////

        mLeftTopIcon = this.getResources().getDrawable(R.drawable.cutmypic);
        mRightTopIcon = this.getResources().getDrawable(R.drawable.cutmypic);
        mLeftBottomIcon = this.getResources().getDrawable(R.drawable.cutmypic);
        mRightBottomIcon = this.getResources().getDrawable(R.drawable.cutmypic);
        // get the window width and height to display buttons
        // according to device screen size
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenHeight = displaymetrics.heightPixels;
        mScreenWidth = displaymetrics.widthPixels;
        mDepthView = (RosSurface)findViewById(R.id.depthview);
        mDepthView.setRec(rec);

      ///////////////////////////////////////////////////////////////////////////////////////////////////


      mDepthView.setViews(mDepthView,tv1,tv2,pick_bt,place_bt,play_bt,this,mLeftTopIcon,mLeftBottomIcon,mRightTopIcon,mRightBottomIcon);

   }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(), getMasterUri());
        nodeConfiguration.setMasterUri(getMasterUri());

        nodeMainExecutor.execute(mDepthView, nodeConfiguration);
    }



    public void teachRobot(View v){
        mDepthView.getCenterPointOnTheBox();
    }

    public void playRobot(View v){

        mDepthView.playTheRobot();
    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    // extra overrides to better understand app lifecycle and assist debugging
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.i(TAG, "onDestroy()");
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mDepthView.startCamera();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Log.i(TAG, "onRestart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDepthView.stopCamera();
        //Log.i(TAG, "onStop()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.i(TAG, "onStart()");
    }


}
