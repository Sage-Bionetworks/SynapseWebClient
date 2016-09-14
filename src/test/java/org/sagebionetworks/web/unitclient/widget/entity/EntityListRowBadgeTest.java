package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadge;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadgeView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadButton;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class EntityListRowBadgeTest {

	EntityListRowBadge widget;
	String entityId = "syn123";
	@Mock
	EntityListRowBadgeView mockView;
	@Mock
	UserBadge mockUserBadge;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	SynapseClientAsync mockSynapseClient;
	
	@Mock
	FileDownloadButton mockFileDownloadButton;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		widget = new EntityListRowBadge(mockView, mockUserBadge, mockSynapseJSNIUtils, mockSynapseClient,
				mockFileDownloadButton, mockSynAlert, mockLazyLoadHelper);
	}
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setCreatedByWidget(any(Widget.class));
		verify(mockView).setSynAlert(any(Widget.class));
		verify(mockFileDownloadButton).setSize(ButtonSize.EXTRA_SMALL);
	}
	
	private EntityBundle setupEntity(Entity entity) {
		EntityBundle bundle = mock(EntityBundle.class);
		when(bundle.getEntity()).thenReturn(entity);
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseClient).getEntityBundleForVersion(anyString(), anyLong(), anyInt(), any(AsyncCallback.class));
		return bundle;
	}
	
	/**
	 * This tests the standard case when the badge is outside the viewport and scrolled into view.  
	 */
	@Test
	public void testCheckForInViewAndLoadData() {
		//set up entity
		String entityId = "syn12345";
		String entityName = "a project";
		String createdByUserId = "4444";
		String description = "an old entity description";
		Project testProject = new Project();
		testProject.setName(entityName);
		testProject.setCreatedBy(createdByUserId);
		testProject.setDescription(description);
		testProject.setCreatedOn(new Date());
		
		//note: can't test modified on because it format it using the gwt DateUtils (calls GWT.create())
		testProject.setId(entityId);
		setupEntity(testProject);
		
		//configure
		Reference ref = new Reference();
		ref.setTargetId(entityId);
		widget.configure(ref);
		
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockLazyLoadHelper).configure(captor.capture(), eq(mockView));
		captor.getValue().invoke();
		
		verify(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		verify(mockView).showLoading();
		verify(mockView).setIcon(IconType.LIST_ALT); //project icon
		verify(mockView).setEntityLink(entityName, "#!Synapse:"+entityId);
		verify(mockUserBadge).configure(createdByUserId);
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setDescription(description);
		verify(mockFileDownloadButton, never()).configure(any(EntityBundle.class));
		verify(mockView, never()).setFileDownloadButton(any(Widget.class));
		verify(mockView).setVersion(EntityListRowBadge.N_A);
	}
	
	@Test
	public void testGetFileEntityBundle() {
		//verify download button is configured and shown
		String entityId = "syn12345";
		String entityName = "file.txt";
		String createdByUserId = "4444";
		String description = "an old entity description";
		Long version = 4L;
		FileEntity testFile = new FileEntity();
		testFile.setName(entityName);
		testFile.setCreatedBy(createdByUserId);
		testFile.setVersionNumber(version);
		testFile.setDescription(description);
		testFile.setId(entityId);
		setupEntity(testFile);
		
		Reference ref = new Reference();
		ref.setTargetId(entityId);
		ref.setTargetVersionNumber(version);
		widget.configure(ref);
		
		widget.getEntityBundle();
		
		verify(mockSynapseClient).getEntityBundleForVersion(anyString(), anyLong(), anyInt(), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		verify(mockView).showLoading();
		verify(mockView).setIcon(IconType.FILE_O); //file icon
		verify(mockView).setEntityLink(entityName, "#!Synapse:"+entityId+"."+version);
		verify(mockUserBadge).configure(createdByUserId);
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setDescription(description);
		verify(mockFileDownloadButton).configure(any(EntityBundle.class));
		verify(mockView).setFileDownloadButton(any(Widget.class));
		verify(mockView).setVersion(version.toString());
	}
	
	@Test
	public void testCheckForInViewAndLoadDataFailure() {
		Reference ref = new Reference();
		ref.setTargetId(entityId);
		widget.configure(ref);
		
		//test failure response from getEntityBundle
		String errorMessage = "problem occurred while asking for entity bundle";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		widget.getEntityBundle();
		verify(mockView).showSynAlert();
		verify(mockSynAlert).handleException(ex);;
	}
}
