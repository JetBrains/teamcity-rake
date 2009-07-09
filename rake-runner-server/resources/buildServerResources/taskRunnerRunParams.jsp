<%--
  ~ Copyright 2000-2009 JetBrains s.r.o.
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
      <props:textProperty name="build-file-path" style="width:30em;" maxlength="256"/>
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
      <props:multilineProperty expanded="${propertiesBean.properties['use-custom-build-file'] == true}" name="build-file" rows="10" cols="58" linkTitle="Type the Rakefile content" onkeydown="$('custom2').checked = true;"/>
      <span class="error" id="error_build-file"></span>
    </td>
  </tr>
  <forms:workingDirectory />
  <tr>
    <th><label for="ui.rakeRunner.rake.tasks.names">Rake tasks: </label></th>
    <td><props:textProperty name="ui.rakeRunner.rake.tasks.names" style="width:30em;" maxlength="256"/>
      <span class="smallNote">Enter tasks names separated by space character if you don't want to use 'default' task.<br/>E.g. 'test:functionals' or 'mytask:test mytask:test2'.</span>
    </td>
  </tr>
  <tr>
    <th><label for="ui.rakeRunner.additional.rake.cmd.params">Additional Rake command line parameters: </label></th>
    <td><props:textProperty name="ui.rakeRunner.additional.rake.cmd.params" style="width:30em;" maxlength="256"/>
      <span class="smallNote">If isn't empty these parameters will be added to 'rake' command line.</span>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="Launching Parameters">
  <tr>
    <th><label for="ui.rakeRunner.ruby.interpreter">Ruby interpreter path: </label></th>
    <td><props:textProperty name="ui.rakeRunner.ruby.interpreter" style="width:30em;" maxlength="256"/>
      <span class="smallNote">If not specified the interpreter will be searched in the PATH.</span>
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
      <%-- Test Unit --%>
    <th>
      <label>Test::Unit</label>
    </th>
    <td>
      <props:checkboxProperty name="ui.rakeRunner.frameworks.testunit.enabled"/>
      <label for="ui.rakeRunner.frameworks.testunit.enabled">Enabled</label>
    </td>
    <td/>
  </tr>
  <tr>
      <%-- Test-Spec --%>
    <th>
      <label>Test-Spec</label>
    </th>
    <td>
      <props:checkboxProperty name="ui.rakeRunner.frameworks.testspec.enabled"/>
      <label for="ui.rakeRunner.frameworks.testspec.enabled">Enabled</label>
    </td>
    <td/>
  </tr>

      <%-- Shoulda --%>
    <th>
      <label>Shoulda</label>
    </th>
    <td>
      <props:checkboxProperty name="ui.rakeRunner.frameworks.shoulda.enabled"/>
      <label for="ui.rakeRunner.frameworks.shoulda.enabled">Enabled</label>
    </td>
    <td/>
  </tr>

  <tr>
      <%-- RSpec --%>
    <th>
      <label>RSpec</label>
    </th>
    <td>
      <props:checkboxProperty name="ui.rakeRunner.frameworks.rspec.enabled"/>
      <label for="ui.rakeRunner.frameworks.rspec.enabled">Enabled</label>
      <br/>
      <br/>
      <props:textProperty name="ui.rakeRunner.rspec.specoptions" style="width:30em;" maxlength="256"/>
      <span class="smallNote">
        Rake will be invoked with a "SPEC_OPTS={internal options}
        <span style="font-weight: bold;">{user options}</span>".
      </span>
    </td>
  <tr>
      <%-- Cucumber --%>
    <th>
      <label>Cucumber</label>
    </th>
    <td>
      <props:checkboxProperty name="ui.rakeRunner.frameworks.cucumber.enabled"/>
      <label for="ui.rakeRunner.frameworks.cucumber.enabled">Enabled</label>
      <br/>
      <br/>
      <props:textProperty name="ui.rakeRunner.cucumber.options" style="width:30em;" maxlength="256"/>
          <span class="smallNote">
            Rake will be invoked with a "CUCUMBER_OPTS={internal options}
            <span style="font-weight: bold;">{user options}</span>".
          </span>
    </td>
  </tr>
</l:settingsGroup>