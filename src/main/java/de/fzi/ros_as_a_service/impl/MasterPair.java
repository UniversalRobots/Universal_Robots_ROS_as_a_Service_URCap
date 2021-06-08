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
 * \author  Carsten Plasberg plasberg@fzi.de
 * \date    2021-01-28
 *
 */
//----------------------------------------------------------------------

package de.fzi.ros_as_a_service.impl;

public class MasterPair {
  private String ip = "0.0.0.0";
  private String port = "9090";
  private String name = "no_name";
  public MasterPair(String name, String ip, String port) {
    this.name = name;
    this.ip = ip;
    this.port = port;
  }
  public MasterPair(String ip, String port) {
    this.ip = ip;
    this.port = port;
  }
  public MasterPair() {}
  public String getIp() {
    return ip;
  }
  public String getPort() {
    return port;
  }
  public String getName() {
    return name;
  }

  public void set(String ip, String port) {
    this.ip = ip;
    this.port = port;
  }
  public void set(String name, String ip, String port) {
    this.name = name;
    this.ip = ip;
    this.port = port;
  }
  public String toString() {
    return name + " : " + ip + " : " + port;
  }
  public static MasterPair fromString(String value) {
    String[] data = value.split(" : ", 3);

    if (data.length == 2) {
      return new MasterPair(data[0], data[1]);
    } else if (data.length == 3) {
      return new MasterPair(data[0], data[1], data[2]);
    }

    throw new IllegalArgumentException("Illegal string passed to MasterPair");
  }

  public void setName(final String name) {
    this.name = name;
  }
  public void setIp(final String ip) {
    this.ip = ip;
  }
  public void setPort(final String port) {
    this.port = port;
  }

  @Override
  public boolean equals(Object comp) {
    if (comp == null || !getClass().equals(comp.getClass()))
      return false;
    MasterPair rhs = (MasterPair) comp;
    if (!rhs.getIp().equals(this.getIp())) {
    }
    if (!rhs.getPort().equals(this.getPort())) {
    }
    return this.getIp().equals(rhs.getIp()) && this.getPort().equals(rhs.getPort());
  }
}
