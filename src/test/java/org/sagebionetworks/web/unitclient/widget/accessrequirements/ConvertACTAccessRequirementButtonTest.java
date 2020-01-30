package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.accessrequirements.ConvertACTAccessRequirementButton.CONVERT_AR_CONFIRM_MESSAGE;
import static org.sagebionetworks.web.client.widget.accessrequirements.ConvertACTAccessRequirementButton.CONVERT_AR_CONFIRM_TITLE;
import static org.sagebionetworks.web.client.widget.accessrequirements.ConvertACTAccessRequirementButton.CONVERT_TO_MANAGED;
import static org.sagebionetworks.web.client.widget.accessrequirements.ConvertACTAccessRequirementButton.DELETE_OLD_INSTRUCTIONS_MESSAGE;
import static org.sagebionetworks.web.client.widget.accessrequirements.ConvertACTAccessRequirementButton.SUCCESS_MESSAGE;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementConversionRequest;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.ConvertACTAccessRequirementButton;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConvertACTAccessRequirementButtonTest {
	ConvertACTAccessRequirementButton widget;
	@Mock
	Button mockButton;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Mock
	ACTAccessRequirement mockAccessRequirement;
	@Mock
	PopupUtilsView mockPopupUtilsView;
	@Mock
	DataAccessClientAsync mockDataAccessClientAsync;
	@Captor
	ArgumentCaptor<AccessRequirementConversionRequest> conversionRequestCaptor;
	@Mock
	Callback mockRefreshCallback;

	public static final Long AR_ID = 87654444L;
	public static final Long AR_VERSION = 3L;
	public static final String AR_ETAG = "987djca";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new ConvertACTAccessRequirementButton(mockButton, mockIsACTMemberAsyncHandler, mockPopupUtilsView, mockDataAccessClientAsync);
		when(mockAccessRequirement.getId()).thenReturn(AR_ID);
		when(mockAccessRequirement.getVersionNumber()).thenReturn(AR_VERSION);
		when(mockAccessRequirement.getEtag()).thenReturn(AR_ETAG);
		widget.configure(mockAccessRequirement, mockRefreshCallback);
	}

	@Test
	public void testConstruction() {
		verify(mockButton).setVisible(false);
		verify(mockButton).setText(CONVERT_TO_MANAGED);
		verify(mockButton).setType(ButtonType.WARNING);
		verify(mockButton).addClickHandler(widget);
	}

	@Test
	public void testConfigureWithAR() {
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackPCaptor.capture());

		CallbackP<Boolean> isACTMemberCallback = callbackPCaptor.getValue();
		// invoking with false should hide the button again
		isACTMemberCallback.invoke(false);
		verify(mockButton, times(2)).setVisible(false);

		isACTMemberCallback.invoke(true);
		verify(mockButton).setVisible(true);
	}

	@Test
	public void testOnClickWithOldInstructions() {
		when(mockAccessRequirement.getActContactInfo()).thenReturn("old instructions are set");

		widget.onClick(null);

		verify(mockPopupUtilsView).showErrorMessage(DELETE_OLD_INSTRUCTIONS_MESSAGE);
	}

	@Test
	public void testOnClick() {
		// no old instructions
		widget.onClick(null);

		verify(mockPopupUtilsView).showConfirmDialog(eq(CONVERT_AR_CONFIRM_TITLE), eq(CONVERT_AR_CONFIRM_MESSAGE), any(Callback.class));
	}

	@Test
	public void testConvertAccessRequirement() {
		AsyncMockStubber.callSuccessWith(mockAccessRequirement).when(mockDataAccessClientAsync).convertAccessRequirement(any(AccessRequirementConversionRequest.class), any(AsyncCallback.class));

		widget.convertAccessRequirement();

		verify(mockDataAccessClientAsync).convertAccessRequirement(conversionRequestCaptor.capture(), any(AsyncCallback.class));
		AccessRequirementConversionRequest request = conversionRequestCaptor.getValue();
		assertEquals(AR_ID.toString(), request.getAccessRequirementId());
		assertEquals(AR_VERSION, request.getCurrentVersion());
		assertEquals(AR_ETAG, request.getEtag());

		verify(mockPopupUtilsView).showInfo(SUCCESS_MESSAGE);
		verify(mockRefreshCallback).invoke();
	}

	@Test
	public void testConvertAccessRequirementFailure() {
		String errorMessage = "bad things happened";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockDataAccessClientAsync).convertAccessRequirement(any(AccessRequirementConversionRequest.class), any(AsyncCallback.class));

		widget.convertAccessRequirement();

		verify(mockDataAccessClientAsync).convertAccessRequirement(conversionRequestCaptor.capture(), any(AsyncCallback.class));

		verify(mockPopupUtilsView).showErrorMessage(errorMessage);
	}
}
