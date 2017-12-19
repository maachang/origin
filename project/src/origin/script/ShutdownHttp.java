package origin.script;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import origin.util.shutdown.CallbackShutdown;

/**
 * シャットダウン時の処理.
 */
public final class ShutdownHttp extends CallbackShutdown {
    private static final Log LOG = LogFactory.getLog(ShutdownHttp.class);

    /** Reception. **/
    protected Http http;

    public ShutdownHttp(Http http) {
        this.http = http;
    }

    /**
     * シャットダウンフック：PushData管理情報を保存.
     */
    public final void execution() {
        LOG.info(" start shutdown Origin");

        // 各サービス停止.
        http.stop();
        while (!http.isExit()) {
            try {
                Thread.sleep(5);
            } catch (Exception e) {
            }
        }

        LOG.info(" end shutdown Origin");
    }

}
