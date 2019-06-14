package org.sagebionetworks.web.unitclient.cache;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.cache.ClientCacheImpl.*;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.cache.ClientCacheImpl;
import org.sagebionetworks.web.client.cache.StorageWrapper;

public class ClientCacheImplTest {
	
	ClientCacheImpl cache;
	StorageWrapper mockStorage;
	
	@Before
	public void setup(){
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
		verify(mockStorage, never()).setItem(eq(PROTECTED_KEYS), anyString());
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
		//put something in that is already expired
		cache.put(key, value, expireTime);
		//it should not come back because it is expired
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
		//put something in that is already expired
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
	
	@Test
	public void testProtectedKeys() {
		// verify setting a protected key updates the protected_keys entry
		when(mockStorage.isStorageSupported()).thenReturn(true);
		String key1 = "testkey1";
		String key2 = "testkey2";
		String value1 = "testValue1";
		String value2 = "testValue2";
		Long expireTime = System.currentTimeMillis() - 1L;
		boolean isProtected = true;
		
		cache.put(key1, value1, expireTime, isProtected);
		cache.put(key2, value2, expireTime, isProtected);
		
		verify(mockStorage).setItem(key1, value1);
		verify(mockStorage).setItem(eq(PROTECTED_KEYS), contains(key1));
		verify(mockStorage).setItem(key2, value2);
		verify(mockStorage).setItem(eq(PROTECTED_KEYS), contains(key2));		
		
		when(mockStorage.getItem(PROTECTED_KEYS)).thenReturn(key1 + "," + key2);
		when(mockStorage.getItem(key1)).thenReturn(value1);
		when(mockStorage.getItem(key2)).thenReturn(value2);
		
		cache.clear();

		// verify that clearing the cache causes the storage to be cleared, and protected keys re-added
		verify(mockStorage).clear();
		verify(mockStorage, times(2)).setItem(key1, value1);
		verify(mockStorage, times(2)).setItem(key2, value2);
		// and the protected keys persist
		verify(mockStorage, times(2)).setItem(eq(PROTECTED_KEYS), contains(key1));
		verify(mockStorage, times(2)).setItem(eq(PROTECTED_KEYS), contains(key2));
	}
}
