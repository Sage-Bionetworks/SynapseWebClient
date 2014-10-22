package org.sagebionetworks.web.unitclient.widget.sharing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.sharing.AddPeopleToAclPanel;
import org.sagebionetworks.web.client.widget.sharing.AddPeopleToAclPanelView;

public class AddPeopleToAclPanelTest {

	private AddPeopleToAclPanel panel;
	private AddPeopleToAclPanelView mockView;
	
	@Before
	public void before() {
		mockView = mock(AddPeopleToAclPanelView.class);
		panel = new AddPeopleToAclPanel(mockView);
		
	}
	
}
