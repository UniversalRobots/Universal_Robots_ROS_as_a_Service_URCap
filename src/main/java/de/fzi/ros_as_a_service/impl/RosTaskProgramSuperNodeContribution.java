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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class RosTaskProgramSuperNodeContribution implements ProgramNodeContribution {
  protected final ProgramAPIProvider apiProvider;
  protected final DataModel model;
  protected final UndoRedoManager undoRedoManager;
  protected boolean useVar = false;
  protected int varCounter = 0;
  protected final TaskType task;
  protected final LeafDataDirection tree_direction;
  protected Collection<Variable> varCollection;
  protected Object[] varList;

  protected final VariableFactory variableFactory;
  protected JTree tree;
  protected static final String SELECTED_VAR = "selectedVar";
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
  protected static final String DEFAULT_VAR = "";

  public static enum TaskType {
    PUBLISHER,
    SUBSCRIBER,
    SERVICECALL,
    ACTIONCALL,
    ACTIONSTATUS,
    ACTIONRESULT
  }
  ;
  protected String ID; // identifier to reuse sockets

  public RosTaskProgramSuperNodeContribution(
      ProgramAPIProvider apiProvider, DataModel model, TaskType task, LeafDataDirection direction) {
    this.apiProvider = apiProvider;
    this.variableFactory = apiProvider.getProgramAPI().getVariableModel().getVariableFactory();
    this.model = model;
    this.task = task;
    this.tree_direction = direction;
    this.varCollection = apiProvider.getProgramAPI().getVariableModel().getAll();
    this.varList = varCollection.toArray();
    this.undoRedoManager = this.apiProvider.getProgramAPI().getUndoRedoManager();
  }

  public void updateVariables() {
    System.out.println("### updateVariables");
    this.varCollection = apiProvider.getProgramAPI().getVariableModel().getAll();
    this.varList = varCollection.toArray();
  }

  public String getTitle() {
    String title = "";
    switch (task) {
      case PUBLISHER:
        title += "Pub. ";
        break;
      case SUBSCRIBER:
        title += "Sub. ";
        break;
      case SERVICECALL:
        title += "Call ";
        break;
      case ACTIONCALL:
        title += "Trig. ";
        break;
      case ACTIONSTATUS:
        title += "Status ";
        break;
      case ACTIONRESULT:
        title += "Get ";
        break;
      default:
    }
    title += getMsg();
    return title;
  }

  public void updateMsgList() {}

  public void onMasterSelection(final String selected_master) {
    System.out.println("### onMasterSelection");
    final MasterPair master = new MasterPair().fromString(selected_master);
    if (getMaster().equals(master.getIp()) && getPort().equals(master.getPort())) { // no changes
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
    updateMsgList();
  }

  public void onCloseView() {
    System.out.println("### onCloseView");
    undoRedoManager.recordChanges(new UndoableChanges() {
      @Override
      public void executeChanges() {
        model.set(MSG_VALUE_KEY, buildJsonString(tree));
        System.out.println("Set Model: " + buildJsonString(tree));
      }
    });
  }

  public void onMsgSelection(
      final String msg, final String layout, final String msg_key, final String layout_key) {
    System.out.println("### onMsgSelection");
    System.out.println("Set " + msg_key + " to " + msg);
    System.out.println("Set " + layout_key + " to " + layout);
    undoRedoManager.recordChanges(new UndoableChanges() {
      @Override
      public void executeChanges() {
        model.set(msg_key, msg);
        model.set(layout_key, layout);
      }
    });
  }

  public JTree createMsgTreeLayout(JSONArray msg_layout, LeafDataDirection direction) {
    System.out.println("### createMsgTreeLayout");
    JTree tree = null;
    try {
      Objects.requireNonNull(msg_layout, "msg layout undefined");
      JSONObject obj = (JSONObject) msg_layout.get(0);
      TreeNodeVector<Object> rootVector = getRoot(msg_layout, obj);
      tree = new JTree(rootVector);
      ValueNodeRenderer renderer = new ValueNodeRenderer(varCollection, direction);
      tree.setCellRenderer(renderer);
      // tree.setCellEditor(new TextFieldNodeEditor(tree, renderer));
      tree.setCellEditor(new ValueNodeEditor(tree, varCollection, direction));
      tree.setEditable(true);
    } catch (org.json.JSONException e) {
      System.err.println("createMsgTreeLayout: JSON-Error: " + e);
    } catch (Exception e) {
      System.err.println("createMsgTreeLayout: Error: " + e);
    }
    return tree;
  }

  public TreeNodeVector<Object> getRoot(JSONArray typedefs, JSONObject obj) {
    JSONObject obj_j = (JSONObject) typedefs.get(0);
    String type = obj_j.get("type").toString();
    TreeNodeVector<Object> root = new TreeNodeVector<Object>(type);
    getNextLevel(typedefs, obj, "", root, tree_direction);
    return root;
  }

  public void getNextLevel(JSONArray typedefs, JSONObject obj, String supertype,
      TreeNodeVector<Object> parentVector, LeafDataDirection direction) {
    JSONArray fieldtypes = obj.getJSONArray("fieldtypes");
    JSONArray fieldnames = obj.getJSONArray("fieldnames");

    for (int j = 0; j < typedefs.length(); j++) {
      Map<String, String> subTypes = new HashMap<String, String>();
      JSONObject obj_j = (JSONObject) typedefs.get(j);
      String type = obj_j.get("type").toString();

      for (int i = 0; i < fieldtypes.length(); i++) {
        String ft = fieldtypes.get(i).toString();
        String fn = fieldnames.get(i).toString();
        TreeNodeVector<Object> branchVector = new TreeNodeVector<Object>(fn + " (" + ft + ")");
        String[] superSplit = supertype.split("-");
        if (!isSimpleType(ft)) {
          if (ft.equals(type)) {
            parentVector.addElement(branchVector);
            if (!supertype.equals("")) {
              ft = supertype + "-" + ft;
            }
            getNextLevel(typedefs, obj_j, ft, branchVector, direction);
          }
        } else if (type.equals(superSplit[superSplit.length - 1])) {
          subTypes.put(fieldnames.getString(i), fieldtypes.getString(i));
        }
      }
      if (!subTypes.isEmpty()) {
        if (direction == LeafDataDirection.INPUT) {
          ValueInputNode[] nodes = new ValueInputNode[subTypes.size()];
          int cnt = 0;
          for (Entry<String, String> st : subTypes.entrySet()) {
            ValueInputNode leafNode = new ValueInputNode(st.getKey(), st.getValue(), " ");
            nodes[cnt] = leafNode;
            ++cnt;
          }
          parentVector.addElements(nodes);
        } else if (direction == LeafDataDirection.OUTPUT) {
          ValueOutputNode[] nodes = new ValueOutputNode[subTypes.size()];
          int cnt = 0;
          for (Entry<String, String> st : subTypes.entrySet()) {
            ValueOutputNode leafNode = new ValueOutputNode(
                st.getKey(), st.getValue(), false, getDefaultType(st.getValue()));
            nodes[cnt] = leafNode;
            ++cnt;
          }
          parentVector.addElements(nodes);
        }
      }
    }
  }

  private String getDefaultType(String type) {
    if (type.equals("int32") | type.equals("int64") | type.equals("uint32")
        | type.equals("uint64")) {
      return "0";
    } else if (type.equals("float64") | type.equals("float32")) {
      return "0.0";
    }
    // default for String
    return "default";
  }

  // methods used for building of json String from TreeModel created with user
  public String buildJsonString(JTree tree) {
    return buildJsonString(tree, false);
  }

  public String buildJsonString(JTree tree, boolean readable_vars) {
    try {
      Objects.requireNonNull(tree, "Json string can not be build, as the tree is null.");
      TreeModel treeModel = tree.getModel();
      Object tmRoot = treeModel.getRoot();
      JSONObject root = getJsonLevel(treeModel, tmRoot, readable_vars);
      return root.toString();
    } catch (Exception e) {
      System.err.println("buildJsonString: Error: " + e);
      return "{}";
    }
  }

  public JSONObject getJsonLevel(TreeModel treeModel, Object parent, boolean sendable_vars) {
    JSONObject jObj = new JSONObject(); // create JSON Object
    int childCount = treeModel.getChildCount(parent); // get number of Children for Parent TreeNode
    for (int i = 0; i < childCount; i++) { // for each Child
      Object child = treeModel.getChild(parent, i); // get Object of Child
      String name = getChildName(child.toString()); // get Name of this Child
      String type = getChildType(child.toString()); // get Type of this Child
      if (treeModel.isLeaf(child)) { // if Child is Leaf
        String val = " ";
        boolean usevar = false;
        DefaultMutableTreeNode treenode = (DefaultMutableTreeNode) child;
        Object node = treenode.getUserObject();
        if (node instanceof ValueInputNode) {
          val = ((ValueInputNode) node).getJson();
          usevar = ((ValueInputNode) node).getUseVariable();
        }
        try {
          if (usevar) {
            if (sendable_vars) {
              jObj.put(name,
                  "-+useVar+- + to_str(" + ((ValueInputNode) node).getValue() + ") + -+useVar+-");
            } else {
              jObj.put(name, new JSONObject(val));
            }
          } else if (!(node instanceof ValueInputNode)) { // ValueInputNode not using a variable
            jObj.put(name, "");
          } else if (type.equals("string")) {
            jObj.put(name, val);
          } else if (type.equals("int32") | type.equals("int64") | type.equals("uint32")
              | type.equals("uint64")) {
            jObj.put(name, Integer.parseInt(val));
          } else if (type.equals("float64")) {
            jObj.put(name, Double.parseDouble(val));
          }
        } catch (Exception e) {
          System.err.println("getJsonLevel: Error: " + usevar + " " + e);
          jObj.put(name, "");
        }

      } else { // If Child is not a Leaf
        jObj.put(name,
            getJsonLevel(treeModel, child,
                sendable_vars)); // put recursive call with child as parent
      }
    }
    return jObj; // return JSON Object
  }

  protected void setTreeValues(LoadValueNode base_node, JTree tree) {
    System.out.println("### setTreeValues");
    TreeModel treeModel = tree.getModel();
    Object tmRoot = treeModel.getRoot();
    setNodeValueLevel(base_node, treeModel, tmRoot);
  }

  protected void setNodeValueLevel(LoadValueNode node, TreeModel treeModel, Object parent) {
    int child_count = treeModel.getChildCount(parent);
    if (node.getChildren().size() != child_count) {
      System.err.println("Different ChildCount of Tree and Values");
      return;
    }
    for (int i = 0; i < child_count; ++i) { // for each child of parent in treeModel
      Object child = treeModel.getChild(parent, i); // get Object of child
      String name = getChildName(child.toString()); // get name of child
      for (int j = 0; j < node.getChildren().size(); j++) { // for each child of LoadValueNode
        if (node.getChildren().get(j).getName().equals(name)) { // if names match
          if (treeModel.isLeaf(child)) { // and child is leaf
            DefaultMutableTreeNode treenode = (DefaultMutableTreeNode) child;
            Object obj = treenode.getUserObject();
            if (obj instanceof ValueInputNode) {
              ((ValueInputNode) obj).setValue(node.getChildren().get(j).getValue());
              node.getChildren().get(j).setNumericType(((ValueInputNode) obj).isNumericType());
              if (obj instanceof ValueOutputNode) {
                ((ValueOutputNode) obj)
                    .setVariableUsed(node.getChildren().get(j).getVariableUsed());
              }
            }
          } else {
            setNodeValueLevel(node.getChildren().get(j), treeModel, child);
          }
          break;
        }
      }
    }
  }

  // TODO rename e.g. getLoadValueNodeTreeFromJSON
  protected LoadValueNode loadValuesToTree(LoadValueNode parent, JSONObject values, String name) {
    JSONArray keys = null;
    try {
      keys = values.names();
    } catch (Exception nptr) {
      System.out.println("loadValuesToTree: Error: " + nptr);
      return null;
    }

    LoadValueNode child = new LoadValueNode(parent, new ValueNode(name, ""));
    if (parent != null) {
      parent.addChild(child);
    }

    for (int i = 0; i < keys.length(); ++i) {
      try {
        JSONObject obj = values.getJSONObject(keys.get(i).toString());
        if (obj.names().get(0).toString().equals("-+useVar+-")) {
          System.out.println("detected use of variable in " + keys.get(i).toString());
          LoadValueNode new_child = new LoadValueNode(
              child, new ValueNode(keys.getString(i), getJsonObjectValue(obj, 0)));
          new_child.setVariableUsed(true);
          child.addChild(new_child);
          continue;
        } else if (obj.names().get(0).toString().equals("-+useVarNum+-")) {
          System.out.println("detected use of numeric variable in " + keys.get(i).toString());
          LoadValueNode new_child = new LoadValueNode(
              child, new ValueNode(keys.getString(i), getJsonObjectValue(obj, 0)));
          new_child.setVariableUsed(true);
          new_child.setNumericType(true);
          child.addChild(new_child);
          continue;
        }
        loadValuesToTree(child, obj, keys.getString(i));
        continue;
      } catch (Exception e) {
        System.err.println("Error: " + e);
      }
      try {
        LoadValueNode new_child = new LoadValueNode(
            child, new ValueNode(keys.getString(i), getJsonObjectValue(values, i)));
        child.addChild(new_child);
      } catch (Exception ex) {
        System.err.println("Error: " + ex);
      }
    }
    return child;
  }

  String getJsonObjectValue(JSONObject obj, int index) {
    JSONArray names = obj.names();
    // try parse element as number
    try {
      return obj.getNumber(names.getString(index)).toString();
    } catch (Exception ex) {
      System.err.println("Error: " + ex);
    }
    // try parse element as string
    try {
      return obj.getString(names.getString(index));
    } catch (Exception ex) {
      System.err.println("Error: " + ex);
    }
    return null;
  }

  // get negative value of installation variables name
  protected String getVarSubstitute(String val) {
    int index = Arrays.asList(varList).indexOf(getSelectedVariable(val));
    return "-".concat(Integer.toString(index + 1));
  }

  // get installation var name back
  protected String getVarOriginal(String val) {
    int intVal = Integer.parseInt(val);
    return varList[Math.abs(intVal) - 1].toString();
  }

  protected String getChildName(String input) {
    String[] typeSplit = input.split("\\[");
    String[] typeSplit_2 = typeSplit[1].split("\\s+");
    return typeSplit_2[0];
  }

  protected String getChildType(String input) {
    String[] typeSplit = input.split("\\(");
    String[] typeSplit_2 = typeSplit[1].split("\\)");
    return typeSplit_2[0];
  }

  protected String getChildVal(String input) {
    String[] typeSplit = input.split("\\s+");
    String[] typeSplit_2 = typeSplit[3].split("\\]");
    return typeSplit_2[0];
  }

  protected boolean isVariable(String input) {
    Iterator<Variable> iterator = varCollection.iterator();
    while (iterator.hasNext()) {
      Variable var = iterator.next();
      if (var.getDisplayName().equals(input)) {
        return true;
      }
    }
    return false;
  }

  protected String getMaster() {
    return model.get(MASTER_KEY, DEFAULT_MASTER);
  }

  protected String getMsg() {
    return model.get(MSG_KEY, DEFAULT_MSG);
  }

  protected String getPort() {
    return model.get(PORT_KEY, DEFAULT_PORT);
  }

  protected JSONObject getMsgValue() {
    JSONObject obj = null;
    String value = model.get(MSG_VALUE_KEY, DEFAULT_MSG_VALUE);
    try {
      obj = new JSONObject(value);
    } catch (org.json.JSONException e) {
      System.err.println("getMsgValue: Error: " + e);
    }
    return obj;
  }

  protected JSONObject getMsgLayout() {
    JSONObject obj = null;
    String value = model.get(MSG_LAYOUT_KEY, DEFAULT_MSG_LAYOUT);
    try {
      obj = new JSONObject(value);
    } catch (org.json.JSONException e) {
      System.err.println("getMsgValue: Error: " + e);
    }
    return obj;
  }

  protected boolean isSimpleType(String type) {
    if (type.equals("string") | type.equals("int32") | type.equals("int64") | type.equals("uint32")
        | type.equals("uint64") | type.equals("float64")) {
      return true;
    }
    return false;
  }

  protected double getModelValueDouble(String key, String def_val) {
    return Double.parseDouble(model.get(key, def_val));
  }

  protected int getModelValueInt(String key, String def_val) {
    return Integer.parseInt(model.get(key, def_val));
  }

  protected String getModelValueString(String key, String def_val) {
    return model.get(key, def_val);
  }

  protected Map<String, String> getMsgFields(JSONArray typedefs, int ind) {
    System.out.println("### getMsgFields");
    try {
      // JSON parsing
      JSONArray fieldnames = typedefs.getJSONObject(ind).getJSONArray("fieldnames");
      JSONArray fieldtypes = typedefs.getJSONObject(ind).getJSONArray("fieldtypes");

      Map<String, String> out = new HashMap<String, String>();
      assert (fieldnames.length() == fieldtypes.length());
      for (int i = 0, l = fieldnames.length(); i < l; i++) {
        out.put(fieldnames.getString(i), fieldtypes.getString(i));
      }
      return out;
    } catch (org.json.JSONException e) {
      System.err.println("JSON-Error: " + e);
    }
    return null;
  }

  protected String[] getMastersList() {
    System.out.println("### getMastersList");
    String[] items = new String[1];
    items[0] = getMaster();
    try {
      Objects.requireNonNull(getInstallation(), "InstallationNode not found");
      MasterPair[] pairs = getInstallation().getMastersList();
      Objects.requireNonNull(pairs, "pairs empty");
      items = new String[pairs.length];
      for (int i = 0; i < pairs.length; i++) {
        items[i] = pairs[i].toString();
      }
    } catch (Exception e) {
      System.err.println("getMastersList: Error: " + e);
    }
    return items;
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
    String hostIp = getMaster();
    int portNr = Integer.parseInt(getPort());

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
    String hostIp = getMaster();
    int portNr = Integer.parseInt(getPort());

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

  // TODO:  At least for Topics we get also a list of Types with that call
  protected String[] getMsgList() {
    System.out.println("### getMsgList");
    String[] items = new String[1];
    items[0] = "";

    String request_string = "";
    String response_placeholder = "";
    switch (task) {
      case PUBLISHER:
      case SUBSCRIBER:
        request_string = "{\"op\": \"call_service\",\"service\": \"/rosapi/topics\"}";
        response_placeholder = "topics";
        break;
      case SERVICECALL:
        request_string = "{\"op\": \"call_service\",\"service\": \"/rosapi/services\"}";
        response_placeholder = "services";
        break;
      case ACTIONCALL:
      case ACTIONSTATUS:
      case ACTIONRESULT:
        request_string = "{\"op\": \"call_service\",\"service\": \"/rosapi/action_servers\"}";
        response_placeholder = "action_servers";
        break;
      default:
        return items;
    }

    try {
      // JSON parsing
      JSONObject json_response = rosbridgeRequest(request_string);
      Objects.requireNonNull(json_response, "Response null");
      JSONArray msgs = json_response.getJSONObject("values").getJSONArray(response_placeholder);
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

  protected String getServiceType(String service_name) {
    try {
      Objects.requireNonNull(service_name, "ServiceName null");
      String request_string =
          "{\"op\": \"call_service\",\"service\":\"/rosapi/service_type\",\"args\":{\"service\":\""
          + service_name + "\"}}";
      JSONObject json_response = rosbridgeRequest(request_string);
      Objects.requireNonNull(json_response, "Response null");
      return json_response.getJSONObject("values").getString("type");
    } catch (org.json.JSONException e) {
      System.err.println("getServiceType: JSON-Error: " + e);
    } catch (Exception e) {
      System.err.println("getServiceType: Error: " + e);
    }
    return null;
  }

  protected JSONArray getServiceRequestLayout(String service_type) {
    try {
      Objects.requireNonNull(service_type, "ServiceType null");
      String request_string =
          "{\"op\": \"call_service\",\"service\": \"/rosapi/service_request_details\", \"args\":{\"type\":\""
          + service_type + "\"}}";
      JSONObject json_response = rosbridgeRequest(request_string);
      Objects.requireNonNull(json_response, "Response null");
      return json_response.getJSONObject("values").getJSONArray("typedefs");
    } catch (org.json.JSONException e) {
      System.err.println("getServiceRequestLayout: JSON-Error: " + e);
    } catch (Exception e) {
      System.err.println("getServiceRequestLayout: Error: " + e);
    }
    return null;
  }

  protected JSONArray getServiceResponseLayout(String service_type) {
    try {
      Objects.requireNonNull(service_type, "ServiceType null");
      String request_string =
          "{\"op\": \"call_service\",\"service\": \"/rosapi/service_response_details\", \"args\":{\"type\":\""
          + service_type + "\"}}";
      JSONObject json_response = rosbridgeRequest(request_string);
      Objects.requireNonNull(json_response, "Response null");
      return json_response.getJSONObject("values").getJSONArray("typedefs");
    } catch (org.json.JSONException e) {
      System.err.println("getServiceResponseLayout: JSON-Error: " + e);
    } catch (Exception e) {
      System.err.println("getServiceResponseLayout: Error: " + e);
    }
    return null;
  }
  protected String getTopicType(String topic_name) {
    try {
      Objects.requireNonNull(topic_name, "Topicname null");
      System.out.println("TopicName: " + topic_name);

      String request_string = "";
      switch (task) {
        case PUBLISHER:
        case SUBSCRIBER:
        case ACTIONCALL:
        case ACTIONSTATUS:
        case ACTIONRESULT:
          request_string =
              "{\"op\": \"call_service\",\"service\":\"/rosapi/topic_type\",\"args\":{\"topic\":\""
              + topic_name + "\"}}";
          break;
        case SERVICECALL:
          request_string =
              "{\"op\": \"call_service\",\"service\":\"/rosapi/service_type\",\"args\":{\"service\":\""
              + topic_name + "\"}}";
          break;
        default:
          return null;
      }

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

  protected JSONArray getTopicLayout(String topic_type) {
    try {
      Objects.requireNonNull(topic_type, "TopicType null");

      String request_string = "";
      switch (task) {
        case PUBLISHER:
        case SUBSCRIBER:
        case ACTIONCALL:
        case ACTIONSTATUS:
        case ACTIONRESULT:
          request_string =
              "{\"op\": \"call_service\",\"service\":\"/rosapi/message_details\", \"args\":{\"type\":\""
              + topic_type + "\"}}";
          break;
        case SERVICECALL:
          request_string =
              "{\"op\": \"call_service\",\"service\":\"/rosapi/service_request_details\", \"args\":{\"type\":\""
              + topic_type + "\"}}";
          break;
        default:
          return null;
      }
      JSONObject json_response = rosbridgeRequest(request_string);
      Objects.requireNonNull(json_response, "Response null");
      return json_response.getJSONObject("values").getJSONArray("typedefs");
    } catch (org.json.JSONException e) {
      System.err.println("getTopicLayout: JSON-Error: " + e);
    } catch (Exception e) {
      System.err.println("getTopicLayout: Error: " + e);
    }
    return null;
  }

  public void updateModel(final JSONObject obj) {
    try {
      Objects.requireNonNull(obj, "JSON Object of msg null");
    } catch (Exception e) {
      System.err.println("updateModel: Error: " + e);
      return;
    }

    undoRedoManager.recordChanges(new UndoableChanges() {
      @Override
      public void executeChanges() {
        model.set(MSG_VALUE_KEY, obj.toString());
        System.out.println("Set MSG_VALUE to " + obj.toString());
      }
    });
  }

  public void generateInstallationCodeContribution(ScriptWriter writer) {
    return;
  }

  public void generateInstallationContributionSkipped(ScriptWriter writer) {}

  public void setID(final String id) {
    this.ID = id;
  }
  public final String getID() {
    return ID;
  }

  protected RosbridgeInstallationNodeContribution getInstallation() {
    return this.apiProvider.getProgramAPI().getInstallationNode(
        RosbridgeInstallationNodeContribution.class);
  }

  public int getNrOfVariablesInInstall() {
    return varCollection.size();
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
    System.out.println("IN GENERATE SCRIPT");
    if (variable != null) {
      String resolvedVariableName = writer.getResolvedVariableName(variable);
      writer.assign(variable, value);
      System.out.println("resolved variable:  " + resolvedVariableName);
      System.out.println("variable:  " + variable);
    }
  }
}
