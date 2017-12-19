package origin.script;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import origin.util.Utils;

/**
 * コンパイルマネージャ.
 */
public class CompileManager extends Thread {
    private Map<String, CompileElement> manager = new ConcurrentHashMap<String, CompileElement>();
    private int compileCacheTimeout;
    private volatile boolean stopFlag = true;

    public CompileManager(int compileCacheTimeout) {
        this.compileCacheTimeout = compileCacheTimeout;
        startThread();
    }

    /**
     * コンパイル要素を取得.
     * 
     * @param path
     *            対象のパスを設定します.
     * @return CompileElement コンパイル要素が返却されます.
     * @throws Exception
     */
    public CompileElement get(String path) throws Exception {
        CompileElement ret = manager.get(path);
        if (ret == null || Utils.getFileTime(path) != ret.getFileTime()) {
            ret = ExecuteScript.compile(this, path);
            ret.update();
            manager.put(path, ret);
        }
        return ret;
    }

    public void startThread() {
        stopFlag = false;
        setDaemon(true);
        start();
    }

    public void stopThread() {
        stopFlag = true;
    }

    public void run() {
        ThreadDeath ret = null;
        boolean endFlag = false;

        while (!endFlag && !stopFlag) {
            try {
                if (manager.size() == 0) {
                    Thread.sleep(500);
                    continue;
                }
                Map.Entry<String, CompileElement> e;
                Iterator<Map.Entry<String, CompileElement>> it = manager
                        .entrySet().iterator();
                while (it.hasNext()) {
                    e = it.next();
                    try {
                        if (e.getValue().updateTime() + compileCacheTimeout < System
                                .currentTimeMillis()) {
                            it.remove();
                        }
                    } catch (Throwable t) {
                    }
                    Thread.sleep(10);
                }
                Thread.sleep(200);
            } catch (ThreadDeath td) {
                ret = td;
                endFlag = true;
            } catch (InterruptedException ie) {
                endFlag = true;
            } catch (Throwable t) {
            }
        }
        if (ret != null) {
            throw ret;
        }
    }
}
