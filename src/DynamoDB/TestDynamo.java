package DynamoDB;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import snowballstemmer.PorterStemmer;

public class TestDynamo {


	public static void main(String[] args) {
		String query = "university of pennsylvania";
		QueryInfo queryInfo = new QueryInfo(query);
		// remove words with low idf
		List<String> wordlist = queryInfo.getWordlist();
		int size = queryInfo.getSize();
		List<Double> idflist = queryInfo.getIDFlist();
		HashMap<ByteBuffer, DocResult> set = new HashMap<ByteBuffer, DocResult>();
		for (int i = 0; i < size; i++) {
			String word = wordlist.get(i);
			System.out.println(word);
			List<InvertedIndex> collection = InvertedIndex.query(word);
			for (InvertedIndex ii : collection) {
				ByteBuffer docID = ii.getId();
				if (!set.containsKey(docID))
					set.put(docID, new DocResult(docID, size, queryInfo.getWindowlist(), idflist));
				set.get(docID).setPositionList(i, ii.getPositions());
				set.get(docID).setTF(i, ii.getTF());
			}
		}
		List<DocResult> intersection = new ArrayList<DocResult>();
		for (ByteBuffer docID : set.keySet()) {
			if (set.get(docID).containsAll()) {
				intersection.add(set.get(docID));
			}
		}
		// compute rank each doc
		for (DocResult doc : intersection) {
			doc.calculateScore();
		}
		Collections.sort(intersection, new Comparator<DocResult>() {
	        @Override
	        public int compare(DocResult o1, DocResult o2) {
	            return o2.compareTo(o1);
	        }
	    });
		for(DocResult doc: intersection){
			System.out.println(DocURL.load(doc.getDocID().array()).getURL() +"\t"+doc.getFinalScore());
			for(List<Integer> w:doc.getPositions()){
				System.out.println(w);
			}
		}
	}

}

class QueryInfo {
	
	private static final String PARSER = " \t\n\r\"'-_/.,:;|{}[]!@#%^&*()<>=+`~?";
	private static final double LIMIT = 1;
	private static final int WINDOW = 3;
	
	List<String> wordlist = new ArrayList<String>();
	List<Integer> indexlist = new ArrayList<Integer>();
	List<Double> idflist = new ArrayList<Double>();
	int[] windowlist;
	
	public QueryInfo(String query){
		List<String> parseQuery = stemContent(query.toLowerCase());
		for(int i=0;i<parseQuery.size();i++){
			String word = parseQuery.get(i);
			double idf = IDF.load(word).getidf();
			System.out.println(idf);
			if(idf > LIMIT){
				wordlist.add(word);
				indexlist.add(i);
				idflist.add(idf);
			}
		}
		windowlist = new int[indexlist.size()-1];
		for(int i=0;i<indexlist.size()-1;i++){
			windowlist[i] = -1+indexlist.get(i+1)-indexlist.get(i)+WINDOW;
		}
	}
	
	public List<String> getWordlist(){
		return wordlist;
	}
	
	public List<Integer> getIndexlist(){
		return indexlist;
	}
	
	public List<Double> getIDFlist(){
		return idflist;
	}
	
	public int[] getWindowlist(){
		return windowlist;
	}
	
	
	
	public int getSize(){
		return wordlist.size();
	}
	
	public static List<String> stemContent(String content) {
		StringTokenizer tokenizer = new StringTokenizer(content, PARSER);
		String word = "";
		PorterStemmer stemmer = new PorterStemmer();
		List<String> parseQuery = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			word = tokenizer.nextToken();
			if (word.equals(""))
				continue;
			boolean flag = false;
			for (int i = 0; i < word.length(); i++) {
				if (Character.UnicodeBlock.of(word.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN) {
					flag = true;
					break;
				}
			}
			if (flag)
				continue;
			int i = 0;
			while (i < word.length()
					&& (!Character.isLetter(word.charAt(i)) && !Character
							.isDigit(word.charAt(i)))) {
				i++;
			}
			if (i >= word.length())
				continue;
			word = word.substring(i);
			i = word.length() - 1;
			while (i >= 0
					&& (!Character.isLetter(word.charAt(i)) && !Character
							.isDigit(word.charAt(i)))) {
				i--;
			}
			if (i < 0)
				continue;
			word = word.substring(0, i + 1);
			stemmer.setCurrent(word);
			if (stemmer.stem()) {
				parseQuery.add(stemmer.getCurrent());
			}
		}
		return parseQuery;
	}

}

class DocResult {
	
	private static final int BASE = 30;
	private static final int WINDOW = 5;
	
	private static final double W_POSITION = 0.5;
	private static final double W_PAGERANK = 0.3;
	private static final double W_ANCHOR = 0;
	private static final double W_TFIDF = 0.2;
	
	ByteBuffer id;
	double[] wordtf;
	List<Integer>[] positions;
	List<Double> idflist;
	int[] windowlist;
	String url;
	int size;
	
	// different factors
	double tfidf;
	int count = 0;
	double positionScore = 0;
	double anchorScore = 0;
	double pageRank;
	double finalScore;

	DocResult(ByteBuffer id, int size, int[] windowlist, List<Double> idflist) {
		this.id = id;
		this.size = size;
		positions = (List<Integer>[])new List[size];
		this.windowlist = windowlist;
		this.wordtf = new double[size];
		for(int i=0;i<size;i++) wordtf[i] = 0;
		this.idflist = idflist;
	}

	public boolean containsAll() {
		return size == count;
	}

	public void setPositionList(int index, Set<Integer> position) {
		List<Integer> po = new ArrayList<Integer>();
		for (int p : position) {
			po.add(p);
		}
		Collections.sort(po);
		positions[index] = po;
		count++;
	}
	
	public void setTF(int index, double tf){
		wordtf[index] = tf;
	}

	public void setFinalScore(double finalScore) {
		this.finalScore = finalScore;
	}

	public List<Integer>[] getPositions() {
		return positions;
	}
	
	public double getPositionScore(){
		return positionScore;
	}
	
	public ByteBuffer getDocID(){
		return id;
	}

	// calculate position score
	public void setPositionScore() {
		for (int i = 0; i < positions.length - 1; i++) {
			int score = 0, j = 0, k = 0, dis;
			boolean firstTime = true;
			List<Integer> word1 = positions[i];
			List<Integer> word2 = positions[i+1];

			// iterate through the two position lists, and both start from index
			// 0
			while (j < word1.size() && k < word2.size()) {
				dis = word2.get(k) - word1.get(j);
				/*
				 * if it's "word2 ... word1" move the pointer of word2 to next
				 */
				if (dis <= 0) {
					k++;
				}
				else if (dis > 0 && dis <= windowlist[i]) {
					if (firstTime) {
						score = BASE;
						firstTime = false;
					} else {
						score += 1;
					}
					j++;
				}
				/*
				 * if it's "word1 ... word2" but their distance is greater than
				 * window move the pointer of word1 to next
				 */
				else {
					j++;

				}
			}
			positionScore += score;
		}
		if(size != 1) positionScore = positionScore/((size-1)*BASE);
	}

	// calculate tf score
	public void setTFScore() {
		tfidf = 0;
		for(int i=0;i<size;i++){
			tfidf += wordtf[i]*idflist.get(i);
		}
	}
	
	// calculate anchor score
	public void setAnchorScore() {
		
	}
	
	// calculate pageRank score
	public void setPageRank(){
		pageRank = PageRank.load(id).getRank();
	}
	
	public void calculateScore(){
		setPositionScore();
		setPageRank();
		setAnchorScore();
		setTFScore();
		finalScore = W_POSITION*positionScore + W_PAGERANK*pageRank + W_ANCHOR*anchorScore + W_TFIDF*tfidf;
	}
	
	public double getPageRank() {
		return pageRank;
	}

	public double getTF(int index) {
		return wordtf[index];
	}

	public double getFinalScore() {
		return finalScore;
	}

//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append(url);
//		sb.append("\t");
//		sb.append(rank);
//		sb.append("\t");
//		for (int i = 0; i < size; i++) {
//			sb.append("word");
//			sb.append(i);
//			sb.append(":  ");
//			sb.append(wordtf[i]);
//		}
//		return sb.toString();
//	}

	public int compareTo(Object other) {
		if (this.finalScore == ((DocResult) other).finalScore)
			return 0;
		else if (this.finalScore > ((DocResult) other).finalScore)
			return 1;
		else
			return -1;
	}
}