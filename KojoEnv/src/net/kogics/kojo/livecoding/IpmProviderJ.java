/*
 * Copyright (C) 2012 Lalit Pant <pant.lalit@gmail.com>
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
package net.kogics.kojo.livecoding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Popup;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.scala.core.lexer.ScalaTokenId;

import net.kogics.kojo.CodeExecutionSupport;

/**
 *
 * @author lalit
 */
@MimeRegistration(mimeType = "text/x-scala", service = HyperlinkProvider.class)
public class IpmProviderJ implements HyperlinkProvider {
    
    IpmProvider provider = new IpmProvider();

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset) {
        return provider.isHyperlinkPoint(doc, offset);
//        if (popup != null) {
//            popup.hide();
//        }
//        return verifyState(doc, offset);
    }

//    @SuppressWarnings("unchecked")
//    public boolean verifyState(Document doc, int offset) {
//        try {
//            TokenHierarchy hi = TokenHierarchy.get(doc);
//            TokenSequence<ScalaTokenId.V> ts = hi.tokenSequence(ScalaTokenId.language());
////            System.out.println("***\nToken Seq: " + ts);
//            if (ts != null) {
//                ts.move(offset);
//                ts.moveNext();
//                Token<ScalaTokenId.V> tok = ts.token();
//                // TODO just check token type instead of doing a regex match
//                int newOffset = ts.offset();
//                String matcherText = tok.text().toString();
//                Matcher m = MY_SPECIAL_PATTERN.matcher(matcherText);
//                if (m.matches()) {
//                    target = m.group(1);
//                    int idx = matcherText.indexOf(target);
//                    targetStart = newOffset + idx;
//                    targetEnd = targetStart + target.length();
//                    return true;
//                }
//            }
//            return false;
//        } catch (Throwable t) {
//            System.out.println(t.getMessage());
//            t.printStackTrace();
//            return false;
//        }
//    }

    @Override
    public int[] getHyperlinkSpan(Document document, int offset) {
        return provider.getHyperlinkSpan(document, offset);
//        if (verifyState(document, offset)) {
//            return new int[]{targetStart, targetEnd};
//        } else {
//            return null;
//        }
    }

    @Override
    public void performClickAction(Document document, int offset) {
        provider.performClickAction(document, offset);
//        if (verifyState(document, offset)) {
//            final CodeExecutionSupport ces = (CodeExecutionSupport) CodeExecutionSupport.instance();
//            InteractiveManipulator imanip = new IntManipulator(ces);
//            imanip.activate(document, offset, target, targetStart);
//        }
    }
}
