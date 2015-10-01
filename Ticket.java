import javax.net.ssl.*; 
import java.io.*; 
import java.net.URL; 
import java.security.*; 
import java.security.cert.CertificateException; 
 
public class Ticket 
{ 
	public static void main(String[] args) 
	{ 
		String xrfkey = "7rBHABt65vFflaZ7"; //Xrfkey to prevent cross-site issues 
		String host = "QlikSenseServerHostName"; //Enter the Qlik Sense Server hostname here 
		String vproxy = "VirtualProxyPrefix"; //Enter the prefix for the virtual proxy configured in Qlik Sense Steps Step 1 
		try 
		{

			/************** BEGIN Certificate Acquisition **************/ 
			String certFolder = "c:\\javaTicket\\"; //This is a folder reference to the location of the jks files used for securing ReST communication 
			String proxyCert = certFolder + "client.jks"; //Reference to the client jks file which includes the client certificate with private key 
			String proxyCertPass="secret"; //This is the password to access the Java Key Store information 
			String rootCert = certFolder + "root.jks"; //Reference to the root certificate for the client cert. Required in this example because Qlik Sense certs are used. 
			String rootCertPass = "secret"; //This is the password to access the Java Key Store information
			/************** END Certificate Acquisition **************/
 
			/************** BEGIN Certificate configuration for use in connection **************/
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(new File(proxyCert)), proxyCertPass.toCharArray()); 
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); 
			kmf.init(ks, proxyCertPass.toCharArray()); 
			SSLContext context = SSLContext.getInstance("SSL"); 
			KeyStore ksTrust = KeyStore.getInstance("JKS"); 
			ksTrust.load(new FileInputStream(rootCert), rootCertPass.toCharArray()); 
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); 
			tmf.init(ksTrust); 
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null); 
			SSLSocketFactory sslSocketFactory = context.getSocketFactory();
			/************** END Certificate configuration for use in connection **************/


			/************** BEGIN HTTPS Connection **************/
			System.out.println("Browsing to: " + "https://" + host + ":4243/qps/" + vproxy + "/ticket?xrfkey=" + xrfkey); 
			URL url = new URL("https://" + host + ":4243/qps/" + vproxy + "/ticket?xrfkey=" + xrfkey); 
			HttpsURLConnection connection = (HttpsURLConnection ) url.openConnection(); 
			connection.setSSLSocketFactory(sslSocketFactory); 
			connection.setRequestProperty("x-qlik-xrfkey", xrfkey); connection.setDoOutput(true); 
			connection.setDoInput(true); 
			connection.setRequestProperty("Content-Type","application/json"); 
			connection.setRequestProperty("Accept", "application/json"); 
			connection.setRequestMethod("POST");
			/************** BEGIN JSON Message to Qlik Sense Proxy API **************/


			String body = "{ 'UserId':'" + args[0] + "','UserDirectory':'" + args[1] +"',";
			body+= "'Attributes': [],"; body+= "}"; System.out.println("Payload: " + body);
			/************** END JSON Message to Qlik Sense Proxy API **************/


			OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream()); 
			wr.write(body); 
			wr.flush(); //Get the response from the QPS BufferedReader 
			in = new BufferedReader(new InputStreamReader(connection.getInputStream())); 
			StringBuilder builder = new StringBuilder(); 
			String inputLine; 
			while ((inputLine = in.readLine()) != null) 
			{ 
				builder.append(inputLine); 
			} 
			in.close(); 
			String data = builder.toString(); 
			System.out.println("The response from the server is: " + data);
			/************** END HTTPS Connection **************/
		} 
		catch (KeyStoreException e) { e.printStackTrace(); } 
		catch (IOException e) { e.printStackTrace(); } 
		catch (CertificateException e) { e.printStackTrace(); } 
		catch (NoSuchAlgorithmException e) { e.printStackTrace(); } 
		catch (UnrecoverableKeyException e) { e.printStackTrace(); } 
		catch (KeyManagementException e) { e.printStackTrace(); } 
	} 
}