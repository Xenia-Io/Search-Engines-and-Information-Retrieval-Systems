# DD2476-Search-Engines-and-Information-Retrieval-Systems
Individual assignments for the course Search Engines and Information Retrieval Systems at KTH Royal Institute of technology


Information Retrieval (IR) is finding material (usually documents) of an unstructured nature that satisfies an information need from within
large collections (usually stored on computers). 


Assignment 1: Boolean Retrieval

The purpose of Assignment 1 is to learn how to build an inverted index. You will learn 1) how build a basic inverted index; 2) how to handle multiword queries; 
3) how to handle phrase queries; 4) how to evaluate a search system; and 5) techniques for handling large indexes.
In realistic applications, we of course cannot index the whole document collection every time we start the search engine. Moreover, the complete index would be too large to fit in working memory. So, we implement the index by means of a persistent hash table on disk. Indexing the davisWiki corpus does not take more than 3 minutes. Search is immediate (definitely less than 0.1s) for any search query. So the implementation of a persistent hash table works fine.
Exercise 1.1-1.7 have been implemented


Assignment 2: Ranked Retrieval

The purpose of Assignment 2 is to learn how to implement ranked retrieval. You will learn 1) how to include tf_idf scores in the inverted index; 2) how to handle ranked retrieval from multiword queries; 3) how to use PageRank to score documents; and 4) how to combine tf_idf and PageRank scoring
Implementation of Monte-Carlo PageRank Approximation. Testing and comparing the 5 approximations (results are presented in excel file). A record of the experimentation has being shown with the four method variants and their N parameter settings for the linksDavis.txt graph

Assignment 3: Relevance Feedback and Tolerant Retrieval

The purpose of Assignment 3 is to learn about ways to get more powerful representations of query and documents. You will learn 1) how to use relevance feedback to improve the query representation; 2) why query expansion is an alternative to relevance feedback; 3) how to build k-gram index; 4) how to perform tolerant retrieval with wildcard queries
Exercises 3.1-3.4 have been implemented
