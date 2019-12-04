package org.sagebionetworks.web.unitclient.widget.biodalliance13.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceSource;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceSource.SourceType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceSourceEditor;
import org.sagebionetworks.web.client.widget.biodalliance13.editor.BiodallianceSourceEditorView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class BiodallianceSourceEditorTest {
	BiodallianceSourceEditorView mockView;
	SynapseClientAsync mockSynapseClient;
	EntityFinder mockEntityFinder, mockIndexEntityFinder;
	BiodallianceSource mockSource;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	EntityBundle mockEntityBundle;
	FileEntity selectedFile;

	BiodallianceSourceEditor editor;

	// source values
	String sourceName = "bigwig source";
	String entityId = "syn123";
	Long version = 4L;
	String indexEntityId = "syn456";
	Long indexVersion = 128L;
	String color = "blue";
	int heightPx = 15;

	// view value
	String viewHeight = "25";

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockView = mock(BiodallianceSourceEditorView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockEntityFinder = mock(EntityFinder.class);
		mockIndexEntityFinder = mock(EntityFinder.class);
		mockSource = mock(BiodallianceSource.class);
		mockEntityBundle = mock(EntityBundle.class);
		String dataFileHandleId = "9";
		selectedFile = new FileEntity();
		selectedFile.setDataFileHandleId(dataFileHandleId);
		when(mockEntityBundle.getEntity()).thenReturn(selectedFile);

		AsyncMockStubber.callSuccessWith(mockEntityBundle).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockEntityBundle).when(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), any(EntityBundleRequest.class), any(AsyncCallback.class));

		when(mockSource.getSourceType()).thenReturn(SourceType.VCF);
		when(mockSource.getSourceName()).thenReturn(sourceName);
		when(mockSource.getEntityId()).thenReturn(entityId);
		when(mockSource.getVersion()).thenReturn(version);
		when(mockSource.getIndexEntityId()).thenReturn(indexEntityId);
		when(mockSource.getIndexVersion()).thenReturn(indexVersion);
		when(mockSource.getStyleColor()).thenReturn(color);
		when(mockSource.getHeightPx()).thenReturn(heightPx);

		when(mockView.getHeight()).thenReturn(viewHeight);
		editor = new BiodallianceSourceEditor(mockView, mockEntityFinder, mockIndexEntityFinder, mockSource, mockSynapseJavascriptClient);
	}

	@Test
	public void testConstructorAndUpdateViewFromSource() {
		verify(mockView).setPresenter(editor);
		verify(mockEntityFinder).configure(eq(EntityFilter.ALL_BUT_LINK), eq(true), any(SelectedHandler.class));
		verify(mockIndexEntityFinder).configure(eq(EntityFilter.ALL_BUT_LINK), eq(true), any(SelectedHandler.class));

		verify(mockView).setSourceName(sourceName);
		verify(mockView).setEntityFinderText(entityId + "." + version);
		verify(mockView).setIndexEntityFinderText(indexEntityId + "." + indexVersion);
		verify(mockView).setColor(color);
		verify(mockView).setHeight(Integer.toString(heightPx));
	}

	@Test
	public void testSetSource() {
		String sourceJson = "source json";
		editor.setSourceJson(sourceJson);
		// pass through
		verify(mockSource).initializeFromJson(sourceJson);
	}

	@Test
	public void testCheckParamsHappyCase() {
		editor.checkParams();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckParamsInvalidHeight1() {
		when(mockView.getHeight()).thenReturn("foo");
		editor.checkParams();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckParamsInvalidHeight2() {
		when(mockView.getHeight()).thenReturn("-2");
		editor.checkParams();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckParamsInvalidSource() {
		when(mockSource.getEntityId()).thenReturn(null);
		editor.checkParams();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckParamsInvalidSourceVersion() {
		when(mockSource.getVersion()).thenReturn(null);
		editor.checkParams();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckParamsInvalidIndex1() {
		// because this is vcf, index file is required
		when(mockSource.getIndexEntityId()).thenReturn(null);
		editor.checkParams();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckParamsInvalidIndex2() {
		// because this is vcf, index file is required
		when(mockSource.getIndexVersion()).thenReturn(null);
		editor.checkParams();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCheckParamsInvalidIndex3() {
		// if bed, index file is required
		when(mockSource.getSourceType()).thenReturn(SourceType.BED);
		when(mockSource.getIndexEntityId()).thenReturn(null);
		editor.checkParams();
	}

	@Test
	public void testCheckParamsValidIndex() {
		// if bigwig, however, an index file will not be used
		when(mockSource.getSourceType()).thenReturn(SourceType.BIGWIG);
		when(mockSource.getIndexEntityId()).thenReturn(null);
		editor.checkParams(); // no exception
	}

	@Test
	public void testToJsonObject() {
		editor.toJsonObject();
		verify(mockView).getSourceName();
		verify(mockView).getColor();
		verify(mockView).getHeight();
		verify(mockSource).toJsonObject();
	}

	// Entity selection tests


	@Test
	public void testEntitySelectedNoVersion() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		Long currentEntityVersion = 20L;
		selectedFile.setVersionNumber(currentEntityVersion);
		selectedFile.setId(selectedEntityId);
		when(mockEntityBundle.getFileName()).thenReturn("test.BW");

		editor.entitySelected(ref);

		verify(mockSource).setEntity(null, null);
		verify(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockSource).setEntity(selectedEntityId, currentEntityVersion);
		verify(mockSource).setSourceType(SourceType.BIGWIG);
		verify(mockView).setEntityFinderText(selectedEntityId + "." + currentEntityVersion);
		verify(mockEntityFinder).hide();
	}

	@Test
	public void testEntitySelectedNoVersionFailure() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		String errorMessage = "lookup failed.";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		editor.entitySelected(ref);

		verify(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockEntityFinder).showError(errorMessage);
	}

	@Test
	public void testEntitySelectedNoVersionInvalidEntityType() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		when(mockEntityBundle.getEntity()).thenReturn(new Project());
		editor.entitySelected(ref);
		verify(mockEntityFinder).showError(anyString());
	}

	@Test
	public void testEntitySelectedNoVersionInvalidSourceType() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		when(mockEntityBundle.getFileName()).thenReturn("invalid-source-type.txt");

		editor.entitySelected(ref);

		verify(mockEntityFinder).showError(anyString());
	}

	@Test
	public void testEntitySelectedVersion() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		Long selectedVersion = 40L;
		ref.setTargetVersionNumber(selectedVersion);
		selectedFile.setVersionNumber(selectedVersion);
		selectedFile.setId(selectedEntityId);
		when(mockEntityBundle.getFileName()).thenReturn("test.bed");

		editor.entitySelected(ref);

		verify(mockSource).setEntity(null, null);
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockSource).setEntity(selectedEntityId, selectedVersion);
		verify(mockSource).setSourceType(SourceType.BED);
		verify(mockView).setEntityFinderText(selectedEntityId + "." + selectedVersion);
		verify(mockEntityFinder).hide();
	}


	@Test
	public void testEntitySelectedVersionFailure() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		ref.setTargetVersionNumber(40L);
		String errorMessage = "lookup failed.";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		editor.entitySelected(ref);

		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockEntityFinder).showError(errorMessage);
	}



	// Index entity selection tests


	@Test
	public void testIndexEntitySelectedNoVersion() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		Long currentEntityVersion = 20L;
		selectedFile.setVersionNumber(currentEntityVersion);
		selectedFile.setId(selectedEntityId);
		when(mockEntityBundle.getFileName()).thenReturn("test.TbI");

		editor.indexEntitySelected(ref);

		verify(mockSource).setIndexEntity(null, null);
		verify(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockSource).setIndexEntity(selectedEntityId, currentEntityVersion);
		verify(mockView).setIndexEntityFinderText(selectedEntityId + "." + currentEntityVersion);
		verify(mockIndexEntityFinder).hide();
	}

	@Test
	public void testIndexEntitySelectedNoVersionFailure() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		String errorMessage = "lookup failed.";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		editor.indexEntitySelected(ref);

		verify(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockIndexEntityFinder).showError(errorMessage);
	}

	@Test
	public void testIndexEntitySelectedNoVersionInvalidEntityType() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		when(mockEntityBundle.getEntity()).thenReturn(new Project());
		editor.indexEntitySelected(ref);
		verify(mockIndexEntityFinder).showError(anyString());
	}

	@Test
	public void testIndexEntitySelectedNoVersionInvalidSourceType() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		when(mockEntityBundle.getFileName()).thenReturn("invalid-index-file.bw");

		editor.indexEntitySelected(ref);

		verify(mockIndexEntityFinder).showError(anyString());
	}

	@Test
	public void testIndexEntitySelectedVersion() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		Long selectedVersion = 40L;
		ref.setTargetVersionNumber(selectedVersion);
		selectedFile.setVersionNumber(selectedVersion);
		selectedFile.setId(selectedEntityId);
		when(mockEntityBundle.getFileName()).thenReturn("test.tbi");

		editor.indexEntitySelected(ref);

		verify(mockSource).setIndexEntity(null, null);
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockSource).setIndexEntity(selectedEntityId, selectedVersion);
		verify(mockView).setIndexEntityFinderText(selectedEntityId + "." + selectedVersion);
		verify(mockIndexEntityFinder).hide();
	}


	@Test
	public void testIndexEntitySelectedVersionFailure() {
		Reference ref = new Reference();
		String selectedEntityId = "syn111";
		ref.setTargetId(selectedEntityId);
		ref.setTargetVersionNumber(40L);
		String errorMessage = "lookup failed.";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		editor.indexEntitySelected(ref);

		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockIndexEntityFinder).showError(errorMessage);
	}


	@Test
	public void testEntityPickerClicked() {
		editor.entityPickerClicked();
		verify(mockEntityFinder).show();
	}

	@Test
	public void testIndexEntityPickerClicked() {
		editor.indexEntityPickerClicked();
		verify(mockIndexEntityFinder).show();
	}

	@Test
	public void testGetSourceType() {
		assertEquals(SourceType.BED, editor.getSourceType("foo.bed"));
		assertEquals(SourceType.BED, editor.getSourceType("foo.bEd.GZ"));
		assertEquals(SourceType.BIGWIG, editor.getSourceType("foo.bw"));
		assertEquals(SourceType.BIGWIG, editor.getSourceType("foo.bigWIG"));
		assertEquals(SourceType.VCF, editor.getSourceType("foo.VCF"));
		assertEquals(SourceType.VCF, editor.getSourceType("foo.vcf.gZ"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSourceFileName() {
		editor.getSourceType("foo.tbi");
	}

	@Test
	public void testAssertFileEntity() {
		editor.assertFileEntity(new FileEntity());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertFileEntityFailure() {
		editor.assertFileEntity(new TableEntity());
	}

	@Test
	public void testAssertIndexFile() {
		editor.assertIndexFile("foo.TBi");
		editor.assertIndexFile("bar.TBI");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertIndexFileFailure() {
		editor.assertIndexFile("foo.bed");
	}

	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testSelection() {
		editor.setSelected(true);
		verify(mockView).setSelected(true);

		editor.isSelected();
		verify(mockView).isSelected();
	}
}
