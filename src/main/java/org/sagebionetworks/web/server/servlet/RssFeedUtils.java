package org.sagebionetworks.web.server.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sagebionetworks.repo.model.RSSEntry;
import org.sagebionetworks.repo.model.RSSFeed;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.server.HttpUtils;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RssFeedUtils {

	public static String getAllFeedData(String feedUrl) throws RestServiceException {
		return getFeedData(feedUrl, null, false);
	}

	public static String getFeedData(String feedUrl, Integer limit, boolean summariesOnly){
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
	
	public static String getWikiPageContent(String pageId){
		String urlString = DisplayUtils.WIKI_CONTENT_URL + pageId;
		String xml = "";
		try {
			xml = HttpUtils.httpGet(urlString, new HashMap<String, String>());
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not read from the source: " + urlString, e);
		}
		return parseContent(xml);
	}
	
	public static String getWikiPageSourceContent(String pageId) {
		String sourceHtml = "";
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("pageId", pageId);
			sourceHtml = HttpUtils.httpGet(DisplayUtils.WIKI_PAGE_SOURCE_CONTENT_URL, params);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not read from the source: " + DisplayUtils.WIKI_PAGE_SOURCE_CONTENT_URL + " for pageId " + pageId, e);
		}
		
		return trimWikiSourceHtml(sourceHtml);
	}
	
	public static String trimWikiSourceHtml(String sourceHtml) {
		String returnHtml = "";
		if (sourceHtml != null) {
			int startIndex = sourceHtml.indexOf(DisplayUtils.WIKI_SOURCE_DELIMITER);
			int endIndex = sourceHtml.lastIndexOf(DisplayUtils.WIKI_SOURCE_DELIMITER);
			if (startIndex > -1 && endIndex > -1)
				returnHtml = sourceHtml.substring(startIndex, endIndex);
		}
		return returnHtml;
	}
	
	public static String parseContent(String xml){
		String pageContent = "";
		try {
			if (xml.trim().length() > 0){
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream is = new ByteArrayInputStream(xml.getBytes());
				Document doc = db.parse(is);
	
				pageContent = ((Element)doc.getElementsByTagName("content").item(0)).getElementsByTagName("body").item(0).getFirstChild().getNodeValue();
				
			}
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException("Could not parse the source data: " + xml, e);
		} catch (SAXException e) {
			throw new IllegalArgumentException("Could not parse the source data: " + xml, e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not read from the source data: " + xml, e);
		}
		return pageContent;
	}	
}
