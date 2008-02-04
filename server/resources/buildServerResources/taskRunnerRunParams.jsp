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
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<l:settingsGroup title="Rake task">
    <tr>
        <th><label for="rakeRunner.rake.task.name">Rake task name: </label></th>
        <td><props:textProperty name="rakeRunner.rake.task.name" style="width:30em;" maxlength="256"/>
            <span class="smallNote">(e.g. 'test:functionals' or 'mytask::test'. If empty 'default' task will be used)</span>
        </td>
    </tr>
</l:settingsGroup>

<forms:workingDirectory />

<l:settingsGroup title="Rake options">
    <tr>
        <th>
            <label>Options:</label>
        </th>
        <td>
            <props:checkboxProperty name="rakeRunner.rake.options.trace"/>
            <label for="rakeRunner.rake.options.trace">Turn on invoke/execute tracing, enable full backtrace (--trace).</label>
            <br/>
            <props:checkboxProperty name="rakeRunner.rake.options.quiet"/>
            <label for="rakeRunner.rake.options.quiet">Do not log messages to standard output (--quiet).</label>
            <br/>
            <props:checkboxProperty name="rakeRunner.rake.options.dryrun"/>
            <label for="rakeRunner.rake.options.dryrun">Do a dry run without executing actions (--dry-run).</label>
        </td>
    </tr>
</l:settingsGroup>

<tr>
    <th><label>Debug:</label></th>
    <td>
        <props:checkboxProperty name="rakeRunner.debug"/>
        <label for="rcodedup.debug">Enable debug messages in the build log</label>
        <span class="smallNote">This is internal option, for plugin debugging.</span>
    </td>
</tr>