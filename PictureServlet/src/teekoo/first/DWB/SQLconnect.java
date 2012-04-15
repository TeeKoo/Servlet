package teekoo.first.DWB;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.imageio.ImageIO;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;


/**Class that connects to Database and to an FTP server**/
public class SQLconnect{
	  Connection conn = null;		
	  String serverName = "kalorikulutus.fi";	//Pretty much self explanatory. Strings for all the fields needed to establish a connection to SQL server.
	  String dbName = "kaloriku_PICS";
	  String url = "jdbc:mysql://" + serverName + "/" + dbName;
	  String driver = "com.mysql.jdbc.Driver";
	  String userName = "kaloriku_teekoo"; 			//username and password
	  String password = "password";					//password
	  private String urlGo;							//String to get url
	  int row;
	  
	  String server="kalorikulutus.fi";			//Also self explanatory, Strings for FTP connection.
		String userNameftp="kaloriku";
		String pw="password";

		String link;
		
		InputStream fis = null;				//inputstream to get the picture from the URL
		SQLconnect sqlc;					
		URL watermark;						//URL for the water mark. (Self hosted picture)
		BufferedImage imgBuffered, imgWater;	//Buffered image objects to get the picture from Inputstream.
		BufferedImage img;			
		int imgW, imgH;						//int fields to get the measurements from the main image, so that the watermark picture could be place in the middle
		FTPClient client;					//client for FTP server

		ArrayList<String> listForSQL = new ArrayList<String>();	//an arraylist to store info from the database (This will be printed after SQL and FTP files have been handled.
		
		/**Method to establish SQL connection to a database and store data**/
	public void establishSQLConnection(){
		
  try {
  Class.forName(driver).newInstance();
  conn = DriverManager.getConnection(url,userName,password);
  } catch (Exception e) {
  e.printStackTrace();
  }
	}
	
	public void pushInfo(){
		 Statement stmt;
	        try
	        {
	            stmt = conn.createStatement();
	            getDBCount(stmt);
	            stmt.executeUpdate("INSERT INTO pic_info " +
	                 "values('"+getDBCount(stmt)+"','"+getUrlGo()+"', '"+getDate()+"')");		//storing the data.
	            conn.close();
	          
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
	}
		/**Simple getter for URL**/
	public String getUrlGo() {
		return urlGo;
	}
		/**Simple setter for URL that converts url to string**/
	public void setUrlGo(URL urlGo) {
		String newURL = urlGo.toString();
		this.urlGo = newURL;
	}

			/**method to get the line count from SQL database. This method also stores all the data to an arraylist that will later get printed when the program is done uploading files.**/
	public int getDBCount(Statement stmt) throws SQLException{
	    ResultSet resultSet = stmt.executeQuery("SELECT * FROM pic_info");
	    while (resultSet.next()) {
            String columnURL = resultSet.getString("pic_url");
            String columnDate= resultSet.getString("pic_date");
            listForSQL.add(columnDate +  "&nbsp;&nbsp;&nbsp;&nbsp;" +"  "+"<a href='"+ columnURL + "'> " + columnURL + "</a>");
    	    row = resultSet.getInt(1);
	    	}

		return row;
	}
		/**method to get date to be stored in SQL database**/
	public String getDate(){
		DateFormat formatD = new SimpleDateFormat("MM/dd/yyyy");
		java.util.Date today = Calendar.getInstance().getTime();    
		String dateRightNow = formatD.format(today);
		return dateRightNow;
		
	}
	
			/**Method that uploads the file to a FTP server.**/
	public void uploadStuff(InputStream in) throws SocketException, IOException, SQLException{
		client = new FTPClient();
		client.connect(server);
		client.enterLocalPassiveMode();
		client.login(userNameftp, pw);
		client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);  //Changed FTP filetype to binary so the picture wont be corrupted.
		client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
		sqlc = new SQLconnect();
		String fileString = "/public_html/waterMarkedPic"+row;  //filestring that names the file and decides to which file the picture will be stored.
		link = "http://kalorikulutus.fi/waterMarkedPic"+row;	//public link for the image after it's watermarked.

		client.storeFile(fileString, fis);
		client.logout();
		client.disconnect();

	}
	
	/**Method that gets Inputstream object, which contains a saved picture from URL.
	 * This method also adds the watermark to the picture and converts it to a Outputstream object, that will be sent to an FTP server. 
	 * **/
	public void downloadStuff(String urli) throws MalformedURLException, IOException, SQLException{
		InputStream input = new URL(urli).openStream();
		img = ImageIO.read(input);
		watermark = new URL("http://servlet.kalorikulutus.fi/watermark.png");
		imgWater = ImageIO.read(watermark);
		Graphics2D bufferedImage = img.createGraphics();		//converted to a graphics object so that the picture can be manipulated.
		getImageHW();
	    bufferedImage.drawImage(imgWater, imgW, imgH, null);	//added the watermark to the middle of the picture.
		
	    ByteArrayOutputStream os = new ByteArrayOutputStream();			//The picture needs to be turned into a bytearray before it can be stored to an Outputstream.
	    ImageIO.write(img, "png", os);
	    fis = new ByteArrayInputStream(os.toByteArray());
	    
		uploadStuff(fis);					
	}
	/**Simple method to make sure the watermark is in the middle of the picture**/
	private void getImageHW(){
		imgH = (img.getHeight()/2)-15;
		imgW = (img.getWidth()/2)-15;
		
	}

}