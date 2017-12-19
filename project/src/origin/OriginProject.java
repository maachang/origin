package origin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import origin.db.kind.DbKind;
import origin.db.kind.KindByH2;
import origin.db.kind.KindByHsql;
import origin.db.kind.KindBySqlite;
import origin.db.kind.KindFactory;
import origin.pref.Def;
import origin.util.Utils;

/**
 * 新しいoriginプロジェクトを作成.
 */
public class OriginProject {

    // 新規プロジェクト雛形作成.
    public static final void main(String[] args) throws Exception {

        // プロジェクトバイナリディレクトリに対して、プロジェクトの環境変数が存在しない.
        String srcPath = System.getenv(Def.PROJECT_ENV_NAME);
        if (srcPath == null) {
            System.out.println("引数が足りません");
            return;
        }
        if (!Utils.isDir(srcPath)) {
            System.out.println("Origin プロジェクトディレクトリ(env)が見つかりません:" + srcPath);
            return;
        }

        boolean windowsFlag = "\\".equals(System.getProperty("file.separator"));

        // originバイナリ元フォルダ.
        if (windowsFlag) {
            srcPath = "/" + Utils.changeString(srcPath, "\\", "/");
        }
        if (!srcPath.endsWith("/")) {
            srcPath += "/";
        }

        // フォルダ定義.
        String[] folders = new String[] { "conf", "jar", "lib/jspec", "log",
                "test", "application", Def.NASHORN_CACHE_DIR };

        // コピーファイル定義.
        String[] copyFiles = new String[] { "application/index.js",
                "conf/origin.conf", "conf/db.conf", "conf/log4j.xml" };

        // コピーファイル定義(src,dest).
        String[] copySrcDest = new String[] { "js/jspec/jspec.js",
                "lib/jspec/jspec.js", "js/jspec/report.js",
                "lib/jspec/report.js" };

        // コマンド実行用ファイル.
        String[] cmdFiles = null;
        if (windowsFlag) {
            // windows.
            cmdFiles = new String[] { "origin.cmd", "ocon.cmd", "osql.cmd",
                    "otest.cmd" };
        } else {
            // linux.
            cmdFiles = new String[] { "origin", "ocon", "osql", "otest" };
        }

        // フォルダ作成.
        int len = folders.length;
        for (int i = 0; i < len; i++) {
            Utils.mkdirs(folders[i]);
        }

        // ファイルコピー.
        len = copyFiles.length;
        for (int i = 0; i < len; i++) {
            copyFile(srcPath + copyFiles[i], copyFiles[i]);
            System.out.println("file:" + copyFiles[i] + " をコピー");
        }
        len = cmdFiles.length;
        for (int i = 0; i < len; i++) {
            copyFile(srcPath + "sh/" + cmdFiles[i], cmdFiles[i]);
            System.out.println("cmdFile:" + copyFiles[i] + " をコピー");
        }
        len = copySrcDest.length;
        for (int i = 0; i < len; i += 2) {
            copyFile(srcPath + copySrcDest[i], copySrcDest[i + 1]);
            System.out.println("file:" + srcPath + copySrcDest[i] + " を "
                    + copySrcDest[i + 1] + " にコピー");
        }

        // 第二引数に特定のDB定義が行われている.
        if (args.length < 1) {
            return;
        }

        String db = args[0];
        DbKind kind = KindFactory.get(db);
        if (kind == null) {
            System.out.println("名前:" + db + " のデータベースシステムはサポート外です");
            return;
        }

        // ドライバファイルが存在する場合は、コピー.
        String driverDir = srcPath + "/driver/";
        String driverName = getDriverFile(driverDir, db);
        if (driverName == null) {
            return;
        }

        // ドライバファイルをコピー.
        copyFile(driverDir + driverName, "jar/" + driverName);
        System.out.println("jdbcDriver:" + driverName + " をコピー");

        // ローカルDBの場合.
        if (kind.isLocalDb()) {
            Utils.mkdirs("db");

            // ローカルDBの場合は、初期定義のconfファイルを渡す.
            if (kind instanceof KindByH2) {
                copyFile(srcPath + "conf/db/h2.conf", "conf/db.conf");
            } else if (kind instanceof KindBySqlite) {
                copyFile(srcPath + "conf/db/sqlite.conf", "conf/db.conf");
            } else if (kind instanceof KindByHsql) {
                copyFile(srcPath + "conf/db/hsqldb.conf", "conf/db.conf");
            }
        }

        System.out.println();
        System.out.println("プロジェクトの雛形を作成");
    }

    // ファイルコピー.
    private static final void copyFile(String src, String dest)
            throws Exception {
        int len;
        byte[] buf = new byte[4096];
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dest);
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            in.close();
            in = null;
            out.close();
            out = null;
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }

    // 指定フォルダ内ファイル検索.
    private static final String getDriverFile(String dir, String headName)
            throws Exception {
        headName = headName.toLowerCase();
        String[] list = new File(dir).list();
        int len = list.length;
        for (int i = 0; i < len; i++) {
            String f = list[i].toLowerCase();
            if (f.endsWith(".jar") && f.startsWith(headName)) {
                return list[i];
            }
        }
        return null;
    }
}
