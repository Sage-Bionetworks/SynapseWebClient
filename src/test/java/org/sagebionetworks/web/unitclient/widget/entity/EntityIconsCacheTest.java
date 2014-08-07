package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeView;
import org.sagebionetworks.web.client.widget.entity.EntityIconsCache;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityIconsCacheTest {
	EntityTypeProvider mockEntityTypeProvider;
	IconsImageBundle mockIconsImageBundle;
	EntityIconsCache entityIconsCache;
	ImageResource mockProjectIcon;

	@Before
	public void before() {
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		entityIconsCache = new EntityIconsCache(mockEntityTypeProvider, mockIconsImageBundle);
		EntityType type = new EntityType("Project", Project.class.getName(), "", null);
		when(mockEntityTypeProvider.getEntityTypeForString(anyString())).thenReturn(type);
		mockProjectIcon = mock(ImageResource.class);
		when(mockIconsImageBundle.synapseProject16()).thenReturn(mockProjectIcon);
	}
	
	@Test
	public void testCaching() throws Exception {
		//type will map to project
		String testType = "Project";
		ImageResource result = entityIconsCache.getIconForType(testType);
		//should have returned the mock project icon
		assertEquals(mockProjectIcon, result);
		//not from the cache
		verify(mockIconsImageBundle).synapseProject16();
		
		//but asking for the same type again should pull from the cache...
		reset(mockIconsImageBundle); //reset count on mock icons image bundle
		result = entityIconsCache.getIconForType(testType);
		//same result
		assertEquals(mockProjectIcon, result);
		verify(mockIconsImageBundle, times(0)).synapseProject16();
	}
}
