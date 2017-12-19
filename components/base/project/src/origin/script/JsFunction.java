package origin.script;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jdk.nashorn.api.scripting.JSObject;


/**
 * JSファンクション.
 *
 * js処理で、functionで定義したものと同様の処理で呼び出せます.
 * このオブジェクトを継承して、call処理を実装することで、functionとして
 * 利用できます.
 */
public abstract class JsFunction implements JSObject {

    /**
     * ファンクションコール.
     *
     * function xxx() { ... } のように、functionと同じように呼び出されます.
     * @param arg0 親オブジェクトが設定されます.
     * @param arg1 functionのパラメータ群が設定されます.
     * @return Object 処理結果を返却します.
     */
    @Override
    public abstract Object call(Object arg0, Object... arg1);

    @Override
    public Object eval(String arg0) {
        return null;
    }

    @Override
    public String getClassName() {
        return "";
    }

    @Override
    public Object getMember(String arg0) {
        return null;
    }

    @Override
    public Object getSlot(int arg0) {
        return null;
    }

    @Override
    public boolean hasMember(String arg0) {
        return false;
    }

    @Override
    public boolean hasSlot(int arg0) {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public boolean isInstance(Object arg0) {
        return false;
    }

    @Override
    public boolean isInstanceOf(Object arg0) {
        return false;
    }

    @Override
    public boolean isStrictFunction() {
        return true;
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<String>();
    }

    @Override
    public Object newObject(Object... arg0) {
        return null;
    }

    @Override
    public void removeMember(String arg0) {
    }

    @Override
    public void setMember(String arg0, Object arg1) {
    }

    @Override
    public void setSlot(int arg0, Object arg1) {
    }

    @Override
    public double toNumber() {
        return 0;
    }

    @Override
    public Collection<Object> values() {
        return null;
    }
}
