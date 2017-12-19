package origin.db;

import java.io.IOException;
import java.util.List;

import origin.conf.Config;
import origin.db.core.DbFactory;
import origin.db.kind.DbKind;
import origin.db.kind.KindFactory;
import origin.pref.Env;
import origin.script.OriginException;
import origin.util.sequence.Time16SequenceId;

/**
 * DB初期化.
 */
public final class InitDb {
    protected InitDb() {
    }

    private static final String DB_SECTION = "db";

    /**
     * DB初期化処理.
     * 
     * @parma conf 対象のコンフィグオブジェクトを設定します.
     * @param seq
     *            シーケンスID発行オブジェクトを設定します.
     * @return DbFactory DBファクトリオブジェクトが返却されます.
     * @exception Exception
     *                例外.
     */
    public static final DbFactory init(Config conf, Time16SequenceId seq)
            throws Exception {

        String section = Env.getEnvSection(conf, DB_SECTION);
        if (section == null) {
            throw new OriginException("InitDbの読み込みに失敗:" + section
                    + "セクションが存在しません");
        }

        DbKind kind = KindFactory.get(conf.get(section, "kind", 0));
        int fetchSize = conf.getInt(section, "fetchSize", 0);
        int batchSize = conf.getInt(section, "batchSize", 0);
        int maxPool = conf.getInt(section, "maxPool", 0);
        int timeout = conf.getInt(section, "timeout", 0);
        String defaults = conf.get(section, "register", 0);

        if (kind == null) {
            throw new IOException("dbコンフィグのkind:"
                    + conf.get(section, "kind", 0) + " はサポートされていません");
        }

        // DBファクトリを生成.
        DbFactory ret = new DbFactory(kind, seq, fetchSize, batchSize, defaults);

        // セクション名を取得.
        List<String> sections = Env.getNotEnvSections(conf, DB_SECTION);
        if (sections == null || sections.size() == 0) {
            return ret;
        }

        // 各プーリングDBを定義.
        int len = sections.size();
        for (int i = 0; i < len; i++) {

            // 環境にあったセクション名を取得.
            section = Env.getEnvSection(conf, sections.get(i));
            if (section == null) {
                continue;
            }
            initPooling(ret, section, conf, maxPool, timeout);
        }

        // DBマネージャに登録.
        DbManager.getInstance().setDbFactory(ret);
        DbManager.getInstance().setSequence(seq);
        return ret;
    }

    // プーリングの初期化.
    private static final void initPooling(DbFactory ret, String section,
            Config conf, int maxPool, int timeout) throws Exception {

        // 定義が利用不可な場合.
        if (conf.getBoolean(section, "use", 0) == false) {
            return;
        }

        DbKind kind = KindFactory.get(conf.get(section, "kind", 0));
        String url = conf.get(section, "url", 0);
        String user = conf.get(section, "user", 0);
        String passwd = conf.get(section, "passwd", 0);

        ret.register(Env.getNotEnvSection(section), kind, url, user, passwd,
                maxPool, (long) timeout);
    }

}