package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.CancelControlWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMembersWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.UserListView;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class CancelControlWidgetTest {
	
	@Mock
	SingleButtonView mockView; 
	@Mock
	ChallengeClientAsync mockChallengeClient;
	@Mock
	AuthenticationController mockAuthController; 
	@Mock
	SynapseAlert mockSynAlert;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	
	CancelControlWidget widget;
	
	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		widget = new CancelControlWidget(mockView, mockChallengeClient, mockAuthController, mockSynAlert, adapterFactory);
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).requestToCancelSubmission(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConstructor() {
		verify(mockView).setButtonText(DisplayConstants.BUTTON_CANCEL);
		verify(mockView).setButtonType(ButtonType.DANGER);
		verify(mockView).setPresenter(widget);
		verify(mockView).addWidget(any(Widget.class));
	}

	
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}











