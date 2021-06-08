// -- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2021 FZI Forschungszentrum Informatik
// Created on behalf of Universal Robots A/S
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
 * \date    2020-09-16
 *
 */
//----------------------------------------------------------------------

package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

public class ServiceCallerProgramNodeContribution extends RosTaskProgramSuperNodeContribution {
  private final ServiceCallerProgramNodeView view;
  static private int num_contributions;

  public ServiceCallerProgramNodeContribution(
      ProgramAPIProvider apiProvider, ServiceCallerProgramNodeView view, DataModel model) {
    super(apiProvider, model);
    this.view = view;
    ServiceCallerProgramNodeContribution.num_contributions = 0;
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
    return "Call " + getMsg();
  }

  @Override
  protected String[] getMsgLayoutKeys() {
    return new String[] {"Request", "Response"};
  }

  @Override
  protected String[] getMsgLayoutDirections() {
    return new String[] {"out", "in"};
  }

  @Override
  protected String getMsgTypeRequestString(final String topic_name) {
    return "{\"op\": \"call_service\",\"service\":\"/rosapi/service_type\",\"args\":{\"service\":\""
        + topic_name + "\"}}";
  }

  @Override
  protected String getMsgListRequestString() {
    return "{\"op\": \"call_service\",\"service\": \"/rosapi/services\"}";
  }

  @Override
  protected String getMsgListResponsePlaceholder() {
    return "services";
  }

  @Override
  protected String[] getMsgLayoutRequestStrings(final String msg_type) {
    return new String[] {
        "{\"op\": \"call_service\",\"service\":\"/rosapi/service_request_details\", \"args\":{\"type\":\""
            + msg_type + "\"}}",
        "{\"op\": \"call_service\",\"service\":\"/rosapi/service_response_details\", \"args\":{\"type\":\""
            + msg_type + "\"}}"};
  }

  @Override
  public void generateScript(ScriptWriter writer) {
    super.generateScript(writer);
    final String sockname = "servicecall" + ID;
    final String globalvar = "serviceResponse" + ID;

    final String parser_function_name = "parseServiceResponse" + ID + "_" + num_contributions;
    generateParsingFunction(
        parser_function_name, writer, getMsgValue(getMsgLayoutKeys()[1]), globalvar);
    // Increment the subscription counter. This way we can make sure that the parser function IDs
    // are unique
    num_contributions += 1;

    writer.appendLine("# open socket for service call");
    writer.appendLine(
        "socket_open(\"" + getMasterIP() + "\", " + getMasterPort() + ", \"" + sockname + "\")");
    writer.assign(globalvar, "\"\"");

    String json = "{\"op\":\"call_service\", \"service\": \"" + getMsg()
        + "\",\"args\":" + buildJsonString("Request", writer) + "}";

    writer.appendLine("socket_send_line(\"" + urscriptifyJson(json) + "\", \"" + sockname + "\")");
    writer.appendLine("textmsg(\"sending: " + urscriptifyJson(json) + "\")");

    writer.assign("response_timeout", "10");
    writer.assign("tmp", "socket_read_string(\"" + sockname + "\", timeout=response_timeout)");
    writer.appendLine("textmsg(\"received response: \", tmp)");
    writer.ifCondition("str_empty(tmp)");
    writer.appendLine(
        "popup(\"Waiting for service response went into timeout.\", \"Response Error\", error=True, blocking=True)");
    writer.elseCondition();
    writer.appendLine("local bounds = json_getElement(tmp, \"result\")");
    writer.appendLine("result = str_sub(tmp, bounds[2], bounds[3]-bounds[2]+1)");
    writer.appendLine("local bounds = json_getElement(tmp, \"values\")");
    writer.appendLine("values = str_sub(tmp, bounds[2], bounds[3]-bounds[2]+1)");
    writer.ifCondition("result != \"true\"");
    writer.appendLine("popup(values, \"Response Error\", error=True, blocking=True)");
    writer.elseCondition();
    writer.assign(globalvar, "values");
    writer.appendLine(parser_function_name + "()");
    writer.end(); // if-clause result_true
    writer.end(); // if-clause tmp_empty

    writer.appendLine("socket_close(\"" + sockname + "\")");
  }
}
