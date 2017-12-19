package origin.net;

import origin.conf.Config;
import origin.pref.Def;
import origin.util.Utils;

/**
 * Ajaxでアクセス許可するヘッダ一覧.
 * Ajaxでは、送信するHTTPヘッダに対して、CROS関連の
 * セキュリティでアクセス対象のHTTPヘッダを「許可」してあげる必要がある.
 * この処理により、origin.confで定義された内容を読み取り、アクセスを許可する
 * HTTPヘッダ情報を取得する.
 */
public class PermissionAccessHeader {
    protected PermissionAccessHeader() {}
    
    /**
     * origin.confで定義されているAjax許可ヘッダをセット.
     * @return String 許可ヘッダ群が返却されます.
     */
    public static final String get() {
        try {
            Config conf = Config.read(Def.CONF_PATH + Def.CONF_FILE);
            int len = conf.size("http","crosHeader");
            if(len > 0) {
                String n;
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < len; i ++) {
                    n = conf.get("http","crosHeader",i);
                    if(Utils.useString(n)) {
                        n = n.toLowerCase();
                        if(buf.length() != 0) {
                            buf.append(",");
                        }
                        buf.append(n);
                    }
                }
                return buf.toString();
            }
        } catch(Exception e) {
        }
        return "";
    }
    
}
