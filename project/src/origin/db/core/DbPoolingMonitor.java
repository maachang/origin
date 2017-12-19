package origin.db.core;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import origin.util.atomic.AtomicNumber32;

/**
 * プーリングタイムアウトチェック.
 */
class DbPoolingMonitor extends Thread {

    /** スレッド停止フラグ. **/
    private final AtomicNumber32 stopFlag = new AtomicNumber32(0);

    /** 監視オブジェクト. **/
    private final Queue<DbPooling> pool = new ConcurrentLinkedQueue<DbPooling>();

    /** シングルオブジェクト. **/
    private static final DbPoolingMonitor SNGL = new DbPoolingMonitor();

    /**
     * 監視オブジェクトを取得.
     */
    protected static final DbPoolingMonitor getInstance() {
        return SNGL;
    }

    /**
     * コンストラクタ.
     */
    private DbPoolingMonitor() {
        this.stopFlag.set(0);
        this.setDaemon(true);
        this.start();
    }

    /**
     * スレッド停止.
     */
    public void stopThread() {
        this.stopFlag.set(1);
    }

    /**
     * スレッドが停止しているかチェック.
     * 
     * @return boolean [true]の場合、スレッド停止しています.
     */
    public boolean isStop() {
        return (stopFlag.get() == 1);
    }

    /**
     * 監視対象のPoolingManagerをセット.
     * 
     * @param man
     *            対象のプーリングマネージャを設定します.
     */
    public void setPooling(DbPooling man) {
        pool.offer(man);
    }

    /**
     * 監視対象のPoolingManagerをクリア.
     * 
     * @param man
     *            対象のプーリングマネージャを設定します.
     */
    public void clearPooling(DbPooling man) {
        Iterator<DbPooling> it = pool.iterator();
        while (it.hasNext()) {
            if (it.next() == man) {
                it.remove();
            }
        }
    }

    private static final long DEF_SLEEP = 50;
    private static final long NO_DATA_SLEEP = 100;

    /**
     * スレッド実行.
     */
    public void run() {

        ThreadDeath tdObject = null;
        boolean endFlag = false;

        DbPooling man;
        DbPoolConnection c;
        Iterator<DbPooling> mans;
        Iterator<SoftReference<DbPoolConnection>> conns;

        while (stopFlag.get() == 0) {
            try {
                // スレッド停止.
                if (endFlag) {
                    break;
                }

                // 監視対象の情報が存在する場合.
                if (pool.size() > 0) {

                    // 監視対象毎に処理.
                    if (pool.size() > 0) {

                        mans = pool.iterator();

                        while (mans.hasNext()) {

                            // 一定時間待機.
                            Thread.sleep(DEF_SLEEP);

                            // プーリングマネージャを取得.
                            // オブジェクトが既に破棄されている場合は処理しない.
                            if ((man = mans.next()).size() > 0
                                    && !man.isDestroy()) {

                                try {

                                    // プーリングオブジェクト群を処理.
                                    conns = man.pooling.iterator();

                                    // オブジェクトが既に破棄されている場合は処理しない.
                                    while (!man.isDestroy() && conns.hasNext()) {

                                        // 一定時間待機.
                                        Thread.sleep(DEF_SLEEP);

                                        // 定義されたコネクション条件のタイムアウトチェック.
                                        if ((c = (conns.next()).get()) == null) {
                                            conns.remove();
                                        } else if (c.lastTime() + man.timeout < System
                                                .currentTimeMillis()) {
                                            // タイムアウト値を越えた場合は、削除処理.
                                            conns.remove();
                                            c.destroy();
                                        }

                                    }

                                } catch (Exception e) {
                                }
                            }

                        }
                    }

                }

                // 一定時間待機.
                Thread.sleep(NO_DATA_SLEEP);

            } catch (InterruptedException ie) {
                endFlag = true;
            } catch (ThreadDeath td) {
                tdObject = td;
                endFlag = true;
            } catch (Throwable t) {
                // InterruptedException.
                // ThreadDeath
                // これ以外の例外は無視.
            }
        }
        // 情報破棄.
        pool.clear();
        // 後処理.
        stopFlag.set(1);
        if (tdObject != null) {
            throw tdObject;
        }
    }
}
