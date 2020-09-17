package de.fzi.ros_as_a_service.impl;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;

public class RosbridgeInstallationNodeView implements SwingInstallationNodeView<RosbridgeInstallationNodeContribution>{

  @Override
  public void buildUI(JPanel panel, RosbridgeInstallationNodeContribution contribution) {
    panel.add(createDescription("This will be the configuration"));
  }

  private Box createDescription(String desc) {
    Box box = Box.createHorizontalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel label = new JLabel(desc);
    box.add(label);

    return box;
  }

}
