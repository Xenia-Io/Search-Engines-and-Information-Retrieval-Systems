/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, KTH, 2018
 */  

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.*;


/*
 *   Implements an inverted index as a hashtable on disk.
 *   
 *   Both the words (the dictionary) and the data (the postings list) are
 *   stored in RandomAccessFiles that permit fast (almost constant-time)
 *   disk seeks. 
 *
 *   When words are read and indexed, they are first put in an ordinary,
 *   main-memory HashMap. When all words are read, the index is committed
 *   to disk.
 */
public class PersistentHashedIndex implements Index {

    /** The directory where the persistent index files are stored. */
    public static final String INDEXDIR = "\\Users\\Xenia\\Documents\\KTH\\SEMESTER_3\\Search_Engines\\assignment_1\\lab1_code\\index";

    /** The dictionary file name */
    public static final String DICTIONARY_FNAME = "dictionary";

    /** The dictionary file name */
    public static final String DATA_FNAME = "data";

    /** The terms file name */
    public static final String TERMS_FNAME = "terms";

    /** The doc info file name */
    public static final String DOCINFO_FNAME = "docInfo";

    /** The dictionary hash table on disk can fit this many entries. */
    public static final long TABLESIZE = 611953L;

    //public static final long DICTIONARY_RECORDSIZE = (Integer.BYTES + Long.BYTES + Integer.BYTES + (50*Character.BYTES)); 
    public static final long DICTIONARY_RECORDSIZE = (Integer.BYTES + Long.BYTES + (50*Character.BYTES)); 

    public static final long TOTAL_DICTIONARY_BYTES = TABLESIZE*DICTIONARY_RECORDSIZE; 
    

    /** The dictionary hash table is stored in this file. */
    RandomAccessFile dictionaryFile;

    /** The data (the PostingsLists) are stored in this file. */
    RandomAccessFile dataFile;

    /** Pointer to the first free memory cell in the data file. */
    long free = 0L;

    /** The cache as a main-memory hash map. */
    HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    // ===================================================================

    /**
     *   A helper class representing one entry in the dictionary hashtable.
     */ 
    public class Entry {
        long address = 0;
        int size = 0;
        String word = "";

        boolean isAvailable() {
            return address ==0 && size == 0 && word != null && word.length()==0;
        }
    }


    // ==================================================================

    
    /**
     *  Constructor. Opens the dictionary file and the data file.
     *  If these files don't exist, they will be created. 
     */
    public PersistentHashedIndex() {
        try {          
            dictionaryFile = new RandomAccessFile( INDEXDIR + "/" + DICTIONARY_FNAME, "rw" );
            dataFile = new RandomAccessFile( INDEXDIR + "/" + DATA_FNAME, "rw" );

            dictionaryFile.setLength(TOTAL_DICTIONARY_BYTES);
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        try {
            readDocInfo();
        } catch ( FileNotFoundException e ) {
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Writes data to the data file at a specified place.
     *
     *  @return The number of bytes written.
     */ 
    int writeData( String dataString, long ptr ) {
        long ptr_before = 0, ptr_after = 0;
        try {
            ptr_before = ptr;
            dataFile.seek( ptr );             
            byte[] b = dataString.getBytes("utf-8");
            dataFile.write( b );
            ptr_after = dataFile.getFilePointer();            
            return (int) (ptr_after - ptr_before);
        } catch ( IOException e ) {
            System.out.println("#### Error: ptr:" + ptr);
            System.out.println("#### Error: ptr_before:" + ptr_before);
            System.out.println("#### Error: ptr_after :" + ptr_after);
            System.out.println("#### Error: " + dataString);
            e.printStackTrace();
            System.exit(-1);
            return -1;
        }
    }


    /**
     *  Reads data from the data file
     */ 
    String readData( long ptr, int size ) {
        try {
          
            dataFile.seek( ptr );
            byte [] b = new byte[size];
            dataFile.read(b);
            String s = new String(b, "UTF8");
            return s;
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }


    // ==================================================================
    //
    //  Reading and writing to the dictionary file.

    /*
     *  Writes an entry to the dictionary hash table file. 
     *
     *  @param entry The key of this entry is assumed to have a fixed length
     *  @param ptr   The place in the dictionary file to store the entry
     */
    void writeEntry( Entry entry, long ptr ) {
        try{
            
            dictionaryFile.seek(ptr);
            dictionaryFile.writeLong(entry.address);            
            dictionaryFile.writeInt(entry.size);   
            if (entry.word.length() < 50)  {
                dictionaryFile.writeUTF(entry.word);              
            } else {
                dictionaryFile.writeUTF(entry.word.substring(0,50));          
            }
        
        }
        catch(IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        
    }

    /**
     *  Reads an entry from the dictionary file.
     *
     *  @param ptr The place in the dictionary file where to start reading.
     */
    Entry readEntry( long ptr ) {   
        Entry entry = new Entry();
        try{
            // System.out.println(ptr);
            // System.out.println(ptr  + Long.BYTES);
            // System.out.println(ptr + Long.BYTES + Integer.BYTES);
                        
            dictionaryFile.seek(ptr);
            entry.address = dictionaryFile.readLong();
            if (entry.address == 0) {
                return null;
            }
            entry.size = dictionaryFile.readInt();            
            entry.word = dictionaryFile.readUTF();

            // System.out.println("from HDD: " +  entry.word);
        }
        catch(IOException e){
            e.printStackTrace();
            System.exit(-1);
        }

        return entry;
    }


    // ==================================================================

    /**
     *  Writes the document names and document lengths to file.
     *
     * @throws IOException  { exception_description }
     */
    private void writeDocInfo() throws IOException {
        FileOutputStream fout = new FileOutputStream( INDEXDIR + "/docInfo" );
        for (Map.Entry<Integer,String> entry : docNames.entrySet()) {
            Integer key = entry.getKey();
            String docInfoEntry = key + ";" + entry.getValue() + ";" + docLengths.get(key) + "\n";
            fout.write(docInfoEntry.getBytes());
        }
        fout.close();
    }


    /**
     *  Reads the document names and document lengths from file, and
     *  put them in the appropriate data structures.
     *
     * @throws     IOException  { exception_description }
     */
    private void readDocInfo() throws IOException {
        File file = new File( INDEXDIR + "/docInfo" );
        FileReader freader = new FileReader(file);
        try (BufferedReader br = new BufferedReader(freader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                docNames.put(new Integer(data[0]), data[1]);
                docLengths.put(new Integer(data[0]), new Integer(data[2]));
            }
        }
        freader.close();
    }

    private PostingsList convertStringToPList(String data) {
        PostingsList pl = new PostingsList();
        
        // System.out.println("data = " + data);
        String[] postingsEntries_i = data.split("#");        

        for(int i=1; i< postingsEntries_i.length; i++){
            
            String[] splitedEntry = postingsEntries_i[i].split(",");
            PostingsEntry newEntry = new PostingsEntry(Integer.parseInt(splitedEntry[0]));
            newEntry.docID = Integer.parseInt(splitedEntry[0]);
            newEntry.score = Double.parseDouble(splitedEntry[1]);

            String[] offsets = splitedEntry[2].split("-");
            
            for(int k=0; k<offsets.length; k++){
                
                newEntry.addOffset(Integer.valueOf(offsets[k]));

            }
            pl.add(newEntry);

        }

        return pl;
    }

    private String convertPListToString(String word, PostingsList pl){
        // pe: <docid, score, offsets>
        // pl: [ pe, pe, pe, pe ] = [ <docid, score>, <docid, score>, <docid, score> ]

        // pl: zombie:4:docid,score,1:2:3#docid,score,2:3:4#docid,score,5:6:7

        StringBuilder builder = new StringBuilder();

        for (PostingsEntry pe : pl.gPostingsEntries())  { 
            builder.append("#");                       
            builder.append(String.valueOf(pe.docID));
            builder.append(",");
            builder.append(String.valueOf(pe.score));
            builder.append(",");
            int cnt = 0;
            for (Integer offset : pe.offsets) {
                if(cnt >= 1) {
                    builder.append("-");
                }
                cnt++;
                builder.append(String.valueOf(offset));
            }            
        }
     
        String data = builder.toString();
        //System.out.println("data 1 = "+ data);
        return data;
    }

    /**
     *  Write the index to files.
     */
    public void writeIndex() {
        int collisions = 0;
        int colissionsInd = 0;
        long availableMeMLocation = 0;

        try {
            // Write the 'docNames' and 'docLengths' hash maps to a file
            writeDocInfo();

            // Write the dictionary and the postings list


            // System.out.println("Total words to be saved: " + index.size());

            for (Map.Entry<String, PostingsList> mapEntry : index.entrySet()) {
                String myWord = mapEntry.getKey();
                PostingsList pl = mapEntry.getValue();

                String pl_string = convertPListToString(myWord, pl);
                                               
                // DataFile - Write PostingsList to the DataFile                    
                int bytesWritten = writeData(pl_string, availableMeMLocation);
                long address = availableMeMLocation;
                availableMeMLocation = availableMeMLocation + bytesWritten ;
                
                // Dictionary File - Write address to the Dictionary File
                int hashvalue1 = hashFunction(myWord);  
                //int hashvalue2 = hashFunction2(myWord);  
                long ptr = hashvalue1*DICTIONARY_RECORDSIZE;
                
                Entry previousentry = readEntry(ptr);

                boolean collisionOccured = false;

                // System.out.println("Trying to insert word: " + myWord);
                while (true) {
                    
                    if (previousentry == null) {
                        Entry entry = new Entry();
                        entry.size = pl_string.getBytes("utf-8").length;
                        entry.word = myWord;
                        entry.address = address;    
                        // System.out.println("INSERT: word " + myWord + " stored at ptr: " + ptr);
                        writeEntry(entry, ptr);
                        break;
                    } else if (previousentry.word.equals(myWord)) {
                        System.out.println("This should never happen ... :! word: " + myWord );
                        System.exit(-1);
                        //  break;
                        // System.out.println("previousentry.hashvalue2: " + previousentry.hashvalue2);
                        // System.out.println("hashvalue1:" + hashvalue1);
                        // System.out.println("hashvalue2:" + hashvalue2);
                        // System.out.println("ptr:" + ptr);  
                    } else {        
                        if (collisionOccured == false ) {
                            collisions++;
                            // System.out.println("collision occured, collisions = "+ collisions);
                            collisionOccured = true;
                        }
                        if (ptr + DICTIONARY_RECORDSIZE < TOTAL_DICTIONARY_BYTES) {
                            ptr = ptr + DICTIONARY_RECORDSIZE;
                        } else {
                            ptr = 0;
                        }
                        previousentry = readEntry(ptr);
                    }    
                }       
            }
            
           
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        System.err.println( collisions + " collisions." );
    }


    // ==================================================================


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        int hashvalue1 = hashFunction(token);          
        long ptr = hashvalue1 * DICTIONARY_RECORDSIZE;
        
        Entry entry = readEntry(ptr);

        while (true) {
            if (entry == null || entry.isAvailable()) {
                System.out.println("no result");
                return null;
            } else if (entry.word.equals(token)) {
                // System.out.println("Mpika !!!!");
                long address = entry.address;

                String pl_string = readData(address, entry.size);
                // System.out.println("pl_string 11111 = " + pl_string);
                PostingsList pl = convertStringToPList(pl_string);
                return pl;                
            } else {                       
                if (ptr + DICTIONARY_RECORDSIZE < TOTAL_DICTIONARY_BYTES) {
                    ptr = ptr + DICTIONARY_RECORDSIZE;
                } else {
                    ptr = 0;
                }
                entry = readEntry(ptr);
            }    
        }     
    }


    /**
     *  Inserts this token in the main-memory hashtable.
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

    // public int hashFunction(String x)
    // {
    //     char ch[];
    //     ch = x.toCharArray();
    //     int xlength = x.length();

    //     int i, sum;
    //     for (sum=0, i=0; i < x.length(); i++)
    //         sum += ch[i];
    //     return Math.abs((int)(sum % TABLESIZE));
    // }

    public int hashFunction(String s){            
        int intLength = s.length() / 4;
        long sum = 0;
        for (int j = 0; j < intLength; j++) {
          char c[] = s.substring(j * 4, (j * 4) + 4).toCharArray();
          long mult = 1;
          for (int k = 0; k < c.length; k++) {
        sum += c[k] * mult;
        mult *= 256;
          }
        }
   
        char c[] = s.substring(intLength * 4).toCharArray();
        long mult = 1;
        for (int k = 0; k < c.length; k++) {
          sum += c[k] * mult;
          mult *= 256;
        }
   
        return Math.abs((int)(sum % TABLESIZE));
    }

    /**
     *  Write index to file after indexing is done.
     */
    public void cleanup() {
        System.err.println( index.keySet().size() + " unique words" );
        System.err.print( "Writing index to disk..." );
        writeIndex();
        System.err.println( "done!" );
    }
}
