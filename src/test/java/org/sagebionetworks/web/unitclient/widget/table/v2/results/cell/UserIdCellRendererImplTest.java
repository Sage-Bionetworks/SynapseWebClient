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
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class UserIdCellRendererImplTest {
	
	@Mock
	UserBadge mockUserBadge;
	@Mock
	DivView mockView;
	@Mock
	UserGroupHeaderAsyncHandler mockUserGroupHeaderAsyncHandler;
	@Mock
	PortalGinInjector mockGinInjector;
	UserIdCellRendererImpl renderer;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		renderer = new UserIdCellRendererImpl(mockView, mockUserGroupHeaderAsyncHandler, mockGinInjector);
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
