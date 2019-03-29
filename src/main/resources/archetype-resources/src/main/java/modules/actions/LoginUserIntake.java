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

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.pool.PoolService;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.FulcrumSecurityException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.IntakeException;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.annotation.TurbineConfiguration;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.modules.Action;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;

/**
 * This is where we authenticate the user logging into the system against a user
 * in the database. If the user exists in the database that users last login
 * time will be updated.
 *
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:peter@courcoux.biz">Peter Courcoux</a>
 * @version $Id$
 */
public class LoginUserIntake extends org.apache.turbine.modules.actions.LoginUser {

	/** Logging */
	private static Log log = LogFactory.getLog(LoginUserIntake.class);

	/** Injected service instance */
	@TurbineService
	private SecurityService security;

	/** Injected configuration instance */
	@TurbineConfiguration
	private Configuration conf;

	/**
	 * Checks for anonymous user, else calls parent method.
	 *
	 * @param pipelineData Turbine information.
	 * @exception FulcrumSecurityException could not get instance of the anonymous
	 *                                     user
	 */
	@Override
	public void doPerform(PipelineData pipelineData) throws FulcrumSecurityException 
	{
		RunData data = (RunData) pipelineData;

		try 
		{
			// Get intake group
			// context only available after ExecutePageValve, could not invoke
			// (IntakeTool)context.get("intake") using pook service instead
			PoolService poolService = (PoolService) TurbineServices.getInstance().getService(PoolService.ROLE);
			IntakeTool intake = (IntakeTool) poolService.getInstance(IntakeTool.class);

			intake.init(data);
			Group group = intake.get("Login", IntakeTool.DEFAULT_KEY);
			String username = (String) group.get("Username").getValue();
			if (StringUtils.isEmpty(username)) 
			{
				return;
			}

			if (username.equals(security.getAnonymousUser().getName())) 
			{
				data.setMessage("Anonymous user cannot login");
				reset(data);
				return;
			}

			if (username.equals(security.getAnonymousUser().getName())) 
			{
				throw new Exception("Anonymous user cannot login");
			}

			String password = (String) group.get("Password").getValue();
			
			// Authenticate the user and get the object.
			User user = security.getAuthenticatedUser(username, password);

			// Store the user object.
			data.setUser(user);

			// Mark the user as being logged in.
			user.setHasLoggedIn(Boolean.TRUE);

			// Set the last_login date in the database.
			user.updateLastLogin();

			// This only happens if the user is valid; otherwise, we
			// will get a valueBound in the User object when we don't
			// want to because the username is not set yet. Save the
			// User object into the session.
			data.save();

			/*
			 * If the setPage("template.vm") method has not been used in the template to
			 * authenticate the user (usually Login.vm), then the user will be forwarded to
			 * the template that is specified by the "template.home" property as listed in
			 * TR.props for the webapp.
			 */

		} catch (Exception e) {
			if (e instanceof DataBackendException || e instanceof IntakeException) {
				log.error(e);
			}

			// Set Error Message and clean out the user.
			data.setMessage(conf.getString(TurbineConstants.LOGIN_ERROR, ""));
			User anonymousUser = security.getAnonymousUser();
			data.setUser(anonymousUser);

			String loginTemplate = conf.getString(TurbineConstants.TEMPLATE_LOGIN);

			if (StringUtils.isNotEmpty(loginTemplate)) {
				// We're running in a templating solution
				data.setScreenTemplate(loginTemplate);
			} else {
				data.setScreen(conf.getString(TurbineConstants.SCREEN_LOGIN));
			}
		}
	}

	private void reset(RunData data) throws UnknownEntityException {
		User anonymousUser = security.getAnonymousUser();
		data.setUser(anonymousUser);

		if (StringUtils.isNotEmpty(conf.getString(TurbineConstants.TEMPLATE_LOGIN, ""))) {
			// We're running in a templating solution
			data.setScreenTemplate(conf.getString(TurbineConstants.TEMPLATE_LOGIN));
		} else {
			data.setScreen(conf.getString(TurbineConstants.SCREEN_LOGIN));
		}
	}

}
