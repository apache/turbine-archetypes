package ${package}.flux.modules.actions.group;

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
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.turbine.annotation.TurbineConfiguration;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import ${package}.flux.modules.actions.FluxAction;

/**
 * Action to manage groups in Turbine.
 * 
 */
public class FluxGroupAction extends FluxAction {

	private static Log log = LogFactory.getLog(FluxGroupAction.class);
	private static String GROUP_ID = "group";

	/** Injected service instance */
	@TurbineService
	private SecurityService security;

	/** Injected configuration instance */
	@TurbineConfiguration
	private Configuration conf;

	/**
	 * ActionEvent responsible for inserting a new user into the Turbine security
	 * system.
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

		String name = data.getParameters().getString(GROUP_ID);
		if (!StringUtils.isEmpty(name)) {

			try {
				// create the group
				Group group = security.getGroupInstance();
				group.setName(name);
				security.addGroup(group);
			} catch (EntityExistsException eee) {

				context.put("name", name);
				context.put("errorTemplate", "group,GroupAlreadyExists.vm");

				/*
				 * We are still in insert mode. So keep this value alive.
				 */
				data.getParameters().add("mode", "insert");
				setTemplate(data, "group,GroupForm.vm");
			} catch (Exception e) {
				log.error("Something bad happened");
			}
		} else {
			log.error("Cannot add empty group name");
		}

	}

	/**
	 * Update a group name
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
		String groupName = data.getParameters().getString("oldName");
		if (!StringUtils.isEmpty(groupName)) {
			Group group = security.getGroupByName(groupName);
			String name = data.getParameters().getString(GROUP_ID);
			if (group != null && !StringUtils.isEmpty(name)) {
				try {
					security.renameGroup(group, name);
				} catch (UnknownEntityException uee) {
					/*
					 * Should do something here but I still think we should use the an id so that
					 * this can't happen.
					 */
				}
			} else {
				log.error("Cannot update group to empty name");
			}
		} else {
			log.error("Cannot update group to empty name");
		}
	}

	/**
	 * ActionEvent responsible for removing a user.
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
			Group group = security.getGroupByName(data.getParameters().getString(GROUP_ID));
			if ( group != null ) {
				security.removeGroup(group);
			} else {
				data.setMessage("Could not find group to remove");
			}
		} catch (UnknownEntityException uee) {
			/*
			 * Should do something here but I still think we should use the an id so that
			 * this can't happen.
			 */
			log.error(uee);
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
