package origin.db.kind;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

/**
 * DBMSサポート種類.
 */
public abstract class DbKind {

    /** デフォルトBusyTimeout. **/
    public static final int DEF_BUSY_TIMEOUT = 30;

    /**
     * アダプタ名を取得.
     * 
     * @return String アダプタ名が返されます.
     */
    public abstract String getAdapter();

    /**
     * ドライバー名を取得.
     * 
     * @return String ドライバー名が返却されます.
     */
    public abstract String getDriver();

    /**
     * URLヘッダを取得.
     * 
     * @return String URLヘッダが返却されます.
     */
    public abstract String getUrlHead();

    /**
     * 読み込み専用データベース.
     * 
     * @return boolean [true]の場合は、読み込み専用データベースです.
     */
    public boolean isReadOnly() {
        return false;
    }

    /**
     * Booleanサポートフラグ.
     * 
     * @return boolean [true]の場合は、サポートしています.
     */
    public boolean isBoolean() {
        return true;
    }

    /**
     * BOOLEAN型[true]情報.
     * 
     * @return Object Boolean型の[true]条件が返されます.
     */
    public String getBooleanTrue() {
        return "true";
    }

    /**
     * BOOLEAN型[false]情報.
     * 
     * @return Object Boolean型の[false]条件が返されます.
     */
    public String getBooleanFalse() {
        return "false";
    }

    /**
     * SQLの最後に「；」セミコロンをつけるか返します.
     * 
     * @return String SQLの最後に付ける場合は、「；」そのものが返されます.
     */
    public String getSemicolon() {
        return ";";
    }

    /**
     * 現在時刻を取得するSQL構文を取得.
     * 
     * @return String 現在時刻を取得するSQL構文が返却されます.
     */
    public String getCurrentTime() {
        return "current_timestamp";
    }

    /**
     * 接続パラメータを取得.
     * 
     * @param fetchSize
     *            フェッチサイズを設定します.
     * @return String 接続パラメータが返却されます.
     */
    public String getDriverParams(Integer fetchSize) {
        return "";
    }

    /**
     * 変更系のSQLの場合の処理対処.
     * 
     * @return boolean [true]の場合、最後のSQL文のみ実行させます.
     */
    public boolean isExecutionByLastSql() {
        return false;
    }

    /**
     * BusyTimeoutを設定.
     * 
     * @param stmt
     *            対象のStatemnetを設定します.
     * @param timeout
     *            対象のタイムアウト値を設定します.
     * @exception Exception
     *                例外.
     */
    public void setBusyTimeout(Statement stmt, Integer timeout)
            throws Exception {
    }

    /**
     * BusyTimeoutを設定.
     * 
     * @param stmt
     *            対象のStatemnetを設定します.
     * @param timeout
     *            対象のタイムアウト値を設定します.
     * @exception Exception
     *                例外.
     */
    public void setBusyTimeout(Statement stmt, Long timeout) throws Exception {
        setBusyTimeout(stmt, timeout.intValue());
    }

    /**
     * 基本トランザクションレベルを設定.
     * 
     * @param connection
     *            対象のコネクションオブジェクトを設定します.
     * @exception Exception
     *                例外.
     */
    public void setTransactionLevel(Connection connection) throws Exception {
        connection
                .setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }

    /**
     * コネクション開始時に実行するSQL文.
     * 
     * @return String[] コネクション開始時に実行するSQL文を設定します.
     */
    public String[] getInitConnectionSQL() {
        return null;
    }

    /**
     * Property定義.
     * 
     * @param prop
     *            対象のプロパティを設定します.
     */
    public void setProperty(Properties prop) {

    }

    /**
     * ローカルDBチェック.
     * 
     * @return boolean [true]の場合、ローカルDBです.
     */
    public boolean isLocalDb() {
        return false;
    }
}
