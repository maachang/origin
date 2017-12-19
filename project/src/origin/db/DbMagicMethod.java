package origin.db;

import java.util.Map;

import origin.db.core.DbException;
import origin.util.Utils;

import origin.script.JsFunction;

/**
 * Databaseマジックメソッド.
 */
@SuppressWarnings("rawtypes")
public class DbMagicMethod extends JsFunction {
    private int type;
    private String sql;
    private TableDao dao;

    public DbMagicMethod() {

    }

    public DbMagicMethod create(TableDao dao, int type, String sql) {
        this.type = type;
        this.sql = sql;
        this.dao = dao;
        return this;
    }

    @Override
    public Object call(Object arg0, Object... arg1) {
        switch (type) {

        case 1: // find.
        {
            return dao.find(sql, arg1);
        }
        case 2: // limit.
        {
            int len = arg1.length;
            Object[] pms = new Object[len - 2];
            System.arraycopy(arg1, 2, pms, 0, len - 2);
            int o = Utils.convertInt(arg1[0]);
            int l = Utils.convertInt(arg1[1]);
            return dao.limit(o, l, sql, pms);
        }
        case 3: // count.
        {
            return dao.count(sql, arg1);
        }
        case 4: // update.
        {
            int len = arg1.length;
            Object[] pms = new Object[len - 1];
            System.arraycopy(arg1, 1, pms, 0, len - 1);
            Map m = (Map) arg1[0];
            return dao.update(m, sql, pms);
        }
        case 5: // delete.
        {
            return dao.delete(sql, arg1);
        }

        }
        throw new DbException("不正な処理タイプ:" + type);
    }
}
