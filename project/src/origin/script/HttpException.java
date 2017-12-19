package origin.script;

import origin.net.http.HttpStatus;

/**
 * HTTP例外.
 */
public class HttpException extends RuntimeException {
    private static final long serialVersionUID = 4991389288497905465L;
    private int status;

    public HttpException(int status) {
        super();
        this.status = status;
    }

    public HttpException(int status, String message) {
        super(message);
        this.status = status;
    }

    public HttpException(int status, Throwable e) {
        super(e);
        this.status = status;
    }

    public HttpException(int status, String message, Throwable e) {
        super(message, e);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
    
    public static final void error(int status, String message) {
        error(status, message, null);
    }

    public static final void error(int status, String message, Throwable e) {
        if (message == null) {
            message = HttpStatus.getMessage(status);
        }
        if (e != null) {
            throw new HttpException(status, message, e);
        } else {
            throw new HttpException(status, message);
        }
    }
}
