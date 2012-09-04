package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.repo.model.RSSEntry;
import org.sagebionetworks.repo.model.RSSFeed;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.RssService;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RssServiceImpl extends RemoteServiceServlet implements RssService {
	private static final long serialVersionUID = 1L;
	
		
	@Override
	public String getAllFeedData(String feedUrl) throws RestServiceException {
		return getFeedData(feedUrl, null, false);
	}
	
	@Override
	public String getFeedData(String feedUrl, Integer limit, boolean summariesOnly) throws RestServiceException {
		String jsonResponse = "";
		try {
			URL feedSource = new URL(feedUrl);
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(feedSource));
			jsonResponse = getFeed(feed, limit, summariesOnly);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Could not connect to the given url: " + feedUrl, e);
		} catch (FeedException e) {
			throw new IllegalArgumentException("Could not parse the given feed: " + feedUrl, e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not read the feed source: " + feedUrl, e);
		}
		return jsonResponse;
	}
	
	public static String getFeed(SyndFeed feed, Integer limit, boolean summariesOnly) {
		//Create a cache (check latest post. if hasn't changed, return cached html. if changed, set the latest post for the feed and calculate the new html response)? 
		//Most of the requests are going to be the same from the client. The question is how frequent is the feed updated.
		RSSFeed jsonFeed = new RSSFeed();
		jsonFeed.setAuthor(feed.getAuthor());
		jsonFeed.setDescription(feed.getDescription());
		jsonFeed.setTitle(feed.getTitle());
		jsonFeed.setUri(feed.getUri());
		List<RSSEntry> jsonEntries = new ArrayList<RSSEntry>();
		jsonFeed.setEntries(jsonEntries);
		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
		
		List<SyndEntry> entries = feed.getEntries();
		if (limit == null)
			limit = entries.size();
		for (int i = 0; i < limit && i < entries.size(); i++) {
			SyndEntry syndEntry = entries.get(i);
			RSSEntry rssEntry = new RSSEntry();
			jsonEntries.add(rssEntry);
			rssEntry.setAuthor(syndEntry.getAuthor());
			rssEntry.setTitle(syndEntry.getTitle());
			rssEntry.setDate(df.format(syndEntry.getPublishedDate()));
			rssEntry.setLink(syndEntry.getLink());

			if (summariesOnly || syndEntry.getContents() == null || syndEntry.getContents().size() == 0){
				String summary = syndEntry.getDescription().getValue();
				if (summariesOnly){
					summary = summary.replaceAll("<p>", "");
					if (summary.length() > 1000){
						summary = summary.substring(0, 500) + "...";
					}
				}
					
				rssEntry.setContent(summary);
			}
			else{ //full content
				for (Iterator<SyndContent> it = syndEntry.getContents().iterator(); it.hasNext();) {
			        SyndContent syndContent = it.next();
			        StringBuilder content = new StringBuilder();
			        if (syndContent != null) {
			        	content.append(syndContent.getValue() + "\n");
			        }
			        rssEntry.setContent(content.toString());
				}
		    }
		}
		
		try {
			return EntityFactory.createJSONStringForEntity(jsonFeed);
		} catch (JSONObjectAdapterException e) {
			throw new IllegalArgumentException("Could not parse the feed source: " + feed.getUri(), e);
		}
	}
	
	@Override
	public String getPageContent(String urlString){
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL(urlString);
			URLConnection con = url.openConnection();
			Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
			Matcher m = p.matcher(con.getContentType());
			String charset = m.matches() ? m.group(1) : "UTF-8";
			Reader r = new InputStreamReader(con.getInputStream(), charset);
			while (true) {
				int c = r.read();
				if (c < 0)
					break;
				sb.append((char) c);
			}
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(
					"Could not connect to the source: " + urlString, e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not read the source: "
					+ urlString, e);
		}
		return sb.toString();
	}	
}