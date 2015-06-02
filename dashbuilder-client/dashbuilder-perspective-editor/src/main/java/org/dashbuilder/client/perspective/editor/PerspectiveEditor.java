package org.dashbuilder.client.perspective.editor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.dashbuilder.client.perspective.editor.events.PerspectiveCreatedEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveDeletedEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOffEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveEditOnEvent;
import org.dashbuilder.client.perspective.editor.events.PerspectiveRenamedEvent;
import org.dashbuilder.client.workbench.panels.impl.MultiListWorkbenchPanelPresenterExt;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManagerImpl;
import org.uberfire.client.mvp.Activity;
import org.uberfire.client.mvp.ActivityBeansCache;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.PanelManager;
import org.uberfire.client.workbench.events.ChangeTitleWidgetEvent;
import org.uberfire.client.workbench.panels.WorkbenchPanelPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.ForcedPlaceRequest;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;

import static org.jboss.errai.ioc.client.QualifierUtil.DEFAULT_QUALIFIERS;

@ApplicationScoped
public class PerspectiveEditor {

    @Inject
    protected PlaceManager placeManager;

    @Inject
    protected PanelManager panelManager;

    @Inject
    protected PerspectiveManager perspectiveManager;

    @Inject
    protected ActivityBeansCache activityBeansCache;

    @Inject
    protected Event<ChangeTitleWidgetEvent> changeTitleEvent;

    @Inject
    protected Event<PerspectiveCreatedEvent> perspectiveCreatedEvent;

    @Inject
    protected Event<PerspectiveDeletedEvent> perspectiveDeletedEvent;

    @Inject
    protected Event<PerspectiveRenamedEvent> perspectiveRenamedEvent;

    @Inject
    protected Event<PerspectiveEditOnEvent> perspectiveEditOnEvent;

    @Inject
    protected Event<PerspectiveEditOffEvent> perspectiveEditOffEvent;

    protected boolean editOn = false;
    protected Set<String> allowedRoles = new HashSet<String>();

    @AfterInitialization
    protected void init() {
        perspectiveManager.loadPerspectiveStates(new ParameterizedCommand<Set<PerspectiveDefinition>>() {
            public void execute(Set<PerspectiveDefinition> list) {
                for (PerspectiveDefinition p : list) {
                    String id = p.getName();
                    if (id.startsWith(EditablePerspectiveActivity.CLASSIFIER)) {
                        registerEditablePerspective(id);
                    }
                }
            }
        });
    }

    public boolean isEditOn() {
        return editOn;
    }

    public void editOn() {
        if (isEditable() && !editOn) {
            this.editOn = true;
            perspectiveEditOnEvent.fire(new PerspectiveEditOnEvent());
        }
    }

    public void editOff() {
        if (isEditable() && editOn) {
            this.editOn = false;
            perspectiveEditOffEvent.fire(new PerspectiveEditOffEvent());
        }
    }

    public Set<String> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(String... roles) {
        this.allowedRoles.clear();
        for (String role : roles) {
            allowedRoles.add(role);
        }
    }

    public boolean isEditable() {
        return perspectiveManager.getCurrentPerspective() instanceof EditablePerspectiveActivity;
    }

    public EditablePerspectiveActivity newEditablePerspective(String perspectiveName) {
        final EditablePerspectiveActivity activity = registerEditablePerspective(EditablePerspectiveActivity.CLASSIFIER + perspectiveName);
        placeManager.goTo(activity.getIdentifier());
        perspectiveManager.savePerspectiveState(new Command() {
            public void execute() {
                perspectiveCreatedEvent.fire(new PerspectiveCreatedEvent(activity.getIdentifier(), activity.getDisplayName()));
            }
        });
        return activity;
    }

    public String getPerspectiveName() {
        PerspectiveActivity perspectiveActivity = perspectiveManager.getCurrentPerspective();
        if (perspectiveActivity instanceof  EditablePerspectiveActivity) {
            return ((EditablePerspectiveActivity) perspectiveActivity).getDisplayName();
        }
        return perspectiveActivity.getIdentifier();
    }

    public void changePerspectiveName(final String name) {
        // Renames implies deleting the current perspective and registering a new one
        final String oldId = getCurrentPerspective().getIdentifier();
        final String newId = EditablePerspectiveActivity.CLASSIFIER + name;
        perspectiveManager.removePerspectiveState(oldId, new Command() {
            public void execute() {
                activityBeansCache.removeActivity(oldId);
                perspectiveManager.getLivePerspectiveDefinition().setName(newId);
                perspectiveManager.savePerspectiveState(new Command() {
                    public void execute() {
                        registerEditablePerspective(newId);
                        perspectiveRenamedEvent.fire(new PerspectiveRenamedEvent(oldId, newId, name));
                        placeManager.goTo(newId);
                    }
                });
            }
        });
    }

    public void changePanelType(WorkbenchPanelPresenter panelPresenter, String newType) {
        panelPresenter.getDefinition().setPanelType(newType);
        PerspectiveActivity currentPerspective = perspectiveManager.getCurrentPerspective();
        saveCurrentPerspective();
        placeManager.goTo(new ForcedPlaceRequest(currentPerspective.getIdentifier()));
    }

    public void changePlaceTitle(PlaceRequest placeRequest, String newTitle) {
        changeTitleEvent.fire(new ChangeTitleWidgetEvent(placeRequest, newTitle));
    }

    public void openPlace(PlaceRequest placeRequest) {
        placeManager.goTo(placeRequest);
    }

    public void updatePlace(PlaceRequest placeRequest, PlaceRequest newPlaceRequest) {
        PanelDefinition panelDefinition = panelManager.getPanelForPlace(placeRequest);
        placeManager.goTo(newPlaceRequest, panelDefinition);
        placeManager.closePlace(placeRequest);
    }

    protected EditablePerspectiveActivity registerEditablePerspective(String id) {
        EditablePerspectiveActivity activity = new EditablePerspectiveActivity(id);

        SyncBeanManagerImpl beanManager = (SyncBeanManagerImpl) IOC.getBeanManager();
        beanManager.addBean((Class) PerspectiveActivity.class, EditablePerspectiveActivity.class, null, activity, DEFAULT_QUALIFIERS, id, true, null);
        activityBeansCache.addNewPerspectiveActivity(beanManager.lookupBeans(id).iterator().next());
        return activity;
    }

    public EditablePerspectiveActivity getEditablePerspectiveById(String id) {
        for (EditablePerspectiveActivity p : getEditablePerspectives()) {
            if (p.getIdentifier().equals(id)) return p;
        }
        return null;
    }

    public EditablePerspectiveActivity getEditablePerspectiveByName(String name) {
        for (EditablePerspectiveActivity p : getEditablePerspectives()) {
            if (p.getDisplayName().equals(name)) return p;
        }
        return null;
    }

    public Set<EditablePerspectiveActivity> getEditablePerspectives() {
        Set<EditablePerspectiveActivity> activities = new HashSet<EditablePerspectiveActivity>();
        for (String activityId : activityBeansCache.getActivitiesById()) {

            IOCBeanDef<Activity> activityDef = activityBeansCache.getActivity(activityId);
            if (activityDef.getBeanClass().equals(EditablePerspectiveActivity.class)) {
                activities.add((EditablePerspectiveActivity) activityDef.getInstance());
            }
        }
        return activities;
    }

    public PerspectiveActivity getCurrentPerspective() {
        return perspectiveManager.getCurrentPerspective();
    }

    public PerspectiveActivity getDefaultPerspective() {
        PerspectiveActivity first = null;
        SyncBeanManagerImpl beanManager = (SyncBeanManagerImpl) IOC.getBeanManager();
        Collection<IOCBeanDef<PerspectiveActivity>> perspectives = beanManager.lookupBeans(PerspectiveActivity.class);
        Iterator<IOCBeanDef<PerspectiveActivity>> perspectivesIterator = perspectives.iterator();
        while (perspectivesIterator.hasNext() ) {

            IOCBeanDef<PerspectiveActivity> perspective = perspectivesIterator.next();
            PerspectiveActivity instance = perspective.getInstance();

            if (instance.isDefault()) {
                return instance;
            }
            if (first == null) {
                first = instance;
            }
        }
        return first;
    }

    public void deleteCurrentPerspective() {
        final EditablePerspectiveActivity activity = (EditablePerspectiveActivity) getCurrentPerspective();
        final String id = activity.getIdentifier();

        perspectiveManager.removePerspectiveState(id, new Command() {
            public void execute() {
                activityBeansCache.removeActivity(id);
                perspectiveDeletedEvent.fire(new PerspectiveDeletedEvent(activity.getIdentifier(), activity.getDisplayName()));
                placeManager.goTo(getDefaultPerspective().getIdentifier());
            }
        });
    }

    public void saveCurrentPerspective() {
        perspectiveManager.savePerspectiveState(new Command() {
            public void execute() {

            }
        });
    }
}
