docker rm -f nabu ; mvn -P f8-build,h2 && docker run -it --name nabu -e JAVA_OPTIONS="-Xmx1280m" -p 9004:9004 -v /git/config/nabu/dev/application.properties:/app/config/application.properties:ro dr.rutebanken.org/rutebanken/nabu:0.0.1-SNAPSHOT
