package com.iextrem.web.http.useragent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class HttpResponse {

    public enum Status {NO_ERROR, ERROR_4xx, ERROR_BAD_URL, ERROR_IO, ERROR_TIMEOUT, ERROR_MALFORMED_URL, ERROR_5xx}
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String GZIP = "gzip";
    private static final String DEFAULT_CHARSET = "utf-8";


    private int code;
    private Status status;
    private String message;
    private String url;
    private Map<String, List<String>> header;
    private List<Byte> content;

    public HttpResponse() {
        content = new ArrayList<>();
    }

    public HttpResponse(String url) {
        this.url = url;
        content = new ArrayList<>();
    }

    /**
     * Return content length
     * @return content length
     */
    public int getContentLength() {
        return content.size();
    }

    /**
     * Read content from inner storage
     * @param buffer outer buffer for data
     * @param off offset from begin content data
     * @param len length retrieved data
     * @return number bytes was read from content
     */
    public int readContent(byte[] buffer, int off, int len) {
        int size = content.size();
        if (off > size) return -1;
        if ( (off + len) > size) {
            len = size - off;
        }
        if (buffer.length < len) return -1;
        for (int i = 0; i < len; i++) {
            buffer[i] = content.get(off + i);
        }
        return len;
    }

    /**
     * Get all content as byte[]
     * @return all content as byte[]
     */
    public byte[] getContent() {
        int size = content.size();
        byte[] buffer = new byte[size];
        for (int i = 0; i < size; i++) {
            buffer[i] = content.get(i);
        }
        return buffer;
    }

    /**
     * Write buffer to content - add to the end
     * @param buffer buffer with data
     * @param length length data in buffer
     * @return true if success
     */
    public boolean writeContent(byte[] buffer, int length) {
        if (buffer.length < length) return false;
        for (int i = 0; i < length; i++) {
            content.add(buffer[i]);
        }
        return true;
    }

    /**
     * Get header field with key
     * @param key key for retrieve value
     * @return List string values
     */
    public List<String> getHeaderField(String key) {
        if (header != null) {
            return header.get(key);
        }
        return new ArrayList<>();
    }

    /**
     * Get header field with key - single string
     * @param key key for retrieve value
     * @return first string value
     */
    public String getHeaderFieldOneValue(String key) {
        String value = "";
        if (header != null) {
            List<String> list = header.get(key);
            if ((list != null) && (list.size() > 0)) {
                value = list.get(0);
            }
        }
        return value;
    }

    /**
     * Get content as Text - charset select auto
     * @return content decoded as text
     * @throws UnsupportedEncodingException
     */
    public String getAsText() throws UnsupportedEncodingException {
        String charset = DEFAULT_CHARSET;
        // get charset from header
        String contentType = getHeaderFieldOneValue(CONTENT_TYPE);
        String[] split = contentType.split("=");
        if (split.length > 1) {
            charset = split[1];
        }
        return getAsText(charset, 0);
    }

    /**
     * Get content as Text
     * @param charset charset for decoding content
     * @return content decoded as text
     * @throws UnsupportedEncodingException
     */
    public String getAsText(String charset) throws UnsupportedEncodingException {
        return getAsText(charset, 0);
    }

    /**
     * Get content as Text
     * @param charset charset for decoding content
     * @param length max length for returned text
     * @return content decoded as text
     * @throws UnsupportedEncodingException
     */
    public String getAsText(String charset, int length) throws UnsupportedEncodingException {
        String contentEncoding = getHeaderFieldOneValue(CONTENT_ENCODING);
        StringBuilder text = new StringBuilder();
        byte[] buffer = new byte[content.size()];
        for (int i = 0; i < content.size(); i++) {
            buffer[i] = content.get(i);
        }
        InputStream in;
        BufferedReader bf;
        try {
            in = new ByteArrayInputStream(buffer);
            if (GZIP.equalsIgnoreCase(contentEncoding)) {
                in = new GZIPInputStream(in);
            }
            bf = new BufferedReader(new InputStreamReader(in, charset));
            String line;
            while ((line = bf.readLine()) != null) {
                text.append(line);
                text.append("\n");
                if ((length > 0 ) && (text.length() > length)) break;
            }
            in.close();
        } catch (UnsupportedEncodingException e) {
            throw e;
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        String res = text.toString();
        if (length > 0 ) res = res.substring(0, length);
        return  res;
    }

    /**
     * Clean content
     */
    public void cleanContent(){
        content.clear();
    }

    /**
     * Check for error after execute request
     * @return true if no error
     */
    public boolean isNoError() {
        return status == Status.NO_ERROR;
    }

    /**
     * Get code executed request
     * @return code response
     */
    public int getCode() {
        return code;
    }

    /**
     * Set code executed request
     * @param code code executed request
     */
    public void setCode(int code) {
        this.code = code;
        switch (code/100) {
            case 5:
                status = Status.ERROR_5xx;
                break;
            case 4:
                status = Status.ERROR_4xx;
                break;
            case 3:
            case 2:
                status = Status.NO_ERROR;
                break;
        }
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Return status after executed request - NO_ERROR, ERROR_4xx, ERROR_BAD_URL, ERROR_IO and etc
     * @return status after executed request
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Get message after executed request
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set message after executed request
     * @param message message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get requested address
     * @return requested address
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set address for request
     * @param url address for request
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get header from response
     * @return header from response
     */
    public Map<String, List<String>> getHeader() {
        return header;
    }

    /**
     * Set header from response
     * @param header  header from response
     */
    public void setHeader(Map<String, List<String>> header) {
        this.header = header;
    }

    /**
     * dump header
     * @return dump header
     */
    public String headerToString () {
        StringBuilder str = new StringBuilder();
        if (header != null ) {
            for (Map.Entry<String, List<String>> entry : header.entrySet()) {
                String key = entry.getKey();
                List<String> value = entry.getValue();
                for (String string : value) {
                    str.append(key);
                    str.append(" = ");
                    str.append(string);
                    str.append("\n");
                }
            }
        }
        return str.toString();
    }

}
