package origin.script;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jdk.nashorn.api.scripting.JSObject;


/**
 * JSインスタンス生成.
 * 
 * js上において、new Object() のようにnew 処理で新しい
 * インスタンスを定義できます.
 */
public abstract class JsNewInstance implements JSObject {
    
    /**
     * インスタンス生成.
     *
     * new Object("a","b","c") のような感じの呼び出し.
     * @param arg0 パラメータを設定します.
     * @return Object 生成されたオブジェクトが返却されます.
     */
    @Override
    public abstract Object newObject(Object... arg0);
    
    /**
     * オブジェクトの型が一致するかチェック.
     *
     * @param arg0 オブジェクトが設定されます.
     */
    @Override
    public boolean isInstance(Object arg0) {
        if(arg0 == null) {
            return false;
        }
        return this.getClass().equals(arg0.getClass());
    }

    /**
     * オブジェクトの型が一致するかチェック.
     *
     * @param arg0 Classオブジェクトが設定されます.
     */
    @Override
    public boolean isInstanceOf(Object arg0) {
        if(arg0 == null) {
            return false;
        }
        return this.getClass().equals(arg0);
    }

    @Override
    public Object call(Object arg0, Object... arg1) {
        return null;
    }

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
        return false;
    }
    
    @Override
    public boolean isStrictFunction() {
        return false;
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<String>();
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
