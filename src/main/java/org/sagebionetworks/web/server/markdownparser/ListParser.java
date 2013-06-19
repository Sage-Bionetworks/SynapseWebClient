package org.sagebionetworks.web.server.markdownparser;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * One of the more complicated parsers.  Needs to have a stack to support nested lists.
 * needs to remember the level (how deep is the nested list), the list type (ordered or unordered), and (if ordered) the current number)
 * @author jayhodgson
 *
 */
public class ListParser extends BasicMarkdownElementParser  {
	
	Pattern p1, p2;
	public static final String ORDERED_LIST_REGEX = "^(\\s*)((?:\\d+[.]))(.+)";
	public static final String UNORDERED_LIST_REGEX = "^(\\s*)((?:[-+*]))(.+)";
	Stack<MarkdownList> stack;
	
	@Override
	public void init() {
		p1 = Pattern.compile(ORDERED_LIST_REGEX, Pattern.DOTALL);
		p2 = Pattern.compile(UNORDERED_LIST_REGEX, Pattern.DOTALL);
	}

	@Override
	public void reset() {
		stack = new Stack<MarkdownList>();
	}

	@Override
	public String processLine(String line) {
		Matcher m1 = p1.matcher(line);
		Matcher m2 = p2.matcher(line);
		
		boolean isOrderedList = m1.matches();
		boolean isUnorderedList = m2.matches();
		
		if (isOrderedList) {
			return getListItem(line, m1, true);
		}
		else if (isUnorderedList) {
			return getListItem(line, m2, false);
		} else if (isInMarkdownElement()) {
			//this is not a list item line.  End all existing lists
			StringBuilder sb = new StringBuilder();
			while(!stack.isEmpty()){
				sb.append(stack.pop().getEndListHtml());
			}
			sb.append(line);
			return sb.toString();
		}
		
		return line;
	}
	
	public String getListItem(String line, Matcher m, boolean isOrderedList) {
		StringBuilder returnString = new StringBuilder();
		//looks like a list item
		String spaces = m.group(1);
        int depth = spaces.length();
        //TODO: use listMarker to test order value (if ordered list)
        String listMarker = m.group(2);
        String value = m.group(3);
        
        //end any list that is of greater depth on the stack
        boolean isGreaterDepth = true;
        while (isGreaterDepth && !stack.isEmpty()) {
        	MarkdownList list = stack.peek();
        	if (list.getDepth() > depth) {
        		stack.pop();
        		returnString.append(list.getEndListHtml());
        	} else {
        		isGreaterDepth = false;
        	}
        }
        
        if (!stack.isEmpty()) {
        	MarkdownList list = stack.peek();
        	//this list is either the parent of the new list, or we should add this item to this list
        	if (list.getDepth() == depth) {
        		//add this to the current list
        		returnString.append(list.getListItemHtml(value));
        	} else {
        		//list depth is less than the current depth
        		//create a new list
        		returnString.append(createNewList(depth, isOrderedList, value));
        	}
        }
        else {
        	//no list in the stack
        	//create a new list
        	returnString.append(createNewList(depth, isOrderedList, value));
        }
        return returnString.toString();
	}
	
	/**
	 * Create a new list of the correct type and push it onto the stack.  return the correct html to start 
	 * the new list and include the item.
	 * @return
	 */
	public String createNewList(int depth, boolean isOrderedList, String value) {
		MarkdownList newList = isOrderedList ? new OrderedMarkdownList(depth) : new UnorderedMarkdownList(depth);
		stack.push(newList);
		return newList.getStartListHtml() + newList.getListItemHtml(value);
	}
	
	@Override
	public boolean isInMarkdownElement() {
		return !stack.isEmpty();
	}
	
	@Override
	public boolean isBlockElement() {
		return true;
	}
}
