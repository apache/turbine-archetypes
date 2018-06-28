package ${package}.services.security;

/*
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
 */
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.fulcrum.security.SecurityService;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.model.turbine.TurbineModelManager;
import org.apache.fulcrum.security.model.turbine.entity.TurbineUser;
import org.apache.fulcrum.security.model.turbine.entity.TurbineUserGroupRole;
import org.apache.fulcrum.security.model.turbine.entity.impl.TurbineUserImpl;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.fulcrum.testcontainer.BaseUnit4Test;
import org.apache.turbine.om.security.TurbineUserDelegate;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.ServiceManager;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.TurbineConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test that the SecurityService works properly by comparing behaviour of Turbine and Fulcrum security services using Torque and Turbine user manager.
 *
 *
 * @author gkallidis
 * @version $Id$
 */

public class TurbineSecurityServiceTest extends BaseUnit4Test
{

    SecurityService fulcrumSecurityService;
    org.apache.turbine.services.security.SecurityService turbineSecurityService;
    static TurbineConfig tc;


    @BeforeClass
    public static void init() throws Exception
    {
        tc = new TurbineConfig(".", "src/test/conf/torque/turbine/CompleteTurbineResources.properties");
        tc.initialize();
    }

    @Before
    public void setUpBefore() throws Exception
    {
    
        ServiceManager serviceManager = TurbineServices.getInstance();
        //
        fulcrumSecurityService = (SecurityService) serviceManager.getService(SecurityService.ROLE);
    
        turbineSecurityService = (org.apache.turbine.services.security.SecurityService)
        		TurbineServices.getInstance().getService(org.apache.turbine.services.security.SecurityService.SERVICE_NAME);
    }
    
    @Test
    public void testAccountExists() throws Exception
    {
    
        User user = new org.apache.turbine.om.security.DefaultUserImpl(new TurbineUserImpl());
        user.setAccessCounter(5);
        assertFalse(turbineSecurityService.accountExists(user));
        assertFalse(fulcrumSecurityService.getUserManager().checkExists(user));
    
    }
    
    @Test
    @Ignore(value="Turbine security service supported requires Turbine 4.0.1")
    public void testCreateUser() throws Exception
    {
    
    	User user = new org.apache.turbine.om.security.DefaultUserImpl(new TurbineUserImpl());
    	user.setAccessCounter(5);
    	user.setName("ringo");
    	turbineSecurityService.addUser(user,"fakepasswrod");
    	assertTrue(turbineSecurityService.accountExists(user));
    	assertTrue(fulcrumSecurityService.getUserManager().checkExists(user));
    
    }
    
    /**
     * Tests Turbine and Fulcrum.
     *
     * Fulcrum part is similar/duplicated from {@link AbstractTurbineModelManagerTest#testGrantUserGroupRole()}
     *
     *
     * @throws Exception
     */
    @Test
    @Ignore(value="Turbine security service supported requires Turbine 4.0.1")
    public void testGrantUserGroupRole() throws Exception
    {
        Group group = fulcrumSecurityService.getGroupManager().getGroupInstance();
        group.setName("TEST_GROUP");
        fulcrumSecurityService.getGroupManager().addGroup(group);
        Role role = fulcrumSecurityService.getRoleManager().getRoleInstance();
        role.setName("TEST_Role");
        fulcrumSecurityService.getRoleManager().addRole(role);
    
        //  Turbine security service returns a wrapped instance: org.apache.turbine.om.security.DefaultUserImpl
        // which implements org.apache.turbine.om.security.User and contains
        User user = turbineSecurityService.getUserInstance("Clint");
    	// memory
        turbineSecurityService.addUser(user, "clint");
        turbineSecurityService.grant(user, group, role);
    
        // this cast may not be required any more in turbine 4.0.1
    	addUserAndCheck(group, role, ( (TurbineUserDelegate) user).getUserDelegate());
    
        // Fulcrum security service returns a raw org.apache.fulcrum.security.model.turbine.entity.impl.TurbineUserImpl,
    	org.apache.fulcrum.security.UserManager  userManager = fulcrumSecurityService.getUserManager();
    	TurbineUser fulcrumUser = userManager.getUserInstance("Clint2");
        userManager.addUser(fulcrumUser, "clint2");         // memory
        ((TurbineModelManager)fulcrumSecurityService.getModelManager()).grant(fulcrumUser, group, role);
    
        addUserAndCheck(group, role, fulcrumUser);
    
    }

    /**
     * Fulcrum contract check
     *
     * @param group Fulcrum interface
     * @param role Fulcrum interface
     * @param user Fulcrum interface
     * @throws EntityExistsException
     * @throws DataBackendException
     * @throws UnknownEntityException
     */
    private void addUserAndCheck(Group group, Role role, TurbineUser user)
        throws EntityExistsException, DataBackendException,
        UnknownEntityException
    {
    
        boolean ugrFound = false;
        TurbineUserGroupRole ugrTest = null;
        for (TurbineUserGroupRole ugr : ((TurbineUser) user).getUserGroupRoleSet())
        {
            if (ugr.getUser().equals(user) && ugr.getGroup().equals(group) && ugr.getRole().equals(role))
            {
                ugrFound = true;
                ugrTest = ugr;
                break;
            }
        }
        assertTrue(ugrFound);
        assertTrue(ugrTest.getGroup().equals(group));
        assertTrue(ugrTest.getUser().equals(user));
    }
    
    
    @AfterClass
    public static void setupAfter()
    {
        ServiceManager serviceManager = TurbineServices.getInstance();
        serviceManager.shutdownService(org.apache.turbine.services.security.SecurityService.SERVICE_NAME);
        serviceManager.shutdownServices();
        // TODO cleanup added users, roles, permissions ..
    }


}
