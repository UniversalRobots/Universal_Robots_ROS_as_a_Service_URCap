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
import com.ur.urcap.api.contribution.ViewAPIProvider;
import de.fzi.ros_as_a_service.impl.RosTaskProgramSuperNodeContribution.TaskType;
import javax.swing.JPanel;
import javax.swing.JTree;
import org.json.JSONArray;

public class TopicPublisherProgramNodeView
  extends RosTaskProgramSuperNodeView<TopicPublisherProgramNodeContribution> {
  public TopicPublisherProgramNodeView(ViewAPIProvider apiProvider) {
    super(apiProvider, TaskType.PUBLISHER);
    this.description = "Select the Topic on that you want to publish to.";
  }

  @Override
  protected void createTreeView(JSONArray layout,
      final ContributionProvider<TopicPublisherProgramNodeContribution> provider) {
    if (layout == null) {
      return;
    };
    JPanel panel = createMsgPanel("Data:");
    System.out.println("#createTreeView");
    JTree tree = createMsgTreeLayout(layout, provider.get().tree_direction, provider.get().getVarCollection());
    addTreePanel(tree, panel);
    LoadValueNode base_node = loadValuesToTree(null, provider.get().getMsgValue(), "msg_base");
    try {
      System.out.println("detected values: " + base_node.toString());
      setTreeValues(base_node, tree);
    } catch (Exception e) {
      System.err.println("Error: " + e);
    }
  }
}
