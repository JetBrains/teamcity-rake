#!/bin/bash

echo "##teamcity[blockOpened name='Checking RVM executable']"
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
    warn "You must specify ruby version as RR_RUBY_VERSION variable"
    echo "##teamcity[message text='RR_RUBY_VERSION are empty' errorDetails='You must specify ruby version as RR_RUBY_VERSION variable' status='ERROR']"
    exit 1
fi

if [[ -z "$rrprefix" ]]; then
    rrprefix=""
fi
echo "##teamcity[message text='Using \$rrprefix = |'$rrprefix|'' status='NORMAL']"

echo "##teamcity[blockOpened name='Deleting Gemsets']"
for i in `ls -d */ | grep -v vendor`; do
    pushd $i
    echo "##teamcity[blockOpened name='Removing gemset |'$RR_RUBY_VERSION@$rrprefix$i|'']"
    rvm use "$RUBY_VERSION"
    rvm --force gemset delete "$rrprefix$i"
    echo "##teamcity[blockClosed name='Removing gemset |'$RR_RUBY_VERSION@$rrprefix$i|'']"
    popd
done
echo "##teamcity[blockClosed name='Deleting Gemsets']"
