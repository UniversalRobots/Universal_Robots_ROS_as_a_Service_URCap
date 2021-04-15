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

import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.*;
import org.json.JSONObject;

public class TopicSubscriberProgramNodeContribution extends RosTaskProgramSuperNodeContribution {
  private final TopicSubscriberProgramNodeView view;

  public TopicSubscriberProgramNodeContribution(
      ProgramAPIProvider apiProvider, TopicSubscriberProgramNodeView view, DataModel model) {
    super(apiProvider, model);
    this.view = view;
  }

  @Override
  public String getTitle() {
    return "Sub. " + getMsg();
  }

  @Override
  protected String[] getMsgLayoutKeys() {
    return new String[] {"Data"};
  }

  @Override
  protected String[] getMsgLayoutDirections() {
    return new String[] {"in"};
  }

  @Override
  protected String getMsgTypeRequestString(final String topic_name) {
    return "{\"op\": \"call_service\",\"service\":\"/rosapi/topic_type\",\"args\":{\"topic\":\""
        + topic_name + "\"}}";
  }

  @Override
  protected String getMsgListRequestString() {
    return "{\"op\": \"call_service\",\"service\": \"/rosapi/topics\"}";
  }

  @Override
  protected String getMsgListResponsePlaceholder() {
    return "topics";
  }

  @Override
  protected String[] getMsgLayoutRequestStrings(final String msg_type) {
    return new String[] {
        "{\"op\": \"call_service\",\"service\":\"/rosapi/message_details\", \"args\":{\"type\":\""
        + msg_type + "\"}}"};
  }

  @Override
  public void openView() {
    // TODO: If we parametrize RosTaskSuperNodeContribution with the view type, we get a cyclic
    // parametrization. So, for now we keep solving this here, but there might be a better solution.
    super.openView();
    view.updateView(this);
  }

  private void generateElementParser(String element_name, String source_var, String target_var,
      boolean numericTarget, ScriptWriter writer) {
    writer.assign("bounds", "json_getElement(" + source_var + ", \"" + element_name + "\")");
    String l_val = new String();
    String r_val = new String();

    l_val += target_var;

    if (numericTarget) {
      r_val = "to_num(";
    }
    r_val += "str_sub(" + source_var + ", bounds[2], bounds[3]-bounds[2]+1)";
    if (numericTarget) {
      r_val += ")";
    }
    writer.assign(l_val, r_val);
    // writer.appendLine("textmsg(\"" + l_val + "\", " + l_val + ")");
    writer.assign(
        source_var, "json_reduceString(" + source_var + ", bounds[0], bounds[3]-bounds[0]+1)");
  }

  private boolean isMapping(final JSONObject obj) {
    if (obj.names().length() != 1) {
      return false;
    }

    String[] field_names = JSONObject.getNames(obj);

    Pattern p = Pattern.compile("-\\+useVar(?<num>Num)?\\+-");
    Matcher m = p.matcher(field_names[0]);
    return m.matches();
  }

  private List<ValueInputNode> getNodesWithVariables(final JSONObject root, ScriptWriter writer) {
    return getNodesWithVariables(root, "l_msg", writer);
  }

  private List<ValueInputNode> getNodesWithVariables(
      final JSONObject root, String name, ScriptWriter writer) {
    System.out.println("# getNodesWithVariables called with\n" + root.toString(2));
    List<ValueInputNode> out_list = new ArrayList<ValueInputNode>();
    List<ValueInputNode> children = new ArrayList<ValueInputNode>();

    System.out.println("Fields to check: " + root.names());
    Pattern p = Pattern.compile("-\\+useVar(?<num>Num)?\\+-");
    for (int i = 0; i < root.names().length(); i++) {
      System.out.println("Checking entry " + root.names().getString(i));

      Object child = root.get(root.names().getString(i));
      if (child instanceof JSONObject) {
        JSONObject obj = (JSONObject) child;

        if (isMapping(obj)) {
          // Get the key and match it
          System.out.println("Found variable " + obj.getString(obj.names().getString(0)));
          System.out.println("Key: " + obj.names().getString(0));
          Matcher m = p.matcher(obj.names().getString(0));
          if (m.matches()) {
            String value = obj.getString(obj.names().getString(0));
            String type = "string";
            if (m.group("num") != null) {
              // At this stage we don't know what kind of number it is but for further processing
              // this might not be of any concern.
              type = "float64";
            }
            String path = name + "/" + root.names().getString(i);
            ValueInputNode new_node = new ValueInputNode(path, type, value);
            System.out.println("Adding new node " + new_node);
            children.add(new_node);
          } else {
            System.out.println("Did not match");
          }
        } else {
          children.addAll(getNodesWithVariables(
              (JSONObject) child, name + "/" + root.names().getString(i), writer));
        }
      }
    }
    if (!children.isEmpty()) {
      ValueInputNode new_node = new ValueInputNode(name, "string", name.replaceAll("/", "_"));
      System.out.println("Adding new node " + new_node);
      out_list.add(new_node);
      out_list.addAll(children);
    }
    return out_list;
  }

  @Override
  public void generateScript(ScriptWriter writer) {
    final String globalvar = "subscriptMsg" + ID;
    final String sockname = "subscriber" + ID;

    writer.appendLine("# init subscriber");
    writer.appendLine(globalvar + " = \"\"");

    System.out.println("Subscriber generate subscriber Script");
    // add parser for values!
    //

    JSONObject values = getMsgValue(getMsgLayoutKeys()[0]);
    // System.out.println("Subscription values:\n" + values.toString(2));

    writer.defineFunction("parseSubscript" + ID); // add function definition
    writer.assign("local l_msg", globalvar);
    writer.assign("local bounds", "[0, 0, 0, 0]");

    List<ValueInputNode> nodes_with_variables = getNodesWithVariables(values, writer);
    System.out.println("Found tree: " + nodes_with_variables);
    String l_msg = "l_msg";
    for (int i = 0; i < nodes_with_variables.size(); i++) {
      String label = nodes_with_variables.get(i).getLabel();
      String[] elements = label.split("/");
      String name = elements[elements.length - 1];
      l_msg = String.join("_", Arrays.copyOfRange(elements, 0, elements.length - 1));
      if (i > 0) {
        generateElementParser(name, l_msg, nodes_with_variables.get(i).getValue(),
            nodes_with_variables.get(i).isNumericType(), writer);
      }
    }
    writer.end(); // end function definition to parse subscript

    String json = "{\"op\": \"subscribe\", \"topic\": \"" + getMsg() + "\"}";
    writer.appendLine(
        "socket_open(\"" + getMasterIP() + "\", " + getMasterPort() + ", \"" + sockname + "\")");
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
