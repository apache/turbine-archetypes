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
*#
import java.sql.Connection;
import java.util.List;

import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.torque.TorqueAbstractGroupManager;
import org.apache.fulcrum.security.torque.peer.Peer;
import org.apache.fulcrum.security.torque.peer.PeerManagable;
import org.apache.fulcrum.security.torque.peer.PeerManager;
import org.apache.fulcrum.security.torque.peer.TorqueTurbinePeer;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.criteria.Criteria;

import ${package}.om.TurbineGroupPeer;
/**
 * This implementation persists to a database via Torque.
 * #set( $H = '#' )
 * @author <a href="mailto:tv@apache.org">Thomas Vandahl</a>
 * @version $Id:$
 */
public class TorqueTurbineGroupManagerImpl extends TorqueAbstractGroupManager implements PeerManagable
{
    
    PeerManager peerManager;
    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractGroupManager$HdoSelectAllGroups(java.sql.Connection)
     */
    @Override
	@SuppressWarnings("unchecked")
	protected <T extends Group> List<T> doSelectAllGroups(Connection con) throws TorqueException
    {
        Criteria criteria = new Criteria(TurbineGroupPeer.DATABASE_NAME);
        
        if ( (getCustomPeer())) {
            try
            {
                return ((TorqueTurbinePeer<T>)getPeerInstance()).doSelect( criteria, con );
            }
            catch ( DataBackendException e )
            {
                throw new TorqueException( e );
            }
        } else {
            return (List<T>) TurbineGroupPeer.doSelect(criteria, con);
        }


    }

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractGroupManager$HdoSelectById(java.lang.Integer, java.sql.Connection)
     */
    @Override
	@SuppressWarnings("unchecked")
	protected <T extends Group> T doSelectById(Integer id, Connection con) throws NoRowsException, TooManyRowsException, TorqueException
    {
        if ( (getCustomPeer())) {
            try
            {
                return ((TorqueTurbinePeer<T>) getPeerInstance()).retrieveByPK( id, con );
            }
            catch ( DataBackendException e )
            {
                throw new TorqueException( e );
            }
        } else {
            return  (T)  TurbineGroupPeer.retrieveByPK(id, con);
        }

    }

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractGroupManager$HdoSelectByName(java.lang.String, java.sql.Connection)
     */
    @Override
	@SuppressWarnings("unchecked")
	protected <T extends Group> T doSelectByName(String name, Connection con) throws NoRowsException, TooManyRowsException, TorqueException
    {
        Criteria criteria = new Criteria(TurbineGroupPeer.DATABASE_NAME);
        criteria.where(TurbineGroupPeer.GROUP_NAME, name);
        criteria.setIgnoreCase(true);
        criteria.setSingleRecord(true);
        List<T> groups = null;
        
        if ( (getCustomPeer())) {
            try
            {
                
                groups = ((TorqueTurbinePeer<T>) getPeerInstance()).doSelect( criteria, con );
            }
            catch ( DataBackendException e )
            {
                throw new TorqueException( e );
            }
        } else {
            groups = (List<T>) TurbineGroupPeer.doSelect(criteria, con);
        }

        if (groups.isEmpty())
        {
            throw new NoRowsException(name);
        }

        return groups.get(0);
    }
    
    public Peer getPeerInstance() throws DataBackendException {
        return getPeerManager().getPeerInstance(getPeerClassName(), TorqueTurbinePeer.class, getClassName());
    }
    
    /**
     * @return Returns the persistenceHelper.
     */
    @Override
	public PeerManager getPeerManager()
    {
        if (peerManager == null)
        {
            peerManager = (PeerManager) resolve(PeerManager.ROLE);
        }
        return peerManager;
    }
}
