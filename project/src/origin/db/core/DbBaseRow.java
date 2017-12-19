package origin.db.core;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import origin.script.Json;
import origin.util.Utils;

/**
 * 結果情報を示すMapオブジェクト.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class DbBaseRow implements DbRow {

    /** 結果オブジェクト. **/
    private DbResult result = null;

    /** Setオブジェクトキャッシュ. **/
    private Set<String> keySet = null;

    /**
     * コンストラクタ.
     */
    public DbBaseRow() {

    }

    /**
     * コンストラクタ.
     * 
     * @return result 結果オブジェクトを設定します.
     */
    public DbBaseRow(DbResult result) {
        create(result);
    }

    /**
     * コンストラクタ.
     * 
     * @return result 結果オブジェクトを設定します.
     */
    public void create(DbResult result) {
        this.result = result;
        this.keySet = null;
    }

    /**
     * 情報クリア.
     */
    public void clear() {
        result = null;
        keySet = null;
    }

    /**
     * 指定項番で情報取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Object 対象の情報が返却されます.
     */
    public Object get(int no) {
        if (no < 0 || no >= result.getMeta().size()) {
            return null;
        }
        try {
            return DbUtils.getResultColumn(result.getResultSet(), result
                    .getMeta().getType(no), no + 1);
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * boolean情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Boolean 情報が返却されます.
     */
    public Boolean getBoolean(int no) {
        return Utils.convertBool(get(no));
    }

    /**
     * int情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Integer 情報が返却されます.
     */
    public Integer getInt(int no) {
        return Utils.convertInt(get(no));
    }

    /**
     * long情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Long 情報が返却されます.
     */
    public Long getLong(int no) {
        return Utils.convertLong(get(no));
    }

    /**
     * float情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Float 情報が返却されます.
     */
    public Float getFloat(int no) {
        Double d = Utils.convertDouble(get(no));
        if (d == null) {
            return null;
        }
        return d.floatValue();
    }

    /**
     * double情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Double 情報が返却されます.
     */
    public Double getDouble(int no) {
        return Utils.convertDouble(get(no));
    }

    /**
     * String情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return String 情報が返却されます.
     */
    public String getString(int no) {
        return Utils.convertString(get(no));
    }

    /**
     * binary情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return byte[] 情報が返却されます.
     */
    public byte[] getBinary(int no) {
        return (byte[]) get(no);
    }

    /**
     * Date情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Date 情報が返却されます.
     */
    public java.sql.Date getDate(int no) {
        return Utils.convertSqlDate(get(no));
    }

    /**
     * Time情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Time 情報が返却されます.
     */
    public java.sql.Time getTime(int no) {
        return Utils.convertSqlTime(get(no));
    }

    /**
     * Timestamp情報を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return Timestamp 情報が返却されます.
     */
    public java.sql.Timestamp getTimestamp(int no) {
        return Utils.convertSqlTimestamp(get(no));
    }

    /**
     * 指定カラム名で情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Object 対象の情報が返却されます.
     */
    public Object get(Object key) {
        int no = result.getMeta().getNumber(
                DbUtils.convertJavaNameByDBName((String) key));
        if (no == -1) {
            return null;
        }
        try {
            return DbUtils.getResultColumn(result.getResultSet(), result
                    .getMeta().getType(no), no + 1);
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    /**
     * boolean情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Boolean 情報が返却されます.
     */
    public Boolean getBoolean(String key) {
        return Utils.convertBool(get(key));
    }

    /**
     * int情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Integer 情報が返却されます.
     */
    public Integer getInt(String key) {
        return Utils.convertInt(get(key));
    }

    /**
     * long情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Long 情報が返却されます.
     */
    public Long getLong(String key) {
        return Utils.convertLong(get(key));
    }

    /**
     * float情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Float 情報が返却されます.
     */
    public Float getFloat(String key) {
        Double d = Utils.convertDouble(get(key));
        if (d == null) {
            return null;
        }
        return d.floatValue();
    }

    /**
     * double情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Double 情報が返却されます.
     */
    public Double getDouble(String key) {
        return Utils.convertDouble(get(key));
    }

    /**
     * String情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return String 情報が返却されます.
     */
    public String getString(String key) {
        return Utils.convertString(get(key));
    }

    /**
     * binary情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return byte[] 情報が返却されます.
     */
    public byte[] getBinary(String key) {
        return (byte[]) get(key);
    }

    /**
     * Date情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Date 情報が返却されます.
     */
    public java.sql.Date getDate(String key) {
        return Utils.convertSqlDate(get(key));
    }

    /**
     * Time情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Time 情報が返却されます.
     */
    public java.sql.Time getTime(String key) {
        return Utils.convertSqlTime(get(key));
    }

    /**
     * Timestamp情報を取得.
     * 
     * @param key
     *            対象のカラム名を設定します.
     * @return Timestamp 情報が返却されます.
     */
    public java.sql.Timestamp getTimestamp(String key) {
        return Utils.convertSqlTimestamp(get(key));
    }

    /**
     * メタデータを取得.
     * 
     * @return JDBCMeta メタデータが返却されます.
     */
    public DbMeta getMeta() {
        return result.getMeta();
    }

    /**
     * JDBCResultを取得.
     * 
     * @return JDBCResult 結果データが返却されます.
     */
    public DbResult getResult() {
        return result;
    }

    /**
     * コネクションオブジェクトを取得.
     * 
     * @return JDBCConnection コネクションオブジェクトが返却されます.
     */
    public DbConnection getConnection() {
        return result.getConnection();
    }

    public void putAll(Map toMerge) {
    }

    public boolean containsValue(Object value) {
        ResultSet r = result.getResultSet();
        DbMeta meta = result.getMeta();
        int len = meta.size();
        try {
            if (value == null) {
                for (int i = 0; i < len; i++) {
                    if (DbUtils.getResultColumn(r, meta.getType(i), i + 1) == null) {
                        return true;
                    }
                }
            } else {
                for (int i = 0; i < len; i++) {
                    if (value.equals(DbUtils.getResultColumn(r,
                            meta.getType(i), i + 1))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            throw new DbException(e);
        }
        return false;
    }

    public Set entrySet() {
        return null;
    }

    public Collection values() {
        ResultSet r = result.getResultSet();
        DbMeta meta = result.getMeta();
        int len = meta.size();
        List<Object> ret = new ArrayList<Object>(len);
        try {
            for (int i = 0; i < len; i++) {
                ret.add(DbUtils.getResultColumn(r, meta.getType(i), i + 1));
            }
        } catch (Exception e) {
            throw new DbException(e);
        }
        return ret;
    }

    public Object put(String name, Object value) {
        return null;
    }

    public boolean containsKey(Object key) {
        return result.getMeta().getNumber(
                DbUtils.convertJavaNameByDBName((String) key)) != -1;
    }

    public Object remove(Object key) {
        return null;
    }

    public boolean isEmpty() {
        return result.getMeta().size() == 0;
    }

    public Set keySet() {
        if (keySet == null) {
            keySet = new HashSet<String>();
            DbMeta meta = result.getMeta();
            int len = meta.size();
            for (int i = 0; i < len; i++) {
                keySet.add(meta.getName(i));
            }
        }
        return keySet;
    }

    public int size() {
        return result.getMeta().size();
    }

    public void getAllKey(Set<Object> set) {
        DbMeta meta = result.getMeta();
        int len = meta.size();
        for (int i = 0; i < len; i++) {
            set.add(meta.getName(i));
        }
    }

    public void getAllValues(Set<Object> set) {
        ResultSet r = result.getResultSet();
        DbMeta meta = result.getMeta();
        int len = meta.size();
        try {
            for (int i = 0; i < len; i++) {
                set.add(DbUtils.getResultColumn(r, meta.getType(i), i + 1));
            }
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        ResultSet r = result.getResultSet();
        DbMeta meta = result.getMeta();
        int len = meta.size();
        try {
            // json変換.
            buf.append("{");
            for (int i = 0; i < len; i++) {
                if (i != 0) {
                    buf.append(",");
                }
                buf.append("\"").append(meta.getName(i)).append("\":");
                buf.append(Json.encode(DbUtils.getResultColumn(r,
                        meta.getType(i), i + 1)));
            }
            buf.append("}");
            return buf.toString();
        } catch (Exception e) {
            throw new DbException(e);
        }
    }
}
