package ${package}.flux.modules.actions.role;

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

import java.util.Iterator;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.model.turbine.entity.TurbineRole;
import org.apache.fulcrum.security.torque.om.TurbineUserGroupRolePeer;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.PermissionSet;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.commons.lang3.StringUtils;
import org.apache.torque.criteria.Criteria;
import org.apache.turbine.annotation.TurbineConfiguration;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import ${package}.flux.modules.actions.FluxAction;


/**
 * Action to manager roles in Turbine.
 * 
 */
public class FluxRoleAction extends FluxAction {

	private static Log log = LogFactory.getLog(FluxRoleAction.class);
	private static String ROLE_ID = "role";

	/** Injected service instance */
	@TurbineService
	private SecurityService security;

	/** Injected configuration instance */
	@TurbineConfiguration
	private Configuration conf;

	public void doInsert(PipelineData pipelineData, Context context) throws Exception {
		RunData data = (RunData) pipelineData;
		Role role = security.getRoleInstance();
		data.getParameters().setProperties(role);

		String name = data.getParameters().getString(ROLE_ID);
		role.setName(name);

		try {
			security.addRole(role);
		} catch (EntityExistsException eee) {
			context.put("name", name);
			context.put("errorTemplate", "role,FluxRoleAlreadyExists.vm");
			context.put("role", role);
			/*
			 * We are still in insert mode. So keep this value alive.
			 */
			data.getParameters().add("mode", "insert");
			setTemplate(data, "role,FluxRoleForm.vm");
		}

	}

	/**
	 * ActionEvent responsible updating a role. Must check the input for integrity
	 * before allowing the user info to be update in the database.
	 * 
	 * @param data
	 *            Turbine information.
	 * @param context
	 *            Context for web pages.
	 * @exception Exception
	 *                a generic exception.
	 */
	public void doUpdate(PipelineData pipelineData, Context context) throws Exception {
		RunData data = (RunData) pipelineData;
		Role role = security.getRoleByName(data.getParameters().getString("oldName"));
		String name = data.getParameters().getString(ROLE_ID);
		if (role != null && !StringUtils.isEmpty(name)) {
			try {
				security.renameRole(role, name);
			} catch (UnknownEntityException uee) {
				log.error("Could not rename role: " + uee);
			}
		} else {
			data.setMessage("Cannot update a role to an empty name");
			log.error("Cannot update role to empty name");
		}
	}

	/**
	 * ActionEvent responsible for removing a role.
	 * 
	 * @param data
	 *            Turbine information.
	 * @param context
	 *            Context for web pages.
	 * @exception Exception
	 *                a generic exception.
	 */
	public void doDelete(PipelineData pipelineData, Context context) throws Exception {
		RunData data = (RunData) pipelineData;

		try {
			// find the role
			Role role = security.getRoleByName(data.getParameters().getString(ROLE_ID));

			if (role != null) {
				// remove dependencies to users with the role
				removeRoleFromAllUsers(role);

				// remove all permissions
				security.revokeAll(role);

				// now remove the role
				security.removeRole(role);
			} else {
				data.setMessage("Role was not found");
			}
		} catch (UnknownEntityException uee) {
			/*
			 * Should do something here but I still think we should use the an id so that
			 * this can't happen.
			 */
			log.error(uee);
		} catch (Exception e) {
			log.error("Could not remove role: " + e);
		}
	}

	/**
	 * Update the roles that are to assigned to a user for a project.
	 * 
	 * @param data
	 *            Turbine information.
	 * @param context
	 *            Context for web pages.
	 * @exception Exception
	 *                a generic exception.
	 */
	public void doPermissions(PipelineData pipelineData, Context context) throws Exception {

		RunData data = (RunData) pipelineData;
		/*
		 * Grab the role we are trying to update. Always not null
		 */
		TurbineRole role = security.<TurbineRole>getRoleByName(data.getParameters().getString(ROLE_ID));

		/*
		 * Grab the permissions for the role we are dealing with.
		 */
		PermissionSet rolePermissions = role.getPermissions();

		/*
		 * Grab all the permissions.
		 */
		PermissionSet permissions = security.getAllPermissions();

		// id part one
		String roleName = role.getName();
		for (Iterator<Permission> iterator = permissions.iterator(); iterator.hasNext();) {
			Permission permission = iterator.next();
			String permissionName = permission.getName();
			String rolePermission = roleName + permissionName;

			String formRolePermission = data.getParameters().getString(rolePermission);
			if (formRolePermission != null && !rolePermissions.contains(permission)) {
				/*
				 * Checkbox has been checked AND the role doesn't already contain this
				 * permission. So assign the permission to the role.
				 */
				log.debug("adding " + permissionName + " to " + roleName);
				security.grant(role, permission);
				// this might also be done with role.addPermission(permission);
			} else if (formRolePermission == null && rolePermissions.contains(permission)) {
				/*
				 * Checkbox has not been checked AND the role contains this permission. So
				 * remove this permission from the role.
				 */
				log.debug("removing " + permissionName + " from " + roleName);
				security.revoke(role, permission);
				// this might also be done with role.removePermission(permission);
			}

		}
	}

	/**
	 * Implement this to add information to the context.
	 *
	 * @param data
	 *            Turbine information.
	 * @param context
	 *            Context for web pages.
	 * @exception Exception
	 *                a generic exception.
	 */
	public void doPerform(PipelineData pipelineData, Context context) throws Exception {
		log.info("Running do perform!");
		( (RunData) pipelineData).setMessage("Can't find the requested action!");
	}

	/**
	 * Helper method for removing roles, must clear associated users with the role
	 */
	private void removeRoleFromAllUsers(Role role) {
		try {
			Criteria criteria = new Criteria();
			criteria.where(TurbineUserGroupRolePeer.ROLE_ID, role.getId());
			TurbineUserGroupRolePeer.doDelete(criteria);
		} catch (Exception e) {
			log.error("Error removing user, role associations: " + e.toString());
		}
	}

}
