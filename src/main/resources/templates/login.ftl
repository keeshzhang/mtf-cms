<#include "themes/header.ftl">

<span class="form-container email-component">
  <!--a href="/login" id="goback">Go back</a-->

  <h5 style="text-align: center;">用户登录</h5>

  <!--span class="error" *ngIf="error" [@fallIn]="state">{{ error }}</span-->

  <#if context.error >
    <span class="error">用户名或密码错误!</span>
  </#if>

  <form action="/login-auth" method="POST" class="form-horizontal" role="form">

    <div class="input-group">
      <span class="input-group-label" style="padding: 0 .425rem;color:dimgray;">
        <i class="material-icons">&#xE8D3;</i>
      </span>
      <input  type="text" id="username" name="username" class="input-group-field" placeholder="Email address">
    </div>


    <div class="input-group">
      <span class="input-group-label" style="padding: 0 .425rem;color:dimgray;">
        <i class="material-icons">&#xE897;</i>
      </span>
      <input  type="password" id="password" name="password" class="input-group-field" placeholder="Password">
    </div>


    <div style="text-align: center;">
      <button type="submit" class="basic-btn button">登录</button>
    </div>
    <!--a href="/registry"  class="alc">Don's have an account</a-->

  </form>

</div>


<div class="row">
  <div class="col-md-12 mt-1">
    <form action="/login-auth" method="POST" class="form-horizontal" role="form">

      <#if context.error >
        <div class="form-group">
          <div class="alert alert-danger"  style="margin: 0 15px;" role="alert">
            登录失败: <br />
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


        <div class="col-sm-offset-2 col-sm-10">
          <button type="submit" class="basic-btn button">登录</button>
        </div>
      </div>
    </form>
  </div>
</div>
<#include "themes/footer.ftl">
