package origin.script;

/**
 * Originコンポーネント.
 */
public interface OriginComponent {
    
    /**
     * javascript登録オブジェクト名を取得.
     * @return String オブジェクト名が返却されます.
     */
    public String getComponentName();

    /**
     * このオブジェクトの生成に対して、Bindingsを設定する場合は[true].
     * @return boolean [true]の場合、オブジェクト生成時にBindingsが必要です.
     */
    public boolean useBindings();

    /**
     * このオブジェクトの生成に対して、ScriptContextを設定する場合は[true].
     * @return boolean [true]の場合、オブジェクト生成時にScriptContextが必要です.
     */
    public boolean useScriptContext();

    /**
     * シングルトンオブジェクトの場合は[true]を返却.
     * @return boolean [true]の場合は、シングルトンオブジェクトです.
     */
    public boolean singleton();
    
    /**
     * サーバモードで起動する場合は[true]を返却.
     * @return boolean [true]の場合、サーバモードで起動します.
     */
    public boolean useServer();
}
