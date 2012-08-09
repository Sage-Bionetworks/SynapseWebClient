package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.web.client.RssService;
import org.sagebionetworks.web.server.RestTemplateProvider;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RssServiceImpl extends RemoteServiceServlet implements RssService {
	private static final long serialVersionUID = 1L;
	
	/**
	 * The template is injected with Gin
	 */
	private RestTemplateProvider templateProvider;

	/**
	 * Injected with Gin
	 */
	private ServiceUrlProvider urlProvider;
		
	/**
	 * Injected via Gin.
	 * 
	 * @param template
	 */
	@Inject
	public void setRestTemplate(RestTemplateProvider template) {
		this.templateProvider = template;
	}
	
	/**
	 * Injected via Gin
	 * @param provider
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider){
		this.urlProvider = provider;
	}
	
	@Override
	public String getAllFeedData(String feedUrl) throws RestServiceException {
		return getFeedData(feedUrl, null, false);
	}
	
	@Override
	public String getFeedData(String feedUrl, Integer limit, boolean summariesOnly) throws RestServiceException {
		validateService();
		String htmlResponse = "";
		try {
			URL feedSource = new URL(feedUrl);
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(feedSource));
			htmlResponse = getFeedHtml(feed, limit, summariesOnly);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Could not connect to the given url: " + feedUrl, e);
		} catch (FeedException e) {
			throw new IllegalArgumentException("Could not parse the given feed: " + feedUrl, e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not read the feed source: " + feedUrl, e);
		}
		return htmlResponse;
	}
	
	private String getFeedHtml(SyndFeed feed, Integer limit, boolean summariesOnly){
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
		StringBuilder htmlResponse = new StringBuilder();
		List<SyndEntry> entries = feed.getEntries();
		if (limit == null)
			limit = entries.size();
		for (int i = 0; i < limit && i < entries.size(); i++) {
			SyndEntry syndEntry = entries.get(i);
			htmlResponse.append("<h5 class =\"nobottommargin\"><a href=\"" + syndEntry.getLink() + "\">" + syndEntry.getTitle() + "</a></h5>\n");
			htmlResponse.append("<p class=\"clear small-italic notopmargin nobottommargin\">" + df.format(syndEntry.getPublishedDate()) + "</p>\n");
			
			if (summariesOnly){
				String summary = syndEntry.getDescription().getValue().replaceAll("<p>", "");
				if (summary.length() > 1000){
					summary = summary.substring(0, 300) + "...";
				}
				htmlResponse.append("<p class=\"clear notopmargin \">" + summary + "</p>");
			}
			else{ //full content
				for (Iterator<SyndContent> it = syndEntry.getContents().iterator(); it.hasNext();) {
			        SyndContent syndContent = it.next();
			        
			        if (syndContent != null) {
			        	htmlResponse.append("<p class=\"clear notopmargin \">" + syndContent.getValue() + "</p>");
			        }
				}
		    }
		}
		return htmlResponse.toString();
	}
	
	/**
	 * Validate that the service is ready to go. If any of the injected data is
	 * missing then it cannot run. Public for tests.
	 */
	public void validateService() {
		if (templateProvider == null)
			throw new IllegalStateException(
					"The org.sagebionetworks.web.server.RestTemplateProvider was not injected into this service");
		if (templateProvider.getTemplate() == null)
			throw new IllegalStateException(
					"The org.sagebionetworks.web.server.RestTemplateProvider returned a null template");
		if (urlProvider == null)
			throw new IllegalStateException(
					"The org.sagebionetworks.rest.api.root.url was not set");
	}
}