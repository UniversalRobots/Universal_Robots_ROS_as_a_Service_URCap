// -- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2021 FZI Forschungszentrum Informatik
// Created on behalf of Universal Robots A/S
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
 * \author  Lea Steffen steffen@fzi.de
 * \date    2020-12-09
 *
 */
//----------------------------------------------------------------------
package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
  @Override
  public void start(BundleContext bundleContext) throws Exception {
    System.out.println("Registering ros_as_a_service_Installation!");
    bundleContext.registerService(
        SwingInstallationNodeService.class, new RosbridgeInstallationNodeService(), null);
    System.out.println("Registering ServiceCaller!");
    bundleContext.registerService(
        SwingProgramNodeService.class, new ServiceCallerProgramNodeService(), null);
    System.out.println("Registering TopicPublisher!");
    bundleContext.registerService(
        SwingProgramNodeService.class, new TopicPublisherProgramNodeService(), null);
    System.out.println("Registering TopicSubscriber!");
    bundleContext.registerService(
        SwingProgramNodeService.class, new TopicSubscriberProgramNodeService(), null);
    System.out.println("Registering ActionCaller!");
    bundleContext.registerService(
        SwingProgramNodeService.class, new ActionCallerProgramNodeService(), null);
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {}
}
