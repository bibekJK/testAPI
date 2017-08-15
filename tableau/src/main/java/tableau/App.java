package tableau;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.json.JSONObject;
import org.json.XML;



public class App {
	
	public static void main(String[] args) throws IOException {

		String userName = "karki@unlv.nevada.edu";
		String password = "Marina@82";
		String signinFile = "C:\\Users\\Owner\\Google Drive\\Tableau\\Resources\\payloadSignin.xml";
		// String projectId = "";
		String workbookName = "World Indicators-En-US.twbx";
		

		// get file
		File file = new File("C:\\Users\\Owner\\Google Drive\\Tableau\\Resources\\World Indicators-En-US.twbx");

		// create payload
		String payloadSignin = createPayload(signinFile);
		// return;
		// call signin, get token
		String token = signIn(userName, password, payloadSignin);
		
		publishWorkbook(userName, password,token, file);

	}

	private static void publishWorkbook(String userName, String password, String token, File file) throws IOException {
		String url = "https://us-east-1.online.tableau.com/api/2.4/sites/69e5f477-52d3-4e92-9960-c138399ec120/workbooks";
		String projectId = "e61be408-6b51-451d-bd00-cb3bab4527a1";
		
		StringBuffer xmlString = new StringBuffer();
		String boundaryString = UUID.randomUUID().toString();
		String bString = boundaryString.replaceAll("-", "");
	
			String fileString = FileUtils.readFileToString(file);

		
		String body =
			    "--" + bString + "\r\n" +
			    "Content-Disposition: name=\"request_payload\"\r\n" +
			    "Content-Type: text/xml\r\n\r\n" +
			    "<tsRequest>\r\n" +
			    "  <workbook name=\"World Indicators-En-US\" showTabs=\"false\" >\r\n" +
			    "    <connectionCredentials name=\"" + userName + "\" password=\"" + password +"\" /> \r\n" +
			    "    <project id=\"" + projectId + "\"/>\r\n" +
			    "  </workbook>\r\n" +
			    "</tsRequest>\r\n" +
			    "\r\n--" + bString + "\r\n" +
			    "Content-Disposition: name=\"tableau_workbook\"; filename=\"" + file.getName() + "\"\r\n" +
			    "Content-Type: application/octet-stream\r\n\r\n" +
			    fileString + "\r\n\r\n" +
			    "--" + bString + "--";
		
		ByteBuffer bodyByteBuffer = Charset.forName("UTF-8").encode(body);
		byte[] bodyBytes = new byte[bodyByteBuffer.remaining()];
		bodyByteBuffer.get(bodyBytes);

		int len= bodyBytes.length;
		
		
		//HTTP URL Connection
		HttpURLConnection conn = null;
		
		try {
			URL urlPost = new URL(url);
		
			conn = (HttpURLConnection) urlPost.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(true);
			conn.setRequestMethod("POST");

			conn.setRequestProperty("X-Tableau-Auth", token);
			conn.setRequestProperty("Content-Type", "multipart/mixed; boundary="+bString);
			conn.setRequestProperty("Content-length", Integer.toString(len));
			
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.write(bodyBytes);
			wr.flush();
			wr.close();
			
			System.out.println("Server Response Code :"+conn.getResponseCode());
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;

			while ((line = br.readLine()) != null) {
				xmlString.append(line + "\n");
			}

			br.close();
			conn.disconnect();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private static String signIn(String userName, String password, String payload) throws IOException {
		String urlSignIn = "https://us-east-1.online.tableau.com/api/2.4/auth/signin";
		StringBuffer xmlString = new StringBuffer();
		HttpURLConnection connection = null;

		try {
			URL url = new URL(urlSignIn);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(true);
			connection.setRequestMethod("POST");

		
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			writer.write(payload);
			writer.flush();
			writer.close();

			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;

			while ((line = br.readLine()) != null) {
				xmlString.append(line + "\n");
			}

			br.close();
			connection.disconnect();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return xmlToJson(xmlString.toString());
	}

	private static String xmlToJson(String string) {

		JSONObject jsonObject = XML.toJSONObject(string);
		JSONObject tsResponse = (JSONObject) jsonObject.get("tsResponse");
		JSONObject credentials = (JSONObject) tsResponse.get("credentials");
		String token = (String) credentials.get("token");
		return token;
	}

	private static String createPayload(String filename) {
		File xmlSignInFile = new File(filename);
		return xmlToString(xmlSignInFile);

	}

	private static String xmlToString(File xmlSignInFile) {
		Reader fileReader = null;
		try {
			fileReader = new FileReader(xmlSignInFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedReader bufReader = new BufferedReader(fileReader);
		StringBuffer sb = new StringBuffer();
		String line = null;
		while (true) {
			try {
				line = bufReader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (line != null) {
				sb.append(line);
				sb.append("\n");
			} else
				break;
		}

		String xmlToString = sb.toString();

		try {
			bufReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return xmlToString;
	}
}
