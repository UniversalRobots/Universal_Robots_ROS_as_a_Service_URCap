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
 * \date    2021-01-27
 *
 */
//----------------------------------------------------------------------

package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JPanel;
import org.json.JSONArray;
import org.json.JSONObject;

public class TopicSubscriberProgramNodeContribution extends RosTaskProgramSuperNodeContribution {
  private final TopicSubscriberProgramNodeView view;

  public TopicSubscriberProgramNodeContribution(
      ProgramAPIProvider apiProvider, TopicSubscriberProgramNodeView view, DataModel model) {
    super(apiProvider, model, TaskType.SUBSCRIBER, LeafDataDirection.INPUT);
    this.view = view;
  }

  public void onTopicSelection(final String topic,
      final ContributionProvider<TopicSubscriberProgramNodeContribution> provider) {
    JSONArray typedefs = null;

    if (!topic.equals(getMsg())) { // topic in model is not the selected one
      String topic_type = getTopicType(topic);
      typedefs = getTopicLayout(topic_type);

      JSONObject layout = new JSONObject();
      layout.put("layout", typedefs);
      onMsgSelection(topic, layout.toString(), MSG_KEY, MSG_LAYOUT_KEY);
      ID = topic.replaceAll("/", "_");
    } else {
      try {
        typedefs = getMsgLayout().getJSONArray("layout");
        ID = getMsg().replaceAll("/", "_");
      } catch (Exception e) {
        System.err.println("openView: Error: " + e);
      }
    }

    view.cleanPanel();
    createTreeView(typedefs);
  }

  private void createTreeView(JSONArray layout) {
    if (layout == null) {
      return;
    }
    Map<String, String> request_types = getMsgFields(layout, 0);
    JPanel panel = view.createMsgPanel();
    for (Entry<String, String> pair : request_types.entrySet()) {
      if (isSimpleType(pair.getValue())) {
        System.out.println("Primitive type: " + pair.getValue());
        view.addMsgField(pair.getKey(), pair.getValue(), panel);
      } else {
        tree = createMsgTreeLayout(layout, tree_direction);
        view.addTreePanel(tree, panel);
        break;
      }
    }
  }

  @Override
  public void openView() {
    updateVariables();
    // TODO on Task creation needs to go out of focus once to get TopicList...
    view.setMasterComboBoxItems(getMastersList());
    view.setTopicComboBoxItems(getMsgList());
    MasterPair model = new MasterPair(getMaster(), getPort());
    boolean masterExists = false;
    for (String i : getMastersList()) {
      masterExists |= (model.toString().compareTo(i) == 0);
    }
    view.setMasterComboBoxSelection(model.toString());
    // TODO Find a better solution for this
    if (!masterExists) {
      onMasterSelection(getMastersList()[0]);
    }
    view.setTopicComboBoxSelection(getMsg());

    LoadValueNode base_node = loadValuesToTree(null, getMsgValue(), "msg_base");
    try {
      System.out.println("detected values: " + base_node.toString());
      setTreeValues(base_node, tree);
    } catch (Exception e) {
      System.err.println("Error: " + e);
    }
  }

  @Override
  public void updateMsgList() {
    view.setTopicComboBoxItems(getMsgList());
  }

  public void updateView() {
    view.setMasterComboBoxSelection(new MasterPair(getMaster(), getPort()).toString());
    view.setTopicComboBoxSelection(getMsg());
  }

  @Override
  public void closeView() {
    onCloseView();
  }

  @Override
  public boolean isDefined() {
    // return getTopic() != "";
    return true;
  }

  private void generateElementParser(String element_name, String source_var, String target_var,
      boolean targetIsGlobal, boolean numericTarget, ScriptWriter writer) {
    writer.assign("bounds", "json_getElement(" + source_var + ", \"" + element_name + "\")");
    String l_val = new String();
    String r_val = new String();

    if (!targetIsGlobal) {
      l_val = "local ";
    }
    l_val += target_var;

    if (numericTarget) {
      r_val = "to_num(";
    }
    r_val += "str_sub(" + source_var + ", bounds[2], bounds[3]-bounds[2]+1)";
    if (numericTarget) {
      r_val += ")";
    }
    writer.assign(l_val, r_val);
    writer.assign(
        source_var, "ri_reduceString(" + source_var + ", bounds[0], bounds[3]-bounds[0]+1)");
  }

  private void generateValueNodeInstallation(LoadValueNode node, ScriptWriter writer) {
    if (node.getParent() == null) {
      return; // this can occur for basenode -> nothing todo here
    }
    String source_variable = node.getParent().getExtractionFunctionName();
    String target_variable;
    if (node.getVariableUsed()) {
      target_variable = node.getValue();
    } else {
      target_variable = node.getExtractionFunctionName();
    }
    String json_key = node.getName();
    generateElementParser(json_key, source_variable, target_variable, node.getVariableUsed(),
        node.isNumericType(), writer);
  }

  @Override
  public void generateScript(ScriptWriter writer) {
    final String globalvar = "subscriptMsg" + ID;
    final String sockname = "subscriber" + ID;

    writer.appendLine("# init subscriber");
    writer.appendLine(globalvar + " = \"\"");

    System.out.println("Subscriber generate subscriber Script");
    // add parser for values!
    LoadValueNode base_node =
        loadValuesToTree(null, new JSONObject(buildJsonString(tree, false)), "l_msg");
    base_node.setExtractionAdded();
    List<LoadValueNode> nodes_list = base_node.getVariableUsingNodes(null);
    ListIterator<LoadValueNode> iterator = nodes_list.listIterator();

    writer.defineFunction("parseSubscript" + ID); // add function definition
    if (!nodes_list.isEmpty()) { // no variables needed -> nothing todo here
      writer.assign("local l_msg", globalvar);
      writer.assign("local bounds", "[0, 0, 0, 0]");

      while (iterator.hasNext()) {
        List<LoadValueNode> path =
            iterator.next()
                .getParentPath(); // get path of parents (at least one element contained!)
        System.out.println(
            "Generate Script for " + path.get(path.size() - 1).getExtractionFunctionName());
        ListIterator<LoadValueNode> parent = path.listIterator();
        while (parent.hasNext()) { // for each node in path
          LoadValueNode element = parent.next();
          System.out.println("Generate Extraction for " + element.getExtractionFunctionName());
          if (!element.isExtractionAdded()) {
            generateValueNodeInstallation(element, writer);
            element.setExtractionAdded();
          } else {
            System.out.println("... already exists!");
          }
        }
      }
    }
    writer.end(); // end function definition to parse subscript

    String json = "{\"op\": \"subscribe\", \"topic\": \"" + getMsg() + "\"}";
    writer.appendLine(
        "socket_open(\"" + getMaster() + "\", " + getPort() + ", \"" + sockname + "\")");
    writer.appendLine("socket_send_line(\"" + urscriptifyJson(json) + "\", \"" + sockname + "\")");

    writer.appendLine("local msg = \" \"");
    writer.appendLine("local tmp = \" \"");
    writer.whileCondition(globalvar + " == \"\"");
    writer.assign("tmp", "socket_read_string(\"" + sockname + "\")");
    writer.ifNotCondition("str_empty(tmp)");
    writer.appendLine("local bounds = json_getElement(tmp, \"msg\")");
    writer.appendLine("msg = str_sub(tmp, bounds[2], bounds[3]-bounds[2]+1)");
    writer.assign(globalvar, "msg");
    writer.end(); // if-clause
    writer.sync();
    writer.end(); // while loop
    writer.appendLine("textmsg(\"subscription is: \", " + globalvar + ")");

    writer.appendLine("parseSubscript" + ID + "()");

    json = "{\"op\": \"unsubscribe\", \"topic\": \"" + getMsg() + "\"}";
    writer.appendLine("socket_send_line(\"" + urscriptifyJson(json) + "\", \"" + sockname + "\")");
    writer.appendLine("socket_close(\"" + sockname + "\")");
  }
}
