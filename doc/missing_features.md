# Feature list

## Missing features
The following features are planned to come soon

### Support multiple ROS remotes in the Installation
While it is possible to select different masters in the program nodes, only one can be configured
inside the Installation, currently.

### Calling ROS actions
Actions are not implemented, yet.

### A certain amount of input validation
Currently, no input validation is being done. If you try to publish a number to a string field or
vice versa, the rosbridge might refuse to execute the command.

### Support message fields with arrays
Currently, message fields containing an array (such as `String[]`) are not supported. Messages
containing those cannot be used with this URCap at the moment.
