<#-- Safe check if selectable is defined -->
<#if selectable?? && selectable>
    ${theme.include(top_head_include)}
</#if>

<div class="portlet-layout my-custom-layout" id="main-content" role="main">

    <div class="row">
        <div class="col-md-12 portlet-column portlet-column-only" id="column-1">
            ${processor.processColumn("column-1", "portlet-column-content portlet-column-content-only")}
        </div>
    </div>
<div class="app-wrapper home-page main-content">
    <div class="row">
        <div class="col-md-8 portlet-column portlet-column-first" id="column-2">
            ${processor.processColumn("column-2", "portlet-column-content portlet-column-content-first")}
        </div>
        <div class="col-md-4 portlet-column portlet-column-last" id="column-3">
            ${processor.processColumn("column-3", "portlet-column-content portlet-column-content-last")}
        </div>
    </div>

    <div class="row">
        <div class="col-md-12 portlet-column portlet-column-only" id="column-6">
            ${processor.processColumn("column-6", "portlet-column-content portlet-column-content-only")}
        </div>
    </div>

    <div class="row">
        <div class="col-md-7 portlet-column portlet-column-first" id="column-7">
            ${processor.processColumn("column-7", "portlet-column-content portlet-column-content-first")}
        </div>
        <div class="col-md-5 portlet-column portlet-column-last" id="column-8">
            ${processor.processColumn("column-8", "portlet-column-content portlet-column-content-last")}
        </div>
    </div>
</div>
</div>

<#if selectable?? && selectable>
    ${theme.include(bottom_include)}
</#if>
