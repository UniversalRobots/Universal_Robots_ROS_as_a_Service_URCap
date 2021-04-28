package de.fzi.ros_as_a_service.impl;

import static org.junit.Assert.assertNotEquals;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for MasterPair
 */
public class MasterPairTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public MasterPairTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(MasterPairTest.class);
  }

  public void testEqual() {
    MasterPair m1 = new MasterPair("192.168.0.1", "9090");
    MasterPair m2 = new MasterPair("192.168.1.1", "9090");
    MasterPair m3 = new MasterPair("192.168.0.1", "90");
    assertEquals(m1, m1);
    assertNotEquals(m1, m2);
    assertNotEquals(m1, m3);
    assertNotEquals(m2, m3);
  }

  public void testToString() {
    MasterPair m1 = new MasterPair("192.168.0.1", "9090");
    assertEquals(m1.toString(), "no_name : 192.168.0.1 : 9090");
  }

  public void testFromString() {
    String master_str = "my_pair : 192.168.0.1 : 9090";
    MasterPair m1 = MasterPair.fromString(master_str);
    assertEquals(m1.toString(), master_str);

    try {
      MasterPair.fromString("1234");
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal string passed to MasterPair", e.getMessage());
    }
  }
}
