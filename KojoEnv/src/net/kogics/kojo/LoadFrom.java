/*
 * Copyright (C) 2009-2011 Lalit Pant <pant.lalit@gmail.com>
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
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                CodeEditorTopComponent.findInstance().setLastLoadStoreDir(selectedFile.getParent());
                ces.closeFileAndClrEditor();
                ces.openFileWithoutClose(selectedFile);
            }
        } catch (RuntimeException ex) {
            // ignore user cancel
        } catch (Throwable t) {
            System.out.println(String.format("Unable to open file: %s", t.getMessage()));
        }
    }
}
