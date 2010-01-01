/*
 * Copyright (C) 2009 Lalit Pant <pant.lalit@gmail.com>
 *
 * The contents of this file are subject to the GNU General Public License
 * Version 3 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.gnu.org/copyleft/gpl.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 */

package net.kogics.kojo.inspect;

import java.awt.BorderLayout;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.Node.Property;
import org.openide.windows.TopComponent;

public class InspectorTopComponent extends TopComponent
        implements ExplorerManager.Provider {

    private final ExplorerManager manager = new ExplorerManager();

    public InspectorTopComponent() {
//        ActionMap map = this.getActionMap();
//        map.put(DefaultEditorKit.copyAction,
//                ExplorerUtils.actionCopy(manager));
//
//        associateLookup(ExplorerUtils.createLookup(manager, map));

        setDisplayName("Object Inspector");
        setLayout(new BorderLayout());
        TreeTableView view = new TreeTableView();

        // specify properties that we want to see in Table
        Property p1 = new TableColumnProperty("type");
        Property p2 = new TableColumnProperty("id");
        Property p3 = new TableColumnProperty("value");

        view.setProperties(new Property[]{p1, p2, p3});

//        view.setRootVisible(false);
        add(view, BorderLayout.CENTER);
    }

    public void inspectObject(Object obj) {
        setDisplayName("Object Inspector - " + obj.getClass().getName());
        manager.setRootContext(new ObjectInspectorNode("Inspected Object", obj));
//        manager.setRootContext(new AbstractNode(new InspectorChildren(obj)));
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

//    @Override
//    protected void componentActivated() {
//        ExplorerUtils.activateActions(manager, true);
//    }
//
//    @Override
//    protected void componentDeactivated() {
//        ExplorerUtils.activateActions(manager, false);
//    }
}

