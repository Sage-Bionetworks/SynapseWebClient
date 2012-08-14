package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.RSSEntry;
import org.sagebionetworks.repo.model.RSSFeed;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.server.servlet.RssServiceImpl;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RssServiceImplTest {
	
	private static RssServiceImpl service = null;
	private static SyndFeed syndFeed = null;
	private static String rssXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\"	xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"	xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\"	xmlns:dc=\"http://purl.org/dc/elements/1.1/\"	xmlns:atom=\"http://www.w3.org/2005/Atom\"	xmlns:sy=\"http://purl.org/rss/1.0/modules/syndication/\"	xmlns:slash=\"http://purl.org/rss/1.0/modules/slash/\"	xmlns:georss=\"http://www.georss.org/georss\" xmlns:geo=\"http://www.w3.org/2003/01/geo/wgs84_pos#\" xmlns:media=\"http://search.yahoo.com/mrss/\"	><channel>	<title>Sage Synapse </title>	<atom:link href=\"https://sagesynapse.wordpress.com/feed/\" rel=\"self\" type=\"application/rss+xml\" />	<link>https://sagesynapse.wordpress.com</link>	<description>News about Sage Bionetwork&#039;s Synapse project</description>	<lastBuildDate>Thu, 09 Aug 2012 23:20:35 +0000</lastBuildDate>	<language>en</language>	<sy:updatePeriod>hourly</sy:updatePeriod>	<sy:updateFrequency>1</sy:updateFrequency>	<generator>http://wordpress.com/</generator><cloud domain='sagesynapse.wordpress.com' port='80' path='/?rsscloud=notify' registerProcedure='' protocol='http-post' /><image>		<url>https://s2.wp.com/i/buttonw-com.png</url>		<title>Sage Synapse </title>		<link>https://sagesynapse.wordpress.com</link>	</image>	<atom:link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"https://sagesynapse.wordpress.com/osd.xml\" title=\"Sage Synapse \" />	<atom:link rel='hub' href='https://sagesynapse.wordpress.com/?pushpress=hub'/>		<item>		<title>If I had a billion dollars...</title>		<link>https://sagesynapse.wordpress.com/2012/08/09/38/</link>		<comments>https://sagesynapse.wordpress.com/2012/08/09/38/#comments</comments>		<pubDate>Thu, 09 Aug 2012 22:34:17 +0000</pubDate>		<dc:creator>sagesynapsenews</dc:creator>				<category><![CDATA[Breast Cancer Challenge]]></category>		<category><![CDATA[Guest Posts]]></category>		<guid isPermaLink=\"false\">http://sagesynapse.wordpress.com/2012/08/09/38/</guid>		<description><![CDATA[Reblogged from Science, Reengineered: In an apparently recurring theme, my thoughts again are running to the incentives that drive human behavior, this time inspired by the recent news that the Russian billionaire Yuri Milner has established a new $3 Million Fundamental Physics Prize.  He&#8217;s actually awarded 9 of these prizes for a cool $27M promoting [...]<img alt=\"\" border=\"0\" src=\"http://stats.wordpress.com/b.gif?host=sagesynapse.wordpress.com&#038;blog=39174488&#038;post=38&#038;subd=sagesynapse&#038;ref=&#038;feed=1\" width=\"1\" height=\"1\" />]]></description>				<content:encoded><![CDATA[<div class=\"reblog-post\"><p class=\"reblog-from\"><img alt='' src='https://1.gravatar.com/avatar/11744786cf82d08f17b96d11e926a315?s=25&amp;d=identicon&amp;r=G' class='avatar avatar-25' height='25' width='25' /> <a href=\"http://sciencereengineered.com/2012/08/09/if-i-had-a-billion-dollars-2/\">Reblogged from Science, Reengineered:</a></p><div class=\"wpcom-enhanced-excerpt\"><div class=\"wpcom-enhanced-excerpt-content\"><p>In an apparently recurring theme, my thoughts again are running to the incentives that drive human behavior, this time inspired by the recent news that the Russian billionaire Yuri Milner has established a new $3 Million <a href=\"Fundamental Physics Prize\">Fundamental Physics Prize</a>.  He&#8217;s actually awarded 9 of these prizes for a cool $27M promoting the efforts of theoretical physics.  Certainly that kind of money and publicity could drive a lot of attention to the field, and I love the fact that we now almost have a basketball team&#8217;s worth of physicists who almost make a basketball player&#8217;s salary.</p></div> <p class=\"read-more\"><a href=\"http://sciencereengineered.com/2012/08/09/if-i-had-a-billion-dollars-2/\" target=\"_self\"><span>Read more&hellip;</span> 901 more words</a></p></div></div><div class=\"reblogger-note\"><div class='reblogger-note-content'>Guest post from Michael Kellen on incentives and competitions</div></div>]]></content:encoded>			<wfw:commentRss>https://sagesynapse.wordpress.com/2012/08/09/38/feed/</wfw:commentRss>		<slash:comments>0</slash:comments>			<media:content url=\"https://0.gravatar.com/avatar/09a4cbe84d602a57c798adde2d1dc1f5?s=96&#38;d=identicon&#38;r=G\" medium=\"image\">			<media:title type=\"html\">sagesynapsenews</media:title>		</media:content>	</item>	</channel></rss>";
			
	@Before
	public void setup(){
		// Create the service
		service = new RssServiceImpl();
		SyndFeedInput input = new SyndFeedInput();
		InputStream is = new ByteArrayInputStream(rssXML.getBytes());
		try {
			syndFeed = input.build(new XmlReader(is));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testNewsFeed(){
		try {
			String feedDataJson = RssServiceImpl.getFeed(syndFeed, null, false);
			if (feedDataJson == null || feedDataJson.length() == 0){
				fail("Empty news feed." + DisplayUtils.NEWS_FEED_URL);
			}
			RSSFeed theFeed = EntityFactory.createEntityFromJSONString(feedDataJson, RSSFeed.class);
			Assert.assertTrue("News feed has no entries.", theFeed.getEntries() != null && theFeed.getEntries().size() > 0);
			RSSEntry firstEntry = theFeed.getEntries().get(0);
			Assert.assertTrue("First entry has no content.", firstEntry.getContent() != null && firstEntry.getContent().length() > 0);
		} catch (JSONObjectAdapterException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSubsetNewsFeed(){
		try {
			//ask for the latest entry, and only ask for the summary
			String feedDataJson = RssServiceImpl.getFeed(syndFeed, 1, true);
			if (feedDataJson == null || feedDataJson.length() == 0){
				fail("Empty news feed." + DisplayUtils.NEWS_FEED_URL);
			}
			RSSFeed theFeed = EntityFactory.createEntityFromJSONString(feedDataJson, RSSFeed.class);
			Assert.assertTrue("Requested 1 entry failure.", theFeed.getEntries() != null && theFeed.getEntries().size() == 1);
			RSSEntry firstEntry = theFeed.getEntries().get(0);
			Assert.assertTrue("Entry has no content.", firstEntry.getContent() != null && firstEntry.getContent().length() > 0);
		} catch (JSONObjectAdapterException e) {
			fail(e.getMessage());
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidUrl() throws RestServiceException{
		service.getAllFeedData("invalid url");
	}	
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidFeed() throws RestServiceException{
		service.getAllFeedData("file://invalidfile");
	}	
}
