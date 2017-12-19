package origin.script;

import origin.conf.Config;
import origin.db.InitDb;
import origin.db.core.DbFactory;
import origin.pref.Def;
import origin.util.ConsokeInKey;
import origin.util.sequence.Time16SequenceId;

/**
 * コンソール入力用.
 */
public abstract class AbstractConsole {

    /** シーケンスID. **/
    protected Time16SequenceId sequence;

    /** DbFactory. **/
    protected DbFactory dbFactory;

    /** 起動処理. **/
    protected final void execute(String[] args) throws Exception {

        try {
            // メインコンフィグファイルが存在するかチェック.
            Config conf = new Config();
            if (!Config.read(conf, Def.CONF_NAME)) {
                error("ConfigFile:" + Def.CONF_FILE + "の読み込みに失敗.");
                return;
            }

            // DBコンフィグファイルが存在するかチェック.
            Config dbConf = new Config();
            if (!Config.read(dbConf, Def.DB_CONF_NAME)) {
                error("ConfigFile:" + Def.DB_CONF_FILE + "の読み込みに失敗.");
                return;
            }

            // シーケンスオブジェクトを生成.
            sequence = new Time16SequenceId(conf.getInt("origin",
                    "machineId", 0));

            // DBファクトリを生成.
            dbFactory = InitDb.init(dbConf, sequence);

            if (args == null || args.length == 0) {

                // コンソール実行.
                ConsokeInKey ckey = new ConsokeInKey();
                try {
                    executionConsole(ckey);
                } finally {
                    ckey.close();
                }
            } else {

                // ファイル実行.
                executionFile(args);
            }
        } finally {
            if (dbFactory != null) {
                dbFactory.destroy();
            }
        }
    }

    /** エラー出力. **/
    protected final void error(String errMessage) {
        System.err.println(errMessage);
        System.exit(-1);
    }

    /**
     * ファイル実行.
     * 
     * @param args
     * @throws Exception
     */
    protected abstract void executionFile(String[] args) throws Exception;

    /**
     * コンソール実行.
     * 
     * @param in
     *            コンソール入力オブジェクトが設定されます.
     * @throws Exception
     */
    protected abstract void executionConsole(ConsokeInKey in) throws Exception;
}
