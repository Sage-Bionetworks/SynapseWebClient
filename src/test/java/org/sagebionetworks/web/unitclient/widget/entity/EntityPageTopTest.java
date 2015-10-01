package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.events.EntityDeletedEvent;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.renderer.IFrameWidgetView;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidget;
import org.sagebionetworks.web.client.widget.handlers.AreaChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableRowHeader;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;

public class EntityPageTopTest {

	QueryTokenProvider queryTokenProvider;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	EntityPageTopView mockView;
	EntitySchemaCache mockSchemaCache;
	JSONObjectAdapter jsonObjectAdapter;
	IconsImageBundle mockIconsImageBundle;
	EventBus mockEventBus;
	JiraURLHelper mockJiraURLHelper;
	WidgetRegistrar mockWidgetRegistrar;
	EntityPageTop pageTop;
	ExampleEntity entity;
	TableEntity tableEntity;
	WidgetRendererPresenter testWidgetRenderer;
	String entityId = "syn123";
	Long entityVersion = 1L;
	String projectId = "syn456";
	String wikiSubpage = "987654";
	EntityBundle entityBundle;
	EntityBundle entityBundleTable;
	EntityHeader projectHeader;
	ArgumentCaptor<Synapse> capture;
	Synapse gotoPlace;
	EntityBundle projectBundle;
	Project projectEntity = new Project();		
	AreaChangeHandler areaChangeHandler;
	Query query;


	@Before
	public void before() throws JSONObjectAdapterException {
		queryTokenProvider = new QueryTokenProvider(new AdapterFactoryImpl());
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockView = mock(EntityPageTopView.class);
		mockSchemaCache = mock(EntitySchemaCache.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		mockEventBus = mock(EventBus.class);
		mockJiraURLHelper = mock(JiraURLHelper.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		areaChangeHandler = mock(AreaChangeHandler.class);
		
		pageTop = new EntityPageTop(mockView, mockAuthenticationController,
				mockSchemaCache,
				mockGlobalApplicationState, mockEventBus, queryTokenProvider);
		pageTop.setAreaChangeHandler(areaChangeHandler);
		pageTop.clearProjectAreaState();
		// Setup the the entity
		entity = new ExampleEntity();
		entity.setId(entityId);
		entity.setEntityType(ExampleEntity.class.getName());
		testWidgetRenderer = new YouTubeWidget(mock(IFrameWidgetView.class));
		when(mockWidgetRegistrar.getWidgetRendererForWidgetDescriptor(any(WikiPageKey.class), anyString(), any(Map.class), any(Callback.class), any(Long.class))).thenReturn(testWidgetRenderer);

		
		// setup table entity
		tableEntity = new TableEntity();
		tableEntity.setId(entityId);
		tableEntity.setEntityType(TableEntity.class.getName());
		entityBundleTable = new EntityBundle();
		entityBundleTable.setEntity(tableEntity);
		
		entityBundle = new EntityBundle();
		entityBundle.setEntity(entity);
		projectHeader = new EntityHeader();
		projectHeader.setId(projectId);
		
		projectEntity.setId(projectId);
		projectBundle = new EntityBundle();
		projectBundle.setEntity(projectEntity);
		
		// setup a complex query.
		query = new Query();
		query.setSql("select one, two, three from syn123 where name=\"bar\" and type in('one','two','three'");
		query.setLimit(101L);
		query.setOffset(33L);
		query.setIsConsistent(true);
		SortItem one = new SortItem();
		one.setColumn("one");
		one.setDirection(SortDirection.ASC);
		SortItem two = new SortItem();
		two.setColumn("one");
		two.setDirection(SortDirection.DESC);
		query.setSort(Arrays.asList(one, two));
		
		ObjectSchema schema = new ObjectSchema();
		schema.setTitle("");
		when(mockSchemaCache.getSchemaEntity(any(Entity.class))).thenReturn(schema);
	}
}
