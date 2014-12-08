package com.iextrem.web.http.useragent;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAgent {
    public static final String USER_AGENT_MOZILLA_WINDOWS = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0";
    public static final String USER_AGENT_MOZILLA_LINUX = "Mozilla/5.0 (X11; Linux i586; rv:36.0) Gecko/20100101 Firefox/36.0";

    private static final int DEFAULT_TIMEOUT = 10000;      // 10s
    private static final int DEFAULT_MAX_SIZE_RESPONSE = 1048576;    // 1Mb
    private static final int BUFFER_SIZE = 4096;

    private boolean refererAutoset;                    // enable set referer new request from last request lastVisitedUrl
    private int connectTimeout;
    private int responseSizeMax;

    public boolean isAutoRedirect() {
        return autoRedirect;
    }

    public void setAutoRedirect(boolean autoRedirect) {
        this.autoRedirect = autoRedirect;
    }

    private boolean autoRedirect;
    private Map<String, String> requestProperties;
    private CookieManager cookieManager;
    private Proxy proxy;

    /**
     * Returns cookie manager
     * @return cookie manager with current cookies
     * @see java.net.CookieManager
     */
    public CookieManager getCookieManager() {
        return cookieManager;
    }

    /**
     * Default constructor - all auto field enabled, cookies enabled, proxy disabled
     */
    public UserAgent() {
        responseSizeMax = DEFAULT_MAX_SIZE_RESPONSE;
        requestProperties = new HashMap<>();
        refererAutoset = true;
        connectTimeout = DEFAULT_TIMEOUT;
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);
        proxy = null;

    }

    /**
     * Executes GET request
     * @param url requested address
     * @param params - parameters for POST - Map key0=value0, key1=value1 ..., keyn=valuen
     * @return response with information - content, number redirect, error and etc
     * @see HttpResponse
     */
    public HttpResponse doGet(String url, Map<String, String> params) {
        url = url + "?" + convertRequestParamsToString(params);
        return doGet(url);
    }

    /**
     * Executes GET request
     * @param url requested address
     * @param params - parameters for POST - key0, value0, key1, value1 ..., keyn, valuen
     * @return response with information - content, number redirect, error and etc
     * @see HttpResponse
     */
    public HttpResponse doGet(String url, String ... params) {
        return doGet(url, convertArrayToHashMap(params));
    }

    /**
     * Executes GET request
     * @param url requested address
     * @return response with information - content, number redirect, error and etc
     * @see HttpResponse
     */
    public HttpResponse doGet(String url) {
            HttpResponse httpResponse = new HttpResponse(url);
            HttpURLConnection connection = null;
            try {
                connection = prepareConnection(url);
                // make connect and get httpResponse
                connection.connect();
                httpResponse.setCode(connection.getResponseCode());
                httpResponse.setHeader(connection.getHeaderFields());
                httpResponse.setMessage(connection.getResponseMessage());
                readFromConnection(connection, httpResponse);
                if (refererAutoset) requestProperties.put("Referer", url);
            } catch (SocketTimeoutException ex) {
                httpResponse.setStatus(HttpResponse.Status.ERROR_TIMEOUT);
            } catch (IllegalArgumentException ex) {
                httpResponse.setStatus(HttpResponse.Status.ERROR_BAD_URL);
            } catch (MalformedURLException ex) {
                httpResponse.setStatus(HttpResponse.Status.ERROR_MALFORMED_URL);
            } catch (IOException ex) {
                httpResponse.setStatus(HttpResponse.Status.ERROR_IO);
            } finally {
                if (connection !=null) {
                    connection.disconnect();
                }
            }
            return httpResponse;
        }

    /**
     * Executes POST request
     * @param url requested address
     * @param params - parameters for POST - key0, value0, key1, value1 ..., keyn, valuen
     * @return response with information - content, number redirect, error and etc
     * @see HttpResponse
     */
    public HttpResponse doPost(String url, String ... params) {
        return doPost(url, convertArrayToHashMap(params));
    }

    /**
     * Executes POST request
     * @param url requested address
     * @param params - parameters for POST - Map key0=value0, key1=value1 ..., keyn=valuen
     * @return response with information - content, number redirect, error and etc
     * @see HttpResponse
     */
    public HttpResponse doPost(String url, Map<String, String> params) {
        return doPost(url, convertRequestParamsToString(params));
    }

    /**
     * Executes POST request
     * @param url requested address
     * @param params - parameters for POST String - "key0=value0&key1=value1&...keyn=valuen
     * @return response with information - content, number redirect, error and etc
     * @see HttpResponse
     */
    public HttpResponse doPost(String url, String params) {
        HttpResponse httpResponse = new HttpResponse(url);
        HttpURLConnection connection = null;
        try {
            connection = prepareConnection(url);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(params);
            out.flush();
            out.close();
            httpResponse.setCode(connection.getResponseCode());
            httpResponse.setHeader(connection.getHeaderFields());
            httpResponse.setMessage(connection.getResponseMessage());
            readFromConnection(connection, httpResponse);
            if (refererAutoset) requestProperties.put("Referer", url);
        } catch (SocketTimeoutException ex) {
            httpResponse.setStatus(HttpResponse.Status.ERROR_TIMEOUT);
        } catch (IllegalArgumentException ex) {
            httpResponse.setStatus(HttpResponse.Status.ERROR_BAD_URL);
        } catch (MalformedURLException ex) {
            httpResponse.setStatus(HttpResponse.Status.ERROR_MALFORMED_URL);
        } catch (IOException ex) {
            httpResponse.setStatus(HttpResponse.Status.ERROR_IO);
        } finally {
            if (connection !=null) {
                connection.disconnect();
            }
        }
        return httpResponse;
    }

    /**
     * clear all request properties
     */
    public void clearAllRequestProperties(){
        requestProperties.clear();
    }

    /**
     * set request property
     * @param key key for property
     * @param value value for property
     * @return old value for property
     */
    public String setRequestProperty(String key, String value){
        return requestProperties.put(key, value);
    }


    /**
     * Set maximum size for content response. Too big content will be cut
     * @param responseSizeMax maximum size for content response
     */
    public void setResponseSizeMax(int responseSizeMax) {
        this.responseSizeMax = responseSizeMax;
    }


    /**
     * Enable cookies storage and accept cookies
     */
    public void enableCookies() {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    /**
     * Disable cookies storage
     */
    public void disableCookies() {
        cookieManager.getCookieStore().removeAll();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_NONE);
    }

    /**
     * Enable auto set for referer field in request properties "Referer"=url
     */
    public void enableRefererAutoset() {
        this.refererAutoset = true;
    }

    /**
     * Disable auto set for referer field in request properties "Referer"=url
     */
    public void disableRefererAutoset() {
        this.refererAutoset = false;
    }

    /**
     * Set authentication proxy for connection
     * @param address address proxy
     * @param port port proxy
     * @param login login for user proxy
     * @param password password for user proxy
     */
    public void setProxyAuthentication(String address, int port, final String login, final String password){
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(address, port));
        Authenticator authenticator = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return (new PasswordAuthentication(login, password.toCharArray()));
            }
        };
        Authenticator.setDefault(authenticator);
    }

    /**
     * Set anonymous proxy for connection
     * @param address address proxy
     * @param port port proxy
     */
    public void setProxyAnonymous(String address, int port) {
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(address, port));
        Authenticator.setDefault(null);
    }

    /**
     * Set no proxy
     */
    public void setProxyNone() {
        proxy = null;
        Authenticator.setDefault(null);
    }


    /**
     * Set timeout for connection
     * @param time timeout in milliseconds
     */
    public void setConnectTimeout(int time) {
        this.connectTimeout = time;
    }

    /**
     * Delete all cookies
     */
    public void deleteCookies() {
        cookieManager.getCookieStore().removeAll();
    }

    private String regexpFindString(String patternString, String text) {
        String result = "";
        Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            if ( matcher.groupCount()>0 ) {
                result = matcher.group(1);
            }
        }
        return result;
    }

    /* from address select base address, "http://example.com/test.php" return "http://example.com" */
    private String getUrlBase(String url) {
        String patternString = "(.+)/.*?";
        return regexpFindString(patternString, url);
    }

    /* read data from connection and write to HttpResponse*/
    private void readFromConnection(HttpURLConnection connection, HttpResponse httpResponse) throws IOException  {
        int dataSize = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            InputStream inputStream = connection.getInputStream();
            int size = 0;
            while ( ((size = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) && (dataSize < responseSizeMax)) {
                if ((dataSize + size) > responseSizeMax) {
                    httpResponse.writeContent(buffer, (responseSizeMax - dataSize));
                    dataSize = responseSizeMax;
                } else {
                    dataSize += size;
                    httpResponse.writeContent(buffer, size);
                }
            }
        }
        catch (IOException e) {
            // get info about Http errors (404,500,etc)
            InputStream err;
            if (connection != null) {
                err = connection.getErrorStream();
            } else {
                throw e;
            }
            if (err == null) {
                throw e;
            }
            int length = 0;
            while ( (length = err.read(buffer, 0, BUFFER_SIZE)) != -1) {
                httpResponse.writeContent(buffer, length);
            }

        }
    }


    /* make HttpURLConnection and prepare for connection - set cookies, proxy, params and etc.      */
    private HttpURLConnection prepareConnection(String url) throws IOException {
        HttpURLConnection connection;
        //URL url = new URL(url);
        if (proxy == null) {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
        } else {
            connection = (HttpURLConnection) (new URL(url)).openConnection(proxy);
        }
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(connectTimeout);
        if (requestProperties != null) {
            for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        connection.setInstanceFollowRedirects(autoRedirect);
        return connection;
    }

    /* make HashMap from Array String key0,value0, key1,value1  */
    private Map<String, String> convertArrayToHashMap(String... params){
        Map<String, String> res = new HashMap<>();
        if (params.length > 1) {
            for (int i=0; i < params.length/2; i++) {
                String key = params[i];
                String value = params[i+1];
                res.put(key, value);
            }
        }
        return res;
    }

    /* make params string for output stream from Map params */
    private String convertRequestParamsToString(Map<String, String> params) {
        StringBuilder paramsString = new StringBuilder();
        if (params != null) {
            int n = 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (n > 0) paramsString.append("&");
                try {
                    paramsString.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    paramsString.append("=");
                    paramsString.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // it never happen
                    e.printStackTrace();
                }
                n++;
            }
        }
        return paramsString.toString();
    }
}