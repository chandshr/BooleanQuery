# BooleanQuery
Logic:
hash table with key = "term" and value = "list of doc ids"
the search query is pushed to appropriate operator and operand stack 
the term and operator is popped from the stack and the hash key is scanned for the "term" the returned docIds from hash table is pushed in result stack. And operator stack is popped to take the appropriate action to be taken with the current searched docIds and the "result" stack.
at last result stack contains the appropriate doc ids which satisfy the boolean query
The manipulation of stack is done by 
public Stack<TreeSet<Integer>> operate( HashMap<String, HashMap<Integer, Integer>> allTerms,  TreeSet<Integer> allDocIds, String inputQuery)
private Stack<TreeSet<Integer>> boolAction(HashMap<String, HashMap<Integer, Integer>> allTerms, TreeSet<Integer> allDocIds, Stack<String> operator, Stack<TreeSet<Integer>> result, String stemOut)
The output of the program is inside src/output folder and the input file cran.txt is inside InvertedFile/src/input folder. The main java class is inside boolQuery/src/invertPackage/ReadTerm.java. Here the main class is ReadTerm we can compile and run this class using the below given command
Compile: javac ReadTerm.java
Run: java ReadTerm
It takes around 50secs to generate the output. The output can be viewed on console and is also stored in output/outFile.txt

We can also import this project in eclipse and we can directly run from eclipse.
