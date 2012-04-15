package teekoo.first.DWB;

import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * This program is a Servlet that takes in the users URL from a picture and adds watermark to it.
 * The program then uploads it to a FTP server and gives you a public link to a picture.
 * After the transaction, the serlet stores the link and date
 * to a database, and finally prints the previous links and the added date.
 * @author Taneli Kärkkäinen
 * **/

/**
 * Main servlet. This class takes in the form from the site and handles it.
 */
@WebServlet("/DWBtry")
public class DWBtry extends HttpServlet {
	private static final long serialVersionUID = 1L;
		
		SQLconnect sqlc;		//SQLconnect object for methods.
       private String URL_GET;	//String field for URL address
       URL getURL;				//URL field for the actual URL
       
    public DWBtry() throws IOException {
        super();
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}
		/**Method which takes in the URL and handles it**/
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    	
		URL_GET = request.getParameter("inputGet");
		try{
			sqlc = new SQLconnect();			//Main logic on these 6 lines. 
			getURL = new URL(URL_GET);
			sqlc.establishSQLConnection();
			sqlc.setUrlGo(getURL);
			sqlc.pushInfo();
			sqlc.downloadStuff(getURL.toString());
			
				
			response.getWriter().print("<html><body>Onnistui!</br></br> "+ "<a href='"+sqlc.link+"'>Kuvan linkki</a><br/><br/>");	//Prints the link for the new picture and the whole database contents
			response.getWriter().print("<p>Ennen lisätyt&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Linkki</p>");
			for(int i=0;i<sqlc.listForSQL.size();i++){
				response.getWriter().println(sqlc.listForSQL.get(i));
				response.getWriter().print("<br/>");
			}
			response.getWriter().print("</body></html>");

		}
		//handles all the needed exceptions. (Bad url, SQL or FTP connection problems etc.)
		catch(Exception e)
		{
			System.out.println(e);
			response.getWriter().print(URL_GET + " was not a valid URL or an image, or SQL server/FTP server was offline.\n" + e);
			
		}
		

	}
	

	


}

