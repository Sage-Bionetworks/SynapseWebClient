package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EditRegisteredTeamDialog;
import org.sagebionetworks.web.client.widget.entity.EditRegisteredTeamDialogView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for EditRegisterTeamDialog
 */
public class EditRegisterTeamDialogTest {

	public static final String LOGGED_IN_USER_ID = "008";
	public static final String CHALLENGE_ID = "12";
	EditRegisteredTeamDialog widget;
	EditRegisteredTeamDialogView mockView;
	ChallengeClientAsync mockChallengeClient;
	PlaceChanger mockPlaceChanger;
	Callback mockCallback;

	ChallengeTeam challengeTeam;

	@Before
	public void before() {
		mockChallengeClient = mock(ChallengeClientAsync.class);
		mockView = mock(EditRegisteredTeamDialogView.class);
		mockCallback = mock(Callback.class);
		widget = new EditRegisteredTeamDialog(mockView, mockChallengeClient);
		challengeTeam = new ChallengeTeam();
		challengeTeam.setChallengeId(CHALLENGE_ID);
		challengeTeam.setTeamId("9");
		AsyncMockStubber.callSuccessWith(challengeTeam).when(mockChallengeClient).updateRegisteredChallengeTeam(any(ChallengeTeam.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).unregisterChallengeTeam(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testConfigure() {
		widget.configure(challengeTeam, mockCallback);
		verify(mockView).setRecruitmentMessage("");
		verify(mockView).showModal();

		// click ok
		String newRecruitmentMessage = "an edited message";
		when(mockView.getRecruitmentMessage()).thenReturn(newRecruitmentMessage);
		widget.onOk();
		ArgumentCaptor<ChallengeTeam> challengeTeamCaptor = ArgumentCaptor.forClass(ChallengeTeam.class);
		verify(mockChallengeClient).updateRegisteredChallengeTeam(challengeTeamCaptor.capture(), any(AsyncCallback.class));

		assertEquals(newRecruitmentMessage, challengeTeamCaptor.getValue().getMessage());
		verify(mockCallback).invoke();
		verify(mockView).hideModal();
	}


	@Test
	public void testConfigureUnregister() {
		widget.configure(challengeTeam, mockCallback);
		verify(mockView).setRecruitmentMessage("");
		verify(mockView).showModal();

		// click Unregister
		widget.onUnregister();
		verify(mockChallengeClient).unregisterChallengeTeam(anyString(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
		verify(mockCallback).invoke();
		verify(mockView).hideModal();
	}


	@Test
	public void testConfigureWithChallengeMessage() {
		challengeTeam.setMessage("my message");
		widget.configure(challengeTeam, mockCallback);
		verify(mockView, times(2)).setRecruitmentMessage(anyString());
		verify(mockView).showModal();
	}


}
