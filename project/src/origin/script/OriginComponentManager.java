package origin.script;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.script.Bindings;
import javax.script.ScriptContext;

import origin.pref.Mode;
import origin.util.atomic.AtomicObject;

import origin.script.OriginComponent;

/**
 * Originコンポーネント管理.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class OriginComponentManager {
    protected OriginComponentManager() {
    }

    private static final OriginComponentManager SNGL = new OriginComponentManager();

    public List<ComponentElement> newComponents = null;

    /** コンポーネント要素. **/
    private static final class ComponentElement {

        // クラス.
        public Class clazz;

        // コンポーネント名.
        public String componentName;

        // scriptContextをオブジェクト生成時に必要な場合は[true].
        public Boolean useScriptContext;

        // bindingsをオブジェクト生成時に必要な場合は[true].
        public Boolean useBindings;

        // シングルトンオブジェクトの場合は、対象オブジェクトが格納される.
        public OriginComponent singleton;
        
        // コンストラクタオブジェクト格納情報.
        public final AtomicObject<Constructor> constructor = new AtomicObject<Constructor>(null);
    }

    /**
     * オブジェクトの取得.
     * 
     * @return
     */
    public static final OriginComponentManager getInstance() {
        return SNGL;
    }

    /**
     * サービスローダーで、コンポーネントロード.
     */
    public static final void loadComponent() {
        OriginComponentManager man = getInstance();

        // サーバモードで起動している場合.
        if (Mode.SERVER) {
            for (OriginComponent o : ServiceLoader
                    .load(OriginComponent.class)) {

                // サーバで利用可能なコンポーネントのみロード.
                if (o.useServer()) {
                    man.register(o);
                }
            }
        } else {
            for (OriginComponent o : ServiceLoader
                    .load(OriginComponent.class)) {
                man.register(o);
            }
        }
    }

    /**
     * 新しいOriginComponentを登録.
     * 
     * @param component
     *            クラス名を設定します.
     * @return boolean [true]の場合処理成功です.
     */
    public boolean register(String component) {
        if (component == null) {
            return false;
        }
        try {
            return register(Class.forName(component));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 新しいOriginComponentを登録.
     * 
     * @param component
     *            クラスを設定します.
     * @return boolean [true]の場合処理成功です.
     */
    public boolean register(Class component) {
        if (component == null) {
            return false;
        }
        try {
            return register((OriginComponent) component.getConstructor().newInstance());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 新しいOriginComponentを登録.
     * 
     * @param component
     *            オブジェクトを設定します.
     * @return boolean [true]の場合処理成功です.
     */
    public boolean register(OriginComponent component) {
        if (component == null) {
            return false;
        }
        if (newComponents == null) {
            newComponents = new ArrayList<ComponentElement>();
        }
        ComponentElement o = new ComponentElement();
        o.clazz = component.getClass();
        o.componentName = component.getComponentName();
        o.useScriptContext = component.useScriptContext();
        o.useBindings = component.useBindings();
        o.singleton = (component.singleton() ? component : null);
        newComponents.add(o);
        return true;
    }

    // オブジェクト生成.
    private static final Object newInstance(Constructor c, Object... params) {
        try {
            if (params == null || params.length == 0) {
                return c.newInstance();
            } else if (params.length == 1) {
                return c.newInstance(params[0]);
            } else if (params.length == 2) {
                return c.newInstance(params[1], params[2]);
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Bindingsに情報をセット.
     * 
     * @param context
     *            対象のScriptContextを設定します.
     * @param bindings
     *            対象のBindingsを設定します.
     */
    public void setBindings(ScriptContext context, Bindings bindings) {
        if (newComponents == null) {
            return;
        }
        Constructor cc;
        OriginComponent c;
        int len = newComponents.size();
        ComponentElement o;
        for (int i = 0; i < len; i++) {
            try {
                if ((o = newComponents.get(i)) == null) {
                    continue;
                }
                // 既に登録されている場合は登録しない.
                if (bindings.containsKey(o.componentName)) {
                    continue;
                }
                // シングルトンの場合は、そのままセット.
                if (o.singleton != null) {
                    bindings.put(o.componentName, o.singleton);
                    
                // オブジェクトの作成が必要な場合.
                // ScriptContextが必要な場合.
                } else if (o.useScriptContext) {
                    
                    // Bindingsオブジェクトが必要な場合.
                    if (o.useBindings) {
                        cc = o.constructor.get();
                        if(cc == null) {
                            cc = o.clazz.getConstructor(ScriptContext.class, Bindings.class);
                            o.constructor.put(cc);
                        }
                        c = (OriginComponent) newInstance(cc, context, bindings);
                        if (c != null) {
                           bindings.put(c.getComponentName(), c);
                        }
                    // ScriptContextのみが必要な場合.
                    } else {
                        cc = o.constructor.get();
                        if(cc == null) {
                            cc = o.clazz.getConstructor(ScriptContext.class);
                            o.constructor.put(cc);
                        }
                        c = (OriginComponent) newInstance(cc, context);
                        if (c != null) {
                            bindings.put(c.getComponentName(), c);
                        }
                    }
                // Bindingsのみが必要な場合.
                } else if (o.useBindings) {
                    cc = o.constructor.get();
                    if(cc == null) {
                        cc = o.clazz.getConstructor(Bindings.class);
                        o.constructor.put(cc);
                    }
                    c = (OriginComponent) newInstance(cc, bindings);
                    if (c != null) {
                        bindings.put(c.getComponentName(), c);
                    }
                // 何も必要でない場合.
                } else {
                    cc = o.constructor.get();
                    if(cc == null) {
                        cc = o.clazz.getConstructor();
                        o.constructor.put(cc);
                    }
                    c = (OriginComponent) newInstance(cc);
                    if (c != null) {
                        bindings.put(c.getComponentName(), c);
                    }
                }
            } catch(Exception e) {
            }
        }
    }
}
