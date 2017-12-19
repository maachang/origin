package origin.db.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import origin.db.kind.DbKind;
import origin.util.LruCache;

/**
 * DB接続オブジェクト.
 */
@SuppressWarnings("unused")
final class DbBaseConnection implements DbConnection {
    public static final int MAX_CONNECT_READER_SIZE = 5;
    public static final int MAX_CONNECT_WRITER_SIZE = 10;

    /** コネクションオブジェクト. **/
    protected Connection connection = null;

    /** ステートメントオブジェクト. **/
    protected LruCache<String, DatabaseOperation> readCache = null;
    protected LruCache<String, DatabaseOperation> writeCache = null;
    protected DbStatement stmt = null;

    /** DbKind. **/
    protected DbKind kind = null;

    /** 読み込み専用. **/
    protected boolean readOnly = false;

    /** オブジェクト再利用. **/
    protected boolean pooling = true;

    /** フェッチサイズ. **/
    protected int fetchSize = -1;

    /** 書き込みバッチ数. */
    protected int batchLength = -1;

    /** 前回アクセス時間. **/
    protected long lastAccessTime = -1L;

    /** Busyロックタイムアウト. **/
    protected int busyTimeout = DbKind.DEF_BUSY_TIMEOUT;

    /** ResultSetオブジェクト. **/
    private final DbResult result = new DbBaseResult();

    /**
     * コンストラクタ.
     */
    private DbBaseConnection() {
    }

    /**
     * コンストラクタ.
     * 
     * @param connection
     *            コネクションオブジェクトを設定します.
     * @param kind
     *            DbKindを設定します.
     * @parma readOnly [true]を設定した場合、読み込み専用オブジェクトが生成されます.
     *        読み込み専用で設定された場合は、オブジェクトは再利用されません.
     * @param pooling
     *            オブジェクトをプーリングする場合は[true]を設定します.
     * @param fetchSize
     *            対象のフェッチサイズを設定します.
     * @param batchSize
     *            対象のバッチサイズを設定します.
     */
    protected DbBaseConnection(Connection connection, DbKind kind,
            boolean readOnly, boolean pooling, int fetchSize, int batchLength) {
        try {
            this.connection = connection;
            this.kind = kind;
            this.readOnly = readOnly;
            this.pooling = ((!readOnly) ? pooling : false);
            this.fetchSize = fetchSize;
            this.batchLength = batchLength;
            update();

            // トランザクションレベルをセット.
            kind.setTransactionLevel(connection);
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
     * Reader、Writerの破棄.
     */
    protected final void removeRW() {
        Map.Entry<String, DatabaseOperation> n;

        // 接続オブジェクトを全てクローズ.
        if (readCache != null) {
            int len = readCache.size();
            if (len > 0) {
                Object[] list = readCache.getElements();
                for (int i = 0; i < len; i++) {
                    try {
                        ((DatabaseOperation) list[i]).close();
                    } catch (Exception e) {
                    }
                }
                readCache.clear();
                readCache = null;
            }
        }
        if (writeCache != null) {
            int len = writeCache.size();
            if (len > 0) {
                Object[] list = writeCache.getElements();
                for (int i = 0; i < len; i++) {
                    try {
                        ((DatabaseOperation) list[i]).close();
                    } catch (Exception e) {
                    }
                }
                writeCache.clear();
                writeCache = null;
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
        stmt = null;
    }

    /**
     * オブジェクトクローズ.
     */
    public void close() {
        if (connection != null) {
            try {
                rollback();
            } catch (Exception e) {
            }
        }
        removeRW();
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
            }
            connection = null;
        }
        result.close();
    }

    /**
     * コネクションがクローズしているかチェック.
     * 
     * @return boolean [true]の場合、クローズしています.
     */
    public boolean isClose() {
        try {
            return (connection == null || connection.isClosed());
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * コネクションオブジェクトを取得.
     */
    protected final Connection getConnection() {
        return connection;
    }

    /**
     * Resultオブジェクトを取得.
     */
    protected final DbResult getResult() {
        if (result.getConnection() == null) {
            return result;
        }
        return new DbBaseResult();
    }

    /**
     * コネクションチェック.
     */
    protected final void check() {
        if (isClose()) {
            throw new DbException("コネクションは既にクローズしています");
        }
    }

    /**
     * コミット処理.
     */
    public void commit() {
        check();
        try {
            if (!readOnly) {
                if (writeCache != null) {
                    DbWriter w;
                    Object[] list = writeCache.getElements();
                    int len = list.length;
                    for (int i = 0; i < len; i++) {
                        try {
                            w = (DbWriter) list[i];
                            if (!w.isClose() && w.getBatchSize() > 0) {
                                w.flush();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                if (stmt != null && !stmt.isClose() && stmt.getBatchSize() > 0) {
                    stmt.flush();
                }
            }
            connection.commit();
            update();
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * ロールバック処理.
     */
    public void rollback() {
        check();
        try {
            if (!readOnly) {
                if (writeCache != null) {
                    DbWriter w;
                    Object[] list = writeCache.getElements();
                    int len = list.length;
                    for (int i = 0; i < len; i++) {
                        try {
                            w = (DbWriter) list[i];
                            if (!w.isClose() && w.getBatchSize() > 0) {
                                w.clearFlush();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                if (stmt != null && !stmt.isClose() && stmt.getBatchSize() > 0) {
                    stmt.clearFlush();
                }
            }
            connection.rollback();
            update();
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * 読み込み専用オブジェクトを取得. ※この条件で処理するSQL文はSelectです.
     * 
     * @param sql
     *            対象の読み込みオブジェクトを設定します.
     * @return JDBCReader 読み込み専用オブジェクトが返却されます.
     */
    public DbReader reader(String sql) {
        return reader(sql, false);
    }

    /**
     * 読み込み専用オブジェクトを取得. ※この条件で処理するSQL文はSelectです.
     * 
     * @param sql
     *            対象の読み込みオブジェクトを設定します.
     * @param limit
     *            [true]を設定した場合、リミット値を指定したSQL文となります.
     * @return JDBCReader 読み込み専用オブジェクトが返却されます.
     */
    public DbReader reader(String sql, boolean limit) {
        check();
        try {
            if (limit) {
                int p;
                if ((p = sql.lastIndexOf(";")) != -1) {
                    sql = sql.substring(0, p);
                }
                sql = new StringBuilder(sql.trim()).append(" LIMIT ? OFFSET ?")
                        .toString();
            }
            sql = DbUtils.sqlBySemicolonOnOff(kind, sql);

            // 過去に同様のSQLが存在するかチェック.
            DbReader r;
            if (readCache != null) {
                r = (DbReader) readCache.get(sql);
                if (r != null) {
                    update();
                    return r;
                }
            } else {
                readCache = new LruReadrWriterCache(MAX_CONNECT_READER_SIZE);
            }

            // 過去に同様の条件が存在しない場合は、新規作成.
            PreparedStatement pre = connection.prepareStatement(sql,
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            kind.setBusyTimeout(pre, busyTimeout);
            if (!limit && fetchSize > 0) {
                pre.setFetchSize(fetchSize);
            }
            r = new DbBaseReader(this, sql, pre, limit);
            readCache.put(sql, r);
            update();
            return r;
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * 書き込み専用オブジェクトを取得. ※この条件で処理するSQL文は、insert,update,deleteです.
     * 
     * @param sql
     *            対象の読み込みオブジェクトを設定します.
     * @return JDBCWriter 書き込み専用オブジェクトが返却されます.
     */
    public DbWriter writer(String sql) {
        check();
        try {
            if (readOnly) {
                throw new DbException("対象コネクションは読み込み専用です");
            }
            sql = DbUtils.sqlBySemicolonOnOff(kind, sql);

            // 過去に同様のSQLが存在するかチェック.
            DbWriter w;
            if (writeCache != null) {
                w = (DbWriter) writeCache.get(sql);
                if (w != null) {
                    update();
                    return w;
                }
            } else {
                writeCache = new LruReadrWriterCache(MAX_CONNECT_WRITER_SIZE);
            }

            // 過去に同様の条件が存在しない場合は、新規作成.
            PreparedStatement pre = connection.prepareStatement(sql,
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            kind.setBusyTimeout(pre, busyTimeout);
            w = new DbBaseWriter(this, sql, pre, batchLength);
            writeCache.put(sql, w);
            update();
            return w;
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * Statementオブジェクトの取得. ※この条件で処理するSQL文は、reader,writer以外の処理です.
     * 
     * @param sql
     *            対象の読み込みオブジェクトを設定します.
     * @return JDBCStatement 書き込み専用オブジェクトが返却されます.
     */
    public DbStatement statement() {
        if (stmt == null || stmt.isClose()) {
            Statement st = createRawStatement();
            DbStatement s = new DbBaseStatement(this, st, batchLength);
            stmt = s;
        }
        update();
        return stmt;
    }

    // 生ステートメントを生成.
    protected Statement createRawStatement() {
        check();
        if (readOnly) {
            throw new DbException("対象コネクションは読み込み専用です");
        }
        try {
            Statement ret = connection.createStatement(
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            kind.setBusyTimeout(ret, busyTimeout);
            return ret;
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * このオブジェクトが読み込み専用かチェック.
     * 
     * @return boolean [true]の場合、読み込み専用です.
     */
    public boolean isReadOnly() {
        check();
        return readOnly;
    }

    /**
     * このオブジェクトが再利用可能かチェック.
     * 
     * @return boolean [true]の場合、再利用可能です.
     */
    public boolean isPooling() {
        return pooling;
    }

    /**
     * 最終アクセス時間を取得.
     * 
     * @return long 最終アクセス時間が返却されます.
     */
    public long lastAccessTime() {
        return lastAccessTime;
    }

    /**
     * Busyロックタイムアウト値を設定. ※この値は、ファイルロックのタイムアウト値に利用されます.
     * 
     * @param timeout
     *            対象のタイムアウト値(秒)を設定します.
     */
    public void setBusyTimeout(int timeout) {
        busyTimeout = (timeout <= 0) ? 0 : timeout;
    }

    /**
     * Busyロックタイムアウト値を取得. ※この値は、ファイルロックのタイムアウト値に利用されます.
     * 
     * @return int 対象のタイムアウト値（秒）が返却されます.
     */
    public int getBusyTimeout() {
        return busyTimeout;
    }

    /**
     * 時間更新.
     */
    public final void update() {
        lastAccessTime = System.currentTimeMillis();
    }
}
