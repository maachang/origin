package origin.db.core;

/**
 * DB接続コネクション.
 */
public interface DbConnection {

    /**
     * オブジェクトクローズ.
     */
    public void close();

    /**
     * コネクションがクローズしているかチェック.
     * 
     * @return boolean [true]の場合、クローズしています.
     */
    public boolean isClose();

    /**
     * 時間更新.
     */
    public void update();

    /**
     * コミット処理.
     */
    public void commit();

    /**
     * ロールバック処理.
     */
    public void rollback();

    /**
     * 読み込み専用オブジェクトを取得. ※この条件で処理するSQL文はSelectです.
     * 
     * @param sql
     *            対象の読み込みオブジェクトを設定します.
     * @return JDBCReader 読み込み専用オブジェクトが返却されます.
     */
    public DbReader reader(String sql);

    /**
     * 読み込み専用オブジェクトを取得. ※この条件で処理するSQL文はSelectです.
     * 
     * @param sql
     *            対象の読み込みオブジェクトを設定します.
     * @param limit
     *            [true]を設定した場合、リミット値を指定したSQL文となります.
     * @return JDBCReader 読み込み専用オブジェクトが返却されます.
     */
    public DbReader reader(String sql, boolean limit);

    /**
     * 書き込み専用オブジェクトを取得. ※この条件で処理するSQL文は、insert,update,deleteです.
     * 
     * @param sql
     *            対象の読み込みオブジェクトを設定します.
     * @return JDBCWriter 書き込み専用オブジェクトが返却されます.
     */
    public DbWriter writer(String sql);

    /**
     * Statementオブジェクトの取得. ※この条件で処理するSQL文は、reader,writer以外の処理です.
     * 
     * @return JDBCStatement 書き込み専用オブジェクトが返却されます.
     */
    public DbStatement statement();

    /**
     * このオブジェクトが読み込み専用かチェック.
     * 
     * @return boolean [true]の場合、読み込み専用です.
     */
    public boolean isReadOnly();

    /**
     * このオブジェクトが再利用可能かチェック.
     * 
     * @return boolean [true]の場合、再利用可能です.
     */
    public boolean isPooling();

    /**
     * 最終アクセス時間を取得.
     * 
     * @return long 最終アクセス時間が返却されます.
     */
    public long lastAccessTime();

    /**
     * Busyロックタイムアウト値を設定. ※この値は、ファイルロックのタイムアウト値に利用されます.
     * 
     * @param timeout
     *            対象のタイムアウト値(秒)を設定します.
     */
    public void setBusyTimeout(int timeout);

    /**
     * Busyロックタイムアウト値を取得. ※この値は、ファイルロックのタイムアウト値に利用されます.
     * 
     * @return int 対象のタイムアウト値（秒）が返却されます.
     */
    public int getBusyTimeout();

}
