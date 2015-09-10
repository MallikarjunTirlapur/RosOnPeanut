package com.ros.tango.peanut;

import android.graphics.Rect;
import android.graphics.YuvImage;

import com.google.common.base.Preconditions;

import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.ros.internal.message.MessageBuffers;
import org.ros.message.Time;
import org.ros.namespace.NameResolver;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import java.nio.ByteBuffer;

import sensor_msgs.Image;

/**
 * Created by mallik on 9/10/15.
 */
public class DepthPublisher implements DepthInterface {

    private final ConnectedNode connectedNode;
    private final Publisher<Image> imagePublisher;
    private final Publisher<sensor_msgs.CameraInfo> cameraInfoPublisher;

    private byte[] rawImageBuffer;
    private int rawImageSize;
    private YuvImage yuvImage;
    private Rect rect;
    private ChannelBufferOutputStream stream;
    private ByteBuffer buf;
    //private ShortBuffer sbuf;

    private double[] D = {0.2104473, -0.5854902, 0.4575633, 0.0, 0.0};
    private double[] K = {234.941, 0.0, 157.622, 0.0, 234.877, 88.2387, 0.0, 0.0, 1.0};
    private double[] R = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    private double[] P = {234.941, 0.0, 157.622, 0.0, 0.0, 234.877, 88.2387, 0.0, 0.0, 0.0, 1.0, 0.0};

    public DepthPublisher(ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        NameResolver resolver = connectedNode.getResolver().newChild("camera/depth");
        imagePublisher =
                connectedNode.newPublisher(resolver.resolve("image_raw"),
                        sensor_msgs.Image._TYPE);
        cameraInfoPublisher =
                connectedNode.newPublisher(resolver.resolve("camera_info"), sensor_msgs.CameraInfo._TYPE);
        stream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());

    }

    @Override
    public void onNewDepthImage(short[] data, int width, int height, Time currentTime) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(width);
        Preconditions.checkNotNull(height);
        String frameId = "/map";
        sensor_msgs.Image image = imagePublisher.newMessage();
        image.getHeader().setStamp(currentTime);
        image.getHeader().setFrameId(frameId);

        for (int i = 0; i < data.length; i++){
            try {
                stream.writeShort((short)data[i]);
            }
            catch (Exception e){

            }
        }


        image.setStep(320*2);
        image.setWidth(width);
        image.setHeight(height);
        image.setEncoding("16UC1");
        image.setData(stream.buffer().copy());
        stream.buffer().clear();

        imagePublisher.publish(image);

        sensor_msgs.CameraInfo cameraInfo = cameraInfoPublisher.newMessage();
        cameraInfo.getHeader().setStamp(currentTime);
        cameraInfo.getHeader().setFrameId(frameId);

        cameraInfo.setDistortionModel("plumb_bob");
        cameraInfo.setD(D);
        cameraInfo.setK(K);
        cameraInfo.setR(R);
        cameraInfo.setP(P);

        cameraInfo.setWidth(width);
        cameraInfo.setHeight(height);
        cameraInfoPublisher.publish(cameraInfo);
    }
}