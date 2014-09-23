package org.sagebionetworks.web.unitclient.widget.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.entity.query.EntityFieldCondition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.EntityType;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.Sort;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.TableListWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.CreateTableModalWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TableListWidgetTest {

	private TableListWidgetView mockView;
	private SynapseClientAsync mockSynapseClient;
	private PaginationWidget mockpaginationWidget;
	private CreateTableModalWidget mockcreateTableModalWidget;
	private TableListWidget widget;
	
	@Before
	public void before(){
		mockView = Mockito.mock(TableListWidgetView.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockpaginationWidget = Mockito.mock(PaginationWidget.class);
		mockcreateTableModalWidget = Mockito.mock(CreateTableModalWidget.class);
		widget = new TableListWidget(mockView, mockSynapseClient, mockcreateTableModalWidget, mockpaginationWidget);
	}
	
	@Test
	public void testCreateQuery(){
		String parentId = "syn123";
		EntityQuery query = widget.createQuery(parentId);
		assertNotNull(query);
		assertNotNull(query.getConditions());
		assertEquals(1, (query.getConditions().size()));
		EntityFieldCondition expectedCondition = EntityQueryUtils.buildCondition(EntityFieldName.parentId, Operator.EQUALS, parentId);
		assertEquals(expectedCondition, query.getConditions().get(0));
		assertEquals(EntityType.table, query.getFilterByType());
		assertEquals(TableListWidget.OFFSET_ZERO, query.getOffset());
		assertEquals(TableListWidget.PAGE_SIZE, query.getLimit());
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.createdOn.name());
		sort.setDirection(SortDirection.DESC);
		assertEquals(sort, query.getSort());
	}
	
	@Test
	public void testConfigureUnderPageSize(){
		String parentId = "syn123";
		boolean canEdit = true;
		EntityQueryResults results = new EntityQueryResults();
		results.setTotalEntityCount(TableListWidget.PAGE_SIZE-1);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		widget.configure(parentId, canEdit);
		verify(mockView).showPaginationVisible(false);
	}
	
	@Test
	public void testConfigureOverPageSize(){
		String parentId = "syn123";
		boolean canEdit = true;
		EntityQueryResults results = new EntityQueryResults();
		results.setTotalEntityCount(TableListWidget.PAGE_SIZE+1);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		widget.configure(parentId, canEdit);
		verify(mockView).showPaginationVisible(true);
	}
	
	@Test
	public void testConfigureCanEdit(){
		String parentId = "syn123";
		boolean canEdit = true;
		EntityQueryResults results = new EntityQueryResults();
		results.setTotalEntityCount(TableListWidget.PAGE_SIZE+1);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		widget.configure(parentId, canEdit);
		verify(mockView).setAddTableVisible(true);
		verify(mockView).setUploadTableVisible(true);
	}
	
	@Test
	public void testConfigureCannotEdit(){
		String parentId = "syn123";
		boolean canEdit = false;
		EntityQueryResults results = new EntityQueryResults();
		results.setTotalEntityCount(TableListWidget.PAGE_SIZE+1);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		widget.configure(parentId, canEdit);
		verify(mockView).setAddTableVisible(false);
		verify(mockView).setUploadTableVisible(false);
	}
	
	@Test
	public void testConfigureFailure(){
		String parentId = "syn123";
		boolean canEdit = false;
		String error = "an error";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		widget.configure(parentId, canEdit);
		verify(mockView).showErrorMessage(error);
	}
}
