package QueryTokenProvider;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.unitclient.presenter.GWTStub;

public class QueryTokenProviderTest {
	
	Query query;
	QueryTokenProvider provider;
	
	@Before
	public void before(){
		provider = new QueryTokenProvider(new AdapterFactoryImpl(), new GWTStub());
		// Setup a complex query
		query = new Query();
		query.setSql("select one, two, three from syn123 where name=\"bar\" and type in('one','two','three'");
		query.setLimit(101L);
		query.setOffset(33L);
		query.setIsConsistent(true);
		SortItem one = new SortItem();
		one.setColumn("one");
		one.setDirection(SortDirection.ASC);
		SortItem two = new SortItem();
		two.setColumn("one");
		two.setDirection(SortDirection.DESC);
		query.setSort(Arrays.asList(one, two));
	}
	
	@Test
	public void testRoundTrip(){
		// To JSON
		String json = provider.queryToToken(query);
		Query clone = provider.tokenToQuery(json);
		assertEquals(query, clone);
	}
	
	@Test
	public void testBadToken(){
		assertEquals("Invalid tokens should return null and not throw exceptions.", null, provider.tokenToQuery("Not a valid token"));
	}
	
	@Test 
	public void testEncoded(){
		Query expected = new Query();
		expected.setSql("SELECT * FROM syn1681358");
		expected.setIsConsistent(true);
		expected.setLimit(25L);
		expected.setOffset(0L);
		String token = "%7B%22limit%22:25,%20%22sql%22:%22SELECT%20*%20FROM%20syn1681358%22,%20%22isConsistent%22:true,%20%22offset%22:0%7D";
		assertEquals(expected, provider.tokenToQuery(token));
	}

}
