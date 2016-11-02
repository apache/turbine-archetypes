package ${package}.modules.actions;

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

import org.apache.fulcrum.security.SecurityService;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlListImpl;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.velocity.context.Context;

/**
 * Velocity Secure action.
 *
 * Always performs a Security Check that you've defined before executing the
 * doBuildtemplate().
 */
public class SecureAction extends VelocitySecureAction {

	protected SecurityService securityService;

	/**
	 * This currently only checks to make sure that user is allowed to view the
	 * storage area. If you create an action that requires more security then
	 * override this method.
	 *
	 * @param data
	 *            Turbine information.
	 * @return True if the user is authorized to access the screen.
	 * @exception Exception,
	 *                a generic exception.
	 */
	@Override
	protected boolean isAuthorized(PipelineData data) throws Exception {

		boolean isAuthorized = false;

		// Who is our current user?
		User user = getRunData(data).getUser();

		// Get the Turbine ACL implementation
		TurbineAccessControlListImpl acl = (TurbineAccessControlListImpl) getRunData(data).getACL();

		if (acl == null || !acl.hasRole("turbineadmin")) {
			getRunData(data).setMessage("You do not have permission to access this action");
			isAuthorized = false;
		} else if (acl.hasRole("admin")) {
			isAuthorized = true;
		}

		return isAuthorized;
	}

	/**
	 * Implement this to add information to the context.
	 *
	 * @param data
	 *            Turbine information.
	 * @param context
	 *            Context for web pages.
	 * @exception Exception,
	 *                a generic exception.
	 */
	public void doPerform(PipelineData data, Context context) throws Exception {

	}
}
