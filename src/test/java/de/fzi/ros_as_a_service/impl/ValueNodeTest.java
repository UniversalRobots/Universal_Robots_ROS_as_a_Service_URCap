// -- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2021 FZI Forschungszentrum Informatik
// Created on behalf of Universal Robots A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// -- END LICENSE BLOCK ------------------------------------------------

//----------------------------------------------------------------------
/*!\file
*
* \author  Felix Exner <exner@fzi.de>
* \date    2021-04-20
*
*/
//----------------------------------------------------------------------
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
