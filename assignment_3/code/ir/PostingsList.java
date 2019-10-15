/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PostingsList {
    
    /** The postings list */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();
    private HashMap<Integer,PostingsEntry> map = new HashMap<Integer,PostingsEntry>();

    // public PostingsList(int length)
    // {
    //     this.list = new ArrayList<PostingsEntry>(length);
    //     this.map = new HashMap<Integer,PostingsEntry>(length);
    // }


    public ArrayList<PostingsEntry> gPostingsEntries()
    {
        return list;
    }

    public HashMap<Integer,PostingsEntry> getMap()
    {
        return map;
    }

    /** Number of postings in this list. */
    public int size() {
        return list.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
        if(i <= list.size())
            return list.get( i );
        return null;
    }
   
    // 
    //  YOUR CODE HERE
    //

    public void addEntryWithOffset(Integer docid, Integer offset) {
        
        PostingsEntry pe = new PostingsEntry(docid);
        pe.addOffset(offset);
        list.add(pe);
        map.put(docid, pe); 
    }

    public void add(PostingsEntry e) {
        list.add(e);
        map.put(e.docID, e);
    }

    public PostingsEntry search(int docID) {
        
        if(map.containsKey(docID))
            return map.get(docID);
        else
            return null;
    }

    public void addOrdered(PostingsEntry pe) {
        if (list.isEmpty()) {
            list.add(pe);
        } else {
            PostingsEntry first = list.get(0);
            PostingsEntry last = list.get(list.size() - 1);

            if (pe.docID <= first.docID) {
                list.add(0, pe);
                return;
            }
                
            if (pe.docID >= last.docID) {
                list.add(pe);
                return;
            }

            for (int i=0;i<list.size();i++) {
                PostingsEntry current = list.get(i);
                if (pe.docID < current.docID) {
                    list.add(i,pe);
                    return;
                }
            }
        }
    }
    
}

