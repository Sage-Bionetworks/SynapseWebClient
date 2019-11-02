package org.sagebionetworks.web.unitclient.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.cache.ClientCacheImpl;
import org.sagebionetworks.web.client.cache.StorageWrapper;

public class ClientCacheImplTest {

	ClientCacheImpl cache;
	StorageWrapper mockStorage;

	@Before
	public void setup() {
		mockStorage = mock(StorageWrapper.class);
		when(mockStorage.isStorageSupported()).thenReturn(true);
		cache = new ClientCacheImpl(mockStorage);
	}

	@Test
	public void testRoundTrip() {
		String key = "testkey";
		String value = "testValue";
		when(mockStorage.getItem(eq(key))).thenReturn(value);
		cache.put(key, value);
		when(mockStorage.getItem(key + ClientCacheImpl.SUFFIX)).thenReturn(Long.toString(System.currentTimeMillis() + ClientCacheImpl.DEFAULT_CACHE_TIME_MS));
		verify(mockStorage).setItem(eq(key), eq(value));
		assertTrue(cache.contains(key));
		assertEquals(value, cache.get(key));
	}

	@Test
	public void testExpiration() {
		String key = "testkey";
		String value = "testValue";
		Long expireTime = System.currentTimeMillis() - 1L;
		when(mockStorage.getItem(key + ClientCacheImpl.SUFFIX)).thenReturn(expireTime.toString());
		when(mockStorage.getItem(eq(key))).thenReturn(value);
		// put something in that is already expired
		cache.put(key, value, expireTime);
		// it should not come back because it is expired
		assertNull(cache.get(key));
		verify(mockStorage).removeItem(eq(key));
	}

	@Test
	public void testExpirationContains() {
		String key = "testkey";
		String value = "testValue";
		Long expireTime = System.currentTimeMillis() - 1L;
		when(mockStorage.getItem(eq(key))).thenReturn(value);
		when(mockStorage.getItem(key + ClientCacheImpl.SUFFIX)).thenReturn(expireTime.toString());
		// put something in that is already expired
		cache.put(key, value, expireTime);
		assertFalse(cache.contains(key));
		verify(mockStorage).removeItem(eq(key));
	}

	@Test
	public void testNoStorageAvailable() {
		when(mockStorage.isStorageSupported()).thenReturn(false);
		String key = "testkey";
		String value = "testValue";

		cache.put(key, value);
		assertFalse(cache.contains(key));
		assertNull(cache.get(key));
	}

}
