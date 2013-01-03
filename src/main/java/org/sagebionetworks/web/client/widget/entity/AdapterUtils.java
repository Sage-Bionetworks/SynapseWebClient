package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.schema.FORMAT;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.TYPE;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;


/**
 * Utility for getting data in and out of adapters.
 * 
 * @author jmhill
 *
 */
public class AdapterUtils {

	/**
	 * Get the value from the adapter.
	 * @param <T>
	 * @param adapter
	 * @param schemaType
	 * @param key
	 * @param clazz
	 * @return
	 */
	public static <T> T getValue(JSONObjectAdapter adapter, TYPE schemaType, String key, Class<? extends T> clazz){
		if(schemaType == null) throw new IllegalArgumentException("Schema type cannot be null");
		if(adapter == null) throw new IllegalArgumentException("Adapter cannot be null");
		if(key == null) throw new IllegalArgumentException("Key cannot be null");
		if(clazz == null) throw new IllegalArgumentException("Clazz cannot be null");
		try{
			// Check for missing
			if(!adapter.has(key)) return null;
			// Check nulls
			if(adapter.isNull(key)) return null;
			if(Double.class == clazz){
				return (T) new Double(adapter.getDouble(key));
			}else if(String.class == clazz){
				return (T) adapter.getString(key);
			}else if(Long.class == clazz){
				return (T) new Long(adapter.getLong(key));
			}else if(Date.class == clazz){
				// Dates can be strings or longs
				if(TYPE.INTEGER == schemaType){
					return (T) new Date(adapter.getLong(key));
				}else if(TYPE.STRING == schemaType){
					return (T) adapter.convertStringToDate(FORMAT.DATE_TIME, adapter.getString(key));
				}else{
					throw new IllegalArgumentException("Unknown type: "+schemaType+" for a date");
				}
			}else {
				throw new IllegalArgumentException("Unknown type: "+schemaType);
			}
		}catch (JSONObjectAdapterException e){
			// Convert it to a runtime
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Put the value in the adapter.
	 * @param adapter
	 * @param schemaType
	 * @param key
	 * @param value
	 */
	public static void setValue(JSONObjectAdapter adapter, TYPE schemaType, String key, Object value){
		if(schemaType == null) throw new IllegalArgumentException("Schema type cannot be null");
		if(adapter == null) throw new IllegalArgumentException("Adapter cannot be null");
		if(key == null) throw new IllegalArgumentException("Key cannot be null");
		try{
			// The value can be null
			if(value == null){
				adapter.putNull(key);
			}else{
				if(value instanceof String){
					adapter.put(key,(String)value);
				}else if(value instanceof Long){
					adapter.put(key,(Long)value);
				}else if(value instanceof Double){
					adapter.put(key,(Double)value);
				}else if(value instanceof Date){
					// Dates can be stored as longs or strings
					Date dateValue = (Date) value;
					if(TYPE.INTEGER == schemaType){
						adapter.put(key, dateValue.getTime());
					}else if(TYPE.STRING == schemaType){
						adapter.put(key, adapter.convertDateToString(FORMAT.DATE_TIME, dateValue));
					}else{
						throw new IllegalArgumentException("Unknown type: "+schemaType+" for a date");
					}
				}else{
					throw new IllegalArgumentException("Unknown class: "+value.getClass().getName());
				}
			}
		}catch (JSONObjectAdapterException e){
			// Convert it to a runtime
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Read a List<T> from JSONArrayAdapter
	 * @param <T>
	 * @param array
	 * @param clazz
	 * @return
	 * @throws JSONObjectAdapterException
	 */
	public static <T> List<T> getListValue(JSONObjectAdapter adapter, TYPE schemaType, String key, Class<? extends T> clazz) {
		if(schemaType == null) throw new IllegalArgumentException("Schema type cannot be null");
		if(adapter == null) throw new IllegalArgumentException("Adapter cannot be null");
		if(key == null) throw new IllegalArgumentException("Key cannot be null");
		if(clazz == null) throw new IllegalArgumentException("Clazz cannot be null");
		if(!adapter.has(key)) return null;
		if(adapter.isNull(key)) return null;
		try{
			JSONArrayAdapter array = adapter.getJSONArray(key);
			List<T> results = new ArrayList<T>();
			for(int i=0; i<array.length(); i++){
				if(array.isNull(i)){
					results.add(null);
				}else if(String.class == clazz){
					String string = array.getString(i);
					results.add((T)string);
				}else if(Double.class == clazz){
					Double value = array.getDouble(i);
					results.add((T)value);
				}else if(Long.class == clazz){
					Long value = array.getLong(i);
					results.add((T)value);
				}else if(Date.class == clazz){
					// Dates can be strings or longs
					if(TYPE.INTEGER == schemaType){
						Date value = new Date(array.getLong(i));
						results.add((T)value);
					}else if(TYPE.STRING == schemaType){
						Date value = array.convertStringToDate(FORMAT.DATE_TIME, array.getString(i));
						results.add((T)value); 
					}else{
						throw new IllegalArgumentException("Unknown type: "+schemaType+" for a date");
					}
				}else if(byte[].class == clazz){
					byte[] value = array.getBinary(i);
					results.add((T)value);
				}else{
					throw new IllegalArgumentException("Unknown type: "+clazz);
				}
			}
			return results;
		}catch (JSONObjectAdapterException e){
			// Convert it to a runtime
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Write a list JSONArrayAdapter
	 * @param <T>
	 * @param newArray
	 * @param list
	 * @param clazz
	 * @throws JSONObjectAdapterException
	 */
	public static <T> void setListValue(JSONObjectAdapter adapter, TYPE schemaType, String key, List<T> list, Class<? extends T> clazz) {
		if(schemaType == null) throw new IllegalArgumentException("Schema type cannot be null");
		if(adapter == null) throw new IllegalArgumentException("Adapter cannot be null");
		if(key == null) throw new IllegalArgumentException("Key cannot be null");
		try{
			if(list == null){
				adapter.putNull(key);
				return;
			}
			// We will need a new array to put the data in
			JSONArrayAdapter newArray = adapter.createNewArray();
			if(list != null){
				for(int i=0; i<list.size(); i++){
					T value = list.get(i);
					if(value == null){
						newArray.putNull(i);
					}else if(clazz == String.class){
						newArray.put(i, (String)list.get(i));
					}else if(Double.class == clazz){
						newArray.put(i, (Double)list.get(i));
					}else if(Long.class == clazz){
						newArray.put(i, (Long)list.get(i));
					}else if(Date.class == clazz){
						// Dates can be stored as longs or strings
						Date dateValue = (Date) value;
						if(TYPE.INTEGER == schemaType){
							newArray.put(i, dateValue.getTime());
						}else if(TYPE.STRING == schemaType){
							newArray.put(i, newArray.convertDateToString(FORMAT.DATE_TIME, dateValue));
						}else{
							throw new IllegalArgumentException("Unknown type: "+schemaType+" for a date");
						}
					}else if(byte[].class == clazz){
						byte[] byteArray = (byte[]) list.get(i);
						newArray.put(i, byteArray);
					}else{
						throw new IllegalArgumentException("Unknown type: "+clazz);
					}
				}		
			}
			// Add the array to the addapter
			adapter.put(key, newArray);
		}catch (JSONObjectAdapterException e){
			// Convert it to a runtime
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * What is the object type for a given property schema?
	 * 
	 * @param schema
	 * @return
	 */
	public static Class getClassTypeForSchema(ObjectSchema schema){
		if(schema == null) throw new IllegalArgumentException("Schema cannot be null");
		if(schema.getType() == null) throw new IllegalArgumentException("Schema type cannot be null");
		if(TYPE.NUMBER == schema.getType()){
			// Numbers are always doubles
			return Double.class;
		}if(TYPE.INTEGER == schema.getType()){
			// Integers can be dates or integers
			if(schema.getFormat() == null){
				return Long.class;
			}else if(FORMAT.UTC_MILLISEC == schema.getFormat()){
				// this is a date
				return Date.class;
			}else{
				throw new IllegalArgumentException("Unknown format: "+schema.getFormat()+" for type "+schema.getType());
			}
		}if(TYPE.STRING == schema.getType()){
			// Strings can be strings or dates
			if(schema.getFormat() == null){
				return String.class;
			}else if(FORMAT.DATE_TIME == schema.getFormat()){
				// this is a date
				return Date.class;
			}else{
				throw new IllegalArgumentException("Unknown format: "+schema.getFormat()+" for type "+schema.getType());
			}
		}if(TYPE.ARRAY == schema.getType()){
			// What is the base type of the array?
			return getClassTypeForSchema(schema.getItems());
		}else{
			throw new IllegalArgumentException("Unknown type: "+schema.getType());
		}
	}
}
