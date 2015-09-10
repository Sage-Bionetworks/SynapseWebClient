package org.sagebionetworks.web.unitclient.widget.biodalliance13.editor;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceSource;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceSourceEditorView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

public class BiodallianceSourceEditorTest {
	BiodallianceSourceEditorView mockView; 
	SynapseClientAsync mockSynapseClient;
	EntityFinder mockEntityFinder, mockIndexEntityFinder;
	BiodallianceSource mockSource;
	
	@Before
	public void setup() throws Exception {
		mockView = mock(BiodallianceSourceEditorView.class); 
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockEntityFinder = mock(EntityFinder.class);
		mockIndexEntityFinder = mock(EntityFinder.class);
		mockSource = mock(BiodallianceSource.class);
	}
	
	@Test
	public void testCheckParams() {
		fail("Not yet implemented");
	}

	@Test
	public void testToJsonObject() {
		fail("Not yet implemented");
	}

	@Test
	public void testEntitySelected() {
		fail("Not yet implemented");
	}

	@Test
	public void testIndexEntitySelected() {
		fail("Not yet implemented");
	}

	@Test
	public void testEntityPickerClicked() {
		fail("Not yet implemented");
	}

	@Test
	public void testIndexEntityPickerClicked() {
		fail("Not yet implemented");
	}

	@Test
	public void testAssertFileEntity() {
		fail("Not yet implemented");
	}

	@Test
	public void testAssertIndexFile() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteClicked() {
		fail("Not yet implemented");
	}

	@Test
	public void testMoveDownClicked() {
		fail("Not yet implemented");
	}

	@Test
	public void testMoveUpClicked() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetMoveUpEnabled() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetMoveDownEnabled() {
		fail("Not yet implemented");
	}

}
