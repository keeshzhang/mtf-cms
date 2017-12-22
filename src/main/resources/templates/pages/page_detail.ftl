<#include "../header.ftl">

<#if context.isArticlePreview == "yes" >
  <h1 class="article_title">${context.content.title}</h1>
</#if>

<app-root style="width:100%;"></app-root>

<#if context.isArticlePreview == "yes" >
  <div class="article_content">
  ${context.content.html_content}
  </div>
</#if>


<#include "../footer.ftl">
