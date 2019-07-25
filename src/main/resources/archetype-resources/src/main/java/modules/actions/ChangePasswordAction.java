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

import org.apache.fulcrum.security.util.PasswordMismatchException;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

/**
 * Change Password action.
 *
 */
public class ChangePasswordAction extends SecureAction
{

	/** Injected service instance */
	@TurbineService
	private SecurityService security;

	/**
	 * Implement this to add information to the context.
	 *
	 * @param data    Turbine information.
	 * @param context Context for web pages.
	 * @exception Exception, a generic exception.
	 */
	@Override
	public void doPerform(PipelineData pipelineData) throws Exception 
	{
		RunData data = (RunData) pipelineData;
		User user = data.getUser();

		String oldPassword = data.getParameters().getString("oldpassword", "");
		String newPassword = data.getParameters().getString("newpassword", "");

		try {
			security.changePassword(user, oldPassword, newPassword);
			data.setMessage("Password changed!");
		} catch (PasswordMismatchException e) {
			data.setMessage(e.getMessage());
			data.setScreenTemplate("Password.vm");
		}

	}

	/**
	 * Implement this to add information to the context.
	 *
	 * @param data    Turbine information.
	 * @param context Context for web pages.
	 * @exception Exception, a generic exception.
	 */
	@Override
	public void doPerform(PipelineData data, Context context) throws Exception 
	{
		context.put("success", "Password changed!!");
	}

}
