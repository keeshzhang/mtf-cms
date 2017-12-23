<#include "header.ftl">

<app-root></app-root>

<div style="margin-top: 1em !important;width:100%;">

<#if context.canCreatePage>
  <div class="float-xs-right">
    <form class="form-inline" action="/create" method="post">

      <div class="form-group" style="width: 50%;float: left;">
        <input style="    margin: 0;" type="text" class="form-control" id="name" name="name" placeholder="New page name">
      </div>

      <button type="submit" class="basic-btn button" style="margin-left: .8em;margin-bottom: 0;">创建新文章</button>
    </form>
  </div>
</#if>

  <h2 class="display-4" style="margin-top: .6em;font-size: 2em;">${context.title}</h2>
</div>

<div style="width:100%;">

  <#list context.content>
    <ul>
      <#items as page>
      <li><a href="${page["url"]}">${page["name"]}, </a>
        <br />
        <span style="color: goldenrod;">${page["url"]}</span>
        <br />
        <span style="color: goldenrod;">${page["file_path"]}</span>
      </li>
      </#items>
    </ul>
    <#else>
    <p style="color: darkorchid">The wiki is currently empty!</p>
  </#list>

  <a style="display: none;" ="submit" href="/backup" class="btn btn-info">Gist Backup</a>

  <div style="display: none;padding: 1em;
  margin-top: 1em;
  background-color: burlywood;">
    <p style="margin-bottom: .3em;color: brown;">Successfully creatd a backup:</p>
    <a href="https://gist.github.com/58abd4dc8636685140b4ec2e174295f1 " target="_blank">
      https://gist.github.com/58abd4dc8636685140b4ec2e174295f1
    </a>
  </div>

</divs>

</div>

<#include "footer.ftl">
