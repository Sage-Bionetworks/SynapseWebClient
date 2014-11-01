package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoWidget;
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.user.client.ui.Widget;


public class QuizInfoDialogTest {
	
	QuizInfoDialog widget;
	Dialog mockModal;
	QuizInfoWidget mockQuizInfo;
	GlobalApplicationState mockGlobalApplicationState;
	CookieProvider mockCookies;
	
	PlaceChanger mockPlaceChanger;
	
	@Before
	public void before() throws Exception {
		mockModal = mock(Dialog.class);
		mockQuizInfo = mock(QuizInfoWidget.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockCookies = mock(CookieProvider.class);
		widget = new QuizInfoDialog(mockModal, mockQuizInfo, mockGlobalApplicationState, mockCookies);
	}

	@Test
	public void testShowIgnoreCertificationReminderCertificationRequired(){
		//simulate that the dialog was previously ignored
		when(mockCookies.getCookie(eq(CookieKeys.IGNORE_CERTIFICATION_REMINDER))).thenReturn("yes");
		//if certification is required, then it should not auto invoke the "remind me later" callback.
		Callback remindMeLaterCallback = mock(Callback.class);
		widget.show(true, remindMeLaterCallback);
		verify(remindMeLaterCallback, never()).invoke();
	}
	
	public void testShowIgnoreCertificationReminder(){
		//simulate that the dialog was previously ignored
		when(mockCookies.getCookie(eq(CookieKeys.IGNORE_CERTIFICATION_REMINDER))).thenReturn("yes");
		Callback remindMeLaterCallback = mock(Callback.class);
		
		//but if certification is not required (perhaps the feature is turned off).  
		//With the cookie set, the callback should be immediately invoked
		widget.show(false, remindMeLaterCallback);
		verify(remindMeLaterCallback).invoke();
		
		//and the modal never configured or shown
		verify(mockModal, never()).configure(anyString(), any(Widget.class), anyString(), anyString(), any(org.sagebionetworks.web.client.widget.modal.Dialog.Callback.class), anyBoolean());
		verify(mockModal, never()).show();
	}

	
	@Test
	public void testShowAndBecomeCertifiedClick(){
		//if certification is required, then it should not auto invoke the "remind me later" callback.
		Callback remindMeLaterCallback = mock(Callback.class);
		widget.show(true, remindMeLaterCallback);
		
		ArgumentCaptor<Dialog.Callback> callbackCaptor = ArgumentCaptor.forClass(Dialog.Callback.class);
		//verify that it configures the modal and the quiz info widget
		verify(mockQuizInfo).configure(anyBoolean());
		verify(mockModal).configure(anyString(), any(Widget.class), anyString(), anyString(), callbackCaptor.capture(), anyBoolean());
		verify(mockModal).show();
		
		Dialog.Callback dialogCallback = callbackCaptor.getValue();
		//Verify that it sends the user to the quiz place on primary click
		dialogCallback.onPrimary();
		verify(mockPlaceChanger).goTo(any(Quiz.class));
	}
	
	@Test
	public void testRemindMeLaterClick(){
		//if certification is required, then it should not auto invoke the "remind me later" callback.
		Callback remindMeLaterCallback = mock(Callback.class);
		widget.show(false, remindMeLaterCallback);
		ArgumentCaptor<Dialog.Callback> callbackCaptor = ArgumentCaptor.forClass(Dialog.Callback.class);
		verify(mockModal).configure(anyString(), any(Widget.class), anyString(), anyString(), callbackCaptor.capture(), anyBoolean());
		Dialog.Callback dialogCallback = callbackCaptor.getValue();
		//Verify that it sends the user to the quiz place on primary click
		dialogCallback.onDefault();
		verify(remindMeLaterCallback).invoke();
	}
	
	@Test
	public void testAsWidget(){
		//use the modal
		widget.asWidget();
		verify(mockModal).asWidget();
	}

}
