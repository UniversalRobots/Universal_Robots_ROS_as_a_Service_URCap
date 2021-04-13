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

  public ServiceCallerProgramNodeContribution(
      ProgramAPIProvider apiProvider, ServiceCallerProgramNodeView view, DataModel model) {
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
  public void generateScript(ScriptWriter writer) {}
}
