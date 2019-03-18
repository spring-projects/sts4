#!/usr/bin/python

import os

def replace(infile, outfile, findword, replaceword):
    with open(infile, "rt") as fin:
        with open(outfile, "wt") as fout:
            for line in fin:
                fout.write(line.replace(findword, replaceword))

branch = os.popen("git rev-parse --abbrev-ref HEAD").read().strip()

if branch == 'master':
    os.system('fly -t tools set-pipeline --load-vars-from ${HOME}/git/sts4-concourse-build-credentials/.sts4-concourse-credentials.yml '+
              '      --var "branch=' + branch + '" ' +
              '      -p sts4-'+ branch +' -c pipeline.yml')
else:
    fname = "pipeline-"+branch+".yml"
    replace("pipeline.yml", "pipeline-"+branch+".yml", "snapshot", branch)
    with open(fname, 'r') as fin:
        print fin.read()
    cmd = ('fly -t tools set-pipeline --load-vars-from ${HOME}/git/sts4-concourse-build-credentials/.sts4-concourse-credentials.yml ' + 
        '--var "branch=' + branch + '" ' + '-p sts4-' +branch+' -c '+fname)
    print cmd
    os.system(cmd)
