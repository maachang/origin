package origin.db.kind;

import java.sql.Connection;

/**
 * H2用DBMSサポート定義.
 */
public class KindByHsql extends DbKind {

    /**
     * 名前を取得.
     * 
     * @return String 名前が返却されます.
     */
    public static final String getName() {
        return "hsqldb";
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
        return "org.hsqldb.jdbc.JDBCDriver";
    }

    /**
     * URLヘッダを取得.
     * 
     * @return String URLヘッダが返却されます.
     */
    public String getUrlHead() {
        return "jdbc:hsqldb:";
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
     * 接続パラメータを取得.
     * 
     * @param fetchSize
     *            フェッチサイズを設定します.
     * @return String 接続パラメータが返却されます.
     */
    public String getDriverParams(int fetchSize) {
        return new StringBuilder(";").append("hsqldb.tx=mvcc;")
                .append("hsqldb.tx_level=READ_UNCOMMITTED;")
                .append("sql.enforce_strict_size=true;")
                .append("hsqldb.default_table_type=cached;")
                .append("shutdown=true;").append("ifexists=false;").toString();
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
