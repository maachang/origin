package origin.db.core;

import java.sql.ResultSet;
import java.util.Iterator;

/**
 * DB読み込み結果情報.
 */
public interface DbResult extends Iterator<DbRow> {

    /**
     * 情報生成.
     * 
     * @param connection
     *            対象のコネクションオブジェクトが設定されます.
     * @param result
     *            結果情報を設定します.
     * @return JDBCResult オブジェクトが返却されます.
     * @exception Exception
     *                例外.
     */
    public DbResult open(DbConnection connection, ResultSet result)
            throws Exception;

    /**
     * 情報生成.
     * 
     * @param connection
     *            対象のコネクションオブジェクトが設定されます.
     * @param result
     *            結果情報を設定します.
     * @param meta
     *            対象のメタデータを設定します.
     * @return JDBCResult オブジェクトが返却されます.
     * @exception Exception
     *                例外.
     */
    public DbResult open(DbConnection connection, ResultSet result, DbMeta meta)
            throws Exception;

    /**
     * 情報クローズ.
     */
    public void close();

    /**
     * オブジェクトクローズチェック.
     * 
     * @return boolean [true]の場合、クローズされています.
     */
    public boolean isClose();

    /**
     * 情報は存在するかチェック.
     * 
     * @return boolean [true]の場合、存在します.
     */
    public boolean hasNext();

    /**
     * 次の情報を取得.
     * 
     * @return JDBCRow １行情報が返却されます.
     */
    public DbRow next();

    /**
     * 対象行の削除.
     */
    public void remove();

    /**
     * 現在行の情報を取得.
     * 
     * @return JDBCRow 現在の行情報が返却されます.
     */
    public DbRow get();

    /**
     * 現在の行番号を取得.
     * 
     * @return int 現在の行番号が返却されます.
     */
    public int getRow();

    /**
     * フェッチサイズを設定.
     * 
     * @param size
     *            対象のフェッチサイズを設定します.
     */
    public void setFetchSize(int size);

    /**
     * フェッチサイズを取得.
     * 
     * @return int フェッチサイズが返却されます.
     */
    public int getFetchSize();

    /**
     * コネクションオブジェクトを取得.
     * 
     * @return JDBCCOnnection コネクションオブジェクトが返却されます.
     */
    public DbConnection getConnection();

    /**
     * メタ情報を取得.
     * 
     * @return JDBCMeta メタ情報が返却されます.
     */
    public DbMeta getMeta();

    /**
     * ResultSetを取得.
     * 
     * @return ResultSet ResultSetが返却されます.
     */
    public ResultSet getResultSet();
}
