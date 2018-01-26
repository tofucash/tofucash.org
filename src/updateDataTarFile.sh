#! /bin/bash

cd ../
tar zcvf data.tar.gz data
rm /var/www/html/eommoe/setting2018/data.tar.gz
mv data.tar.gz /var/www/html/eommoe/setting2018/
