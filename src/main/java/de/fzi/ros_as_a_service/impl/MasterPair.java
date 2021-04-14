// -- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2021 FZI Forschungszentrum Informatik
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
 * \author  Carsten Plasberg plasberg@fzi.de
 * \date    2021-01-28
 *
 */
//----------------------------------------------------------------------

package de.fzi.ros_as_a_service.impl;

public class MasterPair {
  private String ip;
  private String port;
  public MasterPair(String ip, String port) {
    this.ip = ip;
    this.port = port;
  }
  public MasterPair() {
    this.ip = "";
    this.port = "";
  }
  public String getIp() {
    return ip;
  }
  public String getPort() {
    return port;
  }
  public void set(String ip, String port) {
    this.ip = ip;
    this.port = port;
  }
  public String toString() {
    return ip + " : " + port;
  }
  public static MasterPair fromString(String value) {
    String[] data = value.split(" : ", 2);
    if (data.length != 2) {
      throw new IllegalArgumentException("Illegal string passed to MasterPair");
    }
    return new MasterPair(data[0], data[1]);
  }

  @Override
  public boolean equals(Object comp) {
    if (comp == null || getClass() != comp.getClass())
      return false;
    MasterPair rhs = (MasterPair) comp;
    return this.getIp() == rhs.getIp() && this.getPort() == rhs.getPort();
  }
}
