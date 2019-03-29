package ${package}.flux.modules.actions.permission;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.torque.om.TurbinePermission;
import org.apache.fulcrum.security.torque.om.TurbinePermissionPeer;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.torque.criteria.Criteria;
import org.apache.turbine.annotation.TurbineConfiguration;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import ${package}.flux.modules.actions.FluxAction;

/**
 * Action to manage permissions in Turbine.
 * 
 * @version $Id: FluxPermissionAction.java,v 1.11 2017/11/16 10:24:41 painter
 *          Exp $
 */
public class FluxPermissionAction extends FluxAction {
	private static Log log = LogFactory.getLog(FluxPermissionAction.class);
	private static String PERM_ID = "permission";

	/** Injected service instance */
	@TurbineService
	private SecurityService security;

	/** Injected configuration instance */
	@TurbineConfiguration
	private Configuration conf;

	/**
	 * ActionEvent responsible for inserting a new permission into the Turbine
	 * security system.
	 * 
	 * @param data
	 *            Turbine information.
	 * @param context
	 *            Context for web pages.
	 * @exception Exception
	 *                a generic exception.
	 */
	public void doInsert(PipelineData pipelineData, Context context) throws Exception {

		RunData data = (RunData) pipelineData;
		String name = data.getParameters().getString(PERM_ID);
		if (!StringUtils.isEmpty(name)) {
			// create the permission
			TurbinePermission tp = new TurbinePermission();
			tp.setName(name);
			tp.setNew(true);
			tp.save();
		} else {
			data.setMessage("Cannot add permission without a name");
			data.getParameters().add("mode", "insert");
			data.setScreen("permission,FluxPermissionForm.vm");
		}

	}

	/**
	 * ActionEvent responsible updating a permission. Must check the input for
	 * integrity before allowing the user info to be update in the database.
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

		String old_name = data.getParameters().getString("oldName");
		String name = data.getParameters().getString(PERM_ID);
		if (!StringUtils.isEmpty(name)) {

			/*
			 * broken Permission perm = security.getPermissionByName(old_name);
			 * security.renamePermission(perm, name);
			 */

			// manual rename
			Criteria criteria = new Criteria();
			criteria.where(TurbinePermissionPeer.PERMISSION_NAME, old_name);
			TurbinePermission tp = TurbinePermissionPeer.doSelectSingleRecord(criteria);
			if (tp != null) {
				tp.setName(name);
				tp.save();
			}

		} else {
			data.setMessage("Cannot find permission to update");
			data.setScreen("permission,FluxPermissionList.vm");
		}
	}

	/**
	 * ActionEvent responsible for removing a permission.
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
			String permName = data.getParameters().getString(PERM_ID);

			if (!StringUtils.isEmpty(permName)) {
				// just
				org.apache.fulcrum.security.model.turbine.entity.TurbinePermission perm = security.<org.apache.fulcrum.security.model.turbine.entity.TurbinePermission>getPermissionByName(
						permName);
				// remove the role-permission links
				RoleSet roles = perm.getRoles();
				for (Role role : roles) {
					security.revoke(role, perm);
				}

				// Remove the permission
				security.removePermission(perm);

			} else {
				data.setMessage("Cannot find permission to delete");
				data.setScreen("permission,FluxPermissionList.vm");
			}
		} catch (Exception e) {
			data.setMessage("An error occured while trying to remove a permission");
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
		RunData data = (RunData) pipelineData;
		log.info("Running do perform!");
		data.setMessage("Can't find the requested action!");
	}

}
