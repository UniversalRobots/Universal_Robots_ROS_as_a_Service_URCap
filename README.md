---
# Beta version

This project is currently in its beta phase. If you find an error, please open an issue in the
[issue tracker](https://github.com/UniversalRobots/Universal_Robots_ROS_as_a_Service_URCap/issues)
to help tracking down any issues existing with this URCap.

Not all features are implemented, yet, see the [missing features](doc/missing_features.md) list.

---
# ROS as a Service

This URCaps allows you to call ROS `services`, `actions` and use `topics`
from within your Universal Robots application setup on the Teach panel. The main program will stay on the robot while
you can integrate features provided by ROS nodes over the network.
The idea is to combine the _best of both worlds_.
For instance, a computational intense AI application in the ROS
framework could provide the poses of work pieces, which are then used by the
main program running on the robot.

## Acknowledgments
<!--
    ROSIN acknowledgement from the ROSIN press kit
    @ https://github.com/rosin-project/press_kit
-->

<a href="http://rosin-project.eu">
  <img src="http://rosin-project.eu/wp-content/uploads/rosin_ack_logo_wide.png"
       alt="rosin_logo" height="60" >
</a>

Supported by ROSIN - ROS-Industrial Quality-Assured Robot Software Components.
More information: <a href="http://rosin-project.eu">rosin-project.eu</a>

<img src="http://rosin-project.eu/wp-content/uploads/rosin_eu_flag.jpg"
     alt="eu_flag" height="45" align="left" >

This project has received funding from the European Union’s Horizon 2020
research and innovation programme under grant agreement no. 732287.


## Prerequisites
This URCap uses Swing to implement the user interface and requires
PolyScope versions 3.7 (UR3, UR5, UR10) or 5.1 (E-series) or higher.


## Getting started
This URCap implements a client for the [Rosbridge server](http://wiki.ros.org/rosbridge_server) to
connect to a running ROS ecosystem.
Every part of the bi-directional communication goes over that bridge and uses
the common ROS messaging mechanisms as abstraction.
Note that your _ROS side_ must be all set and running when you compose your program in
PolyScope. Available ROS interfaces are parsed and can then be chosen in drop-down menus on the teach panel.


### ROS PC
1. Prepare your _ROS side_ by launching the Rosbridge server:
    ```bash
    roslaunch rosbridge_server rosbridge_tcp.launch
    ```
    Rosbridge uses port `9090` by default. There should be an output similar to `[INFO] [1617114029.525786]: Rosbridge TCP server started on port 9090`.
    You'll need that port later in PolyScope.
    This is all that is required from ROS side.
    For your specific application, you would now directly interact with whatever is available by your ROS nodes.

2. This _getting started_ uses the turtle simulation to have some functionality to test with.
If you want to follow along, make sure that you have the package [turtlesim](http://wiki.ros.org/turtlesim)
installed on your ROS machine. Of course, you can use any other ROS node and use it similarly.
In another terminal, run

    ```bash
    rosrun turtlesim turtlesim_node
    ```
    to bring up a graphical window of the turtle simulation.


### PolyScope
The next steps will explain the workflow with publishing to a ROS topic.

1. In the _Installation_ tab of Polyscope:

	* Under _URCaps_ on the left, select the _Rosbridge adapter_ and adjust the remote host's IP address and port (If you didn't change it, it should be 9090).
        Use `ip addr` on your ROS PC in a terminal if you are unsure about your PC's IP address in the network. Pick the IP address
        of the interface that is connected to the robot. (Your robot's IP address should be similar, as they are in the same subnet.)


1. In the _Program_ tab of Polyscope:

	* Under URCaps on the left, chose _Publish topic_. Under _Command_ on the right, select the Remote master in the drop-down menu.
        The drop-down menue for the topic holds every topic that can be published to.
        Select `/turtle1/cmd_vel` to steer the turtle simulation.

	* A _Message_ tree should appear. You can open it by clicking on the icons left to the
          folders and fill the data types.

        **Note: The current input method isn't final, yet. Tick the "Use variable" checkbox,
          and select a variable that should be used to fill the field's data. Manual data entry is
          currently not supported.**

        Turtle sim only uses the `linear x` and `angular z` fields.

	* Press the play button to control the turtle.

