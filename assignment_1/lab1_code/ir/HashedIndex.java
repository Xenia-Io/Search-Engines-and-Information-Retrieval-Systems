/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.*;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {


    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
        
        if (!index.containsKey(token)) {                        
            PostingsEntry entry = new PostingsEntry(docID);
            entry.addOffset(offset);

            PostingsList postingList = new PostingsList();
            postingList.add(entry);

            index.put(token, postingList);
        } else {
            PostingsList postingList = index.get(token);

            PostingsEntry entry = postingList.search(docID);

            if (entry == null) {
                PostingsEntry newentry = new PostingsEntry(docID);
                newentry.addOffset(offset);
                postingList.add(newentry);

            } else {                               
                
                entry.addOffset(offset);
            } 
        }
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        //
        // REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        if (!index.containsKey(token)) { 
            return null;
        } else {
            PostingsList postingList = index.get(token);
            
            return postingList;
        }
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
        
    }


    

}
