package origin.conf;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import origin.util.Utils;

/**
 * Configオブジェクトキャッシュ処理.
 */
public final class ConfigCache {
    protected ConfigCache() {}
    protected static final ConfigCache SNGL = new ConfigCache();
    
    /**
     * オブジェクトを取得.
     * @return ConfigCache オブジェクトが返却されます.
     */
    public static final ConfigCache getInstance() {
        return SNGL;
    }
    
    /** キャッシュクラス. **/
    private static final class Cache {
        private long lastTime;
        private Config config;
    }
    
    /**
     * コンフィグキャッシュ.
     */
    protected final Map<String,Cache> cache = new ConcurrentHashMap<String,Cache>();
    
    /** コンフィグキャッシュを取得. **/
    protected Cache _getCache(String fileName,boolean noCache) throws Exception {
        fileName = Utils.getFullPath(fileName);
        Cache c = null;
        if(noCache && (c = cache.get(fileName)) != null) {
            long tm = Utils.getFileTime(fileName);
            if(tm != c.lastTime) {
                c = null;
            }
        }
        if(c == null) {
            c = new Cache();
            c.config = Config.read(fileName);
            c.lastTime = Utils.getFileTime(fileName);
            cache.put(fileName,c);
        }
        return c;
    }
    
    /**
     * コンフィグ情報を取得.
     * @param fileName 対象のファイル名を設定します.
     * @return Config コンフィグ情報が返却されます.
     * @exception Exception 例外.
     */
    public Config get(String fileName) throws Exception {
        Cache ret = _getCache(fileName,false);
        if(ret == null) {
            return null;
        }
        return ret.config;
    }
    
    /**
     * コンフィグ情報をキャッシュせずに取得.
     * @param fileName 対象のファイル名を設定します.
     * @return Config コンフィグ情報が返却されます.
     * @exception Exception 例外.
     */
    public Config getNoCache(String fileName) throws Exception {
        Cache ret = _getCache(fileName,true);
        if(ret == null) {
            return null;
        }
        return ret.config;
    }
}
