# Publisher and Subscriber

This tutorial explains the usage of ROS publishers and subscribers with the turtlesim.

## Setup
First, start the rosbridge as described [here](./../README.md) and run the turtlesim as described [here](./turtlebot.md).

Next, set-up the network in the _Installation_ tab of Polyscope:

* Under _URCaps_ on the left, select the _Rosbridge adapter_ and adjust the remote host's (The PC running your ROS system) IP address and port (If you didn't change it, it should be 9090).

* Use `ip addr` on your ROS PC in a terminal if you are unsure about your PC's IP address in the network. Pick the IP address of the interface that is connected to the robot. Your robot's IP address should be similar, as they are in the same subnet.



![](./resources/publisher_subscriber/1.png)

## Publish on a topic

With this program node you can publish a message to an existing topic. Note, that it has to be advertised once before (so it is known by the rosbridge) it can be used on the teach pendant. Advertising new topics is currently not supported.

We create a new program in Polyscope under the _Program_ tab. Uncheck the checkbox for _Program loops forever_ so that our robot program gets executed only once.

![](./resources/publisher_subscriber/2.png)




Under _URCaps_ on the left 

* chose _Publish Topic_ to add a ROS Publisher to the robot program and select its Remote master in the drop-down menu on the right.

* The drop-down menu for _Topic_ holds every topic that can be published to with this Publisher. This information is inquired from ROS when you add a new instance to your robot program. Select `/turtle1/cmd_vel` to steer the turtlebot in simulation.


![](./resources/publisher_subscriber/3.png)


A _Message_ tree should appear. You can open it by clicking on the icons left to the folders and fill the data types. The turtle simulator only uses the `linear x` and `angular z` fields.


![](./resources/publisher_subscriber/4.png)

In general, two options are available to fill the fields:

* The first one is by checking _Use variable_, which lets you re-use the content of specific variables to populate your outgoing message. This is handy if you want to pass information directly without manually editing anything. Variables can be either setup in the _Installation_ tab or by adding _Assign_ nodes before creating the _Publish Topic_ node (see below).

* The second option is to enter the values that should be used directly into the value field.


![](./resources/publisher_subscriber/6.png)

We now have configured our first ROS Publisher. Let's use it once by clicking _Play from beginning_. 

![](./resources/publisher_subscriber/7.png)

Your turtle should move similar to this one:

![](./resources/publisher_subscriber/8.png)


## Read data from a topic

The _Subscribe Topic_ node subscribes to a specified topic, waits for a message to arrive and saves fields from that message to variables if desired.

Before adding a _Subscriber_ to our robot program, we set-up some variables that we can use as buffers for our data. Switch to the _Advanced_ tab on the left and select _Assignment_. This allows us to define variables in-place. Alternatively setup variables using the  _Installation_ tab.

In this example, we want to read the turtle's position and orientation from ROS.


![](./resources/publisher_subscriber/9.png)

Initialize the variable by inserting an _Expression_, which is simply 0 in this case.

![](./resources/publisher_subscriber/10.png)

Add two more variables as shown below.

![](./resources/publisher_subscriber/11.png)

Now we can add the ROS Subscriber by adding _Subscribe Topic_ to our robot program. Select the Remote master as before and _/turtle1/pose/_ as _Topic_.

![](./resources/publisher_subscriber/12.png)

You can now assign the variables we just created to the individual data fields. Data fields without a variable assignment will not be used in the program.


![](./resources/publisher_subscriber/13.png)

Clicking _Play from beginning_ should move your turtlebot.

![](./resources/publisher_subscriber/14.png)

If you used the values from this tutorial, your turtlesim window should look something like this:

![](./resources/publisher_subscriber/15.png)

You can inspect the result of our first ROS Subscriber by clicking on the _Variables_ tab. Note that these values _do not_ correspond to the turtlebot's final position! Instead, the result was read from ROS exactly when executing _Sub. /turtle1/pose_ in the robot program tree on the left. 

![](./resources/publisher_subscriber/16.png)

