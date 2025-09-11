package com.company.permissionmanagement.view.resourcerolemodellist;

import com.company.permissionmanagement.converter.ExtendRoleModelConverter;
import com.company.permissionmanagement.entity.ExtendResourceRoleModel;
import com.company.permissionmanagement.persistence.ExtendDatabaseRolePersistence;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
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

    @Autowired
    private ExtendDatabaseRolePersistence extendDatabaseRolePersistence;

    @ViewComponent
    private DataGrid<ExtendResourceRoleModel> roleModelsTable;

    @Autowired
    private Dialogs dialogs;

    @Override
    @Subscribe
    public void onBeforeShow(View.BeforeShowEvent event) {
        this.loadRoles((RoleFilterChangeEvent) null);
    }

    private void loadRoles(@Nullable RoleFilterChangeEvent event) {
        List<ExtendResourceRoleModel> items =
                roleRepository.getAllRoles().stream()
                        .filter(role -> event == null || event.matches(role))
                        .map(extroleModelConverter::createExtResourceRoleModel)
                        .sorted(Comparator.comparing(BaseRoleModel::getName))
                        .toList();

        roleModelsDc.setItems(items);
    }

    @Subscribe("roleModelsTable.remove")
    public void onRoleModelsTableRemove(ActionPerformedEvent event) {
        List<ExtendResourceRoleModel> selectedRoles = roleModelsTable.getSelectedItems().stream().toList();
        if (!selectedRoles.isEmpty()) {
            dialogs.createOptionDialog()
                    .withHeader("Confirm delete")
                    .withText("Are you sure you want to delete selected roles?")
                    .withActions(
                            new DialogAction(DialogAction.Type.YES).withHandler(e -> {
                                extendDatabaseRolePersistence.removeRoles(selectedRoles);
                                loadRoles(null);
                            }),
                            new DialogAction(DialogAction.Type.NO)
                    )
                    .open();
        }
    }

}