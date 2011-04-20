/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.kogics.kojo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class SaveTo implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        CodeEditorTopComponent cetc = CodeEditorTopComponent.findInstance();
        CodeExecutionSupport ces = (CodeExecutionSupport) CodeExecutionSupport.instance();

        if (ces.hasOpenFile()) {
            ces.saveFile();
        } else {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Kojo Files", "kojo");
            chooser.setFileFilter(filter);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            String loadDir = cetc.getLastLoadStoreDir();
            if (loadDir != null && loadDir != "") {
                File dir = new File(loadDir);
                if (dir.exists() && dir.isDirectory()) {
                    chooser.setCurrentDirectory(dir);
                }
            }

            int returnVal = chooser.showSaveDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                cetc.setLastLoadStoreDir(chooser.getSelectedFile().getParent());
                ces.saveTo(chooser.getSelectedFile());
                ces.openFile(chooser.getSelectedFile());
            }
        }
    }
}
