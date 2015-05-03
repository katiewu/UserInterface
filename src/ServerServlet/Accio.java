package ServerServlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;

/**
 * Servlet implementation class Accio
 */

public class Accio extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Accio() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.write("<!DOCTYPE html><html>"
				+ "<head>"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
				+ "<link rel=\"stylesheet\" href=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css\">"
				+ "<style>"
				+ "body {"
					+ "background: url('http://localhost:8081/JspTest/hp.png');"
					+ "background-size: 1280px 800px;"
					+ "background-repeat:no-repeat;"
					+ "padding-top: 150px;"
				+ "}"
				+ "@media (max-width: 980px) {"
				+ "body {"
				+ "padding-top: 0;"
				+ "}"
				+ "}"
				+ "</style>"
				+ "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script>"
				+ "<script src=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js\"></script>"
				+ "<title>Accio Search Engine</title>"
				+ "</head>"
					+ "<body>"
					+ "<div class = \"row\"></div>"
					+ "<div class=\"container\">"
						+ "<h1 class = \"text-center\">Accio</h1>"
						+ "<div class=\"row\">"
							+ "<form role=\"form\" action=\"/UserInterface/Accio\" method=\"post\">"
								+ "<div class=\"col-md-3\">"
								+ "</div>"
								+ "<div class=\"col-md-6\">"
									+ "<input type=\"text\" id=\"inputdefault\" class=\"form-control\" name=\"phrase\" placeholder=\"What you are looking for?\">"
								+ "</div>"
								+ "<div class=\"col-md-3\">"
									+ "<button type=\"submit\" class=\"btn btn-info\">"
										+ "<span class=\"glyphicon glyphicon-flash\"></span> search"
									+ "</button>"
								+ "</div>"
							+ "</form>"
						+ "</div>"
	
					+ "</div>"
					+ "</body> "
				+ "</html>");
		out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String phrase = request.getParameter("phrase");
		String html = "";
		try {
			html = wiki(phrase);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		out.write("<!DOCTYPE html>"
				+ "<html lang=\"en\">"
					+ "<head>"
						+ "<title>Bootstrap Example</title>"
						+ "<meta charset=\"utf-8\">"
						+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
						+ "<link rel=\"stylesheet\" href=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css\">"
						+ "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script>"
						+ "<script src=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js\"></script>"
						+ "<style>"
							+ "body {"
								+ "padding-top: 15px;"
							+ "}"
						+ "</style>"
					+ ""
					+ "</head>"
					+ "<body>"
						+ "<div class=\"container\">"
							+ "<div class=\"row\">"
								+ "<form role=\"form\" action=\"/UserInterface/Accio\" method=\"post\">"
									+ "<div class=\"col-md-1\">"
										+ "<h3>Accio</h3>"
									+ "</div>"
									+ "<div class=\"col-md-6\">"
										+ "<input type=\"text\" id=\"inputdefault\" class=\"form-control\" name=\"phrase\" placeholder=\"What you are looking for?\">"
									+ "</div>"
									+ "<div class=\"col-md-3\">"
										+ "<button type=\"submit\" class=\"btn btn-info\">"
											+ "<span class=\"glyphicon glyphicon-flash\"></span> search"
										+ "</button>"
									+ "</div>"
								+ "</form>"
							+ "</div>"
							+ "<h2>"
							+ "important things need to be said three times!"
							+ "</h2>"
							+ "<div class=\"col-md-8\">"
								+ "<ul class=\"list-group\">"
									+ "<li class=\"list-group-item\">"
									+ phrase
									+ "</li>"
									+ "<li class=\"list-group-item\">"
									+ phrase
									+ "</li>"
									+ "<li class=\"list-group-item\">"
									+ phrase
									+ "</li>"
								+ "</ul>"
							+"</div>"
							+ "<div class=\"well col-md-4\">"
								+ html
							+ "</div>"
						+ "</div>"
					+ "</body>"
				+ "</html>");

		out.flush();
	}
	
	public String wiki(String phrase) throws Exception{
		String USER_AGENT = "cis455crawler";
	
		String url = "http://en.wikipedia.org/w/api.php?action=query&prop=revisions&titles=computer&rvprop=content&format=json&rvsection=0&rvparse=1";
		String html = "";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
//		System.out.println("\nSending 'GET' request to URL : " + url);
//		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
			response.append("\n");
		}
		in.close();
 
		//print result
//		System.out.println(response.toString());
		
		
		String jsonresult = response.toString();
		JSONObject jsonObj = new JSONObject(jsonresult);
		boolean exist = false;
		try{
			JSONObject result = jsonObj.getJSONObject("query").getJSONObject("pages").getJSONObject("-1");
		}
		catch(Exception e){
			exist = true;
		}
		if(exist){
			JSONObject page = jsonObj.getJSONObject("query").getJSONObject("pages");
			String content = page.toString();
			int i = 2;
			while(content.charAt(i) != '"'){
				i++;
			}
			String pageid = content.substring(2, i);
			JSONArray revisions = jsonObj.getJSONObject("query").getJSONObject("pages").getJSONObject(pageid).getJSONArray("revisions");
			html = revisions.getJSONObject(0).getString("*");
			html = html.replaceAll("<a href=\"/wiki/", "<a href=\"http://en.wikipedia.org/wiki/");
			html = html.replaceAll("src=\"//upload", "src=\"http://upload");
			
		}
		return html;
	}
	

}
