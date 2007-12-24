============================================================
======== PROJECT,  SOURCES =================================
============================================================

1.Before opening the project in IDEA for the first time:
- recreate dist directory
- TeamCity-*.tar.gz in dist/
- run "ant extract" (see build.xml)

============================================================
======= INSTALLATION =======================================
============================================================
1. Agent Reqirements
   * Enviroment variables:
       RUBY_INTERPRETER - Path to ruby interpreter, e.g. C:\IR\InstantRails-1.7-new\ruby\bin\ruby.exe or /usr/bin/ruby
   * System properties:
       system.ruby.interpreter - Path to ruby interpreter, e.g. C:\IR\InstantRails-1.7-new\ruby\bin\ruby.exe or /usr/bin/ruby

   * Rake 0.7.3
   * install ruby gem 'builder'
        /> gem install builder
   * rails - for rails projects

   * patch ruby API :
      - rename [ruby_home]/lib/ruby/1.8/test/unit/autorunner.rb to [ruby_home]/lib/ruby/1.8/test/unit/autorunner_old.rb
      - rename rakerunner/src/ext/lib/ruby/1.8/test/unit/autorunner.rb to [ruby_home]/lib/ruby/1.8/test/unit/autorunner.rb
      - copy all necessary files to [ruby_home]/lib/ruby/1.8/test/unit/teamcity/*.*

2. RUBY_INTERPRETER or system.ruby.interpreter must be set on agent. If system property is set corresponding environment variable will be ignored.

============================================================
======= DEPLOYING ==========================================
============================================================
1. As plugins
2. Using "devel-deploy" task:
   Setup Teamcity BuildServer directories in build file properties.

   E.g.
     agentdir - c:\soft\Teamcity_6527\buildAgent
     webrootdir - c:\soft\Teamcity_6527\webapps\ROOT