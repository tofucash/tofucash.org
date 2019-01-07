tofucash.org
=========

ブロックチェーンを理解するためにはブロックチェーンを作るのが最適だ。
そう思って作りました。
ブロックチェーンノードを二階層に分け、さらにブラウザでマイニングができます。

## 実験
バックエンド層ノードを3台、フロントエンド層ノードを6台用意、またマイニング出来るスロットゲームWebサイトホスティングサーバを1台用意し、
実験を行った結果動作を確認しました。
実験では、送金、マイニング、残高取得、送金等を繰り返し行うことができました。

また、一度に数百のトランザクションを送信するサーバを追加で用意し、実験を行いました。
ネットワークの処理が追いつかずエラーになってしまうため、正常に処理できませんでした。

## 現在

P2Pネットワークの全てのノードと、マイニングできるスロットゲームWebサイトは現在停止しています。

ブロックチェーンプロジェクトは既に多く存在するため、
新しいブロックチェーンを作成するよりも、既存の有用なものを利用したほうが世の中のためになると考え、
このプロジェクトは終了しました。

----

## 仕様

最近はやりのブロックチェーンを使った、仮想通貨のプログラムです。誰でもアクセスできるPublic型と、速いPrivate型のハイブリッド型です。
Blockchain application. Hybrid type of public and private blockchain. 

## What's difference this and Bitcoin or other cryptocurrency?
Public型とPrivate型の両方の利点を活かしている点が、Bitcoinや他の仮想通貨と異なります。
P2PネットワークをBackendとFrontendに分けることで、ブロックの検証と保存をPrivate型のように行い、マイニングをPublic型のように行うことができます。

この新タイプをSeparated型と呼んでいます。

## Demo
デモは現在非公開です。


## Requirement
Javaがあれば実行できます。Java8で開発・テストを行っています。


## Install
$ cd src/

$ make


## Run
Backendサーバを実行する場合

$ sh backendRun.sh


Frontendサーバを実行する場合

$ sh frontendRun.sh


## Licence
READ [LICENSE](https://github.com/eom-moe/Tofucoin/blob/master/LICENSE)

## Author
eom-moe
