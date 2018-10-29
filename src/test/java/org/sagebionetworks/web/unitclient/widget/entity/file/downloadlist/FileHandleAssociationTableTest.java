package org.sagebionetworks.web.unitclient.widget.entity.file.downloadlist;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationRow;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationRowView;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationTable;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationTableView;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class FileHandleAssociationTableTest {
	FileHandleAssociationTable widget;
	@Mock
	FileHandleAssociationTableView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	FileHandleAssociationRow mockRow1;
	@Mock
	FileHandleAssociationRow mockRow2;
	@Mock
	FileHandleAssociation mockFha1;
	@Mock
	FileHandleAssociation mockFha2;
	@Mock
	CallbackP<Double> mockAddToPackageSizeCallback;
	@Mock
	CallbackP<FileHandleAssociation> mockOnRemoveCallback;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	List<FileHandleAssociation> fhaList;
	public static final String ENTITY_ID = "syn92832";
	
	@Before
	public void setUp() throws Exception {
		widget = new FileHandleAssociationTable(mockView, mockGinInjector);
		when(mockGinInjector.getFileHandleAssociationRow()).thenReturn(mockRow1, mockRow2);
		fhaList = new ArrayList<FileHandleAssociation>();
		fhaList.add(mockFha1);
		fhaList.add(mockFha2);
		
		// (also test case insensitive sort)
		when(mockRow1.getFileName()).thenReturn("B.txt");
		when(mockRow2.getFileName()).thenReturn("a.csv");
		when(mockRow1.getHasAccess()).thenReturn(true);
		when(mockRow2.getHasAccess()).thenReturn(false);
		when(mockRow1.getCreatedBy()).thenReturn("Bob");
		when(mockRow2.getCreatedBy()).thenReturn("anna");
		Date now = new Date();
		when(mockRow1.getCreatedOn()).thenReturn(now);
		Date past = new Date(now.getTime() - 1000);
		when(mockRow2.getCreatedOn()).thenReturn(past);
		when(mockRow1.getFileSize()).thenReturn(1000L);
		when(mockRow2.getFileSize()).thenReturn(2000L);
		
	}

	@Test
	public void testConstructor() {
		verify(mockView).setSortingListener(widget);
	}
	
	@Test
	public void testConfigure() {
		widget.configure(fhaList, mockAddToPackageSizeCallback, mockOnRemoveCallback);
		
		verify(mockView).clear();
		verify(mockRow1).configure(eq(mockFha1), callbackCaptor.capture(), eq(mockAddToPackageSizeCallback), eq(mockOnRemoveCallback));
		verify(mockRow2).configure(eq(mockFha2), callbackCaptor.capture(), eq(mockAddToPackageSizeCallback), eq(mockOnRemoveCallback));
		
		//also verify access restriction detected callback
		verify(mockView, never()).showAccessRestrictionsDetectedUI();
		callbackCaptor.getValue().invoke();
		verify(mockView).showAccessRestrictionsDetectedUI();
	}
	
	private void testSort(String columnName) {
		widget.configure(fhaList, mockAddToPackageSizeCallback, mockOnRemoveCallback);
		reset(mockView);
		
		widget.onToggleSort(columnName);
		
		verify(mockView).setSort(columnName, SortDirection.DESC);
		verify(mockView).clearRows();
		verify(mockView).addRow(mockRow2);
		verify(mockView).addRow(mockRow1);
		reset(mockView);
		
		//verify reverse sort (if clicked twice)
		widget.onToggleSort(columnName);
		
		verify(mockView).setSort(columnName, SortDirection.ASC);
		verify(mockView).clearRows();
		verify(mockView).addRow(mockRow1);
		verify(mockView).addRow(mockRow2);
	}
	
	@Test
	public void testSortFileName() {
		testSort(FileHandleAssociationTable.FILE_NAME_COLUMN_NAME);
	}
	@Test
	public void testSortAccess() {
		testSort(FileHandleAssociationTable.ACCESS_COLUMN_NAME);
	}
	@Test
	public void testSortCreatedBy() {
		testSort(FileHandleAssociationTable.CREATED_BY_COLUMN_NAME);
	}
	@Test
	public void testSortCreatedOn() {
		testSort(FileHandleAssociationTable.CREATED_ON_COLUMN_NAME);
	}
	@Test
	public void testSortContentSize() {
		testSort(FileHandleAssociationTable.SIZE_COLUMN_NAME);
	}
}
