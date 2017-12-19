# Origin WebApi Server By javascript.

# origin起動方法

１）OSにあったJava8(JDK)をダウンロード.

　http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

２）JDKインストール

３）JDKインストール先のパスを環境変数に登録.

　>[vi /etc/profile] or [vi ~/.bash_profile]

　>一番下に、以下を追加

　　export JAVA_HOME=JDKインストール先


４）origin/binの環境をコピー + シェル権限付与.

　>/var/bin/origin

  >chmod 755 /var/bin/origin/sh/*

  >chmod 755 /var/bin/origin/sh/core/*


５）環境変数「ORIGIN_HOME」を設定

　>[vi /etc/profile] or [vi ~/.bash_profile]

　>一番下に、以下を追加

　　export ORIGIN_HOME=/var/bin/origin

　　export PATH=${PATH}:ORIGIN_HOME/sh


６）新しいoriginプロジェクトを作成.

　※newProjectフォルダは任意のフォルダ名を設定.

　>mkdir /var/project/newProject

　>cd newProject

　>oproj sqlite


　※sqliteの組み込みDBを新規プロジェクトで導入.


７）サーバ起動.

　>origin


８）サーバ停止.

　>odown




サーバを起動した場合、ポートがデフォルト値のままならば、以下のURLでJSON結果が返却される.


http://localhost:3333/


