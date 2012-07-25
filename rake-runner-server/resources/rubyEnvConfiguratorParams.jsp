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
<%@include file="globalConsts.jsp" %>
<%@include file="rubyEnvConfiguratorConsts.jsp" %>

<tr>
  <td colspan="2">
    <em>Configures Ruby environment for build steps.</em><bs:help file="Ruby+Environment+Configurator"/>
  </td>
</tr>
<tr>
  <th>
    Mode:
  </th>
  <td>
    <c:set var="modeSelected" value="${propertiesBean.properties[UI_USE_RVM_KEY]}"/>
    <props:selectProperty name="${UI_USE_RVM_KEY}" onchange="BS.RubyEC.onModeChanged()">
      <props:option value=""
                    selected="${empty modeSelected}">Ruby interpreter path</props:option>
      <props:option value="manual"
                    currValue="${modeSelected}">RVM interpreter</props:option>
      <props:option value="rvmrc"
                    currValue="${modeSelected}">RVM with .rvmrc file</props:option>
    </props:selectProperty>
  </td>
  <props:hiddenProperty name="${UI_INNER_RVM_EXIST_REQUIRMENT_KEY}" value=""/>
</tr>
<tr id="rec.interpreter.path.container" style="display: none">
  <th>
    <label for="${UI_USE_RVM_KEY_PATH}">Interpreter path:</label>
  </th>
  <td>
    <props:textProperty name="${UI_RUBY_SDK_PATH_KEY}" className="longField"/>
    <span class="smallNote">If not specified the interpreter will be searched in the <strong>PATH</strong> environment variable.</span>
  </td>
</tr>
<tr id="rec.rvm.interpreter.container" style="display: none">
  <th>
    <label for="${UI_RVM_SDK_NAME_KEY}">Interpreter name: <l:star/></label>
  </th>
  <td>
    <props:textProperty name="${UI_RVM_SDK_NAME_KEY}" className="longField"/>
    <span class="error" id="error_${UI_RVM_SDK_NAME_KEY}"></span>
    <span class="smallNote">E.g.: <strong>ruby-1.8.7-p249</strong>, <strong>jruby-1.4.0</strong> or <strong>system</strong></span>
  </td>
</tr>
<tr id="rec.rvm.gemset.container" style="display: none">
  <th>
    <label for="${UI_RVM_GEMSET_NAME_KEY}">Gemset:</label>
  </th>
  <td>
    <props:textProperty name="${UI_RVM_GEMSET_NAME_KEY}" className="longField"/>
    <span class="smallNote">If not specified the default gemset will be used.</span>
  </td>
</tr>
<tr id="rec.rvm.gemset.create.container" style="display: none">
  <td colspan="2">
    <props:checkboxProperty name="${UI_RVM_GEMSET_CREATE_IF_NON_EXISTS}"/>
    <label for="${UI_RVM_GEMSET_CREATE_IF_NON_EXISTS}">Create gemset if not exist</label>
  </td>
</tr>
<tr id="rec.rvm.rvmrc.container" style="display: none">
  <th>
    <label for="${UI_RVM_RVMRC_PATH_KEY}">Path to a&nbsp;'.rvmrc'&nbsp;file:</label>
  </th>
  <td>
    <nobr>
      <div class="completionIconWrapper">
        <props:textProperty name="${UI_RVM_RVMRC_PATH_KEY}" className="longField"/>
        <bs:vcsTree fieldId="${UI_RVM_RVMRC_PATH_KEY}"/>
      </div>
    </nobr>
    <span class="error" id="error_${UI_RVM_RVMRC_PATH_KEY}"></span>
    <span class="smallNote">Path relative to a checkout directory. Leave empty to use ".rvmrc"</span>
  </td>
</tr>
<tr>
  <td colspan="2">
    <props:checkboxProperty name="${UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY}"/>
    <label for="${UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY}">Fail build if Ruby interpreter wasn't found</label>
  </td>
</tr>

<script type="text/javascript">
  BS.RubyEC = {
    onModeChanged:function () {
      var sel = $('${UI_USE_RVM_KEY}');
      var selectedValue = sel[sel.selectedIndex].value;
      if ('manual' == selectedValue) {
        BS.Util.hide('rec.interpreter.path.container');
        BS.Util.show('rec.rvm.interpreter.container');
        BS.Util.show('rec.rvm.gemset.container');
        BS.Util.show('rec.rvm.gemset.create.container');
        BS.Util.hide('rec.rvm.rvmrc.container');

        $('${UI_RVM_SDK_NAME_KEY}').focus();
        $('${UI_INNER_RVM_EXIST_REQUIRMENT_KEY}').value = "%env.rvm_path%";
      } else if ('rvmrc' == selectedValue) {
        BS.Util.hide('rec.interpreter.path.container');
        BS.Util.hide('rec.rvm.interpreter.container');
        BS.Util.hide('rec.rvm.gemset.container');
        BS.Util.hide('rec.rvm.gemset.create.container');
        BS.Util.show('rec.rvm.rvmrc.container');

        $('${UI_RVM_RVMRC_PATH_KEY}').focus();
        $('${UI_INNER_RVM_EXIST_REQUIRMENT_KEY}').value = "%env.rvm_path%";
      } else {
        BS.Util.show('rec.interpreter.path.container');
        BS.Util.hide('rec.rvm.interpreter.container');
        BS.Util.hide('rec.rvm.gemset.container');
        BS.Util.hide('rec.rvm.gemset.create.container');
        BS.Util.hide('rec.rvm.rvmrc.container');

        $('${UI_RUBY_SDK_PATH_KEY}').focus();
        $('${UI_INNER_RVM_EXIST_REQUIRMENT_KEY}').value = "";
      }
      BS.VisibilityHandlers.updateVisibility('mainContent');
    }
  };
  BS.RubyEC.onModeChanged();
</script>
