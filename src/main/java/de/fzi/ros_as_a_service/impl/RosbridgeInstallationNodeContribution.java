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
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

public class RosbridgeInstallationNodeContribution implements InstallationNodeContribution {
  private static final String HOST_IP = "host_ip";
  private static final String PORT_NR = "port_nr";
  private static final String DEFAULT_IP = "192.168.56.1";
  private static final String DEFAULT_PORT = "9090";
  private DataModel model;
  private final RosbridgeInstallationNodeView view;
  private final KeyboardInputFactory keyboardFactory;
  private boolean quote_queried = false;

  public RosbridgeInstallationNodeContribution(
      InstallationAPIProvider apiProvider, RosbridgeInstallationNodeView view, DataModel model) {
    this.keyboardFactory =
        apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
    this.model = model;
    this.view = view;
  }

  @Override
  public void openView() {}

  @Override
  public void closeView() {}

  public boolean isDefined() {
    return !getHostIP().isEmpty();
  }

  public void generateQuoteQueryScript(ScriptWriter writer) {
    if (!quote_queried) {
      writer.appendLine("rosbridge_get_quote()");
      quote_queried = true;
    }
  }

  @Override
  public void generateScript(ScriptWriter writer) {
    // writer.appendLine("MASTER1_IP = \"" + getHostIP() + "\"");
    // writer.appendLine("MASTER1_PORT = " + getCustomPort());
    // writer.appendLine("socket_open(MASTER1_IP, MASTER1_PORT,
    // \"testserver\")");

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
    writer.defineFunction("rosbridge_get_quote");
    writer.appendLine(
        "socket_open(\"" + getHostIP() + "\", " + getCustomPort() + ", \"quotesocket\")");
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

  // IP helper functions
  public void setHostIP(String ip) {
    if ("".equals(ip)) {
      resetToDefaultIP();
    } else {
      model.set(HOST_IP, ip);
    }
  }

  public String getHostIP() {
    return model.get(HOST_IP, DEFAULT_IP);
  }

  // TODO Receive masters list from model
  public MasterPair[] getMastersList() {
    MasterPair[] items = new MasterPair[1];
    items[0] = new MasterPair(getHostIP(), getCustomPort());
    return items;
  }

  private void resetToDefaultIP() {
    model.set(HOST_IP, DEFAULT_IP);
  }

  public KeyboardTextInput getInputForIPTextField() {
    KeyboardTextInput keyboInput = keyboardFactory.createStringKeyboardInput();
    keyboInput.setInitialValue(getHostIP());
    return keyboInput;
  }

  public KeyboardInputCallback<String> getCallbackForIPTextField() {
    return new KeyboardInputCallback<String>() {
      @Override
      public void onOk(String value) {
        setHostIP(value);
        view.UpdateIPTextField(value);
      }
    };
  }

  // port helper functions
  public void setHostPort(String port) {
    if ("".equals(port)) {
      resetToDefaultPort();
    } else {
      model.set(PORT_NR, port);
    }
  }

  public String getCustomPort() {
    return model.get(PORT_NR, DEFAULT_PORT);
  }

  private void resetToDefaultPort() {
    model.set(PORT_NR, DEFAULT_PORT);
  }

  public KeyboardTextInput getInputForPortTextField() {
    KeyboardTextInput keyboInput = keyboardFactory.createIPAddressKeyboardInput();
    keyboInput.setInitialValue(getCustomPort());
    return keyboInput;
  }

  public KeyboardInputCallback<String> getCallbackForPortTextField() {
    return new KeyboardInputCallback<String>() {
      @Override
      public void onOk(String value) {
        setHostPort(value);
        view.UpdatePortTextField(value);
      }
    };
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
}
