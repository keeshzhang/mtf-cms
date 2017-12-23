<#include "../header.ftl">

<div  style="background-color: white;padding: 2em;width: 100%">
  <#if context.isArticlePreview?? && context.isArticlePreview == "yes" || context.content.article_status == "published" >
    <h1 class="article_title">${context.content.title}</h1>
  </#if>

  <app-root style="width:100%;"></app-root>

  <#if context.isArticlePreview?? && context.isArticlePreview == "yes" || context.content.article_status == "published" >
    <div class="article_content">
    ${context.content.html_content}
    </div>
  </#if>
</div>

<#include "../footer.ftl">
