package com.ros.tango.peanut;

import android.content.Context;
import android.util.AttributeSet;

import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;

/**
 * Created by mallik on 4/6/15.
 */
public class RosSurface extends SuperframeParser implements NodeMain {
    public RosSurface(Context context) {
        super(context);
    }
    public RosSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("ros_depth_preview_view");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        setDepthListener(new DepthPublisher(connectedNode), connectedNode);
        setRGBListener(new RGBPublisher(connectedNode), connectedNode);
    }

    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onShutdownComplete(Node node) {
    }

    @Override
    public void onError(Node node, Throwable throwable) {
    }

}