package origin.db;

import java.io.Closeable;
import java.util.Map;

import origin.db.core.DbException;
import origin.db.core.DbResult;
import origin.db.core.DbUtils;
import origin.util.ConvertMap;

/**
 * Table単位のDao.
 */
public class TableDao implements ConvertMap, Closeable, AutoCloseable {
    protected String table;
    protected BaseDao baseDao;
    protected DbMagicMethod magicMethod;

    /**
     * コンストラクタ.
     * 
     * @param table
     *            対象のテーブル名を設定します.
     * @param baseDao
     *            基本Daoを設定します.
     * @param magicMethod
     *            MagicMethodオブジェクトを設定します.
     */
    public TableDao(String table, BaseDao baseDao, DbMagicMethod magicMethod) {
        this.table = DbUtils.convertJavaNameByDBName(table);
        this.baseDao = baseDao;
        this.magicMethod = magicMethod;
    }

    /**
     * クローズ.
     */
    public void close() {
        baseDao.close();
    }

    /**
     * コミット.
     * 
     * @return boolean [true]の場合、処理が行われました.
     */
    public boolean commit() {
        return baseDao.commit();
    }

    /**
     * ロールバック.
     * 
     * @return boolean [true]の場合、処理が行われました.
     */
    public boolean rollback() {
        return baseDao.rollback();
    }

    /**
     * 問い合わせ処理.
     * 
     * @return DbResult 処理結果が返却されます.
     */
    public DbResult find() {
        return find("");
    }

    /**
     * 問い合わせ処理.
     * 
     * @param where
     *            条件文を設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return DbResult 処理結果が返却されます.
     */
    public DbResult find(String where, Object... params) {
        return baseDao.find(table, where, params);
    }

    /**
     * 問い合わせ処理.
     * 
     * @param offset
     *            対象のオフセット値を設定します.
     * @param limit
     *            対象のリミット値を設定します.
     * @return DbResult 処理結果が返却されます.
     */
    public DbResult limit(int offset, int limit) {
        return limit(offset, limit, "");
    }

    /**
     * 問い合わせ処理.
     * 
     * @param offset
     *            対象のオフセット値を設定します.
     * @param limit
     *            対象のリミット値を設定します.
     * @param where
     *            条件文を設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return DbResult 処理結果が返却されます.
     */
    public DbResult limit(int offset, int limit, String where, Object... params) {
        return baseDao.limit(offset, limit, table, where, params);
    }

    /**
     * データ数取得.
     * 
     * @return int データ数が返却されます.
     */
    public int count() {
        return count("");
    }

    /**
     * データ数取得.
     * 
     * @param where
     *            条件文を設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return int データ数が返却されます.
     */
    public int count(String where, Object... params) {
        return baseDao.count(table, where, params);
    }

    /**
     * クエリー実行.
     * 
     * @param sql
     *            対象のSQLを設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return DbResult 処理結果が返却されます.
     */
    public DbResult query(String sql, Object... params) {
        return baseDao.query(sql, params);
    }

    /**
     * クエリー実行.
     * 
     * @param offset
     *            対象のオフセット値を設定します.
     * @param limit
     *            対象のリミット値を設定します.
     * @param sql
     *            対象のSQLを設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return DbResult 処理結果が返却されます.
     */
    public DbResult query(int offset, int limit, String sql, Object... params) {
        return baseDao.query(offset, limit, sql, params);
    }

    /**
     * データ保存.
     * 
     * @param params
     *            保存パラメータを設定します.
     * @return boolean [true]の場合、更新処理、[false]の場合、新規登録処理が行われました.
     */
    public boolean save(Map<String, Object> params) {
        return baseDao.save(table, params);
    }

    /**
     * データ保存.
     * 
     * @param params
     *            保存パラメータを設定します.
     * @return boolean [true]の場合、更新処理、[false]の場合、新規登録処理が行われました.
     */
    public boolean save(Object... params) {
        return baseDao.save(table, params);
    }

    /**
     * データ更新.
     * 
     * @param updateParams
     *            更新内容を設定します.
     * @return int 更新データ数が返却されます.
     */
    @SuppressWarnings("rawtypes")
    public int update(Map updateParams) {
        return update(updateParams, "");
    }

    /**
     * データ更新.
     * 
     * @param updateParams
     *            更新内容を設定します.
     * @param where
     *            条件文を設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return int 更新データ数が返却されます.
     */
    @SuppressWarnings("rawtypes")
    public int update(Map updateParams, String where, Object... params) {
        return baseDao.update(table, updateParams, where, params);
    }

    /**
     * データ削除.
     * 
     * @return int 削除データ数が返却されます.
     */
    public int delete() {
        return delete("");
    }

    /**
     * データ削除.
     * 
     * @param where
     *            条件文を設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return int 削除データ数が返却されます.
     */
    public int delete(String where, Object... params) {
        return baseDao.delete(table, where, params);
    }

    /**
     * 書き込み実行.
     * 
     * @param sql
     *            対象のSQLを設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return int 処理結果（数など）が返却されます.
     */
    public int execute(String sql, Object... params) {
        return baseDao.execute(sql, params);
    }

    /**
     * テーブル名を取得.
     * 
     * @return String テーブル名が返却されます.
     */
    public String getTable() {
        return table;
    }

    /**
     * 登録名を取得.
     * 
     * @return String 登録名が返却されます.
     */
    public String getName() {
        return baseDao.getName();
    }

    /**
     * マジックメソッド呼び出し.
     * 
     * @param key
     *            対象のキー名を設定します.
     * @return Object 結果情報が返却されます.
     */
    public Object get(Object key) {

        // マジックメソッドの解析と、SQL作成.
        String method = ((String) key).trim();
        int off = 0;
        int type = 0;

        if (method.startsWith("findBy")) {
            type = 1;
            off = 6;
        } else if (method.startsWith("limitBy")) {
            type = 2;
            off = 7;
        } else if (method.startsWith("countBy")) {
            type = 3;
            off = 7;
        } else if (method.startsWith("updateBy")) {
            type = 4;
            off = 8;
        } else if (method.startsWith("deleteBy")) {
            type = 5;
            off = 8;
        } else {
            throw new DbException("不明なマジックメソッドです:" + method);
        }
        int len = method.length();
        StringBuilder buf = new StringBuilder();

        // where.
        int p, q, r;
        while (true) {
            p = method.indexOf("And", off);
            q = method.indexOf("OrderBy", off);
            if (p == -1) {
                if (q == -1) {
                    whereAppend(buf, method, off, len);
                    off = len;
                } else if (q != off) {
                    whereAppend(buf, method, off, q);
                    off = q;
                }
                break;
            }
            if (q != -1 && p > q) {
                if (q != off) {
                    whereAppend(buf, method, off, q);
                    off = q;
                }
                break;
            }
            if (p != off) {
                whereAppend(buf, method, off, p);
            }
            buf.append("and ");
            off = p + 3;
        }

        // order by.
        p = method.indexOf("OrderBy", off);
        if (p != -1) {
            off = p + 7;
            buf.append("order by ");
            while (true) {
                p = method.indexOf("Asc", off);
                q = method.indexOf("Desc", off);
                r = method.indexOf("And", off);

                // すべてのキーワードが見つからない.
                if (p == -1 && q == -1 && r == -1) {
                    if (off != len) {
                        orderAppend(buf, method, off, len, "asc");
                        off = len;
                    }
                    break;

                    // Ascのキーワードが一番近くで見つかる.
                } else if (p != -1
                        && ((q == -1 || q > p) && (r == -1 || r > p))) {
                    // asc.
                    orderAppend(buf, method, off, p, "asc");
                    off = p + 3;

                    // Descのキーワードが一番近くで見つかる.
                } else if (q != -1
                        && ((p == -1 || p > q) && (r == -1 || r > q))) {
                    // desc.
                    orderAppend(buf, method, off, q, "desc");
                    off = q + 4;

                    // Andのキーワードが一番近くで見つかる.
                } else if (r != -1
                        && ((p == -1 || p > r) && (q == -1 || q > r))) {
                    if (off == r) {
                        buf.append("and ");
                        off = r + 3;
                        continue;
                    }
                    // And.
                    orderAppend(buf, method, off, r, "asc");
                    off = r + 3;
                }
            }
        }
        return magicMethod.create(this, type, buf.toString());
    }

    private static final void whereAppend(StringBuilder buf, String method,
            int s, int e) {
        buf.append(DbUtils.convertJavaNameByDBName(method.substring(s, e)))
                .append("=? ");
    }

    private static final void orderAppend(StringBuilder buf, String method,
            int s, int e, String ud) {
        buf.append(DbUtils.convertJavaNameByDBName(method.substring(s, e)))
                .append(" ").append(ud).append(" ");
    }

    @Override
    public final String toString() {
        return "[object Table]";
    }
}
