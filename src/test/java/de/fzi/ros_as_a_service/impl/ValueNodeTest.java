package de.fzi.ros_as_a_service.impl;

import static org.junit.Assert.assertNotEquals;

import de.fzi.ros_as_a_service.impl.ValueInputNode.ValueType;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for ValueNode
 */
public class ValueNodeTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public ValueNodeTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(ValueNodeTest.class);
  }

  public void testTypeFromString() {
    assertEquals(ValueType.STRING, ValueInputNode.getTypeFromString("string"));
    assertEquals(ValueType.INTEGER, ValueInputNode.getTypeFromString("int8"));
    assertEquals(ValueType.INTEGER, ValueInputNode.getTypeFromString("int16"));
    assertEquals(ValueType.INTEGER, ValueInputNode.getTypeFromString("int32"));
    assertEquals(ValueType.INTEGER, ValueInputNode.getTypeFromString("int64"));
    assertEquals(ValueType.UINTEGER, ValueInputNode.getTypeFromString("uint8"));
    assertEquals(ValueType.UINTEGER, ValueInputNode.getTypeFromString("uint16"));
    assertEquals(ValueType.UINTEGER, ValueInputNode.getTypeFromString("uint32"));
    assertEquals(ValueType.UINTEGER, ValueInputNode.getTypeFromString("uint64"));
    assertEquals(ValueType.FLOAT, ValueInputNode.getTypeFromString("float32"));
    assertEquals(ValueType.FLOAT, ValueInputNode.getTypeFromString("float64"));
    assertEquals(ValueType.UINTEGER, ValueInputNode.getTypeFromString("bool"));
    assertEquals(ValueType.UNKNOWN, ValueInputNode.getTypeFromString("gsdhfghf"));
  }
}
