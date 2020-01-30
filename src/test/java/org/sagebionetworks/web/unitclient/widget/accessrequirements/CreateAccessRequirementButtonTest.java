package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateAccessRequirementWizard;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import com.google.gwt.event.dom.client.ClickHandler;

public class CreateAccessRequirementButtonTest {
	CreateAccessRequirementButton widget;
	@Mock
	Button mockButton;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	CreateAccessRequirementWizard mockCreateAccessRequirementWizard;;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Mock
	AccessRequirement mockAccessRequirement;
	@Mock
	RestrictableObjectDescriptor mockSubject;
	@Captor
	ArgumentCaptor<ModalWizardWidget.WizardCallback> wizardCallbackCallback;
	@Mock
	Callback mockRefreshCallback;

	ClickHandler onButtonClickHandler;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new CreateAccessRequirementButton(mockButton, mockIsACTMemberAsyncHandler, mockGinInjector);
		when(mockGinInjector.getCreateAccessRequirementWizard()).thenReturn(mockCreateAccessRequirementWizard);
		verify(mockButton).addClickHandler(clickHandlerCaptor.capture());
		onButtonClickHandler = clickHandlerCaptor.getValue();
	}

	@Test
	public void testConstruction() {
		verify(mockButton).setVisible(false);
	}

	@Test
	public void testConfigureWithAR() {
		widget.configure(mockAccessRequirement, mockRefreshCallback);
		verify(mockButton).setText(CreateAccessRequirementButton.EDIT_ACCESS_REQUIREMENT_BUTTON_TEXT);
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackPCaptor.capture());

		CallbackP<Boolean> isACTMemberCallback = callbackPCaptor.getValue();
		// invoking with false should hide the button again
		isACTMemberCallback.invoke(false);
		verify(mockButton, times(2)).setVisible(false);

		isACTMemberCallback.invoke(true);
		verify(mockButton).setVisible(true);

		// configured with an AR, when clicked it should pop up the wizard with the existing AR
		onButtonClickHandler.onClick(null);
		verify(mockCreateAccessRequirementWizard).configure(mockAccessRequirement);
		verify(mockCreateAccessRequirementWizard).showModal(wizardCallbackCallback.capture());
		wizardCallbackCallback.getValue().onFinished();
		verify(mockRefreshCallback).invoke();
	}

	@Test
	public void testConfigureWithSubject() {
		widget.configure(mockSubject, mockRefreshCallback);
		verify(mockButton).setText(CreateAccessRequirementButton.CREATE_NEW_ACCESS_REQUIREMENT_BUTTON_TEXT);
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackPCaptor.capture());

		// configured with a subject, when clicked it should pop up the wizard pointing to the new subject
		onButtonClickHandler.onClick(null);
		verify(mockCreateAccessRequirementWizard).configure(mockSubject);
		verify(mockCreateAccessRequirementWizard).showModal(any(ModalWizardWidget.WizardCallback.class));
	}


	@Test
	public void testOnCancelRefreshPage() {
		widget.configure(mockAccessRequirement, mockRefreshCallback);
		onButtonClickHandler.onClick(null);
		verify(mockCreateAccessRequirementWizard).configure(mockAccessRequirement);
		verify(mockCreateAccessRequirementWizard).showModal(wizardCallbackCallback.capture());
		wizardCallbackCallback.getValue().onCanceled();
		verify(mockRefreshCallback).invoke();
	}
}
