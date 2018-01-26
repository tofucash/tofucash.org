#! /bin/bash
wget https://eom.moe/setting2018/ipAddress --no-check-certificate
DATA=`cat ipAddress`
echo "remove file ${DATA}"
rm "../data/backendServer/${DATA}"
