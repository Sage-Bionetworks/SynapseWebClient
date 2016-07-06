package org.sagebionetworks.web.unitclient.widget.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entity.query.EntityFieldCondition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.Sort;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.TableListWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.CreateTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard.TableType;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TableListWidgetTest {

	private static final String ENTITY_ID = "syn123";
	private PreflightController mockPreflightController;
	private TableListWidgetView mockView;
	private SynapseClientAsync mockSynapseClient;
	private PaginationWidget mockpaginationWidget;
	private CreateTableModalWidget mockcreateTableModalWidget;
	private UploadTableModalWidget mockUploadTableModalWidget;
	private TableListWidget widget;
	private EntityBundle parentBundle;
	private UserEntityPermissions permissions;
	@Mock
	CreateTableViewWizard mockCreateTableViewWizard;
	@Mock
	CookieProvider mockCookies;
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		permissions = new UserEntityPermissions();
		permissions.setCanEdit(true);
		Project project = new Project();
		project.setId(ENTITY_ID);
		parentBundle = new EntityBundle();
		parentBundle.setEntity(project);
		parentBundle.setPermissions(permissions);
		mockView = Mockito.mock(TableListWidgetView.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockpaginationWidget = Mockito.mock(PaginationWidget.class);
		mockcreateTableModalWidget = Mockito.mock(CreateTableModalWidget.class);
		mockUploadTableModalWidget = Mockito.mock(UploadTableModalWidget.class);
		mockPreflightController = Mockito.mock(PreflightController.class);
		widget = new TableListWidget(mockPreflightController, mockView, mockSynapseClient, mockcreateTableModalWidget, mockpaginationWidget, mockUploadTableModalWidget, mockCookies, mockCreateTableViewWizard);
	}
	
	@Test
	public void testCreateQuery(){
		String parentId = ENTITY_ID;
		EntityQuery query = widget.createQuery(parentId);
		assertNotNull(query);
		assertNotNull(query.getConditions());
		assertEquals(2, (query.getConditions().size()));
		EntityFieldCondition expectedCondition = EntityQueryUtils.buildCondition(EntityFieldName.parentId, Operator.EQUALS, parentId);
		assertEquals(expectedCondition, query.getConditions().get(0));
		EntityFieldCondition expectedTypeCondition = EntityQueryUtils.buildCondition(
				EntityFieldName.nodeType, Operator.IN, EntityType.table.name(), EntityType.entityview.name());
		assertEquals(expectedTypeCondition, query.getConditions().get(1));
		assertNull(query.getFilterByType());
		assertEquals(TableListWidget.OFFSET_ZERO, query.getOffset());
		assertEquals(TableListWidget.PAGE_SIZE, query.getLimit());
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.name.name());
		sort.setDirection(SortDirection.ASC);
		assertEquals(sort, query.getSort());
	}
	
	@Test
	public void testConfigureUnderPageSize(){
		EntityQueryResults results = new EntityQueryResults();
		results.setTotalEntityCount(TableListWidget.PAGE_SIZE-1);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		widget.configure(parentBundle);
		verify(mockView).showPaginationVisible(false);
	}
	
	@Test
	public void testConfigureOverPageSize(){
		EntityQueryResults results = new EntityQueryResults();
		results.setTotalEntityCount(TableListWidget.PAGE_SIZE+1);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		widget.configure(parentBundle);
		verify(mockView).showPaginationVisible(true);
	}
	
	@Test
	public void testConfigureCanEdit(){
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		EntityQueryResults results = new EntityQueryResults();
		results.setTotalEntityCount(TableListWidget.PAGE_SIZE+1);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		widget.configure(parentBundle);
		verify(mockView).setAddTableVisible(true);
		verify(mockView).setUploadTableVisible(true);
		verify(mockView).setAddFileViewVisible(true);
	}
	
	@Test
	public void testConfigureCannotEdit(){
		parentBundle.getPermissions().setCanEdit(false);
		EntityQueryResults results = new EntityQueryResults();
		results.setTotalEntityCount(TableListWidget.PAGE_SIZE+1);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		widget.configure(parentBundle);
		verify(mockView).setAddTableVisible(false);
		verify(mockView).setUploadTableVisible(false);
		verify(mockView).setAddFileViewVisible(false);
	}
	
	@Test
	public void testConfigureFailure(){
		parentBundle.getPermissions().setCanEdit(false);
		String error = "an error";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		widget.configure(parentBundle);
		verify(mockView).showErrorMessage(error);
	}
	
	@Test
	public void testUploadTableCSVPreflightFailed(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkCreateEntityAndUpload(any(EntityBundle.class), anyString(), any(Callback.class));
		widget.configure(parentBundle);
		widget.onUploadTable();
		// Failure should not proceed to upload
		verify(mockUploadTableModalWidget, never()).showModal(any(WizardCallback.class));
	}
	
	@Test
	public void testUploadTableCSVPreflightPassed(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkCreateEntityAndUpload(any(EntityBundle.class), anyString(), any(Callback.class));
		widget.configure(parentBundle);
		widget.onUploadTable();
		// proceed to upload
		verify(mockUploadTableModalWidget).showModal(any(WizardCallback.class));
	}
	
	@Test
	public void testAddTableCSVPreflightFailed(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		widget.configure(parentBundle);
		widget.onAddTable();
		// Failure should not proceed to create
		verify(mockcreateTableModalWidget, never()).showCreateModal();
	}
	
	@Test
	public void testCreateTableCSVPreflightPassed(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		widget.configure(parentBundle);
		widget.onAddTable();
		// proceed to create
		verify(mockcreateTableModalWidget).showCreateModal();
	}
	
	@Test
	public void testCreateTableWizardPreflightPassed(){
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		widget.configure(parentBundle);
		widget.onAddTable();
		// proceed to create
		verify(mockCreateTableViewWizard).configure(ENTITY_ID, TableType.table);
		verify(mockCreateTableViewWizard).showModal(any(WizardCallback.class));
	}

	
	@Test
	public void testAddFileViewPreflightFailed(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		widget.configure(parentBundle);
		widget.onAddFileView();
		// Failure should not proceed to create
		verify(mockCreateTableViewWizard, never()).showModal(any(WizardCallback.class));
	}
	
	@Test
	public void testAddFileViewPreflightPassed(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkCreateEntity(any(EntityBundle.class), anyString(), any(Callback.class));
		widget.configure(parentBundle);
		widget.onAddFileView();
		// proceed to create
		verify(mockCreateTableViewWizard).configure(ENTITY_ID, TableType.view);
		verify(mockCreateTableViewWizard).showModal(any(WizardCallback.class));
	}
}
