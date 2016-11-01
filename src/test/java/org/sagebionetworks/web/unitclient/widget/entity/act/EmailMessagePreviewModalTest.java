package org.sagebionetworks.web.unitclient.widget.entity.act;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;


import org.sagebionetworks.web.client.widget.entity.act.EmailMessagePreviewModal;
import org.sagebionetworks.web.client.widget.entity.act.EmailMessagePreviewModalView;
import org.sagebionetworks.web.client.widget.entity.act.EmailMessagePreviewModalView.Presenter;


public class EmailMessagePreviewModalTest {

	EmailMessagePreviewModal dialog;
	@Mock
	EmailMessagePreviewModalView mockView;
	
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		mockView = Mockito.mock(EmailMessagePreviewModalView.class);
		
		dialog = new EmailMessagePreviewModal(mockView);
	}
	
	@Test
	public void testConfigure() {
		dialog.configure("message");
		verify(mockView).setMessageBody("message");
	}
	
	@Test
	public void testShow() {
		dialog.show();
		verify(mockView).show();
	}
	
	@Test
	public void testAsWidget() {
		dialog.asWidget();
		verify(mockView, times(2)).setPresenter(any(Presenter.class));
	}
	
}
