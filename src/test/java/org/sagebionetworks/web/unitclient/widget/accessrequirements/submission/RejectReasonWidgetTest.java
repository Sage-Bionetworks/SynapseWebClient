package org.sagebionetworks.web.unitclient.widget.accessrequirements.submission;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.act.RejectReasonWidget.ERROR_MESSAGE;
import static org.sagebionetworks.web.client.widget.entity.act.RejectReasonWidget.TEMPLATE_HEADER_HELLO;
import static org.sagebionetworks.web.client.widget.entity.act.RejectReasonWidget.TEMPLATE_HEADER_SIGNATURE;
import static org.sagebionetworks.web.client.widget.entity.act.RejectReasonWidget.TEMPLATE_HEADER_THANKS;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.act.RejectReasonView;
import org.sagebionetworks.web.client.widget.entity.act.RejectReasonWidget;

@RunWith(MockitoJUnitRunner.class)
public class RejectReasonWidgetTest {

    RejectReasonWidget widget;
    @Mock
    RejectReasonView mockView;
    @Mock
    CallbackP<String> getReasonCallback;
    
    public static final String SELECTED_CHECKBOXES = "Must do this, and that, and the other thing.";
    public static final String CANNED_RESPONSE = "canned response";
    public static final String DISPLAY_NAME = "Bob Jones";

    @Before
    public void setUp() throws Exception {
        when(mockView.getSelectedCheckboxText()).thenReturn(SELECTED_CHECKBOXES);
        widget = new RejectReasonWidget(mockView);
    }

    @Test
    public void testConstructor() {
        verify(mockView).setPresenter(widget);
    }

    @Test
    public void testShowOnSuccess() {
        // call
        widget.show(DISPLAY_NAME, getReasonCallback);
        // verify/assert
        verify(mockView).clear();
        verify(mockView).show();
        
        // verify save onSave callback
        when(mockView.getValue()).thenReturn(CANNED_RESPONSE);

        widget.onSave();

        verify(mockView).hide();
        verify(getReasonCallback).invoke(CANNED_RESPONSE);
    }

    @Test
    public void testUpdateResponse() {
    	widget.show(DISPLAY_NAME, getReasonCallback);
        String exp = TEMPLATE_HEADER_HELLO  + DISPLAY_NAME +  TEMPLATE_HEADER_THANKS + SELECTED_CHECKBOXES + TEMPLATE_HEADER_SIGNATURE;

        widget.updateResponse();

        verify(mockView).setValue(exp);
    }

    @Test
    public void testOnSaveNoText() {
        when(mockView.getValue()).thenReturn("");

        widget.onSave();

        verify(mockView).showError(ERROR_MESSAGE);
    }
}
