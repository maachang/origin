package origin.db.kind;

import java.util.Map;

import origin.util.Pmap;
import origin.util.Utils;

/**
 * 利用可能データベース作法定義.
 */
public class KindFactory {

    /** サポート対象のデータベース一覧. **/
    private static final Map<String, DbKind> KINDS = new Pmap<String, DbKind>()
            .ad(KindByPostgre.getName(), new KindByPostgre())
            .ad(KindByMySql.getName(), new KindByMySql())
            .ad(KindByH2.getName(), new KindByH2())
            .ad(KindByHsql.getName(), new KindByHsql())
            .ad(KindBySqlite.getName(), new KindBySqlite())
            .ad(KindByOracle.getName(), new KindByOracle())
            .ad(KindBySQLServer.getName(), new KindBySQLServer());

    /**
     * 指定アダプタ名を設定して、サポートデータベース情報を取得.
     * 
     * @param adapter
     *            対象のアダプタ名を設定します.
     * @return DbKind 対応するデータベース情報が返却されます. [null]の場合、サポートされていません.
     */
    public static final DbKind get(String adapter) {
        if (adapter == null
                || (adapter = Utils.toLowerCase(adapter.trim())).length() <= 0) {
            return null;
        }
        return KINDS.get(adapter);
    }

}
