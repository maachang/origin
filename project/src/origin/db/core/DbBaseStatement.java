package origin.db.core;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import origin.pref.Mode;

/**
 * DBステートメントオブジェクト. このオブジェクトは主にテーブル生成などで利用.
 */
final class DbBaseStatement implements DbStatement {

    /** ステートメント. **/
    private Statement statement = null;

    /** コネクションオブジェクト. **/
    private DbBaseConnection connection = null;

    /** 最大バッチ長. **/
    private int maxBatch = -1;

    /** 現在バッチ数. **/
    private int nowBatch = 0;

    /**
     * コンストラクタ.
     */
    protected DbBaseStatement() {

    }

    /**
     * コンストラクタ.
     * 
     * @param connection
     *            対象のコネクションオブジェクトが設定されます.
     * @param statement
     *            対象のステートメントが設定されます.
     * @param batchLength
     *            バッチ長を設定します.
     */
    protected DbBaseStatement(DbBaseConnection connection, Statement statement,
            int batchLength) {
        if (batchLength <= 0 || batchLength >= 999) {
            batchLength = 100;
        }
        this.connection = connection;
        this.statement = statement;
        this.maxBatch = batchLength;
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
                return DbWriter.NON_INT_ARRAY;
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
     * @param sql
     *            対象のSQL文を設定します.
     */
    public void batch(String sql) {
        check();
        if (connection.readOnly) {
            throw new DbException("対象コネクションは読み込み専用です");
        }
        try {
            String s;
            int p, b;
            b = 0;

            if (Mode.DEBUG_SQL) {
                System.out.println("sql> " + sql);
            }

            while (true) {
                if ((p = sql.indexOf("\n", b)) == -1) {
                    s = sql.substring(b).trim();
                } else {
                    s = sql.substring(b, p).trim();
                }
                if (s.length() > 0) {
                    statement.addBatch(DbUtils.sqlBySemicolonOnOff(
                            connection.kind, s));
                    if ((nowBatch++) > maxBatch) {
                        flush();
                    } else {
                        connection.update();
                    }
                }
                if (p == -1) {
                    break;
                }
                b = p + 1;
            }
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * 実行処理. ※この処理は、execution処理と違い、都度実行されます. 毎度データベースと通信が発生するため、速度が低下します.
     * 通常は、batchを利用してください.
     * 
     * @param sql
     *            対象のSQL文を設定します.
     * @return List<Exception> SQL実行エラー群が返却されます.
     */
    public List<Exception> each(String sql) {
        check();
        flush();
        if (connection.readOnly) {
            throw new DbException("対象コネクションは読み込み専用です");
        }
        String s;
        int p, b;
        b = 0;
        List<Exception> ret = null;

        if (Mode.DEBUG_SQL) {
            System.out.println("sql> " + sql);
        }

        while (true) {
            if ((p = sql.indexOf("\n", b)) == -1) {
                s = sql.substring(b).trim();
            } else {
                s = sql.substring(b, p).trim();
            }
            if (s.length() > 0) {
                try {
                    statement.executeUpdate(DbUtils.sqlBySemicolonOnOff(
                            connection.kind, s));
                } catch (Exception e) {
                    if (ret == null) {
                        ret = new ArrayList<Exception>();
                    }
                    ret.add(e);
                }
                connection.update();
            }
            if (p == -1) {
                break;
            }
            b = p + 1;
        }
        return ret;
    }

    /**
     * 実行処理.
     * 
     * @param sql
     *            対象のSQL文を設定します.
     * @return JDBCResult 対象の戻り値を設定します.
     */
    public DbResult query(String sql) {
        return query(sql, 0);
    }

    /**
     * 実行処理.
     * 
     * @param sql
     *            対象のSQL文を設定します.
     * @param limit
     *            limit値を設定します.
     * @return JDBCResult 対象の戻り値を設定します.
     */
    public DbResult query(String sql, int limit) {
        check();
        ResultSet rs = null;
        try {
            rs = execute(DbUtils.sqlBySemicolonOnOff(connection.kind, sql),
                    limit);
            return connection.getResult().open(connection, rs);
        } catch (Exception e) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ee) {
                }
            }
            if (e instanceof DbException) {
                throw (DbException) e;
            }
            throw new DbException(e);
        }
    }

    /**
     * 実行処理.
     * 
     * @param sql
     *            対象のSQL文を設定します.
     * @return ResultSet 対象の戻り値を設定します.
     */
    public ResultSet execute(String sql) {
        return execute(sql, 0);
    }

    /**
     * 実行処理.
     * 
     * @param sql
     *            対象のSQL文を設定します.
     * @param limit
     *            limit値を設定します.
     * @return ResultSet 対象の戻り値を設定します.
     */
    public ResultSet execute(String sql, int limit) {
        check();
        ResultSet rs = null;
        try {
            statement.setMaxRows(limit);
            rs = statement.executeQuery(DbUtils.sqlBySemicolonOnOff(
                    connection.kind, sql));
            if (connection.fetchSize > 0) {
                if (limit > 0) {
                    if (connection.fetchSize > limit) {
                        rs.setFetchSize(limit);
                    }
                } else {
                    if (connection.fetchSize > DbReader.MAX_FETCH_SIZE) {
                        rs.setFetchSize(DbReader.MAX_FETCH_SIZE);
                    }
                }
            }
            if (Mode.DEBUG_SQL) {
                System.out.println("sql> " + sql);
            }
            connection.update();
            return rs;
        } catch (Exception e) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ee) {
                }
            }
            if (e instanceof DbException) {
                throw (DbException) e;
            }
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
        check();
        return IO;
    }

    /**
     * SQLを取得.
     * 
     * @return String SQL文が返却されます.
     */
    public String getSql() {
        return "";
    }
}
