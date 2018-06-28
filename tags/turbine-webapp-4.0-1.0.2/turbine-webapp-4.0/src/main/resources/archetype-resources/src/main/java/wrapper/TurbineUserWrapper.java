package ${package}.wrapper;

import org.apache.fulcrum.security.model.turbine.entity.TurbineUser;
import org.apache.turbine.om.security.DefaultUserImpl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Custom Wrapper instead fo default Turbine wrapper class {@link DefaultUserImpl} to allow for annotations, 
 * - th single requirment for wrappers is to implement a constructor with parameter {@link TurbineUser}
 *
 */
 @JsonIgnoreProperties( {"permStorage", "type","objectdata" } )
public  class TurbineUserWrapper
    extends DefaultUserImpl
{
   public TurbineUserWrapper(TurbineUser user) {
      super(user);
	}
}
