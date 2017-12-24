<html lang="zh-CN">
<head>
  <base href="/">
  <meta http-equiv="content-type" content="text/html;charset=utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=Edge">
  <title><#if context.seo?? && context.seo.title??>${context.seo.title}<#else>Content Management System</#if></title>
  <#if context.seo?? && context.seo.description??>
    <meta name="description" content="${context.seo.description}">
  <#else>
    <meta name="description" content="The page description">
  </#if>
  <#if context.seo?? && context.seo.keywords??>
    <meta name="keywords" content="${context.seo.keywords}">
  <#else>
    <meta name="keywords" content="The page keywords">
  </#if>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/x-icon" href="favicon.ico">
  <!-- cssset -->
  <link media="all" type="text/css" rel="stylesheet"
        href="/assets/libs/normalize-7.0.0.css">
  <link media="all" type="text/css" rel="stylesheet"
        href="/assets/libs/fonts-family-roboto.css">
  <!--Font Icons: https://material.io/icons/-->
  <link media="all" type="text/css" rel="stylesheet"
        href="/assets/libs/google-material-icons.css">
  <!-- Foundation Flex: https://cdnjs.com/libraries/foundation/6.3.0 -->
  <link media="all" type="text/css" rel="stylesheet"
        href="/assets/libs/foundation-flex-6.3.0.min.css">
  <link href="/assets/stylessheets/froala_editor.pkgd.min.css" rel="stylesheet">
  <link href="/assets/stylessheets/font-awesome.min.css" rel="stylesheet">
  <link href="/assets/libs/codemirror-5.25.0.min.css" rel="stylesheet">
  <!-- site style -->
  <link media="all" type="text/css" rel="stylesheet"
        href="/assets/styles.css">
  <!-- angular bundle -->
  <link href="/assets/styles.bundle.css" rel="stylesheet">
</head>
<body>


<#if (!context.isArticlePreview?? || context.isArticlePreview == 'no') || 1==1 >
<div style="background-color: black;color: darkgray;">
  <div class="container">
    <div class="row" style="position: relative;line-height: 2.5em;">

      <div class="top-menu-left">
        <a href="/" style="color: whitesmoke;text-decoration: none;display: inline-block;padding: 0 .5em;background-color: darkcyan;">首页</a>
      </div>

      <div class="top-menu-right" style="right: 0;position: absolute;">

        <#if context.username == "anonymous user" >
          <a href="/login" style="color: whitesmoke;text-decoration: none;">登录</a>
        </#if>

        <#if context.username != "anonymous user" >
          <a href="/logout" role="button" aria-pressed="true" style="color: whitesmoke;text-decoration: none;">${context.username}</a>
        </#if>

      </div>
    </div>
  </div>
</div>
</#if>

<div class="row main_content" style="margin-top: 2.3em;">

