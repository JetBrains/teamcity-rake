#!/bin/bash

source checkRVMCommand.sh

function checkRubySdkExistAndCreateMinimalEnv {
local sdk=$1
# Check for minimal environment
rvm use $sdk || (echo "##teamcity[message text='No such rubies version |'$sdk|'' status='FAILURE']" && exit 1)
rvm use $sdk@global
gem install bundler
}

function initializeRubySdk {
local sdk=$1
echo "##teamcity[blockOpened name='Creating Gemsets for |'$sdk|'']"
for i in `ls -d */ | grep -v vendor`; do
    pushd $i
    echo "##teamcity[blockOpened name='Processing $i']"


    echo "##teamcity[blockOpened name='Copying cache']"
    mkdir -p vendor/cache
    cp -rv ../vendor/cache/* vendor/cache/
    echo "##teamcity[blockClosed name='Copying cache']"


    echo "##teamcity[blockOpened name='Creating gemset |'$sdk@$rrprefix$i|'']"
    rvm use "$RR_RUBY_VERSION@$rrprefix$i" --create
    echo "##teamcity[blockClosed name='Creating gemset |'$sdk@$rrprefix$i|'']"

    echo "##teamcity[blockOpened name='Execute Bundler']"
    if [[ -n "$bundle_repository_local" ]]; then
        echo "##teamcity[message text='Using bundle only with local repository' status='NORMAL']"
        bundle install --local
    else
        bundle install
    fi
    echo "##teamcity[blockClosed name='Execute Bundler']"

    echo "##teamcity[blockOpened name='Updating cache']"
    bundle pack
    mv -v vendor/cache/* ../vendor/cache/
    rm -rf vendor
    echo "##teamcity[blockClosed name='Updating cache']"

    echo "##teamcity[blockClosed name='Processing $i']"
    popd
done
echo "##teamcity[blockClosed name='Creating Gemsets for |'$sdk|'']"
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
    checkRubySdkExistAndCreateMinimalEnv $sdk
    initializeRubySdk $sdk
done





