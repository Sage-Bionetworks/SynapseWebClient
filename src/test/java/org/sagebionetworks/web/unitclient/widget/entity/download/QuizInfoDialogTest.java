package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoWidget;
import org.sagebionetworks.web.client.widget.modal.Dialog;
import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.ui.Widget;


public class QuizInfoDialogTest {

	QuizInfoDialog widget;
	Dialog mockModal;
	QuizInfoWidget mockQuizInfo;
	GlobalApplicationState mockGlobalApplicationState;

	PlaceChanger mockPlaceChanger;

	@Before
	public void before() throws Exception {
		GWTMockUtilities.disarm();
		mockModal = mock(Dialog.class);
		mockQuizInfo = mock(QuizInfoWidget.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		widget = new QuizInfoDialog(mockModal, mockQuizInfo, mockGlobalApplicationState);
	}

	@After
	public void tearDown() {
		// Be nice to the next test
		GWTMockUtilities.restore();
	}

	@Test
	public void testShowAndBecomeCertifiedClick() {
		widget.show();

		ArgumentCaptor<Dialog.Callback> callbackCaptor = ArgumentCaptor.forClass(Dialog.Callback.class);
		// verify that it configures the modal and the quiz info widget
		verify(mockQuizInfo).configure();
		verify(mockModal).configure(anyString(), any(Widget.class), anyString(), anyString(), callbackCaptor.capture(), anyBoolean());
		verify(mockModal).show();

		Dialog.Callback dialogCallback = callbackCaptor.getValue();
		// Verify that it sends the user to the quiz place on primary click
		dialogCallback.onPrimary();
		verify(mockPlaceChanger).goTo(isA(Quiz.class));
	}


	@Test
	public void testAsWidget() {
		// use the modal
		widget.asWidget();
		verify(mockModal).asWidget();
	}

}
