package origin.db.core;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;

import origin.db.kind.DbKind;
import origin.pref.Mode;

/**
 * DB書き込み専用オブジェクト. ※このオブジェクトは主にInsert,update,deleteなどで利用.
 */
final class DbBaseWriter implements DbWriter {
    /** SQL. **/
    private String sql = null;

    /** ステートメント. **/
    private PreparedStatement statement = null;

    /** パラメータ情報. **/
    private ParameterMetaData paramsMeta = null;

    /** コネクションオブジェクト. **/
    private DbBaseConnection connection = null;

    /** 最大バッチ長. **/
    private int maxBatch = -1;

    /** 現在バッチ数. **/
    private int nowBatch = 0;

    /**
     * コンストラクタ.
     */
    protected DbBaseWriter() {

    }

    /**
     * コンストラクタ.
     * 
     * @param connection
     *            対象のコネクションオブジェクトが設定されます.
     * @param sql
     *            対象のSQL文を設定します.
     * @param statement
     *            対象のステートメントが設定されます.
     * @param batchLength
     *            バッチ長を設定します.
     */
    protected DbBaseWriter(DbBaseConnection connection, String sql,
            PreparedStatement statement, int batchLength) {
        try {
            if (batchLength <= 0 || batchLength >= 999) {
                batchLength = 100;
            }
            this.sql = sql;
            this.connection = connection;
            this.statement = statement;
            this.paramsMeta = statement.getParameterMetaData();
            this.maxBatch = batchLength;
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * デストラクタ.
     */
    protected void finalize() throws Exception {
        close();
    }

    /**
     * オブジェクトクローズ.
     */
    public void close() {
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception e) {
            }
            statement = null;
        }
    }

    /**
     * オブジェクトクローズチェック.
     * 
     * @return boolean [true]の場合、クローズされています.
     */
    public boolean isClose() {
        return statement == null;
    }

    /** クローズチェック. **/
    private final void check() {
        if (statement == null) {
            throw new DbException("オブジェクトは既にクローズしています");
        }
    }

    /**
     * バッチ出力. [execution]メソッドで実行した内容をDBに出力します.
     * 
     * @return 実行結果の戻り値が返却されます.
     */
    public int[] flush() {
        check();
        try {
            if (nowBatch == 0) {
                return NON_INT_ARRAY;
            }
            connection.update();
            int[] ret = statement.executeBatch();
            nowBatch = 0;
            return ret;
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * バッチキャンセル.
     */
    public void clearFlush() {
        check();
        try {
            if (nowBatch == 0) {
                return;
            }
            statement.clearBatch();
            nowBatch = 0;
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * バッチ件数を取得.
     * 
     * @return int バッチ件数が返却されます.
     */
    public int getBatchSize() {
        check();
        return nowBatch;
    }

    /**
     * 実行処理. ※この処理は、flushメソッドを実行しないと、条件は反映されません.
     * ただし、規定数の条件が登録された場合は、内部でflush処理が呼び出されます.
     * また、コネクションオブジェクトのclose処理やcommit処理を呼び出した場合も 合わせて呼び出されます.
     * また、都度実行内容を反映させたい場合は、eachメソッドを利用します.
     * 
     * @param args
     *            対象のパラメータを設定します.
     */
    public void batch(Object... args) {
        check();
        try {
            if (args != null && args.length > 0) {
                DbKind kind = connection.kind;
                DbUtils.preParams(!kind.isBoolean(), kind.getBooleanTrue(),
                        kind.getBooleanFalse(), statement, paramsMeta, args);
            }
            if (Mode.DEBUG_SQL) {
                System.out.println("sql> " + sql);
            }
            connection.update();
            statement.addBatch();
            if ((nowBatch++) > maxBatch) {
                flush();
            }
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * 実行処理. ※この処理は、batch処理と違い、都度実行されます. 毎度データベースと通信が発生するため、速度が低下します.
     * 通常は、batchを利用してください.
     * 
     * @param args
     *            対象のパラメータを設定します.
     */
    public int each(Object... args) {
        flush();
        try {
            if (args != null && args.length > 0) {
                DbKind kind = connection.kind;
                DbUtils.preParams(!kind.isBoolean(), kind.getBooleanTrue(),
                        kind.getBooleanFalse(), statement, paramsMeta, args);
            }
            if (Mode.DEBUG_SQL) {
                System.out.println("sql> " + sql);
            }
            connection.update();
            return statement.executeUpdate();
        } catch (DbException de) {
            throw de;
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
        check();
        return connection;
    }

    /**
     * 処理タイプを取得.
     * 
     * @return int 処理タイプが返却されます.
     */
    public int getType() {
        return WRITER;
    }

    /**
     * SQLを取得.
     * 
     * @return String SQL文が返却されます.
     */
    public String getSql() {
        return sql;
    }
}
