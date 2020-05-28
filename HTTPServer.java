import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;


public class HTTPServer {

	static String getServerTime(int GMT) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		String gmtZone;
		if(GMT > 0)
			gmtZone = "+"+String.valueOf(GMT);
		else 
			gmtZone = String.valueOf(GMT);

		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"+gmtZone));
		return dateFormat.format(calendar.getTime());
	}

	static String getTime(String Timezone) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
			"HH:mm:ss z", Locale.US);
		
		dateFormat.setTimeZone(TimeZone.getTimeZone(Timezone));
		return dateFormat.format(calendar.getTime());
	}
	
	public static void main(String[] args) throws Exception {
		Random r = new Random();
		//start server on random port
		int port = r.nextInt(2000-1000)+1 + 1000;
		ServerSocket serverSocket = new ServerSocket(port);
		System.err.println("Server running on : " + port);

		// repeatedly wait for connections, and process
		while (true) {
			Socket clientSocket = serverSocket.accept();
			System.err.println("Client Connected");

			//create reader and writer to clientSocket
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

			//get the timezone from the GET parameter
			String timeZoneToCheck = null;

			String req;
			while ((req = in.readLine()) != null) {
				System.out.println(req);
				if(req.contains("GET")){
					String reqParts[] = req.split(" ");
					timeZoneToCheck = reqParts[1].substring(1).replace("-", "/");
				}
				if (req.isEmpty()) {
					break;
				}
			}

			//set headers
			out.write("HTTP/1.0 200 OK\r\n");
			//get server time (server situated in GMT+2)
			out.write("Date: "+getServerTime(2)+"\r\n");
			out.write("Content-Type: text/html\r\n");
			out.write("\r\n");

			//force HTTP refresh every second
			out.write("<head>");
			out.write("<meta http-equiv=\"refresh\" content=\"1\">");
			out.write("</head>");

			//start with page content
			out.write("<TITLE>TIME | COS332 PRAC3</TITLE>");
			out.write("<h2><strong>South-Africa: </strong>"+getTime("Africa/Johannesburg")+"</h2>");

			//get all available Timezones in the java.util.TimeZone package 
			List<String> timeZoneList = Arrays.asList(TimeZone.getAvailableIDs());
			//don't display the other city if no GET parameter is set
			if(timeZoneList.contains(timeZoneToCheck))
				out.write("<h3><strong>"+timeZoneToCheck+": </strong>"+getTime(timeZoneToCheck)+"</h3>");

			//print a list of available cities to view
			out.write("<p><strong>Available Cities to View</strong><br/>Click on a city to view its current time.</p>");
			for(int i = 0; i < timeZoneList.size(); i++){
				String timezone = timeZoneList.get(i);
				out.write("<a href=\""+timezone.replace("/", "-")+"\">"+timezone+"</a><br/>");
			}

			//close everything
			System.err.println("Client Disconnected");
			out.close();
			in.close();
			clientSocket.close();
		}
	}
}