package origin.script;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * originサービスオブジェクト管理.
 */
public class OriginRegisterService {
    protected OriginRegisterService() {
    }

    private static final OriginRegisterService SNGL = new OriginRegisterService();

    /**
     * オブジェクトを取得.
     * 
     * @return OriginRegisterService オブジェクトが返却されます.
     */
    public static final OriginRegisterService getInstance() {
        return SNGL;
    }

    private List<Runnable> initList = new ArrayList<Runnable>();
    private List<Closeable> exitList = new ArrayList<Closeable>();

    /**
     * Origin開始,終了時に呼び出すオブジェクトの登録.
     * 
     * @param o
     *            対象のオブジェクトを設定します.
     */
    public void initExitRegister(Object o) {
        initRegister((Runnable) o);
        exitRegister((Closeable) o);
    }

    /**
     * Origin開始時に呼び出すオブジェクトの登録.
     * 
     * @param o
     *            対象のオブジェクトを設定します.
     */
    public void initRegister(Runnable o) {
        initList.add(o);
    }

    /**
     * Origin終了時に呼び出すオブジェクトの登録.
     * 
     * @param o
     *            対象のオブジェクトを設定します.
     */
    public void exitRegister(Closeable o) {
        exitList.add(o);
    }

    /**
     * 開始処理に呼び出すオブジェクト一覧を取得.
     * 
     * @return List<Closeable> オブジェクト一覧が返却されます.
     */
    public List<Runnable> getInitList() {
        return initList;
    }

    /**
     * 終了処理に呼び出すオブジェクト一覧を取得.
     * 
     * @return List<Closeable> オブジェクト一覧が返却されます.
     */
    public List<Closeable> getExitList() {
        return exitList;
    }

    /**
     * 開始処理呼び出し.
     * 
     * @param log
     *            エラー時のログオブジェクトを設定します.
     */
    public void initAll(Log log) {
        int len = initList.size();
        for (int i = 0; i < len; i++) {
            try {
                initList.get(i).run();
            } catch (Exception e) {
                if (log != null) {
                    log.info("error", e);
                } else {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 終了処理呼び出し.
     * 
     * @param log
     *            エラー時のログオブジェクトを設定します.
     */
    public void closeAll(Log log) {
        int len = exitList.size();
        for (int i = 0; i < len; i++) {
            try {
                exitList.get(i).close();
            } catch (Exception e) {
                if (log != null) {
                    log.info("error", e);
                } else {
                    e.printStackTrace();
                }
            }
        }
    }
}
