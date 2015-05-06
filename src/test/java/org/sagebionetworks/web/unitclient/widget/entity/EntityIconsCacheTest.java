package org.sagebionetworks.web.unitclient.widget.entity;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.widget.entity.EntityIconsCache;

import com.google.gwt.resources.client.ImageResource;

public class EntityIconsCacheTest {
	IconsImageBundle mockIconsImageBundle;
	EntityIconsCache entityIconsCache;
	ImageResource mockProjectIcon;

	@Before
	public void before() {
		mockIconsImageBundle = mock(IconsImageBundle.class);
		entityIconsCache = new EntityIconsCache(mockIconsImageBundle);
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
