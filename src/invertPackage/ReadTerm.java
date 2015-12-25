package invertPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class ReadTerm implements boolAction {

	/**
	 * Read the content of cran.txt starting from I. and till it finds next I.
	 * write the document ID in the outFile.txt
	 */

	public void readCran(final File srcPath, String s) throws IOException {
		Scanner input = null;
		PrintWriter out = null;
		String word;
		String stemOut;
		Stemmer stemObj = new Stemmer();
		TreeSet<Integer> allDocIds = new TreeSet<Integer>();
		try {
			input = new Scanner(new FileReader(srcPath + "//input//cran.txt"));
			out = new PrintWriter(new FileWriter(srcPath
					+ "//output//outFile.txt"));
			HashMap<String, HashMap<Integer, Integer>> allTerms = new HashMap<String,  HashMap<Integer, Integer>>();
			HashMap<Integer, Integer> getDocTerm = new HashMap<Integer, Integer>();
			int docId = 0;
			int prevCountDoc = 0;
			/**
			 *  CREATE ARRAYLIST OF STOPWORDS FROM STOP.TXT STOP *******
			 *  */
			while (input.hasNext()) {
				word = input.next();
				/**	
				 * Content of a document
				 */
				if(!word.equals(s)){
					if (!checkStopWord(word)) {
						stemOut = stemObj.steamWord(word); // stem this word
						/**
						 * Outer Hash Start
						 */
						if(!allTerms.containsKey(stemOut)){
							allTerms.put(stemOut, new HashMap<Integer, Integer>());
							allTerms.get(stemOut).put(docId, 1); 
						}else{
							//get value of the current hash map
							if(allTerms.get(stemOut).containsKey(docId)){
								prevCountDoc = allTerms.get(stemOut).get(docId);
							}else{
								prevCountDoc = 0;
							}
							allTerms.get(stemOut).put(docId, prevCountDoc+1);
						}
					}
				}else{
					/**
					 * New Document start with .I
					 */
					docId = input.nextInt();
					allDocIds.add(docId);
					prevCountDoc = 0;
					getDocTerm.clear();
					
				}
			}
			input = new Scanner(System.in);
			String inputQuery = input.nextLine().toLowerCase();
			TreeSet<Integer> tempAllIds = new TreeSet<Integer>();
			tempAllIds.addAll(allDocIds);
			Stack<TreeSet<Integer>> searchResult = operate(allTerms, tempAllIds, inputQuery);
			out.println("Query "+ inputQuery);
			if(searchResult.empty()){
				System.out.println("This output is saved in outfile.txt");
				System.out.println("Query contains stopwords");
				out.println("Query contains stopwords");
			}else{
				displayStack(searchResult, out);
			}
			//get the output results printed here 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			input.close();
			out.close();
		}
	}

	/**
	 * Return docIDs for true bool query
	 */
	public Stack<TreeSet<Integer>> operate( HashMap<String, HashMap<Integer, Integer>> allTerms,  TreeSet<Integer> allDocIds, String inputQuery){
		
		Stack<String> operator = new Stack<String>();
		Stack<TreeSet<Integer>> result = new Stack<TreeSet<Integer>>();
		Stack<TreeSet<Integer>> subQueryResult = new Stack<TreeSet<Integer>>();
		TreeSet<Integer> popResult = new TreeSet<Integer>();
		TreeSet<Integer> finalResult = new TreeSet<Integer>();
		TreeSet<Integer> subQueryOther = new TreeSet<Integer>();
		TreeSet<Integer> tempAllIds = new TreeSet<Integer>();
		tempAllIds.addAll(allDocIds);
		Stemmer stemObj = new Stemmer();
		String stemOut = "";
		if("quit".equalsIgnoreCase(inputQuery))
			System.exit(0);
		String[] words = inputQuery.split(" ");
		String q;
		String subQuery = "";
		int count = 0;
		//cases start
//		for(String word : words){
		for( int i = 0; i < words.length; i++ ){
			switch (words[i]){
				case "(": 
					if( inputQuery.substring(inputQuery.indexOf("(")+2, inputQuery.indexOf(")")) == subQuery )
						return result;
//					subQuery = inputQuery.substring(inputQuery.indexOf("(")+2, inputQuery.indexOf(")"));
					count ++;
					while( count > 0 ){
						i++;
						if( words[i].equals(")") )
							count --;
						if( words[i].equals("(") )
							count ++;
						if( count > 0 )
						subQuery = subQuery+" "+words[i];
					}
					if( !subQuery.isEmpty() )
					subQueryResult.push(operate(allTerms, allDocIds, subQuery).pop());
					if( !result.empty() ){
						finalResult = result.pop();
					}
					if( operator.empty() ){
						result.push(subQueryResult.pop());
					}
					while( !operator.empty() ){
						String p = operator.pop();
						if(!subQueryResult.isEmpty()){
							switch(p){
								case "not":
									tempAllIds.removeAll(subQueryResult.pop()); //searchResult already has values and here it adds new values
									result.push(tempAllIds);
									break;
								case "and":
									finalResult.retainAll(subQueryResult.pop()); //searchResult already has values and here it adds new values
									result.push(finalResult);
									break;
								case "or":
									finalResult.addAll(subQueryResult.pop());
									result.push(finalResult);
									break;
							}
						}
					}
					break;
				case  "and": case "or": case "not":
					operator.push(words[i]);
					break;
				default:
					if(!checkStopWord(words[i])){
						stemOut = stemObj.steamWord(words[i]); // stem this word
						if( operator.empty() ){
							result.push(search(allTerms, stemOut));
						}
						else{
							boolAction(allTerms, allDocIds, operator, result, stemOut);
						}
						
					}
					//return false here if its the stopword
					break;
			}
		}
		//cases end
		return result;
	}

	private Stack<TreeSet<Integer>> boolAction(
			HashMap<String, HashMap<Integer, Integer>> allTerms,
			TreeSet<Integer> allDocIds, Stack<String> operator,
			Stack<TreeSet<Integer>> result, String stemOut) {
		TreeSet<Integer> popResult;
		TreeSet<Integer> finalResult;
		String s = operator.pop();
		TreeSet<Integer> tempAllIds = new TreeSet<Integer>();
		tempAllIds.addAll(allDocIds);
		switch(s){
			case "and":
				finalResult = search(allTerms, stemOut);
				popResult = result.pop();
				finalResult.retainAll(popResult); //searchResult already has values and here it adds new values
				result.push(finalResult);
				break;
			case "or":
				finalResult = search(allTerms, stemOut);
				popResult = result.pop();
				finalResult.addAll(popResult); //searchResult already has values and here it adds new values
				result.push(finalResult);
				break;
			case "not":
				finalResult = search(allTerms, stemOut); //current search word docids
				tempAllIds.removeAll(finalResult); //action
				if(result.empty()){
					result.push(tempAllIds);
				}else{
					popResult = result.pop(); //pulls earlier values
					if(operator.empty())
						result.push(tempAllIds); //push the value back
					else{
						String q = operator.pop();
						switch(q){
							case "and":
								tempAllIds.retainAll(popResult); //searchResult already has values and here it adds new values
								result.push(tempAllIds);
								break;
							case "or":
								tempAllIds.addAll(popResult); //searchResult already has values and here it adds new values
								result.push(tempAllIds);
								break;
						}
					}
				}
				break;
		}
		return result;
	}
	
	public void displayStack( Stack<TreeSet<Integer>> result, PrintWriter out ){
		for( int i = 0; i < result.size(); i++ ){
			System.out.println(result.get(i));
			System.out.println("This output is saved in outfile.txt");
			out.println("Doc IDs' "+result.get(i));
		}
	}
	
	/**
	 * Search the word in hashmap of docIds
	 */
	
	public TreeSet<Integer> search( HashMap<String, HashMap<Integer, Integer>> allTerms, String word ){
		Set<String> allTermsHashKey = allTerms.keySet();
		ArrayList<String> termsKeyArray = new ArrayList<String>();
		termsKeyArray.addAll(allTermsHashKey);
		HashMap<Integer, Integer> docIds = new HashMap<Integer, Integer>();
		for (String term: termsKeyArray){
			if(term.equalsIgnoreCase(word)){
				docIds = allTerms.get(term);
				break;
			}
		}
		Set<Integer> allDocIdsHashKeyset = docIds.keySet();
		TreeSet<Integer> allDocIdsHashKey = new TreeSet<Integer>(allDocIdsHashKeyset);
		return allDocIdsHashKey;
	}
	
	/**
	 * This method prints the terms in the file
	 * @param out - Output printwriter stream
	 * @param i - Count for the line
	 * @param termCount - Termcount map to keep track of the terms
	 */
	private void printTerms(PrintWriter out, int i,
			HashMap<String, Integer> termCount) {
		List<String> sortedKeys = new ArrayList<String>(termCount.keySet());
		if (i - 1 > 0) {
			out.println("Doc ID: " + (i - 1) + "\n"
					+ "Total Unique Term Count: " + Term.terms.size()); // Initially
																		// doc-id
																		// = 1
																		// term
																		// count
																		// 0;
		}
		Collections.sort(sortedKeys);
		for (String str : sortedKeys) {
			out.println("Term: " + str + " Count: "
					+ termCount.get(str).intValue());
		}
		Term.terms.clear();
		termCount.clear();
		sortedKeys.clear();
		out.flush();
	}

	/**
	 * it takes the word given by readCran() and checks if it is stopword if
	 * the passed "checkWord" is not stopword it returns the string which was
	 * passed
	 * 
	 * @param srcPath
	 * @param checkWord
	 * @return
	 */
	public static boolean checkStopWord(String checkWord) {
		Path currentRelativePath = Paths.get("");
		String getProjPath = currentRelativePath.toAbsolutePath().toString();
		final File srcPath = new File(getProjPath + "//src");
		File stopWordFile = new File(srcPath + "//input//stopWords.txt");
		if (!stopWordFile.exists()) {
			throw new RuntimeException("File Not Found");
		}
		BufferedReader reader = null;
		StringBuilder stopWords = new StringBuilder();
		try {
			reader = new BufferedReader(new FileReader(stopWordFile));
			String line;
			while ((line = reader.readLine()) != null) {
				stopWords.append(line + " ");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String notStopWord = "";
		String[] arrStopWords = stopWords.toString().split("\\s+");
		if (!Arrays.asList(arrStopWords).contains(checkWord)) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) throws IOException {
		Path currentRelativePath = Paths.get("");
		String getProjPath = currentRelativePath.toAbsolutePath().toString();
		final File srcPath = new File(getProjPath + "//src");
		ReadTerm readTermObj = new ReadTerm();
		readTermObj.readCran(srcPath, ".I");
	}
}
