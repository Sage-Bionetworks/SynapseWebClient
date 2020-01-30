package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidgetView;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class ModifiedCreatedByWidgetTest {

	@Mock
	ModifiedCreatedByWidgetView mockView;
	@Mock
	UserBadge mockCreatedByBadge;
	@Mock
	UserBadge mockModifiedByBadge;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	ModifiedCreatedByWidget presenter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		presenter = new ModifiedCreatedByWidget(mockView, mockCreatedByBadge, mockModifiedByBadge, mockDateTimeUtils);
	}

	@Test
	public void testConfigure() {
		Date date = new Date();
		String formattedDate = "a day";
		when(mockDateTimeUtils.getLongFriendlyDate(any(Date.class))).thenReturn(formattedDate);
		presenter.configure(date, "createdBy", date, "modifiedBy");
		verify(mockCreatedByBadge).configure("createdBy");
		verify(mockModifiedByBadge).configure("modifiedBy");

		verify(mockView).setVisible(true);
		verify(mockView).setCreatedOnText(" on " + formattedDate);
		verify(mockView).setModifiedOnText(" on " + formattedDate);
	}

}
