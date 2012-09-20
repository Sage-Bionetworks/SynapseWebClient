package org.sagebionetworks.web.unitserver;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.servlet.CacheProvider;
import org.sagebionetworks.web.server.servlet.RssServiceImpl;

public class RssServiceImplTest {
	
	private static RssServiceImpl service = null;
	private String testCacheProvider1Id = "myCacheProvider1";
	private String testCacheProvider1Value = "Some cached content!";
	
	@Before
	public void setup(){
		// Create the service
		//with a specific cache provider list
		
		CacheProvider testCacheProvider1 = new CacheProvider(){
			@Override
			public String getCacheProviderId() {
				return testCacheProvider1Id;
			}
			@Override
			public String getCacheValue() {
				return testCacheProvider1Value;
			}
		};
		List<CacheProvider> providers = new ArrayList<CacheProvider>();
		providers.add(testCacheProvider1);
		service = new RssServiceImpl(providers);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNotInCache(){
		service.getCachedContent("fake provider id");
	}
	
	@Test
	public void testInCache(){
		//try a few times (in case we got here before the cache was updated by our provider)
		String cachedValue = service.getCachedContent(testCacheProvider1Id);
		Assert.assertEquals("Unexpected cached value", testCacheProvider1Value, cachedValue);
	}
	
}
