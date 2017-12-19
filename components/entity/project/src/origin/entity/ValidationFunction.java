package origin.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.script.Bindings;

import origin.net.http.HttpRequest;
import origin.pref.Def;
import origin.script.HttpException;
import origin.script.JsFunction;
import origin.script.OriginComponent;
import origin.util.Alphabet;
import origin.util.Utils;

/**
 * パラメータバリデーション命令.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ValidationFunction extends JsFunction implements OriginComponent {
    private Bindings bindings;

    /**
     * コンストラクタ.
     */
    public ValidationFunction() {
    }

    /**
     * コンストラクタ.
     * 
     * @parma b 対象のバインディングを設定します.
     */
    public ValidationFunction(Bindings b) {
        this.bindings = b;
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
        return false;
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
        return "validation";
    }

    @Override
    public String toString() {
        return "function validation() { [native code] } ";
    }

    /**
     * functionコール. 使い方イメージ.
     *
     * validation(
     *     "name",          "String", "not null",   // name文字パラメータで、必須情報.
     *     "age",           "Number", "",           // age数値パラメータ.
     *     "comment",       "String", "max 128",    // comment文字パラメータで、最大文字が128文字.
     *     "X-Test-Code",   "String", "not null"    // X-Test-CodeHttpヘッダパラメータで、必須.
     * );
     * 
     * また、先頭に[method]を設定した場合、許可する条件として設定します. ※何も定義していない場合は、全てのmethodが有効です.
     *
     * validatiom( "POST", ・・・・・・・ );
     * 
     * 上記の場合、POSTのみ許可.
     */
    @Override
    public Object call(Object arg0, Object... arg1) {
        if (arg1 == null || arg1.length == 0) {
            return null;
        }
        int len = arg1.length;
        HttpRequest request = (HttpRequest) bindings.get(Def.SCRIPT_REQUEST);
        String method = request.getMethod();

        // method許可チェック.
        int off = 0;
        boolean eqMethod = false;
        for (int i = 0; i < len; i++) {
            if (postOrGet(arg1[i])) {
                if (Alphabet.eq(method,Utils.convertString(arg1[0]))) {
                    eqMethod = true;
                }
                off++;
            } else {
                break;
            }
        }
        if (off != 0 && !eqMethod) {
            HttpException.error(405, method + " メソッドは許可されていません");
        }

        // validate処理.
        Map<String, Object> newParams = new LinkedHashMap<String, Object>();
        Map<String, Object> params = (Map) bindings.get(Def.SCRIPT_PARAMS);
        for (int i = off; i < len; i += 3) {
            validate(newParams, params, request,
                    Utils.convertString(arg1[i + 0]),
                    Utils.convertString(arg1[i + 1]),
                    Utils.convertString(arg1[i + 2]));
        }
        bindings.put(Def.SCRIPT_PARAMS, newParams);

        return newParams;
    }

    // POST,GET指定.
    private static final boolean postOrGet(Object n) {
        return "POST".equals(n) || "GET".equals(n);
    }

    // validation処理.
    private static final void validate(Map<String, Object> newParams,
            Map<String, Object> params, HttpRequest request, String column,
            String type, String conditions) {

        Object value = null;
        if (!Utils.useString(column)) {
            HttpException.error(500, "validationのカラム名が不正です");
        }
        if (!Utils.useString(type)) {
            type = "string";
        } else {
            type = type.toLowerCase();
        }
        if (!Utils.useString(conditions)) {
            conditions = null;
        }

        // Httpヘッダ情報.
        if (column.startsWith("X-")) {
            try {
                // HTTPヘッダ情報を取得.
                value = request.getHeader(column);
                
                // カラム名の[X-]を取り、-を抜いて、最初の文字を小文字に変換.
                // [X-Test-Code] -> testCode.
                column = Utils.changeString(column.substring(2),"-","");
                column = column.substring(0,1).toLowerCase() + column.substring(1);
            } catch (Exception e) {
                HttpException.error(500, "ヘッダ情報 " + column + " の取得に失敗しました", e);
            }

            // パラメータ情報.
        } else {
            value = params.get(column);
        }

        // データ変換.
        value = TypeConvert.convert(column, type, value);

        // データチェック.
        String[] renameColumn = new String[]{null};
        value = ConditionsChecker.check(renameColumn, column, type, value, conditions);
        if(renameColumn[0] != null) {
            column = renameColumn[0];
        }

        // データセット.
        newParams.put(column, value);
    }
}
