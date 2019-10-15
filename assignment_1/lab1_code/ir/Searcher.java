/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import javax.lang.model.util.ElementScanner6;


/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;
    
    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;
    }


    private PostingsList positionalIntersect(PostingsList pl1,PostingsList pl2) {
       
        PostingsList answer = new PostingsList();         
        ListIterator<PostingsEntry> itr1 = pl1.gPostingsEntries().listIterator();
        ListIterator<PostingsEntry> itr2 = pl2.gPostingsEntries().listIterator();
        PostingsEntry itr1Value = null;
        PostingsEntry itr2Value = null;
        
        if (itr1.hasNext()) {
            itr1Value = itr1.next();
        } else {
            return answer;
        }

        if (itr2.hasNext()) {
            itr2Value = itr2.next();
        } else {
            return answer;
        }

        while (true) {
            if(itr1Value.docID == itr2Value.docID)
            {
                ArrayList<Integer> pp1 = itr1Value.offsets;
                ArrayList<Integer> pp2 = itr2Value.offsets;
                ListIterator<Integer> itrOffset1 = pp1.listIterator();
                ListIterator<Integer> itrOffset2 = pp2.listIterator();
                Integer itrOffset1Value = null;
                Integer itrOffset2Value = null;
                            
                if(itrOffset1.hasNext()) {
                    itrOffset1Value = itrOffset1.next();
                } else {
                    break;
                }
                if(itrOffset2.hasNext()) {
                    itrOffset2Value = itrOffset2.next();          
                } else {
                    break;
                }

                while (true) {
                    if((itrOffset2Value - itrOffset1Value) == 1) {
                        //offsetList.add(itrOffset2Value);
                        PostingsEntry temp = answer.search(itr1Value.docID);
                        if (temp  == null) {
                            // PostingsEntry pe = new PostingsEntry(itr1Value.docID);
                            // answer.add(pe);

                            answer.addEntryWithOffset(itr1Value.docID, itrOffset2Value);
                        } else {
                            temp.offsets.add(itrOffset2Value);
                        }

                        if (itrOffset1.hasNext()) {
                            itrOffset1Value = itrOffset1.next();
                        } else {
                            break;
                        }

                        if (itrOffset2.hasNext()) {
                            itrOffset2Value = itrOffset2.next();
                        } else {
                            break;
                        }
                        
                    } else if(itrOffset2Value > (itrOffset1Value) ) {
                        if (itrOffset1.hasNext()) {
                            itrOffset1Value = itrOffset1.next();
                        } else {
                            break;
                        }                        
                    }else  {
                        if (itrOffset2.hasNext()) {
                            itrOffset2Value = itrOffset2.next();
                        } else {
                            break;
                        }
                    }
                }
                
                if(itr1.hasNext()) {
                    itr1Value = itr1.next();
                } else {
                    break;
                }  

                if(itr2.hasNext())  {
                    itr2Value = itr2.next();
                } else {
                    break;
                }
            } else if(itr1Value.docID < itr2Value.docID) {
                if(itr1.hasNext()) {
                    itr1Value = itr1.next();
                } else {
                    break;
                }                
            } else {
                if(itr2.hasNext())  {
                    itr2Value = itr2.next();
                } else {
                    break;
                }
            }
         }
    
        return answer;
      }

   
    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
   
    private PostingsList intersect(PostingsList pl1,PostingsList pl2) {

        PostingsList answer = new PostingsList();      
        ListIterator<PostingsEntry> itr1 = pl1.gPostingsEntries().listIterator();
        ListIterator<PostingsEntry> itr2 = pl2.gPostingsEntries().listIterator();
        PostingsEntry itr1Value = null;
        PostingsEntry itr2Value = null;

        if (itr1.hasNext()) {
            itr1Value = itr1.next();
        } else {
            return answer;
        }

        if (itr2.hasNext()) {
            itr2Value = itr2.next();
        } else {
            return answer;
        }

        while (true) 
        {            
            if(itr1Value.docID == itr2Value.docID)
            {                
                PostingsEntry pe = new PostingsEntry(itr1Value.docID);
                answer.add(pe);
                
                if(itr1.hasNext()) {
                    itr1Value = itr1.next();
                } else {
                    break;
                }

                if(itr2.hasNext())  {
                    itr2Value = itr2.next();
                } else {
                    break;
                }
            } else if(itr1Value.docID < itr2Value.docID) {
                if(itr1.hasNext()) {
                    itr1Value = itr1.next();
                } else {
                    break;
                }                
            } else {
                if(itr2.hasNext())  {
                    itr2Value = itr2.next();
                } else {
                    break;
                }
            }
         }
         return answer;
    }


    private PostingsList intersect2(PostingsList pl1,PostingsList pl2) {
       
        PostingsList answer = new PostingsList();      
        ArrayList<PostingsEntry> l1 = pl1.gPostingsEntries();
        ArrayList<PostingsEntry> l2 = pl2.gPostingsEntries();

        System.out.println("pl1 size "+ l1.size());   
        System.out.println("pl2 size "+ l2.size());   
        

        int i = 0 ;
        int j = 0;

        while (i < l1.size() && j < l2.size()) {
            if (l1.get(i).docID == l2.get(j).docID) {
                PostingsEntry pe = new PostingsEntry(l1.get(i).docID);

                answer.add(pe);

                i++;
                j++;
            } else if (l1.get(i).docID < l2.get(j).docID) {
                i++;
            } else {
                j++;
            }
        }

        return answer;
      }

    public PostingsList search( Query query, QueryType queryType, RankingType rankingType ) { 
        // 1.3
        if(query.queryterm.size() > 1 && (queryType == QueryType.INTERSECTION_QUERY))
        {
            String[] tokens = new String[query.queryterm.size()];
            PostingsList[] pl_array = new PostingsList[query.queryterm.size()];

            for(int i=0; i<query.queryterm.size(); i++)
            {
                pl_array[i] = new PostingsList();
               
                tokens[i] = query.queryterm.get(i).term;
                pl_array[i] = index.getPostings(tokens[i]);
                
            }

            PostingsList pl = new PostingsList();
            pl = pl_array[0];
            for(int i=1; i<pl_array.length; i++)
            {   
                pl = intersect(pl, pl_array[i]);
            }
            
            return pl;
        }
        // 1.4
        else if(query.queryterm.size() > 1 && (queryType == QueryType.PHRASE_QUERY))
        {
            String[] tokens = new String[query.queryterm.size()];
            PostingsList[] pl_array = new PostingsList[query.queryterm.size()];

            for(int i=0; i<query.queryterm.size(); i++)
            {               
                tokens[i] = query.queryterm.get(i).term;
                pl_array[i] = index.getPostings(tokens[i]);                
            }

            PostingsList pl = pl_array[0];
            for(int i=1; i<pl_array.length; i++)
            {   
                pl = positionalIntersect(pl, pl_array[i]);

            }
            
            return pl;

        }

        // 1.2
        else
        {
            System.out.println("Searching for: " + query.queryterm.get(0).term);
            String token = query.queryterm.get(0).term;

            PostingsList pl = index.getPostings(token);
            
            return pl;
        }
     
    }
}