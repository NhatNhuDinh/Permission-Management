package com.company.permissionmanagement.view.resourcerolemodellist;

import com.company.permissionmanagement.converter.ExtendRoleModelConverter;
import com.company.permissionmanagement.entity.ExtendResourceRoleModel;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.action.list.CreateAction;
import io.jmix.flowui.action.list.EditAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.security.model.BaseRoleModel;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securityflowui.component.rolefilter.RoleFilterChangeEvent;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Comparator;
import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getView;

@Route(value = "sec/resourcerolemodels-ext", layout = DefaultMainViewParent.class)
@ViewController(id = "sec_ResourceRoleModel.list")
@ViewDescriptor(path = "ext-resource-role-model-list-view.xml")
public class ExtResourceRoleModelListView extends ResourceRoleModelListView {

    @ViewComponent
    private CollectionContainer<ExtendResourceRoleModel> roleModelsDc;

    @Autowired
    private ResourceRoleRepository roleRepository;

    @Autowired
    private ExtendRoleModelConverter extroleModelConverter;

    @Override
    @Subscribe
    public void onBeforeShow(View.BeforeShowEvent event) {
        this.loadRoles((RoleFilterChangeEvent) null);
    }

    private void loadRoles(@Nullable RoleFilterChangeEvent event) {
        List<ExtendResourceRoleModel> items =
                roleRepository.getAllRoles().stream()
                        .filter(role -> event == null || event.matches(role))
                        .map(extroleModelConverter::createResourceRoleModel)
                        .map(m -> (ExtendResourceRoleModel) m)
                        .sorted(Comparator.comparing(BaseRoleModel::getName))
                        .toList();

        roleModelsDc.setItems(items);
    }

}