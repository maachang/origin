package origin.test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import origin.conf.Config;
import origin.db.InitDb;
import origin.db.core.DbFactory;
import origin.pref.Def;
import origin.pref.Env;
import origin.script.Json;
import origin.script.OriginComponentManager;
import origin.script.OriginRegisterService;
import origin.util.Utils;
import origin.util.sequence.Time16SequenceId;

/**
 * テストメイン実行.
 */
@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
public class TestMain {
    private static final String LIB_PATH = "./lib/jspec";
    private static final String TEST_PATH = "./test";
    private static final String REPORT_FOLDER = "/report";
    private static final String REPORT_LOG = "report.log";
    
    public static final void main(String[] args) throws Exception {
        
        // 環境をテスト固定.
        Env.ORIGIN_ENV = Env.OperationEnvironment.Test;
        
        System.out.println("origin test.");
        
        OriginComponentManager.loadComponent();
        TestMain testMain = new TestMain();
        OriginRegisterService.getInstance().initAll(null);
        try {
            testMain.execute(args) ;
        } finally {
            OriginRegisterService.getInstance().closeAll(null);
        }
    }
    
    /** エラー出力. **/
    protected final void error(String errMessage) {
        System.err.println(errMessage);
        System.exit(-1);
    }
    
    public void execute(String[] args) throws Exception {
        Time16SequenceId sequence = null;
        DbFactory dbFactory = null;
        
        try {
            // メインコンフィグファイルが存在するかチェック.
            Config conf = new Config();
            if (!Config.read(conf, Def.CONF_NAME)) {
                error("ConfigFile:" + Def.CONF_FILE + "の読み込みに失敗.");
                return;
            }
    
            // DBコンフィグファイルが存在するかチェック.
            Config dbConf = new Config();
            if (!Config.read(dbConf, Def.DB_CONF_NAME)) {
                error("ConfigFile:" + Def.DB_CONF_FILE + "の読み込みに失敗.");
                return;
            }
    
            // シーケンスオブジェクトを生成.
            sequence = new Time16SequenceId(conf.getInt("origin",
                    "machineId", 0));
    
            // DBファクトリを生成.
            dbFactory = InitDb.init(dbConf, sequence);
            
            // レポートフォルダの作成.
            if(!Utils.isDir(TEST_PATH+REPORT_FOLDER)) {
                Utils.mkdirs(TEST_PATH+REPORT_FOLDER);
            }
            
            Map<String,Object> result = new LinkedHashMap<String,Object>();
            result.put("error", 0);
            result.put("count", 0);
            result.put("details", new ArrayList<Map<String,Object>>());
            
            // テスト処理.
            if(args != null && args.length > 0) {
                int len = args.length;
                Map<String,Object> res = null;
                Map<String,Object> one = null;
                List<Map<String,Object>> details = (List)result.get("details");
                for(int i = 0; i < len; i ++) {
                    try {
                        res = ExecuteTest.execute(LIB_PATH, TEST_PATH, REPORT_FOLDER, dbFactory, args[i]);
                        result.put("error",Utils.convertInt(result.get("error")) + Utils.convertInt(one.get("error")));
                        result.put("count",Utils.convertInt(result.get("count")) + Utils.convertInt(one.get("all")));
                        one = new LinkedHashMap<String,Object>();
                        one.put("file",toFile(LIB_PATH + "/" + args[i]));
                        one.put("error",Utils.convertInt(one.get("error")));
                        one.put("count",Utils.convertInt(one.get("all")));
                        details.add(one);
                    } catch(Exception e) {
                        result.put("error",Utils.convertInt(result.get("error")) + 1);
                        result.put("count",Utils.convertInt(result.get("count")) + 1);
                        one = new LinkedHashMap<String,Object>();
                        one.put("file",toFile(LIB_PATH + "/" + args[i]));
                        one.put("error",1);
                        one.put("count",-1);
                        details.add(one);
                    }
                }
                // 処理結果を出力.
                output(result);
                return;
            }
            
            // テストフォルダーに格納されている.jsファイルを実行.
            allTest(result,dbFactory,TEST_PATH,new File(TEST_PATH).list());
            
            // 処理結果を出力.
            output(result);
        } finally {
            if (dbFactory != null) {
                dbFactory.destroy();
            }
        }
    }
    
    // レポートログを出力.
    private static final void output(Map<String,Object> result) throws Exception {
        Utils.setFileString(true, TEST_PATH + REPORT_FOLDER + "/" + REPORT_LOG, Json.encode(result), "UTF8");
    }
    
    // ファイル名を整頓.
    private static final String toFile(String f) {
        return Utils.changeString(f, "\\", "/");
    }
    
    // 全てのテストを行う.
    private void allTest(Map<String,Object> result,DbFactory dbFactory,String folder,String[] list)
        throws Exception {
        if(list == null || list.length == 0) {
            return;
        }
        
        String name,file ;
        int len = list.length;
        
        Map<String,Object> res = null;
        Map<String,Object> one = null;
        List<Map<String,Object>> details = (List)result.get("details");
        
        for(int i = 0; i < len; i ++) {
            file = list[i];
            name = folder + "/" + file;
            if(Utils.isDir(name)) {
                allTest(result,dbFactory,name,new File(name).list());
                continue;
            } else if(!name.toLowerCase().endsWith(".js")) {
                continue;
            }
            
            try {
                res = ExecuteTest.execute(LIB_PATH, TEST_PATH, REPORT_FOLDER, dbFactory, name);
                
                result.put("error",Utils.convertInt(result.get("error")) + Utils.convertInt(res.get("error")));
                result.put("count",Utils.convertInt(result.get("count")) + Utils.convertInt(res.get("all")));
                one = new LinkedHashMap<String,Object>();
                one.put("file",toFile(folder + "/" + file));
                one.put("error",Utils.convertInt(res.get("error")));
                one.put("count",Utils.convertInt(res.get("all")));
                details.add(one);
            } catch(Exception e) {
                result.put("error",Utils.convertInt(result.get("error")) + 1);
                result.put("count",Utils.convertInt(result.get("count")) + 1);
                one = new LinkedHashMap<String,Object>();
                one.put("file",toFile(folder + "/" + file));
                one.put("error",1);
                one.put("count",-1);
                details.add(one);
                e.printStackTrace();
            }
        }
    }
}
