#! /bin/bash

cd ../../
tar zcvf tofucash.tar.gz Tofucoin/
rm /var/www/html/eommoe/setting2018/tofucash.tar.gz
mv tofucash.tar.gz /var/www/html/eommoe/setting2018/
cd /var/www/html/eommoe/setting2018/
sha256sum tofucash.tar.gz
