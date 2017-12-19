package origin.db.core;

import java.io.Closeable;

/**
 * Db操作処理.
 */
public interface DatabaseOperation extends Closeable, AutoCloseable {

    /** 処理タイプ : 読み込み処理. **/
    public static final int READER = 1;

    /** 処理タイプ : 書き込み処理. **/
    public static final int WRITER = 2;

    /** 処理タイプ : 読み書き処理. **/
    public static final int IO = 3;

    /**
     * オブジェクトクローズ.
     */
    public void close();

    /**
     * オブジェクトクローズチェック.
     * 
     * @return boolean [true]の場合、クローズされています.
     */
    public boolean isClose();

    /**
     * コネクションオブジェクトを取得.
     * 
     * @return JDBCCOnnection コネクションオブジェクトが返却されます.
     */
    public DbConnection getConnection();

    /**
     * 処理タイプを取得.
     * 
     * @return int 処理タイプが返却されます.
     */
    public int getType();

    /**
     * SQLを取得.
     * 
     * @return String SQL文が返却されます.
     */
    public String getSql();

}
