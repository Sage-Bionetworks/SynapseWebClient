package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;
import static org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep1.EMPTY_SCOPE_MESSAGE;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.ViewEntityType;
import org.sagebionetworks.repo.model.table.ViewTypeMask;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.evaluation.SubmissionViewScopeEditor;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep1;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep1View;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;



public class CreateTableViewWizardStep1Test {

	@Mock
	CreateTableViewWizardStep1View mockView;
	@Mock
	ModalPresenter mockWizardPresenter;
	@Mock
	EntityContainerListWidget mockEntityContainerListWidget;
	@Mock
	SubmissionViewScopeEditor mockSubmissionViewScope;
	@Mock
	CreateTableViewWizardStep2 mockStep2;

	@Mock
	SynapseJavascriptClient mockJsClient;
	String parentId;
	CreateTableViewWizardStep1 widget;
	List<String> entityScopeIds;
	List<String> evaluationScopeIds;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		entityScopeIds = Collections.singletonList("3");
		when(mockEntityContainerListWidget.getEntityIds()).thenReturn(entityScopeIds);
		evaluationScopeIds = Collections.singletonList("8278743");
		when(mockSubmissionViewScope.getEvaluationIds()).thenReturn(evaluationScopeIds);
		widget = new CreateTableViewWizardStep1(mockView, mockJsClient, mockEntityContainerListWidget, mockSubmissionViewScope, mockStep2);
		widget.setModalPresenter(mockWizardPresenter);
		parentId = "syn123";
	}

	@Test
	public void testNullName() {
		widget.configure(parentId, TableType.files);
		when(mockView.getName()).thenReturn(null);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(CreateTableViewWizardStep1.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockJsClient, never()).createEntity(any(Entity.class));
	}

	@Test
	public void testConfigureSubmissionView() {
		// verify configure with submission_view type
		widget.configure(parentId, TableType.submission_view);
		
		verify(mockView).setSubmissionViewScopeWidgetVisible(true);
		verify(mockView, never()).setEntityViewScopeWidgetVisible(true);
		verify(mockView).setViewTypeOptionsVisible(false);
		verify(mockSubmissionViewScope).configure(anyList());
		
		//and create the submission view entity
		String tableName = "a name";
		SubmissionView table = new SubmissionView();
		table.setName(tableName);
		table.setId("syn57");
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		when(mockJsClient.createEntity(captor.capture())).thenReturn(getDoneFuture(table));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		SubmissionView capturedView = (SubmissionView) captor.getValue();
		assertEquals(evaluationScopeIds, capturedView.getScopeIds());
		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		verify(mockStep2).configure(table, TableType.submission_view);
		verify(mockWizardPresenter).setNextActivePage(mockStep2);

	}
	
	@Test
	public void testCreateFileView() {
		widget.configure(parentId, TableType.files);
		verify(mockView).setName("");
		verify(mockView).setEntityViewScopeWidgetVisible(true);

		verify(mockView).setViewTypeOptionsVisible(true);
		String tableName = "a name";
		EntityView table = new EntityView();
		table.setName(tableName);
		table.setId("syn57");
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		when(mockJsClient.createEntity(captor.capture())).thenReturn(getDoneFuture(table));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		EntityView capturedFileView = (EntityView) captor.getValue();
		assertEquals(entityScopeIds, capturedFileView.getScopeIds());
		assertNull(capturedFileView.getType());
		assertEquals((Long) ViewTypeMask.File.getMask(), capturedFileView.getViewTypeMask());
		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		verify(mockStep2).configure(table, TableType.files);
		verify(mockWizardPresenter).setNextActivePage(mockStep2);
	}

	@Test
	public void testCreateFileFolderTableView() {
		// initially configured with Files only
		widget.configure(parentId, TableType.files);
		verify(mockView).setName("");
		verify(mockView).setEntityViewScopeWidgetVisible(true);
		verify(mockView).setViewTypeOptionsVisible(true);

		// simulate updating view type mask (clicking on check boxes for folder and table)
		when(mockView.isFileSelected()).thenReturn(true);
		when(mockView.isFolderSelected()).thenReturn(true);
		when(mockView.isTableSelected()).thenReturn(true);

		widget.updateViewTypeMask();

		String tableName = "a name";
		EntityView table = new EntityView();
		table.setName(tableName);
		table.setId("syn57");
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		when(mockJsClient.createEntity(captor.capture())).thenReturn(getDoneFuture(table));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		EntityView capturedFileView = (EntityView) captor.getValue();
		assertEquals(entityScopeIds, capturedFileView.getScopeIds());
		assertNull(capturedFileView.getType());
		assertEquals(new Long(TableType.files_folders_tables.getViewTypeMask()), capturedFileView.getViewTypeMask());
		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		verify(mockStep2).configure(table, TableType.files_folders_tables);
		verify(mockWizardPresenter).setNextActivePage(mockStep2);
	}

	@Test
	public void testCreateFileViewEmptyScope() {
		widget.configure(parentId, TableType.files);
		String tableName = "a name";
		EntityView table = new EntityView();
		table.setName(tableName);
		table.setId("syn57");
		when(mockJsClient.createEntity(any(Entity.class))).thenReturn(getDoneFuture(table));
		when(mockView.getName()).thenReturn(tableName);
		when(mockEntityContainerListWidget.getEntityIds()).thenReturn(Collections.EMPTY_LIST);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(EMPTY_SCOPE_MESSAGE);
	}

	@Test
	public void testCreateProjectView() {
		widget.configure(parentId, TableType.projects);
		verify(mockView).setName("");
		verify(mockView).setEntityViewScopeWidgetVisible(true);
		verify(mockView).setViewTypeOptionsVisible(false);
		String tableName = "a name";
		EntityView table = new EntityView();
		table.setName(tableName);
		table.setId("syn57");
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		when(mockJsClient.createEntity(captor.capture())).thenReturn(getDoneFuture(table));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		EntityView capturedFileView = (EntityView) captor.getValue();
		assertEquals(entityScopeIds, capturedFileView.getScopeIds());
		assertNull(capturedFileView.getType());
		assertEquals((Long) ViewTypeMask.Project.getMask(), capturedFileView.getViewTypeMask());
		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		verify(mockStep2).configure(table, TableType.projects);
		verify(mockWizardPresenter).setNextActivePage(mockStep2);
	}

	@Test
	public void testCreateTable() {
		widget.configure(parentId, TableType.table);
		verify(mockView).setEntityViewScopeWidgetVisible(false);
		verify(mockView).setViewTypeOptionsVisible(false);
		String tableName = "a name";
		TableEntity table = new TableEntity();
		table.setName(tableName);
		table.setId("syn57");
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		when(mockJsClient.createEntity(captor.capture())).thenReturn(getDoneFuture(table));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		TableEntity capturedTable = (TableEntity) captor.getValue();
		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		verify(mockStep2).configure(table, TableType.table);
		verify(mockWizardPresenter).setNextActivePage(mockStep2);;
	}

	@Test
	public void testCreateDataset() {
		widget.configure(parentId, TableType.dataset);
		verify(mockView).setEntityViewScopeWidgetVisible(false);
		verify(mockView).setViewTypeOptionsVisible(false);
		String tableName = "a name";
		Dataset dataset = new Dataset();
		dataset.setName(tableName);
		dataset.setId("syn57");

		String defaultColumnId = "999";
		ColumnModel col = new ColumnModel();
		col.setId(defaultColumnId);
		List<ColumnModel> defaultColumns = Collections.singletonList(col);

		ArgumentCaptor<Dataset> captor = ArgumentCaptor.forClass(Dataset.class);
		when(mockJsClient.createEntity(captor.capture())).thenReturn(getDoneFuture(dataset));
		when(mockJsClient.getDefaultColumnsForView(ViewEntityType.dataset)).thenReturn(getDoneFuture(defaultColumns));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();

		// Verify that default columns are added
		assertEquals(captor.getValue().getColumnIds().size(), defaultColumns.size());
		assertEquals(captor.getValue().getColumnIds().get(0), defaultColumnId);

		// Verify that items is an empty list (not null)
		assertEquals(captor.getValue().getItems(), Collections.EMPTY_LIST);

		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		// We shouldn't go to step 2 for dataseets
		verify(mockStep2, never()).configure(any(), any());
		verify(mockWizardPresenter, never()).setNextActivePage(mockStep2);
		verify(mockWizardPresenter).onFinished();
	}


	@Test
	public void testCreateFailed() {
		widget.configure(parentId, TableType.files);
		String tableName = "a name";
		String error = "name already exists";
		when(mockJsClient.createEntity(any(Entity.class))).thenReturn(getFailedFuture(new Throwable(error)));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(error);

		// TODO: should not go to the next step
		verify(mockWizardPresenter, never()).onFinished();
	}

}
