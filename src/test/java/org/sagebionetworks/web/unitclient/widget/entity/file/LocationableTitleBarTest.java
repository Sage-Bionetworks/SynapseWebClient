package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Code;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBarView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class LocationableTitleBarTest {
		
	LocationableTitleBar locationableTitleBar;
	LocationableTitleBarView mockView;
	AuthenticationController mockAuthController;
	EntityTypeProvider mockEntityTypeProvider;
	SynapseClientAsync mockSynapseClient;
	EntityEditor mockEntityEditor;
	@Before
	public void setup(){	
		mockView = mock(LocationableTitleBarView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockEntityEditor = mock(EntityEditor.class);
		locationableTitleBar = new LocationableTitleBar(mockView, mockAuthController, mockEntityTypeProvider, mockSynapseClient, mockEntityEditor);
		
		verify(mockView).setPresenter(locationableTitleBar);
	}
	
	@Test
	public void testAsWidget(){
		locationableTitleBar.asWidget();
	}
	
	@Test
	public void testUpdateNodeStorageUsage() {
		//updating node storage usage is wired to synapse client getStorageUsage (and result is completely based on the return of that call)
		final Long testSize = 1234l;
		locationableTitleBar.setEntityBundle(new EntityBundle(new Project(), null, null, null, null, null, null, null));
		AsyncMockStubber.callSuccessWith(testSize).when(mockSynapseClient).getStorageUsage(anyString(), any(AsyncCallback.class));
		locationableTitleBar.updateNodeStorageUsage(new AsyncCallback<Long>() {
			@Override
			public void onSuccess(Long result) {
				Assert.assertEquals(testSize, result);
			}
			@Override
			public void onFailure(Throwable caught) {
				Assert.fail(caught.getMessage());
			}
		});
		
		verify(mockSynapseClient).getStorageUsage(anyString(), (AsyncCallback<Long>) any());
	}
	@Test (expected=IllegalArgumentException.class)
	public void testIsDataInLocationableFail() {
		EntityBundle bundle = new EntityBundle(new Project(), null, null, null,null,null, null, null);
		LocationableTitleBar.isDataPossiblyWithinLocationable(bundle, true);
	}
	
	@Test
	public void testIsDataInLocationableAnonymous() {
		EntityBundle bundle = new EntityBundle(new Code(), null, null, null,null,null, null, null);
		boolean isLoggedIn = false;
		Assert.assertTrue(LocationableTitleBar.isDataPossiblyWithinLocationable(bundle, isLoggedIn));
	}

	@Test
	public void testIsDataInLocationableUnmetAcccess() {
		List<AccessRequirement> unmetAccessRequirements = new ArrayList<AccessRequirement>();
		unmetAccessRequirements.add(new ACTAccessRequirement());
		EntityBundle bundle = new EntityBundle(new Code(), null, null, null,null,null, unmetAccessRequirements, null);
		Assert.assertTrue(LocationableTitleBar.isDataPossiblyWithinLocationable(bundle, true));
	}
	
	@Test
	public void testIsDataInLocationableHasLocations() {
		Code code = new Code();
		List<LocationData> locations = new ArrayList<LocationData>();
		locations.add(new LocationData());
		code.setLocations(locations);
		EntityBundle bundle = new EntityBundle(code, null, null, null,null,null, null, null);
		Assert.assertTrue(LocationableTitleBar.isDataPossiblyWithinLocationable(bundle, true));
	}
	@Test
	public void testIsDataInLocationableNoLocations() {
		EntityBundle bundle = new EntityBundle(new Code(), null, null, null,null,null, null, null);
		Assert.assertFalse(LocationableTitleBar.isDataPossiblyWithinLocationable(bundle, true));
	}
}
