/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.kogics.kojo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class SaveTo implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Kojo Files", "kojo");
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showSaveDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            CodeExecutionSupport ces = (CodeExecutionSupport) CodeExecutionSupport.instance();
            ces.saveTo(chooser.getSelectedFile());
        }
    }
}
