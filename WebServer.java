import java.io.*;
import java.net.*;
import java.util.*;


public class WebServer extends Thread {
	static final String HTML_START =
			"<html>" +
			"<title>HTTP Server in java</title>" +
			"<body>";

			static final String HTML_END =
			"</body>" +
			"</html>";

Socket connectedClient = null;
BufferedReader inFromClient = null;
DataOutputStream outToClient = null;


public WebServer(Socket client) {
connectedClient = client;
}

public void run() {

try {
	
System.out.println(
  connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " connected successfully");

  inFromClient = new BufferedReader(new InputStreamReader (connectedClient.getInputStream()));
  outToClient = new DataOutputStream(connectedClient.getOutputStream());

String requestString = inFromClient.readLine();
  String headerLine = requestString;

  StringTokenizer tokenizer = new StringTokenizer(headerLine);
String httpMethod = tokenizer.nextToken();
String httpQueryString = tokenizer.nextToken();

//checking error 414
if(requestString.length()>=100){
	 sendResponse(414, " " , false);
 }

StringBuffer responseBuffer = new StringBuffer();
responseBuffer.append("<b>Welcome to Home Page!</b><BR>");

  while (inFromClient.ready())
  {
    //getting the URL
    responseBuffer.append(requestString + "<BR>");
requestString = inFromClient.readLine();
}
  
  

/* IF YOU WANT TO BLOCK A CERTAIN IP ADDRESSES DO THIS, ELSE YOU CAN COMMENT IT OUT
 */
  
 /* 
InetAddress ad2 = connectedClient.getInetAddress();
  String check = ad2.getHostAddress();

if(check.equals("127.0.0.1")){
	  sendResponse(403, " " , false);
 }

*/
  
  
//if the url is a get method
if (httpMethod.equals("GET")) {
if (httpQueryString.equals("/")) {
 // The default home page
sendResponse(200, responseBuffer.toString(), false);
} else {
//This is interpreted as a file name
String fileName = httpQueryString.replaceFirst("/", "");
fileName = URLDecoder.decode(fileName);
if (fileName.equals("test.html")){
sendResponse(302, "", false);
}
else if (new File(fileName).isFile()){
sendResponse(200, fileName, true);
}
else {
sendResponse(404, "404 - Page Not Found", false);
}
}
}
else if(httpMethod.equals("POST")){
	if (httpQueryString.equals("/")) {
		 // The default home page
		sendResponse(200, responseBuffer.toString(), false);
		} else {
		//This is interpreted as a file name
		String fileName = httpQueryString.replaceFirst("/", "");
		fileName = URLDecoder.decode(fileName);
		if (fileName.equals("test.html")){
			sendResponse(302, "", false);
			}
		else if (new File(fileName).isFile()){
		sendResponse(200, fileName, true);
		}
		else {
		sendResponse(404, "404 - Page Not Found", false);
		}
		}
}
else sendResponse(404, "404 - Page Not Found", false);
} catch (Exception e) {
e.printStackTrace();
}
}

public void sendResponse (int statusCode, String responseString, boolean isFile) throws Exception {

String statusLine = null;
String serverdetails = "Server: Java HTTPServer";
String contentLengthLine = null;
String fileName = null;
String contentTypeLine = "Content-Type: text/html" + "\r\n";
FileInputStream fin = null;

//Status 200
if (statusCode == 200)
statusLine = "HTTP/1.1 200 OK" + "\r\n";
//Status 302
else if (statusCode == 302){
	statusLine= "<b> 302 Found " +
			"</b>" + "Please visit <a> www.google.com" + "</a>"+ "\n";
	outToClient.writeBytes(statusLine);
	outToClient.close();
	return;
}
//Status 403
else if (statusCode == 403){
	statusLine= "<b> 403 - Forbidden " +
			"</b>" + "\n";
	outToClient.writeBytes(statusLine);
	outToClient.close();
	return;
}
//Status 414
else if (statusCode == 414){
	statusLine= "<b> 414 - URL length is too long " +
			"</b>" + "\n";
	outToClient.writeBytes(statusLine);
	outToClient.close();
	return;
}
//Status 404
else if (statusCode == 404)
{
	statusLine= "<b> 404 - Page Not Found" +
			"</b>" + "\n";
	outToClient.writeBytes(statusLine);
	outToClient.close();
	return;
}

if (isFile) {
fileName = responseString;
fin = new FileInputStream(fileName);
contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
contentTypeLine = "Content-Type: \r\n";
}
else {
	responseString = WebServer.HTML_START + responseString + WebServer.HTML_END;
contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
}

outToClient.writeBytes(statusLine);
outToClient.writeBytes(serverdetails);
outToClient.writeBytes(contentTypeLine);
outToClient.writeBytes(contentLengthLine);
outToClient.writeBytes("Connection: close\r\n");
outToClient.writeBytes("\r\n");

if (isFile) sendFile(fin, outToClient);
else outToClient.writeBytes(responseString);

outToClient.close();
}

public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
byte[] buffer = new byte[1024] ;
int bytesRead;

while ((bytesRead = fin.read(buffer)) != -1 ) {
out.write(buffer, 0, bytesRead);
}
fin.close();
}

public static void main (String args[]) throws Exception {

ServerSocket Server = new ServerSocket (8221, 10, InetAddress.getByName("127.0.0.1"));
System.out.println ("Waiting.");

while(true) {
Socket connected = Server.accept();
    (new WebServer(connected)).start();
}

}
}