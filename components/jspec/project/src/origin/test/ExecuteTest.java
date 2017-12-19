package origin.test;

import java.util.Map;

import origin.db.DbCreateBaseDao;
import origin.db.core.DbFactory;
import origin.script.ExecuteScript;
import origin.script.Json;
import origin.script.OriginBindings;
import origin.util.Utils;

/**
 * テスト実行.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExecuteTest {
    protected ExecuteTest() {}
    
    /**
     * テスト実行.
     * @param libPath ライブラリのパスを設定します.
     * @param testPath テスト用のパスを設定します.
     * @param reportPath レポート用のパスを設定します.
     * @param dbFactory DbFactoryを設定します.
     * @param name テストファイル名を設定します.
     * @return Map<String,Object> 処理結果が返却されます.
     * @throws Exception 例外.
     */
    public static final Map<String,Object> execute(
            String libPath,String testPath, String reportPath, DbFactory dbFactory,String name)
            throws Exception {
        String src = Utils.getFileString(name, "UTF8");
        
        String reportFile = testPath + reportPath + name.substring(testPath.length());
        int p = reportFile.lastIndexOf("/");
        if(p != -1) {
            String f = reportFile.substring(0,p);
            if(!Utils.isDir(f)) {
                Utils.mkdirs(f);
            }
        }
        
        src = new StringBuilder("(function(global){\n\"use strict\";\n")
            .append("var report = require(\"").append(libPath).append("/report.js\");\n")
            .append("report.open(\"").append(reportFile).append("\");\n")
            .append("global._output = function(n) { report.println(n); };\n")
            .append("global._output(\"").append(name).append("\");\n")
            .append("global._output(\"\");\n")
            .append("require(\"").append(libPath).append("/jspec.js\");\n")
            .append("try {\n")
            .append("\n")
            .append(src)
            .append("\n")
            .append("} finally {\n")
            .append("  report.close();\n")
            .append("}\n")
            .append("return Json.encode(global[\"_$report\"]);\n")
            .append("})(this);\n")
            .toString();
        
        OriginBindings b = new OriginBindings(dbFactory,new DbCreateBaseDao());
        String res = (String)ExecuteScript.eval(src, name, b);
        
        return (Map)Json.decode(res);
    }
    
    
}
