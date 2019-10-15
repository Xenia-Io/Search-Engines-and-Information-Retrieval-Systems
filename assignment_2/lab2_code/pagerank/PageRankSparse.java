import java.util.*;
import java.io.*;

public class PageRankSparse {

    /**
     * Maximal number of documents. We're assuming here that we don't have more docs
     * than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     * Mapping from document names to document numbers.
     */
    HashMap<String, Integer> docNumber = new HashMap<String, Integer>();

    /**
     * Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**
     * A memory-efficient representation of the transition matrix. The outlinks are
     * represented as a HashMap, whose keys are the numbers of the documents linked
     * from.
     * <p>
     *
     * The value corresponding to key i is a HashMap whose keys are all the numbers
     * of documents j that i links to.
     * <p>
     *
     * If there are no outlinks from i, then the value corresponding key i is null.
     */
    HashMap<Integer, HashMap<Integer, Boolean>> link = new HashMap<Integer, HashMap<Integer, Boolean>>();

    /**
     * The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     * The probability that the surfer will be bored, stop following links, and take
     * a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     * Convergence criterion: Transition probabilities do not change more that
     * EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    final int noOfDocs;
    int noOfDocuments = 0;

    HashMap<String, String> mapTitles = new HashMap<String, String>();

    class PageRankDocument implements Comparable<PageRankDocument> {
        double pageRank;
        // int index;
        String docNumber;
        int numberOfStops = 0;

        PageRankDocument(String docNumber, double pageRank) {
            this.docNumber = docNumber;
            this.pageRank = pageRank;
        }
        PageRankDocument(double pageRank) {
            this.pageRank = pageRank;
        }

        public int compareTo(PageRankDocument pg) {

            if (this.pageRank == pg.pageRank)
                return 0;

            if (this.pageRank < pg.pageRank)
                return 1;

            return -1;
        }

        public String toString() {
            return String.format("%5s %f", docNumber, pageRank);
        }

    }
    /* --------------------------------------------- */

    public PageRankSparse(String filename) {
        System.out.println("Calling constructor PageRankSparse with filename = " + filename);
        this.noOfDocs = readDocs(filename);
        
        if(filename.equals("linksDavis.txt"))
            readTitles("davisTitles.txt");
        else if(filename.equals("linksSvwiki.txt"))
            readTitles("svwikiTitles.txt");

        // iterate(noOfDocs, 1000);
    }

    /* --------------------------------------------- */

    /**
     * Reads the documents and fills the data structures.
     *
     * @return the number of documents read.
     */
    int readDocs(String filename) {
        int fileIndex = 0;
        int countTrue = 0;
        try {
            System.err.print("Reading file... " + filename);
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = in.readLine()) != null && fileIndex < MAX_NUMBER_OF_DOCS) {
                int index = line.indexOf(";");
                String title = line.substring(0, index);
                Integer fromdoc = docNumber.get(title);
                // System.out.println("fromdoc = " + fromdoc);
                // Have we seen this document before?
                if (fromdoc == null) {
                    // This is a previously unseen doc, so add it to the table.
                    fromdoc = fileIndex++;
                    docNumber.put(title, fromdoc);
                    docName[fromdoc] = title;
                }

                // Check all outlinks.
                StringTokenizer tok = new StringTokenizer(line.substring(index + 1), ",");

                while (tok.hasMoreTokens() && fileIndex < MAX_NUMBER_OF_DOCS) {
                    String otherTitle = tok.nextToken();
                    Integer otherDoc = docNumber.get(otherTitle);
                    if (otherDoc == null) {
                        // This is a previousy unseen doc, so add it to the table.
                        otherDoc = fileIndex++;
                        docNumber.put(otherTitle, otherDoc);
                        docName[otherDoc] = otherTitle;
                    }

                    // Set the probability to 0 for now, to indicate that there is
                    // a link from fromdoc to otherDoc.
                    if (link.get(fromdoc) == null) {
                        link.put(fromdoc, new HashMap<Integer, Boolean>());
                    }
                    if (link.get(fromdoc).get(otherDoc) == null) {
                        link.get(fromdoc).put(otherDoc, true);
                        countTrue++;
                        out[fromdoc]++;
                    }
                }
            }
            if (fileIndex >= MAX_NUMBER_OF_DOCS) {
                System.err.print("stopped reading since documents table is full. ");
            } else {
                System.err.print("done. Total true: " + countTrue);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File " + filename + " not found!");
        } catch (IOException e) {
            System.err.println("Error reading file " + filename);
        }
        System.err.println(" Read " + fileIndex + " number of documents");
        System.err.println("done. Total true: " + countTrue);
        return fileIndex;
    }

    double[] multiply(double[] x, double n) {
        double[] next_x = new double[x.length];

        for (int i = 0; i < x.length; i++) {
            next_x[i] = 0;

            for (int j = 0; j < x.length; j++) {
                double p_res = 0;
                if (out[j] == 0) {
                    p_res = 1.0 / n;
                }
                // when the probability that the random surfer clicks from page i to page j is 0
                else if (link.get(j).get(i) == null) {
                    p_res = BORED / n;
                } else {
                    p_res = BORED / n;
                    p_res += (1.0 - BORED) / (double) out[j];
                }

                next_x[i] += x[j] * p_res;
            }
        }

        return next_x;
    }

    boolean checkConvergence(double[] x, double[] next_x) {
        for (int i = 0; i < x.length; i++) {
            if (Math.abs(x[i] - next_x[i]) > EPSILON) {
                return false;
            }
        }
        return true;
    }

    /* --------------------------------------------- */

    public void save(String fileName, ArrayList<PageRankDocument> list) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (PageRankDocument pdDoc : list) {
                // String line = String.format("%5s %f", docNumber.get(pdDoc.docNumber),
                // pdDoc.pageRank);
                String line = String.format("%s\t%f", pdDoc.docNumber, pdDoc.pageRank);

                bw.write(line + "\n");
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    HashMap<String, String> davisMap = new HashMap<String, String>();

    public void readTitles(String fileName) {

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split(";");
                mapTitles.put(split[0], split[1]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    double[] davisScores = new double[30];
    String[] davisTop30Titles = new String[30];

    public void readTitles_2(String fileName) {

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split(":");
                davisMap.put(split[0], split[1]);
                davisScores[i] = Double.valueOf(split[1].trim()).doubleValue();
                davisTop30Titles[i] = split[0].trim();
                i++;

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    /*
     * Chooses a probability vector a, and repeatedly computes aP, aP^2, aP^3...
     * until aP^i = aP^(i+1).
     */
    void iterate(int maxIterations) {
        long start = System.currentTimeMillis();
        int numberOfDocs = noOfDocs;
        int loops = 0;
        double[] x = new double[numberOfDocs];
        double[] next_x = new double[numberOfDocs];

        for (int i = 0; i < numberOfDocs; i++) {

            x[i] = 1.0 / numberOfDocs;
        }

        while (loops <= maxIterations) {

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
        ArrayList<PageRankDocument> res2 = new ArrayList<PageRankDocument>();

        for (int i = 0; i < numberOfDocs; i++) {
            res.add(new PageRankDocument(mapTitles.get(docName[i]), next_x[i]));
            res2.add(new PageRankDocument(docName[i], next_x[i]));

        }
        
        Collections.sort(res);
        Collections.sort(res2);

        // Save pageranks in txt file - ONLY ONCE !!!!
        // save("pageranks.txt", res);

        for (int i = 0; i < 30; i++) {
            System.out.printf("%s\n", res2.get(i));

        }
        float seconds = 1e-3f * (System.currentTimeMillis() - start);
        System.err.println("Iterate time: " + seconds);
    }

    /* --------------------------------------------- */

    ArrayList<PageRankDocument> method1(int N, int c) {

        long start = System.currentTimeMillis();

        int probability_continue = c;
        int probability_stop = 100 - c;
        Random random = new Random();

        ArrayList<PageRankDocument> res = new ArrayList<PageRankDocument>();

        for (int i = 0; i < noOfDocs; i++) {
            res.add(new PageRankDocument(docName[i], 0.0));

        }

        for (int n = 0; n < N; n++) {
            int currentDocumentInternalID = random.nextInt(noOfDocs);

            while (true) {
                int dice = random.nextInt(100); // 0 ... 99
                if (dice < probability_stop) {
                    break;
                } else {
                    int outgoing = out[currentDocumentInternalID]; // 3 outgoing links
                    if (outgoing == 0) {
                        // Dangling node, jump to random
                        currentDocumentInternalID = random.nextInt(noOfDocs);
                    }
                    else{
                        int nextStop = random.nextInt(outgoing); // 0..2

                        HashMap<Integer, Boolean> map = link.get(currentDocumentInternalID);
    
                        Integer temp = (Integer) map.keySet().toArray()[nextStop];
                        currentDocumentInternalID = temp.intValue();
                    }
                   
                }
            }

            res.get(currentDocumentInternalID).numberOfStops++;
        }

        for (int i = 0; i < noOfDocs; i++) {
            res.get(i).pageRank = res.get(i).numberOfStops / (double) N;
        }

        Collections.sort(res);

        save("mc1.txt", res);

        float seconds = 1e-3f * (System.currentTimeMillis() - start);
        System.err.println("Monte Carlo 1 time: " + seconds);

        return res;
    }


    ArrayList<PageRankDocument> method2(int N, int c) {
        int numberOfDocs = noOfDocs;

        long start = System.currentTimeMillis();
        int probability_continue = c;
        int probability_stop = 100 - c;
        Random random = new Random();

        ArrayList<PageRankDocument> res = new ArrayList<PageRankDocument>();

        for (int i = 0; i < noOfDocs; i++) {
            res.add(new PageRankDocument(docName[i], 0));
        }

        int M = N / numberOfDocs;
       
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < numberOfDocs; n++) {
                int currentDocumentInternalID = n;
                while (true) {
                    int dice = random.nextInt(100); // 0 ... 99
                    if (dice < probability_stop) {
                        break;
                    } else {
                        int outgoing = out[currentDocumentInternalID]; // 3 outgoing links
                        if (outgoing == 0) {
                            // Dangling node, jump to random
                            currentDocumentInternalID = random.nextInt(noOfDocs);
                        }
                        else{
                            int nextStop = random.nextInt(outgoing); // 0..2

                            HashMap<Integer, Boolean> map = link.get(currentDocumentInternalID);
    
                            Integer temp = (Integer) map.keySet().toArray()[nextStop];
                            currentDocumentInternalID = temp.intValue();
                        }
                       
                    }
                }

                res.get(currentDocumentInternalID).numberOfStops++;
            }
        }

        for (int i = 0; i < noOfDocs; i++) {
            res.get(i).pageRank = res.get(i).numberOfStops / (double) (noOfDocs * M);
        }

        Collections.sort(res);

        save("mc2.txt", res);
        float seconds = 1e-3f * (System.currentTimeMillis() - start);
        System.err.println("Monte Carlo 2 time: " + seconds);

        return res;
    }

    ArrayList<PageRankDocument> method4(int N, int c, ArrayList<PageRankDocument> res) {

        int numberOfDocs = noOfDocs;
        long start = System.currentTimeMillis();
        int probability_continue = c;
        int probability_stop = 100 - c;
        Random random = new Random();

        // ArrayList<PageRankDocument> res = new ArrayList<PageRankDocument>();

        // for (int i = 0; i < noOfDocs; i++) {
        //     res.add(new PageRankDocument(docName[i], 0));
        // }

        int totalVisits = 0;
        int M = N / numberOfDocs;

        for (int m = 0; m < M; m++) {
            for (int n = 0; n < numberOfDocs; n++) {
                int currentDocumentInternalID = n;
                res.get(currentDocumentInternalID).pageRank += 1.;
                totalVisits++;

                while (true) {
                    int dice = random.nextInt(100); // 0 ... 99
                    if (dice < probability_stop) {
                        break;
                    } else {
                        int outgoing = out[currentDocumentInternalID]; // 3 outgoing links
                        if (outgoing == 0) {
                            break;
                        } else {
                            int nextStop = random.nextInt(outgoing); // 0..2

                            HashMap<Integer, Boolean> map = link.get(currentDocumentInternalID);

                            Integer temp = (Integer) map.keySet().toArray()[nextStop];
                            currentDocumentInternalID = temp.intValue();
                        }
                        res.get(currentDocumentInternalID).pageRank += 1.;
                        totalVisits++;
                    }
                }

            }
        }

        for (int i = 0; i < noOfDocs; i++) {
            res.get(i).pageRank /= totalVisits;
        }

        Collections.sort(res);

        save("mc4.txt", res);
        float seconds = 1e-3f * (System.currentTimeMillis() - start);
        System.err.println("Monte Carlo 4 time: " + seconds);

        return res;
    }

    ArrayList<PageRankDocument> method5(int N, int c) {

        int numberOfDocs = noOfDocs;
        long start = System.currentTimeMillis();
        int probability_continue = c;
        int probability_stop = 100 - c;
        Random random = new Random();

        ArrayList<PageRankDocument> res = new ArrayList<PageRankDocument>();

        for (int i = 0; i < noOfDocs; i++) {
            res.add(new PageRankDocument(docName[i], 0));
        }
        double visits = 0.0;
        int totalVisits = 1;
        HashMap<Integer, Boolean> outgoing;

        for (int n = 0; n < N; n++) {
            int currentDocumentInternalID = random.nextInt(numberOfDocs);
            res.get(currentDocumentInternalID).pageRank += 1.;

            while (true) {
                int dice = random.nextInt(100); // 0 ... 99
                if (dice < probability_stop) {
                    break;
                } else {

                    outgoing = link.get(currentDocumentInternalID); // 3 outgoing links
                    if (outgoing == null) {
                        break;
                    } else {
                        totalVisits++;
                        ArrayList<Integer> keys = new ArrayList<>(outgoing.keySet());
                        int nrOfLinks = out[currentDocumentInternalID];
                        currentDocumentInternalID = keys.get(random.nextInt(nrOfLinks));
                        res.get(currentDocumentInternalID).pageRank += 1.;

                    }
                }
            }

        }

        for (int i=0;i<noOfDocs;i++) {
			visits += res.get(i).pageRank;
		}
    	for (int i =0;i<noOfDocs;i++) {
            res.get(i).pageRank = res.get(i).pageRank/visits;
            res.add(new PageRankDocument(docName[i], res.get(i).pageRank));
    	}
        

        Collections.sort(res);

        save("mc5.txt", res);
        float seconds = 1e-3f * (System.currentTimeMillis() - start);
        System.err.println("Monte Carlo 5 time: " + seconds);

        return res;
    }

    /* --------------------------------------------- */
    boolean checkConvergence2(ArrayList<PageRankDocument> x, ArrayList<PageRankDocument> next_x) {
        for(PageRankDocument pgr : x ){
            for(PageRankDocument next_pgr: next_x){
                if (Math.abs(pgr.pageRank - next_pgr.pageRank) > EPSILON) {
                    return false;
                }
            }

        }
        
        return true;
    }

    void approximate_MC4(int maxIterations, int N) {
        long start = System.currentTimeMillis();
        int numberOfDocs = N;
        int loops = 0;
        ArrayList<PageRankDocument> x = new ArrayList<PageRankDocument>();
        ArrayList<PageRankDocument> next_x = new ArrayList<PageRankDocument>();

        for (int i = 0; i < numberOfDocs; i++) {
            PageRankDocument pg = new PageRankDocument(1.0 / numberOfDocs);
            x.add(i, pg);
            x.get(i).pageRank =  pg.pageRank;
        }

        
        for (int i = 0; i < numberOfDocs; i++) {

            PageRankDocument next_pg = new PageRankDocument(x.get(i).pageRank);
            next_x.add(i, next_pg);
            next_x.get(i).pageRank =  next_pg.pageRank;
        }

        while (loops <= maxIterations) {

            
            next_x = method4(maxIterations, 85, x);

            if (checkConvergence2(x, next_x) == true) {
                break;
            } else {
            
                for (int i = 0; i < numberOfDocs; i++) {
                    PageRankDocument next_pg = new PageRankDocument(next_x.get(i).pageRank);
                    x.add(i, next_pg);
                    x.get(i).pageRank =  next_x.get(i).pageRank;
                }
                loops++;
            }

        }

        
        System.out.println("Iterations of MC 4: " + loops);

        // sort, but preserve indices
        ArrayList<PageRankDocument> res2 = new ArrayList<PageRankDocument>();

        for (int i = 0; i < numberOfDocs; i++) {
            res2.add(new PageRankDocument(docName[i], next_x.get(i).pageRank));

        }
        
        Collections.sort(res2);

        // Save pageranks in txt file - ONLY ONCE !!!!
        // save("pageranks.txt", res);

        for (int i = 0; i < 30; i++) {
            // System.err.println("MC4 approximation for Svwiki: " + seconds);
            System.out.printf("%s\n", res2.get(i));

        }
        // float seconds = 1e-3f * (System.currentTimeMillis() - start);
        // System.err.println("MC4 approximation for Svwiki time: " + seconds);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please give the name of the link file");
        } else {
            PageRankSparse obj = new PageRankSparse(args[0]);

            obj.iterate(1000);
           //=================
            // read file with top_30 and get pageranks
            // if(args[0].equals("linksDavis.txt"))
            //     obj.readTitles_2("davis_top_30.txt");
            // else if(args[0].equals("linksSvwiki.txt"))
            //     obj.readTitles_2("svwiki1000_top_30.txt");
                
            // obj.approximate_MC4(960726, 960726);

            // System.out.printf("Top 30 for Monte Carlo 4");
            // for (int i = 0; i < 30; i++) {

            //     System.out.printf("%s\n", list.get(i));

            // }

            // // obj.readTitles_2("davis_top_30.txt");
            // System.out.println("DAVIS MAP SIZE ========== " + obj.davisMap.size());

            // int[] N_array = { 24221, 242210, 2422100, 24221000, 242210000 };

            // double[] errorsArray = new double[N_array.length];

            // int [] methodversions = { 1, 2, 4, 5 };

            // for (int method = 0; method < 4; method++) {
            //     for (int i = 0; i < N_array.length; i++) {
            //         ArrayList<PageRankDocument> list = null;

            //         switch (method) {
            //         case 0:
            //             list = obj.method1(N_array[i], 85);
            //             break;
            //         case 1:
            //             list = obj.method2(N_array[i], 85);
            //             break;
            //         case 2:
            //             list = obj.method4(N_array[i], 85);
            //             break;
            //         case 3:
            //             list = obj.method5(N_array[i], 85);
            //             break;
            //         }

            //         double sum = 0.0;
            //         for (int j = 0; j < 30; j++) {
            //             // find index of object in mc1 top 30
            //             String docname_davis = obj.davisTop30Titles[j];
            //             double score_davis = obj.davisScores[j];

            //             for (PageRankDocument p : list) {
            //                 if (p.docNumber.equals(docname_davis)) {
            //                     double score_carlo = p.pageRank;
            //                     sum += Math.pow(score_davis - score_carlo, 2);
            //                     break;
            //                 }
            //             }

                        
            //         }

            //         errorsArray[i] = sum;
            //     }

            //     for (int i = 0; i < N_array.length; i++) {
            //         System.out.printf("   Method %d:\t%d\t%d\t%.14f\n ", methodversions[method] , i , N_array[i] , errorsArray[i]);                    
            //     }
            // }
//===========
        }
    }
}