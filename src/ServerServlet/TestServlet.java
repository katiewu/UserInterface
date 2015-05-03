package ServerServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TestServlet
 */
public class TestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    public static String toBigInteger(String key) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.update(key.getBytes());
			byte[] bytes = messageDigest.digest();
			Formatter formatter = new Formatter();
			for (int i = 0; i < bytes.length; i++) {
				formatter.format("%02x", bytes[i]);
			}
			String resString = formatter.toString();
			formatter.close();
			return String.valueOf(new BigInteger(resString, 16));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return String.valueOf(new BigInteger("0", 16));
	}
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		if(path.equals("/test")){
			System.out.println(path);
			PrintWriter out = response.getWriter();
			out.println("<html>");
			out.println("<body>");
			out.println("<h1 id=\"query\">w3school</h1>");
			out.println("<a id=\"link\" href=\"http://www.w3schools.com/ajax/ajax_xmlhttprequest_create.asp\" onclick=\"sendRequest()\">http://www.w3schools.com/ajax/ajax_xmlhttprequest_create.asp</a>");
			out.println("<script>");
			out.println("function sendRequest() {"
					+ "console.log(\"receive request\");"
					+ "var target = event.target;"
					+ "var url = target.innerHTML;"
					+ "var query = document.getElementById(\"query\").innerHTML;"
					+ "var xmlhttp;"
					+ "if (window.XMLHttpRequest){"
					+ "xmlhttp = new XMLHttpRequest();"
					+ "}"
					+ "else{ xmlhttp = new ActiveXObject(\"Microsoft.XMLHTTP\");}"
					+ "var getquery = \"url=\" + url + \"&query=\" + query;"
					+ "var path = \"/UserInterface/insertquery?\" + getquery;"
					+ "xmlhttp.open(\"GET\", path, true);"
					+ "xmlhttp.send();"
					+ "}");
			out.println("</script>");
			out.println("</body>");
			out.println("</html>");
		}
		else{
			String url = request.getParameter("url");
			String query = request.getParameter("query");
			String docID = toBigInteger(url);
			System.out.println(url);
			System.out.println(query);
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
