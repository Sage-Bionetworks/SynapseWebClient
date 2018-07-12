package org.sagebionetworks.web.unitclient.widget.accessrequirements.submission;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.presenter.RejectReasonWidget;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.RejectReasonView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class RejectReasonWidgetTest {

    RejectReasonWidget widget;
    @Mock
    RejectReasonView mockView;
    @Mock
    UserProfileAsyncHandler mockHandler;
    @Mock
    ArgumentCaptor<CallbackP<String>> promptCaptor;
    @Mock
    CallbackP<String> getReasonCallback;
    @Mock
    UserProfile mockUserProfile;
    @Mock
    Throwable mockThrowable;

    public static final String MOCK_USER_ID = "0";
    public static final String MOCK_USER_DISPLAY_NAME = "JOHN DOE";
    public static final String MOCK_CUSTOM_RESPONSE = "Fill out paperwork";
    public static final String MOCK_CANNED_RESPONSE = "canned response";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockView.optionOneIsUsed()).thenReturn(false);
        when(mockView.optionTwoIsUsed()).thenReturn(false);
        when(mockView.optionThreeIsUsed()).thenReturn(false);
        when(mockView.optionFourIsUsed()).thenReturn(false);
        when(mockView.optionFiveIsUsed()).thenReturn(false);
        widget = new RejectReasonWidget(
            mockHandler,
            mockView
        );

    }

    // Test Interface
    @Test
    public void testConstruction() {
        verify(mockView).setPresenter(widget);
    }

    @Test
    public void testShowOnSuccess() {
        // verify username
        // setup
        when(mockUserProfile.getUserName()).thenReturn(MOCK_USER_DISPLAY_NAME);
        AsyncMockStubber.callSuccessWith(mockUserProfile).when(mockHandler).getUserProfile(anyString(), any(AsyncCallback.class));
        // call
        widget.show(MOCK_USER_ID, getReasonCallback);
        // verify/assert
        verify(mockHandler).getUserProfile(eq(MOCK_USER_ID), any(AsyncCallback.class));
        verify(mockView).show();
        assertEquals(MOCK_USER_DISPLAY_NAME, widget.getUserName());

        // verify save onSave callback
        when(mockView.getValue()).thenReturn(MOCK_CANNED_RESPONSE);

        widget.onSave();

        verify(mockView).clear();
        verify(mockView).hide();
        verify(getReasonCallback).invoke(MOCK_CANNED_RESPONSE);
    }

    @Test
    public void testShowOnFailure() {
        // setup
        when(mockThrowable.getMessage()).thenReturn(MOCK_USER_ID);
        AsyncMockStubber.callFailureWith(mockThrowable).when(mockHandler).getUserProfile(anyString(), any(AsyncCallback.class));
        // call
        widget.show(MOCK_USER_ID, getReasonCallback);
        // verify/assert
        verify(mockView).showError( "Could not find user id -- " + MOCK_USER_ID + "\nError -- " + mockThrowable.getMessage());
        verify(mockView).show();
    }

    /*
        Test functionality clicking single checkboxes
    */
    @Test
    public void testUpdateResponseWithOneOptions() {
        // setup
        String exp = RejectReasonWidget.TEMPLATE_HEADER_HELLO  +  RejectReasonWidget.TEMPLATE_HEADER_THANKS;
        // add response one
        exp += "\n\t" + RejectReasonWidget.REJECT_TAKE_SYNAPSE_QZ  + "\n";
        // add signature
        exp += "\n" + RejectReasonWidget.TEMPLATE_HEADER_SIGNATURE;
        // first checkbox used
        when(mockView.optionOneIsUsed()).thenReturn(true);
        when(mockView.getValue()).thenReturn(exp);

        widget.updateResponse();

        // verify that clear and get value were called
        verify(mockView).setValue(exp);
        verify(mockView).clear();
    }


    /*
        Test functionality clicking two checkboxes and filling out
        the textarea
     */
    @Test
    public void testUpdateResponseWithThreeOptions() {
        String exp = RejectReasonWidget.TEMPLATE_HEADER_HELLO + RejectReasonWidget.TEMPLATE_HEADER_THANKS;
        // add response one
        exp += "\n\t" + RejectReasonWidget.REJECT_TAKE_SYNAPSE_QZ + "\n";
        exp += "\n\t" + RejectReasonWidget.REJECT_ADD_INFO + "\n";
        exp += "\n\t" + MOCK_CUSTOM_RESPONSE + "\n";
        // add signature
        exp += "\n"  + RejectReasonWidget.TEMPLATE_HEADER_SIGNATURE;
        // first two checkboxes are used
        when(mockView.optionOneIsUsed()).thenReturn(true);
        when(mockView.optionTwoIsUsed()).thenReturn(true);
        when(mockView.optionFiveIsUsed()).thenReturn(true);
        when(mockView.getCustomTextResponse()).thenReturn(MOCK_CUSTOM_RESPONSE);
        when(mockView.getValue()).thenReturn(exp);

        widget.updateResponse();

        verify(mockView).setValue(exp);
        verify(mockView).clear();
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
