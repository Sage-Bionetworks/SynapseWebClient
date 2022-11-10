package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.dom.client.ClickHandler;
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
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.IntendedDataUseReportButton;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.BigPromptModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.IntendedDataUseReportWidget;
import org.sagebionetworks.web.client.widget.modal.DialogView;

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
  DialogView mockDialog;

  @Mock
  DivView mockDivView;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  IntendedDataUseReportWidget mockIntendedDataUseReportWidget;

  @Before
  public void setUp() throws Exception {
    when(mockAccessRequirement.getId()).thenReturn(AR_ID);
    widget =
      new IntendedDataUseReportButton(
        mockButton,
        mockIsACTMemberAsyncHandler,
        mockDialog,
        mockDivView,
        mockSynAlert,
        mockIntendedDataUseReportWidget
      );
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
    verify(mockButton)
      .setText(IntendedDataUseReportButton.GENERATE_REPORT_BUTTON_TEXT);
    verify(mockIsACTMemberAsyncHandler)
      .isACTActionAvailable(callbackPCaptor.capture());

    CallbackP<Boolean> isACTMemberCallback = callbackPCaptor.getValue();
    // invoking with false should hide the button again
    isACTMemberCallback.invoke(false);
    verify(mockButton, times(2)).setVisible(false);

    isACTMemberCallback.invoke(true);
    verify(mockButton).setVisible(true);

    // configured with an AR, when clicked it should get all approved submissions (for research
    // projects) and show IDUs
    onButtonClickHandler.onClick(null);

    verify(mockIntendedDataUseReportWidget).configure(AR_ID.toString());
    verify(mockDialog).show();
  }
}
