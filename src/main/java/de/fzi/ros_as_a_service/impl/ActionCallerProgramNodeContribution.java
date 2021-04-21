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
 * \author  Felix Exner exner@fzi.de
 * \date    2021-04-21
 *
 */
//----------------------------------------------------------------------

package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import java.util.Objects;
import org.json.JSONObject;

public class ActionCallerProgramNodeContribution extends RosTaskProgramSuperNodeContribution {
  private final ActionCallerProgramNodeView view;
  static private int num_contributions;

  public ActionCallerProgramNodeContribution(
      ProgramAPIProvider apiProvider, ActionCallerProgramNodeView view, DataModel model) {
    super(apiProvider, model);
    this.view = view;
    ActionCallerProgramNodeContribution.num_contributions = 0;
  }

  @Override
  public void openView() {
    // TODO: If we parametrize RosTaskSuperNodeContribution with the view type, we get a cyclic
    // parametrization. So, for now we keep solving this here, but there might be a better solution.
    super.openView();
    view.updateView(this);
  }

  @Override
  public String getTitle() {
    return "Call action " + getMsg();
  }

  @Override
  protected String[] getMsgLayoutKeys() {
    return new String[] {"Goal", "Feedback", "Result"};
  }

  @Override
  protected String[] getMsgLayoutDirections() {
    return new String[] {"out", "in", "in"};
  }

  @Override
  protected String getMsgTypeRequestString(final String topic_name) {
    return "{\"op\": \"call_service\",\"service\":\"/rosapi/topic_type\",\"args\":{\"topic\":\""
        + topic_name + "/goal\"}}";
  }

  @Override
  protected String getMsgListRequestString() {
    return "{\"op\": \"call_service\",\"service\": \"/rosapi/action_servers\"}";
  }

  @Override
  protected String getMsgListResponsePlaceholder() {
    return "action_servers";
  }

  @Override
  protected String queryTopicType(String topic_name) {
    try {
      Objects.requireNonNull(topic_name, "Topicname null");
      System.out.println("ActionTopic: " + topic_name);

      String request_string = getMsgTypeRequestString(topic_name);

      JSONObject json_response = rosbridgeRequest(request_string);
      Objects.requireNonNull(json_response, "Response null");
      String response = json_response.getJSONObject("values").getString("type");
      return response.replaceFirst("(?s)(.*)ActionGoal", "$1");

    } catch (org.json.JSONException e) {
      System.err.println("getTopicType: JSON-Error: " + e);
    } catch (Exception e) {
      System.err.println("getTopicType: Error: " + e);
    }
    return null;
  }

  @Override
  protected String[] getMsgLayoutRequestStrings(final String msg_type) {
    return new String[] {
        "{\"op\": \"call_service\",\"service\":\"/rosapi/message_details\", \"args\":{\"type\":\""
            + msg_type + "Goal\"}}",
        "{\"op\": \"call_service\",\"service\":\"/rosapi/message_details\", \"args\":{\"type\":\""
            + msg_type + "Feedback\"}}",
        "{\"op\": \"call_service\",\"service\":\"/rosapi/message_details\", \"args\":{\"type\":\""
            + msg_type + "Result\"}}"};
  }

  @Override
  public void generateScript(ScriptWriter writer) {
    super.generateScript(writer);
    final String sockname = "actioncall" + ID;
    final String globalvar = "actionResponse" + ID;

    String json = null;

    writer.appendLine("# open socket for action call");
    writer.appendLine(
        "socket_open(\"" + getMasterIP() + "\", " + getMasterPort() + ", \"" + sockname + "\")");
    writer.assign(globalvar, "\"\"");

    String feedback_parser_function_name = "parseActionFeedback" + ID + "_" + num_contributions;
    generateParsingFunction(
        feedback_parser_function_name, writer, getMsgValue(getMsgLayoutKeys()[1]), "feedback");
    String result_parser_function_name = "parseActionResult" + ID + "_" + num_contributions;
    generateParsingFunction(
        result_parser_function_name, writer, getMsgValue(getMsgLayoutKeys()[2]), "result");

    writer.assign("result", "\"\"");
    writer.assign("feedback", "\"\"");

    String listen_thread_function_name = "listenToAction" + ID + "_" + num_contributions;
    writer.defineThread(listen_thread_function_name);
    json = "{\"op\": \"subscribe\", \"topic\": \"" + getMsg() + "/feedback\"}";
    writer.appendLine("socket_send_line(\"" + urscriptifyJson(json) + "\", \"" + sockname + "\")");
    json = "{\"op\": \"subscribe\", \"topic\": \"" + getMsg() + "/result\"}";
    writer.appendLine("socket_send_line(\"" + urscriptifyJson(json) + "\", \"" + sockname + "\")");
    writer.whileCondition("result == \"\"");
    writer.whileCondition(globalvar + " == \"\"");
    writer.assign("tmp", "socket_read_string(\"" + sockname + "\")");
    writer.ifNotCondition("str_empty(tmp)");
    writer.appendLine("local bounds = json_getElement(tmp, \"msg\")");
    writer.appendLine("msg = str_sub(tmp, bounds[2], bounds[3]-bounds[2]+1)");
    writer.assign(globalvar, "msg");
    writer.appendLine("local bounds = json_getElement(tmp, \"topic\")");
    // writer.appendLine("topic = str_sub(tmp, bounds[2] + 1, bounds[3]-bounds[2] - 1)");
    writer.assign("local topic_end", "json_findCorrespondingDelimiter(tmp, bounds[2])");
    writer.assign("topic", "\"\"");
    writer.ifCondition("topic_end != -1");
    writer.assign("topic", "str_sub(tmp, bounds[2] + 1, topic_end-bounds[2]-1)");
    writer.elseCondition();
    writer.appendLine("textmsg(\"topic without delimiter!\")");
    writer.end(); // if-clause

    writer.appendLine("textmsg(\"Received from action: \", " + globalvar + ")");
    writer.appendLine("textmsg(\"Topic: \", topic)");

    writer.ifCondition("topic == \"" + getMsg() + "/feedback\"");
    writer.appendLine("local bounds = json_getElement(" + globalvar + ", \"feedback\")");
    writer.appendLine("feedback = str_sub(" + globalvar + ", bounds[2], bounds[3]-bounds[2]+1)");
    writer.appendLine("textmsg(\"Received feedback: \", feedback)");
    writer.appendLine(feedback_parser_function_name + "()");
    writer.elseCondition();
    writer.appendLine("local bounds = json_getElement(" + globalvar + ", \"status\")");
    writer.appendLine("status = str_sub(" + globalvar + ", bounds[2], bounds[3]-bounds[2]+1)");
    writer.appendLine("local bounds = json_getElement(status, \"status\")");
    writer.appendLine("final_status = to_num(str_sub(status, bounds[2], bounds[3]-bounds[2]+1))");
    writer.appendLine("textmsg(\"Action finished in state: \", final_status)");
    writer.ifCondition("final_status != 3");
    writer.appendLine(
        "popup(\"Action call did not succeed, but finished in state \" + goalStateString(final_status), title=\"Action call failed\", error=True, blocking=True)");
    writer.end(); // if-clause goal status
    writer.appendLine("local bounds = json_getElement(" + globalvar + ", \"result\")");
    writer.appendLine("result = str_sub(" + globalvar + ", bounds[2], bounds[3]-bounds[2]+1)");
    writer.appendLine("textmsg(\"Received result: \", result)");
    writer.appendLine(result_parser_function_name + "()");
    writer.end(); // if-clause topic is feedback
    writer.end(); // if-clause empty
    writer.sync();
    writer.end(); // while globalvar empty
    writer.assign(globalvar, "\"\"");
    writer.end(); // while result empty

    json = "{\"op\": \"unsubscribe\", \"topic\": \"" + getMsg() + "/feedback\"}";
    writer.appendLine("socket_send_line(\"" + urscriptifyJson(json) + "\", \"" + sockname + "\")");
    json = "{\"op\": \"unsubscribe\", \"topic\": \"" + getMsg() + "/result\"}";
    writer.appendLine("socket_send_line(\"" + urscriptifyJson(json) + "\", \"" + sockname + "\")");

    writer.end(); // end thread definition

    final String listen_thread_name = "listen_thread" + ID + num_contributions;
    writer.runThread(listen_thread_name, listen_thread_function_name + "()");

    json = "{\"op\":\"publish\", \"topic\": \"" + getMsg() + "/goal"
        + "\",\"msg\":{\"goal\":" + buildJsonString(false, "Goal") + "}}";

    writer.appendLine("textmsg(\"Sending: " + urscriptifyJson(json) + "\")");
    writer.appendLine("socket_send_line(\"" + urscriptifyJson(json) + "\", \"" + sockname + "\")");

    writer.appendLine("join " + listen_thread_name);

    writer.appendLine("socket_close(\"" + sockname + "\")");

    // Increment the subscription counter. This way we can make sure that the parser function IDs
    // are unique
    num_contributions += 1;
  }
}
