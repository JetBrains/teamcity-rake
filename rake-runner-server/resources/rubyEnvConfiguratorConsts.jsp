<%@ page import="jetbrains.buildServer.feature.RubyEnvConfiguratorConstants" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<c:set var="UI_USE_RVM_KEY" value="<%=RubyEnvConfiguratorConstants.UI_USE_RVM_KEY%>"/>
<c:set var="MODE_PATH" value="path"/>
<c:set var="MODE_RVM" value="rvm"/>
<c:set var="MODE_RVMRC" value="rvmrc"/>
<c:set var="UI_USE_RVM_KEY_PATH" value="${UI_USE_RVM_KEY}:${MODE_PATH}"/>
<c:set var="UI_USE_RVM_KEY_RVM" value="${UI_USE_RVM_KEY}:${MODE_RVM}"/>
<c:set var="UI_USE_RVM_KEY_RVMRC" value="${UI_USE_RVM_KEY}:${MODE_RVMRC}"/>

<c:set var="UI_RUBY_SDK_PATH_KEY" value="<%=RubyEnvConfiguratorConstants.UI_RUBY_SDK_PATH_KEY%>"/>

<c:set var="UI_RVM_SDK_NAME_KEY" value="<%=RubyEnvConfiguratorConstants.UI_RVM_SDK_NAME_KEY%>"/>
<c:set var="UI_RVM_GEMSET_NAME_KEY" value="<%=RubyEnvConfiguratorConstants.UI_RVM_GEMSET_NAME_KEY%>"/>
<c:set var="UI_RVM_GEMSET_CREATE_IF_NON_EXISTS" value="<%=RubyEnvConfiguratorConstants.UI_RVM_GEMSET_CREATE_IF_NON_EXISTS%>"/>

<c:set var="UI_RVM_RVMRC_PATH_KEY" value="<%=RubyEnvConfiguratorConstants.UI_RVM_RVMRC_PATH_KEY%>"/>

<c:set var="UI_INNER_RVM_EXIST_REQUIRMENT_KEY" value="<%=RubyEnvConfiguratorConstants.UI_INNER_RVM_EXIST_REQUIRMENT_KEY%>"/>

<c:set var="UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY" value="<%=RubyEnvConfiguratorConstants.UI_FAIL_BUILD_IF_NO_RUBY_FOUND_KEY%>"/>
