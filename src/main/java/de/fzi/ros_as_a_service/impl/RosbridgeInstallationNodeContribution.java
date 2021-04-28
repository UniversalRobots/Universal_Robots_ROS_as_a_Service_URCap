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
    writer.appendLine("socket_open(remoteIP, remotePort, \"quotesocket\")");
    String call_time = "{\"op\":\"call_service\", \"service\":\"/rosapi/get_time\"}";
    byte[] bytes = call_time.getBytes();
    char a;
    for (int j = 0; j < bytes.length; j++) {
      a = (char) bytes[j];
      writer.appendLine("socket_send_byte(" + bytes[j] + ", \"quotesocket\")\t# " + a);
    }
    writer.appendLine("local response = \" \"");
    writer.assign("response", "socket_read_string(\"quotesocket\")");
    writer.assign("quote", "str_at(response, 1)");
    writer.appendLine("socket_close(\"quotesocket\")");
    writer.end();
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
