#TODO: toimiiko tama oikeastii....
service jetty stop
DIR=`pwd`
WEBAPP="/usr/share/jetty/webapps/root"
cp target/mp3-web-0.1.war $WEBAPP/mp3.war
cd $WEBAPP
jar xf mp3.war
cd $DIR
service jetty start
