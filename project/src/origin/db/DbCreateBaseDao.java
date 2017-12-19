package origin.db;

import origin.db.core.DbConnection;
import origin.util.sequence.Time16SequenceId;

/**
 * BaseDao生成用インターフェイス.
 */
public class DbCreateBaseDao implements CreateBaseDao {

    public DbCreateBaseDao() {
    }

    /**
     * 生成処理.
     * 
     * @param n
     *            登録名を設定します.
     * @param c
     *            接続オブジェクトを設定します.
     * @param s
     *            シーケンス発行オブジェクトを設定します.
     */
    public BaseDao create(String n, DbConnection c, Time16SequenceId s) {
        return new DbBaseDao(n, c, s);
    }
}
