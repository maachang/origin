package origin.script;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import origin.db.core.DbFactory;
import origin.net.NioCall;
import origin.net.NioElement;
import origin.net.SendData;
import origin.util.atomic.AtomicNumber32;

/**
 * Httpコール処理.
 */
public final class HttpCall extends NioCall {
    private static final Log LOG = LogFactory.getLog(HttpCall.class);
    private DbFactory dbFactory;
    private CompileManager compileManager;
    private int workerLength = -1;
    private Map<String,Object> memory = new ConcurrentHashMap<String,Object>();
    private final AtomicNumber32 counter = new AtomicNumber32(0);
    private ScriptWorkerThread[] worker = null;

    /**
     * コンストラクタ.
     * 
     * @param dbFactory
     *            DBファクトリオブジェクトを設定します.
     * @param compileManage
     *            コンパイルマネージャを設定します.
     * @param wprkerLength
     *            ワーカースレッド長を設定します.
     */
    public HttpCall(DbFactory dbFactory, CompileManager compileManager,
            int workerLength) {
        this.dbFactory = dbFactory;
        this.compileManager = compileManager;
        this.workerLength = workerLength;
    }

    /**
     * 新しい通信要素を生成.
     * 
     * @return BaseNioElement 新しい通信要素が返却されます.
     */
    public NioElement createElement() {
        return new HttpElement();
    }

    /**
     * 開始処理.
     * 
     * @return boolean [true]の場合、正常に処理されました.
     */
    public boolean startNio() {
        LOG.info(" start Http nio");

        // ワーカースレッドを生成.
        ScriptWorkerThread[] w = new ScriptWorkerThread[workerLength];
        for (int i = 0; i < workerLength; i++) {
            w[i] = new ScriptWorkerThread(dbFactory, compileManager, memory, i);
            w[i].startThread();
        }
        worker = w;
        return true;
    }

    /**
     * 終了処理.
     */
    public void endNio() {
        LOG.info(" stop Http nio");

        // ワーカースレッドを破棄.
        ScriptWorkerThread[] w = worker;
        worker = null;
        for (int i = 0; i < workerLength; i++) {
            w[i].stopThread();
        }

        // ワーカースレッド停止待ち.
        boolean allEndFlag = false;
        while (!allEndFlag) {
            allEndFlag = true;
            for (int i = 0; i < workerLength; i++) {
                if (!w[i].isEndThread()) {
                    allEndFlag = false;
                    break;
                }
            }
            if (!allEndFlag) {
                try {
                    Thread.sleep(5);
                } catch (Exception e) {
                }
            }
        }

        // DBファクトリを破棄.
        dbFactory.destroy();

        // 終了処理.
        OriginRegisterService.getInstance().closeAll(LOG);

        LOG.info(" exit Http nio");
    }

    /**
     * エラーハンドリング.
     */
    public void error(Throwable e) {
        LOG.error(" error Http nio", e);
    }

    /**
     * Accept処理.
     * 
     * @param em
     *            対象のBaseNioElementオブジェクトが設定されます.
     * @return boolean [true]の場合、正常に処理されました.
     * @exception IOException
     *                IO例外.
     */
    public boolean accept(NioElement em) throws IOException {
        LOG.debug(" accept Http nio");

        return true;
    }

    /**
     * Send処理.
     * 
     * @param em
     *            対象のBaseNioElementオブジェクトが設定されます.
     * @param buf
     *            対象のByteBufferを設定します.
     * @return boolean [true]の場合、正常に処理されました.
     * @exception IOException
     *                IO例外.
     */
    public boolean send(NioElement em, ByteBuffer buf) throws IOException {
        LOG.debug(" send Http nio");

        HttpElement rem = (HttpElement) em;

        // 送信データを取得.
        SendData sendData = rem.getSendData();
        byte[] b = sendData.get();

        // 送信対象のデータが存在しない場合.
        if (b == null) {

            // データが存在しない場合.
            if (buf.position() == 0) {

                // 通信切断処理.
                return false;
            }
            return true;
        }

        // 送信処理.
        int p = sendData.getPosition();
        int n = b.length - p;
        int len = buf.limit() - buf.position();
        if (len > n) {
            buf.put(b, p, n);
            sendData.clear();
        } else {
            buf.put(b, p, len);
            sendData.setPosition(p + len);
        }
        return true;
    }

    /**
     * Receive処理.
     * 
     * @param em
     *            対象のBaseNioElementオブジェクトが設定されます.
     * @param buf
     *            対象のByteBufferを設定します.
     * @return boolean [true]の場合、正常に処理されました.
     * @exception IOException
     *                IO例外.
     */
    public boolean receive(NioElement em, ByteBuffer buf) throws IOException {
        LOG.debug(" recv Http nio:" + buf);

        HttpElement rem = (HttpElement) em;

        // 受信バッファに今回分の情報をセット.
        rem.getBuffer().write(buf);

        int no = rem.getWorkerNo();
        if (no == -1) {
            no = counter.inc() % workerLength;
            counter.set(no);
        }
        worker[no].register(rem);
        return true;
    }

}
