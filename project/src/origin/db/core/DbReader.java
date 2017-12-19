package origin.db.core;

import java.sql.ResultSet;

/**
 * DB読み込み専用オブジェクト. ※このオブジェクトは主にSelectで利用.
 */
public interface DbReader extends DatabaseOperation {

    /** 最大フェッチサイズ. **/
    public static final int MAX_FETCH_SIZE = 100;

    /**
     * オフセット、リミット値をセット.
     * 
     * @param limit
     *            対象のリミット値を設定します.
     * @parma off 対象のオフセット値を設定します.
     * @return JDBCReader オブジェクトが返却されます.
     */
    public DbReader setPosition(int limit, int off);

    /**
     * オフセット値を前回リミット分移動.
     * 
     * @return JDBCReader オブジェクトが返却されます.
     */
    public DbReader next();

    /**
     * オフセット値を前回リミット分戻る.
     * 
     * @return JDBCReader オブジェクトが返却されます.
     */
    public DbReader before();

    /**
     * オフセット値をセット.
     * 
     * @param off
     *            対象のオフセット値を設定します.
     * @return JDBCReader オブジェクトが返却されます.
     */
    public DbReader setOffset(int off);

    /**
     * リミット値をセット.
     * 
     * @param limit
     *            対象のリミット値を設定します.
     * @return JDBCReader オブジェクトが返却されます.
     */
    public DbReader setLimit(int limit);

    /**
     * オフセット値を取得.
     * 
     * @return int オフセット値が返却されます.
     */
    public int getOffset();

    /**
     * リミット値を取得.
     * 
     * @return int リミット値が返却されます.
     */
    public int getLimit();

    /**
     * 読み込み実行処理.
     * 
     * @param args
     *            対象のパラメータを設定します.
     * @return JDBCResult 結果情報が返却されます.
     */
    public DbResult query(Object... args);

    /**
     * 読み込み実行処理.
     * 
     * @param args
     *            対象のパラメータを設定します.
     * @return ResultSet 結果情報が返却されます.
     */
    public ResultSet execute(Object... args);
}
