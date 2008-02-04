<%--
  ~ Copyright 2000-2008 JetBrains s.r.o.
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
  Rake task name: <strong><props:displayValue name="rakeRunner.rake.task.name" emptyValue="default"/></strong>
</div>

<props:viewWorkingDirectory />

<div class="nestedParameter">
  <ul style="list-style: none; padding-left: 0; margin-left: 0; margin-top: 0.1em; margin-bottom: 0.1em;">
    <li>Turn on invoke/execute tracing, enable full backtrace (--trace): <strong><props:displayCheckboxValue name="rakeRunner.rake.options.trace"/></strong></li>
    <li>Do not log messages to standard output (--quiet): <strong><props:displayCheckboxValue name="rakeRunner.rake.options.quiet"/></strong></li>
    <li>Do a dry run without executing actions (--dry-run): <strong><props:displayCheckboxValue name="rakeRunner.rake.options.dryrun"/></strong></li>
    <li>Rake tests options: <strong><props:displayValue name="rakeRunner.testoptions" emptyValue="not specified"/></strong></li>
    <li>Additional rake arguments: <strong><props:displayValue name="rakeRunner.other.rake.args" emptyValue="not specified"/></strong></li>
  </ul>
</div>

<div class="parameter">
  Enable debug messages: <strong><props:displayCheckboxValue name="rakeRunner.debug"/></strong>
</div>