#!/bin/sh

function checkRVMCommand {
  echo "##teamcity[blockOpened name='Checking RVM executable']"
  source $HOME/.bash_profile # TODO: may be not needed, but without this line some bugs occured

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
}
