package nucleus.dm;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.AuthCache;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import java.io.StringReader;
import javax.net.ssl.SSLContext;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.ssl.SSLContextBuilder;

/**
 *
 * @author Administrator
 */
public class Net {
    
    private BasicCookieStore cookieStore = null;
    private final HttpHost targetHost;
    private final String protocol;
    private CloseableHttpClient httpClient = null;
    private String token = null;
    
    public static void main(String[] args) throws Exception {
    
    }
    
    public Net(String host, int port, String protocol) throws Exception {
        token = null;
        this.protocol = protocol;
        targetHost = new HttpHost(host,port,protocol);
        initializeHttpClient();
    }
    
    void clearToken() {
        token = null;
    }
    
        
    String getRawToken() {
        return token;
    }

    public void initializeHttpClient() throws Exception {
        BasicHttpClientConnectionManager bhccm; 
        SSLConnectionSocketFactory sslFactory;        
        cookieStore = new BasicCookieStore();        
        if( protocol.equalsIgnoreCase("HTTPS")) {           
            SSLContext sslcontext;           
            sslcontext = SSLContextBuilder.create().loadTrustMaterial(null, (certificate, auth) -> true).build();                            
            sslFactory = new SSLConnectionSocketFactory(sslcontext, new NoopHostnameVerifier());
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register(protocol, sslFactory).build();            
            bhccm = new BasicHttpClientConnectionManager(socketFactoryRegistry);
        }
            else             
                bhccm = new BasicHttpClientConnectionManager();

        httpClient = HttpClients.custom().setConnectionManager(bhccm).setDefaultCookieStore(cookieStore).build();
    }
    
    private void closeHttpClient() throws Exception {
        if ( httpClient != null)
         httpClient.close();
    }
    

    public void requestToken(String idmUser, String idmPassword, String consumer, String consumerPassword, String tenant) throws Exception {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(targetHost.getHostName(),targetHost.getPort()),new UsernamePasswordCredentials(idmUser,idmPassword));
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);
        // Http Context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        HttpPost httpPost = new HttpPost("/idm-service/v2.0/tokens");
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-Type", "application/json");
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> payload = new HashMap<>();
        Map<String,String> passwordCredentials = new HashMap<>();
        payload.put("tenantName", tenant);
        passwordCredentials.put("username", consumer);
        passwordCredentials.put("password", consumerPassword);
        payload.put("passwordCredentials", passwordCredentials);
        String body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);      
        HttpEntity bodyEntity = new StringEntity(body);
        httpPost.setEntity(bodyEntity);
        CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        if ( statusCode != 200 ) 
            throw new RuntimeException(String.format("HTTP Status Code: %3d%n Response: %s", statusCode, responseString)); 
        response.close();
        token = responseString;
    }  

        
    public String getHttp(String tkn, String userName, String password, String uri, String acceptContent) throws Exception {
        CloseableHttpResponse response;
        HttpGet httpGet = new HttpGet(uri);
        httpGet.addHeader("Accept", acceptContent);

        if ( tkn != null ) {
            httpGet.addHeader("X-Auth-Token",tkn);
            response = httpClient.execute(targetHost, httpGet);
        }
        else if ( userName != null && password != null) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(targetHost.getHostName(),targetHost.getPort()),new UsernamePasswordCredentials(userName,password));
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);
            // Http Context
            HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);
            //System.out.println(TARGETHOST.toURI());
            response = httpClient.execute(targetHost, httpGet, context);
        }     
        else 
            response = httpClient.execute(targetHost, httpGet);
        
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String output = EntityUtils.toString(entity, "UTF-8");
        if ( statusCode != 200 ) 
            throw new RuntimeException(String.format("HTTP Status Code: %3d%n Response: %s", statusCode, output)); 
        response.close();
        return output;
    }
    
    public String deleteHttp(String tkn, String userName, String password, String uri, String acceptContent) throws Exception {
        CloseableHttpResponse response;
        HttpDelete httpDelete = new HttpDelete(uri);
        httpDelete.addHeader("Accept", acceptContent);
            if ( tkn != null ) {
                httpDelete.addHeader("X-Auth-Token",tkn);
                response = httpClient.execute(targetHost, httpDelete);
            }
            else if ( userName != null && password != null) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(targetHost.getHostName(),targetHost.getPort()),new UsernamePasswordCredentials(userName,password));
                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicAuth = new BasicScheme();
                authCache.put(targetHost, basicAuth);
                // Http Context
                HttpClientContext context = HttpClientContext.create();
                context.setCredentialsProvider(credsProvider);
                context.setAuthCache(authCache);
                response = httpClient.execute(targetHost, httpDelete, context);
            }
            else 
                response = httpClient.execute(targetHost, httpDelete);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String output = EntityUtils.toString(entity, "UTF-8");
        if ( statusCode != 200 ) 
            throw new RuntimeException(String.format("HTTP Status Code: %3d%n Response: %s", statusCode, output)); 
        response.close();
        return output;

    }
    
    public String postHttp(String tkn, String userName, String password, String uri, String payload, String acceptContent, String content) throws Exception {
        CloseableHttpResponse response;
        HttpPost httpPost = new HttpPost(uri);     
        HttpEntity bodyEntity = new StringEntity(payload);
        httpPost.setEntity(bodyEntity);
        httpPost.addHeader("Accept", acceptContent);
        httpPost.addHeader("Content-Type", content);
        if ( tkn != null ) {
            httpPost.addHeader("X-Auth-Token",tkn);
            response = httpClient.execute(targetHost, httpPost);
        }
        else {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(targetHost.getHostName(),targetHost.getPort()),new UsernamePasswordCredentials(userName,password));
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);
            // Http Context
            HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);
            response = httpClient.execute(targetHost, httpPost, context);
        }        

        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String output = EntityUtils.toString(entity, "UTF-8");
        if ( statusCode != 200 ) 
            throw new RuntimeException(String.format("HTTP Status Code: %3d%n Response: %s", statusCode, output)); 
        response.close();
        return output;
    }
    
       public String putHttp(String tkn, String userName, String password, String uri, String payload, String acceptContent, String content) throws Exception {
        CloseableHttpResponse response = null;
        HttpPut httpPut = new HttpPut(uri);
        HttpEntity bodyEntity = new StringEntity(payload);
        httpPut.setEntity(bodyEntity);
        httpPut.addHeader("Accept", acceptContent);
        httpPut.addHeader("Content-Type", content);
        if ( tkn != null ) {
            httpPut.addHeader("X-Auth-Token",tkn);
            response = httpClient.execute(targetHost, httpPut);
        }
        else {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(targetHost.getHostName(),targetHost.getPort()),new UsernamePasswordCredentials(userName,password));
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);
            // Http Context
            HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);
            response = httpClient.execute(targetHost, httpPut, context);
            }        

        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String output = EntityUtils.toString(entity, "UTF-8");
        if ( statusCode != 200 ) 
            throw new RuntimeException(String.format("HTTP Status Code: %3d%n Response: %s", statusCode, output)); 
        response.close();
        return output;
    }
    
    public String getEslCI(String uri, String payload) throws Exception {
        CloseableHttpResponse response;
        HttpPost httpPost = new HttpPost(uri);
        String paramStr = "param=" + URLEncoder.encode(payload, "UTF-8");
        HttpEntity bodyEntity = new StringEntity(paramStr);
        httpPost.setEntity(bodyEntity);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        response = httpClient.execute(targetHost, httpPost);
        int statusCode =  response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String output = EntityUtils.toString(entity, "UTF-8");
        if ( statusCode != 200 ) 
            throw new RuntimeException(String.format("HTTP Status Code: %3d%n Response: %s", statusCode, output)); 
        response.close();
        return output;
    }
    
}


