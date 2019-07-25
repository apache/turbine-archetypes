package ${package}.flux.modules.actions;

/*
 * Copyright 2001-2019 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.configuration2.Configuration;
import org.apache.fulcrum.localization.LocalizationService;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.turbine.Turbine;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.annotation.TurbineConfiguration;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

/**
 * Velocity Secure action.
 *
 * Always performs a Security Check that you've defined before executing the
 * doPerform().
 */
public class SecureAction extends VelocitySecureAction {

	@TurbineService
	private LocalizationService localizationService;

	/** Injected configuration instance */
	@TurbineConfiguration
	private Configuration conf;

	@TurbineConfiguration(TurbineConstants.TEMPLATE_LOGIN)
	private Configuration templateLogin;

	@TurbineConfiguration(TurbineConstants.TEMPLATE_HOMEPAGE)
	private Configuration templateHomepage;

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
	protected boolean isAuthorized(PipelineData pipelineData) throws Exception {

		boolean isAuthorized = false;
		RunData data = (RunData) pipelineData;

		/*
		 * Grab the Flux Admin role listed in the Flux.properties file that is included
		 * in the the standard TurbineResources.properties file.
		 */
		String fluxAdminRole = Turbine.getConfiguration().getString("flux.admin.role");

		// Get the Turbine ACL implementation
		TurbineAccessControlList acl = data.getACL();

		if (acl == null) {
			String msg = localizationService.getString(localizationService.getDefaultBundleName(),
					localizationService.getLocale(((RunData) data).getRequest()), "no_permission");

			data.setMessage(msg);

			log.debug(
					"call not permitted for template: " + data.getScreenTemplate() + " and action " + data.getAction());
			data.setScreenTemplate((String) templateHomepage.getProperty(""));
			isAuthorized = false;

		} else if (acl.hasRole(fluxAdminRole)) {
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
	 * @param data
	 *            Turbine information.
	 * @param context
	 *            Context for web pages.
	 * @exception Exception,
	 *                a generic exception.
	 */
	public void doPerform(PipelineData pipelineData, Context context) throws Exception 
	{
		RunData data = (RunData) pipelineData;
		User user = data.getUser();
		context.put("user", user);
	}
}
