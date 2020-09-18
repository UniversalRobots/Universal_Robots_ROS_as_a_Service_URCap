package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;

public class RosbridgeInstallationNodeContribution implements InstallationNodeContribution {

  private DataModel model;
  private final RosbridgeInstallationNodeView view;
  private final KeyboardInputFactory keyboardFactory;

  public RosbridgeInstallationNodeContribution(InstallationAPIProvider apiProvider,
      RosbridgeInstallationNodeView view, DataModel model) {
    this.keyboardFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
    this.model = model;
    this.view = view;
  }

  @Override
  public void openView() {
    // TODO Auto-generated method stub

  }

  @Override
  public void closeView() {
    // TODO Auto-generated method stub

  }

  @Override
  public void generateScript(ScriptWriter writer) {
    writer.appendLine("socket_open(\"192.168.56.1\", 9090, \"testserver\")");
  }

}
