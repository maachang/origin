package origin;

import java.util.HashMap;

import javax.script.Bindings;

import origin.db.DbCreateBaseDao;
import origin.db.core.DbFactory;
import origin.net.NetUtil;
import origin.pref.Def;
import origin.pref.Env;
import origin.pref.Mode;
import origin.script.AbstractConsole;
import origin.script.ExecuteScript;
import origin.script.OriginBindings;
import origin.script.OriginComponentManager;
import origin.script.OriginRegisterService;
import origin.util.ConsokeInKey;
import origin.util.Utils;

/**
 * JSコンソール起動. もしくは、第一引数のファイルを読み込み実行.
 */
public class OriginConsole extends AbstractConsole {

    /** Main. **/
    public static final void main(String[] args) {
        try {
            NetUtil.initNet();
            OriginComponentManager.loadComponent();
            OriginConsole console = new OriginConsole();
            OriginRegisterService.getInstance().initAll(null);
            
            System.out.println("origin console version " + Def.VERSION +
                    " env:" + Env.ORIGIN_ENV.getName());
            
            try {
                console.execute(args);
            } finally {
                OriginRegisterService.getInstance().closeAll(null);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // 初期起動スクリプト.
    private static final String INIT_SCRIPT = "\"use strict\"\n"
            + ExecuteScript.BASE_SCRPIT;

    // コンソールファイル名.
    private static final String CONSOLE_FILE_NAME = "console";

    // 初期起動スクリプト.
    private static final Bindings initScript(DbFactory f) throws Exception {
        OriginBindings ret = new OriginBindings(f, new DbCreateBaseDao());
        ret.put(Def.SHARE_MEMORY,new HashMap<String,Object>());
        ExecuteScript.eval(INIT_SCRIPT, CONSOLE_FILE_NAME, ret);
        return ret;
    }

    // ファイル実行.
    protected void executionFile(String[] args) throws Exception {
        String file = args[0];
        if (!Utils.isFile(file)) {
            error("実行対象ファイル:" + file + " は存在しません.");
        }

        Bindings b = initScript(dbFactory);

        String js = Utils.getFileString(file, "UTF8");
        ExecuteScript.eval(js, file, b);
    }

    // コンソール実行.
    protected void executionConsole(ConsokeInKey in) throws Exception {
        Bindings b = initScript(dbFactory);
        String cmd;

        Mode.DEBUG_SQL = true;

        while (true) {
            try {
                if ((cmd = in.readLine("js> ")) == null) {
                    return;
                } else if ((cmd = cmd.trim()).length() == 0) {
                    continue;
                } else if ("exit".equals(cmd) || "quit".equals(cmd)) {
                    System.out.println("exit.");
                    return;
                }
                Object o = ExecuteScript.eval(cmd, CONSOLE_FILE_NAME, b);
                System.out.println(o);

            } catch (Throwable e) {
                //System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
