<%--Copyright 2000-2012 JetBrains s.r.o.--%>

<%--Licensed under the Apache License, Version 2.0 (the "License");--%>
<%--you may not use this file except in compliance with the License.--%>
<%--You may obtain a copy of the License at--%>

<%--http://www.apache.org/licenses/LICENSE-2.0--%>

<%--Unless required by applicable law or agreed to in writing, software--%>
<%--distributed under the License is distributed on an "AS IS" BASIS,--%>
<%--WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.--%>
<%--See the License for the specific language governing permissions and--%>
<%--limitations under the License.--%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<tr>
  <td colspan="2">
    <em>Configures Ruby environment for build steps.</em><bs:help file="Ruby+Environment+Configurator"/>
  </td>
</tr>
<tr>
  <th style="width:33%">
    <c:set var="onclick">
      if (this.checked) {
        $('ui.ruby.configurator.ruby.interpreter.path').focus();
        $('ui.ruby.configurator.rvm.path').value='';
      }
    </c:set>
    <props:radioButtonProperty name="ui.ruby.configurator.use.rvm" value="" id="ui.ruby.configurator.use.rvm:path"
                               checked="${empty propertiesBean.properties['ui.ruby.configurator.use.rvm']}" onclick="${onclick}"/>
    <label for="ui.ruby.configurator.use.rvm:path">Ruby interpreter path:</label>
  </th>
  <td>
    <props:textProperty name="ui.ruby.configurator.ruby.interpreter.path" style="width:25em;" maxlength="256" className="buildTypeParams"/>
    <span class="smallNote">If not specified the interpreter will be searched in the <span style="font-weight: bold;">PATH</span> environment variable.</span>
  </td>
</tr>
<tr>
  <th>
    <c:set var="onclick">
      if (this.checked) {
        $('ui.ruby.configurator.rvm.sdk.name').focus();
        $('ui.ruby.configurator.rvm.path').value='%env.rvm_path%';
      }
    </c:set>
    <props:radioButtonProperty name="ui.ruby.configurator.use.rvm" value="manual" id="ui.ruby.configurator.use.rvm:rvm" onclick="${onclick}"/>
    <label for="ui.ruby.configurator.use.rvm:rvm">RVM interpreter:</label>
    <props:hiddenProperty name="ui.ruby.configurator.rvm.path" value=""/>
  </th>
  <td>
    <style type="text/css">
      .rvm_options {
        padding-top: 3px;
      }

      .rvm_options_editor {
        padding-top: 2px;
      }
    </style>
    <div class="rvm_options">
      Interpreter name:
      <div class="rvm_options_editor">
        <props:textProperty name="ui.ruby.configurator.rvm.sdk.name" style="width:25em;" maxlength="256" className="buildTypeParams"/>
        <span class="smallNote">E.g.: <span style="font-weight: bold;">ruby-1.8.7-p249</span>, <span
            style="font-weight: bold;">jruby-1.4.0</span> or <span style="font-weight: bold;">system</span></span>
      </div>
      <script type="text/javascript">
        // TODO: This is a temporary fix for TW-20333. Must replaced with db converter.
        if (!$('ui.ruby.configurator.use.rvm:path').checked && !$('ui.ruby.configurator.use.rvm:rvm').checked) {
          $('ui.ruby.configurator.use.rvm:rvm').checked = true;
          $('ui.ruby.configurator.rvm.sdk.name').focus();
          $('ui.ruby.configurator.rvm.path').value="%env.rvm_path%";
        }
        // End of fix
        if ($('ui.ruby.configurator.use.rvm:path').checked) {
          $('ui.ruby.configurator.ruby.interpreter.path').focus();
          $('ui.ruby.configurator.rvm.path').value="";
        }
        if ($('ui.ruby.configurator.use.rvm:rvm').checked) {
          $('ui.ruby.configurator.rvm.sdk.name').focus();
          $('ui.ruby.configurator.rvm.path').value="%env.rvm_path%";
        }
//        if ($('ui.ruby.configurator.use.rvm:rvmrc').checked) {
//          $('ui.ruby.configurator.rvm.rvmrc.path').focus();
//          $('ui.ruby.configurator.rvm.path').value="%env.rvm_path%";
//        }
      </script>
    </div>
    <div class="rvm_options">
      Gemset:
      <div class="rvm_options_editor">
        <props:textProperty name="ui.ruby.configurator.rvm.gemset.name" style="width:25em;" maxlength="256" className="buildTypeParams"/>
        <span class="smallNote">If not specifed the default gemset will be used.</span>
      </div>
    </div>
  </td>
</tr>
<%--<tr>--%>
  <%--<th>--%>
    <%--<c:set var="onclick">--%>
      <%--if (this.checked) {--%>
        <%--$('ui.ruby.configurator.rvm.rvmrc.path').focus();--%>
        <%--$('ui.ruby.configurator.rvm.path').value='%env.rvm_path%';--%>
      <%--}--%>
    <%--</c:set>--%>
    <%--<props:radioButtonProperty name="ui.ruby.configurator.use.rvm" value="rvmrc" id="ui.ruby.configurator.use.rvm:rvmrc" onclick="${onclick}"/>--%>
    <%--<label for="ui.ruby.configurator.use.rvm:rvmrc">RVM with .rvmrc file:</label>--%>
  <%--</th>--%>
  <%--<td>--%>
    <%--<style type="text/css">--%>
      <%--.rvm_options {--%>
        <%--padding-top: 3px;--%>
      <%--}--%>

      <%--.rvm_options_editor {--%>
        <%--padding-top: 2px;--%>
      <%--}--%>
    <%--</style>--%>
    <%--<div class="rvm_options">--%>
      <%--Path to a '.rvmrc' file:--%>
      <%--<div class="rvm_options_editor">--%>
        <%--<props:textProperty name="ui.ruby.configurator.rvm.rvmrc.path" style="width:25em;" maxlength="256" className="buildTypeParams"/>--%>
        <%--<span class="smallNote">Path relative to a checkout directory</span>--%>
      <%--</div>--%>
    <%--</div>--%>
  <%--</td>--%>
<%--</tr>--%>
<td colspan="2">
  <props:checkboxProperty name="ui.ruby.configurator.fail.build.if.interpreter.not.found"/>
  <label for="ui.ruby.configurator.fail.build.if.interpreter.not.found">Fail build if Ruby interpreter wasn't found</label>
</td>
