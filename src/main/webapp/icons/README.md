## How to regenerate Synapse custom icon set
1.  Go to http://fontello.com/
2.  Import config.json from this directory.
3.  Add additional custom icons, and select for export.
4.  Select Download webfont.
5.  Unzip the downloaded file. Update the config.json, and add all css and font files into the font directory. 
6.  To use gwtbootstrap IconType:
- Check out the current gwtbootstrap3 fork from Sage-Bionetworks, branch 1-5
https://github.com/Sage-Bionetworks/gwtbootstrap3/tree/1-5
- Replace or add the new icon definitions to org.gwtbootstrap3.client.ui.constants.IconType enum definition (with the correct css class names).
- Bump the gwtbootstrap3 version in the root pom.xml and gwtbootstrap3/pom.xml
- Pull this change into that repo.
(may need to manually start a build, at build-system-synapse.sagebase.org:8081/job/gwtbootstrap3-1.5/)
- In SWC (in the root pom.xml) update to the new gwtbootstrap3 version.
- Use the new IconTypes! 