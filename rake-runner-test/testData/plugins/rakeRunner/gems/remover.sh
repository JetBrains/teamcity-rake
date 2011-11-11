#!/bin/bash

# [[ -s "$HOME/.rvm/scripts/rvm" ]] && . "$HOME/.rvm/scripts/rvm" # Load RVM function
# must be in $HOME/.bash_profile
source $HOME/.bash_profile


if [[ -z "$RUBY_VERSION" ]]; then
    echo "You must specify ruby version"
    exit 1
fi

if [[ -z "$rrprefix" ]]; then
    rrprefix=""
fi

for i in `ls -d */`; do
    pushd $i
    rvm use "$RUBY_VERSION"
    rvm --force gemset delete "$rrprefix$i"
    popd
done

