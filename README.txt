============================================================
======= ABOUT ==============================================
============================================================
TeamCity plugin for running Rake tasks, Test::Unit tests and Spec examples.

Features:
 * Rake Tasks Monitoring
   ** Execution Progress and Estimation
   ** Tasks artifacts
 * Testing
   ** On-the-fly Test Results Reporting
   ** Test::Unit tests support
   ** RSpec examples support

============================================================
======= INSTALLATION =======================================
============================================================
1. Agent's Reqirements
   * Ruby 1.8.6.
   * Rake 0.7.3 or higher
   * RSpec 1.1.3

   * Builder gem.
     TeamCity Rake Runner uses 'builder' gem. Please install it.
        /> gem install builder
   * Rails gem (only for rails projects)

2. Unpack "rakeRunnerPlugin-XXXX.zip"
3. Copy "rakeRunnerPluginServer.jar"
   to "[Teamcity Server installation directory]/webapps/ROOT/WEB-INF/lib"

4. To install plugin on
   * All agents : Copy "rakeRunnerPluginAgent.zip" to "[Teamcity Server installation directory]/webapps/ROOT/update/plugins/"
   * On particular agent : Copy "rakeRunnerPluginAgent.zip" to "[Teamcity Agent installation directory]/plugins/"

=============================================================
========= Using Rake Runner Plugin  =========================
=============================================================
1. Create build configuration and set "Rake" build runner.
2. Setup runner options.
3. In field "Ruby interpreter path" you can use values of environment and system variables.
E.g.
%env.I_AM_DEFINED_IN_BUILDAGENT_CONFIGURATION%
4. Save options and run build.
5. If you use spec task we advise you to disable option "faile on error"

------------
E.g:

Spec::Rake::SpecTask.new('spec_examples') do |t|
  t.spec_files = FileList['spec/common/**/*_spec.rb']
  t.fail_on_error = false;
  t.rcov = true
end
------------

Otherwise RSpec will show redundant Runtime Exception if any example fails.

6. You can show "rcov" and "rspec" html reports as additional tabs on Build details page.

a) Read "Including Third-Party Reports in the Build Results" article on http://www.jetbrains.net/confluence/display/TCD3/Including+Third-Party+Reports+in+the+Build+Results#IncludingThird-PartyReportsintheBuildResults-TCInfoXML
b) Add
------------
      <report-tab title="RSpec Report" basePath="." startPage="rspec_html_resut.html" />
      <report-tab title="RCov Report" basePath="coverage" />
------------
  to .BuildServer/config/main-config.xml file
c) Add "coverage=>coverage, rspec_html_resut.html" to [Build configuration]/[General Setting]/[Artifact paths]
d) "--format html:rspec_html_resut.html" to [Build configuration]/[Runner: Rake]/[RSpec options(SPEC_OPTS)]
e) Before running tests your rake task should delete "coverage" and "rspec_html_resut.html" files from your project's root.
f) Run build and open build details.

=============================================================
========= NOTES =======================================
=============================================================
1. RakeRunner plugin uses own unit tests runner and loads it with RUBYLIB enviroment variable.
Be sure that your program doesn't clear this environment variable. But obviously you may append
your pathes to it.

2. On windows with enabled "colouring" option RSpec will suggest install "win32console" gem.
 You will see this warning in build log, but you can ignore it. Teamcity Rake Runner Plugin doesn't use this gem.

3. Rake Runner Plugin runs spec examples with custom formatter. If you use additional console
formatter you will see redundant information in Build Log.

4. It seems that Spec::Rake::SpecTask.spec_opts is affected by SPEC_OPTS command line parameter.
Rake Runner Plugin always uses SPEC_OPTS to setup it's custom formatter. Thus you should
setup Spec Options in Web UI.

5. In "Overwiew" tab Teamcity Rake Runner names pending specs as "Ignored Tests"

6. TeamCity Ruby Plugin uses the following format of command line
   'ruby rake {Additional Rake command line parameters} {Teamcity Rake Runner options, e.g TESTOPTS} {tasks names}'.

7. Version of agent's part of plugin - see in rakeRunnerPluginAgent.zip/version
   Version of server's part of plugin - see in rakeRunnerPluginServer.jar/version
=============================================================
========= TO DO =============================================
=============================================================
* "Open in IDE" action doesn't work for tests and spec examples
