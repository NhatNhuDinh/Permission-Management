package com.company.permissionmanagement.view.user;

import com.company.permissionmanagement.entity.ExtResourceRoleModel;
import com.company.permissionmanagement.entity.User;
import com.company.permissionmanagement.extension.ExtRoleModelConverter;
import com.company.permissionmanagement.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.data.impl.BeforeCommitTransactionListener;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.security.model.BaseRoleModel;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentModel;
import io.jmix.security.role.assignment.RoleAssignmentPersistence;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityflowui.component.rolefilter.RoleFilterChangeEvent;
import org.aspectj.lang.annotation.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "users/:id", layout = MainView.class)
@ViewController(id = "User.detail")
@ViewDescriptor(path = "user-detail-view.xml")
@EditedEntityContainer("userDc")
public class UserDetailView extends StandardDetailView<User> {

    @ViewComponent
    private TypedTextField<String> usernameField;
    @ViewComponent
    private PasswordField passwordField;
    @ViewComponent
    private PasswordField confirmPasswordField;
    @ViewComponent
    private ComboBox<String> timeZoneField;
    @ViewComponent
    private MessageBundle messageBundle;
    @Autowired
    private Notifications notifications;

    @Autowired
    private EntityStates entityStates;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @ViewComponent
    private CollectionContainer<ExtResourceRoleModel> roleModelsDc;

    @Autowired
    private ResourceRoleRepository roleRepository;

    @Autowired
    private ExtRoleModelConverter extroleModelConverter;

    @ViewComponent
    private MultiSelectComboBox<ExtResourceRoleModel> resourceRolesSelectComboBox;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Metadata metadata;

    @Autowired
    private RoleAssignmentPersistence roleAssignmentPersistence;

    @Subscribe
    public void onInit(final InitEvent event) {
        timeZoneField.setItems(List.of(TimeZone.getAvailableIDs()));
    }

    @Subscribe
    public void onInitEntity(final InitEntityEvent<User> event) {
        usernameField.setReadOnly(false);
        passwordField.setVisible(true);
        confirmPasswordField.setVisible(true);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            usernameField.focus();
        }
        loadRoles(null);
    }

    @Subscribe
    public void onValidation(final ValidationEvent event) {
        if (entityStates.isNew(getEditedEntity())
                && !Objects.equals(passwordField.getValue(), confirmPasswordField.getValue())) {
            event.getErrors().add(messageBundle.getMessage("passwordsDoNotMatch"));
        }
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            getEditedEntity().setPassword(passwordEncoder.encode(passwordField.getValue()));

            notifications.create(messageBundle.getMessage("noAssignedRolesNotification"))
                    .withType(Notifications.Type.WARNING)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        }
    }

    private void loadRoles(@Nullable RoleFilterChangeEvent event) {
        List<ExtResourceRoleModel> items =
                roleRepository.getAllRoles().stream()
                        .filter(role -> event == null || event.matches(role))
                        .map(extroleModelConverter::createResourceRoleModel)
                        .filter(roleModel -> Boolean.TRUE.equals(roleModel.getForUser()))
                        .sorted(Comparator.comparing(BaseRoleModel::getName))
                        .toList();

        roleModelsDc.setItems(items);
    }

    protected RoleAssignmentPersistence getRoleAssignmentPersistence() {
        if (roleAssignmentPersistence == null) {
            throw new IllegalStateException("RoleAssignmentPersistence is not available");
        }
        return roleAssignmentPersistence;
    }

    @Subscribe
    public void onAfterSave(AfterSaveEvent event) {
        User user = getEditedEntity();
        Set<ExtResourceRoleModel> selectedRoles = resourceRolesSelectComboBox.getSelectedItems();

        // 1. Load tất cả các assignment hiện tại của user
        List<RoleAssignmentModel> existingAssignments = roleAssignmentPersistence
                .loadRoleAssignments(user.getUsername(), RoleAssignmentRoleType.RESOURCE);

        // 2. Chuẩn bị các assignment sẽ giữ lại
        List<RoleAssignmentModel> toSave = selectedRoles.stream()
                .map(role -> {
                    RoleAssignmentModel ra = metadata.create(RoleAssignmentModel.class);
                    ra.setUsername(user.getUsername());
                    ra.setRoleCode(role.getCode());
                    ra.setRoleType(RoleAssignmentRoleType.RESOURCE);
                    return ra;
                })
                .toList();

        // 3. Tìm các assignment cần remove
        Set<String> selectedCodes = selectedRoles.stream()
                .map(ExtResourceRoleModel::getCode)
                .collect(Collectors.toSet());
        List<RoleAssignmentModel> toRemove = existingAssignments.stream()
                .filter(ra -> !selectedCodes.contains(ra.getRoleCode()))
                .toList();

        roleAssignmentPersistence.save(toSave, toRemove);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        loadRoles(null);

        User user = getEditedEntity();

        List<RoleAssignmentModel> existingAssignments = roleAssignmentPersistence
                .loadRoleAssignments(user.getUsername(), RoleAssignmentRoleType.RESOURCE);

        Set<String> assignedRoleCodes = existingAssignments.stream()
                .map(RoleAssignmentModel::getRoleCode)
                .collect(Collectors.toSet());

        Set<ExtResourceRoleModel> selectedRoles = roleModelsDc.getItems().stream()
                .filter(role -> assignedRoleCodes.contains(role.getCode()))
                .collect(Collectors.toSet());

        resourceRolesSelectComboBox.setValue(selectedRoles);
    }


}