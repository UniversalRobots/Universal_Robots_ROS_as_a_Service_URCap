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
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;

public class TopicSubscriberProgramNodeContribution extends RosTaskProgramSuperNodeContribution {
  private final TopicSubscriberProgramNodeView view;
  static int num_contributions;

  public TopicSubscriberProgramNodeContribution(
      ProgramAPIProvider apiProvider, TopicSubscriberProgramNodeView view, DataModel model) {
    super(apiProvider, model);
    this.view = view;
    this.num_contributions = 0;
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

  @Override
  public void generateScript(ScriptWriter writer) {
    super.generateScript(writer);
    final String globalvar = "subscriptMsg" + ID;
    final String sockname = "subscriber" + ID;

    writer.appendLine("# init subscriber");
    writer.appendLine(globalvar + " = \"\"");

    System.out.println("Subscriber generate subscriber Script");
    // add parser for values!
    //

    JSONObject values = getMsgValue(getMsgLayoutKeys()[0]);
    // System.out.println("Subscription values:\n" + values.toString(2));

    final String parser_function_name = "parseSubscript" + ID + "_" + num_contributions;
    // Increment the subscription counter. This way we can make sure that the parser function IDs
    // are unique
    num_contributions += 1;

    writer.defineFunction(parser_function_name); // add function definition
    writer.appendLine("textmsg(\"Subscription is: \", " + globalvar + ")");
    writer.assign("local l_msg", globalvar);
    writer.assign("local bounds", "[0, 0, 0, 0]");

    // check for a complete json received
    writer.ifCondition("json_findCorrespondingDelimiter(l_msg) == -1");
    writer.appendLine(
        "popup(\"Received String exceeds maximum length (1023 Bytes).\", \"JSON Error\", error=True, blocking=True)");
    writer.end(); // if-clause

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

    writer.appendLine(parser_function_name + "()");

    json = "{\"op\": \"unsubscribe\", \"topic\": \"" + getMsg() + "\"}";
    writer.appendLine("socket_send_line(\"" + urscriptifyJson(json) + "\", \"" + sockname + "\")");
    writer.appendLine("socket_close(\"" + sockname + "\")");
  }
}
