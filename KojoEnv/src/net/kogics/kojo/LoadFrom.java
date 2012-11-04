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

public final class LoadFrom implements ActionListener {
	private String ext = "kojo";

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Kojo Files", "kojo");
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        CodeEditorTopComponent cetc = CodeEditorTopComponent.findInstance();
        String loadDir = cetc.getLastLoadStoreDir();
        if (loadDir != null && loadDir != "") {
            File dir = new File(loadDir);
            if (dir.exists() && dir.isDirectory()) {
                chooser.setCurrentDirectory(dir);
            }
        }

        CodeExecutionSupport ces = (CodeExecutionSupport) CodeExecutionSupport.instance();
        try {
            ces.closeFileAndClrEditor();
            int returnVal = chooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
    			if (!selectedFile.getName().endsWith("." + ext)) {
    				selectedFile = new File(selectedFile.getAbsolutePath() + "."
    						+ ext);
    			}
                CodeEditorTopComponent.findInstance().setLastLoadStoreDir(selectedFile.getParent());
                ces.openFileWithoutClose(selectedFile);
            }
        } catch (RuntimeException ex) {
            // ignore user cancel
        }
    }
}
