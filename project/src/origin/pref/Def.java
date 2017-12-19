package origin.pref;

/**
 * 基本定義.
 */
public final class Def {
    protected Def() {
    }

    public static final String VERSION = "1.0.0";

    public static final String SERVER_NAME = "Origin (" + VERSION + ")";
    public static final String CLIENT_NAME = "Origin (" + VERSION + ")";

    public static final String CONF_PATH = "./conf/";
    public static final String CONF_FILE = "origin.conf";
    public static final String DB_CONF_FILE = "db.conf";

    public static final String CONF_NAME = Def.CONF_PATH + Def.CONF_FILE;
    public static final String DB_CONF_NAME = Def.CONF_PATH + Def.DB_CONF_FILE;

    public static final String SCRPIT_DIR = "./application";

    public static final int NOT_GZIP_BODY_LENGTH = 128;

    public static final int MAX_CONTENT_LENGTH = 1 * 0x100000;

    public static final String PROJECT_ENV_NAME = "ORIGIN_HOME";

    public static final String NASHORN_CACHE_DIR_PROPERTY = "nashorn.persistent.code.cache";
    public static final String NASHORN_CACHE_DIR = "./.cache";

    public static final String SCRIPT_PARAMS = "params";
    public static final String SCRIPT_REQUEST = "request";
    public static final String SCRIPT_RESPONSE = "response";
    public static final String SHARE_MEMORY = "share";

}
