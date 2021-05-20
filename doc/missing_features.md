# Missing features and limitations

## Missing features
The following features are planned to come soon

### A certain amount of input validation
Currently, no input validation is being done. If you try to publish a number to a string field or
vice versa, the rosbridge might refuse to execute the command. See
[#1](https://github.com/UniversalRobots/Universal_Robots_ROS_as_a_Service_URCap/issues/1) for
details.

### Support message fields with arrays
Currently, message fields containing an array (such as `String[]`) are not supported. Messages
containing those cannot be used with this URCap at the moment. See also [#26](https://github.com/UniversalRobots/Universal_Robots_ROS_as_a_Service_URCap/issues/26).

## Limitations
The underlying API (URCap API and URScript) generate certain limitations that prevent implementing
some of the missing features above. This list is generated to the best of our knowledge, but might
not be 100% correct. If you think that there is something wrong, please contact us e.g. by creating
an issue inside this repository.

### No type deduction in URScript
It is not possible to determine the type of a variable in URScript. However, variables are typed
upon first assignment. e.g. the following script code will generate an error:

```python
my_var = 5
my_var = "hello"
```

Error: `Type error: left-hand side of the expression exected type 'Int' but found 'Const String'`

Thus, variable assignment from ROS data have to be chosen carefully in order to not run into runtime
errors when data received from ROS side does not match the target variable's type.

### Type of variables cannot be deducted in Java
On the Java side we know the variables currently defined inside the system, so the user can select
those e.g. to map them to fields in ROS messages. We know the datatype of the ROS message fields, so
it might be reasonable to only offer variables of the correct type to not run into the issue
described [above](#no-type-deduction-in-urscript).

However, a variable's type cannot be deducted in the URCap API.

Also, this would help with the [parameter
issue](https://github.com/UniversalRobots/Universal_Robots_ROS_as_a_Service_URCap#working-with-ros-parameters)
as we could do automatic type conversion if we knew the target variable's type.

### The maximum string length of URScript is 1023 characters
URScript restricts strings to a maximum length of 1023 characters. Any message retrieved from the
ROSBridge longer than that cannot be parsed, as we cannot pack it into one string message. This is
something that could probably be worked around by implementing partial parsing combined with
multiple read operations from the socket but it is currently not planned to follow up on this.

### No string arrays in URScript
URScript currently doesn't support arrays containing strings. This is part of why
[#26](https://github.com/UniversalRobots/Universal_Robots_ROS_as_a_Service_URCap/issues/26) isn't
implemented, yet.
