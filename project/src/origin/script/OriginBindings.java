package origin.script;

import java.util.Map;

import javax.script.SimpleBindings;

import origin.db.CreateBaseDao;
import origin.db.DbObject;
import origin.db.core.DbFactory;

/**
 * origin用Bindings.
 */
public class OriginBindings extends SimpleBindings {
    private static final String DB_SIMBOL = "db";
    private DbObject db = null;

    /**
     * コンストラクタ.
     */
    protected OriginBindings() {
        super();
    }

    /**
     * コンストラクタ.
     * 
     * @param dbFactory
     *            DbFactoryを設定します.
     * @param createBaseDao
     *            createBaseDaoを設定します.
     */
    public OriginBindings(DbFactory dbFactory, CreateBaseDao createBaseDao) {
        super();
        db = new DbObject(dbFactory, createBaseDao);
    }

    /**
     * コンストラクタ.
     * 
     * @param dbFactory
     *            DbFactoryを設定します.
     * @param createBaseDao
     *            createBaseDaoを設定します.
     * @param map
     *            マージ対象のデータを設定します.
     */
    public OriginBindings(DbFactory dbFactory, CreateBaseDao createBaseDao,
            Map<String, Object> map) {
        super(map);
        db = new DbObject(dbFactory, createBaseDao);
    }

    /**
     * データクリア.
     */
    public void close() {
        db.close();
    }

    /**
     * データクリア.
     */
    public void clear() {
        super.clear();
        this.close();
    }

    /**
     * 取得.
     * 
     * @param key
     *            対象のキー名を設定します.
     * @return Object オブジェクトが返却されます.
     */
    @Override
    public final Object get(Object key) {
        Object ret = super.get(key);
        if (ret == null && DB_SIMBOL.equals(key)) {
            return db;
        }
        return ret;
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
        boolean ret = super.containsKey(key);
        if (!ret && DB_SIMBOL.equals(key)) {
            return true;
        }
        return ret;
    }

    @Override
    public final String toString() {
        return "[object DbBindings]";
    }

}
