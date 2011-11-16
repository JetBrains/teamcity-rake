#!/bin/bash

echo "##teamcity[blockOpened name='Checking RVM executable']"
source $HOME/.bash_profile # TODO: may be not needed, but withot this line some bugs occured

if [[ -z `rvm` ]]; then
    # Try to use default .bash_profile
    if [[ -s "$HOME/.bash_profile" ]]; then
        source $HOME/.bash_profile
    fi
fi
if [[ -z `rvm` ]]; then
    # Create & Use manual default .bash_profile
    echo "[[ -s \"$HOME/.rvm/scripts/rvm\" ]] && . \"$HOME/.rvm/scripts/rvm\"" > .bash_profile
    source .bash_profile
    rm .bash_profile
fi
if [[ -z `rvm` ]]; then
    # Cannot locate rvm! must fail build
    echo "##teamcity[message text='Cannot locate rvm!' status='FAILURE']"
    exit 1
fi
echo "##teamcity[blockClosed name='Checking RVM executable']"


if [[ -z "$RR_RUBY_VERSION" ]]; then
    echo "You must specify ruby version as RR_RUBY_VERSION variable"
    echo "##teamcity[message text='RR_RUBY_VERSION are empty' errorDetails='You must specify ruby version as RR_RUBY_VERSION variable' status='ERROR']"
    exit 1
fi

# Check for minimal environment
rvm use $RR_RUBY_VERSION || (echo "##teamcity[message text='No such rubies version |'$RR_RUBY_VERSION|'' status='FAILURE']" && exit 1)
rvm use $RR_RUBY_VERSION@global
gem install bundler

if [[ -z "$rrprefix" ]]; then
    rrprefix="TC-"
fi
echo "##teamcity[message text='Using \$rrprefix = |'$rrprefix|'' status='NORMAL']"

echo "##teamcity[blockOpened name='Creating Gemsets']"
for i in `ls -d */ | grep -v vendor`; do
    pushd $i
    echo "##teamcity[blockOpened name='Processing $i']"


    echo "##teamcity[blockOpened name='Copying cache']"
    mkdir -p vendor/cache
    cp -rv ../vendor/cache/* vendor/cache/
    echo "##teamcity[blockClosed name='Copying cache']"


    echo "##teamcity[blockOpened name='Creating gemset |'$RR_RUBY_VERSION@$rrprefix$i|'']"
    rvm use "$RR_RUBY_VERSION@$rrprefix$i" --create
    echo "##teamcity[blockClosed name='Creating gemset |'$RR_RUBY_VERSION@$rrprefix$i|'']"

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
echo "##teamcity[blockClosed name='Creating Gemsets']"

