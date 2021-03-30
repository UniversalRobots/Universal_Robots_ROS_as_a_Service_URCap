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
    return new RosbridgeInstallationNodeView();
  }

  @Override
  public RosbridgeInstallationNodeContribution createInstallationNode(
      InstallationAPIProvider apiProvider, RosbridgeInstallationNodeView view, DataModel model,
      CreationContext context) {
    return new RosbridgeInstallationNodeContribution(apiProvider, view, model);
  }
}
