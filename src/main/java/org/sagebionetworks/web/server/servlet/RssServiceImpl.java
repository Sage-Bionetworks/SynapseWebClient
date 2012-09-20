package org.sagebionetworks.web.server.servlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.RssService;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class RssServiceImpl extends RemoteServiceServlet implements RssService {
	private static final long serialVersionUID = 1L;
	
	// Cache all known responses!
	private Map<String, String> cache = new ConcurrentHashMap<String, String>();
	private Collection<CacheProvider> registeredCacheProviders = Collections.synchronizedCollection(new ArrayList<CacheProvider>());
	private static Logger logger = Logger.getLogger(RssServiceImpl.class.getName());
	
	public RssServiceImpl() {
		//register all known cache providers
		registerKnownCacheProviders();
	}
	
	public RssServiceImpl(List<CacheProvider> providers) {
		//register the given CacheProviders and update the cache once (used for testing purposes)
		registerCacheProviders(providers);
		try {
			updateCache();
		} catch (Throwable e) {
			logger.throwing(RssServiceImpl.class.getName(), "RssServiceImpl(List<CacheProvider> providers)", e);
		}
	}
	
	protected void registerCacheProviders(List<CacheProvider> providers) {
		registeredCacheProviders.addAll(providers);
	}
	
	protected void registerKnownCacheProviders(){
		//add all known cache providers to the list
		if (registeredCacheProviders.isEmpty()) {
			registeredCacheProviders.add(new BCCOverviewCacheProvider());
			registeredCacheProviders.add(new BCCSummaryContentCacheProvider());
			registeredCacheProviders.add(new SupportFeedCacheProvider());
			registeredCacheProviders.add(new NewsFeedCacheProvider());
			registeredCacheProviders.add(new DataAccessLevelsCacheProvider());
		}
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		//update the cache now, and every 5 minutes
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		ScheduledFuture<?> scheduleHandle = scheduler.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				try {
					updateCache();
				} catch (Throwable e) {
					logger.throwing(RssServiceImpl.class.getName(), "updateCache()", e);
				}
			}
		}, 0, 5, TimeUnit.MINUTES);
	}
	
	private void updateCache() throws RestServiceException{
		//initialize all of the feeds/pages that our app supports
		logger.info("updating cache");
		//go through all cache providers, and update the content
		for (Iterator it = registeredCacheProviders.iterator(); it.hasNext();) {
			CacheProvider cacheProvider = (CacheProvider) it.next();
			cache.put(cacheProvider.getCacheProviderId(), cacheProvider.getCacheValue());
		}
		logger.info("finished cache update");
	}
	@Override
	public String getCachedContent(String cacheproviderId) {
		String cacheValue = cache.get(cacheproviderId);
		if (cacheValue == null)
			throw new IllegalArgumentException(DisplayConstants.ERROR_EXTERNAL_CONTENT_NOT_IN_CACHE + cacheproviderId);
		return cacheValue;
	}
	
}

