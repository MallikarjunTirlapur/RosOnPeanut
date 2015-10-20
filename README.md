# RosOnPeanut

The android application built for project tango smartphone on ROS platform utilizing rosjava apis. 
The application parses the superframe for RGB and Depth images.These images are compressed and published in the ROS network.
A bounding box is also created to select objects in the camera preview and to compute the 2D pixel coordinate.

Requirements,
Android Studio 0.8.6
Minimum SDK version 18

For more information on project tango see,
[https://www.google.com/atap/projecttango/?ref=producthunt#project)

### The instructions,
* **Create a ROS catkin workspace. Please follow the instruction [http://wiki.ros.org/catkin/Tutorials/create_a_workspace)
* **pull the repository into your workspace
```
> cd ~/RosOnPeanut/src
> git clone https://github.com/MallikarjunTirlapur/RosOnPeanut.git
````
* **pull the repository into your workspace
```
> cd ~/RosOnPeanut
> catkin_make
```
* **Open the project in android studio, build the project and install the application in project tango smartphone. 
Please let me know, if you face any problem.


