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

<%--<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>--%>
<%@include file="globalConsts.jsp" %>
<%@include file="rubyEnvConfiguratorConsts.jsp" %>

<tr>
  <td colspan="2">
    <em>Configures Ruby environment for build steps.</em><bs:help file="Ruby+Environment+Configurator"/>
  </td>
</tr>
<tr>
  <th style="width:33%">
    <c:set var="onclick">
      if (this.checked) {
      $('${UI_RUBY_SDK_PATH_KEY}').focus();
      $('${UI_INNER_RVM_EXIST_REQUIRMENT_KEY}').value='';
      }
    </c:set>
    <props:radioButtonProperty name="${UI_USE_RVM_KEY}" value="" id="${UI_USE_RVM_KEY_PATH}" onclick="${onclick}"/>
    <%--checked="${empty propertiesBean.properties[UI_USE_RVM_KEY]}"--%>
    <label for="${UI_USE_RVM_KEY_PATH}">Ruby interpreter path:</label>
  </th>
  <td>
    <props:textProperty name="${UI_RUBY_SDK_PATH_KEY}" style="width:25em;" maxlength="256" className="buildTypeParams"/>
    <span class="smallNote">If not specified the interpreter will be searched in the <strong>PATH</strong> environment variable.</span>
  </td>
</tr>
<tr>
  <th>
    <c:set var="onclick">
      if (this.checked) {
      $('${UI_RVM_SDK_NAME_KEY}').focus();
      $('${UI_INNER_RVM_EXIST_REQUIRMENT_KEY}').value='%env.rvm_path%';
      }
    </c:set>
    <props:radioButtonProperty name="${UI_USE_RVM_KEY}" value="manual" id="${UI_USE_RVM_KEY_RVM}" onclick="${onclick}"/>
    <label for="${UI_USE_RVM_KEY_RVM}">RVM interpreter:</label>
    <props:hiddenProperty name="${UI_INNER_RVM_EXIST_REQUIRMENT_KEY}" value=""/>
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
        <props:textProperty name="${UI_RVM_SDK_NAME_KEY}" style="width:25em;" maxlength="256" className="buildTypeParams"/>
        <span class="error" id="error_${UI_RVM_SDK_NAME_KEY}"></span>
        <span class="smallNote">E.g.: <strong>ruby-1.8.7-p249</strong>, <strong>jruby-1.4.0</strong> or <strong>system</strong></span>
      </div>
      <script type="text/javascript">
        if ($('${UI_USE_RVM_KEY_PATH}').checked) {
          $('${UI_RUBY_SDK_PATH_KEY}').focus();
          $('${UI_INNER_RVM_EXIST_REQUIRMENT_KEY}').value = "";
        }
        if ($('${UI_USE_RVM_KEY_RVM}').checked) {
          $('${UI_RVM_SDK_NAME_KEY}').focus();
          $('${UI_INNER_RVM_EXIST_REQUIRMENT_KEY}').value = "%env.rvm_path%";
        }
        if ($('${UI_USE_RVM_KEY_RVMRC}').checked) {
          $('${UI_RVM_RVMRC_PATH_KEY}').focus();
          $('${UI_INNER_RVM_EXIST_REQUIRMENT_KEY}').value = "%env.rvm_path%";
        }
      </script>
    </div>
    <div class="rvm_options">
      Gemset:
      <div class="rvm_options_editor">
        <props:textProperty name="${UI_RVM_GEMSET_NAME_KEY}" style="width:25em;" maxlength="256" className="buildTypeParams"/>
        <span class="smallNote">If not specifed the default gemset will be used.</span>
      </div>
    </div>
  </td>
</tr>
<tr>
  <th>
    <c:set var="onclick">
      if (this.checked) {
      $('${UI_RVM_RVMRC_PATH_KEY}').focus();
      $('${UI_INNER_RVM_EXIST_REQUIRMENT_KEY}').value='%env.rvm_path%';
      }
    </c:set>
    <props:radioButtonProperty name="${UI_USE_RVM_KEY}" value="${MODE_RVMRC}" id="${UI_USE_RVM_KEY_RVMRC}" onclick="${onclick}"/>
    <label for="${UI_USE_RVM_KEY_RVMRC}">RVM with .rvmrc file:</label>
  </th>
  <td>
    <div class="rvm_options">
      Path to a '.rvmrc' file:
      <div class="rvm_options_editor">
        <props:textProperty name="${UI_RVM_RVMRC_PATH_KEY}" style="width:25em;" maxlength="256" className="buildTypeParams"/><bs:vcsTree fieldId="${UI_RVM_RVMRC_PATH_KEY}"/>
        <span class="smallNote">Path relative to a checkout directory. Leave empty to use ".rvmrc"</span>
      </div>
    </div>
  </td>
</tr>
<td colspan="2">
  <props:checkboxProperty name="${UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY}"/>
  <label for="${UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY}">Fail build if Ruby interpreter wasn't found</label>
</td>
