@rem *------------------------------------------------------------------------*
@rem * origin sql console command.
@rem *------------------------------------------------------------------------*
@set ARGS=%1 %2 %3 %4 %5 %6 %7 %8 %9
@if "%OS%" == "Windows_NT" setlocal
@echo off
@cls

@rem *------------------------------------------------------------------------*
@rem * シェル起動時のフォルダに移動.
@rem *------------------------------------------------------------------------*
@set SCRIPT=%~0
@for /f "delims=\ tokens=*" %%z in ("%SCRIPT%") do (
@set SCRIPT_CURRENT_DIR=%%~dpz )
@cd %SCRIPT_CURRENT_DIR%

@rem *------------------------------------------------------------------------*
@rem * JDKインストール先.
@rem * ※ 設定しない場合は何も指定しないでください.
@rem *------------------------------------------------------------------------*
@set SET_JAVA_HOME=

@rem *------------------------------------------------------------------------*
@rem * プロジェクトディレクトリ設定.
@rem *------------------------------------------------------------------------*
@set PROJ_DIR=.\

@rem *------------------------------------------------------------------------*
@rem * Javaオプション.
@rem *------------------------------------------------------------------------*
@set OPT=

@rem *------------------------------------------------------------------------*
@rem * 起動プログラムセット.
@rem *------------------------------------------------------------------------*
@set EXEC_PACKAGE=origin.db.DbConsole

@rem *------------------------------------------------------------------------*
@rem * 開始メモリ領域.
@rem * 単位はMByte
@rem *------------------------------------------------------------------------*
@set STM=128

@rem *------------------------------------------------------------------------*
@rem * 最大メモリ領域.
@rem * 単位はMByte
@rem *------------------------------------------------------------------------*
@set EXM=128



@rem ##########################################################################
@rem # ※これ以下は設定しないでください.
@rem ##########################################################################
@rem * baseFolder. *
@set BASE_HOME=%ORIGIN_HOME%

@rem *------------------------------------------------------------------------*
@rem * 設定条件反映.
@rem *------------------------------------------------------------------------*
@if not "%SET_JAVA_HOME%" == "" @set JAVA_HOME=%SET_JAVA_HOME%

@rem *------------------------------------------------------------------------*
@rem * 起動バッチディレクトリ.
@rem *------------------------------------------------------------------------*
@set BATCH_DIR=%BASE_HOME%\sh

@rem *------------------------------------------------------------------------*
@rem * 定義データ格納ディレクトリ.
@rem *------------------------------------------------------------------------*
@set CONF_DIR=%PROJ_DIR%\conf

@rem *------------------------------------------------------------------------*
@rem * JAR格納ディレクトリ.
@rem *------------------------------------------------------------------------*
@set LIB_DIR=%BASE_HOME%\lib

@rem *------------------------------------------------------------------------*
@rem * エラー判別.
@rem *------------------------------------------------------------------------*
@if "%JAVA_HOME%" == "" goto errJAVA_HOME
@if "%PROJ_DIR%" == "" goto errPROJ_DIR

@rem *------------------------------------------------------------------------*
@rem * execution java.
@rem *------------------------------------------------------------------------*

@call %BATCH_DIR%\core\parselib
@set LIB_DIR=%PROJ_DIR%\jar
@call %BATCH_DIR%\core\parselib
@set CLASSPATH=.;%INST_LIB%;%CONF_DIR%

@%JAVA_HOME%\bin\java -Xms%STM%m -Xmx%EXM%m %OPT% %EXEC_PACKAGE% %ARGS%
goto end

@rem *------------------------------------------------------------------------*
@rem * エラー処理.
@rem *------------------------------------------------------------------------*

:errJAVA_HOME
@echo 環境変数 JAVA_HOME が設定されていません.
goto end

:errPROJ_DIR
@echo プロジェクトディレクトリが不正です.
goto end

:end
@if "%OS%" == "Windows_NT" endlocal
