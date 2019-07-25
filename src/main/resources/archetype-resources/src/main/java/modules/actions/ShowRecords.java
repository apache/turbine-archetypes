package ${package}.modules.actions;

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

import java.util.List;

import org.apache.torque.criteria.Criteria;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import ${package}.om.Author;
import ${package}.om.AuthorPeer;

public class ShowRecords  extends SecureAction 
{

	@Override
	public void doPerform( PipelineData pipelineData, Context context )
	    throws Exception
	{
	    super.doPerform( pipelineData, context );
	    List<Author> authors = AuthorPeer.doSelect( new Criteria() );//all
	    context.put( "authors", authors );
	    RunData data = (RunData) pipelineData;
	    data.setScreenTemplate(Character.toLowerCase(getClass().getSimpleName().charAt(0)) + 
	    		getClass().getSimpleName().substring(1) + ".vm"
	    ); 
	    
	}
}
