package origin.db.core;

/**
 * DB例外.
 */
public class DbException extends RuntimeException {
    private static final long serialVersionUID = 8410682417362584257L;

    public DbException() {
        super();
    }

    public DbException(String message) {
        super(message);
    }

    public DbException(Throwable e) {
        super(e);
    }

    public DbException(String message, Throwable e) {
        super(message, e);
    }
}
