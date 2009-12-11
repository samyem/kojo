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
import java.awt.Frame;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultEditorKit;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.UndoRedo;

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
    private UndoRedo.Manager manager = new UndoRedo.Manager();

    public CodeEditorTopComponent() {
        initComponents();

        CodeEditor ce = (CodeEditor) CodeEditor.instance();
        ce.setPreferredSize(this.getPreferredSize());

        ce.codePane().getDocument().addUndoableEditListener(manager);

        ActionMap actionMap = getActionMap();
        final Action copyAction = new DefaultEditorKit.CopyAction();
        final Action cutAction = new DefaultEditorKit.CutAction();
        final Action pasteAction = new DefaultEditorKit.PasteAction();

        cutAction.setEnabled(false);
        copyAction.setEnabled(false);

        actionMap.put(DefaultEditorKit.copyAction, copyAction);
        actionMap.put(DefaultEditorKit.cutAction, cutAction);
        actionMap.put(DefaultEditorKit.pasteAction, pasteAction);

        // For code pane window, enable only copy and cut buttons
        // when text is selected
        ce.codePane().addCaretListener(new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                int dot = e.getDot();
                int mark = e.getMark();
                if (dot == mark) {  // no selection
                    cutAction.setEnabled(false);
                    copyAction.setEnabled(false);
                } else {
                    cutAction.setEnabled(true);
                    copyAction.setEnabled(true);
                }
            }
        });

        // For output window, enable only copy (and not cut) button
        // when text is selected
        ce.output().addCaretListener(new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                int dot = e.getDot();
                int mark = e.getMark();
                if (dot == mark) {  // no selection
                    cutAction.setEnabled(false);
                    copyAction.setEnabled(false);
                } else {
                    cutAction.setEnabled(false);
                    copyAction.setEnabled(true);
                }
            }
        });

        ce.codePane().addFocusListener(new FocusAdapter() {

            public void focusGained(FocusEvent e) {
                pasteAction.setEnabled(true);
                tweakSourceMenuEtc(true);
            }
        });

        ce.output().addFocusListener(new FocusAdapter() {

            public void focusGained(FocusEvent e) {
                pasteAction.setEnabled(false);
                tweakSourceMenuEtc(false);
            }
        });

        add(ce, BorderLayout.CENTER);

        setName(NbBundle.getMessage(CodeEditorTopComponent.class,
                "CTL_CodeEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(CodeEditorTopComponent.class, "HINT_CodeEditorTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);
    }

    private void tweakSourceMenuEtc(boolean enable) {
        try {
            JMenu sourceMenu = findMenu("Source");
            if (sourceMenu != null) {
                int n = sourceMenu.getItemCount();
                for (int i = 0; i < n; i++) {
                    sourceMenu.getItem(i).setEnabled(enable);
                }
            }

            JMenu editMenu = findMenu("Edit");
            if (editMenu != null) {
                int n = editMenu.getItemCount();
                for (int i = 0; i < n; i++) {
                    JMenuItem menuItem = editMenu.getItem(i);
                    if (menuItem == null) {
                        continue;
                    }

                    String itemText = menuItem.getText();
                    if (itemText != null && itemText.equals("Select All")) {
                        menuItem.setEnabled(enable);
                        break;
                    }
                }

            }
        } catch (Throwable t) {
            t.printStackTrace();
            // ignore
        }
    }

    private JMenu findMenu(String menuToFind) {
        Frame frame = WindowManager.getDefault().getMainWindow();
        JMenuBar menuBar = ((JFrame) frame).getRootPane().getJMenuBar();
        int n = menuBar.getMenuCount();
        for (int i = 0; i < n; i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu == null) {
                continue;
            }

            String menuText = menu.getText();
            if (menuText != null && menuText.equals(menuToFind)) {
                return menu;
            }
        }
        return null;
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
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        requestActive();
    }

    @Override
    public UndoRedo getUndoRedo() {
        return manager;
    }

    @Override
    protected void componentActivated() {
        CodeEditor ce = (CodeEditor) CodeEditor.instance();
        boolean success = ce.codePane().requestFocusInWindow();
        if (!success) {
            ce.codePane().scheduleFocusRequest();
        }

        super.componentActivated();
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
}
