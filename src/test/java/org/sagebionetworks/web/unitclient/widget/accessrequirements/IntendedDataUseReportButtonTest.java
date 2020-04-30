package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.IntendedDataUseGenerator;
import org.sagebionetworks.web.client.widget.accessrequirements.IntendedDataUseReportButton;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.BigPromptModalView;
import com.google.gwt.event.dom.client.ClickHandler;

@RunWith(MockitoJUnitRunner.class)
public class IntendedDataUseReportButtonTest {
	public static final Long AR_ID = 8888L;
	
	IntendedDataUseReportButton widget;
	@Mock
	Button mockButton;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Mock
	AccessRequirement mockAccessRequirement;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	ClickHandler onButtonClickHandler;
	@Mock
	BigPromptModalView mockBigPromptModal;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	IntendedDataUseGenerator mockIntendedDataUseGenerator;
	public static String IDU_REPORT_MD = "Intended Data Use report (md)";

	@Before
	public void setUp() throws Exception {
		when(mockAccessRequirement.getId()).thenReturn(AR_ID);
		widget = new IntendedDataUseReportButton(mockButton, mockIsACTMemberAsyncHandler, mockBigPromptModal, mockIntendedDataUseGenerator);
		verify(mockButton).addClickHandler(clickHandlerCaptor.capture());
		onButtonClickHandler = clickHandlerCaptor.getValue();
	}

	@Test
	public void testConstruction() {
		verify(mockButton).setVisible(false);
	}

	@Test
	public void testConfigureWithAR() {
		widget.configure(mockAccessRequirement);
		verify(mockButton).setText(IntendedDataUseReportButton.GENERATE_REPORT_BUTTON_TEXT);
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackPCaptor.capture());

		CallbackP<Boolean> isACTMemberCallback = callbackPCaptor.getValue();
		// invoking with false should hide the button again
		isACTMemberCallback.invoke(false);
		verify(mockButton, times(2)).setVisible(false);

		isACTMemberCallback.invoke(true);
		verify(mockButton).setVisible(true);

		// configured with an AR, when clicked it should get all approved submissions (for research
		// projects) and show IDUs
		onButtonClickHandler.onClick(null);

		verify(mockIntendedDataUseGenerator).gatherAllSubmissions(eq(AR_ID.toString()), callbackPCaptor.capture());
		CallbackP iduGeneratorCallback = callbackPCaptor.getValue();
		
		iduGeneratorCallback.invoke(IDU_REPORT_MD);
		
		verify(mockBigPromptModal).configure(IntendedDataUseReportButton.IDU_MODAL_TITLE, IntendedDataUseReportButton.IDU_MODAL_FIELD_NAME, IDU_REPORT_MD);
	}
}
