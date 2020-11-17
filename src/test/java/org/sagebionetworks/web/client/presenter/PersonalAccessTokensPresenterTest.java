package org.sagebionetworks.web.client.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.PersonalAccessTokenPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.view.PersonalAccessTokensView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class PersonalAccessTokensPresenterTest {

    @Mock
    PersonalAccessTokensView mockView;

    @Mock
    GlobalApplicationState globalApplicationState;

    @InjectMocks
    PersonalAccessTokensPresenter presenter;

    @Test
    public void testSetPlace() {
        PersonalAccessTokenPlace place = new PersonalAccessTokenPlace("token");

        // Method under test
        presenter.setPlace(place);

        // Called once in the constructor and once on setPlace invocation
        verify(mockView, times(2)).setPresenter(presenter);
    }

    @Test
    public void testStart() {
        GWTMockUtilities.disarm();

        AcceptsOneWidget mockPanel = mock(AcceptsOneWidget.class);
        EventBus mockEventBus = mock(EventBus.class);
        when(mockView.asWidget()).thenReturn(mock(Widget.class));

        // Method under test
        presenter.start(mockPanel, mockEventBus);

        verify(mockView).render();
        verify(mockPanel).setWidget(mockView.asWidget());

        GWTMockUtilities.restore();
    }

    @Test
    public void testGoTo() {
        PlaceChanger mockPlaceChanger = mock(PlaceChanger.class);
        when(globalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
        Profile profilePlace = new Profile("token", Synapse.ProfileArea.SETTINGS);

        // Method under test
        presenter.goTo(profilePlace);

        verify(mockPlaceChanger).goTo(profilePlace);
    }
}