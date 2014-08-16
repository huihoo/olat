/**
 * 
 */
package org.olat.presentation.course.nodes.co;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.ObjectMother;
import org.olat.system.security.OLATPrincipal;

/**
 * @author patrick
 *
 */
public class CORunUIModelTest {

	private String mSubject;
	private String mBody;
	private List<String> emailListConfig;
	private List<String> grpList;
	private List<String> areaList;
	private OLATResourceable  courseOLATResourceable;
	private CourseGroupManager courseGroupManager;
	private Boolean  partipsConfigured;
	private Boolean  coachesConfigured;
	private Translator translator;
	private Identity  identity;
	private String learningObjectives;
	private String longTitle;
	private String shortTitle;

	@Before
	public void setupEmptyAttributes(){
		mSubject = null;
		mBody = null;
		emailListConfig = new ArrayList<String>();
		grpList = new ArrayList<String>();
		areaList = new ArrayList<String>();
		courseOLATResourceable = null;
		courseGroupManager = null;
		partipsConfigured = null;
		coachesConfigured = null;
		
		translator = mock(Translator.class);
		when(translator.translate(CourseContactMessageUIModel.KEY_FORM_MESSAGE_CHCKBX_COACHES)).thenReturn("Coaches");
		when(translator.translate(CourseContactMessageUIModel.KEY_FORM_MESSAGE_CHCKBX_PARTIPS)).thenReturn("Partips");
		when(translator.translate(CourseContactMessageUIModel.KEY_RECIPIENTS)).thenReturn("Recipients");
		

		identity = null;
		learningObjectives = null;
		longTitle = null;
		shortTitle = null;
		
	}
	
	@Test
	public void testInitializationOfCourseContactMessageWithEmptyValues(){
		//setup
		
		//exercise
		CourseContactMessageUIModel courseContactMessageUIModel = new CourseContactMessageUIModel(mSubject, mBody, emailListConfig, grpList, areaList, courseOLATResourceable, courseGroupManager, partipsConfigured, coachesConfigured, translator);
		
		//verify
		Stack<ContactList> contactLists = courseContactMessageUIModel.getContactLists();
		assertTrue(contactLists.isEmpty());
		assertNull(courseContactMessageUIModel.getmBody());
		assertNull(courseContactMessageUIModel.getmSubject());
		
	}
	
	@Test
	public void testContactRunWithEmptyCourseContactMessage() {
		//Setup
		
		//exercise
		CourseContactMessageUIModel emptyCourseContactMessageUIModel = new CourseContactMessageUIModel(mSubject, mBody, emailListConfig, grpList, areaList, courseOLATResourceable, courseGroupManager, partipsConfigured, coachesConfigured, translator);
		ContactRunUIModel contactRunUIModel = new ContactRunUIModel(shortTitle, longTitle, learningObjectives, identity, emptyCourseContactMessageUIModel);
		ContactMessage courseContactMessage = contactRunUIModel.getCourseContactMessage();
		
		//verify all values are null or empty via getters, e.g. no hidden "conversion" of null to "".
		List<OLATPrincipal> disabledIdentities = courseContactMessage.getDisabledIdentities();
		assertTrue(disabledIdentities.isEmpty());
		
		
		List emailToContactLists = courseContactMessage.getEmailToContactLists();
		assertTrue(emailToContactLists.isEmpty());
		
		assertNull(courseContactMessage.getBodyText());
		assertNull(courseContactMessage.getFrom());
		assertNull(courseContactMessage.getSubject());
		
	}

	@Test
	public void testContactMessageWithStringEmailsOnly(){
		//setup
		emailListConfig.add("test01@olat.org");
		emailListConfig.add("test02@olat.org");
		
		//exercise
		CourseContactMessageUIModel emptyCourseContactMessageUIModel = new CourseContactMessageUIModel(mSubject, mBody, emailListConfig, grpList, areaList, courseOLATResourceable, courseGroupManager, partipsConfigured, coachesConfigured, translator);		
		ContactRunUIModel contactRunUIModel = new ContactRunUIModel(shortTitle, longTitle, learningObjectives, identity, emptyCourseContactMessageUIModel);
		ContactMessage courseContactMessage = contactRunUIModel.getCourseContactMessage();

		//verify
		List<ContactList> emailToContactLists = courseContactMessage.getEmailToContactLists();
		assertEquals(1, emailToContactLists.size());
		
		ContactList contactList = emailToContactLists.get(0);
		ArrayList<String> emailsAsStrings = contactList.getEmailsAsStrings();
		assertEquals(2, emailsAsStrings.size());
		
	}
	
	@Test
	public void testContactMessageWithDefinedGroupsAreasAndStringEmailsForParticipantsAndCoaches(){
		//setup
		emailListConfig.add("test01@olat.org");
		emailListConfig.add("test02@olat.org");
		
		coachesConfigured = Boolean.TRUE;
		partipsConfigured = Boolean.TRUE;
		
		grpList.add("rot");
		grpList.add("grün");
		grpList.add("blau");
		
		areaList.add("allgroups");
		
		setupGroupManagerForRedGreenBlueGroupsAndAreas();

		// participants + coaches + EmailListConf
		int expectedNumberOfCreatedContactlists = 3;
		
		//exercise
		CourseContactMessageUIModel emptyCourseContactMessageUIModel = new CourseContactMessageUIModel(mSubject, mBody, emailListConfig, grpList, areaList, courseOLATResourceable, courseGroupManager, partipsConfigured, coachesConfigured, translator);
		ContactRunUIModel contactRunUIModel = new ContactRunUIModel(shortTitle, longTitle, learningObjectives, identity, emptyCourseContactMessageUIModel);
		ContactMessage courseContactMessage = contactRunUIModel.getCourseContactMessage();

		//verify
		List<ContactList> emailToContactLists = courseContactMessage.getEmailToContactLists();
		
		int numberOfCreatedContactLists = emailToContactLists.size();
		assertEquals(expectedNumberOfCreatedContactlists, numberOfCreatedContactLists);
				
	}

	private void setupGroupManagerForRedGreenBlueGroupsAndAreas() {
		courseGroupManager = mock(CourseGroupManager.class);
		List<Identity> redGroup = setupGroup(ObjectMother.createHeidiBirkenstockPrincipal(), ObjectMother.createMiaBrennerPrincipal());
		List<Identity> redGroupCoaches = setupGroup(ObjectMother.createNicolas33Principal());
		
		List<Identity> greenGroup = setupGroup(ObjectMother.createPeterBichselPrincipal(), ObjectMother.createRetoAlbrechtPrincipal());
		List<Identity> blueGroup = setupGroup(ObjectMother.createRuediZimmermannPrincipal(),ObjectMother.createPeterBichselPrincipal());
		
		when(courseGroupManager.getCoachesFromLearningGroup("rot", null)).thenReturn(redGroupCoaches);
		when(courseGroupManager.getParticipantsFromLearningGroup("rot", null)).thenReturn(redGroup);
		when(courseGroupManager.getParticipantsFromLearningGroup("grün", null)).thenReturn(greenGroup);
		when(courseGroupManager.getParticipantsFromLearningGroup("blau", null)).thenReturn(blueGroup);
		when(courseGroupManager.getCoachesFromArea("allgroups", null)).thenReturn(redGroupCoaches);
		List<Identity> allGroupsPartips = new ArrayList<Identity>(redGroup);
		allGroupsPartips.addAll(greenGroup);
		allGroupsPartips.addAll(blueGroup);
		when(courseGroupManager.getParticipantsFromArea("allgroups", null)).thenReturn(allGroupsPartips);
	}

	private List<Identity> setupGroup(OLATPrincipal... principals) {
		List<Identity> identityList = new ArrayList<Identity>();
		for (int i = 0; i < principals.length; i++) {
			OLATPrincipal olatPrincipal = principals[i];
			Identity olatIdentity = ObjectMother.getIdentityFrom(olatPrincipal);
			identityList.add(olatIdentity);	
		}
		return identityList;
	}
	
	
}
