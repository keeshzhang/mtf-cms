<#include "../header.ftl">
<div class="row">

  <div style="width: 100%; margin-top: 1em !important;">
    <span class="float-xs-right">
      <a class="btn btn-outline-primary" href="/" role="button" aria-pressed="true">Home</a>
        <button class="btn btn-outline-warning" type="button"
                data-toggle="collapse"
                data-target="#editor" aria-expanded="false" aria-controls="editor">Edit</button>
    </span>
    <h1 class="display-4" style="padding-top: .3em;">
      <span class="text-muted">{</span> ${context.id}
      <span class="text-muted">}</span>
    </h1>
  </div>

  <div class= clearfix" style="width: 100%;padding: 1em;    padding-bottom: .5em;">
  ${context.title}
  </div>

  <div class=" clearfix" id="editor" style="width: 100%;">

    <form action="/save" method="post">

      <div class="form-group">

        <input type="hidden" name="id" value="${context.id}">
        <input type="hidden" name="title" value="${context.title}">
        <input type="hidden" name="newPage" value="${context.newPage}">

        <textarea class="form-control" id="markdown" name="markdown" rows="15">${context.rawContent}</textarea>

      </div>

      <button type="submit" class="btn btn-primary">Save</button>

      <#if context.id != -1>
        <button type="submit" formaction="/delete" class="btn btn-danger float-xs-right">Delete</button>
      </#if>
    </form>

  </div>

  <div style="width: 100%;">
    <hr class="mt-1">
    <p class="small">Rendered: ${context.timestamp}</p>
  </div>

</div>
<#include "../footer.ftl">
