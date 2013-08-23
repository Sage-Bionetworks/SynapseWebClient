package org.sagebionetworks.web.server.markdownparser;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;
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
	Pattern p4 = Pattern.compile(MarkdownRegExConstants.BLOCK_QUOTE_REGEX, Pattern.DOTALL);;
	Stack<MarkdownList> stack;
	boolean hasSeenBlockQuote;
	boolean preserveForBlockQuoteParser;
	
	@Override
	public void reset() {
		stack = new Stack<MarkdownList>();
		hasSeenBlockQuote = false;
		preserveForBlockQuoteParser = false;
	}

	@Override
	public void processLine(MarkdownElements line) {
		Matcher m1 = p1.matcher(line.getMarkdown());
		Matcher m2 = p2.matcher(line.getMarkdown());
		Matcher m3 = p3.matcher(line.getMarkdown());
		Matcher m4 = p4.matcher(line.getMarkdown());
	
		boolean isOrderedList = m1.matches();
		boolean isUnorderedList = m2.matches();

		if (isOrderedList) {
			getListItem(line, m1, true, m4);
		}
		else if (isUnorderedList) {
			getListItem(line, m2, false, m4);
		} 
		else if (isInMarkdownElement()) {
			//this is not a list item
			m3.matches();
			String spaces = m3.group(1);
			int depth = spaces.length();
			String value = m3.group(2);
			
			boolean isInBlockQuote = m4.matches();
			if(isInBlockQuote) {
	        	depth--;						//Account for leading ">" blockquote character
	        	value = m4.group(2) + value; 	//Prepend original prefix again for blockquote parser
	        }
			
			checkForGreaterDepth(line, depth);
	        if (!stack.isEmpty()) {
	        	MarkdownList list = stack.peek();
	        	if(depth > list.getDepth()) {
	        		//this is an element under a list item, add it as a child to the item
	        		list.addExtraElementHtml(line,  value);
	        	} else {
	        		//this is not an element under any list item. End all existing lists
	    	        while(!stack.isEmpty()){
	    	        	list.closeOpenListItems(line);
	    				line.prependElement(stack.pop().getEndListHtml());
	    			}
	    	        hasSeenBlockQuote = false;
	    			preserveForBlockQuoteParser = false;
	        	}	        	
	        }        
		}
	}
	
	public void getListItem(MarkdownElements line, Matcher m, boolean isOrderedList, Matcher blockquoteMatcher) {
		//looks like a list item
		String prefix = m.group(1);
        int depth = prefix.length();
        String value = m.group(3);

        boolean isInBlockQuote = blockquoteMatcher.matches();
        if(isInBlockQuote) {    
        	if(hasSeenBlockQuote) {
        		//We're in the middle of a blockquote, so preserve
        		//the ">" character for blockquote parser
        		value = blockquoteMatcher.group(2) + value;
        		preserveForBlockQuoteParser = false;
        	} else {
        		//The blockquote tag has not been made/this list item is starting the blockquote
        		//We need to encompass the list with the blockquote element 
        		preserveForBlockQuoteParser = true;
        		hasSeenBlockQuote = true;
        	}
        	
        	//Account for leading ">" blockquote character
        	depth--;
        } else if(line.hasElement(ServerMarkdownUtils.START_BLOCKQUOTE_TAG)) {
        	//This list item starts the blockquote/the blockquote has already been made
        	hasSeenBlockQuote = true;
        } 
        //else, create a list item with normal value

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
        	if(preserveForBlockQuoteParser) {
        		//Preserve the ">" character for the blockquote parser to prepend the blockquote element
        		line.updateMarkdown(prefix);
        		line.appendElement(newList.getStartListHtml() + "<li><p>" + value + "</p>");	
        	} else {
        		//Start a normal list
        		line.prependElement(newList.getStartListHtml());
        		newList.addListItemHtml(line, value);
        	}
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
