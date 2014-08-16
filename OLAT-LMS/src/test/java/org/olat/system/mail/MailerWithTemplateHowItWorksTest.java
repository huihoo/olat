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
package org.olat.system.mail;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;

/**
 * This tests shows the usage of velocity templating inside the MailerWithTemplate.
 * For furher templating reference information have a look at the apache velocity documentation.
 * <P>
 * Initial Date:  Oct 5, 2011 <br>
 * @author patrick
 */
public class MailerWithTemplateHowItWorksTest {

	private MailerWithTemplate mailerWithTemplate;
	private VelocityContext context;
	private MailerResult result;
	private StringWriter writer;
	private MailTemplate mailTemplateMock;
	private MailPackageStaticDependenciesWrapper webappAndMailhelperMock;
	private String template;


	@Before
	public void setupMailerWithTemplate() {
		mailTemplateMock = mock(MailTemplate.class);
		when(mailTemplateMock.getFooterTemplate()).thenReturn(ObjectMother.MAIL_TEMPLATE_FOOTER);
		
		webappAndMailhelperMock = mock(MailPackageStaticDependenciesWrapper.class);
		when(webappAndMailhelperMock.getSystemEmailAddress()).thenReturn(ObjectMother.OLATADMIN_EMAIL);
		
		MailerWithTemplate.setUnittestingInstanceWith(webappAndMailhelperMock);
		mailerWithTemplate = MailerWithTemplate.getInstance();
	}
	
	@Before
	public void declareAndInitSomeVariablesUsedPerTest(){
		context = new VelocityContext();
		result = new MailerResult();
		writer = new StringWriter();
		template = null;
	}
	
	@Test
	public void testSimpleReplacement() {
		//setup
		context.put("foo", "bar");
		template = "foo $foo";
		//exercise
		mailerWithTemplate.evaluate(context, template, writer, result);
		//verify
		String templateResult = writer.toString();
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo bar", templateResult);
	}

	@Test
	public void testMetaReplacement(){
		//setup
		writer = new StringWriter();
		context.put("foo", "bar");
		context.put("bar", "anotherfoo");
		String metaTemplate = "foo $$foo";
		//exercise
		mailerWithTemplate.evaluate(context, metaTemplate, writer, result);
		String firstReplacement = writer.toString();
		
		String secondTemplate = firstReplacement;
		writer = new StringWriter();
		mailerWithTemplate.evaluate(context, secondTemplate, writer, result);
		String secondReplacement = writer.toString();
		
		//verify
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo $bar", firstReplacement);
		assertEquals("foo anotherfoo", secondReplacement);
	}
	
	
	@Test
	public void testNoReplacementIfNoDollarSignIsAvailable(){
		//Setup
		writer = new StringWriter();
		context.put("foo", "bar");
		template = "foo foo";
		//exercise
		mailerWithTemplate.evaluate(context, template, writer, result);
		//verify
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo foo", writer.toString());
	}
	
	
	@Test
	public void testNoReplacementIfDollarIsSurroundedWithWhitespaces() {
		//Setup
		writer = new StringWriter();
		context.put("foo", "bar");
		template = "foo $ foo";
		//exercise
		mailerWithTemplate.evaluate(context, template, writer, result);
		//verify
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo $ foo", writer.toString());
	}
	
	@Test
	public void testNoReplacementInTemplateCommentAndCommentIsRemoved() {
		//Setup
		writer = new StringWriter();
		context.put("foo", "bar");
		template = "foo #foo \n##sdf $foo jubla";
		//exercise
		mailerWithTemplate.evaluate(context, template, writer, result);
		//Verify
		assertEquals("foo #foo \n", writer.toString());
	}

	@Test
	public void testIfStatementTrueCase(){
		//Setup
		writer = new StringWriter();
		context.put("bar", Boolean.TRUE);
		template = "foo #if($bar) \nand bar\n#end";
		//exercise
		mailerWithTemplate.evaluate(context, template, writer, result);
		//verify
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo and bar\n", writer.toString());
	}
	
	@Test
	public void testIfStatementFalseCase(){
		//Setup
		writer = new StringWriter();
		context.put("bar", Boolean.FALSE);
		template = "foo #if($bar) \nand bar\n#end";
		//exercise
		mailerWithTemplate.evaluate(context, template, writer, result);
		//verify
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo ", writer.toString());
	}
	
	@Test
	public void testUnclosedIfElseLeadsToIllegalTemplate(){
		//setup
		writer = new StringWriter();
		template = "foo #if";
		//exercise
		mailerWithTemplate.evaluate(context, template, writer, result);
		//verify
		assertEquals(MailerResult.TEMPLATE_PARSE_ERROR, result.getReturnCode());
		assertEquals("", writer.toString());
	}
	
	

}
