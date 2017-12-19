package origin.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import origin.net.http.HttpRequest;
import origin.util.Utils;

/**
 * テスト用Httpリクエスト.
 */
public class TestHttpRequest extends HttpRequest {
    private String method = "GET";
    private String url = null;
    private String version = "1.1";
    private Map<String, String> header = null;

    public TestHttpRequest(String u) {
        String host = getHost(u);
        header = defaultHeader(host);
        url = getUrl(u);
    }

    public TestHttpRequest(String m, String u) {
        String host = getHost(u);
        method = m;
        header = defaultHeader(host);
        url = getUrl(u);
    }

    public TestHttpRequest(String u, Map<String, String> h) {
        String host = getHost(u);
        header = defaultHeader(host);
        marge(header, h);
        url = getUrl(u);
    }

    public TestHttpRequest(String m, String u, Map<String, String> h) {
        String host = getHost(u);
        method = m;
        header = defaultHeader(host);
        marge(header, h);
        url = getUrl(u);
    }

    // マージ.
    private static final void marge(Map<String, String> src,
            Map<String, String> dest) {
        Map.Entry<String, String> e;
        Iterator<Map.Entry<String, String>> it = dest.entrySet().iterator();
        while (it.hasNext()) {
            e = it.next();
            src.put(e.getKey(), e.getValue());
        }
    }

    private static final String getHost(String url) {
        int p = url.indexOf("://");
        if (p == -1) {
            p = -3;
        }
        p += 3;
        int pp = url.indexOf("/", p);
        if (pp == -1) {
            pp = url.length();
        }
        return url.substring(p, pp);
    }
    
    private static final String getUrl(String url) {
        int p = url.indexOf("://");
        if (p == -1) {
            return url;
        }
        p += 3;
        int pp = url.indexOf("/",p);
        if (pp == -1) {
            return "/";
        }
        return url.substring(pp);
    }

    public static final Map<String, String> defaultHeader(String host) {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        ret.put("Accept-Encoding", "gzip, deflate");
        ret.put("Accept-Language", "ja,en-US;q=0.7,en;q=0.3");
        ret.put("Cache-Control", "max-age=0");
        ret.put("Connection", "keep-alive");
        ret.put("Host", host);
        ret.put("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0");
        return ret;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public String getHeader(String key) throws IOException {
        return header.get(key);
    }

    public List<String> getHeaders() throws IOException {
        List<String> ret = new ArrayList<String>();
        Iterator<String> it = header.keySet().iterator();
        while (it.hasNext()) {
            ret.add(it.next());
        }
        return ret;
    }

    public void setBody(byte[] body) {

    }

    public byte[] getBody() {
        return null;
    }

    public int getContentLength() throws IOException {
        String h = getHeader("Content-Length");
        if (h == null) {
            return 0;
        }
        return Utils.convertInt(h);
    }

    public Map<String, String> header() {
        return header;
    }
}
