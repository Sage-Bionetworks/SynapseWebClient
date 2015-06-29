package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.controller.EntityRefProvEntryView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidget;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceURLDialogWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StorageLocationWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.controller.URLProvEntryView;

public class StorageLocationWidgetTest {

	StorageLocationWidgetView mockView;
	SynapseClientAsync mockSynapse;
	StorageLocationWidget widget;
	SynapseAlert mockSynAlert;
	
	@Before
	public void setup() {
		mockView = mock(StorageLocationWidgetView.class);
		mockSynapse = mock(SynapseClientAsync.class);
		mockSynAlert = mock(SynapseAlert.class);
		widget = new StorageLocationWidget(mockView, mockSynapse, mockSynAlert);
	}
	
	@Test
	public void testConfigure() {
		fail("Not yet implemented");
	}

	@Test
	public void testShow() {
		fail("Not yet implemented");
	}

	@Test
	public void testHide() {
		fail("Not yet implemented");
	}

	@Test
	public void testClear() {
		fail("Not yet implemented");
	}

	@Test
	public void testOnSave() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStorageLocationSettingFromView() {
		fail("Not yet implemented");
	}

	@Test
	public void testValidate() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsValidSftpUrl() {
		fail("Not yet implemented");
	}

}
