package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.presenter.SynapseForumPresenter.DEFAULT_IS_MODERATOR;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.SynapseForumPlace;
import org.sagebionetworks.web.client.presenter.SynapseForumPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.SynapseForumView;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;

public class SynapseForumPresenterTest {
	@Mock
	SynapseForumView mockView;
	@Mock
	Forum mockForum;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	ForumWidget mockForumWidget;
	@Mock
	SynapseForumPlace mockPlace;
	@Mock
	AccessControlList mockACL;
	@Mock
	SynapseProperties mockSynapseProperties;
	SynapseForumPresenter presenter;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		presenter = new SynapseForumPresenter(mockView, mockGlobalApplicationState, mockForumWidget, mockSynapseProperties);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockPlace.toToken()).thenReturn("fake token");
	}

	@Test
	public void testShowForum() {
		String entityId = "syn1";
		presenter.setPlace(mockPlace);
		presenter.showForum(entityId);
		verify(mockForumWidget).configure(anyString(), any(ParameterizedToken.class), eq(DEFAULT_IS_MODERATOR), any(ActionMenuWidget.class), any(CallbackP.class), any(Callback.class));
	}

}
