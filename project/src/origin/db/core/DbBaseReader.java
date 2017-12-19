package origin.db.core;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import origin.db.kind.DbKind;
import origin.pref.Mode;

/**
 * DB読み込み専用オブジェクト. ※このオブジェクトは主にSelectで利用.
 */
final class DbBaseReader implements DbReader {
    /** SQL. **/
    private String sql = null;

    /** ステートメント. **/
    private PreparedStatement statement = null;

    /** パラメータ情報. **/
    private ParameterMetaData paramsMeta = null;

    /** パラメータ数. **/
    private int paramLength = -1;

    /** コネクションオブジェクト. **/
    private DbBaseConnection connection = null;

    /** Result-JDBCMetaデータ. **/
    private DbMeta resultMeta = null;

    /** リミット有効フラグ. **/
    private boolean limitFlag = false;

    /** オフセット値. **/
    private int offset = 0;

    /** リミット値. **/
    private int limit = Integer.MAX_VALUE;

    /**
     * コンストラクタ.
     */
    protected DbBaseReader() {

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
     * @param limitFlag
     *            [true]を設定した場合、オフセット、リミット値が定義されます.
     */
    protected DbBaseReader(DbBaseConnection connection, String sql,
            PreparedStatement statement, boolean limitFlag) {
        int len = -1;
        try {
            // リミット処理で行う場合のみ定義.
            if (limitFlag) {
                len = statement.getParameterMetaData().getParameterCount();
            }
            this.sql = sql;
            this.connection = connection;
            this.paramLength = len;
            this.statement = statement;
            this.paramsMeta = statement.getParameterMetaData();
            this.limitFlag = limitFlag;
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
        resultMeta = null;
        offset = 0;
        limit = Integer.MAX_VALUE;
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
     * オフセット、リミット値をセット.
     * 
     * @param limit
     *            対象のリミット値を設定します.
     * @parma off 対象のオフセット値を設定します.
     * @return JDBCReader オブジェクトが返却されます.
     */
    public DbReader setPosition(int limit, int off) {
        this.limit = limit;
        this.offset = off;
        return this;
    }

    /**
     * オフセット値を前回リミット分移動.
     * 
     * @return JDBCReader オブジェクトが返却されます.
     */
    public DbReader next() {
        offset += limit;
        return this;
    }

    /**
     * オフセット値を前回リミット分戻る.
     * 
     * @return JDBCReader オブジェクトが返却されます.
     */
    public DbReader before() {
        offset -= limit;
        if (offset < 0) {
            offset = 0;
        }
        return this;
    }

    /**
     * オフセット値をセット.
     * 
     * @param off
     *            対象のオフセット値を設定します.
     * @return JDBCReader オブジェクトが返却されます.
     */
    public DbReader setOffset(int off) {
        offset = off;
        return this;
    }

    /**
     * リミット値をセット.
     * 
     * @param limit
     *            対象のリミット値を設定します.
     * @return JDBCReader オブジェクトが返却されます.
     */
    public DbReader setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * オフセット値を取得.
     * 
     * @return int オフセット値が返却されます.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * リミット値を取得.
     * 
     * @return int リミット値が返却されます.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * 読み込み実行処理.
     * 
     * @param args
     *            対象のパラメータを設定します.
     * @return JDBCResult 結果情報が返却されます.
     */
    public DbResult query(Object... args) {
        check();
        ResultSet rs = null;
        try {
            // 読み込みパラメータをセットして処理.
            rs = execute(args);

            // 結果データのメタデータが存在しない場合.
            DbResult ret = connection.getResult();
            if (resultMeta == null) {

                // JDBCResultでメタデータを作成.
                ret.open(connection, rs);

                // 作成したメタデータを保持.
                resultMeta = ret.getMeta();
                return ret;
            }

            // メタデータを設定して処理.
            return ret.open(connection, rs, resultMeta);
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
     * 読み込み実行処理.
     * 
     * @param args
     *            対象のパラメータを設定します.
     * @return ResultSet 結果情報が返却されます.
     */
    public ResultSet execute(Object... args) {
        check();
        ResultSet rs = null;
        try {
            if (args != null && args.length > 0) {
                DbKind kind = connection.kind;
                DbUtils.preParams(!kind.isBoolean(), kind.getBooleanTrue(),
                        kind.getBooleanFalse(), statement, paramsMeta, args);
            }

            if (Mode.DEBUG_SQL) {
                System.out.println("sql> " + sql);
            }

            // リミットクエリーではない場合.
            if (!limitFlag) {
                statement.setMaxRows(0);
                rs = statement.executeQuery();

                // フェッチサイズをセット.
                if (connection.fetchSize > 0) {
                    rs.setFetchSize((connection.fetchSize > MAX_FETCH_SIZE) ? MAX_FETCH_SIZE
                            : connection.fetchSize);
                }
            }
            // リミットクエリの場合.
            else {
                // オフセット、リミット値をセット.
                statement.setMaxRows(limit);
                statement.setInt(paramLength - 1, limit);
                statement.setInt(paramLength, offset);
                rs = statement.executeQuery();
                rs.setFetchSize((limit > MAX_FETCH_SIZE) ? MAX_FETCH_SIZE
                        : limit);
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
        return READER;
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
