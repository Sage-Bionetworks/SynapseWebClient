package org.sagebionetworks.web.unitclient.widget.login;

import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.widget.login.LoginModalView;
import org.sagebionetworks.web.client.widget.login.LoginModalWidget;

public class LoginModalWidgetTest {

	@Mock
	LoginModalView mockView;

	LoginModalWidget widget;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		widget = new LoginModalWidget(mockView);
	}

	@Test
	public void testOnSubmitCompleteUploadResult() {
		String errorMessage = "an error message";
		UploadResult result = new UploadResult();
		result.setUploadStatus(UploadStatus.FAILED);
		result.setMessage(errorMessage);
		widget.onSubmitComplete(result);

		verify(mockView).showErrorMessagePopup(errorMessage);
	}

}
