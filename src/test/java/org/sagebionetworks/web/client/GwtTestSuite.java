package org.sagebionetworks.web.client;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.sagebionetworks.client.exceptions.SynapseClientException;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.schema.FORMAT;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.TYPE;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererNone;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Since the GWT test are so slow to start and we could not get the GWTTestSuite to work,
 * we put all GWT tests in one class.
 * @author jmhill
 *
 */
public class GwtTestSuite extends GWTTestCase {

	/**
	 * Must refer to a valid module that sources this class.
	 */
	public String getModuleName() { 
		return "org.sagebionetworks.web.Portal";
	}
	
	@Override
	public void gwtSetUp() {
		// Create a dataset with all fields filled in
	}

	

	
	/**
	 * Populate a given entity using all of the fields from the schema.
	 * @param toPopulate
	 * @throws JSONObjectAdapterException 
	 */
	private void initilaizedJSONEntityFromSchema(JSONEntity toPopulate) throws JSONObjectAdapterException{
		JSONObjectAdapter adapter =  JSONObjectGwt.createNewAdapter().createNew(toPopulate.getJSONSchema());
		ObjectSchema schema = new ObjectSchema(adapter);
		// This adapter will be used to populate the entity
		JSONObjectAdapter adapterToPopulate = JSONObjectGwt.createNewAdapter();
		Map<String, ObjectSchema> properteis = schema.getProperties();
		assertNotNull(properteis);
		int index = 0;
		for(String propertyName: properteis.keySet()){
			ObjectSchema propertySchema = properteis.get(propertyName);
			if(TYPE.STRING == propertySchema.getType()){
				// This could be a date or an enumeration.
				String value = "StringValue for "+propertyName;
				if(propertySchema.getFormat() == null && propertySchema.getEnum() == null){
					// This is just a normal string
					value = "StringValue for "+propertyName;
				}else if(FORMAT.DATE_TIME == propertySchema.getFormat()){
					value = adapter.convertDateToString(FORMAT.DATE_TIME, new Date());
				}else if(propertySchema.getEnum() != null){
					int enumIndex = propertySchema.getEnum().length-1 % index;
					value = propertySchema.getEnum()[enumIndex];
				}else{
					if(propertySchema.isRequired()){
						throw new IllegalArgumentException("Unknown FORMAT: "+propertySchema.getFormat()+" for required property");
					}
				}
				// Set the string value
				adapterToPopulate.put(propertyName, value);
			}else if(TYPE.BOOLEAN == propertySchema.getType()){
				Boolean value = index % 2 == 0;
				adapterToPopulate.put(propertyName, value);
			}else if(TYPE.INTEGER == propertySchema.getType()){
				Long value = new Long(123+index);
				adapterToPopulate.put(propertyName, value);
			}else if(TYPE.NUMBER == propertySchema.getType()){
				Double value = new Double(456.0909+index);
				adapterToPopulate.put(propertyName, value);
			}else{
				if(propertySchema.isRequired()){
					throw new IllegalArgumentException("Unknown type:"+propertySchema.getType()+" for required property");
				}
			}
			index++;
		}
		// Now populate it with data
		toPopulate.initializeFromJSONObject(adapterToPopulate);
	}
	
	@Test
	public void testCreateException(){
		// This will fail if the project is not configured correctly.
		SynapseException e = new SynapseClientException();
	}
	
	
	@Test
	public void testDecimalNumberFormat() {
		assertNull(APITableColumnRendererNone.getDecimalNumberFormat(null));
		NumberFormat formatter = APITableColumnRendererNone.getDecimalNumberFormat(1);
		assertEquals("12.3",formatter.format(12.3456));
		assertEquals("12.0",formatter.format(12));
	}
	
	@Test
	public void testGetColumnValue() {
		assertEquals("abc", APITableColumnRendererNone.getColumnValue("abc", null));
		assertEquals("13.456", APITableColumnRendererNone.getColumnValue("13.456", null));
		NumberFormat formatter = APITableColumnRendererNone.getDecimalNumberFormat(4);
		// See SWC-1590.  On windows the value will be 13.4568 due to rounding up.
		assertEquals("13.4567".length(), APITableColumnRendererNone.getColumnValue("13.456789", formatter).length());
		assertEquals("hello", APITableColumnRendererNone.getColumnValue("hello", formatter));
	}
	
	public static String entityListToString(List<? extends JSONEntity> list) throws JSONObjectAdapterException {		
		JSONArrayAdapter aa = JSONObjectGwt.createNewAdapter().createNewArray();
		for (int i=0; i<list.size(); i++) {
			JSONObjectAdapter oa = JSONObjectGwt.createNewAdapter();
			list.get(i).writeToJSONObject(oa);
			aa.put(i, oa);
		}
		return aa.toJSONString();
	}
	
	@Override
	public String toString() {
		return "GwtTestSuite for Module: "+getModuleName();
	}

}
