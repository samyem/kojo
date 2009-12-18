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
package net.kogics.kojo;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultEditorKit;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.awt.UndoRedo;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//net.kogics.kojo//CodeEditor//EN",
autostore = false)
public final class CodeEditorTopComponent extends TopComponent {

    private static CodeEditorTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "net/kogics/kojo/code-editor.png";
    private static final String PREFERRED_ID = "CodeEditorTopComponent";
    JPopupMenu popupMenu;

    public CodeEditorTopComponent() {
        initComponents();

        CodeEditor ce = (CodeEditor) CodeEditor.instance();
        ce.setPreferredSize(this.getPreferredSize());

        tweakActions(ce);
        installPopup(ce);

        add(ce, BorderLayout.CENTER);
        setName(NbBundle.getMessage(CodeEditorTopComponent.class,
                "CTL_CodeEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(CodeEditorTopComponent.class, "HINT_CodeEditorTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);
    }

    private void installPopup(CodeEditor ce) {
        ce.codePane().addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    showPopup(evt);
                }
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    showPopup(evt);
                }
            }
        });
    }

    private void tweakActions(CodeEditor ce) {

        ActionMap actionMap = getActionMap();

        // Cut/Copy/Paste
        final Action copyAction = new DefaultEditorKit.CopyAction();
        final Action cutAction = new DefaultEditorKit.CutAction();
        final Action pasteAction = new DefaultEditorKit.PasteAction();

        cutAction.setEnabled(false);
        copyAction.setEnabled(false);
        pasteAction.setEnabled(true);

        actionMap.put(DefaultEditorKit.copyAction, copyAction);
        actionMap.put(DefaultEditorKit.cutAction, cutAction);
        actionMap.put(DefaultEditorKit.pasteAction, pasteAction);

        ce.codePane().addCaretListener(new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                int dot = e.getDot();
                int mark = e.getMark();
                if (dot == mark) {
                    // no selection
                    cutAction.setEnabled(false);
                    copyAction.setEnabled(false);
                } else {
                    cutAction.setEnabled(true);
                    copyAction.setEnabled(true);
                }
            }
        });


        // Find/Replace
        Object findKey = SystemAction.get(org.openide.actions.FindAction.class).getActionMapKey();
        Action findAction = new FindAction();
        // link Find Menu item to Find action
        actionMap.put(findKey, findAction);
        // Enable shortcut
        KeyStroke ctrlF = Utilities.stringToKey("D-F"); // tight coupling with layer shortcut entry here. Bad!
        ce.codePane().getInputMap().put(ctrlF, findKey);
        ce.codePane().getActionMap().put(findKey, findAction);

        Object replaceKey = SystemAction.get(org.openide.actions.ReplaceAction.class).getActionMapKey();
        Action replaceAction = new ReplaceAction();
        actionMap.put(replaceKey, replaceAction);
        KeyStroke ctrlR = Utilities.stringToKey("D-R"); // tight coupling with layer shortcut entry here. Bad!
        ce.codePane().getInputMap().put(ctrlR, replaceKey);
        ce.codePane().getActionMap().put(replaceKey, replaceAction);

        // Make Keyboard and Menu Undo/Redo actions the same
        // so that things stay in sync irrespective of whether the user uses
        // shortcuts or menu items
        org.openide.actions.UndoAction undoAction = SystemAction.get(org.openide.actions.UndoAction.class);
        KeyStroke ctrlZ = Utilities.stringToKey("D-Z"); // tight coupling with layer shortcut entry here. Bad!
        ce.codePane().getInputMap().put(ctrlZ, "Undo");
        ce.codePane().getActionMap().put("Undo", undoAction);

        org.openide.actions.RedoAction redoAction = SystemAction.get(org.openide.actions.RedoAction.class);
        KeyStroke ctrlY = Utilities.stringToKey("D-Y"); // tight coupling with layer shortcut entry here. Bad!
        ce.codePane().getInputMap().put(ctrlY, "Redo");
        ce.codePane().getActionMap().put("Redo", redoAction);
    }

    private void showPopup(MouseEvent evt) {
        CodeEditor ce = (CodeEditor) CodeEditor.instance();
        if (popupMenu == null) {
            popupMenu = new CodeEditorPopupMenu();
        }

        popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized CodeEditorTopComponent getDefault() {
        if (instance == null) {
            instance = new CodeEditorTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the CodeEditorTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized CodeEditorTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(CodeEditorTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof CodeEditorTopComponent) {
            return (CodeEditorTopComponent) win;
        }
        Logger.getLogger(CodeEditorTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        requestActive();
    }

    @Override
    public UndoRedo getUndoRedo() {
        CodeEditor ce = (CodeEditor) CodeEditor.instance();
        return ce.undoRedoManager();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        CodeEditor ce = (CodeEditor) CodeEditor.instance();
        boolean success = ce.codePane().requestFocusInWindow();
        if (!success) {
            ce.codePane().scheduleFocusRequest();
        }
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    class CodeEditorPopupMenu extends JPopupMenu {

        public CodeEditorPopupMenu() {
            FileObject configRoot = FileUtil.getConfigRoot();
            buildPopupMenu(configRoot, "Menu/Edit", "Edit");
            buildPopupMenu(configRoot, "Menu/Source", "Source");
        }

        private void buildPopupMenu(FileObject configRoot, String folderName, String menuName) {
            FileObject fo = configRoot.getFileObject(folderName);
            if (fo == null) {
                return;
            }
            JMenu menu = new JMenu(menuName);
            buildPopupMenu(fo, menu);
            add(menu);
        }

        private void buildPopupMenu(FileObject fo, JComponent comp) {

            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] childs = df.getChildren();
            DataObject dob;
            Object instanceObj;

            for (int i = 0; i < childs.length; i++) {
                dob = childs[i];
                if (dob.getPrimaryFile().isFolder()) {
                    FileObject childFo = childs[i].getPrimaryFile();
                    JMenu menu = new JMenu();
                    Mnemonics.setLocalizedText(menu, dob.getNodeDelegate().getDisplayName());
                    comp.add(menu);
                    buildPopupMenu(childFo, menu);
                } else {
                    //Cookie or Lookup API discovery:
                    InstanceCookie ck = dob.getCookie(InstanceCookie.class);
                    try {
                        instanceObj = ck.instanceCreate();
                    } catch (Exception ex) {
                        instanceObj = null;
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                    }
                    if (instanceObj == null) {
                        continue;
                    }
                    if (instanceObj instanceof JSeparator) {
                        comp.add((JSeparator) instanceObj);
                    } else if (instanceObj instanceof BooleanStateAction) {
                        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem();
                        Actions.connect(menuItem, (BooleanStateAction) instanceObj, true);
                        comp.add(menuItem);
                    } else if (instanceObj instanceof Action) {
                        JMenuItem menuItem = new JMenuItem();
                        Actions.connect(menuItem, (Action) instanceObj, true);
                        comp.add(menuItem);
                    } else if (instanceObj instanceof Presenter.Menu) {
                        Action action = ((Presenter.Menu) instanceObj).getMenuPresenter().getAction();
                        if (action != null) {
                            JMenuItem menuItem = new JMenuItem();
                            Actions.connect(menuItem, action, true);
                            comp.add(menuItem);
                        }
                    }
                }
            }
        }
    }
}
