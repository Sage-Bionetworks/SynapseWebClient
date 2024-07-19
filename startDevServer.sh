#!/bin/bash
TOMCAT_IMAGE="tomcat:9.0"
CONTAINER_NAME="swc-dev"
EXPLODED_WAR_DIR="$(pwd)/target/portal-develop-SNAPSHOT/"
GWT_DEV_ARGS="-Dgwt.module=org.sagebionetworks.web.PortalDebug -Dgwt.codeServer.launcherDir=$EXPLODED_WAR_DIR -Dgwt.style=PRETTY"

trap 'echo \"Stopping container "$CONTAINER_NAME"\"; docker stop "$CONTAINER_NAME"; exit' EXIT

# Precompile the app. This is important; if the exploded war directory does not exist, the tomcat container would create
# it, and it would be owned by the root user. The GWT code server would not be able to write to it!
mvn compile $GWT_DEV_ARGS

docker pull $TOMCAT_IMAGE
docker run --name $CONTAINER_NAME -d --rm -p 8888:8080 -v "$EXPLODED_WAR_DIR:/usr/local/tomcat/webapps/ROOT/:ro" -v "/$HOME/.m2/settings.xml:/root/.m2/settings.xml:ro" $TOMCAT_IMAGE

echo "Deployed image $TOMCAT_IMAGE as container $CONTAINER_NAME. Waiting for Tomcat to start..."
until [ $(curl -sS --connect-timeout 3 -w "%{http_code}" "http://127.0.0.1:8888/" -o /dev/null 2>/dev/null) == "200" ]; do
  printf "."
  sleep 1
done;

mvn gwt:run-codeserver -Dgwt.precompile=false $GWT_DEV_ARGS
