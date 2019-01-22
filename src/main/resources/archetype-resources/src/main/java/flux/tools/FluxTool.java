package ${package}.flux.tools;

/*
 * Copyright 2001-2018 The Apache Software Foundation.
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

import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.pool.Recyclable;
import org.apache.fulcrum.security.acl.AccessControlList;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.torque.om.TurbinePermission;
import org.apache.fulcrum.security.torque.om.TurbinePermissionPeer;
import org.apache.fulcrum.security.torque.om.TurbineRolePermissionPeer;
import org.apache.fulcrum.security.torque.om.TurbineUserPeer;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.torque.criteria.Criteria;
import org.apache.turbine.annotation.TurbineConfiguration;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;

/**
 * The pull api for flux templates
 *
 * @version $Id: FluxTool.java,v 1.13 2018/04/02 13:24:41 painter Exp $
 */
public class FluxTool implements ApplicationTool, Recyclable {

	/** Used for logging */
	protected static final Log log = LogFactory.getLog(FluxTool.class);

	/** The object containing request specific data */
	private RunData data;

	/** A Group object for use within the Flux API. */
	private Group group = null;

	/** A Issue object for use within the Flux API. */
	private Role role = null;

	/** A Permission object for use within the Flux API. */
	private Permission permission = null;

	/** A User object for use within the Flux API. */
	private User user = null;

	/** Injected service instance */
	@TurbineService
	private SecurityService security;

	/** Injected configuration instance */
	@TurbineConfiguration
	private Configuration conf;

	/**
	 * Constructor
	 */
	public FluxTool() {
	}

	/**
	 * Prepares flux tool for a single request
	 */
	@Override
	public void init(Object runData) {
		this.data = (RunData) runData;
	}

	public Group getGroup() throws Exception {
		String groupName = data.getParameters().getString("group");
		if (StringUtils.isEmpty(groupName)) {
			group = security.getGroupInstance();
		} else {
			group = security.getGroupByName(groupName);
		}
		return group;
	}

	public String getMode() {
		return data.getParameters().getString("mode");
	}

	public GroupSet getGroups() throws Exception {
		return security.getAllGroups();
	}

	public Role getRole() throws Exception {
		String roleName = data.getParameters().getString("role");
		if (StringUtils.isEmpty(roleName)) {
			role = security.getRoleInstance();
		} else {
			role = security.getRoleByName(roleName);
		}
		return role;
	}

	/**
	 */
	public RoleSet getRoles() throws Exception {
		return security.getAllRoles();
	}

	public TurbinePermission getPermission() throws Exception {

		// make sure that the get permission returns the one linked to the role
		String permissionName = data.getParameters().getString("permission");
		if (StringUtils.isEmpty(permissionName)) {
			return null;
		} else {
			try {
				Criteria criteria = new Criteria();
				criteria.where(TurbinePermissionPeer.PERMISSION_NAME, permissionName);
				return TurbinePermissionPeer.doSelectSingleRecord(criteria);
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * Get last cached role - useful after calling getPermissions()
	 * 
	 * @return
	 */
	public Role getCachedRole() {
		return role;
	}

	/**
	 * Return all permissions for a role
	 */
	public List<TurbinePermission> getRolePermissions() throws Exception {

		String roleName = data.getParameters().getString("role");
		if (StringUtils.isEmpty(roleName)) {
			role = security.getRoleInstance();
		} else {
			role = security.getRoleByName(roleName);
		}

		if (role != null) {
			try {
				Criteria criteria = new Criteria();
				criteria.where(TurbineRolePermissionPeer.ROLE_ID, role.getId());
				criteria.addJoin(TurbineRolePermissionPeer.PERMISSION_ID, TurbinePermissionPeer.PERMISSION_ID);
				criteria.addAscendingOrderByColumn(TurbinePermissionPeer.PERMISSION_NAME);
				return TurbinePermissionPeer.doSelect(criteria);
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Return all permissions
	 */
	public List<TurbinePermission> getPermissions() throws Exception {

		try {
			Criteria criteria = new Criteria();
			criteria.addAscendingOrderByColumn(TurbinePermissionPeer.PERMISSION_NAME);
			return TurbinePermissionPeer.doSelect(criteria);
		} catch (Exception e) {
			return null;
		}
	}

	public User getUser() throws Exception {
		String name = data.getParameters().getString("username");
		if (StringUtils.isEmpty(name)) {
			user = security.getUserInstance();
		} else {
			user = security.getUser(name);
		}
		return user;
	}

	public AccessControlList getACL() throws Exception {
		// Get the Turbine ACL implementation
		return security.getUserManager().getACL(getUser());
	}

	/**
	 */
	public String getFieldList() throws Exception {
        StringBuilder selectorBox = new StringBuilder();
        selectorBox.append("<select name=\"fieldList\">");
        selectorBox.append("<option value=\"username\">Username</option>");
        selectorBox.append("<option value=\"firstname\">First Name</option>");
        selectorBox.append("<option value=\"middlename\">Middle Name</option>");
        selectorBox.append("<option value=\"lastname\">Last Name</option>");
        selectorBox.append("</select>");
		return selectorBox.toString();
	}

	/**
	 * This is a tie to the DB implementation something should be added the
	 * pluggable pieces to allow decent parameterized searching.
	 */
	public String getUserFieldList() throws Exception {
        StringBuilder selectorBox = new StringBuilder();
        selectorBox.append("<select name=\"fieldList\">");
        selectorBox.append("<option value=\"" + TurbineUserPeer.LOGIN_NAME + "\">User Name</option>");
        selectorBox.append("<option value=\"" + TurbineUserPeer.FIRST_NAME + "\">First Name</option>");
        selectorBox.append("<option value=\"" + TurbineUserPeer.LAST_NAME + "\">Last Name</option>");
        selectorBox.append("</select>");
		return selectorBox.toString();
	}

	/**
	 * Select all the users and place them in an array that can be used within the
	 * UserList.vm template.
	 */
	public List<User> getUsers() {
		try {
			Criteria criteria = new Criteria();
			String fieldList = data.getParameters().getString("fieldList");

			if (fieldList != null) {
				// This is completely database centric.
				String searchField = data.getParameters().getString("searchField");
				criteria.where(fieldList, searchField, Criteria.LIKE);
			}

			return (List<User>) security.getUserManager().retrieveList(criteria);
		} catch (Exception e) {
			log.error("Could not retrieve user list: " + e.toString());
			return null;
		}
	}

	/**
	 * Implementation of ApplicationTool interface is not needed for this tool as it
	 * is request scoped
	 */
	@Override
	public void refresh() {
		// empty
	}

	// ****************** Recyclable implementation ************************
	private boolean disposed;

	/**
	 * Recycles the object for a new client. Recycle methods with parameters must be
	 * added to implementing object and they will be automatically called by pool
	 * implementations when the object is taken from the pool for a new client. The
	 * parameters must correspond to the parameters of the constructors of the
	 * object. For new objects, constructors can call their corresponding recycle
	 * methods whenever applicable. The recycle methods must call their super.
	 */
	@Override
	public void recycle() {
		disposed = false;
	}

	/**
	 * Disposes the object after use. The method is called when the object is
	 * returned to its pool. The dispose method must call its super.
	 */
	@Override
	public void dispose() {
		group = null;
		role = null;
		permission = null;
		user = null;
		disposed = true;
	}

	/**
	 * Checks whether the recyclable has been disposed.
	 *
	 * @return true, if the recyclable is disposed.
	 */
	@Override
	public boolean isDisposed() {
		return disposed;
	}
}
