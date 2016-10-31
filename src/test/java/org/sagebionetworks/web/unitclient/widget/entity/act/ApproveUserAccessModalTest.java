package org.sagebionetworks.web.unitclient.widget.entity.act;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModalView;
import org.sagebionetworks.web.client.widget.entity.act.EmailMessagePreviewModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.table.modal.download.CreateDownloadPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadFilePage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;

public class ApproveUserAccessModalTest {

	ApproveUserAccessModal dialog;
	ApproveUserAccessModalView mockView;
	SynapseAlert mockSynAlert;
	SynapseSuggestBox mockPeopleSuggestWidget;
	UserGroupSuggestionProvider mockProvider; 
	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	JobTrackingWidgetStub progressWidgetStub;
	EmailMessagePreviewModal mockMessagePreview;
	
	String userId;
	String datasetId;
	String message;
	EntityBundle entityBundle;
	List<ACTAccessRequirement> actList;
	
	@Before
	public void before(){
		mockView = Mockito.mock(ApproveUserAccessModalView.class);
		mockSynAlert = Mockito.mock(SynapseAlert.class);
		progressWidgetStub = new JobTrackingWidgetStub();
		
		mockPeopleSuggestWidget = Mockito.mock(SynapseSuggestBox.class);
		mockProvider = Mockito.mock(UserGroupSuggestionProvider.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockGlobalApplicationState = Mockito.mock(GlobalApplicationState.class);
		progressWidgetStub = Mockito.mock(JobTrackingWidgetStub.class);
		mockMessagePreview = Mockito.mock(EmailMessagePreviewModal.class);
		dialog = new ApproveUserAccessModal(mockView, mockSynAlert, mockPeopleSuggestWidget, mockProvider, mockSynapseClient, mockGlobalApplicationState, progressWidgetStub, mockMessagePreview);
		
		
		userId = "1234567";
		datasetId = "syn3219045"; //ROSMAP
		
//		Query query = new Query();
//		StringBuilder builder = new StringBuilder();
//		builder.append("SELECT \"Email Body\" FROM ");
//		builder.append(mockGlobalApplicationState.getSynapseProperty("org.sagebionetworks.portal.act.synapse_storage_id"));
//		builder.append(" WHERE \"Dataset Id\"= \"");
//		builder.append(datasetId + "\"");
//		query.setSql(builder.toString());
//		query.setIsConsistent(true);
//		when(dialog.getDefaultQuery()).thenReturn(query);
		
		entityBundle = new EntityBundle();
		Entity entity = new FileEntity();
		entity.setId(datasetId);
		entity.setName("ROSMAP Study");
		entityBundle.setEntity(entity);
		ACTAccessRequirement act = Mockito.mock(ACTAccessRequirement.class);
		actList = new ArrayList<ACTAccessRequirement>();
		actList.add(act);
		
	}
	
	@Test
	public void testConfigureNoAccessReqs() {
		List<ACTAccessRequirement> accessReqs = new ArrayList<ACTAccessRequirement>();
		dialog.configure(accessReqs, entityBundle);
		verify(mockView, times(0)).setAccessRequirement(anyString(), anyString());
	}
	
	@Test
	public void testConfigureOneAccessReq() {
		ACTAccessRequirement ar = actList.get(0);
		String num = Long.toString(ar.getId());
		String text = GovernanceServiceHelper.getAccessRequirementText(ar);
		dialog.configure(actList, entityBundle);
		verify(mockView).setAccessRequirement(eq(num), eq(text));
		verify(mockView, times(1)).setAccessRequirement(anyString(), anyString());
		verify(mockView).setDatasetTitle(entityBundle.getEntity().getName());
	}
	
	@Test
	public void testLoadEmailMessage() {
		dialog.configure(actList, entityBundle);
		verify(mockView).finishLoadingEmail();
		//verify(mockSynAlert).handleException(Throwable.class);
		/*
		Query query = getDefaultQuery();
		QueryBundleRequest qbr = new QueryBundleRequest();
		qbr.setPartMask(ALL_PARTS_MASK);
		qbr.setQuery(query);
		qbr.setEntityId(QueryBundleUtils.getTableId(query));
		this.progressWidget.startAndTrackJob("Running query...", false, AsynchType.TableQuery, qbr, new AsynchronousProgressHandler() {
			
			@Override
			public void onFailure(Throwable failure) {
				synAlert.handleException(failure);
			}
			
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				QueryResultBundle result = (QueryResultBundle) response;
				message = result.getQueryResult().getQueryResults().getRows().get(0).getValues().get(0);
				messagePreview.configure(message);
				view.finishLoadingEmail();
			}
			
			@Override
			public void onCancel() {
				synAlert.showError("Query cancelled");
			}
		});
		*/
	}
	
	
//	
//	@Test
//	public void testSetModalPresenter(){
//		// This is the main entry to the page
//		page.setModalPresenter(mockModalPresenter);
//		verify(mockModalPresenter).setPrimaryButtonText(CreateDownloadPageImpl.NEXT);
//		verify(mockView).setFileType(FileType.CSV);
//		verify(mockView).setIncludeHeaders(true);
//		verify(mockView).setIncludeRowMetadata(true);
//		verify(mockView).setTrackerVisible(false);
//	}
//	
//	@Test
//	public void testgetDownloadFromTableRequest(){
//		page.setModalPresenter(mockModalPresenter);
//		DownloadFromTableRequest expected = new DownloadFromTableRequest();
//		CsvTableDescriptor descriptor = new CsvTableDescriptor();
//		descriptor.setSeparator("\t");
//		expected.setCsvTableDescriptor(descriptor);
//		expected.setIncludeRowIdAndRowVersion(false);
//		expected.setSql(sql);
//		expected.setWriteHeader(true);
//		when(mockView.getFileType()).thenReturn(FileType.TSV);
//		when(mockView.getIncludeHeaders()).thenReturn(true);
//		when(mockView.getIncludeRowMetadata()).thenReturn(false);
//		
//		DownloadFromTableRequest request = page.getDownloadFromTableRequest();
//		assertEquals(expected, request);
//	}
//	
//	@Test
//	public void testOnPrimarySuccess(){
//		page.setModalPresenter(mockModalPresenter);
//		when(mockView.getFileType()).thenReturn(FileType.TSV);
//		when(mockView.getIncludeHeaders()).thenReturn(true);
//		when(mockView.getIncludeRowMetadata()).thenReturn(false);
//	
//		String fileHandle = "45678";
//		DownloadFromTableResult results = new DownloadFromTableResult();
//		results.setResultsFileHandleId(fileHandle);
//	
//		jobTrackingWidgetStub.setResponse(results);
//		page.onPrimary();
//		verify(mockModalPresenter).setLoading(true);
//		verify(mockView).setTrackerVisible(true);
//		verify(mockNextPage).configure(fileHandle);
//		verify(mockModalPresenter).setNextActivePage(mockNextPage);
//	}
//	
//	@Test
//	public void testOnPrimaryCancel(){
//		page.setModalPresenter(mockModalPresenter);
//		when(mockView.getFileType()).thenReturn(FileType.TSV);
//		when(mockView.getIncludeHeaders()).thenReturn(true);
//		when(mockView.getIncludeRowMetadata()).thenReturn(false);
//	
//		String fileHandle = "45678";
//		DownloadFromTableResult results = new DownloadFromTableResult();
//		results.setResultsFileHandleId(fileHandle);
//	
//		jobTrackingWidgetStub.setOnCancel(true);
//		page.onPrimary();
//		verify(mockModalPresenter).setLoading(true);
//		verify(mockView).setTrackerVisible(true);
//		verify(mockNextPage, never()).configure(fileHandle);
//		verify(mockModalPresenter).onCancel();
//	}
//	
//	@Test
//	public void testOnPrimaryFailure(){
//		page.setModalPresenter(mockModalPresenter);
//		when(mockView.getFileType()).thenReturn(FileType.TSV);
//		when(mockView.getIncludeHeaders()).thenReturn(true);
//		when(mockView.getIncludeRowMetadata()).thenReturn(false);
//	
//		String fileHandle = "45678";
//		DownloadFromTableResult results = new DownloadFromTableResult();
//		results.setResultsFileHandleId(fileHandle);
//		String error = "failure";
//		jobTrackingWidgetStub.setError(new Throwable(error));
//		page.onPrimary();
//		verify(mockModalPresenter).setLoading(true);
//		verify(mockView).setTrackerVisible(true);
//		verify(mockNextPage, never()).configure(fileHandle);
//		verify(mockModalPresenter).setErrorMessage(error);
//	}
}
