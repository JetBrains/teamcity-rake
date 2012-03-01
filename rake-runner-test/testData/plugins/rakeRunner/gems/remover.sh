#!/bin/bash

source checkRVMCommand.sh

function removeGemsetsForRubySdk {
local sdk=$1
echo "##teamcity[blockOpened name='Deleting Gemsets for |'$sdk|'']"
for i in `ls -d */ | grep -v vendor`; do
    pushd $i > /dev/null
    echo "##teamcity[blockOpened name='Removing gemset |'$sdk@$rrprefix$i|'']"
    rvm use "$sdk"
    rvm --force gemset delete "$rrprefix$i"
    echo "##teamcity[blockClosed name='Removing gemset |'$sdk@$rrprefix$i|'']"
    popd > /dev/null
done
echo "##teamcity[blockClosed name='Deleting Gemsets for |'$sdk|'']"
}


if [ $# -eq 0 ]; then
    warn "You must pass at least one ruby version as script parameter"
    echo "##teamcity[message text='No ruby versions to proceed' errorDetails='You must pass at least one ruby version as script parameter' status='ERROR']"
    exit 1
fi

checkRVMCommand

if [[ -z "$rrprefix" ]]; then
    rrprefix=""
fi
echo "##teamcity[message text='Using \$rrprefix = |'$rrprefix|'' status='NORMAL']"

for sdk in "$@"
do
    removeGemsetsForRubySdk $sdk
done
