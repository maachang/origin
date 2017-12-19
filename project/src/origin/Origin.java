package origin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import origin.conf.Config;
import origin.db.InitDb;
import origin.db.core.DbFactory;
import origin.net.NetUtil;
import origin.pref.Def;
import origin.pref.Env;
import origin.pref.Mode;
import origin.script.Http;
import origin.script.HttpInfo;
import origin.script.OriginComponentManager;
import origin.script.OriginRegisterService;
import origin.script.ShutdownHttp;
import origin.util.sequence.Time16SequenceId;
import origin.util.shutdown.ShutdownHook;
import origin.util.shutdown.WaitShutdown;

/**
 * Originサービス.
 */
public final class Origin {

    /** ログ. **/
    private static final Log LOG = LogFactory.getLog(Origin.class);

    /** Main. **/
    public static final void main(String[] args) {
        Mode.SERVER = true;
        LOG.info("startup origin env:" + Env.ORIGIN_ENV.getName());
        try {
            System.setProperty(Def.NASHORN_CACHE_DIR_PROPERTY,
                    Def.NASHORN_CACHE_DIR);
            NetUtil.initNet();
            OriginComponentManager.loadComponent();
            Origin origin = new Origin();
            origin.execute();
        } catch (Throwable e) {
            LOG.error("error", e);
        } finally {
            LOG.info("exit origin");
        }
    }

    /** シーケンスID. **/
    protected Time16SequenceId sequence;

    /** DbFactory. **/
    protected DbFactory dbFactory;

    /** http. **/
    protected Http http;

    /** 起動処理. **/
    protected final void execute() throws Exception {
        LOG.info(" readConfig.");

        // メインコンフィグファイルが存在するかチェック.
        Config conf = new Config();
        if (!Config.read(conf, Def.CONF_NAME)) {
            error("file:" + Def.CONF_FILE + "の読み込みに失敗.");
            return;
        }

        // DBコンフィグファイルが存在するかチェック.
        Config dbConf = new Config();
        if (!Config.read(dbConf, Def.DB_CONF_NAME)) {
            error("file:" + Def.DB_CONF_FILE + "の読み込みに失敗.");
            return;
        }
        
        // クラスキャッシュサイズをセット.
        Integer cacheSize = conf.getInt("origin", "clasCacheSize", 0);
        if(cacheSize != null) {
            Mode.CLASS_CACHE_SIZE = cacheSize;
        }

        // シーケンスID管理オブジェクトを生成.
        LOG.info(" create SequenceId.");
        sequence = new Time16SequenceId(conf.getInt("origin", "machineId", 0));

        // DBファクトリを生成.
        LOG.info(" create DbFactory.");
        dbFactory = InitDb.init(dbConf, sequence);

        // 開始処理.
        OriginRegisterService.getInstance().initAll(LOG);

        // Httpの生成.
        LOG.info(" create Http.");
        HttpInfo httpInfo = new HttpInfo();
        HttpInfo.load(httpInfo, conf);
        http = new Http(httpInfo, dbFactory);

        http.start();

        // シャットダウンまで待つ処理を生成.
        int shutdownPort = conf.getInt("origin", "shutdownPort", 0);

        // シャットダウンフックセット.
        ShutdownHttp sd = new ShutdownHttp(http);
        ShutdownHook.registHook(sd);

        // サーバーシャットダウン待ち.
        WaitShutdown.waitSignal(shutdownPort, 0);
    }

    /** エラー出力. **/
    protected final void error(String errMessage) {
        LOG.error(errMessage);
        System.exit(-1);
    }
}
