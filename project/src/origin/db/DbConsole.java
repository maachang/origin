package origin.db;

import java.io.IOException;
import java.util.List;

import origin.db.core.DbConnection;
import origin.db.core.DbResult;
import origin.db.core.DbStatement;
import origin.pref.Def;
import origin.pref.Env;
import origin.script.AbstractConsole;
import origin.util.ConsokeInKey;
import origin.util.Utils;

/**
 * JDBC経由のデータベースコンソール環境.
 */
public class DbConsole extends AbstractConsole {

    private static final int MAX_RESULT_LIMIT = 100;

    /** Main. **/
    public static final void main(String[] args) {
        try {
            DbConsole console = new DbConsole();
            
            System.out.println("origin sql console version " + Def.VERSION +
                    " env:" + Env.ORIGIN_ENV.getName());
            
            console.execute(args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // ファイル実行.
    protected void executionFile(String[] args) throws Exception {
        String file = args[0];
        String charset = "UTF8";
        if (args.length >= 2) {
            charset = args[1];
        }
        String dbName = null;
        DbConnection conn = null;
        DbStatement stmt = null;
        try {
            dbName = dbFactory.getDefaultPool();
            String sql = Utils.getFileString(file, charset);
            sql = cutComment(sql);

            // endpoint単位でSQL実行.
            int p, b;
            b = 0;
            String o, ol;
            while ((p = endPoint(sql, b)) != -1) {
                o = sql.substring(b, p).trim();
                ol = o.toLowerCase();
                b = p + 1;

                // databaseの変更.
                if (ol.startsWith("database")) {
                    String name = changeDatabaseCommand(o);

                    // 指定データベース名が存在しない.
                    if (!dbFactory.contains(name)) {
                        throw new IOException("指定データベース名:" + name + " は存在しません");
                    }

                    // 変更元と変更先が同じ場合.
                    if (dbName.equals(name)) {
                        continue;
                    }

                    // 現在のDBをクローズ.
                    if (conn != null) {
                        conn.close();
                        conn = null;
                    }

                    // 現在のDBオープン名を変更.
                    stmt = null;
                    dbName = name;

                } else {

                    // connectionが存在しない場合は生成.
                    if (conn == null) {
                        conn = dbFactory.getConnection(dbName);
                    }

                    // commit.
                    if (ol.equals("commit")) {
                        conn.commit();

                        // rollback.
                    } else if (ol.equals("rollback")) {
                        conn.rollback();

                        // 通常のSQL処理.
                    } else {

                        if (stmt == null) {
                            stmt = conn.statement();
                        }

                        // 改行があれば、スペースに変換.
                        ol = null;
                        o = Utils.changeString(o, "\r", "");
                        o = Utils.changeString(o, "\n", " ");
                        o += ";";

                        // SQL実行.
                        List<Exception> res = stmt.each(o);
                        if (res != null) {
                            for (int i = 0; i < res.size(); i++) {
                                System.err.println("error[" + (i + 1) + "]:"
                                        + res.get(i).getMessage());
                            }
                            return;
                        }
                    }
                }
            }
            if (conn != null) {
                conn.close();
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    // コメント除去.
    private static final String cutComment(String sql) throws Exception {
        if (sql == null || sql.length() <= 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        int len = sql.length();
        int cote = -1;
        int commentType = -1;
        int bef = -1;
        char c, c2;
        for (int i = 0; i < len; i++) {
            if (i != 0) {
                bef = sql.charAt(i - 1);
            }
            c = sql.charAt(i);
            // コメント内の処理.
            if (commentType != -1) {
                switch (commentType) {
                case 1: // １行コメント.
                    if (c == '\n') {
                        buf.append(c);
                        commentType = -1;
                    }
                    break;
                case 2: // 複数行コメント.
                    if (c == '\n') {
                        buf.append(c);
                    } else if (len > i + 1 && c == '*'
                            && sql.charAt(i + 1) == '/') {
                        i++;
                        commentType = -1;
                    }
                    break;
                }
                continue;
            }
            // シングル／ダブルコーテーション内の処理.
            if (cote != -1) {
                if (c == cote && (char) bef != '\\') {
                    cote = -1;
                }
                buf.append(c);
                continue;
            }
            // コメント(// or /* ... */).
            if (c == '/') {
                if (len <= i + 1) {
                    buf.append(c);
                    continue;
                }
                c2 = sql.charAt(i + 1);
                if (c2 == '*') {
                    commentType = 2;
                    continue;
                } else if (c2 == '/') {
                    commentType = 1;
                    continue;
                }
            }
            // コメント(--)
            else if (c == '-') {
                if (len <= i + 1) {
                    buf.append(c);
                    continue;
                }
                c2 = sql.charAt(i + 1);
                if (c2 == '-') {
                    commentType = 1;
                    continue;
                }
            }
            // コーテーション開始.
            else if ((c == '\'' || c == '\"') && (char) bef != '\\') {
                cote = (int) (c & 0x0000ffff);
            }
            buf.append(c);
        }
        return buf.toString();
    }

    private static final int endPoint(String sql, int off) {
        int len = sql.length();
        if (len <= off) {
            return -1;
        }
        int cote = -1;
        int b = -1;
        char c;
        for (int i = off; i < len; i++) {
            // コーテーション内.
            c = sql.charAt(i);
            if (cote != -1) {
                if (cote == c && b != '\\') {
                    cote = -1;
                }
                // コーテーション開始.
            } else if (c == '\'' || c == '\"') {
                cote = c;
                // 終端.
            } else if (c == ';') {
                return i;
            }
            b = c;
        }
        return -1;
    }

    // database変更コマンドを取得.
    protected static final String changeDatabaseCommand(String o) {
        int n = o.indexOf("=");
        if (n == -1) {
            n = 8;
        } else {
            n += 1;
        }
        String ret = o.substring(n).trim();
        if (ret.startsWith("\"") || ret.startsWith("\'")) {
            ret = ret.substring(1, ret.length() - 1).trim();
        }
        return ret;
    }

    // コンソール実行.
    protected void executionConsole(ConsokeInKey in) throws Exception {
        int len;
        String cmd, lcmd;
        DbResult result = null;
        DbConnection conn = null;
        DbStatement stmt = null;
        String dbName = dbFactory.getDefaultPool();

        try {
            while (true) {
                try {
                    lcmd = null;
                    if ((cmd = in.readLine("sql> ")) == null) {
                        return;
                    } else if ((cmd = cmd.trim()).length() == 0) {
                        continue;
                    }
                    if (cmd.endsWith(";")) {
                        cmd = cmd.substring(0, cmd.length() - 1).trim();
                    }
                    lcmd = cmd.toLowerCase();
                    if ("exit".equals(lcmd) || "quit".equals(lcmd)) {
                        System.out.println("exit.");
                        return;
                    }

                    // databaseの変更.
                    if (lcmd.startsWith("database")) {
                        String name = changeDatabaseCommand(cmd);

                        // 指定データベース名が存在しない.
                        if (!dbFactory.contains(name)) {

                            // データベース名一覧を取得.
                            if (name.length() == 0) {
                                List<String> names = dbFactory.getNames();
                                len = names.size();
                                for (int i = 0; i < len; i++) {
                                    System.out.println(names.get(i));
                                }
                                continue;
                            }
                            System.out
                                    .println("指定データベース名:" + name + " は存在しません");
                        }

                        // 変更元と先が同じ場合.
                        if (dbName.equals(name)) {
                            System.out.println("変更元と先が同じです:" + name);
                            continue;
                        }

                        // 現在のDBをクローズ.
                        if (conn != null) {
                            conn.close();
                            conn = null;
                        }

                        // 現在のDBオープン名を変更.
                        stmt = null;
                        dbName = name;

                        System.out.println("データベースを切り替えました:" + name);
                    }

                    // connectionが存在しない場合は生成.
                    if (conn == null) {
                        conn = dbFactory.getConnection(dbName);
                    }

                    // commit.
                    if (lcmd.equals("commit")) {
                        conn.commit();
                        System.out.println("success");
                        continue;
                    }
                    // rollback.
                    if (lcmd.equals("rollback")) {
                        conn.rollback();
                        System.out.println("success");
                        continue;
                    }

                    // sqlの終端をセット.
                    cmd += ";";

                    if (stmt == null) {
                        stmt = conn.statement();
                    }

                    // select系の処理.
                    if (lcmd.startsWith("select ")) {
                        result = stmt.query(cmd, MAX_RESULT_LIMIT);
                        int cnt = 0;
                        while (result.hasNext()) {
                            cnt++;
                            System.out.println(result.next());
                        }
                        System.out.println(cnt + "件");
                        result.close();
                        result = null;
                        continue;
                    }

                    // 通常のSQL.
                    List<Exception> err = stmt.each(cmd);
                    if (err != null) {
                        len = err.size();
                        for (int i = 0; i < len; i++) {
                            System.out.println("error" + (i + 1) + ":"
                                    + err.get(i).getMessage());
                        }
                    } else {
                        System.out.println("success");
                    }
                } catch (Throwable e) {
                    System.out.println("error:" + e.getMessage());
                } finally {
                    if (result != null) {
                        result.close();
                        result = null;
                    }
                }
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
