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
import org.apache.fulcrum.security.util.FulcrumSecurityException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.annotation.TurbineConfiguration;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;

/**
 * This is where we authenticate the user logging into the system
 * against a user in the database. If the user exists in the database
 * that users last login time will be updated.
 *
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:peter@courcoux.biz">Peter Courcoux</a>
 * @version $Id: LoginUser.java 1725011 2016-01-16 17:38:47Z tv $
 */
public class LoginUser
        extends org.apache.turbine.modules.actions.LoginUser
{

    /** Logging */
    private static Log log = LogFactory.getLog(LoginUser.class);

    /** Injected service instance */
    @TurbineService
    private SecurityService security;
    
    /** Injected configuration instance */
    @TurbineConfiguration
    private Configuration conf;



    /**
     * Checks for anonymous user, else calls parent method.
     *
     * @param     pipelineData Turbine information.
     * @exception FulcrumSecurityException could not get instance of the
     *            anonymous user
     */
    @Override
    public void doPerform(PipelineData pipelineData)
            throws FulcrumSecurityException
    {
        RunData data = (RunData) pipelineData;
        String username = data.getParameters().getString(LoginUser.CGI_USERNAME, "");

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
    	
       super.doPerform(pipelineData);

    }



	private void reset(RunData data) throws UnknownEntityException {
		User anonymousUser = security.getAnonymousUser();
		data.setUser(anonymousUser);

		if (StringUtils.isNotEmpty(conf.getString(TurbineConstants.TEMPLATE_LOGIN,"")))
		{
		    // We're running in a templating solution
		    data.setScreenTemplate(
		    		conf.getString(TurbineConstants.TEMPLATE_LOGIN));
		}
		else
		{
		    data.setScreen(
		    		conf.getString(TurbineConstants.SCREEN_LOGIN));
		}
	}

}

