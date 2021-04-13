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

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.domain.variable.Variable;
import com.ur.urcap.api.domain.variable.VariableFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class RosTaskProgramSuperNodeContribution implements ProgramNodeContribution {
  protected final ProgramAPIProvider apiProvider;
  protected final DataModel model;
  protected final UndoRedoManager undoRedoManager;
  protected boolean useVar = false;
  protected int varCounter = 0;
  protected Collection<Variable> varCollection;
  protected Object[] varList;

  protected final VariableFactory variableFactory;
  protected static final String MASTER_KEY = "MASTER";
  protected static final String PORT_KEY = "PORT";
  protected static final String MSG_KEY = "MSG";
  protected static final String MSG_VALUE_KEY = "MSG_VALUE";
  protected static final String MSG_LAYOUT_KEY = "MSG_LAYOUT";

  protected static final String DEFAULT_MASTER = "192.168.56.1";
  protected static final String DEFAULT_PORT = "9090";
  protected static final String DEFAULT_MSG = "";
  protected static final String DEFAULT_MSG_VALUE = "";
  protected static final String DEFAULT_MSG_LAYOUT = "";

  protected String ID; // identifier to reuse sockets

  public RosTaskProgramSuperNodeContribution(ProgramAPIProvider apiProvider, DataModel model) {
    this.apiProvider = apiProvider;
    this.variableFactory = apiProvider.getProgramAPI().getVariableModel().getVariableFactory();
    this.model = model;
    this.varCollection = apiProvider.getProgramAPI().getVariableModel().getAll();
    this.varList = varCollection.toArray();
    this.undoRedoManager = this.apiProvider.getProgramAPI().getUndoRedoManager();

    ID = getMsg().replaceAll("/", "_");
  }

  public void updateVariables() {
    System.out.println("### updateVariables");
    this.varCollection = apiProvider.getProgramAPI().getVariableModel().getAll();
    this.varList = varCollection.toArray();
  }

  public MasterPair getMaster() {
    return new MasterPair(getMasterIP(), getMasterPort());
  }

  @Override
  public void openView() {
    System.out.println("# openView");
  }

  @Override
  public String getTitle() {
    String title = "";
    title += getMsg();
    return title;
  }

  protected abstract String[] getMsgLayoutKeys();
  protected abstract String[] getMsgLayoutDirections();
  protected abstract String getMsgTypeRequestString(final String topic_name);
  protected abstract String getMsgListRequestString();
  protected abstract String getMsgListResponsePlaceholder();
  protected abstract String[] getMsgLayoutRequestStrings(final String msg_type);

  public JSONArray getTopicStructure() {
    System.out.println("## getTopicStructure");
    JSONArray structure = new JSONArray();

    JSONArray layout = getMsgLayout();
    JSONArray values = getMsgValues();

    String[] msg_layout_keys = getMsgLayoutKeys();
    String[] msg_layout_directions = getMsgLayoutDirections();

    for (int i = 0; i < msg_layout_keys.length; i++) {
      JSONObject obj = null;
      obj = new JSONObject();
      obj.put("name", msg_layout_keys[i]);
      obj.put("direction", msg_layout_directions[i]);
      obj.put("layout", layout.get(i));
      obj.put("values", values.get(i));
      structure.put(obj);
    }

    System.out.println("Topic structure: " + structure.toString(2));

    return structure;
  }

  public void onMasterSelection(final String selected_master) {
    System.out.println("### onMasterSelection");
    final MasterPair master = MasterPair.fromString(selected_master);
    if (getMasterIP().equals(master.getIp())
        && getMasterPort().equals(master.getPort())) { // no changes
      return;
    }
    System.out.println("Set model master to " + master.toString());
    undoRedoManager.recordChanges(new UndoableChanges() {
      @Override
      public void executeChanges() {
        model.set(MASTER_KEY, master.getIp());
        model.set(PORT_KEY, master.getPort());
      }
    });
  }

  @Override
  public void closeView() {
    System.out.println("### onCloseView");
  }

  @Override
  public boolean isDefined() {
    boolean result = true;
    String[] msg_layout_keys = getMsgLayoutKeys();
    for (int i = 0; i < msg_layout_keys.length; i++) {
      result &= !model.get(MSG_LAYOUT_KEY + "_" + msg_layout_keys[i], DEFAULT_MSG_LAYOUT)
                     .equals(DEFAULT_MSG_LAYOUT);
    }
    result &= !getMsg().equals(DEFAULT_MSG);
    return result;
  }

  public void onMsgSelection(final String
          topic) { //, final String layout, final String msg_key, final String layout_key) {
    System.out.println("### onMsgSelection");
    undoRedoManager.recordChanges(new UndoableChanges() {
      @Override
      public void executeChanges() {
        model.set(MSG_KEY, topic);
      }
    });
    updateTopicStructure(topic);
  }

  public void updateTopicStructure(final String new_topic) {
    System.out.println("#### updateTopicStructure");
    String topic = getMsg();
    String topic_type = queryTopicType(topic);
    ID = topic.replaceAll("/", "_");
    final JSONArray typedefs = queryTopicLayout(topic_type);

    System.out.println("LAYOUT: " + typedefs);

    String[] msg_layout_keys = getMsgLayoutKeys();
    for (int i = 0; i < msg_layout_keys.length; i++) {
      final JSONArray layout = typedefs.getJSONArray(i);
      final String model_key = MSG_LAYOUT_KEY + "_" + msg_layout_keys[i];
      undoRedoManager.recordChanges(new UndoableChanges() {
        @Override
        public void executeChanges() {
          // System.out.println("Saving layout for " + model_key + ":\n" + layout.toString(2) );
          model.set(model_key, layout.toString());
        }
      });
    }
  }

  // methods used for building of json String from TreeModel created with user
  public String buildJsonString(final String identifier) {
    return buildJsonString(false, identifier);
  }

  public String buildJsonString(final boolean readable_vars, final String identifier) {
    System.out.println("# buildJsonString");
    try {
      JSONArray values_all = getMsgValues();
      JSONObject values = null;
      String[] msg_layout_keys = getMsgLayoutKeys();
      for (int i = 0; i < msg_layout_keys.length; i++) {
        if (msg_layout_keys[i] == identifier) {
          values = values_all.getJSONObject(i).getJSONObject("values");
          break;
        }
      }

      Objects.requireNonNull(values, "No values found for key " + identifier);
      System.out.println(values.toString());

      String output = values.toString();

      if (readable_vars) {
        output = output.toString().replaceAll("\\{\\\"-\\+useVar\\+-\\\":\\\"([^\\\"]*)\\\"\\}",
            "\"-+useVar+- + to_str($1) + -+useVar+-\"");
        output = output.toString().replaceAll("\\{\\\"-\\+useVarNum\\+-\\\":\\\"([^\\\"]*)\\\"\\}",
            "\"-+useVar+- + to_str($1) + -+useVar+-\"");
      }
      System.out.println(output);
      return output;

    } catch (Exception e) {
      System.err.println("buildJsonString: Error: " + e);
    }
    return "{}";
  }

  protected String getMasterIP() {
    return model.get(MASTER_KEY, DEFAULT_MASTER);
  }

  protected String getMsg() {
    return model.get(MSG_KEY, DEFAULT_MSG);
  }

  protected String getMasterPort() {
    return model.get(PORT_KEY, DEFAULT_PORT);
  }

  JSONObject getMsgValue(final String identifier) {
    JSONArray values_all = getMsgValues();
    System.out.println("All values: " + values_all);
    JSONObject values = null;
    String[] msg_layout_keys = getMsgLayoutKeys();
    for (int i = 0; i < msg_layout_keys.length; i++) {
      if (msg_layout_keys[i] == identifier) {
        try {
          values = values_all.getJSONObject(i);
        } catch (org.json.JSONException e) {
          System.err.println("getMsgValue: Error: " + e);
        }

        break;
      }
    }
    return values;
  }

  /*!
   * \brief Get all message values currently stored in the model.
   *
   * \returns The array will have as many entries, as the saved model has layout_keys. Each entry
   * will contain the full value specifications for the underlying message structure (e.g. message,
   * ServiceRequest, ...)
   */
  protected JSONArray getMsgValues() {
    JSONArray arr = new JSONArray();
    String[] msg_layout_keys = getMsgLayoutKeys();
    for (int i = 0; i < msg_layout_keys.length; i++) {
      JSONObject obj = null;
      String value_str = model.get(MSG_VALUE_KEY + "_" + msg_layout_keys[i], DEFAULT_MSG_VALUE);
      if (value_str != "") {
        try {
          obj = new JSONObject(value_str);
          arr.put(obj);
        } catch (org.json.JSONException e) {
          System.err.println("getMsgValue: Error: " + e);
        }
      } else {
        arr.put(new JSONObject());
      }
    }
    System.out.println("VALUES: " + arr);
    return arr;
  }

  /*!
   * \brief Creates a JSONArray representation of the message layout stored inside the model
   *
   * \returns A valid JSONArray of the currently stored model.
   */
  protected JSONArray getMsgLayout() {
    System.out.println("# getMsgLayout");
    JSONArray arr = new JSONArray();
    String[] msg_layout_keys = getMsgLayoutKeys();
    for (int i = 0; i < msg_layout_keys.length; i++) {
      JSONArray obj = null;
      String layout_str = model.get(MSG_LAYOUT_KEY + "_" + msg_layout_keys[i], DEFAULT_MSG_LAYOUT);
      System.out.println("Layout: " + layout_str);
      // If no model is set, simply return a null object.
      if (!layout_str.equals("")) {
        try {
          obj = new JSONArray(layout_str);
          arr.put(obj);
        } catch (org.json.JSONException e) {
          System.err.println("getMsgLayout: Error: " + e);
        }
      }
    }
    return arr;
  }

  //  protected double getModelValueDouble(String key, String def_val) {
  //    return Double.parseDouble(model.get(key, def_val));
  //  }
  //
  //  protected int getModelValueInt(String key, String def_val) {
  //    return Integer.parseInt(model.get(key, def_val));
  //  }
  //
  //  protected String getModelValueString(String key, String def_val) {
  //    return model.get(key, def_val);
  //  }

  protected String[] getMastersList() {
    System.out.println("### getMastersList");
    List<String> items = new ArrayList<String>();
    // Add the master currently stored
    System.out.println("Adding stored master: <" + getMaster().toString() + ">");
    items.add(getMaster().toString());
    try {
      Objects.requireNonNull(getInstallation(), "InstallationNode not found");
      MasterPair[] pairs = getInstallation().getMastersList();
      Objects.requireNonNull(pairs, "pairs empty");
      for (int i = 0; i < pairs.length; i++) {
        // We might have added one master from the installation already, as it was stored
        // previously.
        if (!getMaster().equals(pairs[i])) {
          System.out.println("Adding master: <" + pairs[i].toString() + ">");
          items.add(pairs[i].toString());
        }
      }
    } catch (Exception e) {
      System.err.println("getMastersList: Error: " + e);
    }
    String[] arr = new String[items.size()];
    return items.toArray(arr);
  }

  protected void rosbridgeSend(String msg, DataOutputStream out) {
    System.out.println("### rosbridgeSend");
    try {
      Objects.requireNonNull(out, "stream null");
      Objects.requireNonNull(msg, "msg null");

      out.write(msg.getBytes("US-ASCII"));

      System.out.println("Sent: " + msg);
    } catch (IOException e) {
      System.err.println("rosbridgeSend: IO Error: " + e);
    } catch (java.lang.Exception e) {
      System.err.println("rosbridgeSend: Error: " + e);
    }
  }

  protected String rosbridgeReceive(DataInputStream in) {
    System.out.println("### rosbridgeReceive");
    try {
      Objects.requireNonNull(in, "socket null");
      // TODO define timeout
      byte[] messageByte = new byte[1000];
      boolean end = false;
      boolean inString = false;
      int open_braces = 0;
      int closed_braces = 0;
      String dataString = "";

      while (!end) {
        int bytesRead = in.read(messageByte);
        String substring = new String(messageByte, 0, bytesRead);

        for (int i = 0; i < substring.length(); i++) {
          if (substring.charAt(i) == '\"') {
            if (inString) {
              inString = false;
            } else {
              inString = true;
            }
          }
          if (substring.charAt(i) == '{') {
            if (!inString) {
              open_braces += 1;
            }
          }
          if (substring.charAt(i) == '}') {
            if (!inString) {
              closed_braces += 1;
            }
          }
        }
        dataString += substring;
        end = open_braces == closed_braces && open_braces > 0;
      }
      // System.out.println("Received: " + dataString);
      return dataString;
    } catch (IOException e) {
      System.err.println("rosbridgeReceive: IOError: " + e);
    } catch (Exception e) {
      System.err.println("rosbridgeReceive: Error: " + e);
    }
    return null;
  }

  protected void rosbridgeSendOnly(String msg) {
    String hostIp = getMasterIP();
    int portNr = Integer.parseInt(getMasterPort());

    try {
      Socket socket = new Socket();
      int timeout = 5 * 100;
      socket.connect(new InetSocketAddress(hostIp, portNr), timeout);
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());

      if (socket.isConnected()) {
        rosbridgeSend(msg, out);
      }

      out.flush();
      out.close();
      socket.close();
    } catch (IOException e) {
      System.err.println("rosbridgeSendOnly: IO Exception:" + e);
    }
  }

  protected JSONObject rosbridgeRequest(String topic_string) {
    String hostIp = getMasterIP();
    int portNr = Integer.parseInt(getMasterPort());

    JSONObject json_response = null;
    try {
      Socket socket = new Socket();
      int timeout = 5 * 100;
      socket.connect(new InetSocketAddress(hostIp, portNr), timeout);

      if (socket.isConnected()) {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        rosbridgeSend(topic_string, out);
        out.flush();
        String response = rosbridgeReceive(in);
        out.close();
        in.close();
        try {
          Objects.requireNonNull(response, "Response null");
          json_response = new JSONObject(response);
        } catch (org.json.JSONException e) {
          System.err.println("rosbridgeRequest: JSON-Error: " + e);
        } catch (Exception e) {
          System.err.println("rosbridgeRequest: Error: " + e);
        }
      }
      socket.close();
    } catch (IOException e) {
      System.err.println("rosbridgeRequest: IO Exception:" + e);
    }
    return json_response;
  }

  protected String[] queryMsgList() {
    System.out.println("### getMsgList");
    String[] items = new String[1];
    items[0] = getMsg();

    String request_string = getMsgListRequestString();

    try {
      // JSON parsing
      JSONObject json_response = rosbridgeRequest(request_string);
      Objects.requireNonNull(json_response, "Response null");
      JSONArray msgs =
          json_response.getJSONObject("values").getJSONArray(getMsgListResponsePlaceholder());
      items = new String[msgs.length() + 1];
      items[0] = " ";
      for (int i = 0, l = msgs.length(); i < l; ++i) {
        items[i + 1] = msgs.getString(i);
      }
    } catch (org.json.JSONException e) {
      System.err.println("getMsgList: JSON-Error: " + e);
    } catch (java.lang.Exception ex) {
      System.err.println("getMsgList: Error: " + ex);
    }

    return items;
  }

  protected String queryTopicType(String topic_name) {
    try {
      Objects.requireNonNull(topic_name, "Topicname null");
      System.out.println("TopicName: " + topic_name);

      String request_string = getMsgTypeRequestString(topic_name);

      JSONObject json_response = rosbridgeRequest(request_string);
      Objects.requireNonNull(json_response, "Response null");
      return json_response.getJSONObject("values").getString("type");
    } catch (org.json.JSONException e) {
      System.err.println("getTopicType: JSON-Error: " + e);
    } catch (Exception e) {
      System.err.println("getTopicType: Error: " + e);
    }
    return null;
  }

  protected JSONArray queryTopicLayout(String topic_type) {
    try {
      JSONArray resp = new JSONArray();
      Objects.requireNonNull(topic_type, "TopicType null");

      String[] request_strings = getMsgLayoutRequestStrings(topic_type);

      for (int i = 0; i < request_strings.length; i++) {
        JSONObject json_response = rosbridgeRequest(request_strings[i]);
        Objects.requireNonNull(json_response, "Response null");
        resp.put(json_response.getJSONObject("values").getJSONArray("typedefs"));
      }
      return resp;
    } catch (org.json.JSONException e) {
      System.err.println("getTopicLayout: JSON-Error: " + e);
    } catch (Exception e) {
      System.err.println("getTopicLayout: Error: " + e);
    }
    return null;
  }

  public void updateModel(final String name, final JSONObject obj) {
    try {
      Objects.requireNonNull(obj, "JSON Object of msg null");
    } catch (Exception e) {
      System.err.println("updateModel: Error: " + e);
      return;
    }

    undoRedoManager.recordChanges(new UndoableChanges() {
      @Override
      public void executeChanges() {
        model.set(MSG_VALUE_KEY + "_" + name, obj.toString());
        System.out.println("Set MSG_VALUE of " + name + " to " + obj.toString());
      }
    });
  }

  protected RosbridgeInstallationNodeContribution getInstallation() {
    return this.apiProvider.getProgramAPI().getInstallationNode(
        RosbridgeInstallationNodeContribution.class);
  }

  public int getNrOfVariablesInInstall() {
    return varCollection.size();
  }

  public Collection<Variable> getVarCollection() {
    return varCollection;
  }

  public Variable getSelectedVariable(String varName) {
    Iterator<Variable> iterator = varCollection.iterator();
    while (iterator.hasNext()) {
      Variable var = iterator.next();
      if (var.getDisplayName().equals(varName)) {
        return var;
      }
    }

    return null;
  }

  protected String urscriptifyJson(String json) {
    // replace quotes
    String tmp1 = json.replaceAll("\"", "\" + quote + \"");
    // add variable handling
    String tmp2 = tmp1.replaceAll("\" \\+ quote \\+ \"-\\+useVar\\+-", "\"");
    String tmp3 = tmp2.replaceAll("-\\+useVar\\+-\" \\+ quote \\+ \"", "\"");
    String urscriptified = tmp3.replaceAll("-\\+useVarNum\\+-\" \\+ quote \\+ \"", "\"");
    return urscriptified;
  }

  // variable setting via UR script (maybe in superclass)
  // TODO must be adapted for handling multiple variables
  public void setVariableViaURscript(ScriptWriter writer, String value, String name) {
    Variable variable = getSelectedVariable(name);
    System.out.println("# setVariableViaURscript");
    if (variable != null) {
      String resolvedVariableName = writer.getResolvedVariableName(variable);
      writer.assign(variable, value);
      System.out.println("resolved variable:  " + resolvedVariableName);
      System.out.println("variable:  " + variable);
    }
  }
}
