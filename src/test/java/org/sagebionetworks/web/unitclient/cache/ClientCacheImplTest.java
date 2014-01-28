package org.sagebionetworks.web.unitclient.cache;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;


import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.cache.ClientCacheImpl;
import org.sagebionetworks.web.client.cache.StorageWrapper;

import com.google.gwt.storage.client.Storage;

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
		verify(mockStorage).setItem(eq(key), eq(value));
		assertTrue(cache.contains(key));
		assertEquals(value, cache.get(key));
	}
	
	@Test
	public void testExpiration() {
		String key = "testkey";
		String value = "testValue";
		when(mockStorage.getItem(eq(key))).thenReturn(value);
		//put something in that is already expired
		cache.put(key, value, System.currentTimeMillis() - 1L);
		//it should not come back because it is expired
		assertNull(cache.get(key));
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
