<%--
  ~ Copyright 2000-2011 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<%--Default initial settings format version--%>
<%-- [NB] Config version should be synchronized with RakeRunnerConstants --%>
<props:hiddenProperty name="ui.rakeRunner.config.version" value="2" />

<l:settingsGroup title="Rake Parameters">
  <tr>
    <th>
      <c:set var="onclick">
        if (this.checked) {
        $('build-file-path').focus();
        }
      </c:set>
      <props:radioButtonProperty name="use-custom-build-file" value="" id="custom1"
                                 checked="${empty propertiesBean.properties['use-custom-build-file']}" onclick="${onclick}"/>
      <label for="custom1">Path to a Rakefile:</label>
    </th>
    <td>
      <props:textProperty name="build-file-path"  className="longField" maxlength="256"/>
      <span class="error" id="error_build-file-path"></span>
      <span class="smallNote">Enter Rakefile path if you don't want to use a default one. Specified path should be relative to the checkout directory.</span>
    </td>
  </tr>
  <tr>
    <th>
      <c:set var="onclick">
        if (this.checked) {
        try {
        BS.MultilineProperties.show('build-file', true);
        $('build-file').focus();
        } catch(e) {}
        }
      </c:set>
      <props:radioButtonProperty name="use-custom-build-file" value="true" id="custom2" onclick="${onclick}"/>
      <label for="custom2">Rakefile content:</label>
    </th>
    <td>
      <props:multilineProperty expanded="${propertiesBean.properties['use-custom-build-file'] == true}" name="build-file" rows="10" cols="58" linkTitle="Type the Rakefile content" onkeydown="$('custom2').checked = true;"  className="longField"/>
      <span class="error" id="error_build-file"></span>
    </td>
  </tr>
  <forms:workingDirectory />
  <tr>
    <th><label for="ui.rakeRunner.rake.tasks.names">Rake tasks: </label></th>
    <td><props:textProperty name="ui.rakeRunner.rake.tasks.names"  className="longField" maxlength="256"/>
      <span class="smallNote">Enter tasks names separated by space character if you don't want to use 'default' task.<br/>E.g. 'test:functionals' or 'mytask:test mytask:test2'.</span>
    </td>
  </tr>
  <tr>
    <th><label for="ui.rakeRunner.additional.rake.cmd.params">Additional Rake command line parameters: </label></th>
    <td><props:textProperty name="ui.rakeRunner.additional.rake.cmd.params"  className="longField" expandable="true"/>
      <span class="smallNote">If isn't empty these parameters will be added to 'rake' command line.</span>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="Ruby Interpreter">
  <tr>
    <th>
      <props:radioButtonProperty name="ui.rakeRunner.ruby.use.mode" value="default" id="ui.rakeRunner.ruby.use.mode:default"/>
      <label for="ui.rakeRunner.ruby.use.mode:default">Use default Ruby:</label>
    </th>
    <td>
    <span class="smallNote">E.g. Ruby interpreter provided by  <span style="font-weight: bold;">Ruby Environment Configurator</span> build feature. If build feature isn't configured the interpreter will be searched in the <span
        style="font-weight: bold;">PATH</span> environment variable.</span>
    </td>
  </tr>
  <tr>
    <th>
      <c:set var="onclick">
        if (this.checked) {
          $('ui.rakeRunner.ruby.interpreter.path').focus();
        }
      </c:set>
      <props:radioButtonProperty name="ui.rakeRunner.ruby.use.mode" value="path" id="ui.rakeRunner.ruby.use.mode:path" onclick="${onclick}"/>
      <label for="ui.rakeRunner.ruby.use.mode:path">Ruby interpreter path:</label>
    </th>
    <td>
      <props:textProperty name="ui.rakeRunner.ruby.interpreter.path"  className="longField" maxlength="256"/>
    </td>
  </tr>
  <tr>
    <th>
      <c:set var="onclick">
        if (this.checked) {
          $('ui.rakeRunner.ruby.rvm.sdk.name').focus();
        }
      </c:set>
      <props:radioButtonProperty name="ui.rakeRunner.ruby.use.mode" value="rvm" id="ui.rakeRunner.ruby.use.mode:rvm" onclick="${onclick}"/>
      <label for="ui.rakeRunner.ruby.use.mode:rvm">RVM interpreter:</label>
    </th>
    <td>
      <style type="text/css">
        .rvm_options {
          padding-top: 3px;
        }
        <%----%>
        .rvm_options_editor {
          padding-top: 2px;
        }
      </style>
      <div class="rvm_options">
        Interpreter name:
        <div class="rvm_options_editor">
          <props:textProperty name="ui.rakeRunner.ruby.rvm.sdk.name"  className="longField" maxlength="256"/>
          <span class="smallNote">E.g.: <span style="font-weight: bold;">ruby-1.8.7-p249</span>, <span style="font-weight: bold;">jruby-1.4.0</span> or <span
              style="font-weight: bold;">system</span></span>
        </div>
      </div>
      <div class="rvm_options">
        Gemset:
        <div class="rvm_options_editor">
          <props:textProperty name="ui.rakeRunner.ruby.rvm.gemset.name"  className="longField" maxlength="256"/>
          <span class="smallNote">If not specifed the default gemset will be used.</span>
        </div>
      </div>
    </td>
  </tr>
</l:settingsGroup>
<l:settingsGroup title="Launching Parameters">
  <tr>
  <tr>
    <th>
      <label>Bundler: </label>
    </th>
    <td>
      <props:checkboxProperty name="ui.rakeRunner.bunlder.exec.enabled"/>
      <label for="ui.rakeRunner.bunlder.exec.enabled">bundle exec</label>
      <span class="smallNote">If your project uses <strong>Bundler</strong> gem requirements manager, this option will allow you to launch rake tasks using 'bundle exec' command.</span>
    </td>
  </tr>
  <tr>
    <th>
      <label>Debug: </label>
    </th>
    <td>
      <props:checkboxProperty name="ui.rakeRunner.rake.trace.invoke.exec.stages.enabled"/>
      <label for="ui.rakeRunner.rake.trace.invoke.exec.stages.enabled">Track invoke/execute stages</label>
      <br/>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="Tests Reporting">
  <tr>
    <th>
      <label>Attached reporters:</label>
    </th>

    <td>
      <style type="text/css">
        .rake_reporter {
          padding-top: 3px;
        }

        .rake_reporter_options {
          padding-top: 2px;
          padding-left: 17px;
        }
      </style>

      <%-- Test Unit --%>
      <div class="rake_reporter">
        <props:checkboxProperty name="ui.rakeRunner.frameworks.testunit.enabled"/>
        <label for="ui.rakeRunner.frameworks.testunit.enabled">Test::Unit</label>
      </div>

      <%-- Test-Spec --%>
      <div class="rake_reporter">
        <props:checkboxProperty name="ui.rakeRunner.frameworks.testspec.enabled"/>
        <label for="ui.rakeRunner.frameworks.testspec.enabled">Test-Spec</label>
      </div>

      <%-- Shoulda --%>
      <div class="rake_reporter">
        <props:checkboxProperty name="ui.rakeRunner.frameworks.shoulda.enabled"/>
        <label for="ui.rakeRunner.frameworks.shoulda.enabled">Shoulda</label>
      </div>

      <%-- RSpec --%>
      <div class="rake_reporter">
        <props:checkboxProperty name="ui.rakeRunner.frameworks.rspec.enabled"/>
        <label for="ui.rakeRunner.frameworks.rspec.enabled">RSpec</label>
        <div class="rake_reporter_options">
        <props:textProperty name="ui.rakeRunner.rspec.specoptions"  className="longField" maxlength="256"/>
        <span class="smallNote">Rake will be invoked with a "SPEC_OPTS={internal options}
          <span style="font-weight: bold;">{user options}</span>".
        </span>
        </div>
      </div>

      <%-- Cucumber --%>
      <div class="rake_reporter">
        <props:checkboxProperty name="ui.rakeRunner.frameworks.cucumber.enabled"/>
        <label for="ui.rakeRunner.frameworks.cucumber.enabled">Cucumber</label>
        <div class="rake_reporter_options">
          <props:textProperty name="ui.rakeRunner.cucumber.options"  className="longField" maxlength="256"/>
          <span class="smallNote">Rake will be invoked with a "CUCUMBER_OPTS={internal options}
            <span style="font-weight: bold;">{user options}</span>".
          </span>
        </div>
      </div>
    </td>
  </tr>
</l:settingsGroup>