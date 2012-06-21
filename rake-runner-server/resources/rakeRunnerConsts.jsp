<%@ page import="jetbrains.buildServer.rakerunner.RakeRunnerConstants" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="UI_RAKE_TASKS_PROPERTY" value="<%=RakeRunnerConstants.SERVER_UI_RAKE_TASKS_PROPERTY%>"/>
<c:set var="UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED" value="<%=RakeRunnerConstants.SERVER_UI_RAKE_TRACE_INVOKE_EXEC_STAGES_ENABLED%>"/>
<c:set var="UI_RAKE_ADDITIONAL_CMD_PARAMS_PROPERTY" value="<%=RakeRunnerConstants.SERVER_UI_RAKE_ADDITIONAL_CMD_PARAMS_PROPERTY%>"/>
<c:set var="UI_RUBY_INTERPRETER_ADDITIONAL_PARAMS" value="<%=RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_ADDITIONAL_PARAMS%>"/>
<c:set var="UI_RUBY_INTERPRETER_PATH" value="<%=RakeRunnerConstants.SERVER_UI_RUBY_INTERPRETER_PATH%>"/>
<c:set var="UI_RUBY_RVM_GEMSET_NAME" value="<%=RakeRunnerConstants.SERVER_UI_RUBY_RVM_GEMSET_NAME%>"/>
<c:set var="UI_RUBY_RVM_SDK_NAME" value="<%=RakeRunnerConstants.SERVER_UI_RUBY_RVM_SDK_NAME%>"/>
<c:set var="UI_BUNDLE_EXEC_PROPERTY" value="<%=RakeRunnerConstants.SERVER_UI_BUNDLE_EXEC_PROPERTY%>"/>
<c:set var="UI_RAKE_TESTUNIT_ENABLED_PROPERTY" value="<%=RakeRunnerConstants.SERVER_UI_RAKE_TESTUNIT_ENABLED_PROPERTY%>"/>
<c:set var="UI_RAKE_RSPEC_ENABLED_PROPERTY" value="<%=RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_ENABLED_PROPERTY%>"/>
<c:set var="UI_RAKE_RSPEC_OPTS_PROPERTY" value="<%=RakeRunnerConstants.SERVER_UI_RAKE_RSPEC_OPTS_PROPERTY%>"/>
<c:set var="UI_RAKE_TESTSPEC_ENABLED_PROPERTY" value="<%=RakeRunnerConstants.SERVER_UI_RAKE_TESTSPEC_ENABLED_PROPERTY%>"/>
<c:set var="UI_RAKE_SHOULDA_ENABLED_PROPERTY" value="<%=RakeRunnerConstants.SERVER_UI_RAKE_SHOULDA_ENABLED_PROPERTY%>"/>
<c:set var="UI_RAKE_CUCUMBER_ENABLED_PROPERTY" value="<%=RakeRunnerConstants.SERVER_UI_RAKE_CUCUMBER_ENABLED_PROPERTY%>"/>
<c:set var="UI_RAKE_CUCUMBER_OPTS_PROPERTY" value="<%=RakeRunnerConstants.SERVER_UI_RAKE_CUCUMBER_OPTS_PROPERTY%>"/>

<c:set var="UI_RUBY_USAGE_MODE" value="<%=RakeRunnerConstants.SERVER_UI_RUBY_USAGE_MODE%>"/>
<c:set var="MODE_DEFAULT" value="default"/>
<c:set var="MODE_PATH" value="path"/>
<c:set var="MODE_RVM" value="rvm"/>
<c:set var="UI_RUBY_USAGE_MODE_DEFAULT" value="${UI_RUBY_USAGE_MODE}:${MODE_DEFAULT}"/>
<c:set var="UI_RUBY_USAGE_MODE_PATH" value="${UI_RUBY_USAGE_MODE}:${MODE_PATH}"/>
<c:set var="UI_RUBY_USAGE_MODE_RVM" value="${UI_RUBY_USAGE_MODE}:${MODE_RVM}"/>


<c:set var="CONFIGURATION_VERSION_PROPERTY" value="<%=RakeRunnerConstants.SERVER_CONFIGURATION_VERSION_PROPERTY%>"/>
<c:set var="CONFIGURATION_VERSION_CURRENT" value="<%=RakeRunnerConstants.CURRENT_CONFIG_VERSION%>"/>

