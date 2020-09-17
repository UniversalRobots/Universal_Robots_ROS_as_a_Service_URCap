package de.fzi.ros_as_a_service.impl;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class ServiceCallerProgramNodeService implements SwingProgramNodeService<ServiceCallerProgramNodeContribution, ServiceCallerProgramNodeView>{

  @Override
  public String getId() {
    return "serviceCaller";
  }

  @Override
  public void configureContribution(ContributionConfiguration configuration) {
    configuration.setChildrenAllowed(false);
  }

  @Override
  public String getTitle(Locale locale) {
    return "Service Call";
  }

  @Override
  public ServiceCallerProgramNodeView createView(ViewAPIProvider apiProvider) {
    return new ServiceCallerProgramNodeView(apiProvider);
  }

  @Override
  public ServiceCallerProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
      ServiceCallerProgramNodeView view, DataModel model, CreationContext context) {
    return new ServiceCallerProgramNodeContribution(apiProvider, view, model);
  }

}
