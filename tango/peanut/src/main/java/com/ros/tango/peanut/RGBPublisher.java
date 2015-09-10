package com.ros.tango.peanut;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

import com.google.common.base.Preconditions;

import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.ros.internal.message.MessageBuffers;
import org.ros.internal.message.RawMessage;
import org.ros.message.Time;
import org.ros.namespace.NameResolver;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import sensor_msgs.CompressedImage;
import std_msgs.Bool;
import std_msgs.UInt16MultiArray;

/**
 * Created by mallik on 9/10/15.
 */
public class RGBPublisher implements RGBInterface {

    private final ConnectedNode connectedNode;
    private final Publisher<CompressedImage> imagePublisher;
    private final Publisher<sensor_msgs.CameraInfo> cameraInfoPublisher;
    private final Publisher<UInt16MultiArray> pixelCordPublish;
    private final Publisher<std_msgs.String> commandPublish;
    private final Publisher<Bool> boolPublish;

    private byte[] rawImageBuffer;
    private Camera.Size rawImageSize, size;
    private YuvImage yuvImage;
    private Rect rect;
    private ChannelBufferOutputStream stream;
    sensor_msgs.CompressedImage image;
    sensor_msgs.CameraInfo cameraInfo;
    UInt16MultiArray cord;
    std_msgs.String comnd;
    Bool boolData;

    private double[] D = {0.2104473, -0.5854902, 0.4575633, 0.0, 0.0};
    private double[] K = {234.941, 0.0, 157.622, 0.0, 234.877, 88.2387, 0.0, 0.0, 1.0};
    private double[] R = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    private double[] P = {234.941, 0.0, 157.622, 0.0, 0.0, 234.877, 88.2387, 0.0, 0.0, 0.0, 1.0, 0.0};

    public RGBPublisher(ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        NameResolver resolver = connectedNode.getResolver().newChild("camera/rgb");
        imagePublisher =
                connectedNode.newPublisher(resolver.resolve("compressed"),
                        sensor_msgs.CompressedImage._TYPE);
        pixelCordPublish = connectedNode.newPublisher(resolver.resolve("pixelCord"),
                UInt16MultiArray._TYPE);
        commandPublish = connectedNode.newPublisher(resolver.resolve("command"), std_msgs.String._TYPE);
        boolPublish = connectedNode.newPublisher(resolver.resolve("bool"),Bool._TYPE);
        cameraInfoPublisher =
                connectedNode.newPublisher(resolver.resolve("camera_info"), sensor_msgs.CameraInfo._TYPE);
        stream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());

        image = imagePublisher.newMessage();
        cameraInfo = cameraInfoPublisher.newMessage();
        cord = pixelCordPublish.newMessage();
        comnd = commandPublish.newMessage();
        boolData = boolPublish.newMessage();

        initTopic();
    }

    public void initTopic() {
        String frameId = "/map";
        image.setFormat("jpeg");
        image.getHeader().setFrameId(frameId);

        cameraInfo.getHeader().setFrameId(frameId);
        cameraInfo.setDistortionModel("plumb_bob");
        cameraInfo.setD(D);
        cameraInfo.setK(K);
        cameraInfo.setR(R);
        cameraInfo.setP(P);

        cameraInfo.setWidth(320);
        cameraInfo.setHeight(180);


    }

    @Override
    public void onNewRawImage(byte[] data, int width, int height, Time currentTime) {

        //    Time currentTime = connectedNode.getCurrentTime();

        Preconditions.checkNotNull(data);
        System.out.println(data.length);

        rawImageBuffer = quatYUV420(data, (width), (height));

        System.out.println(rawImageBuffer.length);

        yuvImage = new YuvImage(rawImageBuffer, ImageFormat.NV21, 320, 180, null);
        rect = new Rect(0, 0, 320, 180);
        image.getHeader().setStamp(currentTime);
        Preconditions.checkState(yuvImage.compressToJpeg(rect, 20, stream));
        image.setData(stream.buffer().copy());
        stream.buffer().clear();
        imagePublisher.publish(image);

        cameraInfo.getHeader().setStamp(currentTime);
        cameraInfoPublisher.publish(cameraInfo);


    }

    @Override
    public void onNewCord(short x, short y,short leftTopX,short leftTopY, short leftBotX, short leftBotY,
                          short rightTopX, short rightTopY,short rightBotX, short rightBotY ) {
        short[] var = new short[2];
        var[0] = x;
        var[1] = y;
        cord.setData(var);
        pixelCordPublish.publish(cord);
    }

    @Override
    public void onNewCommand(String cmnd) {
        try {
            comnd.setData(cmnd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        commandPublish.publish(comnd);
    }

    @Override
    public void onPlayCommand(Boolean data) {
        boolData.setData(data);
        boolPublish.publish(boolData);
    }

    public static byte[] quatYUV420(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[(imageWidth / 4) * (imageHeight / 4) * 3 / 2];
        // quat yuma
        int i = 0;
        for (int y = 0; y < imageHeight; y += 4) {
            for (int x = 0; x < imageWidth; x += 4) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // quat U and V color components
        for (int y = 0; y < imageHeight / 2; y += 4) {
            for (int x = 0; x < imageWidth; x += 8) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i++;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x + 1)];
                i++;
            }
        }
        return yuv;
    }

}


