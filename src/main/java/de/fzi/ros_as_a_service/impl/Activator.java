package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Hello world activator for the OSGi bundle URCAPS contribution
 *
 */
public class Activator implements BundleActivator {
  @Override
  public void start(BundleContext bundleContext) throws Exception {
    System.out.println("Registering ServiceCaller!");
    bundleContext.registerService(
        SwingInstallationNodeService.class, new RosbridgeInstallationNodeService(), null);
    bundleContext.registerService(
        SwingProgramNodeService.class, new ServiceCallerProgramNodeService(), null);
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {}
}
