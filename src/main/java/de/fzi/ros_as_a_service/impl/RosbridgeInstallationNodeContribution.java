// -- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2019 FZI Forschungszentrum Informatik
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
 * \author  Lea Steffen steffen@fzi.de
 * \date    2020-12-09
 *
 */
//----------------------------------------------------------------------

package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

public class RosbridgeInstallationNodeContribution implements InstallationNodeContribution {
  private static final String MASTERS_KEY = "rosbridge_masters";
  private static final String[] DEFAULT_MASTERS = {"default : 192.168.56.1 : 9090"};
  private DataModel model;
  private final RosbridgeInstallationNodeView view;
  private boolean quote_queried = false;
  private MasterPair[] masters;

  public RosbridgeInstallationNodeContribution(
      InstallationAPIProvider apiProvider, RosbridgeInstallationNodeView view, DataModel model) {
    this.model = model;
    this.view = view;
    this.masters = loadMastersFromModel();
  }

  @Override
  public void openView() {}

  @Override
  public void closeView() {}

  public boolean isDefined() {
    return masters.length > 0;
  }

  // We query the quote string from the first master that is setup inside this program
  public void generateQuoteQueryScript(
      ScriptWriter writer, final String remoteIP, final String remotePort) {
    if (!quote_queried) {
      writer.appendLine("rosbridge_get_quote(\"" + remoteIP + "\", " + remotePort + ")");
      quote_queried = true;
    }
  }

  @Override
  public void generateScript(ScriptWriter writer) {
    // WORKAROUND:
    // Reset this once a new Program is compiled. Otherwise, if we create a program, save it,
    // create another program the static variable will still be true, although the quote string was
    // never queried in this particular program. If we had a proper way to check whether this
    // particular program contains a node of this type, this could be implemented in a cleaner way.
    quote_queried = false;

    // Append JSON Parser
    writer.appendRaw(LoadResourceFile("json_parser.script"));

    // generate quote here!
    writer.appendLine("# get quote for json parsing");
    writer.appendLine("def rosbridge_get_quote(remoteIP, remotePort):");
    writer.assign("quote_socket_connected", "socket_open(remoteIP, remotePort, \"quotesocket\")");
    writer.ifCondition("quote_socket_connected == False");
    writer.appendLine(
        "popup(\"Could not connect to rosbridge at \" + remoteIP + \":\" + to_str(remotePort) + \". Following ROS calls will not work! Check your connection setup and make sure the rosbridge is actually running.\", \"Connection failed\", error=True, blocking=True)");
    writer.end();
    String call_time = "{\"op\":\"call_service\", \"service\":\"/rosapi/get_time\"}";
    byte[] bytes = call_time.getBytes();
    char a;
    for (int j = 0; j < bytes.length; j++) {
      a = (char) bytes[j];
      writer.appendLine("socket_send_byte(" + bytes[j] + ", \"quotesocket\")\t# " + a);
    }
    writer.assign("response", "socket_read_string(\"quotesocket\")");
    writer.ifCondition("str_len(response) > 2");
    writer.appendLine("textmsg(response)");
    writer.assign("quote", "str_at(response, 1)");
    writer.assign("bounds", "json_getElement(inp_string=response, name=\"result\")");
    writer.assign("quote_result", "str_sub(response, bounds[2], bounds[3]-bounds[2]+1)");
    writer.ifCondition("quote_result != \"true\"");
    writer.appendLine(
        "popup(\"Parsing quote from rosbridge answer failed! Make sure that the rosbridge is actually running at \" + remoteIP + \":\" + to_str(remotePort) + \".\", \"Parsing quote char failed\", error=True, blocking=True)");
    writer.end();
    writer.elseCondition();
    writer.appendLine(
        "popup(\"Could not receive quote char from rosbridge. Make sure that the rosbridge is actually running at \" + remoteIP + \":\" + to_str(remotePort) + \".\", \"Did not receive quote char\", error=True, blocking=True)");
    writer.end(); // if response
    writer.appendLine("socket_close(\"quotesocket\")");
    writer.end(); // function definition
  }

  public MasterPair[] loadMastersFromModel() {
    String[] masters = model.get(MASTERS_KEY, DEFAULT_MASTERS);
    MasterPair[] items = new MasterPair[masters.length];
    for (int i = 0; i < masters.length; i++) {
      System.out.println("Found master in model: " + masters[i]);
      items[i] = MasterPair.fromString(masters[i]);
    }
    return items;
  }

  public MasterPair[] getMastersList() {
    return masters;
  }

  public String LoadResourceFile(String fileName) {
    if ("".contentEquals(fileName)) {
      return "# empty filename to load from resource";
    }
    URL fileURL = getClass().getClassLoader().getResource(fileName);
    if (fileURL == null) {
      return "# " + fileName + " not found!";
    }

    InputStreamReader inputReader =
        new InputStreamReader(getClass().getResourceAsStream(fileURL.getPath()));
    BufferedReader reader = new BufferedReader(inputReader);

    String line = reader.lines().collect(Collectors.joining("\n"));

    return line;
  }

  public void setMasterList(MasterPair[] data) {
    masters = data;

    String[] mastersStrings = new String[masters.length];
    for (int i = 0; i < mastersStrings.length; i++) {
      mastersStrings[i] = data[i].toString();
    }
    model.set(MASTERS_KEY, mastersStrings);
  }
}
