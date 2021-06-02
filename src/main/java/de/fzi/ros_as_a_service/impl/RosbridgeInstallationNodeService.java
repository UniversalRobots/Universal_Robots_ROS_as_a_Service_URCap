//-- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2021 FZI Forschungszentrum Informatik
// Created on behalf of Universal Robots A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//-- END LICENSE BLOCK ------------------------------------------------

//----------------------------------------------------------------------
/*!\file
 *
 * \author  Lea Steffen steffen@fzi.de
 * \date    2020-12-09
 *
 */
//----------------------------------------------------------------------
package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.installation.ContributionConfiguration;
import com.ur.urcap.api.contribution.installation.CreationContext;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.ur.urcap.api.domain.data.DataModel;
import java.util.Locale;

public class RosbridgeInstallationNodeService
    implements SwingInstallationNodeService<RosbridgeInstallationNodeContribution,
        RosbridgeInstallationNodeView> {
  @Override
  public void configureContribution(ContributionConfiguration configuration) {
    // TODO Auto-generated method stub
  }

  @Override
  public String getTitle(Locale locale) {
    return "Rosbridge adapter";
  }

  @Override
  public RosbridgeInstallationNodeView createView(ViewAPIProvider apiProvider) {
    return new RosbridgeInstallationNodeView(apiProvider);
  }

  @Override
  public RosbridgeInstallationNodeContribution createInstallationNode(
      InstallationAPIProvider apiProvider, RosbridgeInstallationNodeView view, DataModel model,
      CreationContext context) {
    return new RosbridgeInstallationNodeContribution(apiProvider, view, model);
  }
}
