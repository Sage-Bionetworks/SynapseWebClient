package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.ReviewAccessRequestsButton;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;

public class ReviewAccessRequestsButtonTest {
	ReviewAccessRequestsButton widget;
	@Mock
	Button mockButton;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Mock
	AccessRequirement mockAccessRequirement;
	@Mock
	RestrictableObjectDescriptor mockSubject;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Captor
	ArgumentCaptor<Place> placeCaptor;
	@Mock
	PlaceChanger mockPlaceChanger;

	ClickHandler onButtonClickHandler;
	public static final Long AR_ID = 87654444L;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new ReviewAccessRequestsButton(mockButton, mockIsACTMemberAsyncHandler, mockGlobalApplicationState);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		verify(mockButton).addClickHandler(clickHandlerCaptor.capture());
		onButtonClickHandler = clickHandlerCaptor.getValue();
		when(mockAccessRequirement.getId()).thenReturn(AR_ID);
	}

	@Test
	public void testConstruction() {
		verify(mockButton).setVisible(false);
		verify(mockButton).setText(ReviewAccessRequestsButton.REVIEW_ACCESS_BUTTON_TEXT);
	}

	@Test
	public void testConfigureWithAR() {
		widget.configure(mockAccessRequirement);
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackPCaptor.capture());

		CallbackP<Boolean> isACTMemberCallback = callbackPCaptor.getValue();
		// invoking with false should hide the button again
		isACTMemberCallback.invoke(false);
		verify(mockButton, times(2)).setVisible(false);

		isACTMemberCallback.invoke(true);
		verify(mockButton).setVisible(true);

		// configured with an AR, when clicked it should direct the ACT place for managing access
		onButtonClickHandler.onClick(null);
		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		Place place = placeCaptor.getValue();
		assertTrue(place instanceof ACTDataAccessSubmissionsPlace);
		assertEquals(AR_ID.toString(), ((ACTDataAccessSubmissionsPlace) place).getParam(ACTDataAccessSubmissionsPlace.ACCESS_REQUIREMENT_ID_PARAM));
	}
}
