package origin.db;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import origin.db.core.DbConnection;
import origin.db.core.DbFactory;
import origin.util.BlankMap;
import origin.util.ConvertMap;

/**
 * Dbオブジェクト.
 */
public class DbObject implements ConvertMap, Closeable, AutoCloseable {
    private DbFactory dbFactory;
    private CreateBaseDao createBaseDao;
    private Map<String, BaseDao> daoManager = new HashMap<String, BaseDao>();
    private DbMagicMethod magicMethod = new DbMagicMethod();

    public DbObject(DbFactory dbFactory, CreateBaseDao createBaseDao) {
        this.dbFactory = dbFactory;
        this.createBaseDao = createBaseDao;
    }

    // DBアクション.
    private static final int DB_CLOSE = 1;
    private static final int DB_COMMIT = 2;
    private static final int DB_ROLLBACK = 3;

    private final void dbAction(int mode) {
        String k;
        BaseDao b;
        Iterator<String> it = daoManager.keySet().iterator();
        while (it.hasNext()) {
            k = it.next();
            b = daoManager.get(k);
            try {
                switch (mode) {
                case DB_CLOSE:
                    b.close();
                    break;
                case DB_COMMIT:
                    b.commit();
                    break;
                case DB_ROLLBACK:
                    b.rollback();
                    break;
                }
            } catch (Exception e) {
            }
        }
        daoManager.clear();
    }

    /**
     * DBクローズ.
     */
    public final void close() {
        dbAction(DB_CLOSE);
    }

    /**
     * DBコミット.
     */
    public final void commit() {
        dbAction(DB_COMMIT);
    }

    /**
     * DBロールバック.
     */
    public final void rollback() {
        dbAction(DB_ROLLBACK);
    }

    /**
     * Db.[key]処理.
     * 
     * @param key
     *            対象のキーを設定します. この情報はプーリング名、もしくは、テーブル名が設定されます.
     * @return Object プーリング名を設定した場合、TableMapが返却され、
     *         テーブル名を指定した場合は、TableDaoが返却されます.
     */
    @Override
    public final Object get(Object key) {
        if (key == null) {
            return null;
        }
        String simbol = key.toString();
        BaseDao dao = getBaseDao(simbol);
        if (dao == null) {
            dao = getBaseDao(dbFactory.getDefaultPool());
            if (dao == null) {
                return null;
            }
            return new TableDao(simbol, dao, magicMethod);
        }
        return new TableManager(simbol, daoManager, magicMethod);
    }

    // プーリング名に従った、基本Daoオブジェクトを取得.
    private final BaseDao getBaseDao(String simbol) {
        if (dbFactory.contains(simbol)) {
            BaseDao dao = daoManager.get(simbol);
            if (dao == null) {
                DbConnection con = dbFactory.getConnection(simbol);
                dao = createBaseDao
                        .create(simbol, con, dbFactory.getSequence());
                daoManager.put(simbol, dao);
            }
            return dao;
        }
        return null;
    }

    /**
     * 確認.
     * 
     * @param key
     *            対象のキー名を設定します.
     * @return boolean [true]の場合、情報は存在します.
     */
    @Override
    public final boolean containsKey(Object key) {
        if (key == null) {
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        return "[object Database]";
    }

    // テーブルマネージャ.
    private static final class TableManager implements BlankMap {
        private String simbol;
        private Map<String, BaseDao> daoManager;
        private DbMagicMethod magicMethod;

        public TableManager(String s, Map<String, BaseDao> d, DbMagicMethod m) {
            simbol = s;
            daoManager = d;
            magicMethod = m;
        }

        @Override
        public final Object get(Object key) {
            if (key == null) {
                return null;
            }
            BaseDao dao = daoManager.get(simbol);
            return new TableDao(key.toString(), dao, magicMethod);
        }

        @Override
        public final String toString() {
            return "[object TableManager]";
        }
    }
}
