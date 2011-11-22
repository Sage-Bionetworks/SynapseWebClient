import os, sys, fileinput
'''
Walk the complete PLFM hierarchy and bring all the pom.xml files up to new minor version
'''

#Old and new minor versions
oldVersion = '0.8-SNAPSHOT'
newVersion = '0.8.4'
#Path to PLFM on your system
startPath = '/Users/dburdick/code/platform/Synapse-0.8.4/'
for root, subFolders, files in os.walk(startPath):    
    for file in files:
        if file == 'pom.xml': 
            f = os.path.join(root, file) 
            print 'updating '+f
            for line in fileinput.FileInput(f, inplace=1):
                found = False
                if line.find('<version>'+oldVersion+'</version>') >= 0 and not found:
                    line = line.replace(oldVersion,newVersion)
                    found = True
                print line.rstrip()
#        if file == 'DESCRIPTION' and root.find('rSynapseClient') >= 0:
#            f = os.path.join(root, file)
#            print 'updating '+f
#            for line in fileinput.FileInput(f, inplace=1):
#                found = False
#                if not found and line.find('Version') >= 0:
#                    line = 'Version: '+newVersion+'-0'
#                    found = True
#                print line.rstrip()
