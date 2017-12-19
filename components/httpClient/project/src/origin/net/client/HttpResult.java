package origin.net.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HttpClient処理結果.
 */
public class HttpResult {
    private byte[] headers;
    private String headersString;

    private byte[] body;
    private int status;
    private String url;

    protected HttpResult(String url, int status, byte[] header) {
        this.url = url;
        this.status = status;
        this.headers = header;
    }

    public void clear() {
        url = null;
        headers = null;
        headersString = null;
        body = null;
        status = -1;
    }

    public String getUrl() {
        return url;
    }

    public int getStatus() {
        return status;
    }

    public String getHeader(String key) throws IOException {
        convertString();
        if (headersString == null) {
            return null;
        }

        int p = headersString.indexOf(key + ": ");
        if (p == -1) {
            return null;
        }
        int end = headersString.indexOf("\r\n", p);
        if (end == -1) {
            return null;
        }
        return headersString.substring(p + key.length() + 2, end);
    }

    public List<String> getHeaders() throws IOException {
        convertString();
        if (headersString == null) {
            return null;
        }

        int p;
        int b = 0;
        List<String> ret = new ArrayList<String>();
        while ((p = headersString.indexOf(": ", b)) != -1) {
            ret.add(headersString.substring(b, p));
            b = p + 2;
            p = headersString.indexOf("\r\n", b);
            if (p == -1) {
                break;
            }
            b = p + 2;
        }
        return ret;
    }

    private final void convertString() throws IOException {
        if (headers != null) {
            headersString = new String(headers, "UTF8");
            headers = null;
        }
    }

    protected void setResponseBody(byte[] body) {
        this.body = body;
    }

    public String responseText() throws IOException {
        String charset = charset(getHeader("Content-Type"));
        return new String(body, charset);
    }

    private static final String charset(String contentType) {
        int p = contentType.indexOf(" charset=");
        if (p == -1) {
            return "UTF8";
        }
        int b = p + 9;
        p = contentType.indexOf(";", b);
        if (p == -1) {
            p = contentType.length();
        }
        return contentType.substring(b, p);
    }

    public String toString() {
        try {
            convertString();

            return new StringBuilder().append("status:").append(status)
                    .append("\n").append("url:").append(url).append("\n")
                    .append(headersString).append("\n").append("body:")
                    .append((body == null) ? "null" : body.length).toString();
        } catch (Exception e) {
            return "error";
        }
    }
}
