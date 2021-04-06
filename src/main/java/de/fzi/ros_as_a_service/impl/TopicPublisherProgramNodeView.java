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
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import de.fzi.ros_as_a_service.impl.RosTaskProgramSuperNodeContribution.TaskType;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TopicPublisherProgramNodeView extends RosTaskProgramSuperNodeView
    implements SwingProgramNodeView<TopicPublisherProgramNodeContribution> {
  public TopicPublisherProgramNodeView(ViewAPIProvider apiProvider) {
    super(apiProvider, TaskType.PUBLISHER);
  }

  @Override
  public void buildUI(
      JPanel panel, ContributionProvider<TopicPublisherProgramNodeContribution> provider) {
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(createDescription("Select the Topic on that you want to publish"));
    panel.add(createMasterComboBox(masterComboBox, provider));
    panel.add(createTopicComboBox(topicComboBox, provider));
    panel.add(createVertSeparator(50));

    panel.add(createMsgPanel());
    panel.add(createVertSeparator(20));
  }

  private Box createMasterComboBox(final JComboBox<String> combo,
      final ContributionProvider<TopicPublisherProgramNodeContribution> provider) {
    Box box = Box.createHorizontalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel label = new JLabel("Remote master");

    combo.setPreferredSize(new Dimension(200, 30));
    combo.setMaximumSize(combo.getPreferredSize());

    combo.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          provider.get().onMasterSelection((String) e.getItem());
        }
      }
    });

    box.add(label);
    box.add(createHorSpacer(10));
    box.add(combo);

    return box;
  }

  private Box createTopicComboBox(final JComboBox<String> combo,
      final ContributionProvider<TopicPublisherProgramNodeContribution> provider) {
    Box box = Box.createHorizontalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel label = new JLabel("Topic");

    combo.setPreferredSize(new Dimension(400, 30));
    combo.setMaximumSize(combo.getPreferredSize());

    combo.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          provider.get().onTopicSelection((String) e.getItem(), provider);
        }
      }
    });

    box.add(label);
    box.add(createHorSpacer(10));
    box.add(combo);

    return box;
  }
}
