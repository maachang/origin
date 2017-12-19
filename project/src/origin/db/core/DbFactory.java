package origin.db.core;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import origin.db.kind.DbKind;
import origin.util.sequence.Time16SequenceId;

/**
 * DBファクトリ.
 */
public final class DbFactory {

    /** デフォルトフェッチサイズ. **/
    protected static final int DEF_FETCH_SIZE = 10;

    /** プーリング管理オブジェクト. **/
    private final DbPoolingManager pool = new DbPoolingManager();

    /** デフォルトDbKind. **/
    private DbKind defKind = null;

    /** シーケンスID管理オブジェクト. **/
    private Time16SequenceId sequence = null;

    /** フェッチサイズ. **/
    private int fetchSize;

    /** バッチサイズ. **/
    private int batchSize;

    /** デフォルトのプーリング名. **/
    private String defaultPool;

    /**
     * コンストラクタ.
     */
    protected DbFactory() {
    }

    /**
     * コンストラクタ.
     * 
     * @param kind
     *            デフォルトのDbKindを設定します.
     * @param seq
     *            シーケンスID発行オブジェクトを設定します.
     * @param fetchSize
     *            対象のフェッチサイズを設定します. この値は、selectでの結果キャッシュ行数を示します.
     * @param batchSize
     *            バッチサイズを設定します. この値は、書き込み処理時に実行キャッシュを行う 数を設定します.
     * @param defaultPool
     *            デフォルトのプーリング名を設定します.
     * @exception Exception
     *                例外.
     */
    public DbFactory(DbKind kind, Time16SequenceId seq, int fetchSize,
            int batchSize, String defaultPool) {
        this.defKind = kind;
        this.sequence = seq;
        this.fetchSize = fetchSize;
        this.batchSize = batchSize;
        this.defaultPool = defaultPool;

        // 接続パラメータの設定.
        if (this.fetchSize > 0) {
            this.fetchSize = (this.fetchSize > DbReader.MAX_FETCH_SIZE) ? DbReader.MAX_FETCH_SIZE
                    : this.fetchSize;
        }
    }

    /**
     * デストラクタ.
     */
    protected void finalize() throws Exception {
        destroy();
    }

    /**
     * オブジェクト破棄.
     */
    public void destroy() {
        if (!pool.isDestroy()) {
            pool.destroy();
        }
    }

    /**
     * チェック処理.
     */
    private final void check() {
        if (pool.isDestroy()) {
            throw new DbException("オブジェクトは既に破棄されています");
        }
    }

    /**
     * プーリング接続条件を登録.
     * 
     * @param name
     *            対象の登録名を設定します.
     * @param kind
     *            対象のDbKindを設定します.
     * @param url
     *            対象のコネクションURLを設定します.
     * @param user
     *            対象のユーザ数を設定します.
     * @param passwd
     *            対象のパスワード数を設定します.
     * @param maxPool
     *            プーリング最大管理数を設定します.
     * @param timeout
     *            コネクションタイムアウト値を設定します.
     * @exception Exception
     *                例外.
     */
    public void register(String name, DbKind kind, String url, String user,
            String passwd, int maxPool, long timeout) throws Exception {
        check();
        if (pool.contains(name)) {
            throw new DbException("指定情報名[" + name + "]は既に登録されています");
        }
        if (kind == null) {
            kind = defKind;
        }
        url += kind.getDriverParams(this.fetchSize);
        pool.register(name, new DbPooling(kind, name, url, user, passwd,
                maxPool, timeout));
    }

    /**
     * コネクションプーリングオブジェクトを破棄.
     * 
     * @param name
     *            対象のプーリングオブジェクト名を設定します.
     */
    public void release(String name) {
        check();
        if (pool.contains(name)) {
            DbPooling p = pool.release(name);
            if (p != null) {
                p.destroy();
            }
        }
    }

    /**
     * JDBCConnectionを取得. ※この情報はコネクションプーリングされたオブジェクトが返却されます.
     * 
     * @param name
     *            対象のプーリング名を設定します.
     * @return JDBCConnection コネクションオブジェクトが返却されます.
     */
    public DbConnection getConnection(String name) {
        check();
        try {
            DbPooling p = pool.get(name);
            if (p == null) {
                throw new DbException("指定名[" + name + "]のプーリング登録は行われていません");
            }
            return new DbBaseConnection(p.getConnection(), p.getKind(), false,
                    true, fetchSize, batchSize);
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * 新規コネクション生成. ※プーリングされた情報は利用せず、ドライバーマネージャ経由で新規作成します.
     * 
     * @param url
     *            対象のURLを設定します.
     * @param user
     *            対象のユーザ名を設定します.
     * @param passwd
     *            対象のパスワードを設定します.
     * @return JDBCConnection コネクションオブジェクトが返却されます.
     * @exception Exception
     *                例外.
     */
    public DbConnection getConnection(String url, String user, String passwd)
            throws Exception {
        return getConnection(false, null, url, user, passwd);
    }

    /**
     * 新規コネクション生成. ※プーリングされた情報は利用せず、ドライバーマネージャ経由で新規作成します.
     * 
     * @param kind
     *            対象のDbKindオブジェクトを設定します.
     * @param url
     *            対象のURLを設定します.
     * @param user
     *            対象のユーザ名を設定します.
     * @param passwd
     *            対象のパスワードを設定します.
     * @return JDBCConnection コネクションオブジェクトが返却されます.
     * @exception Exception
     *                例外.
     */
    public DbConnection getConnection(DbKind kind, String url, String user,
            String passwd) throws Exception {
        return getConnection(false, kind, url, user, passwd);
    }

    /**
     * 新規コネクション生成. ※プーリングされた情報は利用せず、ドライバーマネージャ経由で新規作成します.
     * 
     * @param readOnly
     *            [true]を設定した場合、読み込み専用オブジェクトが返却されます.
     * @param kind
     *            対象のDbKindオブジェクトを設定します.
     * @param url
     *            対象のURLを設定します.
     * @param user
     *            対象のユーザ名を設定します.
     * @param passwd
     *            対象のパスワードを設定します.
     * @return JDBCConnection コネクションオブジェクトが返却されます.
     * @exception Exception
     *                例外.
     */
    public DbConnection getConnection(boolean readOnly, DbKind kind,
            String url, String user, String passwd) throws Exception {
        Connection conn;
        if (kind == null) {
            kind = defKind;
        }
        url += kind.getDriverParams(this.fetchSize);
        if (readOnly) {

            try {
                // 新規コネクションを生成.
                conn = DbDriverManager.readOnly(kind, url, user, passwd);
            } catch (Exception e) {
                // エラーの場合は、ドライバー登録して、再取得.
                DbDriverManager.regDriver(kind.getDriver());
                conn = DbDriverManager.readOnly(kind, url, user, passwd);
            }

        } else {
            try {
                // 新規コネクションを生成.
                conn = DbDriverManager.readWrite(kind, url, user, passwd);
            } catch (Exception e) {
                // エラーの場合は、ドライバー登録して、再取得.
                DbDriverManager.regDriver(kind.getDriver());
                conn = DbDriverManager.readWrite(kind, url, user, passwd);
            }
        }
        try {
            return new DbBaseConnection(conn, kind, readOnly, false, fetchSize,
                    batchSize);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ee) {
                }
            }
            throw e;
        }
    }

    /**
     * デフォルトのDbKindを取得.
     * 
     * @return DbKind デフォルトのDbKindが返却されます.
     */
    public DbKind getKind() {
        return defKind;
    }

    /**
     * 指定データベース登録名のDbKindを取得.
     * 
     * @param name
     *            対象のデータベース登録名を設定します.
     * @return DbKind 登録されているDbKindが返却されます.
     * @exception Exception
     *                例外.
     */
    public DbKind getKind(String name) throws Exception {
        check();
        DbPooling p = pool.get(name);
        if (p == null) {
            return null;
        }
        return p.getKind();
    }

    /**
     * シーケンスID発行オブジェクトを取得.
     * 
     * @return Time16SequenceId シーケンスID発行オブジェクトが返却されます.
     */
    public Time16SequenceId getSequence() {
        return sequence;
    }

    /**
     * デフォルトのプーリング名を設定します.
     * 
     * @return String デフォルトのプーリング名が返却されます.
     */
    public String getDefaultPool() {
        return defaultPool;
    }

    /**
     * フェッチサイズを取得.
     * 
     * @return int フェッチサイズが返却されます.
     */
    public int getFetchSize() {
        return fetchSize;
    }

    /**
     * バッチサイズを取得.
     * 
     * @return int バッチサイズが返却されます.
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * 登録名一覧を取得.
     * 
     * @return List<String> 登録名一覧が返却されます.
     */
    public List<String> getNames() {
        check();
        List<String> ret = new ArrayList<String>();
        getNames(ret);
        return ret;
    }

    /**
     * 登録名一覧を取得.
     * 
     * @param out
     *            登録名一覧が返却されます.
     */
    public List<String> getNames(List<String> out) {
        check();
        pool.getNames(out);
        return out;
    }

    /**
     * 登録名が存在するかチェック.
     * 
     * @param name
     *            対象の登録名を設定します.
     * @return boolean [true]の場合、存在します.
     */
    public boolean contains(String name) {
        check();
        return pool.contains(name);
    }

    /**
     * 登録数を取得.
     * 
     * @return int 登録数が返却されます.
     */
    public int size() {
        check();
        return pool.size();
    }
}
