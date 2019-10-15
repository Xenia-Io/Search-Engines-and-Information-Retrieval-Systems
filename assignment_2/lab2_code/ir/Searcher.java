/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.io.*;
import pagerank.*;
import java.util.*;
import java.lang.Math;
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
    
    HashMap<String, Double> pgMap = new HashMap<String, Double>();

    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;

        try{
            File file = new File("C:\\Users\\Xenia\\Documents\\KTH\\SEMESTER_3\\Search_Engines\\assignment_2\\lab2_code\\pagerank\\pageranks.txt"); 

            BufferedReader br = new BufferedReader(new FileReader(file)); 
            String st; 
            while ((st = br.readLine()) != null) {
                String docname = st.split("\t")[0];

                pgMap.put(docname, Double.parseDouble(st.split("\t")[1]));
            }

        }catch (IOException e) {
            e.printStackTrace ();
        }
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

    
    private PostingsList sortPLWithScore(PostingsList pl, int N){

        int tf = 0; //number of occurrences of t in d       
        int df_t = 0; //number of documents in the corpus which contain t
        int lengthOfDocd = 0; //number of words in d
        double tf_idf = 0.0;
        double idf = 0.0;

        // for each PE in my PL, calculate the score and find tf_idf for each PE
        for(int i=0; i<pl.size(); i++){
            tf = pl.get(i).offsets.size();
            df_t = pl.size();
            lengthOfDocd = index.docLengths.get(pl.get(i).docID);
            idf = java.lang.Math.log((double)N/(double)df_t);

            tf_idf = (tf * idf)/lengthOfDocd;
            pl.get(i).score = tf_idf;
            // System.out.println("i = "+ i +" , tf_idf = "+ tf_idf);
            
        }

        // sort my PL according to score
        Collections.sort(pl.gPostingsEntries());
        return pl;

    }

    
    

    private PostingsList findUnion(PostingsList pl1, PostingsList pl2){
        HashMap<Integer,PostingsEntry> map1 = pl1.getMap();
        HashMap<Integer,PostingsEntry> map2 = pl2.getMap();
        HashMap<Integer,PostingsEntry> mergedMap = new HashMap<Integer,PostingsEntry>();   

        mergedMap.putAll(map1);
        
        for(Map.Entry<Integer, PostingsEntry> entry : map2.entrySet()){

            if(mergedMap.containsKey(entry.getKey()) == false){
                
                mergedMap.put(entry.getKey(), entry.getValue());
            }
            else if(mergedMap.containsKey(entry.getKey()) == true){
                
                mergedMap.get(entry.getKey()).score += map2.get(entry.getKey()).score;

                // entry.getValue().score;
                // for(int i =0; i<entry.getValue().offsets.size(); i++){
                //     mergedMap.get(entry.getKey()).offsets.add(entry.getValue().offsets.get(i));
                // }      
            }
        }
        
        PostingsList pl = new PostingsList();
        for(Map.Entry<Integer, PostingsEntry> entry : mergedMap.entrySet()){

            pl.add(entry.getValue());
        }
            
        return pl;
    }

    class PageRankDocument implements Comparable<PageRankDocument>{
        double pageRank;
        // int index;
        String docNumber;

        PageRankDocument(String docNumber, double pageRank) {
            this.docNumber = docNumber;
            this.pageRank = pageRank;    
        }

        public int compareTo(PageRankDocument pg){
            
            if (this.pageRank == pg.pageRank) 
                return 0;
            
            if (this.pageRank < pg.pageRank)
                return 1;
            
            return -1;
        }

        public String toString(){
            return String.format("%5s %f", docNumber, pageRank);
        }

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
        // 2.1
        else if(query.queryterm.size() == 1 && (queryType == QueryType.RANKED_QUERY) && (rankingType == RankingType.TF_IDF))
        {
            String token = query.queryterm.get(0).term;

            PostingsList pl = index.getPostings(token);
            int N = index.docLengths.size();   
            
            pl = sortPLWithScore(pl, N);
            double idf = java.lang.Math.log((double)N/(double)pl.size());
            // System.out.println("idf  ==  " + idf );
            return pl;
        }
        // 2.2
        else if(query.queryterm.size() > 1 && (queryType == QueryType.RANKED_QUERY) && (rankingType == RankingType.TF_IDF))
        {
            int N = Index.docLengths.size();
            ArrayList<String> terms = new ArrayList<String>();         

            for (int i=0;i<query.queryterm.size();i++) {
                terms.add(query.queryterm.get(i).term);
            }
        
            PostingsList pResult = new PostingsList();

            for (int counter=0;counter<query.queryterm.size();counter++) {
                PostingsList wordPostingList = sortPLWithScore(index.getPostings(terms.get(counter)),N);

                for (PostingsEntry pe : wordPostingList.gPostingsEntries()) {
                    PostingsEntry existingEntry = pResult.search(pe.docID);
                    if (existingEntry == null) {
                        pResult.add(new PostingsEntry(pe.docID, pe.score));
                    } else {
                        existingEntry.score += pe.score;
                    }
                }
            }

            Collections.sort(pResult.gPostingsEntries());
            return pResult;
        }
         // 2.6
         else if(query.queryterm.size() >= 1 && (queryType == QueryType.RANKED_QUERY) && (rankingType == RankingType.PAGERANK))
         {
             System.out.println("running page rank algorithm");

            int N = Index.docLengths.size();
            ArrayList<String> terms = new ArrayList<String>();    

            for (int i=0;i<query.queryterm.size();i++) {
                terms.add(query.queryterm.get(i).term);
            }
           
            PostingsList pResult = new PostingsList();

            for (int counter=0;counter<query.queryterm.size();counter++) {
                PostingsList wordPostingList = index.getPostings(terms.get(counter));

                for (PostingsEntry pe : wordPostingList.gPostingsEntries()) {
                    PostingsEntry existingEntry = pResult.search(pe.docID);
                    String s = index.docNames.get(pe.docID);
        
                    if (existingEntry == null) {
                        
                        pResult.add(new PostingsEntry( pe.docID,pgMap.get(s.substring(s.lastIndexOf(File.separator) + 1, s.length()))) );
                    } else {
                        
                        existingEntry.score = pgMap.get(s.substring(s.lastIndexOf(File.separator) + 1, s.length()));

                    }
                }
            }

            Collections.sort(pResult.gPostingsEntries());
            return pResult;

             // System.out.println("4945 doc id =========== " + (pResult.get(0).docID));

            // System.out.println("4945 doc name =========== " + index.docNames.get(pResult.get(0).docID));
            // String s =index.docNames.get(pResult.get(0).docID);
            // System.out.println("4945 doc name =========== " + (  s.substring(s.lastIndexOf(File.separator) + 1, s.length())   ) );

            // System.out.println("4945 doc name =========== " + pgMap.get(  s.substring(s.lastIndexOf(File.separator) + 1, s.length())   ) );


         }

         else if(query.queryterm.size() >= 1 && (queryType == QueryType.RANKED_QUERY) && (rankingType == RankingType.COMBINATION))
         {
            System.out.println("Running RankingType.COMBINATION algorithm");
            int N = Index.docLengths.size();
            ArrayList<String> terms = new ArrayList<String>();        

            for (int i=0;i<query.queryterm.size();i++) {
                terms.add(query.queryterm.get(i).term);
            }
        
            PostingsList pResult = new PostingsList();

            for (int counter=0;counter<query.queryterm.size();counter++) {
                PostingsList wordPostingList = sortPLWithScore(index.getPostings(terms.get(counter)),N);

                for (PostingsEntry pe : wordPostingList.gPostingsEntries()) {
                    String s = index.docNames.get(pe.docID);
                    PostingsEntry existingEntry = pResult.search(pe.docID);
                    if (existingEntry == null) {
                        pResult.add(new PostingsEntry(pe.docID, 0.8*pgMap.get(s.substring(s.lastIndexOf(File.separator) + 1, s.length())) + 0.2*pe.score));

                    } else {

                        existingEntry.score += 0.2*pe.score+ 0.8*pgMap.get(s.substring(s.lastIndexOf(File.separator) + 1, s.length()));
                    }
                }
            }

            Collections.sort(pResult.gPostingsEntries());
            return pResult;

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