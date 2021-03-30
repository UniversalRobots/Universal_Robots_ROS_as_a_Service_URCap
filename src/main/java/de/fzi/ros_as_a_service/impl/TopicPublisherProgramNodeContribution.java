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
 * \date    2021-01-26
 *
 */
//----------------------------------------------------------------------

package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.domain.variable.Variable;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JPanel;
import org.json.JSONArray;
import org.json.JSONObject;

public class TopicPublisherProgramNodeContribution extends RosTaskProgramSuperNodeContribution {
  private final TopicPublisherProgramNodeView view;

  public TopicPublisherProgramNodeContribution(
      ProgramAPIProvider apiProvider, TopicPublisherProgramNodeView view, DataModel model) {
    super(apiProvider, model, TaskType.PUBLISHER, LeafDataDirection.OUTPUT);
    this.view = view;
  }

  public void onTopicSelection(final String topic,
      final ContributionProvider<TopicPublisherProgramNodeContribution> provider) {
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
    };
    Map<String, String> request_types = getMsgFields(layout, 0);
    JPanel panel = view.createMsgPanel("Message:");
    for (Entry<String, String> pair : request_types.entrySet()) {
      if (isSimpleType(pair.getValue())) {
        System.out.println("Primitive type: " + pair.getValue());
        view.addMsgField(pair.getKey(), pair.getValue(), panel);
      } else {
        System.out.println("Add tree");
        tree = createMsgTreeLayout(layout, tree_direction);
        view.addTreePanel(tree, panel);
        break;
      }
    }
  }

  public void setvar(boolean bool) {
    useVar = bool;
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
    // TODO hack hack
    if (!masterExists) {
      System.out.println("former master not found");
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
    System.out.println("### updateView");
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

  public void setVariable(final Variable variable) {
    System.out.println("### setVariable");
    undoRedoManager.recordChanges(new UndoableChanges() {
      @Override
      public void executeChanges() {
        model.set(SELECTED_VAR, variable);
      }
    });
  }

  @Override
  public void generateScript(ScriptWriter writer) {
    // TODO: Create unique ID here
    final String sockname = "publisher_" + ID;

    String json = "{\"op\": \"advertise\", \"topic\": \"" + getMsg() + "\", \"type\": \""
        + getTopicType(getMsg()) + "\"}";
    String urscriptified = json.replaceAll("\"", "\" + quote + \"");

    writer.appendLine(
        "socket_open(\"" + getMaster() + "\", " + getPort() + ", \"" + sockname + "\")");
    writer.appendLine("socket_send_line(\"" + urscriptified + "\", \"" + sockname + "\")");
    useVar = false;

    json = "{\"op\": \"publish\", \"topic\": \"" + getMsg()
        + "\", \"msg\": " + buildJsonString(tree, true) + "}";

    writer.appendLine("local msg = \"" + urscriptifyJson(json) + "\"");
    writer.appendLine("socket_send_line(\"" + urscriptifyJson(json) + "\", \"" + sockname + "\")");
  }
}
