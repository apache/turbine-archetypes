package ${package}.flux.modules.actions.user;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import ${package}.flux.modules.actions.FluxAction;

/**
 * Change user action
 */
public class FluxUserAction extends FluxAction 
{
	/** Logging **/
	private static Log log = LogFactory.getLog(FluxUserAction.class);

	/** Injected service instance */
	@TurbineService
	private SecurityService security;

	/**
	 * ActionEvent responsible for inserting a new user into the Turbine security
	 * system.
	 */
	public void doInsert(PipelineData pipelineData, Context context) throws Exception 
	{
		RunData data = (RunData) pipelineData;

		/*
		 * Grab the username entered in the form.
		 */
		String username = data.getParameters().getString("username");
		String password = data.getParameters().getString("password");

		if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) 
		{
			/*
			 * Make sure this account doesn't already exist. If the account already exists
			 * then alert the user and make them change the username.
			 */
			if (security.accountExists(username)) 
			{
				context.put("username", username);
				context.put("errorTemplate", "user,FluxUserAlreadyExists.vm");

				data.setMessage("The user already exists");
				data.getParameters().add("mode", "insert");
				data.setScreen("user,FluxUserForm.vm");
				return;
			} 
			else 
			{

				try 
				{
					/*
					 * Create a new user modeled directly from the SecurityServiceTest method
					 */
					User user = security.getUserInstance(username);
					data.getParameters().setProperties(user);
					security.addUser(user, password);

					// Use security to force the password
					security.forcePassword(user, password);

				} 
				catch (Exception e) 
				{
					log.error("Error adding new user: " + e);

					context.put("username", username);
					context.put("errorTemplate", "user,FluxUserAlreadyExists.vm");

					data.setMessage("Could not add the user");
					data.getParameters().add("mode", "insert");
					data.setScreen("user,FluxUserForm.vm");
					return;
				}
			}

		} else {
			String msg = "Cannot add user without username or password";
			log.error(msg);
			data.setMessage(msg);
			data.getParameters().add("mode", "insert");
			data.setScreen("user,FluxUserForm.vm");
		}
	}

	/**
	 * ActionEvent responsible updating a user
	 */
	public void doUpdate(PipelineData pipelineData, Context context) throws Exception 
	{
		RunData data = (RunData) pipelineData;
		String username = data.getParameters().getString("username");
		if (!StringUtils.isEmpty(username)) 
		{
			if (security.accountExists(username)) 
			{
				// Load the wrapped user object
				User user = security.getUser(username);
				User tmp_user = security.getUser(username);
				if (user != null) {

					// Update user details except for the password
					data.getParameters().setProperties(user);
					user.setPassword(tmp_user.getPassword());
					security.saveUser(user);

					// Test if Admin provided new password
					String password = data.getParameters().getString("password");
					if (!StringUtils.isEmpty(password)) 
					{
						// Change user password
						security.changePassword(user, user.getPassword(), password);
						security.forcePassword(user, password);
					} else {
						data.setMessage("Cannot provide an empty password");
						return;
					}

				}

			} else {
				log.error("User does not exist!");
			}
		}
	}

	/**
	 * ActionEvent responsible for removing a user
	 */
	public void doDelete(PipelineData pipelineData, Context context) throws Exception 
	{

		try 
		{
			RunData data = (RunData) pipelineData;
			String username = data.getParameters().getString("username");
			if (!StringUtils.isEmpty(username)) 
			{
				if (security.accountExists(username)) 
				{
					// find the user object and remove using security mgr
					User user = security.getUser(username);

					// get the turbine user id
					int id = (int) user.getId();

					// remove the turbine user
					security.removeUser(user);

				} else {
					log.error("User does not exist!");
					data.setMessage("User not found!");
				}
			}
		} 
		catch (Exception e) 
		{
			log.error("Could not remove user: " + e);
		}
	}

	/**
	 * Update the roles that are to assigned to a user for a project.
	 */
	public void doRoles(PipelineData pipelineData, Context context) throws Exception 
	{
		RunData data = (RunData) pipelineData;

		try 
		{
			/*
			 * Get the user we are trying to update. The username has been hidden in the
			 * form so we will grab the hidden username and use that to retrieve the user.
			 */
			String username = data.getParameters().getString("username");
			if (!StringUtils.isEmpty(username)) 
			{
				if (security.accountExists(username)) 
				{
					User user = security.getUser(username);

					// Get the Turbine ACL implementation
					TurbineAccessControlList acl = security.getUserManager().getACL(user);

					/*
					 * Grab all the Groups and Roles in the system.
					 */
					GroupSet groups = security.getAllGroups();
					RoleSet roles = security.getAllRoles();

					for (Group group : groups) 
					{
						String groupName = group.getName();
						for (Role role : roles) 
						{
							String roleName = role.getName();

							/*
							 * In the UserRoleForm.vm we made a checkbox for every possible Group/Role
							 * combination so we will compare every possible combination with the values
							 * that were checked off in the form. If we have a match then we will grant the
							 * user the role in the group.
							 */
							String groupRole = groupName + roleName;
							String formGroupRole = data.getParameters().getString(groupRole);
							boolean addGroupRole = false;

							// signal to add group
							if (!StringUtils.isEmpty(formGroupRole))
								addGroupRole = true;

							if (addGroupRole) {
								// only add if new
								if (!acl.hasRole(role, group)) {
									security.grant(user, group, role);
								}

							} else {

								// only remove if it was previously assigned
								if (acl.hasRole(role, group)) {

									// revoke the role for this user
									acl.getRoles(group).remove(role);

									// revoke the user/group/role entry
									security.revoke(user, group, role);
								}
							}

						}
					}

				} else {
					log.error("User does not exist!");
				}
			}

		} catch (Exception e) {
			log.error("Error on role assignment: " + e);
		}
	}

	/**
	 * Implement this to add information to the context.
	 */
	public void doPerform(PipelineData pipelineData, Context context) throws Exception 
	{
		log.info("Running do perform!");
		( (RunData) pipelineData).setMessage("Can't find the requested action!");
	}

}
