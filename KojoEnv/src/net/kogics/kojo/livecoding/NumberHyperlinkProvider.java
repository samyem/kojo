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
public class NumberHyperlinkProvider implements HyperlinkProvider {

    public static final Pattern MY_SPECIAL_PATTERN =
            Pattern.compile("(\\d*)");
    private String target;
    private int targetStart;
    private int targetEnd;
    Popup popup = null;

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset) {
        if (popup != null) {
            popup.hide();
        }
        return verifyState(doc, offset);
    }

    public boolean verifyState(Document doc, int offset) {
        try {

            TokenHierarchy hi = TokenHierarchy.get(doc);
            TokenSequence<ScalaTokenId.V> ts = hi.tokenSequence(ScalaTokenId.language());
//            System.out.println("***\nToken Seq: " + ts);
            if (ts != null) {
                ts.move(offset);
                ts.moveNext();
                Token<ScalaTokenId.V> tok = ts.token();
                // TODO just check token type instead of doing a regex match
                int newOffset = ts.offset();
                String matcherText = tok.text().toString();
                Matcher m = MY_SPECIAL_PATTERN.matcher(matcherText);
                if (m.matches()) {
                    target = m.group(1);
                    int idx = matcherText.indexOf(target);
                    targetStart = newOffset + idx;
                    targetEnd = targetStart + target.length();
                    return true;
                }
            }
            return false;
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            t.printStackTrace();
            return false;
        }
    }

    @Override
    public int[] getHyperlinkSpan(Document document, int offset) {
        if (verifyState(document, offset)) {
            return new int[]{targetStart, targetEnd};
        } else {
            return null;
        }
    }

    @Override
    public void performClickAction(Document document, int offset) {
        if (verifyState(document, offset)) {
            final CodeExecutionSupport ces = (CodeExecutionSupport) CodeExecutionSupport.instance();
            ces.handleNumberHyperclick(document, offset, target, targetStart);
        }
    }
}
