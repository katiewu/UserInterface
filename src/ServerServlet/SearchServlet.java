package ServerServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import snowballstemmer.PorterStemmer;
import DynamoDB.DocURL;
import DynamoDB.IDF;
import DynamoDB.InvertedIndex;
import DynamoDB.PageRank;

/**
 * Servlet implementation class SearchInterface
 */

public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String PARSER = " \t\n\r";

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

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

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println("<html><body>");
		out.println("<form action=\"\" method=\"POST\">");
		out.println("<input type=\"text\" name=\"search\">");
		out.println("<br><input type=\"submit\" value=\"Search!!!\">");
		out.println("</form>");
		out.println("</body></html>");
		System.out.println("1");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String query = request.getParameter("search").toLowerCase();
		List<String> parseQuery = stemContent(query);
		/*
		 * search query
		 */
		out.println("<html><body>");
		out.println("<h1>"+query+"</h1>");
		int size = parseQuery.size();
		
		
		// populate set
		double[] idfset = new double[size];
		HashMap<ByteBuffer, DocResult> set = new HashMap<ByteBuffer, DocResult>();
		for(int i=0;i<size;i++){
			double idf = IDF.load(parseQuery.get(i)).getidf();
			idfset[i] = idf;
			System.out.println(parseQuery.get(i));
			List<InvertedIndex> wordCollection = InvertedIndex.query(parseQuery.get(i));
			System.out.println(wordCollection.size());
			for(InvertedIndex item:wordCollection){
				ByteBuffer id = item.getId();
				if(!set.containsKey(id)){
					PageRank rankItem = PageRank.load(id);
					if(rankItem == null){
						continue;
					}
					float rank = rankItem.getRank();
					String url = DocURL.load(id.array()).getURL();
					DocResult docResult = new DocResult(id, url, size, rank);
					set.put(id, docResult);
				}
				DocResult result = set.get(id);
				float tf = item.getTF();
				Set<Integer> positionList = item.getPositions();
				result.setTF(i, tf);
				result.setPositionList(i, positionList);
			}
		}
		System.out.println("print result");
		System.out.println(set.keySet().size());
		for(ByteBuffer id:set.keySet()){
			DocResult result = set.get(id);
			double tfidf = 0;
			for(int i=0;i<size;i++){
				tfidf += result.getTF(i)*idfset[i];
			}
			result.setTFIDF(tfidf);
			out.println(result);
			out.println("<br>");
		}
		List<Map.Entry<ByteBuffer,DocResult>> entries = new LinkedList<Map.Entry<ByteBuffer,DocResult>>(set.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<ByteBuffer,DocResult>>() {
	        @Override
	        public int compare(Entry<ByteBuffer, DocResult> o1, Entry<ByteBuffer, DocResult> o2) {
	            return o1.getValue().compareTo(o2.getValue());
	        }
	    });
		for(Map.Entry<ByteBuffer, DocResult> entry:entries){
			out.println(entry.getValue());
			out.println("<br>");
			out.println(entry.getValue().getFinalRank());
			out.println("<br>");
		}


		out.println("</body></html>");

	}

//	private class ResultType {
//		String url;
//		double rank;
//
//		private ResultType(String url, double rank) {
//			this.url = url;
//			this.rank = rank;
//		}
//
//		private double getRank() {
//			return this.rank;
//		}
//	}
//
//	private class ResultTypeComparator implements Comparator<ResultType> {
//
//		@Override
//		public int compare(ResultType o1, ResultType o2) {
//			return new Double(o2.getRank()).compareTo(o1.getRank());
//		}
//
//	}

}

class DocResult{
	ByteBuffer id;
	double[] wordtf;
	Set<Integer>[] positions;
	double rank;
	String url;
	int size;
	double finalRank;
	double tfidf;
	
	DocResult(ByteBuffer id, String url, int size, double rank){
		this.id = id;
		this.url = url;
		this.size = size;
		wordtf = new double[size];
		positions = (Set<Integer>[])new Set[size];
		this.rank = rank;
		for(int i=0;i<size;i++){
			wordtf[i] = 0;
		}
	}
	
	public void setTF(int index, float tf){
		wordtf[index] = tf;
	}
	
	public void setTFIDF(double tfidf){
		this.tfidf = tfidf;
		finalRank = tfidf*rank;
	}
	
	public void setPositionList(int index, Set<Integer> position){
		positions[index] = position;
	}
	
	public void setFinalRank(double finalRank){
		this.finalRank = finalRank;
	}
	
	public double getRank(){
		return rank;
	}
	
	public double getTF(int index){
		return wordtf[index];
	}
	
	public double getFinalRank(){
		return finalRank;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(url);
		sb.append("\t");
		sb.append(rank);
		sb.append("\t");
		for(int i=0;i<size;i++){
			sb.append("word");
			sb.append(i);
			sb.append(":  ");
			sb.append(wordtf[i]);
		}
		return sb.toString();
	}
	
	public int compareTo(Object other) {
		if(this.finalRank == ((DocResult)other).finalRank) return 0;
		else if(this.finalRank > ((DocResult)other).finalRank) return 1;
		else return -1;
    }
}
