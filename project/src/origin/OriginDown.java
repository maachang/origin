package origin;

import origin.conf.Config;
import origin.pref.Def;
import origin.util.Utils;
import origin.util.shutdown.ShutdownClient;

/**
 * サーバシャットダウン処理.
 */
public final class OriginDown {
    private OriginDown() {
    }

    /** サーバー停止処理. **/
    public static final void main(String[] args) throws Exception {

        // パラメータからのポート指定がない場合は、サーバ定義を読み込み、
        // そこからポート番号で処理.
        if (args == null || args.length == 0) {

            // コンフィグファイルが存在するかチェック.
            Config conf = new Config();
            if (!Config.read(conf, Def.CONF_NAME)) {
                System.out.println("file:" + Def.CONF_FILE + "の読み込みに失敗.");
                return;
            }
            int shutdownPort = conf.getInt("origin", "shutdownPort", 0);

            ShutdownClient.send(shutdownPort);
        }
        // パラメータ指定されている場合は、その内容を利用する.
        else {
            int port = Utils.convertInt(args[0]);
            ShutdownClient.send(port);
        }
    }
}
