package ${package}.fulcrum.security.torque.turbine;
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
* #set( $H = '#' )
*#
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.fulcrum.security.model.turbine.entity.TurbineGroup;
import org.apache.fulcrum.security.model.turbine.entity.TurbineUserGroupRole;
import org.apache.fulcrum.security.torque.om.TorqueTurbineUserGroupRole;
import org.apache.fulcrum.security.torque.turbine.TorqueAbstractTurbineTurbineSecurityEntity;
import org.apache.torque.TorqueException;
import org.apache.torque.criteria.Criteria;
import org.apache.torque.om.SimpleKey;

import ${package}.om.TurbineGroupPeer;
import ${package}.om.TurbineUserGroupRolePeer;
/**
 * This abstract class provides the SecurityInterface to the managers.
 *
 * @author <a href="mailto:tv@apache.org">Thomas Vandahl</a>
 * @version $Id:$
 */
public abstract class TorqueAbstractTurbineGroup extends TorqueAbstractTurbineTurbineSecurityEntity
    implements TurbineGroup
{
    /** Serial version */
	private static final long serialVersionUID = -6230312046016785990L;

    /**
     * Forward reference to generated code
     *
     * Get a list of association objects, pre-populated with their TurbineRole
     * objects.
     *
     * @param criteria Criteria to define the selection of records
     * @param con a database connection
     * @throws TorqueException
     *
     * @return a list of User/Group/Role relations
     */
    protected List<${package}.om.TurbineUserGroupRole> getTurbineUserGroupRolesJoinTurbineRole(Criteria criteria, Connection con)
        throws TorqueException
    {
        criteria.and(TurbineUserGroupRolePeer.GROUP_ID, getEntityId() );
        return TurbineUserGroupRolePeer.doSelectJoinTurbineRole(criteria, con);
    }

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractSecurityEntity$HgetDatabaseName()
     */
    @Override
	public String getDatabaseName()
    {
        return TurbineGroupPeer.DATABASE_NAME;
    }

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractSecurityEntity$HretrieveAttachedObjects(java.sql.Connection)
     */
    @Override
	public void retrieveAttachedObjects(Connection con) throws TorqueException
    {
        Set<TurbineUserGroupRole> userGroupRoleSet = new HashSet<TurbineUserGroupRole>();

        List<${package}.om.TurbineUserGroupRole> ugrs = getTurbineUserGroupRolesJoinTurbineRole(new Criteria(), con);

        for (${package}.om.TurbineUserGroupRole ttugr : ugrs)
        {
            TurbineUserGroupRole ugr = new TurbineUserGroupRole();
            ugr.setGroup(this);
            ugr.setRole(ttugr.getTurbineRole());
            ugr.setUser(ttugr.getTurbineUser(con));
            userGroupRoleSet.add(ugr);
        }

        setUserGroupRoleSet(userGroupRoleSet);
    }

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractSecurityEntity$Hupdate(java.sql.Connection)
     */
    @Override
	public void update(Connection con) throws TorqueException
    {
    	Set<TurbineUserGroupRole> userGroupRoleSet = getUserGroupRoleSet();
        if (userGroupRoleSet != null)
        {
            Criteria criteria = new Criteria();

            /* remove old entries */
            criteria.where(TurbineUserGroupRolePeer.GROUP_ID, getEntityId());
            TurbineUserGroupRolePeer.doDelete(criteria, con);

            for (TurbineUserGroupRole ugr : userGroupRoleSet)
            {
            	TorqueTurbineUserGroupRole ttugr = new TorqueTurbineUserGroupRole();
                ttugr.setGroupId((Integer)ugr.getGroup().getId());
                ttugr.setUserId((Integer)ugr.getUser().getId());
                ttugr.setRoleId((Integer)ugr.getRole().getId());
                ttugr.save(con);
            }
        }

        try
        {
            save(con);
        }
        catch (Exception e)
        {
            throw new TorqueException(e);
        }
    }

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractSecurityEntity$Hdelete()
     */
    @Override
	public void delete() throws TorqueException
    {
        TurbineGroupPeer.doDelete(SimpleKey.keyFor(getEntityId()));
    }
}
