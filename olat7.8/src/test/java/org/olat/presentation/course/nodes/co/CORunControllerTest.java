package org.olat.presentation.course.nodes.co;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.api.VerificationMode;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.presentation.framework.core.PresentationFrameworkTestContext;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.ObjectMother;

public class CORunControllerTest {

    private ContactRunView contactRunView;
    private ContactRunUIModel contactRunUIModel;

    @Before
    public void setUp() {
        Locale locale = Locale.ENGLISH;
        contactRunView = createDefaultRunView(locale);
    }

    @Test
    public void testInitialViewWithoutRecipients() {
        // Setup
        CourseContactMessageUIModel emptyContactMessageUIModel = configureContactUIModelWithEmptyContactLists();
        contactRunUIModel = createDefaultContactRunUIModel(emptyContactMessageUIModel);

        // exercise
        new CORunController(contactRunView, contactRunUIModel);

        // verify
        verifyCallsToViewExpectation(contactRunView, never());
    }

    @Test
    public void testViewWithRecipients() {
        // Setup
        CourseContactMessageUIModel hasRecipientsContactMessageUIModel = configureContactUIModelWithOneContactLists();
        contactRunUIModel = createDefaultContactRunUIModel(hasRecipientsContactMessageUIModel);

        // exercise
        new CORunController(contactRunView, contactRunUIModel);

        // verify
        verifyCallsToViewExpectation(contactRunView, times(1));

    }

    private void verifyCallsToViewExpectation(ContactRunView contactRunView, VerificationMode verificationMode) {
        verify(contactRunView, verificationMode).setCourseContactMessage(any(ContactMessage.class));
        verify(contactRunView, verificationMode).setLearninObjectives(anyString());
        verify(contactRunView, verificationMode).setLongTitle(anyString());
        verify(contactRunView, verificationMode).setShortTitle(anyString());
    }

    private CourseContactMessageUIModel configureContactUIModelWithOneContactLists() {
        ContactList aContactListMock = ObjectMother.createRecipientsContactList();

        Stack<ContactList> contactLists = new Stack<ContactList>();
        contactLists.add(aContactListMock);

        CourseContactMessageUIModel contactMessageUIModel = mock(CourseContactMessageUIModel.class);
        when(contactMessageUIModel.getContactLists()).thenReturn(contactLists);
        return contactMessageUIModel;
    }

    private CourseContactMessageUIModel configureContactUIModelWithEmptyContactLists() {
        Stack<ContactList> contactLists = new Stack<ContactList>();
        CourseContactMessageUIModel contactMessageUIModel = mock(CourseContactMessageUIModel.class);
        when(contactMessageUIModel.getContactLists()).thenReturn(contactLists);
        return contactMessageUIModel;
    }

    private ContactRunView createDefaultRunView(Locale locale) {
        ContactRunView contactRunView = mock(ContactRunView.class);
        UserRequest ureq = createPresentationFrameworkEnvironment(locale);
        when(contactRunView.getUreq()).thenReturn(ureq);
        return contactRunView;
    }

    private ContactRunUIModel createDefaultContactRunUIModel(CourseContactMessageUIModel contactMessageUIModel) {
        String shortTitle = null;
        String longTitle = null;
        String learningObjectives = null;
        Identity identity = mock(Identity.class);
        ContactRunUIModel contactRunUIModel = new ContactRunUIModel(shortTitle, longTitle, learningObjectives, identity, contactMessageUIModel);
        return contactRunUIModel;
    }

    private UserRequest createPresentationFrameworkEnvironment(Locale locale) {
        PresentationFrameworkTestContext guiTestContext = org.olat.presentation.framework.core.ObjectMother.createPresentationFrameworkEnvironment(locale);
        return guiTestContext.getUserRequest();
    }

}
