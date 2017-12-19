package origin.script;

import java.nio.channels.ServerSocketChannel;

import origin.db.core.DbFactory;
import origin.net.BaseNio;
import origin.net.NioUtil;

/**
 * Http処理.
 */
public final class Http {

    // net設定.
    protected static final boolean TCP_NO_DELAY = false; // Nagle アルゴリズムを有効にします.
    protected static final boolean KEEP_ALIVE = false; // TCP-KeepAliveを無効に設定します.

    /** Nio処理. **/
    private BaseNio nio = null;

    /**
     * コンストラクタ.
     * 
     * @param info
     * @throws Exception
     */
    public Http(HttpInfo info, DbFactory dbFactory) throws Exception {

        // nio:サーバーソケット作成.
        ServerSocketChannel ch = NioUtil.createServerSocketChannel(
                info.getSocketReceiveBuffer(), info.getLocalAddress(),
                info.getLocalPort(), info.getBacklog());

        // nio処理を生成.
        this.nio = new BaseNio(info.getByteBufferLength(),
                info.getSocketSendBuffer(), info.getSocketReceiveBuffer(),
                KEEP_ALIVE, TCP_NO_DELAY, ch, new HttpCall(dbFactory,
                        new CompileManager(info.getCompileCacheTimeout()),
                        info.getWorkerThread()));
    }

    public void start() {
        nio.startThread();
    }

    public void stop() {
        nio.stopThread();
    }

    public boolean isStop() {
        return nio.isStopThread();
    }

    public boolean isExit() {
        return nio.isExitThread();
    }
}
