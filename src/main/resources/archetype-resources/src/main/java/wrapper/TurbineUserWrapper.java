package ${package}.wrapper;

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
