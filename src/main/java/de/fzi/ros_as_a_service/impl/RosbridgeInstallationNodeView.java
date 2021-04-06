//-- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2021 FZI Forschungszentrum Informatik
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

import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RosbridgeInstallationNodeView
    implements SwingInstallationNodeView<RosbridgeInstallationNodeContribution> {
  private JTextField textFieldIP;
  private JTextField textFieldPort;

  @Override
  public void buildUI(JPanel panel, RosbridgeInstallationNodeContribution contribution) {
    panel.add(createDescription("This will be the configuration"));
    panel.add(createIPBox(contribution));
    panel.add(createSpacer(60));
    panel.add(createPortBox(contribution));
  }

  private Box createDescription(String desc) {
    Box box = Box.createHorizontalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel label = new JLabel(desc);
    box.add(label);

    return box;
  }

  public void UpdateIPTextField(String value) {
    textFieldIP.setText(value);
  }

  public void UpdatePortTextField(String value) {
    textFieldPort.setText(value);
  }

  private Box createIPBox(final RosbridgeInstallationNodeContribution contribution) {
    Box box = Box.createVerticalBox();
    // create IP Label
    JLabel label = new JLabel("Please setup the remote host's IP: ");
    box.add(label);
    // create IP Textfield
    textFieldIP = new JTextField(15);
    textFieldIP.setText(contribution.getHostIP());
    textFieldIP.setFocusable(false);
    textFieldIP.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        // TODO: Use IPAddress keyboard here
        KeyboardTextInput keyboardInput = contribution.getInputForIPTextField();
        keyboardInput.show(textFieldIP, contribution.getCallbackForIPTextField());
      }
    });
    box.add(textFieldIP);
    return box;
  }

  private Box createPortBox(final RosbridgeInstallationNodeContribution contribution) {
    Box box = Box.createVerticalBox();
    // create port Label
    JLabel label = new JLabel("Please setup the custom port: ");
    box.add(label);
    // create port Textfield
    textFieldPort = new JTextField(15);
    textFieldPort.setText(contribution.getCustomPort());
    textFieldPort.setFocusable(false);
    textFieldPort.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        // TODO: Use Integer keyboard here
        KeyboardTextInput keyboardInput = contribution.getInputForPortTextField();
        keyboardInput.show(textFieldPort, contribution.getCallbackForPortTextField());
      }
    });
    box.add(textFieldPort);
    return box;
  }

  private Component createSpacer(int height) {
    return Box.createRigidArea(new Dimension(0, height));
  }
}
