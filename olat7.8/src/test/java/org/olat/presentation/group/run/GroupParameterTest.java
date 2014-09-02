/**
 * 
 */
package org.olat.presentation.group.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.presentation.group.run.BusinessGroupSendToChooserFormUIModel.GroupParameter;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.ObjectMother;

/**
 * @author patrick
 * 
 */
public class GroupParameterTest {

    private List<Identity> groupMemberList;
    private String translatedContactListName;
    private List<String> expectedEmailsAsStrings;
    private List<Long> selectedGroupMemberKeys;
    private Identity peterBichsel;
    private Identity heleneMeyer;
    private Identity miaBrenner;
    private Identity retoAlbrecht;

    @Before
    public void setupDataForValidCase() {
        groupMemberList = new ArrayList<Identity>();

        peterBichsel = ObjectMother.getIdentityFrom(ObjectMother.createPeterBichselPrincipal());
        heleneMeyer = ObjectMother.getIdentityFrom(ObjectMother.createHeleneMeyerPrincipial());
        miaBrenner = ObjectMother.getIdentityFrom(ObjectMother.createMiaBrennerPrincipal());
        retoAlbrecht = ObjectMother.getIdentityFrom(ObjectMother.createRetoAlbrechtPrincipal());

        groupMemberList.add(peterBichsel);
        groupMemberList.add(heleneMeyer);
        groupMemberList.add(miaBrenner);
        groupMemberList.add(retoAlbrecht);

        translatedContactListName = "Your translated Name";

        expectedEmailsAsStrings = new ArrayList<String>();
        expectedEmailsAsStrings.add(ObjectMother.RETO_ALBRECHT_PRIVATE_EMAIL);
        expectedEmailsAsStrings.add(ObjectMother.MIA_BRENNER_PRIVATE_EMAIL);
        expectedEmailsAsStrings.add(ObjectMother.PETER_BICHSEL_PRIVATE_EMAIL);
        expectedEmailsAsStrings.add(ObjectMother.HELENE_MEYER_PRIVATE_EMAIL);

        selectedGroupMemberKeys = new ArrayList<Long>();
    }

    @Test
    public void testSimpleValidAllGroupParameterConversionToContactlist() {
        // Setup

        // exercise
        GroupParameter groupParameter = new GroupParameter(groupMemberList, translatedContactListName);
        ContactList asContactList = groupParameter.asContactList();

        // verify
        assertEquals(translatedContactListName, asContactList.getName());
        assertEquals(expectedEmailsAsStrings, asContactList.getEmailsAsStrings());
    }

    @Test(expected = NullPointerException.class)
    public void testNullAsAllGroupMemberListFailsWithExceptionDuringConstructionTime() {
        // setup
        List<Identity> groupMemberList = null;

        // exercise
        GroupParameter groupParameter = new GroupParameter(groupMemberList, translatedContactListName);
        groupParameter.asContactList();

        // verify
        fail("expected Exception not thrown");
    }

    @Test(expected = NullPointerException.class)
    public void testNullAsContactListNameFailsWithExceptionDuringConstructionTime() {
        // setup
        translatedContactListName = null;

        // exercise
        GroupParameter groupParameter = new GroupParameter(groupMemberList, translatedContactListName);
        groupParameter.asContactList();

        // verify
        fail("expected Exception not thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore("removed IllegalArgumentException, could not find a good reason for keep it")
    public void testEmptyAllGroupMemberListFailsWithExceptionDuringConstructionTime() {
        // setup
        List<Identity> groupMemberList = new ArrayList<Identity>();

        // exercise
        GroupParameter groupParameter = new GroupParameter(groupMemberList, translatedContactListName);
        groupParameter.asContactList();

        // verify
        fail("expected Exception not thrown");
    }

    @Test
    public void testSimpleValidSelectGroupParameterConversionToContactlist() {
        // Setup
        expectedEmailsAsStrings.clear();
        selectedGroupMemberKeys.add(miaBrenner.getKey());
        selectedGroupMemberKeys.add(peterBichsel.getKey());
        expectedEmailsAsStrings.add(ObjectMother.MIA_BRENNER_PRIVATE_EMAIL);
        expectedEmailsAsStrings.add(ObjectMother.PETER_BICHSEL_PRIVATE_EMAIL);

        // exercise
        GroupParameter groupParameter = new GroupParameter(groupMemberList, selectedGroupMemberKeys, translatedContactListName);
        ContactList asContactList = groupParameter.asContactList();

        // verify
        assertEquals(translatedContactListName, asContactList.getName());
        assertEquals(expectedEmailsAsStrings, asContactList.getEmailsAsStrings());
    }

    @Test
    public void testWrongSelectionWithNonEmptyGroupParameterConversionToContactlist() {
        // Setup
        expectedEmailsAsStrings.clear();
        selectedGroupMemberKeys.add(Long.valueOf(1));
        selectedGroupMemberKeys.add(Long.valueOf(2));

        // exercise
        GroupParameter groupParameter = new GroupParameter(groupMemberList, selectedGroupMemberKeys, translatedContactListName);
        ContactList asContactList = groupParameter.asContactList();

        // verify
        assertEquals(translatedContactListName, asContactList.getName());
        assertEquals(expectedEmailsAsStrings, asContactList.getEmailsAsStrings());
    }

    @Test
    public void testEmptySelectionWithNonEmptyGroupParameterConversionToContactlist() {
        // Setup
        expectedEmailsAsStrings.clear();

        // exercise
        GroupParameter groupParameter = new GroupParameter(groupMemberList, selectedGroupMemberKeys, translatedContactListName);
        ContactList asContactList = groupParameter.asContactList();

        // verify
        assertEquals(translatedContactListName, asContactList.getName());
        assertEquals(expectedEmailsAsStrings, asContactList.getEmailsAsStrings());
    }

    @Test(expected = NullPointerException.class)
    public void testNullSelectionWithNonEmptyGroupParameterFailsWithExceptionDuringConstructionTime() {
        // setup
        selectedGroupMemberKeys = null;

        // exercise
        GroupParameter groupParameter = new GroupParameter(groupMemberList, selectedGroupMemberKeys, translatedContactListName);
        groupParameter.asContactList();

        // verify
        fail("expected Exception not thrown");
    }
}
