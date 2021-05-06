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

import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

public class TopicPublisherProgramNodeContribution extends RosTaskProgramSuperNodeContribution {
  private final TopicPublisherProgramNodeView view;

  public TopicPublisherProgramNodeContribution(
      ProgramAPIProvider apiProvider, TopicPublisherProgramNodeView view, DataModel model) {
    super(apiProvider, model);
    this.view = view;
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
    return "Pub. " + getMsg();
  }

  @Override
  protected String[] getMsgLayoutKeys() {
    return new String[] {"Data"};
  }

  @Override
  protected String[] getMsgLayoutDirections() {
    return new String[] {"out"};
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
  public void generateScript(ScriptWriter writer) {
    System.out.println("# TopicPub generateScript");
    super.generateScript(writer);
    final String sockname = "publisher_" + ID;

    String json = "{\"op\": \"advertise\", \"topic\": \"" + getMsg() + "\", \"type\": \""
        + queryTopicType(getMsg()) + "\"}";
    String urscriptified = json.replaceAll("\"", "\" + quote + \"");

    writer.appendLine(
        "socket_open(\"" + getMasterIP() + "\", " + getMasterPort() + ", \"" + sockname + "\")");
    writer.appendLine("socket_send_line(\"" + urscriptified + "\", \"" + sockname + "\")");

    System.out.println("Building json string");
    json = "{\"op\": \"publish\", \"topic\": \"" + getMsg()
        + "\", \"msg\": " + buildJsonString("Data", writer) + "}";

    writer.appendLine("socket_send_line(\"" + urscriptifyJson(json) + "\", \"" + sockname + "\")");
    writer.appendLine("textmsg(\"sending: " + urscriptifyJson(json) + "\")");

    writer.appendLine("socket_close(\"" + sockname + "\")");
  }
}
