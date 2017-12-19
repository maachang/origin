package origin.db.kind;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Sqlite3用DBMS定義.
 */
public class KindBySqlite extends DbKind {
    /**
     * 名前を取得.
     * 
     * @return String 名前が返却されます.
     */
    public static final String getName() {
        return "sqlite";
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
        return "org.sqlite.JDBC";
    }

    /**
     * URLヘッダを取得.
     * 
     * @return String URLヘッダが返却されます.
     */
    public String getUrlHead() {
        return "jdbc:sqlite:";
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
     * 変更系のSQLの場合の処理対処.
     * 
     * @return boolean [true]の場合、最後のSQL文のみ実行させます.
     */
    public boolean isExecutionByLastSql() {
        return true;
    }

    /**
     * BusyTimeoutを設定.
     * 
     * @param stmt
     *            対象のStatemnetを設定します.
     * @param timeout
     *            対象のタイムアウト値を設定します.
     * @exception SQLException
     *                例外.
     */
    public void setBusyTimeout(Statement stmt, Integer timeout)
            throws SQLException {
        // sqliteは、busyTimeoutは有効.
        if (timeout > 0L) {
            stmt.setQueryTimeout(timeout);
        } else {
            stmt.setQueryTimeout(DEF_BUSY_TIMEOUT);
        }
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
                .setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    }

    /**
     * コネクション開始時に実行するSQL文.
     * 
     * @return String コネクション開始時に実行するSQL文を設定します.
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
        prop.put("shared_cache", "true"); // 共有キャッシュを許可.
        prop.put("busy_timeout", "120000"); // デフォルトビジータイムアウト(120sec).

        // pragma定義.
        prop.put("encoding", "UTF8"); // UTF8エンコード.
        prop.put("journal_mode", "WAL"); // ジャーナルモード(WAL).
        prop.put("locking_mode", "NORMAL"); // ノーマルロック.
        prop.put("transaction_mode", "DEFFERED"); // トランザクションは[DEFFERED].
        prop.put("read_uncommited", "true"); // READ UNCOMMITTED.
        prop.put("case_sensitive_like", "true"); // Like検索の大文字小文字区別する.
        prop.put("legacy_file_format", "false"); // 古いフォーマットは対応しない.
        prop.put("cache_size", "16384"); // キャッシュサイズ(16k).
        prop.put("page_size", "2048"); // ページサイズ(2k).
        // prop.put( "synchronous","NORMAL" ) ; // 通常書き込み.
        prop.put("synchronous", "OFF"); // 遅延書き込み.

        // default定義を行った場合、書き込み処理が存在すると
        // ロックが利いてしまう.
        // sqliteでは、pragma default_xxxx定義してはいけない.
    }

    /**
     * ローカルDBチェック.
     * 
     * @return boolean [true]の場合、ローカルDBです.
     */
    public boolean isLocalDb() {
        return true;
    }
}
