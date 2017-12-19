package origin.script.component;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import origin.script.CompileElement;
import origin.script.CompileManager;
import origin.script.HttpException;
import origin.util.Utils;

import origin.script.JsFunction;
import origin.script.OriginComponent;

/**
 * Require命令.
 */
public class RequireFunction extends JsFunction implements OriginComponent {
    private ScriptContext ctx;
    private ScriptEngine engine;
    private CompileManager man;

    public RequireFunction() {
    }

    public RequireFunction(ScriptContext ctx, ScriptEngine engine,
            CompileManager man) {
        this.ctx = ctx;
        this.engine = engine;
        this.man = man;
    }

    /**
     * このオブジェクトの生成に対して、Bindingsを設定する場合は[true].
     * 
     * @return boolean [true]の場合、オブジェクト生成時にBindingsが必要です.
     */
    @Override
    public boolean useBindings() {
        return true;
    }

    /**
     * このオブジェクトの生成に対して、ScriptContextを設定する場合は[true].
     * 
     * @return boolean [true]の場合、オブジェクト生成時にScriptContextが必要です.
     */
    @Override
    public boolean useScriptContext() {
        return true;
    }

    /**
     * シングルトンオブジェクトの場合は[true]を返却.
     * 
     * @return boolean [true]の場合は、シングルトンオブジェクトです.
     */
    @Override
    public boolean singleton() {
        return false;
    }

    /**
     * サーバモードで起動する場合は[true]を返却.
     * 
     * @return boolean [true]の場合、サーバモードで起動します.
     */
    @Override
    public boolean useServer() {
        return true;
    }

    /**
     * javascript登録オブジェクト名を取得.
     * 
     * @return String オブジェクト名が返却されます.
     */
    @Override
    public String getComponentName() {
        return "require";
    }

    @Override
    public String toString() {
        return "function require() { [native code] } ";
    }

    @Override
    public Object call(Object arg0, Object... arg1) {
        if (arg1 == null || arg1.length == 0) {
            return null;
        }
        try {
            String file = arg1[0].toString();
            if (!file.toLowerCase().endsWith(".js")) {
                file = file + ".js";
            }
            Bindings b = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
            b.put(ScriptEngine.FILENAME, file);

            // コンパイルマネージャが存在する場合.
            if (man != null) {
                CompileElement e = man.get(file);
                e.update();
                return e.getCompile().eval(ctx);

                // コンパイルマネージャが存在しない場合.
            } else {
                String js = Utils.getFileString(file, "UTF8");
                return engine.eval(js, ctx);
            }
        } catch (Exception e) {
            throw new HttpException(500, e);
        }
    }
}
