package origin.db.core;

import java.sql.ResultSet;
import java.util.NoSuchElementException;

/**
 * DB読み込み結果情報.
 */
public final class DbBaseResult implements DbResult {

    /** 結果情報. **/
    protected ResultSet result = null;

    /** コネクションオブジェクト. **/
    protected DbConnection connection = null;

    /** メタデータ. **/
    protected DbMeta meta = null;

    /** 情報存在フラグ. **/
    private boolean nextFlag = false;

    /** next呼び出しフラグ. **/
    private boolean execNext = false;

    /** 結果オブジェクト. **/
    private final DbRow row = new DbBaseRow();

    /**
     * コンストラクタ.
     */
    public DbBaseResult() {

    }

    /**
     * コンストラクタ.
     * 
     * @param connection
     *            対象のコネクションオブジェクトが設定されます.
     * @param result
     *            結果情報を設定します.
     * @exception Exception
     *                例外.
     */
    public DbBaseResult(DbConnection connection, ResultSet result)
            throws Exception {
        open(connection, result);
    }

    /**
     * コンストラクタ.
     * 
     * @param connection
     *            対象のコネクションオブジェクトが設定されます.
     * @param result
     *            結果情報を設定します.
     * @param meta
     *            対象のメタデータを設定します.
     * @exception Exception
     *                例外.
     */
    public DbBaseResult(DbConnection connection, ResultSet result, DbMeta meta)
            throws Exception {
        open(connection, result, meta);
    }

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
            throws Exception {
        return open(connection, result, new DbMeta(result.getMetaData()));
    }

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
            throws Exception {
        this.connection = connection;
        this.result = result;
        this.meta = meta;
        this.nextFlag = result.next();
        this.execNext = false;
        this.row.create(this);
        return this;
    }

    /**
     * デストラクタ.
     */
    protected void finalize() throws Exception {
        close();
    }

    /**
     * 情報クローズ.
     */
    public void close() {
        if (result != null) {
            try {
                result.close();
            } catch (Exception e) {
            }
        }
        result = null;
        connection = null;
        meta = null;
        nextFlag = false;
        execNext = false;
        row.clear();
    }

    /**
     * オブジェクトクローズチェック.
     * 
     * @return boolean [true]の場合、クローズされています.
     */
    public boolean isClose() {
        return result == null;
    }

    /** クローズチェック. **/
    private final void check() {
        if (result == null) {
            throw new DbException("オブジェクトは既にクローズしています");
        }
    }

    /**
     * 情報は存在するかチェック.
     * 
     * @return boolean [true]の場合、存在します.
     */
    public boolean hasNext() {
        check();
        if (execNext) {
            try {
                nextFlag = result.next();
            } catch (Exception e) {
                throw new DbException(e);
            }
            execNext = false;
        }
        return nextFlag;
    }

    /**
     * 次の情報を取得.
     * 
     * @return JDBCRow １行情報が返却されます.
     */
    public DbRow next() {
        check();
        if (execNext) {
            try {
                nextFlag = result.next();
            } catch (Exception e) {
                throw new DbException(e);
            }
            execNext = false;
        }
        if (nextFlag) {
            execNext = true;
            return row;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * 対象行の削除.
     */
    public void remove() {
        check();
        throw new UnsupportedOperationException();
    }

    /**
     * 現在行の情報を取得.
     * 
     * @return JDBCRow 現在の行情報が返却されます.
     */
    public DbRow get() {
        check();
        return row;
    }

    /**
     * 現在の行番号を取得.
     * 
     * @return int 現在の行番号が返却されます.
     */
    public int getRow() {
        check();
        try {
            return result.getRow();
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * フェッチサイズを設定.
     * 
     * @param size
     *            対象のフェッチサイズを設定します.
     */
    public void setFetchSize(int size) {
        check();
        try {
            result.setFetchSize(size);
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * フェッチサイズを取得.
     * 
     * @return int フェッチサイズが返却されます.
     */
    public int getFetchSize() {
        check();
        try {
            return result.getFetchSize();
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * コネクションオブジェクトを取得.
     * 
     * @return JDBCCOnnection コネクションオブジェクトが返却されます.
     */
    public DbConnection getConnection() {
        return connection;
    }

    /**
     * メタ情報を取得.
     * 
     * @return JDBCMeta メタ情報が返却されます.
     */
    public DbMeta getMeta() {
        check();
        return meta;
    }

    /**
     * ResultSetを取得.
     * 
     * @return ResultSet ResultSetが返却されます.
     */
    public ResultSet getResultSet() {
        return result;
    }
}
