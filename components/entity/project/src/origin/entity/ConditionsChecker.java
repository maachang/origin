package origin.entity;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import origin.script.HttpException;
import origin.util.Alphabet;
import origin.util.Utils;

/**
 * 条件チェック.
 */
class ConditionsChecker {
    protected ConditionsChecker(){}
    
    // パラメータ変換.
    public static final Object check(String[] renameOut,String column, String type, Object value,
            String conditions) {
        if (!Utils.useString(conditions)) {
            return value;
        }

        // 情報をカット.
        List<String> list = Utils.cutString(true, false, conditions," 　\t_");
        if (list.size() == 0) {
            return value;
        }

        // 区切り条件毎に、チェック.
        int len = list.size();
        boolean notFlag = false;
        Object[] o = new Object[1];
        for (int pos = 0; pos < len; pos++) {
            if (not(list.get(pos))) {
                notFlag = true;
                continue;
            }

            // バリデーションなし.
            if (Alphabet.eq("none",list.get(pos))) {
                notFlag = false;
                continue;
            }

            if (isNull(notFlag, list.get(pos), column, conditions, value)) {
                notFlag = false;
                continue;
            }

            if (date(notFlag, list.get(pos), column, conditions, value)) {
                notFlag = false;
                continue;
            }

            if (time(notFlag, list.get(pos), column, conditions, value)) {
                notFlag = false;
                continue;
            }

            if (zip(notFlag, list.get(pos), column, conditions, value)) {
                notFlag = false;
                continue;
            }

            if (tel(notFlag, list.get(pos), column, conditions, value)) {
                notFlag = false;
                continue;
            }

            if (tel(notFlag, list.get(pos), column, conditions, value)) {
                notFlag = false;
                continue;
            }

            if (ipv4(notFlag, list.get(pos), column, conditions, value)) {
                notFlag = false;
                continue;
            }

            if (url(notFlag, list.get(pos), column, conditions, value)) {
                notFlag = false;
                continue;
            }

            if (email(notFlag, list.get(pos), column, conditions, value)) {
                notFlag = false;
                continue;
            }

            if (min(notFlag, list, pos, column, conditions, value)) {
                pos += 1;
                notFlag = false;
                continue;
            }

            if (max(notFlag, list, pos, column, conditions, value)) {
                pos += 1;
                notFlag = false;
                continue;
            }

            if (range(notFlag, list, pos, column, conditions, value)) {
                pos += 2;
                notFlag = false;
                continue;
            }

            o[0] = value;
            if(defaultValue(o,notFlag,list,pos,column,type,conditions)) {
                value = o[0];
                o[0] = null;
                pos += 1;
                notFlag = false;
                continue;
            }
            
            o[0] = null;
            if(renameValue(o,notFlag,list,pos,column,type,conditions)) {
                if(o[0] != null) {
                    renameOut[0] = (String)o[0];
                }
                o[0] = null;
                pos += 1;
                notFlag = false;
                continue;
            }

            HttpException.error(500, column + " のvalidate条件が不明です:" + conditions);
        }

        if (notFlag) {
            HttpException.error(500, column + " のvalidate条件が不明です:" + conditions);
        }
        
        return value;
    }

    // not.
    private static final boolean not(String a) {
        return Alphabet.eq("not",a);
    }

    // null.
    private static final boolean isNull(boolean notFlag, String a,
            String column, String conditions, Object value) {
        if (Alphabet.eq("null",a)) {
            if (notFlag && value == null) {
                HttpException.error(400, column + " の値がnullです:" + conditions);
            }
            return true;
        }
        return false;
    }

    // date.
    private static final boolean date(boolean notFlag, String a, String column,
            String conditions, Object value) {
        return exp(DATE_EXP, "date", notFlag, a, column, conditions, value,
            "は、日付のフォーマットではありません");
    }

    // time.
    private static final boolean time(boolean notFlag, String a, String column,
            String conditions, Object value) {
        return exp(TIME_EXP, "time", notFlag, a, column, conditions, value,
            "は、時間のフォーマットではありません");
    }

    // zip.
    private static final boolean zip(boolean notFlag, String a, String column,
            String conditions, Object value) {
        return exp(ZIP_EXP, "zip", notFlag, a, column, conditions, value,
            "は、郵便番号のフォーマットではありません");
    }

    // tel.
    private static final boolean tel(boolean notFlag, String a, String column,
            String conditions, Object value) {
        return exp(TEL_EXP, "tel", notFlag, a, column, conditions, value,
            "は、電話番号のフォーマットではありません");
    }

    // ipv4.
    private static final boolean ipv4(boolean notFlag, String a, String column,
            String conditions, Object value) {
        return exp(IPV4_EXP, "ipv4", notFlag, a, column, conditions, value,
            "は、IPアドレス(IPV4)のフォーマットではありません");
    }

    // url.
    private static final boolean url(boolean notFlag, String a, String column,
            String conditions, Object value) {
        return exp(URL_EXP, "url", notFlag, a, column, conditions, value,
            "は、URLのフォーマットではありません");
    }

    // email.
    private static final boolean email(boolean notFlag, String a,
            String column, String conditions, Object value) {
        return exp(EMAIL_EXP, "email", notFlag, a, column, conditions, value,
            "は、メールアドレスのフォーマットではありません");
    }

    // min [number].
    private static final boolean min(boolean notFlag, List<String> list,
            int pos, String column, String conditions, Object value) {
        if(Alphabet.eq("min",list.get(pos))) {
            if (value == null) {
                HttpException.error(400, column + " の値がnullです");
            }
            int len = list.size();
            if (pos + 1 >= len) {
                HttpException.error(500, column + " の条件定義が数値ではありません:" + conditions);
            }
            String b = list.get(pos + 1);
            boolean eq;
            if (Utils.isNumeric(b)) {
                eq = Utils.convertString(value).length() < Utils.convertInt(b);
                if (eq != notFlag) {
                    HttpException.error(400, column + " の長さが条件外です:" + conditions);
                }
            } else {
                HttpException.error(500, column + " の条件定義が数値ではありません:" + conditions);
            }
            return true;
        }
        return false;
    }

    // max [number].
    private static final boolean max(boolean notFlag, List<String> list,
            int pos, String column, String conditions, Object value) {
        if(Alphabet.eq("max",list.get(pos))) {
            if (value == null) {
                HttpException.error(400, column + " の値がnullです");
            }
            int len = list.size();
            if (pos + 1 >= len) {
                HttpException.error(500, column + " の条件定義が数値ではありません:" + conditions);
            }
            String b = list.get(pos + 1);
            boolean eq;
            if (Utils.isNumeric(b)) {
                eq = Utils.convertString(value).length() > Utils.convertInt(b);
                if (eq != notFlag) {
                    HttpException.error(400, column + " の長さが条件外です:" + conditions);
                }
            } else {
                HttpException.error(500, column + " の条件定義が数値ではありません:" + conditions);
            }
            return true;
        }
        return false;
    }
    
    // default [value].
    private static final boolean defaultValue(Object[] out,boolean notFlag, List<String> list,
            int pos, String column, String type, String conditions) {
        if(Alphabet.eq("default",list.get(pos))) {
            if(notFlag) {
                HttpException.error(400, column + " の not 条件は間違っています:" + conditions);
            } else if (out[0] != null) {
                return true;
            }
            int len = list.size();
            if (pos + 1 >= len) {
                HttpException.error(500, column + " の条件定義が数値ではありません:" + conditions);
            }
            String b = list.get(pos + 1);
            out[0] = TypeConvert.convert(column,type,b);
            return true;
        }
        return false;
    }

    // rename [value].
    private static final boolean renameValue(Object[] out,boolean notFlag,List<String> list,
            int pos, String column, String type, String conditions) {
        if(Alphabet.eq("rename",list.get(pos))) {
            if(notFlag) {
                HttpException.error(400, column + " の not 条件は間違っています:" + conditions);
            }
            int len = list.size();
            if (pos + 1 >= len) {
                HttpException.error(500, column + " の条件定義が数値ではありません:" + conditions);
            }
            out[0] = list.get(pos + 1);
            return true;
        }
        return false;
    }

    // range [number] [number].
    private static final boolean range(boolean notFlag, List<String> list,
            int pos, String column, String conditions, Object value) {
        if(Alphabet.eq("range",list.get(pos))) {
            if (value == null) {
                HttpException.error(400, column + " の値がnullです");
            }
            int len = list.size();
            if (pos + 2 >= len) {
                HttpException.error(500, column + " の条件定義が数値ではありません:" + conditions);
            }
            String b = list.get(pos + 1);
            String c = list.get(pos + 2);
            boolean eq;
            String n = Utils.convertString(value);
            if (Utils.isNumeric(b)) {
                eq = n.length() < Utils.convertInt(b);
                if (eq != notFlag) {
                    HttpException.error(400, column + " の長さが条件外です:" + conditions);
                }
            } else {
                HttpException.error(500, column + " の条件定義が数値ではありません:" + conditions);
            }
            if (Utils.isNumeric(c)) {
                eq = n.length() > Utils.convertInt(c);
                if (eq != notFlag) {
                    HttpException.error(400, column + " の長さが条件外です:" + conditions);
                }
            } else {
                HttpException.error(500, column + " の条件定義が数値ではありません:" + conditions);
            }
            return true;
        }
        return false;
    }

    // exp.
    private static final Pattern DATE_EXP = Pattern
            .compile("^\\d{2,4}\\/([1][0-2]|[0][1-9]|[1-9])\\/([3][0-1]|[1-2][0-9]|[0][1-9]|[1-9])$");
    private static final Pattern TIME_EXP = Pattern
            .compile("^([0-1][0-9]|[2][0-3]|[0-9])\\:([0-5][0-9]|[0-9])$");
    private static final Pattern ZIP_EXP = Pattern.compile("^\\d{3}-\\d{4}$");
    private static final Pattern TEL_EXP = Pattern
            .compile("^[0-9]+\\-[0-9]+\\-[0-9]+$");
    private static final Pattern IPV4_EXP = Pattern
            .compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    private static final Pattern URL_EXP = Pattern
            .compile("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+");
    private static final Pattern EMAIL_EXP = Pattern
            .compile("\\w{1,}[@][\\w\\-]{1,}([.]([\\w\\-]{1,})){1,3}$");

    // exp.
    private static final boolean exp(Pattern p, String m, boolean notFlag,
            String a, String column, String conditions, Object value,String message) {
        if(Alphabet.eq(m,a)) {
            if (value == null) {
                HttpException.error(400, column + " の値がnullです");
            }
            Matcher mc = p.matcher(Utils.convertString(value));
            if(notFlag) {
                if (mc.find()) {
                    HttpException.error(400, column + " " + message + "");
                }
            } else if (!mc.find()) {
                HttpException.error(400, column + " " + message + "");
            }
            return true;
        }
        return false;
    }

}
