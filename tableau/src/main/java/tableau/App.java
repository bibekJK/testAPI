package tableau;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.net.URLPermission;
import java.nio.charset.Charset;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.json.JSONObject;
import org.json.XML;
import org.xml.sax.SAXException;

import bindings.TsRequest;
import bindings.TsResponse;

public class App {
	private static Marshaller s_jaxbMarshaller;
	private static Unmarshaller s_jaxbUnmarshaller;

	public static void main(String[] args) throws IOException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(TsRequest.class, TsResponse.class);
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(new File("ts-api_2_4.xsd"));
			s_jaxbMarshaller = jaxbContext.createMarshaller();
			s_jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			s_jaxbUnmarshaller.setSchema(schema);
			s_jaxbMarshaller.setSchema(schema);
		} catch (JAXBException | SAXException ex) {
			throw new IllegalStateException("Failed to initialize the REST API");
		}

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

	}

	private static String publishWorkbook(String token, File file, String requestPayload) {
		String url = "https://us-east-1.online.tableau.com/api/2.4/sites/69e5f477-52d3-4e92-9960-c138399ec120/workbooks";
		Object credentialObject;

		URL serverURL = null;
		try {
			serverURL = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpPost target = new HttpPost(serverURL.getHost());
		RequestConfig rc = RequestConfig.custom().setCircularRedirectsAllowed(true).setRedirectsEnabled(true)
				.setConnectionRequestTimeout(30000).setSocketTimeout(30000).setConnectTimeout(30000).build();
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(rc)
				.setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpPost httpPost = new HttpPost(url);
		String boundary = UUID.randomUUID().toString();
		httpPost.setHeader("X-Tableu-Auth", token);
		MultipartEntityBuilder mb = MultipartEntityBuilder.create();
		mb.setBoundary(boundary);
		mb.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		mb.setCharset(Charset.forName("UTF-8"));
		return "";
	}

	private static String signIn(String userName, String password, String payload) throws IOException {
		String urlSignIn = "https://us-east-1.online.tableau.com/api/2.4/auth/signin";
		StringBuffer xmlString = new StringBuffer();
		HttpURLConnection connection = null;

		try {
			URL url = new URL(urlSignIn);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
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
