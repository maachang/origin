package origin.script.component;

public class ComponentException extends RuntimeException {
    private static final long serialVersionUID = -4629785356704415205L;

    public ComponentException() {
        super();
    }

    public ComponentException(String msg) {
        super(msg);
    }

    public ComponentException(Throwable e) {
        super(e);
    }

    public ComponentException(String msg, Throwable e) {
        super(msg, e);
    }
}
