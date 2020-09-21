# UR Rosbridge client

This URCap contains a client for the [Rosbridge server](http://wiki.ros.org/rosbridge_server). With
this, a UR program can participate into a ROS system e.g. by performing subscriptions, service calls
or parameter interaction.

## Development
This URCap is currently being heavily developed. The branching model is as follows:
  - The **master** branch should be stable at all times. It should build and not contain any known
    bugs. Pushes to this branch are disabled, commits can only be added via merge requests.
  - The **devel** branch currently is used for merging different feature branches at an early stage.
    It's history might be rewritten at certain points.
  - All other branches are considered feature branches. New features should be developed on
    individual branches. If for testing integration of other branches is needed, merge into the
    devel branch.
