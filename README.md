# ROS as a Service

---
## Beta version

This project is currently in its beta phase. If you find an error, please open an issue in the
[issue tracker](https://github.com/UniversalRobots/Universal_Robots_ROS_as_a_Service_URCap/issues)
to help tracking down any issues existing with this URCap.

Not all features are implemented, yet, see the [missing features](doc/missing_features.md) list.

---

<img height="300" alt="program structure" src="doc/resources/tutorial/7.png"> <img height="300" alt="turtlesim window" src="doc/resources/tutorial/8.png">

This URCaps allows you to call ROS `services`, `actions` and use `topics`
from within your Universal Robots application setup on the Teach panel. The main program will stay on the robot while
you can integrate features provided by ROS nodes over the network.
The idea is to combine the _best of both worlds_.
For instance, a computational intense AI application in the ROS
framework could provide the poses of work pieces, which are then used by the
main program running on the robot.

## Prerequisites
This URCap uses Swing to implement the user interface and requires
PolyScope versions 3.7 (UR3, UR5, UR10) or 5.1 (E-series) or higher.

## Installation
The installation is described in a [separate guide](doc/installation.md)


## Quick start
This URCap implements a client for the [Rosbridge server](http://wiki.ros.org/rosbridge_server) to
connect to a running ROS ecosystem.
Every part of the bi-directional communication goes over that bridge and uses
the common ROS messaging mechanisms as abstraction.
Note that your _ROS side_ must be all set and running when you compose your program in
PolyScope. Available ROS interfaces are parsed and can then be chosen in drop-down menus on the teach panel.

Follow the instructions below to use ROS functionality directly from within
your Polyscope programs. For more detailed instructions see the separate
[tutorial](doc/tutorial.md).


1. Prepare your _ROS side_ by launching the Rosbridge server:
    ```bash
    roslaunch rosbridge_server rosbridge_tcp.launch
    ```
 
1. In the _Installation_ tab of Polyscope:

   Under _URCaps_ on the left, select the _Rosbridge adapter_ and adjust the remote host's IP address and port (If you didn't change it, it should be 9090).
   Use `ip addr` on your ROS PC in a terminal if you are unsure about your PC's IP address in the network. Pick the IP address
   of the interface that is connected to the robot. (Your robot's IP address should be similar, as they are in the same subnet.)

1. In the _Program_ tab of Polyscope:

   Add program nodes to publish or read data from topics or calling a ROS service to your program.
   Setup variable mapping where desired.

## Acknowledgments

Developed in collaboration between:

[<img height="60" alt="Universal Robots A/S" src="doc/resources/ur_logo.jpg">](https://www.universal-robots.com/) &nbsp; and &nbsp;
[<img height="60" alt="FZI Research Center for Information Technology" src="doc/resources/fzi-logo_transparenz.png">](https://www.fzi.de).

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

