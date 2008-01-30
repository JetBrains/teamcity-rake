<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<l:settingsGroup title="Rake task">
    <tr>
        <th><label for="rakeRunner.rake.task.name">Rake task name: <l:star/></label></th>
        <td><props:textProperty name="rakeRunner.rake.task.name" style="width:30em;" maxlength="256"/>
            <span class="error" id="error_rakeRunner.rake.task.name"></span>
            <span class="smallNote">(e.g. test:functionals or mytask::test)</span>
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
            <props:checkboxProperty name="rakeRunner.rake.options.quite"/>
            <label for="rakeRunner.rake.options.quite">Do not log messages to standard output (--quite).</label>
        </td>
    </tr>
</l:settingsGroup>

<tr>
    <th><label>Debug:</label></th>
    <td>
        <props:checkboxProperty name="rakeRunner.debug"/>
        <label for="rcodedup.debug">Enable debug messages in the build log</label>
    </td>
</tr>