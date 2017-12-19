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

  <style type="text/css">

    .button {
      cursor: pointer;
    }

    .button, .button:hover, button:focus {
      outline:0 !important;
      -webkit-box-shadow: 0px 0px 5px 0px rgba(0,0,0,0.35);
      -moz-box-shadow: 0px 0px 5px 0px rgba(0,0,0,0.35);
      box-shadow: 0px 0px 5px 0px rgba(0,0,0,0.35);
    }

    .button:active {
      /*background-color: #3e8e41;*/
      /*box-shadow: 0 5px #666;*/
      transform: translateY(1px);
      /*color:red !important;*/
    }

    /*a:link {color:#FF0000;}    !* unvisited link, same as regular 'a' *!*/
    /*a:hover {color:#FF00FF;}   !* mouse over link *!*/
    /*a:focus {color:#0000FF;}   !* link has focus *!*/
    /*a:active {color:#0000FF;}  !* selected link *!*/
    /*a:visited {color:#00FF00;} !* visited link *!*/



    .error {
      padding: .1em .5em;
      margin-bottom: 0.5em;
      background-color: burlywood;
      color: brown;
      display: inline-block;
    }

  </style>
</head>
<body>

  <app-root></app-root>