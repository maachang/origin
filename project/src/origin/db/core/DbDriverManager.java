package origin.db.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import origin.db.kind.DbKind;

/**
 * DB接続ドライバーマネージャ. 他ユーティリティー関連.
 */
public final class DbDriverManager {
    private DbDriverManager() {
    }

    /** ブロックサイズ. **/
    private static final String BLOCK_SIZE = "512";

    /**
     * ドライバーマネージャにドライバ登録.
     * 
     * @param driver
     *            対象のドライバ名を設定します.
     * @exception Exception
     *                例外.
     */
    public static final void regDriver(String driver) throws Exception {
        Class.forName(driver);
    }

    /**
     * 読み込み専用コネクションの取得.
     * 
     * @param kind
     *            DbKindを設定します.
     * @param url
     *            対象の接続先を設定します.
     * @param user
     *            対象のユーザ名を設定します.
     * @param passwd
     *            対象のパスワードを設定します.
     * @return Connection コネクション情報が返却されます.
     * @exception Exception
     *                例外.
     */
    public static final Connection readOnly(DbKind kind, String url,
            String user, String passwd) throws Exception {
        Connection ret;
        Properties p = new java.util.Properties();
        kind.setProperty(p);

        url = checkURL(kind, url);
        if (user == null || user.length() <= 0) {
            p.put("user", "");
            p.put("password", "");
            ret = DriverManager.getConnection(url, p);
        } else {
            p.put("user", user);
            p.put("password", passwd);
            ret = DriverManager.getConnection(url, p);
        }
        // 初期処理.
        DbUtils.initConnection(kind, ret);

        ret.setReadOnly(true);
        ret.setAutoCommit(false);

        return ret;
    }

    /**
     * 読み書きコネクションの取得.
     * 
     * @param kind
     *            DbKindを設定します.
     * @param url
     *            対象の接続先を設定します.
     * @param user
     *            対象のユーザ名を設定します.
     * @param passwd
     *            対象のパスワードを設定します.
     * @return Connection コネクション情報が返却されます.
     * @exception Exception
     *                例外.
     */
    public static final Connection readWrite(DbKind kind, String url,
            String user, String passwd) throws Exception {
        Connection ret;
        Properties p = new java.util.Properties();
        kind.setProperty(p);
        p.put("block size", BLOCK_SIZE);

        url = checkURL(kind, url);
        if (user == null || user.length() <= 0) {
            p.put("user", "");
            p.put("password", "");
            ret = DriverManager.getConnection(url, p);
        } else {
            p.put("user", user);
            p.put("password", passwd);
            ret = DriverManager.getConnection(url, p);
        }
        // 初期処理.
        DbUtils.initConnection(kind, ret);

        ret.setReadOnly(false);
        ret.setAutoCommit(false);

        return ret;
    }

    /** URLチェック. **/
    private static final String checkURL(DbKind kind, String url) {
        url = url.trim();
        // URLヘッダが一致しない場合は付加する.
        if (!url.startsWith(kind.getUrlHead())) {
            return kind.getUrlHead() + url;
        }
        return url;
    }

}
