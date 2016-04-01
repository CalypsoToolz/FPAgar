#!/bin/sh

while :;
do
	java -server -Dfile.encoding=UTF-8 -Dlog4j.configuration=file:./config/log4j.xml -Xmx128m -jar server.jar
	[ $? -ne 2 ] && break # if server shutdowned with code != 2, when cancel this script
	sleep 5; # check for start after restart
done
