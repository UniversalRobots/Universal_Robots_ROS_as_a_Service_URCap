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

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;
import java.util.Locale;

public class ActionCallerProgramNodeService
    implements SwingProgramNodeService<ActionCallerProgramNodeContribution,
        ActionCallerProgramNodeView> {
  @Override
  public String getId() {
    return "actionCaller";
  }

  @Override
  public void configureContribution(ContributionConfiguration configuration) {
    configuration.setChildrenAllowed(false);
  }

  @Override
  public String getTitle(Locale locale) {
    return "Action Call";
  }

  @Override
  public ActionCallerProgramNodeView createView(ViewAPIProvider apiProvider) {
    return new ActionCallerProgramNodeView(apiProvider);
  }

  @Override
  public ActionCallerProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
      ActionCallerProgramNodeView view, DataModel model, CreationContext context) {
    return new ActionCallerProgramNodeContribution(apiProvider, view, model);
  }
}
