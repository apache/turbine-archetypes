package ${package}.services.security;

import static org.hamcrest.CoreMatchers.describedAs;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.fulcrum.security.GroupManager;
import org.apache.fulcrum.security.PermissionManager;
import org.apache.fulcrum.security.RoleManager;
import org.apache.fulcrum.security.SecurityService;
import org.apache.fulcrum.security.UserManager;
import org.apache.fulcrum.security.entity.ExtendedUser;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.model.turbine.TurbineModelManager;
import org.apache.fulcrum.security.model.turbine.entity.TurbineGroup;
import org.apache.fulcrum.security.model.turbine.entity.TurbineRole;
import org.apache.fulcrum.security.model.turbine.entity.TurbineUser;
import org.apache.fulcrum.security.model.turbine.entity.TurbineUserGroupRole;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.PermissionSet;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.fulcrum.testcontainer.BaseUnit4Test;
import org.apache.torque.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;

/**
 * Initialization of services in implementing tests
 * @author Eric Pugh
 * 
 */
public abstract class AbstractTurbineTorqueModelManagerTest extends BaseUnit4Test
{
    protected Role role;

    protected TurbineModelManager modelManager;

    protected RoleManager roleManager;

    protected GroupManager groupManager;

    protected PermissionManager permissionManager;

    protected UserManager userManager;

    protected SecurityService securityService;

    @Before
    public void setUp() throws Exception
    {
    	securityService  = (SecurityService) lookup(SecurityService.ROLE);
        roleManager = securityService.getRoleManager();
        userManager = securityService.getUserManager();
        groupManager = securityService.getGroupManager();
        permissionManager = securityService.getPermissionManager();
        modelManager = (TurbineModelManager) securityService.getModelManager();
    }


    @Test
    public void testGetGlobalGroup() throws Exception
    {
        Group global = modelManager.getGlobalGroup();
        assertNotNull(global);
        assertEquals(global.getName(), modelManager.getGlobalGroupName());
    }
    @Test
    public void testGrantRolePermission() throws Exception
    {
        Permission permission = permissionManager.getPermissionInstance();
        permission.setName("ANSWER_PHONE");
        checkAndAddPermission( permission );
        role = roleManager.getRoleInstance("RECEPTIONIST");
        checkAndAddRole( role );
        checkAndGrant( permission );
        role = roleManager.getRoleById(role.getId());
        PermissionSet permissions = ((TurbineRole) role).getPermissions();
        assertEquals(1, permissions.size());
        assertTrue(((TurbineRole) role).getPermissions().contains(permission));
    }

    @Test
    public void testRevokeRolePermission() throws Exception
    {
        Permission permission = securityService.getPermissionManager().getPermissionInstance();
        permission.setName("ANSWER_FAX");
        checkAndAddPermission( permission );
        role = roleManager.getRoleInstance("SECRETARY");
        checkAndAddRole( role );
        checkAndGrant( permission );
        role = roleManager.getRoleById(role.getId());
        PermissionSet permissions = ((TurbineRole) role).getPermissions();
        assertEquals(1, permissions.size());
        checkAndRevoke( permission );
        role = roleManager.getRoleById(role.getId());
        permissions = ((TurbineRole) role).getPermissions();
        assertEquals(0, permissions.size());
        assertFalse(((TurbineRole) role).getPermissions().contains(permission));
    }
    @Test
    public void testRevokeAllRole() throws Exception
    {
        Permission permission = securityService.getPermissionManager().getPermissionInstance();
        Permission permission2 = securityService.getPermissionManager().getPermissionInstance();
        permission.setName("SEND_SPAM");
        permission2.setName("ANSWER_EMAIL");
        checkAndAddPermission( permission );
        checkAndAddPermission( permission2 );
        role = roleManager.getRoleInstance("HELPER");
        checkAndAddRole( role );
        checkAndGrant( permission );
        checkAndGrant( permission2);
        role = roleManager.getRoleById(role.getId());
        PermissionSet permissions = ((TurbineRole) role).getPermissions();
        assertEquals(2, permissions.size());
        try {
            modelManager.revokeAll(role);
        } catch (Exception e) {
            System.out.println( "Might fail " + e.getMessage() );
        }
        role = roleManager.getRoleById(role.getId());
        permissions = ((TurbineRole) role).getPermissions();
        assertEquals(0, permissions.size());
    }

    @Test
    public void testRevokeAllUser() throws Exception
    {
        Group group = securityService.getGroupManager().getGroupInstance();
        group.setName("TEST_REVOKEALLUSER_GROUP");
        group = checkAndAddGroup( group );
        
        role = securityService.getRoleManager().getRoleInstance();
        role.setName("TEST_REVOKEALLUSER_ROLE");
        checkAndAddRole( role );

        User user = userManager.getUserInstance("calvin");
        user = checkAndAddUser( user, "calvin" );
        try {
            modelManager.grant(user, group, role);
        } catch (DataBackendException e ){
            if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                System.out.println( "error due to " + e.getCause().getMessage() );
            } else {
                System.out.println( "error due to " + e.getMessage() );
            }
        }

        group = groupManager.getGroupById(group.getId());
        Set<TurbineUserGroupRole> userGroupRoleSet = ((TurbineGroup) group).getUserGroupRoleSet();
        assertEquals(1, userGroupRoleSet.size());
        Set<TurbineUserGroupRole> userGroupRoleSet2 = ((TurbineGroup) group).getUserGroupRoleSet();
        assertEquals(1, userGroupRoleSet2.size());

        try {
            modelManager.revokeAll(user);
        } catch (Exception e) {
            
        }
        group = groupManager.getGroupById(group.getId());
        assertEquals(0, ((TurbineGroup) group).getUserGroupRoleSet().size());
        role = securityService.getRoleManager().getRoleByName("TEST_REVOKEALLUSER_ROLE");

        // assertFalse(((TurbineRole) role).getGroups().contains(group));

    }

    @Test
    public void testGrantUserGroupRole() throws Exception
    {
        Group group = securityService.getGroupManager().getGroupInstance();
        group.setName("TEST_GROUP");
        group = checkAndAddGroup( group );
        role = roleManager.getRoleInstance();
        role.setName("TEST_Role");
        checkAndAddRole( role );
        User user = userManager.getUserInstance("Clint");
       
        user = checkAndAddUser( user, "clint" );

        try {
            modelManager.grant(user, group, role);
        } catch (DataBackendException e ){
            if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                System.out.println( "error due to " + e.getCause().getMessage() );
            } else {
                System.out.println( "error due to " + e.getMessage() );
            }
        }
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
    @Test
    public void testRevokeUserGroupRole() throws Exception
    {
        Group group = securityService.getGroupManager().getGroupInstance();
        group.setName("TEST_REVOKE");
        group = checkAndAddGroup( group );
        User user = userManager.getUserInstance("Lima");
        
        user = checkAndAddUser( user, "pet" );
        role = roleManager.getRoleInstance();
        role.setName("TEST_REVOKE_ROLE");
        checkAndAddRole( role );
        try {
            modelManager.grant(user, group, role);
        } catch (DataBackendException e ){
            if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                System.out.println( "error due to " + e.getCause().getMessage() );
            } else {
                System.out.println( "error due to " + e.getMessage() );
            }
        }
        try {
            modelManager.revoke(user, group, role);
        } catch (DataBackendException e ){
            if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                System.out.println( "error due to " + e.getCause().getMessage() );
            } else {
                System.out.println( "error due to " + e.getMessage() );
            }
        }
        boolean ugrFound = false;
        for (TurbineUserGroupRole ugr : ((TurbineUser) user).getUserGroupRoleSet())
        {
            if (ugr.getUser().equals(user) && ugr.getGroup().equals(group) && ugr.getRole().equals(role))
            {
                ugrFound = true;
                break;
            }
        }
        assertFalse(ugrFound);
    }

    private void checkAndAddPermission( Permission permission )
                    throws DataBackendException, UnknownEntityException, EntityExistsException
    {
        // cleanup if using real db
        Permission dbPermission;
        if (securityService.getPermissionManager().checkExists( permission )) {
            dbPermission =  securityService.getPermissionManager().getPermissionByName( permission.getName() );
            // this might fail as it is referenced
//            try {
//                securityService.getPermissionManager().removePermission(  permission );
//            } catch (Exception e) {
//                System.out.println("removing permission failed" +  e.getMessage() );
//            }
        } else {
            dbPermission =securityService.getPermissionManager().addPermission(permission);            
        }
        permission.setId( dbPermission.getId() );

    }

    // adds user but might be 
    private User checkAndAddUser( User user, String password )
        throws DataBackendException, UnknownEntityException, EntityExistsException
    {
        if (user instanceof ExtendedUser) {
            // set first last name which might be required almost always
            ((ExtendedUser)user).setFirstName( user.getName() );
            ((ExtendedUser)user).setLastName(  user.getName() );
        }
        if (userManager.checkExists( user )) {
            return userManager.getUser( user.getName() );
            //userManager.removeUser( dbUser );
        } else {
            return userManager.addUser(user, password ); 
        }
    }
    
    private Group checkAndAddGroup( Group group )
                    throws DataBackendException, UnknownEntityException, EntityExistsException
    {
        Group dbGroup;
        // cleanup if using real db
        if (securityService.getGroupManager().checkExists( group )) {
            return securityService.getGroupManager().getGroupByName( group.getName() );
            // might fail as it is referenced
//            try {
//                securityService.getGroupManager().removeGroup( group );
//            }  catch (Exception e) {
//                System.out.println("removing group failed" +  e.getMessage() );
//            }
        } else {
            return securityService.getGroupManager().addGroup(group);
        }
    }


    private void checkAndAddRole( Role role )
        throws DataBackendException, UnknownEntityException, EntityExistsException
    {
        Role dbRole;
        // cleanup if using real db
        if (securityService.getRoleManager().checkExists( role )) {
            dbRole = securityService.getRoleManager().getRoleByName( role.getName() );
            // might fail as it is referenced
//            try {
//                securityService.getRoleManager().removeRole( role );
//            } catch (Exception e) {
//                System.out.println("removing role failed " +  e.getMessage() );
//            }
        } else {
            dbRole = securityService.getRoleManager().addRole(role); 
        }
        if (this.role != null) {
            this.role.setId( dbRole.getId() );
        } else {
            this.role =dbRole;
        }
       
    }
    
    private void checkAndGrant( Permission permission )
                    throws UnknownEntityException
    {
        // short cut if duplicate entry use acl ...
        try {
            modelManager.grant(role, permission);
        } catch (DataBackendException e) {
            System.out.println( "Might be duplicate TODO ACL check" + e.getMessage() );
        }
    }
    
    private void checkAndRevoke( Permission permission )
                    throws UnknownEntityException
    {
        // short cut if duplicate entry use acl ...
        try {
            modelManager.revoke(role, permission);
        } catch (DataBackendException e) {
            System.out.println( "Might be duplicate TODO ACL check" + e.getMessage() );
        }
    }

}
