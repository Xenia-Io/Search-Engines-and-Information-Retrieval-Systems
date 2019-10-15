import java.util.*;

import sun.tools.jar.resources.jar;

import java.io.*;

public class PageRank {

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

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory;
     */
    final static int MAX_NUMBER_OF_DOCS = 1000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   The transition matrix. p[i][j] = the probability that the
     *   random surfer clicks from page i to page j.
     */
    double[][] p = new double[MAX_NUMBER_OF_DOCS][MAX_NUMBER_OF_DOCS];

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   In the initializaton phase, we use a negative number to represent 
     *   that there is a direct link from a document to another.
     */
    final static double LINK = -1.0;
    
    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    
    /* --------------------------------------------- */

    
    public PageRank( String filename ) {
        System.out.println("Initializing pagerank with filename: " + filename);
	    int noOfDocs = readDocs( filename );
	    iterate( noOfDocs, 100);
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and fills the data structures. When this method 
     *   finishes executing, <code>p[i][j] = LINK</code> if there is a direct
     *   link from i to j, and <code>p[i][j] = 0</code> otherwise.
     *   <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
            int index = line.indexOf( ";" );
            String title = line.substring( 0, index );
            Integer fromdoc = docNumber.get( title );
            
            //  Have we seen this document before?
            if ( fromdoc == null ) {	
                // This is a previously unseen doc, so add it to the table.
                fromdoc = fileIndex++;
                docNumber.put( title, fromdoc );
                docName[fromdoc] = title;
            }
            
            // Check all outlinks.
            StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
            while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
                String otherTitle = tok.nextToken();
                Integer otherDoc = docNumber.get( otherTitle );
                if ( otherDoc == null ) {
                // This is a previousy unseen doc, so add it to the table.
                otherDoc = fileIndex++;
                docNumber.put( otherTitle, otherDoc );
                docName[otherDoc] = otherTitle;
                }
                // Set the probability to LINK for now, to indicate that there is
                // a link from d to otherDoc.
                if ( p[fromdoc][otherDoc] >= 0 ) {
                    p[fromdoc][otherDoc] = LINK;
                    out[fromdoc]++;
                }
                
            }
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }

    /* --------------------------------------------- */


    /*
     *   Initiates the probability matrix. 
     */

    /* --------------------------------------------- */

    double [] multiply(double [] x, double n ) {
        double [] next_x = new double[x.length];

        for (int i=0;i<x.length;i++) {
            next_x[i] = 0;

            for (int j=0;j<x.length;j++) {
                double p_res = 0;
                if(out[j] == 0){
                    p_res = 1.0/n;
                } 
                // when the probability that the random surfer clicks from page i to page j is 0
                else if(p[j][i] == 0) {
                    p_res = BORED/n;
                } 
                else {
                    p_res = BORED/n;
                    p_res += (1.0-BORED)/(double)out[j];
                } 
                
                next_x[i] += x[j]*p_res;
            }
        }

        return next_x;
    }

    
    boolean checkConvergence(double [] x, double [] next_x) {
        for (int i=0;i<x.length;i++) {
            if (Math.abs(x[i] - next_x[i]) > EPSILON) {
                return false;
            }
        }
        return true;
    }


    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     */
    void iterate( int numberOfDocs, int maxIterations ) {
        
        int loops  =0;
        double [] x = new double[numberOfDocs];
        double [] next_x = new double[numberOfDocs];

        for (int i=0; i<numberOfDocs; i++) {
            
            x[i] = 1.0/numberOfDocs;
        }
        
        while (loops <= maxIterations){
            
            loops++;
            next_x = multiply(x, (double) numberOfDocs);            

            if (checkConvergence(x, next_x) == true) {
                break;
            } else {
                x = next_x;
            }
            
        }

        System.out.println("Iterations: " + loops);

        // sort, but preserve indices
        ArrayList<PageRankDocument> res = new ArrayList<PageRankDocument>();
        // System.out.println("docName[i] ========= " + docName[1]);
        for (int i=0;i<numberOfDocs;i++) {
            res.add(new PageRankDocument(docName[i], next_x[i]));
        }
        Collections.sort(res);
        // System.out.println("docName[i] ========= " + docName[901]);

        for(int i = 0; i < 30; i++){
            System.out.printf("%s\n", res.get(i));
        }

    }


    /* --------------------------------------------- */


    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}