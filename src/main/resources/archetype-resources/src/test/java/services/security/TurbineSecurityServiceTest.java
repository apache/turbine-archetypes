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
import static org.junit.jupiter.api.Assertions.*;

import org.apache.fulcrum.security.GroupManager;
import org.apache.fulcrum.security.ModelManager;
import org.apache.fulcrum.security.RoleManager;
import org.apache.fulcrum.security.entity.ExtendedUser;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.fulcrum.security.model.turbine.TurbineModelManager;
import org.apache.fulcrum.security.model.turbine.entity.TurbineUser;
import org.apache.fulcrum.security.model.turbine.entity.TurbineUserGroupRole;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.fulcrum.testcontainer.BaseUnit5Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.torque.ConstraintViolationException;
import org.apache.turbine.annotation.AnnotationProcessor;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.DefaultUserImpl;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.ServiceManager;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.TurbineConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Test that the SecurityService works properly by comparing behaviour of Turbine and Fulcrum security services using
 * Torque and Turbine user manager. Requires MySQL
 *
 * @author <a href="mailto:gk@apache.org">Georg Kallidis</a>
 * @version $Id$
 */

public class TurbineSecurityServiceTest
    extends BaseUnit5Test
{

    private static final String TEST_GROUP = "TEST_GROUP";

    private static final String TEST_ROLE = "TEST_Role";

    org.apache.fulcrum.security.SecurityService fulcrumSecurityService;
    
    @TurbineService
    SecurityService turbineSecurityService;

    static TurbineConfig tc;
    
    ModelManager modelManager;

    boolean onDeleteCascade = true;

    Logger log = LogManager.getLogger( getClass().getName() );

    @BeforeAll
    public static void init()
    {
        tc = new TurbineConfig( ".", "src/test/conf/torque/turbine/CompleteTurbineResources.properties" );
        tc.initialize();
    }

    @BeforeEach
    public void setUpBefore()
        throws Exception
    {
        AnnotationProcessor.process(this);
//      turbineSecurityService = (SecurityService) TurbineServices.getInstance().getService( SecurityService.SERVICE_NAME );
        
        ServiceManager serviceManager = TurbineServices.getInstance();
        fulcrumSecurityService = (org.apache.fulcrum.security.SecurityService) serviceManager.getService( org.apache.fulcrum.security.SecurityService.ROLE );
        
//      setConfigurationFileName("src/test/conf/torque/fulcrumComponentConfiguration.xml");
//      setRoleFileName("src/test/conf/torque/fulcrumRoleConfiguration.xml");
//      fulcrumSecurityService  = (org.apache.fulcrum.security.SecurityService) lookup(org.apache.fulcrum.security.SecurityService.ROLE);
//      factory = (ACLFactory) lookup(ACLFactory.ROLE);
//      modelManager  = (TurbineModelManager) lookup(TurbineModelManager.ROLE);
    }

    @Test
    public void testAccountExists()
        throws Exception
    {
        // User user = new org.apache.turbine.om.security.DefaultUserImpl(new TurbineUserImpl());
        User user = turbineSecurityService.getUserInstance();
        user.setAccessCounter( 5 );
        assertFalse( turbineSecurityService.accountExists( user ) );
        assertFalse( fulcrumSecurityService.getUserManager().checkExists( user ) );

    }

    @Test
    // @Ignore(value="Turbine security service supported requires Turbine 4.0.1")
    public void testCreateUser()
        throws Exception
    {
        User user = turbineSecurityService.getUserInstance();
        user.setAccessCounter( 5 );
        user.setName( "ringo" );
        // required not null constraint
        ( (ExtendedUser) user ).setFirstName( user.getName() );
        ( (ExtendedUser) user ).setLastName( user.getName() );
        turbineSecurityService.addUser( user, "fakepasswrod" );
        assertTrue( turbineSecurityService.accountExists( user ) );
        assertTrue( fulcrumSecurityService.getUserManager().checkExists( user ) );
        deleteUser( user );
    }
    
    @Test
    // @Ignore(value="Turbine security service supported requires Turbine 4.0.1")
    public void testPermissionRole()
    {
        Role role = null;
        Permission permission = null;
        Permission permission2 = null;
        try {
            role = turbineSecurityService.getRoleInstance( "TEST_SECRETARY" );
            role = turbineSecurityService.addRole( role );
            permission = turbineSecurityService.getPermissionInstance("TEST_ANSWER_FAX");
            permission = turbineSecurityService.addPermission( permission );
            
            permission2 = turbineSecurityService.getPermissionInstance("TEST_ANSWER_PHONE");
            turbineSecurityService.addPermission( permission2 );
            
            turbineSecurityService.grant( role, permission );
            turbineSecurityService.grant( role, permission2 );
            
            role = turbineSecurityService.getRoleById( (Integer) role.getId());
            
            turbineSecurityService.revokeAll( role );
            turbineSecurityService.removeRole( role );
            // not necessarily required
            permission = turbineSecurityService.getPermissionById( (Integer) permission.getId() );
            turbineSecurityService.removePermission( permission );
            //permission2 = turbineSecurityService.getPermissionByName( permission2.getName() );
            //turbineSecurityService.removePermission( permission2 );
        } catch (EntityExistsException e) {
            if (role != null)  deleteRole( role );
            if (permission != null)  deletePermission( permission );
            if (permission2 != null)  deletePermission( permission2 );
        } catch (Exception e) {
            log.error( "error" , e);
            fail();
        }
    }

    /**
     * Tests Turbine Security Manager User / Group / Role
     * 
     * @throws Exception
     */
    @Test
    public void testGrantUserGroupRole()
    {
        Group group = null;
        Role role = null;
        User user = null;
        try
        {            
            // Turbine security service returns a wrapped instance: org.apache.turbine.om.security.DefaultUserImpl
            // which implements org.apache.turbine.om.security.User
            user = turbineSecurityService.getUserInstance( "Clint" );
           
            if (turbineSecurityService.accountExists( user ) ) {
                // only now the user object is updated with id
                user = turbineSecurityService.getUser( "Clint" );
                turbineSecurityService.revokeAll( user );
            } else {          
                // required not null constraint;
                ( (ExtendedUser) user ).setFirstName( user.getName() );
                ( (ExtendedUser) user ).setLastName( user.getName() );
                turbineSecurityService.addUser( user, "clint" );
                user = turbineSecurityService.getUser( "Clint" );
            }
             
            role = addNewTestRole(turbineSecurityService); 
            group = addNewTestGroup(turbineSecurityService); 

            turbineSecurityService.grant( user, group, role );
            
            TurbineAccessControlList tacl = turbineSecurityService.<TurbineAccessControlList>getACL( user );
            
            for (Group aclgroup : tacl.getAllGroups())
            {
                log.info(  "acl group class:" + aclgroup.getClass() );
                log.info(  "acl group class:" + aclgroup );
            }
            
            for (TurbineUserGroupRole userGroupRoleSet : user.getUserGroupRoleSet())
            {
                log.info(  "First Group class:" + userGroupRoleSet.getGroup().getClass() );
                log.info(  "First UserGroupRoleSet:" + userGroupRoleSet );
            }
            assertTrue(tacl.hasRole( role, group ) );
            
            // existing role
            // special global group
            Group globalGroup = turbineSecurityService.getGlobalGroup();
            Role turbineuserRole = turbineSecurityService.getRoleByName( "turbineuser" );
            turbineSecurityService.grant( user, globalGroup, turbineuserRole );
            assertEquals(2, user.getUserGroupRoleSet().size());
            turbineSecurityService.saveUser( user );
            
            // get fresh db user
            user = turbineSecurityService.getUser( "Clint" );
            for (TurbineUserGroupRole userGroupRoleSet : user.getUserGroupRoleSet())
            {
                log.info(  "Second (with turbineuser Group class:" + userGroupRoleSet.getGroup().getClass() );
                log.info(  "UserGroupRoleSet:" + userGroupRoleSet );
            }
            
            Group existingGroup = turbineSecurityService.getGroupByName( "turbine" );
            Role existingRole = turbineSecurityService.getRoleByName( "turbineadmin" );
            turbineSecurityService.grant( user, existingGroup, existingRole );
            
            for (TurbineUserGroupRole userGroupRoleSet : user.getUserGroupRoleSet())
            {
                log.info(  "Third with turbineadmin Group class:" + userGroupRoleSet.getGroup().getClass() );
                log.info(  "UserGroupRoleSet:" + userGroupRoleSet );
            }
            assertEquals(3, user.getUserGroupRoleSet().size());

            user = turbineSecurityService.getUser( "Clint" );
            tacl = turbineSecurityService.<TurbineAccessControlList>getACL( user );
            
            GroupSet aclgroupset = tacl.getGroupSet();
            for (Group aclgroup : aclgroupset )
            {
                log.info(  "last acl group class:" + aclgroup.getClass() );
                log.info(  "acl group class:" + aclgroup );
                RoleSet roles = tacl.getRoles( aclgroup );
                if (roles == null) {
                    log.info( "No roles for group " + aclgroup );
                } else {
                    for (Role aclrole : roles ) {
                        log.info(  "last acl aclrole class:" + aclrole.getClass() );
                        log.info(  "acl aclrole class:" + aclrole );
                    }
                } 
            }
           
            assertTrue(tacl.hasRole( role, group ), 
                    "acl has not role " + role + ", group: " + group );
            assertTrue(tacl.hasRole( existingRole, existingGroup ),  
                    "acl has not role " + existingRole + ", group: " + existingGroup );
            
            log.info( "tacl.hasRole( \"turbineuser\" ):" + tacl.hasRole( "turbineuser" ) );
            //assertTrue(tacl.hasRole( "turbineuser" ), "acl " + tacl + " of user " + user + " has not expected role turbineuser: " + tacl.getRoles() + " in default group global");
            //assertTrue(tacl.hasRole( "turbineuser" ), "acl " + tacl + " of user " + user + " has not expected role turbineuser: " + tacl.getRoles());
            
            turbineSecurityService.revoke( user, existingGroup, existingRole );

            //user = turbineSecurityService.getUser( "Clint" );
            assertTrue( user.getUserGroupRoleSet().size() == 2 );
            userGroupRoleCheck( group, role, user.getUserDelegate() );
            
            tacl = turbineSecurityService.getACL( user );
            // revoked
            assertFalse(tacl.hasRole( existingRole, existingGroup ) );
            
            
        } catch (EntityExistsException e) {
          // cft. finally
            fail(e);
        } catch (Exception e) {
            log.error( "error" , e);
            fail(e);
        }
        finally
        {
            if ( user != null )
                deleteUser( user );
            // not needed as user remove revokes all :
            // if (group != null && role != null && user != null) turbineSecurityService.revoke(user, group, role);
            if ( group != null )
                deleteGroup( group );
            if ( role != null )
                deleteRole( role );
        }
    }

    /**
     * Test Fulcrum Turbine Security Manager User / Group / Role
     * 
     * @throws Exception
     */
    @Test
    public void testGrantUserGroupRoleAlternative()
    {
        Group group = null;
        Role role = null;
        TurbineUser fulcrumUser = null;
        try
        {          
            role = addNewTestRole(fulcrumSecurityService.getRoleManager()); 
            group = addNewTestGroup(fulcrumSecurityService.getGroupManager());
            
            // compare
            // Fulcrum security service returns a raw
            // org.apache.fulcrum.security.model.turbine.entity.impl.TurbineUserImpl,
            org.apache.fulcrum.security.UserManager userManager = fulcrumSecurityService.getUserManager();
            fulcrumUser = userManager.getUserInstance( "Clint2" );
            // clean up
            if (userManager.checkExists( fulcrumUser )) {
                ( (TurbineModelManager) fulcrumSecurityService.getModelManager() ).revokeAll( fulcrumUser );   
            }
            // required not null constraint
            ( (ExtendedUser) fulcrumUser ).setFirstName( fulcrumUser.getName() );
            ( (ExtendedUser) fulcrumUser ).setLastName( fulcrumUser.getName() );
            userManager.addUser( fulcrumUser, "clint2" );
            ( (TurbineModelManager) fulcrumSecurityService.getModelManager() ).grant( fulcrumUser, group, role );
            userGroupRoleCheck( group, role, fulcrumUser );
            
            TurbineAccessControlList tacl = fulcrumSecurityService.getUserManager().getACL( fulcrumUser );
            
            Group existingGroup = fulcrumSecurityService.getGroupManager().getGroupByName( "turbine" );
            Role existingRole = fulcrumSecurityService.getRoleManager().getRoleByName( "turbineadmin" );
            
            assertFalse(tacl.hasRole( existingRole, existingGroup ) );
            
            fulcrumSecurityService.<TurbineModelManager> getModelManager().grant( fulcrumUser, existingGroup, existingRole );
            
            tacl = fulcrumSecurityService.getUserManager().getACL( fulcrumUser );
            assertTrue(tacl.hasRole( existingRole, existingGroup ) );
            
            Role turbineuserRole = fulcrumSecurityService.getRoleManager().getRoleByName( "turbineuser" );
            Group globalGroup = fulcrumSecurityService.<TurbineModelManager> getModelManager().getGlobalGroup();
            fulcrumSecurityService.<TurbineModelManager> getModelManager().grant( fulcrumUser, globalGroup, turbineuserRole );

            tacl = fulcrumSecurityService.getUserManager().getACL( fulcrumUser );
            assertTrue(tacl.hasRole( turbineuserRole, globalGroup ) );
            // global role
            assertTrue(tacl.hasRole( turbineuserRole ) );
            
        } catch (EntityExistsException e) {
          // cft. finally
            fail(e);
        } catch (Exception e) {
            log.error( "error" , e);
            fail(e);
        }
        finally
        {
            if ( fulcrumUser != null )
                deleteUser( new DefaultUserImpl( fulcrumUser ) );
            if ( group != null )
                deleteGroup( group );
            if ( role != null )
                deleteRole( role );
        }
    }
    
    public void testCreatingTurbineACLandModel() throws Exception
    {

    }
    
    /**
     * 
     * @param securityService
     * @return
     * @throws UnknownEntityException
     * @throws DataBackendException
     * @throws EntityExistsException
     */
    private Group addNewTestGroup(SecurityService securityService) throws UnknownEntityException, DataBackendException, EntityExistsException
    {
        Group group = null;
        group = securityService.getGroupInstance( TEST_GROUP );
        
        GroupSet groups =  securityService.getAllGroups();
        for (Group delGroup : groups)
        {               
            if (delGroup.getName().toLowerCase().contains( "test" )) {
                log.warn( "deleting all group: "+ delGroup );
                securityService.removeGroup( delGroup );
            }
        }
       // assign here to delete later as
        group = securityService.addGroup( group );
        return group;
    }

    private Role addNewTestRole(SecurityService securityService) throws DataBackendException, UnknownEntityException, EntityExistsException
    {
        Role role = null;
        RoleSet roles =  securityService.getAllRoles();
        for (Role delRole : roles)
        {               
            if (delRole.getName().toLowerCase().contains( "test" )) {
                log.warn( "deleting all user,groups for role: "+ delRole );
                securityService.revokeAll( delRole, true );
                securityService.removeRole( delRole );
            }
        }
        role = securityService.getRoleInstance( TEST_ROLE );
       // assign here to delete later as
        role = securityService.addRole( role );
        return role;
    }
    
    
    private Group addNewTestGroup(GroupManager groupManager) throws UnknownEntityException, DataBackendException, EntityExistsException
    {
        Group group = null;
        group = groupManager.getGroupInstance( TEST_GROUP );
        
        GroupSet groups =  groupManager.getAllGroups();
        for (Group delGroup : groups)
        {               
            if (delGroup.getName().toLowerCase().contains( "test" )) {
                log.warn( "deleting all group: "+ delGroup );
                groupManager.removeGroup( delGroup );
            }
        }
       // assign here to delete later as
        group = groupManager.addGroup( group );
        return group;
    }

    private Role addNewTestRole(RoleManager roleManager) throws DataBackendException, UnknownEntityException, EntityExistsException
    {
        Role role = null;
        RoleSet roles =  roleManager.getAllRoles();
        for (Role delRole : roles)
        {               
            if (delRole.getName().toLowerCase().contains( "test" )) {
                log.warn( "deleting all user,groups for role could not be done with fulcrum security service! " );
                roleManager.removeRole( delRole );
            }
        }
        role = roleManager.getRoleInstance( TEST_ROLE );
       // assign here to delete later as
        role = roleManager.addRole( role );
        return role;
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
    private void userGroupRoleCheck( Group group, Role role, TurbineUser user ) throws DataBackendException
    {

        boolean ugrFound = false;
        TurbineUserGroupRole ugrTest = null;
        for ( TurbineUserGroupRole ugr : ( (TurbineUser) user ).getUserGroupRoleSet() )
        {
            if ( ugr.getUser().equals( user ) && ugr.getGroup().equals( group ) && ugr.getRole().equals( role ) )
            {
                ugrFound = true;
                ugrTest = ugr;
                break;
            }
        }
        assertTrue( ugrFound, "Not found user" + user + " with role " + role + " in group " + group);
        assertTrue( ugrTest.getGroup().equals( group ) );
        assertTrue( ugrTest.getUser().equals( user ) );
    }

    @AfterAll
    public static void setupAfter()
    {
        ServiceManager serviceManager = TurbineServices.getInstance();
        serviceManager.shutdownService( org.apache.turbine.services.security.SecurityService.SERVICE_NAME );
        serviceManager.shutdownServices();
        // TODO cleanup added users, roles, permissions ..
    }
    
    private void deletePermission( Permission permission )
    {
        try
        {
            if (permission.getId() == null) {
                permission = turbineSecurityService.getPermissionByName(permission.getName() );
            }
            log.info( "deleting permission " + permission.getName() );
            turbineSecurityService.removePermission(  permission );
            permission = null;// not needed
        }
        catch ( UnknownEntityException e )
        {
           // not found / existing
            log.error( e.getMessage(), e );
        }
        catch (Exception e ) {
            log.error( e.getMessage(), e );
            fail();
        }
    }

    private void deleteRole( Role role )
    {
        try
        {
            if (role.getId() == null) {
                role = turbineSecurityService.getRoleByName(role.getName() );
            }
            log.info( "deleting role " + role.getName() );
            turbineSecurityService.removeRole( role );
            role = null;
        }
        catch ( UnknownEntityException e )
        {
           // not found / existing
            log.error( e.getMessage(), e );
        }
        catch (Exception e ) {
            log.error( e.getMessage(), e );
            fail();
        }
    }

    private void deleteGroup( Group group )
    {
        try
        {
            if (group.getId() == null) {
                group = turbineSecurityService.getGroupByName(group.getName() );
            }
            log.info( "deleting group " + group.getName() );
            
            turbineSecurityService.removeGroup( group );
            group = null;
        }
        catch ( UnknownEntityException e )
        {
           // not found / existing
            log.error( e.getMessage(), e );
        }
        catch (Exception e ) {
            log.error( e.getMessage(), e );
            fail();
        }
    }

    private void deleteUser( User user )
    {
        if ( onDeleteCascade )
        {
            try
            {
                // revokeAll is called before user delete
                turbineSecurityService.removeUser( user );
                log.info( "try to delete user " + user.getName() );
            }
            catch ( Exception e )
            {
                log.error( "deleting user " + user.getName() + " failed. " + e.getMessage() );
                if ( e.getCause() != null && e.getCause() instanceof ConstraintViolationException )
                {
                    log.info( "error due to " + e.getCause().getMessage() );
                }
                else
                {
                    log.info( "error due to " + e.getMessage() );
                }
            }
        }
        else
        {
            log.info( "onDeleteCascade false, user " + user.getName() + " not deleted!" );
        }
    }

}
