package origin.script;

/**
 * リダイレクト例外.
 */
public class HttpRedirectException extends RuntimeException {
    private static final long serialVersionUID = -7357193463738411313L;
    private int status;
    private String url;

    public HttpRedirectException(int status, String url) {
        super();
        this.status = status;
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }
}
