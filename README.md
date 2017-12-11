Tofucoin
=========

最近はやりのブロックチェーンを使った、仮想通貨のプログラムです。よくあるPublic型とPrivate型のハイブリッド型です。
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
