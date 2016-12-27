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

import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.model.turbine.TurbineUserManager;

import ${package}.fulcrum.security.torque.TorqueAbstractUserManager;

import org.apache.fulcrum.security.torque.om.TorqueTurbineUserPeer;
import org.apache.fulcrum.security.torque.peer.Peer;
import org.apache.fulcrum.security.torque.peer.PeerManagable;
import org.apache.fulcrum.security.torque.peer.PeerManager;
import org.apache.fulcrum.security.torque.peer.TorqueTurbinePeer;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.criteria.Criteria;

import ${package}.om.TurbineUserPeer;
import ${package}.om.TurbineUserPeerImpl;

/**
 * This implementation persists to a database via Torque.
 * 
 *
 * @author <a href="mailto:tv@apache.org">Thomas Vandahl</a>
 * @version $Id$
 */
public class TorqueTurbineUserManagerImpl extends TorqueAbstractUserManager implements TurbineUserManager, PeerManagable
{
    PeerManager peerManager;
    private static final String ANON = "anon";

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractUserManager#doSelectAllUsers(java.sql.Connection)
     */
    @Override
	@SuppressWarnings("unchecked")
	protected <T extends User> List<T> doSelectAllUsers(Connection con) throws TorqueException
    {
        Criteria criteria = new Criteria();
        
        if ( (getCustomPeer())) {
            try
            {
                return ((TorqueTurbinePeer<T>) getPeerInstance()).doSelect( criteria, con );
            }
            catch ( DataBackendException e )
            {
                throw new TorqueException( e );
            }
        } else {
            return (List<T>) TorqueTurbineUserPeer.doSelect(criteria, con);
        }
    }

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractUserManager#doSelectById(java.lang.Integer, java.sql.Connection)
     */
    @Override
	@SuppressWarnings("unchecked")
	protected <T extends User> T doSelectById(Integer id, Connection con) throws NoRowsException, TooManyRowsException, TorqueException
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
            return  (T)  TorqueTurbineUserPeer.retrieveByPK(id, con);
        }
    }

    /**
     * @see org.apache.fulcrum.security.torque.TorqueAbstractUserManager#doSelectByName(java.lang.String, java.sql.Connection)
     */
    @Override
	@SuppressWarnings("unchecked")
	protected <T extends User> T doSelectByName(String name, Connection con) throws NoRowsException, TooManyRowsException, TorqueException
    {
        Criteria criteria = new Criteria();
        criteria.setIgnoreCase(true);
        //criteria.setSingleRecord(true);
        
        List<T> users = null;
        if ( (getCustomPeer())) {
            try
            {
              System.out.println("criteria dbname is "+  criteria.getDbName());
              TorqueTurbinePeer<T> peerInstance = (TorqueTurbinePeer<T>)getPeerInstance();
              criteria.where( 
            		TurbineUserPeer.LOGIN_NAME,
                  name);
              System.out.println("peerInstance is "+  peerInstance);
              System.out.println("databae is "+  ((TurbineUserPeerImpl)peerInstance).getDatabaseName());

              System.out.println("checkuser is "+  criteria.toString());
              users = peerInstance.doSelect( criteria, con );
            }
            catch ( DataBackendException e )
            {
                throw new TorqueException( e );
            }
        } else {
            //users = (List<T>) TorqueTurbineUserPeer.doSelect(criteria, con);
        }

         System.out.println("users is "+  users);

        if (users.isEmpty())
        {
            throw new NoRowsException(name);
        }

        System.out.println("user is "+  users.get(0));
        return users.get(0);
    }

    /**
     * Default implementation.
     */
    @Override
    public <T extends User> T getAnonymousUser()
        throws UnknownEntityException
    {
        try
        {
            T anonUser =  getUser( ANON );
            // add more, if needed
            return anonUser;
        }
        catch ( DataBackendException e )
        {
            throw new UnknownEntityException( "Failed to load anonymous user",e);
        } 
    }

    /**
     * Default implementation.
     */
    @Override
    public boolean isAnonymousUser( User u )
    {
        try
        {
            User anon = getAnonymousUser();
            if (u.equals( anon )) 
                {
                 return true;
                }
        }
        catch ( Exception e )
        {
            getLogger().error( "Failed to check user:" + e.getMessage(),e);
        }
        return false;
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
