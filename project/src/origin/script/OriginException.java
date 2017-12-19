package origin.script;

public class OriginException extends RuntimeException {
    private static final long serialVersionUID = 8649847526489600557L;

    public OriginException() {
        super();
    }

    public OriginException(String msg) {
        super(msg);
    }

    public OriginException(Throwable e) {
        super(e);
    }

    public OriginException(String msg, Throwable e) {
        super(msg, e);
    }
}
