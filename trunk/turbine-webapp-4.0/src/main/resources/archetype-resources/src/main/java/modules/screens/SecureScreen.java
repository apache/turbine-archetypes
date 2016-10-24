package ${package}.modules.screens;
#*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*#


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.services.security.TurbineSecurity;
import org.apache.fulcrum.security.acl.AccessControlList;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlListImpl;
import org.apache.velocity.context.Context;

/**
 * This class provides a sample implementation for creating a secured screen
 */
public class SecureScreen extends VelocitySecureScreen {
	// create an instance of the logging facility
	private static Log log = LogFactory.getLog(SecureScreen.class);

	protected SecurityService securityService;

	@Override
	protected boolean isAuthorized(PipelineData data) throws Exception {
		boolean isAuthorized = false;

		// Load the security service
		securityService = (SecurityService) TurbineServices.getInstance().getService(SecurityService.SERVICE_NAME);

		// Who is our current user?
		User user = getRunData(data).getUser();

		// Get the Turbine ACL implementation
		TurbineAccessControlListImpl acl = (TurbineAccessControlListImpl) getRunData(data).getACL();

		if (acl == null) {
			getRunData(data).setScreenTemplate(Turbine.getConfiguration().getString("template.login"));
			isAuthorized = false;
		} else if (acl.hasRole("TurbineAdmin")) {
			isAuthorized = true;
		} else {
			getRunData(data).setScreenTemplate(Turbine.getConfiguration().getString("template.home"));
			getRunData(data).setMessage("You do not have access to this part of the site.");
			isAuthorized = false;
		}
		return isAuthorized;
	}

	@Override
	protected void doBuildTemplate(PipelineData data, Context context) throws Exception {
		// TODO Auto-generated method stub

	}

}
