package com.company.permissionmanagement.view.resourcerolemodellist;

import com.company.permissionmanagement.extension.ExtRoleModelConverter;
import com.company.permissionmanagement.entity.ExtResourceRoleModel;
import com.company.permissionmanagement.extension.ExtDatabaseRolePersistence;
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
    private CollectionContainer<ExtResourceRoleModel> roleModelsDc;

    @Autowired
    private ResourceRoleRepository roleRepository;

    @Autowired
    private ExtRoleModelConverter extroleModelConverter;

    @Autowired
    private ExtDatabaseRolePersistence extDatabaseRolePersistence;

    @ViewComponent
    private DataGrid<ExtResourceRoleModel> roleModelsTable;

    @Autowired
    private Dialogs dialogs;

    @Override
    @Subscribe
    public void onBeforeShow(View.BeforeShowEvent event) {
        this.loadRoles((RoleFilterChangeEvent) null);
    }

    private void loadRoles(@Nullable RoleFilterChangeEvent event) {
        List<ExtResourceRoleModel> items =
                roleRepository.getAllRoles().stream()
                        .filter(role -> event == null || event.matches(role))
                        .map(extroleModelConverter::createResourceRoleModel)
                        .sorted(Comparator.comparing(BaseRoleModel::getName))
                        .toList();

        roleModelsDc.setItems(items);
    }

    @Subscribe("roleModelsTable.remove")
    public void onRoleModelsTableRemove(ActionPerformedEvent event) {
        List<ExtResourceRoleModel> selectedRoles = roleModelsTable.getSelectedItems().stream().toList();
        if (!selectedRoles.isEmpty()) {
            dialogs.createOptionDialog()
                    .withHeader("Confirm delete")
                    .withText("Are you sure you want to delete selected roles?")
                    .withActions(
                            new DialogAction(DialogAction.Type.YES).withHandler(e -> {
                                extDatabaseRolePersistence.removeRoles(selectedRoles);
                                loadRoles(null);
                            }),
                            new DialogAction(DialogAction.Type.NO)
                    )
                    .open();
        }
    }

}