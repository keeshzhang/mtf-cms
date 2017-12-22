<!doctype html>
<html lang="en">
<head>
  <base href="/">
  <meta charset="utf-8">

  <title>A4 Demo</title>

  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/x-icon" href="favicon.ico">

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

<div style="background-color: black;color: darkgray;">
  <div class="container">
    <div class="row" style="text-align: right">
      <div style="padding: .3em 0;width: 100%;">

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

<div class="row main_content" style="margin-top: 2.3em;">

