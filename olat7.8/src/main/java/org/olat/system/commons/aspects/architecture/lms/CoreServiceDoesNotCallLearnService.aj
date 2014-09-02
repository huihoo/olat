/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.system.commons.aspects;

/**
 * Check dependencies of core-services and learning-services
 *
 * @author Christian Guretzki
 */
public aspect CoreServiceDoesNotCallLearnService {
	
	pointcut scope():
		within(org.olat.lms.core..*);
		//     org.olat.lms.core.hello.impl
		
 
	pointcut useClassesFromAbove(): 
		call(* org.olat.lms.learn..*.*(..)) || call(org.olat.lms.learn..*.new(..));
        //     org.olat.lms.learn.hello.service.HelloWorldService
	declare error
		: scope() && useClassesFromAbove()
		: "CoreServiceDoesNotCallLearnService: You are not allowed to use learning-service from a core-service!";
		

}
