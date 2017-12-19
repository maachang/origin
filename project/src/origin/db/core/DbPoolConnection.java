package origin.db.core;

import java.sql.Connection;

/**
 * プーリング対応コネクションオブジェクト.
 */
interface DbPoolConnection extends Connection {

    /**
     * コネクションオブジェクト破棄.
     */
    public void destroy();

    /**
     * オブジェクト復帰時の呼び出し処理.
     */
    public void recreate();

    /**
     * 最終設定時間を取得.
     * 
     * @return long 最終設定時間が返却されます.
     */
    public long lastTime();

}
