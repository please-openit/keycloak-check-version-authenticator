<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=false; section>
    <#if section = "title">
        New version available
    <#elseif section = "header">
        New version available
    <#elseif section = "form">
    <h1>New version available</h1>
        <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <h3>Actual : ${current}</h3>
                <h3>Available : ${available}</h3>
            </div>

            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" value="Ok"/>
        </form>
    </#if>
</@layout.registrationLayout>
