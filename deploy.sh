#!/bin/sh
git clean -fxd -- resources/public/js
lein cljsbuild once min
lein uberjar
scp target/uberjar/clweb-0.1.0-SNAPSHOT-standalone.jar lex@axw.se:cloud/clweb/
ssh lex@axw.se "cd cloud; ./up"
