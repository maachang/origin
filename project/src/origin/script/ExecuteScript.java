package origin.script;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import origin.pref.Def;
import origin.pref.Mode;
import origin.script.component.ConfigFunction;
import origin.script.component.ConsoleComponent;
import origin.script.component.HttpComponent;
import origin.script.component.JsonComponent;
import origin.script.component.LockComponent;
import origin.script.component.ReadWriteLockComponent;
import origin.script.component.RequireFunction;
import origin.script.component.SleepFunction;
import origin.script.component.SynchronizedFunction;
import origin.util.Utils;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import origin.script.OriginComponent;

/**
 * スクリプト実行処理.
 */
public class ExecuteScript {
    protected ExecuteScript() {
    }

    // スクリプトエンジン生成.
    protected static final ScriptEngine engine;

    /**
     * スクリプトエンジン初期処理.
     */
    static {
        ScriptEngine e = null;
        // nashorn.
        // java8用のjs.
        try {

            // nashorn.persistent.code.cacheで、キャッシュ先をセット.
            // -ccs class cache size : グローバルクラスキャッシュサイズ.
            // -ot optimisitic type : 内部で持つ型の最適化.
            // -pcc persistent code cach : 楽観的型付情報もキャッシュされる.

            // キャッシュフォルダが設定されている場合.
            if (System.getProperty(Def.NASHORN_CACHE_DIR_PROPERTY) != null) {
                e = new NashornScriptEngineFactory()
                        .getScriptEngine(new String[] { "-ccs=" + Mode.CLASS_CACHE_SIZE,
                                "-ot=true", "-pcc=true" });

                // キャッシュフォルダが設定されていない場合.
            } else {
                e = new NashornScriptEngineFactory()
                        .getScriptEngine(new String[] { "-ot=false",
                                "-pcc=false" });
            }
        } catch (Exception n) {
            ScriptEngineManager manager = new ScriptEngineManager();
            e = manager.getEngineByName("js");
        }
        engine = e;
    }

    /**
     * 対象JSをコンパイル.
     * 
     * @param man
     * @param file
     * @return CompileElement
     * @throws Exception
     */
    public static final CompileElement compile(CompileManager man, String file)
            throws Exception {
        Compilable compilable = (Compilable) engine;
        String js = loadScript(file);
        CompiledScript c = compilable.compile(js);
        return new CompileElement(c, man, file, Utils.getFileTime(file));
    }

    /**
     * コンパイル結果を実行.
     * 
     * @param c
     * @param b
     * @return Object
     * @throws Exception
     */
    public static final Object execute(CompileElement c, Bindings b)
            throws Exception {
        if (b == null) {
            b = new SimpleBindings();
        }
        b.put(ScriptEngine.FILENAME, c.getFileName());
        ScriptContext context = new SimpleScriptContext();
        setBindings(context, b, c.getManager());
        context.setBindings(b, ScriptContext.ENGINE_SCOPE);
        return c.getCompile().eval(context);
    }

    /**
     * コンパイルせずに、直接実行.
     * 
     * @param file
     * @param b
     * @return Object
     * @throws Exception
     */
    public static final Object execute(String file, Bindings b)
            throws Exception {
        String js = loadScript(file);
        return eval(js, file, b);
    }

    /**
     * コンパイルせずに、直接実行.
     * 
     * @param script
     * @param file
     * @param b
     * @return Object
     * @throws Exception
     */
    public static final Object eval(String script, String file, Bindings b)
            throws Exception {
        if (b == null) {
            b = new SimpleBindings();
        }
        b.put(ScriptEngine.FILENAME, file);
        ScriptContext context = new SimpleScriptContext();
        setBindings(context, b, null);
        context.setBindings(b, ScriptContext.ENGINE_SCOPE);
        return engine.eval(script, context);
    }

    // bindingsにコンポーネント関連をセット.
    private static final void setBindings(ScriptContext ctx, Bindings b,
            CompileManager man) {
        OriginComponent c = new ConsoleComponent(b);
        if (!b.containsKey(c.getComponentName())) {
            b.put(c.getComponentName(), c);
        }

        c = JsonComponent.getInstance();
        if (!b.containsKey(c.getComponentName())) {
            b.put(c.getComponentName(), c);
        }

        c = new HttpComponent(b);
        if (!b.containsKey(c.getComponentName())) {
            b.put(c.getComponentName(), c);
        }

        c = new RequireFunction(ctx, engine, man);
        if (!b.containsKey(c.getComponentName())) {
            b.put(c.getComponentName(), c);
        }

        c = SleepFunction.getInstance();
        if (!b.containsKey(c.getComponentName())) {
            b.put(c.getComponentName(), c);
        }

        c = ConfigFunction.getInstance();
        if (!b.containsKey(c.getComponentName())) {
            b.put(c.getComponentName(), c);
        }

        c = LockComponent.getInstance();
        if (!b.containsKey(c.getComponentName())) {
            b.put(c.getComponentName(), c);
        }

        c = ReadWriteLockComponent.getInstance();
        if (!b.containsKey(c.getComponentName())) {
            b.put(c.getComponentName(), c);
        }

        c = SynchronizedFunction.getInstance();
        if (!b.containsKey(c.getComponentName())) {
            b.put(c.getComponentName(), c);
        }

        // プラグインコンポーネントの展開.
        OriginComponentManager.getInstance().setBindings(ctx, b);
    }

    // 起動時実行スクリプト.
    public static final String BASE_SCRPIT = "var global=this;";

    // スクリプトファイル取得.
    protected static final String loadScript(String file) throws Exception {
        String ret = Utils.getFileString(file, "UTF8");
        return new StringBuilder().append(BASE_SCRPIT).append("(function(){")
            .append("var ret = (function(global){\n\"use strict\";\n")
            .append(ret)
            .append("\n})(global);\n")
            .append("if(typeof(ret) == 'string') {\n")
            .append("  response['Content-Type'] = 'text/html; charset=UTF-8';\n")
            .append("  return ret;\n")
            .append( "}\n")
            .append("return Json.encode(ret);\n")
            .append("})();").toString();
    }
}
