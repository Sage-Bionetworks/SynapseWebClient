After creating/updating a markdown-it plugin, be sure to execute the following command from this directory.  Update the version, and Portal.html (so that it does not load the cached version):
cat *.js | java -jar ../yuicompressor-2.4.8.jar --type js -o ../markdown-it-plugins-16.min.js

