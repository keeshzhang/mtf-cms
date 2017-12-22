<#include "../header.ftl">

<#if context.isArticlePreview == "yes" >
  <h1 class="article_title">${context.content.title}</h1>
</#if>

<app-root style="width:100%;"></app-root>

<#include "../footer.ftl">
