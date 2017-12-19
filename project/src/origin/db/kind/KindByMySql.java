package origin.db.kind;

/**
 * MySQL用DBMS定義.
 */
public class KindByMySql extends DbKind {

    /**
     * 名前を取得.
     * 
     * @return String 名前が返却されます.
     */
    public static final String getName() {
        return "mysql";
    }

    /**
     * Booleanサポートフラグ.
     * 
     * @return boolean [true]の場合は、サポートしています.
     */
    public boolean isBoolean() {
        return false;
    }

    /**
     * アダプタ名を取得.
     * 
     * @return String アダプタ名が返されます.
     */
    public String getAdapter() {
        return getName();
    }

    /**
     * ドライバー名を取得.
     * 
     * @return String ドライバー名が返却されます.
     */
    public String getDriver() {
        return "com.mysql.jdbc.Driver";
    }

    /**
     * URLヘッダを取得.
     * 
     * @return String URLヘッダが返却されます.
     */
    public String getUrlHead() {
        return "jdbc:mysql:";
    }

    /**
     * BOOLEAN型[true]情報.
     * 
     * @return String Boolean型の[true]条件が返されます.
     */
    public String getBooleanTrue() {
        return "1";
    }

    /**
     * BOOLEAN型[false]情報.
     * 
     * @return String Boolean型の[false]条件が返されます.
     */
    public String getBooleanFalse() {
        return "0";
    }

    /**
     * 接続パラメータを取得.
     * 
     * @param fetchSize
     *            フェッチサイズを設定します.
     * @return String 接続パラメータが返却されます.
     */
    public String getDriverParams(int fetchSize) {
        StringBuilder buf = new StringBuilder("?").append("useUnicode=true")
                .append("&").append("characterEncoding=UTF-8").append("&")
                .append("useServerPrepStmts=true").append("&")
                .append("emulateUnsupportedPstmts=false").append("&")
                .append("tcpNoDelay=false").append("&")
                .append("useCursorFetch=true");
        if (fetchSize > 0) {
            buf.append("&defaultFetchSize=").append(fetchSize);
        }
        return buf.toString();
    }
}
