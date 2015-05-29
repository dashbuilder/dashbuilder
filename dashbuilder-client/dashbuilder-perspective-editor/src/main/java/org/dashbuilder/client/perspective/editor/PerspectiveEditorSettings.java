package org.dashbuilder.client.perspective.editor;

import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PerspectiveEditorSettings {

    protected boolean editOn = false;
    protected Set<String> editAllowedRoles = new HashSet<String>();

    public boolean isEditOn() {
        return editOn;
    }

    public void setEditOn(boolean editOn) {
        this.editOn = editOn;
    }

    public Set<String> getEditAllowedRoles() {
        return editAllowedRoles;
    }

    public void setEditAllowedRoles(String... roles) {
        this.editAllowedRoles.clear();
        for (String role : roles) {
            editAllowedRoles.add(role);
        }
    }
}
