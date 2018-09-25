package org.sagebionetworks.web.unitclient.widget.accessrequirements.submission;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.presenter.RejectReasonWidget;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.RejectReasonView;

public class RejectReasonWidgetTest {

    RejectReasonWidget widget;
    @Mock
    RejectReasonView mockView;
    @Mock
    CallbackP<String> getReasonCallback;
    
    public static final String USER_ID = "0";
    public static final String CUSTOM_RESPONSE = "Fill out paperwork";
    public static final String CANNED_RESPONSE = "canned response";
    public static final String DISPLAY_NAME = "Bob Jones";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockView.isOptionOneUsed()).thenReturn(false);
        when(mockView.isOptionTwoUsed()).thenReturn(false);
        when(mockView.isOptionThreeUsed()).thenReturn(false);
        when(mockView.isOptionFourUsed()).thenReturn(false);
        when(mockView.isOptionFiveUsed()).thenReturn(false);

        widget = new RejectReasonWidget(mockView);
    }

    @Test
    public void testConstructor() {
        verify(mockView).setPresenter(widget);
    }

    @Test
    public void testShowOnSuccess() {
        // call
        widget.show(USER_ID, getReasonCallback);
        // verify/assert
        verify(mockView).clear();
        verify(mockView).show();
        
        // verify save onSave callback
        when(mockView.getValue()).thenReturn(CANNED_RESPONSE);

        widget.onSave();

        verify(mockView).hide();
        verify(getReasonCallback).invoke(CANNED_RESPONSE);
    }

    /*
        Test functionality clicking single checkboxes
    */
    @Test
    public void testUpdateResponseWithOneOptions() {
        // setup
        String exp = RejectReasonWidget.TEMPLATE_HEADER_HELLO  + "null" +  RejectReasonWidget.TEMPLATE_HEADER_THANKS;
        // add response one
        exp += "\n\t" + RejectReasonWidget.REJECT_TAKE_SYNAPSE_QZ  + "\n";
        // add signature
        exp += "\n" + RejectReasonWidget.TEMPLATE_HEADER_SIGNATURE;
        // first checkbox used
        when(mockView.isOptionOneUsed()).thenReturn(true);
        when(mockView.getValue()).thenReturn(exp);

        widget.updateResponse();

        // verify that clear and get value were called
        verify(mockView).setValue(exp);
        verify(mockView).clearError();
    }


    /*
        Test functionality clicking two checkboxes and filling out
        the textarea
     */
    @Test
    public void testUpdateResponseWithThreeOptions() {
    	widget.show(DISPLAY_NAME, getReasonCallback);
    	
        String exp = RejectReasonWidget.TEMPLATE_HEADER_HELLO + DISPLAY_NAME +  RejectReasonWidget.TEMPLATE_HEADER_THANKS;
        // add response one
        exp += "\n\t" + RejectReasonWidget.REJECT_TAKE_SYNAPSE_QZ + "\n";
        exp += "\n\t" + RejectReasonWidget.REJECT_ADD_INFO + "\n";
        exp += "\n\t" + CUSTOM_RESPONSE + "\n";
        // add signature
        exp += "\n"  + RejectReasonWidget.TEMPLATE_HEADER_SIGNATURE;
        // first two checkboxes are used
        when(mockView.isOptionOneUsed()).thenReturn(true);
        when(mockView.isOptionTwoUsed()).thenReturn(true);
        when(mockView.isOptionFiveUsed()).thenReturn(true);
        when(mockView.getCustomTextResponse()).thenReturn(CUSTOM_RESPONSE);
        when(mockView.getValue()).thenReturn(exp);

        widget.updateResponse();

        verify(mockView).setValue(exp);
        verify(mockView).clearError();
    }

    @Test
    public void testUpdateResponseNone() {
        when(mockView.getValue()).thenReturn("");

        widget.updateResponse();

        verify(mockView).showError(RejectReasonWidget.ERROR_MESSAGE);
    }

    @Test
    public void testOnSaveNoText() {
        when(mockView.getValue()).thenReturn("");

        widget.onSave();

        verify(mockView).showError(RejectReasonWidget.ERROR_MESSAGE);
    }

}
