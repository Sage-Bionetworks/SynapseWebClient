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
	
	Pattern p1= Pattern.compile(MarkdownRegExConstants.ORDERED_LIST_REGEX, Pattern.DOTALL);
	Pattern p2 = Pattern.compile(MarkdownRegExConstants.UNORDERED_LIST_REGEX, Pattern.DOTALL);
	Pattern p3 = Pattern.compile(MarkdownRegExConstants.INDENTED_REGEX, Pattern.DOTALL);
	Stack<MarkdownList> stack;

	@Override
	public void reset() {
		stack = new Stack<MarkdownList>();
	}

	@Override
	public void processLine(MarkdownElements line) {
		Matcher m1 = p1.matcher(line.getMarkdown());
		Matcher m2 = p2.matcher(line.getMarkdown());
		Matcher m3 = p3.matcher(line.getMarkdown());

		boolean isOrderedList = m1.matches();
		boolean isUnorderedList = m2.matches();

		if (isOrderedList) {
			getListItem(line, m1, true);
		}
		else if (isUnorderedList) {
			getListItem(line, m2, false);
		} 
		else if (isInMarkdownElement()) {
			//this is not a list item
			m3.matches();
			String spaces = m3.group(1);
			int depth = spaces.length();
			String value = m3.group(2);
			
			checkForGreaterDepth(line, depth);
	        if (!stack.isEmpty()) {
	        	MarkdownList list = stack.peek();
	        	if(depth != 0) {
	        		//this is an element under a list item, add it as a child to the item
	        		list.addExtraElementHtml(line,  value);
	        	} else {
	        		//this is not an element under any list item. End all existing lists
	    	        while(!stack.isEmpty()){
	    	        	list.closeOpenListItems(line);
	    				line.prependElement(stack.pop().getEndListHtml());
	    			}	    			
	        	}	        	
	        }        
		}
	}
	
	public void getListItem(MarkdownElements line, Matcher m, boolean isOrderedList) {
		//looks like a list item
		//String prefixGroup = m.group(1);
		String spaces = m.group(1);
        int depth = spaces.length();
        //TODO: use listMarker to test order value (if ordered list)
        String listMarker = m.group(2);
        String value = m.group(3);
        
        checkForGreaterDepth(line, depth);
        if (!stack.isEmpty()) {
        	MarkdownList list = stack.peek();
        	//this list is either the parent of the new list, or we should add this item to this list
        	if (list.getDepth() == depth) {
        		//add this to the current list
        		list.addListItemHtml(line,  value);
        	} else {
        		//list depth is less than the current depth
        		//create a new list
        		MarkdownList newList = getNewList(depth, isOrderedList);
        		line.prependElement(newList.getStartListHtml());
        		newList.addListItemHtml(line, value);
        	}
        }
        else {
        	//no list in the stack
        	//create a new list
        	MarkdownList newList = getNewList(depth, isOrderedList);
    		line.prependElement(newList.getStartListHtml());
    		newList.addListItemHtml(line, value);
        }
	}
	
	public void checkForGreaterDepth(MarkdownElements line, int depth) {
        //end any list that is of greater depth on the stack
        boolean isGreaterDepth = true;
        while (isGreaterDepth && !stack.isEmpty()) {
        	MarkdownList list = stack.peek();
        	if (list.getDepth() > depth) {
        		stack.pop();
        		list.closeOpenListItems(line);
        		line.prependElement(list.getEndListHtml());
        	} else {
        		isGreaterDepth = false;
        	}
        }
	}
	
	/**
	 * Create a new list of the correct type and push it onto the stack.  return the correct html to start 
	 * the new list and include the item.
	 * @return
	 */
	public MarkdownList getNewList(int depth, boolean isOrderedList) {
		MarkdownList newList = isOrderedList ? new OrderedMarkdownList(depth) : new UnorderedMarkdownList(depth);
		stack.push(newList);
		return newList;
	}
	
	@Override
	public boolean isInMarkdownElement() {
		return !stack.isEmpty();
	}
	
	@Override
	public boolean isBlockElement() {
		return true;
	}
	
	@Override
	public boolean isInputSingleLine() {
		return false;
	}
}
