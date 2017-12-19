package origin.db;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Map;

import origin.db.core.DbConnection;
import origin.db.core.DbException;
import origin.db.core.DbMeta;
import origin.db.core.DbReader;
import origin.db.core.DbResult;
import origin.db.core.DbStatement;
import origin.db.core.DbUtils;
import origin.db.core.DbWriter;
import origin.util.Alphabet;
import origin.util.sequence.Time16SequenceId;

/**
 * BaseDao Database処理実装.
 */
public class DbBaseDao implements BaseDao {
    private String name;
    private DbConnection conn;
    private Time16SequenceId seq;

    /**
     * コンストラクタ.
     * 
     * @param n
     *            登録名を設定します.
     * @param c
     *            接続オブジェクトを設定します.
     * @param s
     *            シーケンス発行オブジェクトを設定します.
     */
    public DbBaseDao(String n, DbConnection c, Time16SequenceId s) {
        name = n;
        conn = c;
        seq = s;
    }

    protected void finalize() throws Exception {
        close();
    }

    /**
     * クローズ.
     */
    public void close() {
        if (conn != null) {
            conn.close();
            conn = null;
        }
        name = null;
        seq = null;
    }

    /**
     * コミット.
     * 
     * @return boolean [true]の場合、処理が行われました.
     */
    public boolean commit() {
        if (conn != null) {
            conn.commit();
            return true;
        }
        return false;
    }

    /**
     * ロールバック.
     * 
     * @return boolean [true]の場合、処理が行われました.
     */
    public boolean rollback() {
        if (conn != null) {
            conn.rollback();
            return true;
        }
        return false;
    }

    // コネクションクローズチェック.
    private void check() {
        if (conn == null) {
            throw new DbException("コネクションが破棄されています");
        }
    }

    /**
     * 問い合わせ処理.
     * 
     * @param table
     *            対象のテーブル名を設定します.
     * @param where
     *            条件文を設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return DbResult 処理結果が返却されます.
     */
    public DbResult find(String table, String where, Object... params) {
        return limit(-1, -1, table, where, params);
    }

    /**
     * 問い合わせ処理.
     * 
     * @param offset
     *            対象のオフセット値を設定します.
     * @param limit
     *            対象のリミット値を設定します.
     * @param table
     *            対象のテーブル名を設定します.
     * @param where
     *            条件文を設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return DbResult 処理結果が返却されます.
     */
    public DbResult limit(int offset, int limit, String table, String where,
            Object... params) {
        return (DbResult) select(offset, limit, true, "select * from", table,
                where, params);
    }

    /**
     * データ数取得.
     * 
     * @param table
     *            対象のテーブル名を設定します.
     * @param where
     *            条件文を設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return int データ数が返却されます.
     */
    public int count(String table, String where, Object... params) {
        ResultSet res = null;
        try {
            res = (ResultSet) select(-1, -1, false, "select count(*) from",
                    table, where, params);
            res.next();
            int ret = res.getInt(1);
            res.close();
            res = null;
            return ret;
        } catch (DbException de) {
            throw de;
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                }
            }
        }
    }

    // 問い合わせ共通.
    private final Object select(int offset, int limit, boolean retType,
            String head, String table, String where, Object... params) {
        check();
        StringBuilder buf = new StringBuilder(head).append(" ").append(table);
        if (where != null && (where = where.trim()).length() != 0) {
            if (Alphabet.indexOf(where, "where ") == 0
                    || Alphabet.indexOf(where, "order by ") == 0) {
                buf.append(" ").append(where);
            } else {
                buf.append(" where ").append(where);
            }
        }
        DbReader reader = null;
        String sql = buf.toString();
        buf = null;
        if (offset == -1 || limit == -1) {
            reader = conn.reader(sql);
        } else {
            reader = conn.reader(sql, true);
            reader.setPosition(limit, offset);
        }
        if (retType) {
            return reader.query(params);
        } else {
            return reader.execute(params);
        }
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
        return query(-1, -1, sql, params);
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
        check();
        DbReader reader = null;
        if (offset == -1 || limit == -1) {
            reader = conn.reader(sql);
        } else {
            reader = conn.reader(sql, true);
            reader.setPosition(limit, offset);
        }
        return reader.query(params);
    }

    /**
     * データ保存.
     * 
     * @param table
     *            テーブル名を設定します.
     * @param params
     *            保存パラメータを設定します.
     * @return boolean [true]の場合、更新処理、[false]の場合、新規登録処理が行われました.
     */
    public boolean save(String table, Map<String, Object> params) {
        check();
        if (params != null && params.size() > 0) {
            int pos = 0;
            String k;
            int len = params.size();
            Object[] pms = new Object[len << 1];
            Iterator<String> it = params.keySet().iterator();
            while (it.hasNext()) {
                k = it.next();
                pms[pos++] = k;
                pms[pos++] = params.get(k);
            }
            return save(table, pms);
        }
        return save(table);
    }

    /**
     * データ保存.
     * 
     * @param table
     *            テーブル名を設定します.
     * @param params
     *            保存パラメータを設定します.
     * @return boolean [true]の場合、更新処理、[false]の場合、新規登録処理が行われました.
     */
    public boolean save(String table, Object... params) {
        check();
        DbMeta meta = null;
        boolean[] update = new boolean[] { false };
        int pos = getId(params);
        if (pos == -1) {

            // IDが存在しない場合は、Insertのみ処理.
            meta = getTableMeta(update, table, null);
        } else {

            // IDが存在する場合.
            if (params[pos] == null) {

                // 新規作成.
                meta = getTableMeta(update, table, null);
                params[pos] = seq.getUUID();
            } else {

                // 更新の可能性がある場合.
                meta = getTableMeta(update, table, params[pos].toString());
            }
        }

        // パラメータをメタデータの並び順で取得.
        Object[] pms = convertOrderMetaByParams(meta, params);
        String sql = null;

        // update処理.
        if (update[0] == true) {
            sql = updateSql(table, meta);

            // updateのwhere id=?用に、パラメータを１つ付加.
            Object[] p = new Object[pms.length + 1];
            System.arraycopy(pms, 0, p, 0, pms.length);
            p[pms.length] = params[pos].toString();
            pms = p;
            p = null;
        }
        // insert処理.
        else {
            sql = insertSql(table, meta);
        }
        DbWriter writer = conn.writer(sql);
        writer.each(pms);
        return update[0];
    }

    // パラメータ内からIDを取得.
    private static final int getId(Object[] params) {
        int len = params.length;
        for (int i = 0; i < len; i += 2) {
            if (Alphabet.eq("id", params[i].toString())) {
                return i + 1;
            }
        }
        return -1;
    }

    // 対象テーブルのメタデータを取得.
    // IDが存在するかを、確認する処理も併用.
    private final DbMeta getTableMeta(boolean[] update, String table, String id) {
        update[0] = false;
        DbResult res = null;
        try {
            DbStatement stmt = conn.statement();
            StringBuilder buf = new StringBuilder("select * from ")
                    .append(table);
            if (id != null) {
                if (Time16SequenceId.getBytes(id) == null) {
                    throw new DbException("指定IDの内容はUUID形式ではありません:" + id);
                }
                buf.append(" where id=\'").append(id).append("\'");
            }
            buf.append(";");
            res = stmt.query(buf.toString(), 1);
            DbMeta ret = res.getMeta();
            if (id != null) {
                while (res.hasNext()) {
                    if (res.next() != null) {
                        update[0] = true;
                    }
                    break;
                }
            }
            res.close();
            res = null;
            return ret;
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    // メタデータのカラム名順にデータを並べ替える.
    private static final Object[] convertOrderMetaByParams(DbMeta meta,
            Object[] params) {
        String c;
        int i, j;
        int len = meta.size();
        int paramsLen = params.length;
        Object[] ret = new Object[len];
        for (i = 0; i < len; i++) {
            c = meta.getName(i);
            for (j = 0; j < paramsLen; j += 2) {
                if (c.equals(params[j])) {
                    ret[i] = params[j + 1];
                    break;
                }
            }
        }
        return ret;
    }

    // 更新SQL作成.
    private static final String updateSql(String table, DbMeta meta) {
        StringBuilder buf = new StringBuilder("update ").append(table).append(
                " set ");
        int len = meta.size();
        for (int i = 0; i < len; i++) {
            if (i != 0) {
                buf.append(",");
            }
            buf.append(meta.getName(false, i)).append("=?");
        }
        return buf.append(" where id=?").toString();
    }

    // 新規登録用のSQL作成.
    private static final String insertSql(String table, DbMeta meta) {
        StringBuilder buf = new StringBuilder("insert into ").append(table)
                .append(" (");
        int len = meta.size();
        for (int i = 0; i < len; i++) {
            if (i != 0) {
                buf.append(",");
            }
            buf.append(meta.getName(false, i));
        }
        buf.append(") values (");
        for (int i = 0; i < len; i++) {
            if (i != 0) {
                buf.append(",");
            }
            buf.append("?");
        }
        return buf.append(")").toString();
    }

    /**
     * データ更新.
     * 
     * @param table
     *            対象のテーブル名を設定します.
     * @param updateParams
     *            更新内容を設定します.
     * @param where
     *            条件文を設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return int 更新データ数が返却されます.
     */
    @SuppressWarnings("rawtypes")
    public int update(String table, Map updateParams, String where,
            Object... params) {
        check();
        StringBuilder buf = new StringBuilder("update ").append(table).append(
                " set ");
        Object k;
        int cnt = 0;
        int len = updateParams.size();
        Object[] pms = new Object[len + params.length];
        Iterator it = updateParams.keySet().iterator();
        while (it.hasNext()) {
            k = it.next();
            if (cnt != 0) {
                buf.append(",");
            }
            buf.append(DbUtils.convertJavaNameByDBName(k.toString())).append(
                    "=?");
            pms[cnt++] = updateParams.get(k);
        }
        if (where != null && (where = where.trim()).length() != 0) {
            if (Alphabet.indexOf(where, "where ") == 0) {
                buf.append(" ").append(where);
            } else {
                buf.append(" where ").append(where);
            }
        }
        System.arraycopy(params, 0, pms, len, params.length);
        String sql = buf.toString();
        buf = null;

        DbWriter writer = conn.writer(sql);
        return writer.each(pms);
    }

    /**
     * データ削除.
     * 
     * @param table
     *            対象のテーブル名を設定します.
     * @param where
     *            条件文を設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return int 削除データ数が返却されます.
     */
    public int delete(String table, String where, Object... params) {
        check();
        StringBuilder buf = new StringBuilder("delete from ").append(table);
        if (where != null && (where = where.trim()).length() != 0) {
            if (Alphabet.indexOf(where, "where ") == 0) {
                buf.append(" ").append(where);
            } else {
                buf.append(" where ").append(where);
            }
        }
        DbWriter writer = null;
        String sql = buf.toString();
        buf = null;
        writer = conn.writer(sql);
        return writer.each(params);
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
        DbWriter writer = conn.writer(sql);
        return writer.each(params);
    }

    /**
     * 登録名を取得.
     * 
     * @return String 登録名が返却されます.
     */
    public String getName() {
        return name;
    }
}
