package com.company.permissionmanagement.view.resourcerolemodeldetail;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelDetailView;

@Route(value = "sec/resourcerolemodels/:code", layout = DefaultMainViewParent.class)
@ViewController(id = "sec_ResourceRoleModel.detail")
@ViewDescriptor(path = "ext-resource-role-model-detail-view.xml")
public class ExtResourceRoleModelDetailView extends ResourceRoleModelDetailView {
}