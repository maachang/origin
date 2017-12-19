package origin.db.core;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import origin.db.kind.DbKind;
import origin.util.atomic.AtomicNumber32;

/**
 * 複数データベースプーリング管理.
 */
public class DbPoolingManager {

    /** プーリングデータベース管理オブジェクト群. **/
    private final Map<String, DbPooling> manager = new ConcurrentHashMap<String, DbPooling>();

    /** オブジェクト破棄チェック. **/
    private final AtomicNumber32 destroyFlag = new AtomicNumber32(0);

    /**
     * コンストラクタ.
     */
    public DbPoolingManager() {

    }

    /** デストラクタ. **/
    protected void finalize() throws Exception {
        destroy();
    }

    /**
     * 情報破棄.
     */
    public void destroy() {
        if (destroyFlag.get() == 1) {
            return;
        }
        destroyFlag.set(1);

        String name;
        DbPooling pool;
        Iterator<String> it = manager.keySet().iterator();
        while (it.hasNext()) {
            name = it.next();
            pool = manager.get(name);
            if (pool != null) {
                pool.destroy();
            }
        }
        manager.clear();
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

    /**
     * コネクションプーリングオブジェクトを登録.
     * 
     * @param name
     *            プーリングオブジェクト名を設定します.
     * @param pool
     *            プーリングオブジェクトを設定します.
     */
    public void register(String name, DbPooling pool) {
        check();
        if (name == null || name.length() <= 0) {
            throw new DbException("登録対象のプーリングオブジェクト名が定義されていません");
        }
        if (pool == null || pool.isDestroy()) {
            throw new DbException("登録対象のプーリングオブジェクトは有効ではありません");
        }
        if (manager.containsKey(name)) {
            throw new DbException("対象のプーリングオブジェクト名[" + name + "]は既に存在します");
        }
        manager.put(name, pool);
    }

    /**
     * コネクションプーリングオブジェクトを破棄.
     * 
     * @param name
     *            対象のプーリングオブジェクト名を設定します.
     * @return JDBCPooling 登録解除されたプーリングオブジェクトが返却されます.
     */
    public DbPooling release(String name) {
        check();
        if (name == null || name.length() <= 0) {
            throw new DbException("登録対象のプーリングオブジェクト名が定義されていません");
        }
        if (!manager.containsKey(name)) {
            return null;
        }
        return manager.remove(name);
    }

    /**
     * コネクションオブジェクトを取得.
     * 
     * @param name
     *            対象のプーリングオブジェクト名を設定します.
     * @return Connection コネクションオブジェクトが返却されます.
     * @exception Exception
     *                例外.
     */
    public Connection getConnection(String name) throws Exception {
        check();
        if (name == null || name.length() <= 0) {
            throw new DbException("登録対象のプーリングオブジェクト名が定義されていません");
        }
        DbPooling pool;
        if ((pool = manager.get(name)) == null) {
            throw new DbException("対象のプーリングオブジェクト名[" + name + "]は存在しません");
        }
        return pool.getConnection();
    }

    /**
     * 登録名のDbKindを取得.
     * 
     * @param name
     *            対象の登録名を設定します.
     * @return DbKind DbKindが返却されます.
     */
    public DbKind getKind(String name) {
        check();
        if (name == null || name.length() <= 0) {
            throw new DbException("登録対象のプーリングオブジェクト名が定義されていません");
        }
        DbPooling pool;
        if ((pool = manager.get(name)) == null) {
            throw new DbException("対象のプーリングオブジェクト名[" + name + "]は存在しません");
        }
        return pool.getKind();
    }

    /**
     * 登録数を取得.
     * 
     * @return int 登録数が返却されます.
     */
    public int size() {
        check();
        return manager.size();
    }

    /**
     * 登録名一覧を取得.
     * 
     * @return String[] 登録名一覧が返却されます.
     */
    public String[] getNames() {
        check();
        Object[] names = manager.keySet().toArray();
        if (names == null) {
            return null;
        }
        int len = names.length;
        String[] ret = new String[len];
        System.arraycopy(names, 0, ret, 0, len);
        return ret;
    }

    /**
     * 登録名を取得.
     * 
     * @param list
     *            格納対象のオブジェクトを設定します.
     */
    public void getNames(List<String> list) {
        check();
        Object[] names = manager.keySet().toArray();
        if (names == null) {
            return;
        }
        int len = names.length;
        for (int i = 0; i < len; i++) {
            list.add((String) names[i]);
        }
    }

    /**
     * 登録名のプーリングオブジェクトを取得.
     * 
     * @param name
     *            対象の登録名を設定します.
     * @return JDBCPooling プーリングオブジェクトが返却されます.
     */
    public DbPooling get(String name) {
        check();
        if (name == null || name.length() <= 0) {
            throw new DbException("登録対象のプーリングオブジェクト名が定義されていません");
        }
        return manager.get(name);
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
        if (name == null || name.length() <= 0) {
            return false;
        }
        return manager.containsKey(name);
    }

    /**
     * 文字変換.
     * 
     * @return String 登録されている情報を文字情報に変換します.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        String name;
        Iterator<String> it = manager.keySet().iterator();
        buf.append("登録数:").append(manager.size()).append("\n");
        while (it.hasNext()) {
            name = it.next();
            buf.append("  name:").append(name).append("\n").append("    ")
                    .append(manager.get(name)).append("\n");
        }
        return buf.toString();
    }
}
