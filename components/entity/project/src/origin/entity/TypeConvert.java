package origin.entity;

import java.util.List;

import origin.script.HttpException;
import origin.script.Json;
import origin.util.Utils;

/**
 * タイプ変換.
 */
class TypeConvert {
    
    // タイプコード.
    protected static final int STRING = 0;
    protected static final int BOOL = 1;
    protected static final int NUMBER = 10;
    protected static final int INTEGER = 11;
    protected static final int LONG = 12;
    protected static final int FLOAT = 13;
    protected static final int DATE = 20;
    protected static final int JSON = 30;
    protected static final int ARRAY = 31;
    
    protected TypeConvert(){}
    
    // パラメータ変換.
    public static final Object convert(String column, String type, Object value) {
        int code = typeByCode(type);
        return convert(column,code,type,value);
    }
    
    // 指定タイプに対して、タイプコード変換.
    public static final int typeByCode(String type) {
        if ("string".equals(type)) {
            return STRING;
        } else if ("number".equals(type)) {
            return NUMBER;
        } else if ("int".equals(type) || "integer".equals(type)) {
            return INTEGER;
        } else if ("long".equals(type)) {
            return LONG;
        } else if ("float".equals(type) || "double".equals(type)) {
            return FLOAT;
        } else if ("date".equals(type)) {
            return DATE;
        } else if ("bool".equals(type) || "boolean".equals(type)) {
            return BOOL;
        } else if ("json".equals(type)) {
            return JSON;
        } else if ("array".equals(type) || "list".equals(type)) {
            return ARRAY;
        } else {
            return STRING;
        }
    }
    
    // パラメータ変換.
    public static final Object convert(String column, int typeCode, String type, Object value) {
        try {
            if (value == null) {
                value = null;
            }
            switch(typeCode) {
            case STRING :
                try {
                    value = Utils.convertString(value);
                } catch(Exception e) {
                    value = null;
                }
                break;
            case NUMBER :
                if (Utils.isNumeric(value)) {
                    if (Utils.isFloat(value)) {
                        value = Utils.convertDouble(value);
                    } else {
                        Integer v1 = Utils.convertInt(value);
                        Long v2 = Utils.convertLong(value);
                        if ((long) v1 == v2) {
                            value = v1;
                        } else {
                            value = v2;
                        }
                    }
                } else {
                    //HttpException.error(500, "対象カラム " + column + " の数値変換に失敗しました:" + value);
                    value = null;
                }
                break;
            case INTEGER :
                if (Utils.isNumeric(value)) {
                    value = Utils.convertInt(value);
                } else {
                    //HttpException.error(500, "対象カラム " + column + " の数値変換に失敗しました:" + value);
                    value = null;
                }
                break;
            case LONG :
                if (Utils.isNumeric(value)) {
                    value = Utils.convertLong(value);
                } else {
                    //HttpException.error(500, "対象カラム " + column + " の数値変換に失敗しました:" + value);
                    value = null;
                }
                break;
            case FLOAT:
                if (Utils.isNumeric(value)) {
                    value = Utils.convertDouble(value);
                } else {
                    //HttpException.error(500, "対象カラム " + column + " の数値変換に失敗しました:" + value);
                    value = null;
                }
                break;
            case DATE :
                try {
                    value = Utils.convertDate(value);
                } catch(Exception e) {
                    value = null;
                }
                break;
            case BOOL :
                try {
                    value = Utils.convertBool(value);
                } catch(Exception e) {
                    value = null;
                }
                break;
            case JSON :
                try {
                    value = Json.decode(Utils.convertString(value));
                } catch(Exception e) {
                    value = null;
                }
                break;
            case ARRAY:
                if (value instanceof List) {
                    return value;
                }
                //HttpException.error(500, "対象カラム " + column + " は配列ではありません:" + value);
                value = null;
                break;
            default:
                try {
                    value = Utils.convertString(value);
                } catch(Exception e) {
                    value = null;
                }
                break;
            }
        } catch (HttpException he) {
            throw he;
        } catch (Exception e) {
            HttpException.error(500, "対象カラム " + column + " に対して、変換条件 " + type + " で失敗しました:"
                    + value, e);
        }
        return value;
    }
}
