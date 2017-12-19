package origin.pref;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import origin.conf.Config;

/**
 * origin環境.
 */
public class Env {

    /**
     * 動作環境情報.
     */
    public enum OperationEnvironment {
        Test(0, "test"), // テスト環境.
        Local(1, "local"), // ローカル環境.
        Staging(2, "staging"), // ステージング環境.
        Product(3, "production"); // プロダクション環境.

        int mode;
        String name;

        OperationEnvironment(int m, String n) {
            mode = m;
            name = n;
        }

        public int getMode() {
            return mode;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }
    }

    /**
     * 現在の動作環境を取得.
     */
    public static OperationEnvironment ORIGIN_ENV = OperationEnvironment.Local;
    static {

        OperationEnvironment value = null;
        OperationEnvironment[] VALUES = OperationEnvironment.values();

        // 環境変数を取得.
        String env = System.getenv("ORIGIN_ENV");
        if (env != null) {
            env = env.trim().toLowerCase();
            int len = VALUES.length;
            for (int i = 0; i < len; i++) {
                if (VALUES[i].getName().equals(env)) {
                    value = VALUES[i];
                    break;
                }
            }
        }

        // 条件が存在する場合はセット.
        if (value != null) {
            ORIGIN_ENV = value;
        }
    }

    /**
     * 環境条件を加味したセクション名を取得.
     * 
     * @param conf
     *            対象のConfオブジェクトを取得.
     * @param section
     *            対象のセクション名を設定します.
     * @return String 有効なセクション名が返却されます.
     */
    public static final String getEnvSection(Config conf, String section) {
        OperationEnvironment originEnv = Env.ORIGIN_ENV;

        // 動作環境のセクションが存在する場合は、そちらで処理.
        String ret = originEnv.getName() + "." + section;
        if (!conf.isSection(ret)) {
            ret = section;
            if (!conf.isSection(ret)) {
                return null;
            }
        }
        return ret;
    }

    /**
     * 現在の環境条件を除外した、セクション名を取得.
     * 
     * @param section
     *            対象のセクション名を設定します.
     * @return String 本来のセクション名が返却されます.
     */
    public static final String getNotEnvSection(String section) {
        OperationEnvironment originEnv = Env.ORIGIN_ENV;
        String env = originEnv.getName() + ".";
        if (section.startsWith(env)) {
            return section.substring(env.length());
        }
        return section;
    }

    /**
     * 環境条件を除外した、セクション名群の取得.
     * 
     * @param sections
     *            セクション名を設定します.
     * @param noSectionName
     *            除外するセクション名を設定します.
     * @return List<String> セクション名群が返却されます.
     */
    public static final List<String> getNotEnvSections(Config conf,
            String noSectionName) {
        return getNotEnvSections(conf, ".", noSectionName);
    }

    /**
     * 環境条件を除外した、セクション名群の取得.
     * 
     * @param sections
     *            セクション名を設定します.
     * @param appendCode
     *            環境名 + appendCode + セクション名の関係のうち、appendCodeに当たる文字情報です.
     * @param noSectionName
     *            除外するセクション名を設定します.
     * @return List<String> セクション名群が返却されます.
     */
    public static final List<String> getNotEnvSections(Config conf,
            String appendCode, String noSectionName) {
        Object[] sections = conf.getSections();
        if (sections == null || sections.length == 0) {
            return null;
        }
        // appendCodeがつくもの、dbのセクションは除外.
        Env.OperationEnvironment[] list = Env.OperationEnvironment.values();
        Set<String> set = new HashSet<String>();
        int len = sections.length;
        String section;
        int lenJ = list.length;
        for (int i = 0; i < len; i++) {
            if (noSectionName != null && sections[i].equals(noSectionName)) {
                continue;
            }
            // originEnv + "." + sectionの場合は、sectionにして、取得.
            section = (String) sections[i];
            if (section.indexOf(appendCode) != -1) {
                for (int j = 0; j < lenJ; j++) {
                    if (section.startsWith(list[j].getName() + appendCode)) {
                        section = section.substring(list[j].getName().length()
                                + appendCode.length());
                        break;
                    }
                }
            }
            if (noSectionName != null && section.equals(noSectionName)) {
                continue;
            }
            set.add(section);
        }
        List<String> ret = new ArrayList<String>();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            ret.add(it.next());
        }
        return ret;
    }

    /**
     * Env名と一致するかチェック.
     * 
     * @param name
     *            対象の名前を設定します.
     * @return String 一致している場合はnull以外が返却されます.
     */
    public static final String checkEnv(String name) {
        if (name == null || (name = name.trim()).length() == 0) {
            return null;
        }
        name = name.toLowerCase();
        Env.OperationEnvironment[] list = Env.OperationEnvironment.values();
        int len = list.length;
        for (int i = 0; i < len; i++) {
            if (name.equals(list[i].getName())) {
                return name;
            }
        }
        return null;
    }
}
