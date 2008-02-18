============================================================
======= ABOUT ==============================================
============================================================
Teamcity plugin for running user Rake tasks.

Features:
 * Shows rake tasks progress on buildserver
 * Show Test::Unit tests results on buildserver
   NOTE: Rake::Teamcity.run_tests(...) instead of Rake.run_tests(...)
 * Supports correct processing of tests results for rails tasks: test, test::functionals, etc.


============================================================
======= INSTALLATION =======================================
============================================================
1. Agent Reqirements
   * Enviroment variables:
       RUBY_INTERPRETER - Path to ruby interpreter, e.g. C:\IR\InstantRails-1.7-new\ruby\bin\ruby.exe or /usr/bin/ruby
   * System properties:
       system.ruby.interpreter - Path to ruby interpreter, e.g. C:\IR\InstantRails-1.7-new\ruby\bin\ruby.exe or /usr/bin/ruby

   * Rake 0.7.3 or higher
   * install ruby gem 'builder'
        /> gem install builder
   * rails - for rails projects

   * patch Ruby Test/Unit API :
      - rename [ruby_home]/lib/ruby/1.8/test/unit/autorunner.rb to [ruby_home]/lib/ruby/1.8/test/unit/autorunner_old.rb
      - rename rakerunner/src/ext/lib/ruby/1.8/test/unit/autorunner.rb to [ruby_home]/lib/ruby/1.8/test/unit/autorunner.rb
      - copy all necessary files to [ruby_home]/lib/ruby/1.8/test/unit/ui/teamcity/*.*

2. RUBY_INTERPRETER or system.ruby.interpreter must be set on agent. If system property is set corresponding environment variable will be ignored.

============================================================
======= BUILD  =============================================
============================================================
1. Setup Teamcity libraries root dir in build file properties:
   E.g.
     tc.root.dir - dist\Teamcity
2. Run "dist" task


============================================================
======== PROJECT,  SOURCES =================================
============================================================

1.Before opening the project in IDEA for the first time:
- recreate dist directory
- TeamCity-*.tar.gz in dist/
- run "ant extract" (see build.xml)

============================================================
======= DEPLOYING ==========================================
============================================================
1. As plugins
2. Using "devel-deploy" task:
   Setup Teamcity BuildServer directories in build file properties.

   E.g.
     agentdir - c:\soft\Teamcity_6527\buildAgent
     webrootdir - c:\soft\Teamcity_6527\webapps\ROOT

=============================================================
========= LIMITATIONS =======================================
=============================================================
RakeRunner plugin uses own unit tests runner and loads it with RUBYLIB enviroment variable.
Be sure that your program doesn't clear this enviroment variable. But obviously you may append
your pathes to it.

#TODO - describe properties - environment, sytem, env. on agent, build runner params
#TODO - remind about "win32console gem"
#TODO - about "ignored specs"

#TODO --require "spec/runner/formatter/teamcity/formatter"
#TODO --format Spec::Runner::Formatter::TeamcityFormatter:matrix
#TODO - Open in IDE action doesn't work
#TODO - spec task - disable fail on error