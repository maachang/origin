package origin.script;

import origin.conf.Config;
import origin.pref.Env;

/**
 * Httpサーバ設定.
 */
public class HttpInfo {

    /** HTTP同時接続数. **/
    private int backlog = Integer.MAX_VALUE;

    /** Nioバッファ長. **/
    private int byteBufferLength = 1024;

    /** ソケット送信バッファ長. **/
    private int socketSendBuffer = 1024;

    /** ソケット受信バッファ長. **/
    private int socketReceiveBuffer = 2048;

    /** サーバーバインドアドレス. **/
    private String localAddress = null;

    /** サーバーバインドポート. **/
    private int localPort = 3334;

    /** ワーカースレッド数. **/
    private int workerThread = 5;

    /** コンパイルキャッシュタイムアウト. **/
    private int compileCacheTimeout = 30000;

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getByteBufferLength() {
        return byteBufferLength;
    }

    public void setByteBufferLength(int byteBufferLength) {
        this.byteBufferLength = byteBufferLength;
    }

    public int getSocketSendBuffer() {
        return socketSendBuffer;
    }

    public void setSocketSendBuffer(int socketSendBuffer) {
        this.socketSendBuffer = socketSendBuffer;
    }

    public int getSocketReceiveBuffer() {
        return socketReceiveBuffer;
    }

    public void setSocketReceiveBuffer(int socketReceiveBuffer) {
        this.socketReceiveBuffer = socketReceiveBuffer;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(int workerThread) {
        this.workerThread = workerThread;
    }

    public int getCompileCacheTimeout() {
        return compileCacheTimeout;
    }

    public void setCompileCacheTimeout(int compileCacheTimeout) {
        this.compileCacheTimeout = compileCacheTimeout;
    }

    // セクション名.
    private static final String SECTION = "http";

    /**
     * Http設定データを取得.
     * 
     * @param info
     *            データセット先のHttpInfoオブジェクトを設定します.
     * @param conf
     *            対象のコンフィグファイルを設定します.
     */
    public static final void load(HttpInfo info, Config conf) throws Exception {

        // 動作環境を取得.
        Env.OperationEnvironment originEnv = Env.ORIGIN_ENV;

        // 動作環境のセクションが存在する場合は、そちらで処理.
        String section = originEnv.getName() + "." + SECTION;
        if (!conf.isSection(section)) {
            section = SECTION;
            if (!conf.isSection(section)) {
                throw new OriginException("HttpInfoの読み込みに失敗:" + section
                        + "セクションが存在しません");
            }
        }

        Object o = null;

        o = conf.getInt(section, "backlog", 0);
        if (o != null) {
            info.setBacklog((Integer) o);
        }

        o = conf.getInt(section, "byteBufferLength", 0);
        if (o != null) {
            info.setByteBufferLength((Integer) o);
        }

        o = conf.getInt(section, "socketSendBuffer", 0);
        if (o != null) {
            info.setSocketSendBuffer((Integer) o);
        }

        o = conf.getInt(section, "socketReceiveBuffer", 0);
        if (o != null) {
            info.setSocketReceiveBuffer((Integer) o);
        }

        o = conf.get(section, "localAddress", 0);
        if (o != null) {
            info.setLocalAddress((String) o);
        }

        o = conf.getInt(section, "localPort", 0);
        if (o != null) {
            info.setLocalPort((Integer) o);
        }

        o = conf.getInt(section, "workerThread", 0);
        if (o != null) {
            info.setWorkerThread((Integer) o);
        }

        o = conf.getInt(section, "compileCacheTimeout", 0);
        if (o != null) {
            info.setCompileCacheTimeout((Integer) o);
        }
    }
}
