package origin.db.core;

import java.sql.ResultSet;
import java.util.List;

/**
 * DB書き込み専用ステートメントオブジェクト. このオブジェクトは主にテーブル生成などで利用.
 */
public interface DbStatement extends DatabaseOperation {

    /**
     * バッチ出力. [execution]メソッドで実行した内容をDBに出力します.
     * 
     * @return 実行結果の戻り値が返却されます.
     */
    public int[] flush();

    /**
     * バッチキャンセル.
     */
    public void clearFlush();

    /**
     * バッチ件数を取得.
     * 
     * @return int バッチ件数が返却されます.
     */
    public int getBatchSize();

    /**
     * 実行処理. ※この処理は、flushメソッドを実行しないと、条件は反映されません.
     * ただし、規定数の条件が登録された場合は、内部でflush処理が呼び出されます.
     * また、コネクションオブジェクトのclose処理やcommit処理を呼び出した場合も 合わせて呼び出されます.
     * また、都度実行内容を反映させたい場合は、eachメソッドを利用します.
     * 
     * @param sql
     *            対象のSQL文を設定します.
     */
    public void batch(String sql);

    /**
     * 実行処理. ※この処理は、execution処理と違い、都度実行されます. 毎度データベースと通信が発生するため、速度が低下します.
     * 通常は、batchを利用してください.
     * 
     * @param sql
     *            対象のSQL文を設定します.
     * @return List<Exception> SQL実行エラー群が返却されます.
     */
    public List<Exception> each(String sql);

    /**
     * 実行処理.
     * 
     * @param sql
     *            対象のSQL文を設定します.
     * @return JDBCResult 対象の戻り値を設定します.
     */
    public DbResult query(String sql);

    /**
     * 実行処理.
     * 
     * @param sql
     *            対象のSQL文を設定します.
     * @param limit
     *            limit値を設定します.
     * @return JDBCResult 対象の戻り値を設定します.
     */
    public DbResult query(String sql, int limit);

    /**
     * 実行処理.
     * 
     * @param sql
     *            対象のSQL文を設定します.
     * @return ResultSet 対象の戻り値を設定します.
     */
    public ResultSet execute(String sql);

    /**
     * 実行処理.
     * 
     * @param sql
     *            対象のSQL文を設定します.
     * @param limit
     *            limit値を設定します.
     * @return ResultSet 対象の戻り値を設定します.
     */
    public ResultSet execute(String sql, int limit);

}
