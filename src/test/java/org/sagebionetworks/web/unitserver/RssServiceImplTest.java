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
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidWikiXml() throws RestServiceException{
		service.parseContent("invalid xml");
	}
	
	@Test
	public void testParseWikiXML(){
		String content = service.parseContent("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><content type=\"page\" expand=\"space,children,comments,attachments,labels\" id=\"24084517\"><link rel=\"alternate\" type=\"text/html\" href=\"https://sagebionetworks.jira.com/wiki/display/SWC/Synapse+Web+Client+BCC+Content\"/><link rel=\"alternate\" type=\"application/pdf\" href=\"https://sagebionetworks.jira.com/wiki/spaces/flyingpdf/pdfpageexport.action?pageId=24084517\"/><link rel=\"self\" href=\"https://sagebionetworks.jira.com/wiki/rest/prototype/1/content/24084517\"/><title>Synapse Web Client BCC Content</title><wikiLink>[SWC:Synapse Web Client BCC Content]</wikiLink><lastModifiedDate date=\"2012-09-04T14:41:05-0700\" friendly=\"yesterday at 2:41 PM\"/><createdDate date=\"2012-09-04T13:56:56-0700\" friendly=\"yesterday at 1:56 PM\"/><space type=\"space\" title=\"Synapse Web Client\" name=\"Synapse Web Client\" key=\"SWC\"><link rel=\"self\" href=\"https://sagebionetworks.jira.com/wiki/rest/prototype/1/space/SWC\"/></space><children size=\"1\"/><comments total=\"0\"/><body type=\"2\">&lt;h1&gt;Sage / DREAM breast cancer prognosis challenge&lt;/h1&gt;&lt;p&gt;The past two decades have seen an amazing growth in the ability to generate genomic data fueled by the rapidly decreasing cost of sequencing technologies. However, with a few exceptions, acquisition of this type of data has so far failed to generate significant improvements in the treatment of human diseases. Improving the methodology around this genotype to phenotype prediction problem is an active area of research. The challenge covers both the statistical / machine learning approaches to analyzing clinical genomics data as well as the broader incentives driving how scientists collaborate though shared data and approaches.&lt;/p&gt;&lt;p&gt;Sage Bionetworks believes that a key impediment to advances in this area is the relatively closed nature of scientific research built around the publication - grant - work cycle of academic research, or the walled-off approaches of industry. For the past 6 years the DREAM project has organized a series of challenges in Systems Biology meant to drive the field forward by getting multiple groups to attack the same problems from different angles. These groups feel that catalyzing more open and collaborative approaches to scientific research is an essential part of moving the field forward, and have partnered to organize the Sage / DREAM Breast Cancer Prognosis Challenge.&lt;/p&gt;&lt;p&gt;The goal of the breast cancer prognosis challenge is to assess the accuracy of computational models designed to predict breast cancer survival (median 10 year follow up) based on clinical information about the patient's tumor as well as genome-wide molecular profiling data including gene expression and copy number profiles. This challenge is fueled by the generous donation of clinical study data on 1,200 breast cancer patients obtained by Carlos Caldas of Cancer Research UK and Anne-Lise Borresen-Dale of Oslo University Hospital.&lt;/p&gt;&lt;h4&gt;July 18 Challenge Launch Webinar&lt;/h4&gt;&lt;p&gt;www.youtube.com/embed/xSfd5mkkmGM&lt;/p&gt;&lt;h2&gt;Contest Resources&lt;/h2&gt;&lt;h4&gt;&lt;span style=&quot;text-decoration: underline;&quot;&gt;&lt;a href=&quot;https://sagebionetworks.jira.com/wiki/display/BCC/Home&quot;&gt;Technical Information&lt;/a&gt;&lt;/span&gt;&lt;/h4&gt;&lt;h4&gt;&lt;span style=&quot;text-decoration: underline;&quot;&gt;&lt;a href=&quot;http://support.sagebase.org/&quot;&gt;Community and Support&lt;/a&gt;&lt;/span&gt;&lt;/h4&gt;&lt;h4&gt;&lt;span style=&quot;text-decoration: underline;&quot;&gt;&lt;a href=&quot;http://validation.bcc.sagebase.org/bcc-leaderboard-public.php&quot;&gt;Real-time Leader Board&lt;/a&gt;&lt;/span&gt;&lt;/h4&gt;&lt;p&gt;&lt;span style=&quot;color: rgb(102,102,102);&quot;&gt;&amp;nbsp;&lt;/span&gt;&lt;/p&gt;&lt;h5&gt;The challenge consists of building predictive models of breast cancer survival, specified by survival data containing:&lt;/h5&gt;&lt;ul class=&quot;list arrow-list&quot;&gt;&lt;li&gt;Time from diagnosis until death, or time of last follow-up if the patient is not known to have died.&lt;/li&gt;&lt;li&gt;Whether the patient was alive at last follow up time.&lt;/li&gt;&lt;/ul&gt;&lt;h5&gt;Predictive models will be built using the following feature data:&lt;/h5&gt;&lt;ul class=&quot;list arrow-list&quot;&gt;&lt;li&gt;Genome-wide gene expression profiles&lt;/li&gt;&lt;li&gt;Genome-wide gene copy number profiles&lt;/li&gt;&lt;li&gt;Detailed clinical information about each tumor&lt;/li&gt;&lt;li&gt;Additional information from related data sets, such as other breast cancer studies (some suggested publicly available datasets will be formatted and provided to users as part of the challenge)&lt;/li&gt;&lt;/ul&gt;&lt;h5&gt;The final scoring of the algorithms will be performed by scoring them against data to be obtained from another 350 patients, with tissue samples already banked. The challenge time line is:&lt;/h5&gt;&lt;ul class=&quot;list arrow-list&quot;&gt;&lt;li&gt;June-July 17th, 2012: Sign up for the Sage Bionetworks-DREAM Breast Cancer Prognosis Challenge. Registered participants will be notified by email about the initiation of the Challenge.&lt;/li&gt;&lt;li&gt;July 17th-October 15th, 2012: A live demo call was held on July 17th to help participants get started with the Challenge. A step by step guide is available&amp;nbsp;&lt;a href=&quot;https://sagebionetworks.jira.com/wiki/display/BCC/Getting+Started&quot; style=&quot;text-decoration: none;margin-left: 0.0px;&quot;&gt;&lt;span style=&quot;text-decoration: underline;&quot;&gt;here&lt;/span&gt;&lt;/a&gt;&amp;nbsp;with additional details about the Challenge available at&amp;nbsp;&lt;a href=&quot;https://sagebionetworks.jira.com/wiki/display/BCC/Breast+Cancer+Challenge%3A+Detailed+Description&quot; style=&quot;text-decoration: none;margin-left: 0.0px;&quot;&gt;&lt;span style=&quot;text-decoration: underline;&quot;&gt;Breast Cancer Challenge: Detailed Description&lt;/span&gt;&lt;/a&gt;. Data from 1,000 samples will be provided to participants for training of models. An additional 500 samples will be used to provide real-time evaluation of all submitted models. The remaining 500 samples will be used for final scoring of all models (taking place after October 15th).&lt;/li&gt;&lt;li&gt;October 15th, 2012: Final submission of all models, to be scored against the 500 Metabric data samples not used in the previous phase. The deadline for submitting models for the Breast Cancer Prognosis Challenge is 5PM EST October 15th, and the best performers will be announced at the DREAM 7 Conference taking place in San Francisco on November 12 to 16.&lt;/li&gt;&lt;li&gt;Late 2012: Final assessment of all models in newly generated data. An additional cohort of approximately 350 breast cancer samples with archived fresh frozen tumor samples has been identified by Anne-Lise Borresen-Dale of Oslo University Hospital and a generous donation has been made by the Avon Foundation to obtain gene expression and copy number data on these samples. We are currently curating the clinical records of this patient cohort to harmonize with the current METABRIC dataset and working on generating the genomic profiling data for these samples. We aim to generate these data by the November 12 DREAM conference (being held in San Francisco) and announce initiation of the final evaluation to be performed on this data set. We will keep participants informed on progress in generating these data.&lt;/li&gt;&lt;/ul&gt;&lt;p&gt;A challenge of standardizing computational models developed for this competition is that most sophisticated models require the use of large compute clusters for model optimization. Therefore, individual labs often program customized workflows to run on their own cluster architecture, making it difficult to standardize or re-run analyses. For this reason, prior competition efforts either 1) abandoned the requirement for re-runnable code submission (DREAM6 competition), or 2) limited entries to those that complete in a small amount of time on a single processor (Innocentive competition).&lt;/p&gt;&lt;p&gt;We believe that supporting reusable, extensible code is critical to facilitating the type of transparency, rigor, and community development that we hope to promote with this competition. We have therefore partnered with Google to donate to the community the computational cycles allowing participants to develop and test complex models on a common compute architecture in the cloud. In addition to promoting scientific rigor and transparency, this donation of compute time will also enable the &amp;ldquo;democratization of medicine&amp;rdquo; in which participants from around the world can develop sophisticated methods from a level playing field, without being in a rich institution with access to high-performance compute clusters. The challenge is unique in its attention to the reproducibility and comparison of different analytical approaches; notably it requires contestants to run code in a common compute environment to ensure reproducibility of the results.&lt;/p&gt;</body><attachments size=\"0\"/><labels/><creator><links rel=\"self\" href=\"https://sagebionetworks.jira.com/wiki/rest/prototype/1/user/non-system/jay.hodgson\"/><name>jay.hodgson</name><displayName>Jay Hodgson</displayName><avatarUrl>/wiki/s/en_GB/4005/6b369ed7e36635cc083008aa53c04924ccc64d8e.2/_/images/icons/profilepics/default.gif</avatarUrl><displayableEmail>jay.hodgson@sagebase.org</displayableEmail><anonymous>false</anonymous></creator><lastModifier><links rel=\"self\" href=\"https://sagebionetworks.jira.com/wiki/rest/prototype/1/user/non-system/jay.hodgson\"/><name>jay.hodgson</name><displayName>Jay Hodgson</displayName><avatarUrl>/wiki/s/en_GB/4005/6b369ed7e36635cc083008aa53c04924ccc64d8e.2/_/images/icons/profilepics/default.gif</avatarUrl><displayableEmail>jay.hodgson@sagebase.org</displayableEmail><anonymous>false</anonymous></lastModifier></content>");
		Assert.assertTrue(content.length() > 0);
	}
	
}
