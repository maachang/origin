package origin.db.kind;

import java.sql.Connection;

/**
 * H2用DBMSサポート定義.
 */
public class KindByH2 extends DbKind {

    /**
     * 名前を取得.
     * 
     * @return String 名前が返却されます.
     */
    public static final String getName() {
        return "h2";
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
        return "org.h2.Driver";
    }

    /**
     * URLヘッダを取得.
     * 
     * @return String URLヘッダが返却されます.
     */
    public String getUrlHead() {
        return "jdbc:h2:";
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
        return new StringBuilder(";").
        // append( "MULTI_THREADED=1;" ). // マルチスレッドで処理(MVCC利用と併用できない).
                append("MVCC=TRUE;"). // MVCCモード(多版型同時実行制御)
                append("LOCK_MODE=0;"). // 低レベルロック(READ_UNCOMMITTED).
                append("LOCK_TIMEOUT=120000;"). // ビジータイムアウト(120秒).
                append("DB_CLOSE_ON_EXIT=TRUE;"). // VM終了時にDBクローズ.
                append("CACHE_SIZE=131072;"). // キャッシュは12k.
                append("PAGE_SIZE=32768;"). // ページサイズは32768.
                append("IFEXISTS=FALSE;"). // ファイルが存在しない場合はファイル作成.
                append("AUTOCOMMIT=FALSE;"). // 通常コミットモード.
                append("LOG=0;"). // ログは必要なし
                append("CACHE_TYPE=TQ;"). // scan-resistantキャッシュタイプ"TQ"(two
                                          // queue).
                append("UNDO_LOG=0;").toString();
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
