package org.sagebionetworks.web.unitclient.widget.entity;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidgetView;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import static org.mockito.Mockito.verify;

public class ModifiedCreatedByWidgetTest {

	@Mock
	ModifiedCreatedByWidgetView mockView;
	
	@Mock
	UserBadge mockCreatedByBadge;
	
	@Mock
	UserBadge mockModifiedByBadge;
	
	@Mock
	GWTWrapper mockGWT;
	
	ModifiedCreatedByWidget presenter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		presenter = new ModifiedCreatedByWidget(mockView, mockCreatedByBadge, mockModifiedByBadge, mockGWT);
	}
	
	@Test
	public void testConfigure() {
		Date date = new Date();
		presenter.configure(date, "createdBy", date, "modifiedBy");
		verify(mockCreatedByBadge).configure("createdBy");
		verify(mockModifiedByBadge).configure("modifiedBy");
		verify(mockView).setVisible(true);
		verify(mockView).setCreatedOnText(" on " + mockGWT.getFormattedDateString(date));
		verify(mockView).setModifiedOnText(" on " + mockGWT.getFormattedDateString(date));
	}
	
	@Test
	public void testClear() {
		presenter.clear();
		verify(mockCreatedByBadge).clearState();
		verify(mockModifiedByBadge).clearState();
		verify(mockView).clear();
	}
}
