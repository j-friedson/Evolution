#!/usr/bin/python

import sys,os, re, fileinput

p = re.compile('\"(body|population)\",[^\]]*[,]')
dirpath = sys.argv[1]

for f in os.listdir(dirpath):
    for line in fileinput.input(dirpath + "/" + f, inplace=True):
        m = p.search(line)
        while m:
            line = line[0:m.start()] + line[m.end()-2:len(line)]
            m = p.search(line)
        print line,
