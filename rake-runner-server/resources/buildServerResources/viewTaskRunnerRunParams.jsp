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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<div class="parameter">
  Rakefile file:
  <c:choose>
    <c:when test="${empty propertiesBean.properties['use-custom-build-file']}">
      <props:displayValue name="build-file-path" emptyValue="not specified"/>
    </c:when>
    <c:otherwise>
      <props:displayValue name="build-file" emptyValue="<empty>" showInPopup="true" popupTitle="Rakefile content" popupLinkText="view Rakefile content"/>
    </c:otherwise>
  </c:choose>
</div>

<props:viewWorkingDirectory />

<div class="parameter">
  Rake tasks: <strong><props:displayValue name="ui.rakeRunner.rake.tasks.names" emptyValue="default"/></strong>
</div>

<div class="parameter">
    Additional Rake command line parameters: <strong><props:displayValue name="ui.rakeRunner.additional.rake.cmd.params" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
    Launching Parameters:
    <div class="nestedParameter">
      <ul style="list-style: none; padding-left: 0; margin-left: 0; margin-top: 0.1em; margin-bottom: 0.1em;">
          <li>Ruby interpreter path: <strong><props:displayValue name="ui.rakeRunner.ruby.interpreter" emptyValue="will be searched in the PATH"/></strong></li>
          <li>Track invoke/execute stages<strong><props:displayCheckboxValue name="ui.rakeRunner.rake.trace.invoke.exec.stages.enabled"/></strong></li>
      </ul>
    </div>
</div>

<div class="parameter">
    Tests Parameters:
    <div class="nestedParameter">
        <ul style="list-style: none; padding-left: 0; margin-left: 0; margin-top: 0.1em; margin-bottom: 0.1em;">
            <li>Tests options(TESTOPTS): <strong><props:displayValue name="ui.rakeRunner.test.unit.options" emptyValue="not specified"/></strong></li>
            <li>RSpec options(SPEC_OPTS): <strong><props:displayValue name="ui.rakeRunner.rspec.specoptions" emptyValue="not specified"/></strong></li>
      </ul>
    </div>
</div>