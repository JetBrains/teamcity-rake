#!/bin/bash

# [[ -s "$HOME/.rvm/scripts/rvm" ]] && . "$HOME/.rvm/scripts/rvm" # Load RVM function
# must be in $HOME/.bash_profile
source $HOME/.bash_profile


if [[ -z "$RR_RUBY_VERSION" ]]; then
    echo "You must specify ruby version as RR_RUBY_VERSION variable"
    exit 1
fi

if [[ -z "$rrprefix" ]]; then
    rrprefix=""
fi

for i in `ls -d */`; do
    pushd $i

    mkdir -p vendor/cache
    cp -rv ../vendor/cache/* vendor/cache/

    rvm use "$RR_RUBY_VERSION@$rrprefix$i" --create
    bundle install

    bundle pack
    mv -v vendor/cache/* ../vendor/cache/
    rm -rf vendor
    popd
done

