package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class UserIdCellRendererImplTest {
	
	@Mock
	UserBadge mockUserBadge;
	UserIdCellRendererImpl renderer;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		renderer = new UserIdCellRendererImpl(mockUserBadge);
	}
	
	@Test
	public void testSetValue(){
		String userId = "1";
		renderer.setValue(userId);
		verify(mockUserBadge).configure(userId);
	}
	
	@Test
	public void testSetValueEmpty(){
		renderer.setValue("");
		verifyZeroInteractions(mockUserBadge);
	}
	
	@Test
	public void testSetValueNull(){
		renderer.setValue(null);
		verifyZeroInteractions(mockUserBadge);
	}

}
