package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.S3UploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBarView;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class BasicTitleBarTest {
		
	BasicTitleBar titleBar;
	BasicTitleBarView mockView;
	AuthenticationController mockAuthController;
	EntityBundle mockBundle;
	FavoriteWidget mockFavoriteWidget;
	Entity entity;
	String testEntityName = "Entity Name";
	@Before
	public void setup(){	
		mockView = mock(BasicTitleBarView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockFavoriteWidget = mock(FavoriteWidget.class);
		titleBar = new BasicTitleBar(mockView, mockAuthController, mockFavoriteWidget);
		mockBundle = mock(EntityBundle.class);
		entity = new Folder();
		entity.setId("syn123");
		entity.setName(testEntityName);
		when(mockBundle.getEntity()).thenReturn(entity);
		verify(mockView).setPresenter(titleBar);
		verify(mockView).setFavoritesWidget(any(Widget.class));
		verify(mockFavoriteWidget).asWidget();
		when(mockAuthController.isLoggedIn()).thenReturn(true);
	}
	
	@Test
	public void testAsWidget(){
		titleBar.asWidget();
	}
	
	@Test
	public void testConfigureLoggedIn() {
		titleBar.configure(mockBundle);
		verify(mockView).setFavoritesWidgetVisible(true);
		verify(mockView).setTitle(testEntityName);
		verify(mockView).setIconType(IconType.FOLDER);
	}
	
	@Test
	public void testConfigureAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		entity = new TableEntity();
		when(mockBundle.getEntity()).thenReturn(entity);
		titleBar.configure(mockBundle);
		verify(mockView).setFavoritesWidgetVisible(false);
		verify(mockView).setIconType(IconType.TABLE);
	}


}
