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
import static org.junit.Assert.fail;

import org.apache.fulcrum.security.ModelManager;
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
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.fulcrum.testcontainer.BaseUnit4Test;
import org.apache.torque.ConstraintViolationException;
import org.apache.turbine.annotation.AnnotationProcessor;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.DefaultUserImpl;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.ServiceManager;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.TurbineConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test that the SecurityService works properly by comparing behaviour of Turbine and Fulcrum security services using
 * Torque and Turbine user manager. Requires MySQL
 *
 * @author gkallidis
 * @version $Id$
 */

public class TurbineSecurityServiceTest
    extends BaseUnit4Test
{

    org.apache.fulcrum.security.SecurityService fulcrumSecurityService;
    
    @TurbineService
    SecurityService turbineSecurityService;

    static TurbineConfig tc;
    
    ModelManager modelManager;

    boolean onDeleteCascade = true;

    // By default org.slf4j.LoggerFactory is optional in 4.0, but included in webapp
    Logger log = LoggerFactory.getLogger( getClass().getName() );

    @BeforeClass
    public static void init()
        throws Exception
    {
        tc = new TurbineConfig( ".", "src/test/conf/torque/turbine/CompleteTurbineResources.properties" );
        tc.initialize();
    }

    @Before
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
     * Tests Turbine and Fulcrum.
     * 
     * @throws Exception
     */
    @Test
    // @Ignore(value="Turbine security service supported requires Turbine 4.0.1")
    public void testGrantUserGroupRole()
        throws Exception
    {
        Group group = null;
        Role role = null;
        User user = null;
        TurbineUser fulcrumUser = null;
        try
        {
            group = turbineSecurityService.getGroupInstance( "TEST_GROUP" );
            group = turbineSecurityService.addGroup( group ); // assign here to delete later as
            role = turbineSecurityService.getRoleInstance( "TEST_Role" );
            role = turbineSecurityService.addRole( role );
            // Turbine security service returns a wrapped instance: org.apache.turbine.om.security.DefaultUserImpl
            // which implements org.apache.turbine.om.security.User
            user = turbineSecurityService.getUserInstance( "Clint" );
            // required not null constraint
            ( (ExtendedUser) user ).setFirstName( user.getName() );
            ( (ExtendedUser) user ).setLastName( user.getName() );

            turbineSecurityService.addUser( user, "clint" );
            turbineSecurityService.grant( user, group, role );

            // existing role
            Group existingGroup = turbineSecurityService.getGroupByName( "global" );
            Role existingRole = turbineSecurityService.getRoleByName( "turbineuser" );
            turbineSecurityService.grant( user, existingGroup, existingRole );
            assertTrue( user.getUserGroupRoleSet().size() == 2 );
            // get fresh db user
            user = turbineSecurityService.getUser( "Clint" );
            existingGroup = turbineSecurityService.getGroupByName( "turbine" );
            existingRole = turbineSecurityService.getRoleByName( "turbineadmin" );
            turbineSecurityService.grant( user, existingGroup, existingRole );
            assertTrue( user.getUserGroupRoleSet().size() == 3 );

            TurbineAccessControlList tacl = turbineSecurityService.<TurbineAccessControlList>getACL( user );
            
            assertTrue(tacl.hasRole( existingRole, existingGroup ) );
            assertTrue(tacl.hasRole( "turbineuser" ));
            
            turbineSecurityService.revoke( user, existingGroup, existingRole );

            //user = turbineSecurityService.getUser( "Clint" );
            assertTrue( user.getUserGroupRoleSet().size() == 2 );
            userGroupRoleCheck( group, role, user.getUserDelegate() );
            // compare
            // Fulcrum security service returns a raw
            // org.apache.fulcrum.security.model.turbine.entity.impl.TurbineUserImpl,
            org.apache.fulcrum.security.UserManager userManager = fulcrumSecurityService.getUserManager();
            fulcrumUser = userManager.getUserInstance( "Clint2" );
            // required not null constraint
            ( (ExtendedUser) fulcrumUser ).setFirstName( fulcrumUser.getName() );
            ( (ExtendedUser) fulcrumUser ).setLastName( fulcrumUser.getName() );
            userManager.addUser( fulcrumUser, "clint2" );
            ( (TurbineModelManager) fulcrumSecurityService.getModelManager() ).grant( fulcrumUser, group, role );
            userGroupRoleCheck( group, role, fulcrumUser );
            
            tacl = turbineSecurityService.getACL( user );
            // revoked
            assertFalse(tacl.hasRole( existingRole, existingGroup ) );
;
            
            
        } catch (EntityExistsException e) {
          // cft. finally
            fail();
        } catch (Exception e) {
            log.error( "error" , e);
            fail();
        }
        finally
        {
            if ( fulcrumUser != null )
                deleteUser( new DefaultUserImpl( fulcrumUser ) );
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
    
    public void testCreatingTurbineACLandModel() throws Exception
    {

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
        assertTrue( ugrFound );
        assertTrue( ugrTest.getGroup().equals( group ) );
        assertTrue( ugrTest.getUser().equals( user ) );
    }

    @AfterClass
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
