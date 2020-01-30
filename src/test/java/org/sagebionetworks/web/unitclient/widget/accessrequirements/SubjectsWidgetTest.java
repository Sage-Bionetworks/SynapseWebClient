package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;

public class SubjectsWidgetTest {
	SubjectsWidget widget;

	@Mock
	DivView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Mock
	RestrictableObjectDescriptor mockRestrictableObjectDescriptor;
	@Captor
	ArgumentCaptor<CallbackP<Boolean>> callbackCaptor;
	@Mock
	SubjectWidget mockSubjectWidget;
	@Mock
	CallbackP<RestrictableObjectDescriptor> mockDeleteCallback;
	@Captor
	ArgumentCaptor<CallbackP<SubjectWidget>> subjectWidgetCallbackCaptor;
	public static final String ID = "876787";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new SubjectsWidget(mockView, mockGinInjector, mockIsACTMemberAsyncHandler);
		when(mockGinInjector.getSubjectWidget()).thenReturn(mockSubjectWidget);
		when(mockRestrictableObjectDescriptor.getId()).thenReturn(ID);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setVisible(false);
	}

	@Test
	public void testConfigureEntity() {
		when(mockRestrictableObjectDescriptor.getType()).thenReturn(RestrictableObjectType.ENTITY);

		widget.configure(Collections.singletonList(mockRestrictableObjectDescriptor));

		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackCaptor.capture());
		CallbackP<Boolean> callback = callbackCaptor.getValue();

		// verify no widget created if not ACT
		callback.invoke(false);
		verifyZeroInteractions(mockGinInjector);

		// verify widget created if ACT
		callback.invoke(true);
		verify(mockGinInjector).getSubjectWidget();
		verify(mockSubjectWidget).configure(mockRestrictableObjectDescriptor, null);
	}

	@Test
	public void testConfigureTeamWithDeleteCallback() {
		when(mockRestrictableObjectDescriptor.getType()).thenReturn(RestrictableObjectType.TEAM);
		widget.configure(Collections.singletonList(mockRestrictableObjectDescriptor));
		when(mockSubjectWidget.getRestrictableObjectDescriptor()).thenReturn(mockRestrictableObjectDescriptor);
		widget.setDeleteCallback(mockDeleteCallback);
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackCaptor.capture());
		CallbackP<Boolean> callback = callbackCaptor.getValue();
		callback.invoke(true);

		verify(mockGinInjector).getSubjectWidget();
		verify(mockSubjectWidget).configure(eq(mockRestrictableObjectDescriptor), subjectWidgetCallbackCaptor.capture());
		CallbackP<SubjectWidget> callbackP = subjectWidgetCallbackCaptor.getValue();

		// simulate subject deleted by the subjects widget
		callbackP.invoke(mockSubjectWidget);
		verify(mockView).remove(mockSubjectWidget);
		verify(mockDeleteCallback).invoke(mockRestrictableObjectDescriptor);
	}

}
