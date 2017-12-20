<#include "themes/header.ftl">
<div class="row">
  <div style="width: 100%;max-width: 450px;margin: auto;margin-top: 40px;    margin-top: 2em;">
    <form action="/login-auth" method="POST" class="form-horizontal" role="form">

      <#if context.error >
        <div class="form-group">
          <div class="alert alert-danger"  style="font-size: .7em;padding: .3em 0.8em;background-color: brown;display: inline-block;margin-bottom: 1em;color: white;" role="alert">
            用户名或密码错误!
          </div>
        </div>
      </#if>


      <div class="form-group">
        <label for="username" class="col-sm-2 control-label">名字</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" id="username" name="username" placeholder="login">
        </div>
      </div>
      <div class="form-group">
        <label for="password" class="col-sm-2 control-label">姓</label>
        <div class="col-sm-10">
          <input type="password" class="form-control" id="password" name="password" placeholder="password">
        </div>
      </div>
      <!--div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
          <div class="checkbox">
            <label>
              <input type="checkbox">请记住我
            </label>
          </div>
        </div>
      </div-->
      <div class="form-group">


        <div style="text-align: right">
          <button type="submit" class="basic-btn button" style="padding: .6em 1.35em .6em 1.35em;">登录</button>
        </div>
      </div>
    </form>
  </div>
</div>
<#include "themes/footer.ftl">
