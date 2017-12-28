<#include "header.ftl">


<div style="margin-top: 1em !important;width:100%;">

  <#if context.error == "yes">
    <div class="form-group">
      <div class="alert alert-danger"  style="font-size: .7em;padding: .3em 0.8em;background-color: brown;display: inline-block;margin-bottom: 1em;color: white;" role="alert">
        错误: 文章名字不能为空、文章名字重复或文章名字太长!
      </div>
    </div>
  </#if>

  <#if context.canCreatePage>
    <div class="float-xs-right">
      <form class="form-inline" action="/articles" method="post">

        <div class="form-group" style="width: 50%;float: left;">
          <input style="    margin: 0;" type="text" class="form-control" id="title" name="article_title" placeholder="New page name">
        </div>

        <button type="submit" class="basic-btn button" style="margin-left: .8em;margin-bottom: 0;">创建新文章</button>
      </form>
    </div>
  </#if>

  <h2 class="display-4" style="margin-top: .6em;font-size: 2em;color: burlywood;font-style: oblique;">${context.title}</h2>
</div>

<div style="width:100%;">

  <input type="hidden" id="hidArticlePageSize" value="2" />
  <input type="hidden" id="hidArticleListSize" value="${context.content?size}" />

  <#list context.content>
    <ul>
      <#items as page>
      <li>
        <a style="color: lightseagreen;font-size: 1.2em;font-weight: bold;" href="${page["url_escape"]}">${page["title"]}</a>

        <br />
        <#if page["article_status"] == "published" >
          <span style="color: darkgoldenrod;font-size: .85em;">发布时间: ${page["published_at"]}, </span>
        </#if>

        <#if context.username != "anonymous user" >
          <span style="color: cadetblue;font-size: .85em;">最后更新时间: ${page["last_updated"]}</span>
          <span style="color: crimson;font-style: oblique;font-size: .85em;">${page["article_status_name"]}</span>
        </#if>

        <br />
        <p style="color:dimgray;">${page["description"]}</p>
      </li>
      </#items>


      <app-root></app-root>

    </ul>
  <#else>
    <app-root></app-root>
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
