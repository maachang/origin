package origin.db;

import java.io.Closeable;
import java.util.Map;

import origin.db.core.DbResult;

/**
 * Base Database Access Object.
 */
@SuppressWarnings("rawtypes")
public interface BaseDao extends Closeable, AutoCloseable {

    /**
     * クローズ.
     */
    public void close();

    /**
     * コミット.
     * 
     * @return boolean [true]の場合、処理が行われました.
     */
    public boolean commit();

    /**
     * ロールバック.
     * 
     * @return boolean [true]の場合、処理が行われました.
     */
    public boolean rollback();

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
    public DbResult find(String table, String where, Object... params);

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
            Object... params);

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
    public int count(String table, String where, Object... params);

    /**
     * クエリー実行.
     * 
     * @param sql
     *            対象のSQLを設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return DbResult 処理結果が返却されます.
     */
    public DbResult query(String sql, Object... params);

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
    public DbResult query(int offset, int limit, String sql, Object... params);

    /**
     * データ保存.
     * 
     * @param table
     *            テーブル名を設定します.
     * @param params
     *            保存パラメータを設定します.
     * @return boolean [true]の場合、更新処理、[false]の場合、新規登録処理が行われました.
     */
    public boolean save(String table, Map<String, Object> params);

    /**
     * データ保存.
     * 
     * @param table
     *            テーブル名を設定します.
     * @param params
     *            保存パラメータを設定します.
     * @return boolean [true]の場合、更新処理、[false]の場合、新規登録処理が行われました.
     */
    public boolean save(String table, Object... params);

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
    public int update(String table, Map updateParams, String where,
            Object... params);

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
    public int delete(String table, String where, Object... params);

    /**
     * 書き込み実行.
     * 
     * @param sql
     *            対象のSQLを設定します.
     * @param params
     *            処理パラメータを設定します.
     * @return int 処理結果（数など）が返却されます.
     */
    public int execute(String sql, Object... params);

    /**
     * 登録名を取得.
     * 
     * @return String 登録名が返却されます.
     */
    public String getName();
}
