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

import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.turbine.Turbine;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.annotation.TurbineConfiguration;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.commons.configuration2.Configuration;

/**
 * This class provides a sample implementation for creating a secured screen
 */
public class SecureScreen extends VelocitySecureScreen {
	@TurbineService
	protected SecurityService securityService;

	@TurbineConfiguration(TurbineConstants.TEMPLATE_LOGIN)
	private Configuration templateLogin;

	@TurbineConfiguration(TurbineConstants.TEMPLATE_HOMEPAGE)
	private Configuration templateHomepage;

	@Override
	protected boolean isAuthorized(PipelineData pipelineData) throws Exception {
		boolean isAuthorized = false;
		RunData data = (RunData) pipelineData;

		// Who is our current user?
		User user = data.getUser();

		// Get the Turbine ACL implementation
		TurbineAccessControlList acl = data.getACL();

		if (acl == null) {
			// commons configuration getProperty: prefix removed, the key for the value ..
			// is an empty string, the result an object
			data.setScreenTemplate((String) templateLogin.getProperty(""));
			isAuthorized = false;
		} else if (acl.hasRole("turbineuser") || acl.hasRole("turbineadmin")) {
			isAuthorized = true;
		} else {
			data.setScreenTemplate((String) templateHomepage.getProperty(""));
			data.setMessage("You do not have access to this part of the site.");
			isAuthorized = false;
		}
		return isAuthorized;
	}

	/**
	 * Implement this to add information to the context.
	 *
	 * @param data    Turbine information.
	 * @param context Context for web pages.
	 * @exception Exception, a generic exception.
	 */
	@Override
	protected void doBuildTemplate(PipelineData data, Context context) throws Exception {

	}
}
