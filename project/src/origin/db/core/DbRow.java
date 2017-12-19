package origin.db.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 結果情報を示すMapオブジェクト.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public interface DbRow extends Map<String, Object> {

    /**
     * 生成処理.
     * 
     * @return result 結果オブジェクトを設定します.
     */
    public void create(DbResult result);

    /**
     * 情報クリア.
     */
    public void clear();

    /**
     * 指定項番で情報取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Object 対象の情報が返却されます.
     */
    public Object get(int no);

    /**
     * boolean情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Boolean 情報が返却されます.
     */
    public Boolean getBoolean(int no);

    /**
     * int情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Integer 情報が返却されます.
     */
    public Integer getInt(int no);

    /**
     * long情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Long 情報が返却されます.
     */
    public Long getLong(int no);

    /**
     * float情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Float 情報が返却されます.
     */
    public Float getFloat(int no);

    /**
     * double情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Double 情報が返却されます.
     */
    public Double getDouble(int no);

    /**
     * String情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return String 情報が返却されます.
     */
    public String getString(int no);

    /**
     * binary情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return byte[] 情報が返却されます.
     */
    public byte[] getBinary(int no);

    /**
     * Date情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Date 情報が返却されます.
     */
    public java.sql.Date getDate(int no);

    /**
     * Time情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Time 情報が返却されます.
     */
    public java.sql.Time getTime(int no);

    /**
     * Timestamp情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Timestamp 情報が返却されます.
     */
    public java.sql.Timestamp getTimestamp(int no);

    /**
     * 指定カラム名で情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Object 対象の情報が返却されます.
     */
    public Object get(Object key);

    /**
     * boolean情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Boolean 情報が返却されます.
     */
    public Boolean getBoolean(String key);

    /**
     * int情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Integer 情報が返却されます.
     */
    public Integer getInt(String key);

    /**
     * long情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Long 情報が返却されます.
     */
    public Long getLong(String key);

    /**
     * float情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Float 情報が返却されます.
     */
    public Float getFloat(String key);

    /**
     * double情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Double 情報が返却されます.
     */
    public Double getDouble(String key);

    /**
     * String情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return String 情報が返却されます.
     */
    public String getString(String key);

    /**
     * binary情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return byte[] 情報が返却されます.
     */
    public byte[] getBinary(String key);

    /**
     * Date情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Date 情報が返却されます.
     */
    public java.sql.Date getDate(String key);

    /**
     * Time情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Time 情報が返却されます.
     */
    public java.sql.Time getTime(String key);

    /**
     * Timestamp情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Timestamp 情報が返却されます.
     */
    public java.sql.Timestamp getTimestamp(String key);

    /**
     * メタデータを取得.
     * 
     * @return JDBCMeta メタデータが返却されます.
     */
    public DbMeta getMeta();

    /**
     * JDBCResultを取得.
     * 
     * @return JDBCResult 結果データが返却されます.
     */
    public DbResult getResult();

    /**
     * コネクションオブジェクトを取得.
     * 
     * @return JDBCConnection コネクションオブジェクトが返却されます.
     */
    public DbConnection getConnection();

    public void putAll(Map toMerge);

    public boolean containsValue(Object value);

    public Set entrySet();

    public Collection values();

    public Object put(String name, Object value);

    public boolean containsKey(Object key);

    public Object remove(Object key);

    public boolean isEmpty();

    public Set keySet();

    public int size();

    public void getAllKey(Set<Object> set);

    public void getAllValues(Set<Object> set);
}
