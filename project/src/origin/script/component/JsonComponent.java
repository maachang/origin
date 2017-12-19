package origin.script.component;

import java.io.IOException;

import origin.script.Json;

import origin.script.OriginComponent;

/**
 * Jsonオブジェクト変換.
 */
public class JsonComponent implements OriginComponent {
    public JsonComponent() {
    }

    private static final JsonComponent SNGL = new JsonComponent();

    /**
     * オブジェクトを取得.
     * 
     * @return JsonComponent オブジェクトが返却されます.
     */
    public static final JsonComponent getInstance() {
        return SNGL;
    }

    /**
     * このオブジェクトの生成に対して、Bindingsを設定する場合は[true].
     * 
     * @return boolean [true]の場合、オブジェクト生成時にBindingsが必要です.
     */
    @Override
    public boolean useBindings() {
        return false;
    }

    /**
     * このオブジェクトの生成に対して、ScriptContextを設定する場合は[true].
     * 
     * @return boolean [true]の場合、オブジェクト生成時にScriptContextが必要です.
     */
    @Override
    public boolean useScriptContext() {
        return false;
    }

    /**
     * シングルトンオブジェクトの場合は[true]を返却.
     * 
     * @return boolean [true]の場合は、シングルトンオブジェクトです.
     */
    @Override
    public boolean singleton() {
        return true;
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
        return "Json";
    }

    @Override
    public String toString() {
        return "[object " + getComponentName() + "]";
    }

    /**
     * エンコード.
     * 
     * @param o
     *            対象のオブジェクトを設定します.
     * @return String 文字列が返却されます.
     * @exception IOException
     *                I/O例外.
     */
    public String encode(Object o) throws IOException {
        return Json.encode(o);
    }

    /**
     * デコード.
     * 
     * @param o
     *            対象の文字列を設定します.
     * @return Object 変換されたJSONオブジェクトが返却されます.
     * @exception IOException
     *                I/O例外.
     */
    public Object decode(Object o) throws IOException {
        return Json.decode(o == null ? "null" : o.toString());
    }
}
