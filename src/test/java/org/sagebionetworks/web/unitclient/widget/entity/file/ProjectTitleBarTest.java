package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.Widget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.file.ProjectTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.ProjectTitleBarView;

@RunWith(MockitoJUnitRunner.class)
public class ProjectTitleBarTest {

  ProjectTitleBar titleBar;

  @Mock
  ProjectTitleBarView mockView;

  @Mock
  AuthenticationController mockAuthController;

  @Mock
  EntityBundle mockBundle;

  @Mock
  FavoriteWidget mockFavoriteWidget;

  Entity entity;
  String testEntityName = "Entity Name";
  String entityId = "syn123";

  @Before
  public void setup() {
    titleBar =
      new ProjectTitleBar(mockView, mockAuthController, mockFavoriteWidget);
    entity = new Folder();
    entity.setId(entityId);
    entity.setName(testEntityName);
    when(mockBundle.getEntity()).thenReturn(entity);
    verify(mockView).setFavoritesWidget(any(Widget.class));
    verify(mockFavoriteWidget).asWidget();
    when(mockAuthController.isLoggedIn()).thenReturn(true);
  }

  @Test
  public void testAsWidget() {
    titleBar.asWidget();
  }

  @Test
  public void testConfigureLoggedIn() {
    titleBar.configure(mockBundle);
    verify(mockView).setFavoritesWidgetVisible(true);
    verify(mockView).setTitle(testEntityName);
    verify(mockView).setEntityType(EntityType.folder);
  }

  @Test
  public void testConfigureAnonymous() {
    when(mockAuthController.isLoggedIn()).thenReturn(false);
    entity = new TableEntity();
    entity.setId(entityId);
    when(mockBundle.getEntity()).thenReturn(entity);
    titleBar.configure(mockBundle);
    verify(mockView).setFavoritesWidgetVisible(false);
    verify(mockView).setEntityType(EntityType.table);
  }
}
