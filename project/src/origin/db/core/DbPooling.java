package origin.db.core;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import origin.db.kind.DbKind;
import origin.util.Pmap;
import origin.util.atomic.AtomicNumber32;

/**
 * １データベースのプーリング管理.
 */
public class DbPooling {

    /** プーリング最大管理数. **/
    private static final int MAX_POOL = 50;

    /** デフォルトプーリング数. **/
    public static final int DEF_POOL = 10;

    /** 最大タイムアウト値(ミリ秒). **/
    /** 30分. **/
    private static final long MAX_TIMEOUT = 1800000L;

    /** デフォルトタイムアウト値(ミリ秒). **/
    /** 1分. **/
    public static final long DEF_TIMEOUT = 60000L;

    /** プーリングデータ格納用. **/
    protected final Queue<SoftReference<DbPoolConnection>> pooling = new ConcurrentLinkedQueue<SoftReference<DbPoolConnection>>();

    /** データーベース名. **/
    private String databaseName = null;

    /** オブジェクト破棄チェック. **/
    private final AtomicNumber32 destroyFlag = new AtomicNumber32(0);

    /** dbKind. **/
    private DbKind kind;

    /** 接続URL. **/
    private String url;

    /** ユーザ名. **/
    private String user;

    /** パスワード. **/
    private String passwd;

    /** 最大プーリング数. **/
    private int maxPool;

    /** タイムアウト値. **/
    protected long timeout;

    /**
     * コンストラクタ.
     */
    protected DbPooling() {
    }

    /**
     * コンストラクタ.
     * 
     * @param kind
     *            対象のDbKindを設定します.
     * @param databaseName
     *            対象のデータベース登録名を設定します.
     * @param url
     *            対象のコネクションURLを設定します.
     * @exception Exception
     *                例外.
     */
    public DbPooling(DbKind kind, String databaseName, String url)
            throws Exception {
        this(kind, databaseName, url, null, null, DEF_POOL, DEF_TIMEOUT);
    }

    /**
     * コンストラクタ.
     * 
     * @param kind
     *            対象のDbKindを設定します.
     * @param databaseName
     *            対象のデータベース登録名を設定します.
     * @param url
     *            対象のコネクションURLを設定します.
     * @param maxPool
     *            プーリング最大管理数を設定します.
     * @exception Exception
     *                例外.
     */
    public DbPooling(DbKind kind, String databaseName, String url, int maxPool)
            throws Exception {
        this(kind, databaseName, url, null, null, maxPool, DEF_TIMEOUT);
    }

    /**
     * コンストラクタ.
     * 
     * @param kind
     *            対象のDbKindを設定します.
     * @param databaseName
     *            対象のデータベース登録名を設定します.
     * @param url
     *            対象のコネクションURLを設定します.
     * @param maxPool
     *            プーリング最大管理数を設定します.
     * @param timeout
     *            コネクションタイムアウト値を設定します.
     * @exception Exception
     *                例外.
     */
    public DbPooling(DbKind kind, String databaseName, String url, int maxPool,
            long timeout) throws Exception {
        this(kind, databaseName, url, null, null, maxPool, timeout);
    }

    /**
     * コンストラクタ.
     * 
     * @param kind
     *            対象のDbKindを設定します.
     * @param databaseName
     *            対象のデータベース登録名を設定します.
     * @param url
     *            対象のコネクションURLを設定します.
     * @param user
     *            対象のユーザ数を設定します.
     * @param passwd
     *            対象のパスワード数を設定します.
     * @exception Exception
     *                例外.
     */
    public DbPooling(DbKind kind, String databaseName, String url, String user,
            String passwd) throws Exception {
        this(kind, databaseName, url, user, passwd, DEF_POOL, DEF_TIMEOUT);
    }

    /**
     * コンストラクタ.
     * 
     * @param kind
     *            対象のDbKindを設定します.
     * @param databaseName
     *            対象のデータベース登録名を設定します.
     * @param url
     *            対象のコネクションURLを設定します.
     * @param user
     *            対象のユーザ数を設定します.
     * @param passwd
     *            対象のパスワード数を設定します.
     * @param maxPool
     *            プーリング最大管理数を設定します.
     * @exception Exception
     *                例外.
     */
    public DbPooling(DbKind kind, String databaseName, String url, String user,
            String passwd, int maxPool) throws Exception {
        this(kind, databaseName, url, user, passwd, maxPool, DEF_TIMEOUT);
    }

    /**
     * コンストラクタ.
     * 
     * @param kind
     *            対象のDbKindを設定します.
     * @param databaseName
     *            対象のデータベース登録名を設定します.
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
    public DbPooling(DbKind kind, String databaseName, String url, String user,
            String passwd, int maxPool, long timeout) throws Exception {
        if (maxPool > MAX_POOL) {
            maxPool = MAX_POOL;
        } else if (maxPool <= 0) {
            maxPool = DEF_POOL;
        }
        if (timeout > MAX_TIMEOUT) {
            timeout = MAX_TIMEOUT;
        } else if (timeout <= 0) {
            timeout = DEF_TIMEOUT;
        }

        this.databaseName = databaseName;
        this.url = url;
        this.user = user;
        this.passwd = passwd;
        this.kind = kind;
        this.maxPool = maxPool;
        this.timeout = timeout;

        // プーリング監視オブジェクトに登録.
        DbPoolingMonitor.getInstance().setPooling(this);
    }

    /** デストラクタ. **/
    protected void finalize() throws Exception {
        destroy();
    }

    /**
     * オブジェクト破棄.
     */
    public void destroy() {
        if (destroyFlag.put(1) == 0) {

            // プーリング監視オブジェクトに登録されている条件を破棄.
            DbPoolingMonitor.getInstance().clearPooling(this);

            // 保持しているコネクションを全て破棄.
            if (pooling.size() > 0) {
                SoftReference<DbPoolConnection> n;
                Iterator<SoftReference<DbPoolConnection>> it = pooling
                        .iterator();
                while (it.hasNext()) {
                    try {
                        n = it.next();
                        if (n.get() != null) {
                            n.get().destroy();
                        }
                    } catch (Exception e) {
                    }
                }
                pooling.clear();
            }

        }
    }

    /**
     * オブジェクトが既に破棄されているかチェック.
     * 
     * @return boolean [true]の場合、既に破棄されています.
     */
    public boolean isDestroy() {
        return destroyFlag.get() == 1;
    }

    /** チェック処理. **/
    private void check() {
        if (isDestroy()) {
            throw new DbException("オブジェクトは既に破棄されています");
        }
    }

    /** プーリングコネクション拡張命令. **/
    private static final Map<String, Integer> methods = new Pmap<String, Integer>()
            .ad("finalize", 1).ad("destroy", 2).ad("recreate", 3)
            .ad("lastTime", 4).ad("close", 5).ad("isClosed", 6);

    /** プーリングコネクションクラス. **/
    @SuppressWarnings("rawtypes")
    private static final Class[] POOL_CONN_CLASS = new Class[] { DbPoolConnection.class };

    /** ConnectionProxyクラス. **/
    private static class DbPoolConnectionImpl implements InvocationHandler {
        private final AtomicNumber32 closeFlag = new AtomicNumber32(0); // 論理Open.
        private final Queue<SoftReference<DbPoolConnection>> resource;
        private final AtomicNumber32 destroyFlag;
        private final int max;
        private final Connection src;
        private long lastTime;

        /** コンストラクタ. **/
        private DbPoolConnectionImpl(final Connection conn, final int mx,
                final Queue<SoftReference<DbPoolConnection>> pl,
                final AtomicNumber32 df) {
            resource = pl;
            destroyFlag = df;
            max = mx;
            src = conn;
            lastTime = -1L;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            try {
                String name = method.getName();
                Integer no = methods.get(name);
                if (no != null) {
                    switch (no.intValue()) {
                    case 1:
                    case 2:
                        // finalize or destroy.
                        try {
                            if (!src.isClosed()) {
                                src.close();
                            }
                        } catch (Throwable e) {
                        }
                        return null;
                    case 3:
                        // recreate.
                        if (src.isClosed()) {
                            throw new SQLException("Connection is closed");
                        }
                        closeFlag.set(0); // 論理open.
                        return null;
                    case 4:
                        // getLastTime.
                        return lastTime;
                    case 5:
                        // close.
                        if (src.isClosed()) {
                            return null;
                        }
                        if (!src.getAutoCommit()) {
                            src.rollback();
                        }
                        closeFlag.set(1); // 論理close.
                        lastTime = System.currentTimeMillis();
                        if (destroyFlag.get() == 1 || max < resource.size()) {
                            // 最大コネクション管理数を越える場合は削除.
                            // オブジェクトが破棄されている場合も同様.
                            try {
                                if (!src.isClosed()) {
                                    src.close();
                                }
                            } catch (Throwable e) {
                            }
                        } else {
                            // プーリング可能な場合は、セット.
                            resource.offer(new SoftReference<DbPoolConnection>(
                                    (DbPoolConnection) proxy));
                        }
                        return null;
                    case 6:
                        // isClosed.
                        if (closeFlag.get() == 0) { // 論理open状態の場合.

                            // 実際のConnection状態を反映.
                            closeFlag.set((src.isClosed()) ? 1 : 0);
                        }
                        return closeFlag.get() != 0;
                    }
                }

                // 論理クローズの場合.
                if (closeFlag.get() != 0) {
                    throw new SQLException("Connection is closed");
                }
                // 通常コネクション命令はリフレクション呼び出し.
                return method.invoke(src, args);
            } catch (SQLException e) {
                throw e;
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        }
    }

    /** 対象コネクションオブジェクトを取得. **/
    private static final DbPoolConnection createPoolConnection(
            final Connection conn, final int mx,
            final Queue<SoftReference<DbPoolConnection>> pl,
            final AtomicNumber32 df) throws SQLException {

        // proxy返却.
        return (DbPoolConnection) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), POOL_CONN_CLASS,
                new DbPoolConnectionImpl(conn, mx, pl, df));
    }

    /**
     * コネクションオブジェクトを取得.
     * 
     * @return Connection コネクションオブジェクトが返却されます.
     * @exception Exception
     *                例外.
     */
    public Connection getConnection() throws Exception {
        check();

        // プーリング領域からコネクションオブジェクトを取得.
        SoftReference<DbPoolConnection> conn;
        Connection ret = null;

        // Pooling先から取得.
        while ((conn = pooling.poll()) != null) {
            if ((ret = conn.get()) != null) {
                ((DbPoolConnection) ret).recreate();
                return ret;
            }
        }

        // 存在しない場合は、新規コネクションを生成.
        try {
            ret = DbDriverManager.readWrite(kind, url, user, passwd);
        } catch (Exception e) {
            // エラーの場合は、ドライバー登録して、再取得.
            DbDriverManager.regDriver(kind.getDriver());
            ret = DbDriverManager.readWrite(kind, url, user, passwd);
        }

        // プーリングコネクションオブジェクトに変換.
        return DbPooling.createPoolConnection(ret, maxPool, pooling,
                destroyFlag);
    }

    /**
     * 現在のプーリングコネクション数を取得.
     * 
     * @return int 現在のプーリングコネクション数が返却されます.
     */
    public int size() {
        return pooling.size();
    }

    /**
     * DbKindを取得.
     * 
     * @return DbKind DbKindが返却されます.
     */
    public DbKind getKind() {
        return kind;
    }

    /**
     * URLを取得.
     * 
     * @return String URLが返却されます.
     */
    public String getURL() {
        return url;
    }

    /**
     * ユーザ名を取得.
     * 
     * @return String ユーザ名が返却されます.
     */
    public String getUser() {
        return user;
    }

    /**
     * パスワードを取得.
     * 
     * @return String パスワードが返却されます.
     */
    public String getPasswd() {
        return passwd;
    }

    /**
     * 最大プーリング数を取得.
     * 
     * @return int 最大プーリング数が返却されます.
     */
    public int getMaxPool() {
        return maxPool;
    }

    /**
     * コネクション待機タイムアウト値を取得.
     * 
     * @return long コネクション待機タイムアウト値が返却されます.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * データベース名を取得.
     * 
     * @return String データベース名が返却されます.
     * @exception Exception
     *                例外.
     */
    public final String getDatabase() throws Exception {
        check();
        return databaseName;
    }

    /**
     * 文字変換.
     * 
     * @return String 登録されている情報内容が文字で返却されます.
     */
    public String toString() {
        return new StringBuilder().append("adapter:").append(kind.getAdapter())
                .append(" ").append("url:").append(url).append(" ")
                .append("user:").append(user).append(" ").append("passwd:")
                .append(passwd).append(" ").append("maxPool:").append(maxPool)
                .append(" ").append("timeout:").append(timeout).toString();
    }
}
