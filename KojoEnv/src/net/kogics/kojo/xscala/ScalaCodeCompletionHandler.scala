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
package net.kogics.kojo
package xscala

import javax.swing._
import javax.swing.text.{BadLocationException, JTextComponent}
import java.util.logging._


import org.netbeans.editor.{BaseDocument, Utilities}
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager
import org.netbeans.modules.csl.api._
import CodeCompletionHandler._
import org.netbeans.modules.csl.spi.{DefaultCompletionResult, ParserResult}
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport
import org.openide.util.{Exceptions, NbBundle, ImageUtilities}
import org.openide.filesystems.FileObject

import net.kogics.kojo.util._
import net.kogics.kojo.core.CodeCompletionSupport

// import org.netbeans.api.language.util.ast.AstElementHandle


class ScalaCodeCompletionHandler(completionSupport: CodeCompletionSupport) extends CodeCompletionHandler {
  val Log = Logger.getLogger(getClass.getName);

  class ScalaElementHandle(name: String, offset: Int, kind: ElementKind) extends ElementHandle {
    def getFileObject: FileObject = null
    def getMimeType: String = org.netbeans.modules.scala.core.ScalaMimeResolver.MIME_TYPE
    def getName: String = name
    def getIn: String = null
    def getKind: ElementKind = kind
    def getModifiers: java.util.Set[Modifier] = java.util.Collections.emptySet[Modifier]
    def signatureEquals(handle: ElementHandle): Boolean = false;
    def getOffsetRange(result: ParserResult): OffsetRange = new OffsetRange(0,offset)
  }

  class ScalaCompletionProposal(offset: Int, proposal: String, kind: ElementKind, 
                                template: String = null,
                                icon: ImageIcon = null) extends CompletionProposal {
    val elemHandle = new ScalaElementHandle(proposal, offset, kind)
    def getAnchorOffset: Int = offset
    def getName: String = proposal
    def getInsertPrefix: String = proposal
    def getSortText: String = proposal
    def getSortPrioOverride: Int = 0
    def getElement: ElementHandle = elemHandle
    def getKind: ElementKind = kind
    def getIcon: ImageIcon = icon
    def getLhsHtml(fm: HtmlFormatter): String = {
      val kind = getKind
      fm.name(kind, true)
      fm.appendText(proposal)
      fm.name(kind, false)
      fm.getText
    }
    def getRhsHtml(fm: HtmlFormatter): String = ""
    def getModifiers: java.util.Set[Modifier] = elemHandle.getModifiers
    override def toString: String = "Proposal(%s, %s)" format(proposal, template)
    def isSmart: Boolean = false
    def getCustomInsertTemplate: String = template
  }

  import core.CompletionInfo
  class ScalaElementHandle2(name: String, offset: Int, kind: ElementKind, val proposal: CompletionInfo) extends ElementHandle {
    def getFileObject: FileObject = null
    def getMimeType: String = org.netbeans.modules.scala.core.ScalaMimeResolver.MIME_TYPE
    def getName: String = name
    def getIn: String = null
    def getKind: ElementKind = kind
    def getModifiers: java.util.Set[Modifier] = java.util.Collections.emptySet[Modifier]
    def signatureEquals(handle: ElementHandle): Boolean = false;
    def getOffsetRange(result: ParserResult): OffsetRange = new OffsetRange(0,offset)
  }

  class ScalaCompletionProposal2(offset: Int, proposal: CompletionInfo, kind: ElementKind, 
                                 icon: ImageIcon = null) extends CompletionProposal {
    val elemHandle = new ScalaElementHandle2(proposal.name, offset, kind, proposal)
    def getAnchorOffset: Int = offset
    def getName: String = proposal.name
    def getInsertPrefix: String = proposal.name
    def getSortText: String = proposal.name
    def getSortPrioOverride: Int = proposal.prio
    def getElement: ElementHandle = elemHandle
    def getKind: ElementKind = kind
    def getIcon: ImageIcon = icon
    val valOrNoargFunc = proposal.value || proposal.params.size == 0 && proposal.ret != "Unit"
    def getLhsHtml(fm: HtmlFormatter): String = {
      val kind = getKind
      fm.name(kind, true)
      fm.appendText(proposal.name)
      fm.name(kind, false)
      if (!valOrNoargFunc) {
        fm.parameters(true)
        fm.appendText(proposal.params.zip(proposal.paramTypes).
                      map{ p => "%s: %s" format(p._1, p._2) }.
                      mkString("(", ", ", ")"))
        fm.parameters(false)
      }
      fm.getText
    }
    def getRhsHtml(fm: HtmlFormatter): String = proposal.ret
    def getModifiers: java.util.Set[Modifier] = elemHandle.getModifiers
    override def toString: String = "Proposal2(%s)" format(proposal)
    def isSmart: Boolean = false
    def getCustomInsertTemplate: String = {
      val c0 = methodTemplate(proposal.name)
      if (c0 != null ) {
        c0
      }
      else {
        if (valOrNoargFunc) {
          proposal.name
        }
        else {
          "%s(%s)" format(proposal.name, proposal.params.map{"${%s}"format(_)}.mkString(","))
        }
      }
    }
  }
  
  def signature(proposal: CompletionInfo) = {
    val valOrNoargFunc = proposal.value || proposal.params.size == 0 && proposal.ret != "Unit"
    val sb = new StringBuilder
    sb.append("<strong>%s</strong>" format(proposal.name))
    if (!valOrNoargFunc) {
      sb.append(proposal.params.zip(proposal.paramTypes).
                map{ p => "%s: %s" format(p._1, p._2) }.
                mkString("(", ", ", ")"))
    }
    sb.append(": ")
    sb.append("<em>%s</em>" format(proposal.ret))
    sb.toString
  }
  
  val scalaImageIcon = Utils.loadIcon("/images/scala16x16.png")

  def methodTemplate(completion: String) = {
    CodeCompletionUtils.BuiltinsMethodTemplates.getOrElse(
      completion,
      CodeCompletionUtils.ExtraMethodTemplates.getOrElse(completion, null)
    )
  }

  override def complete(context: CodeCompletionContext): CodeCompletionResult = {
    if (context.getQueryType != QueryType.COMPLETION) {
      return CodeCompletionResult.NONE
    }
    
    val proposals = new java.util.ArrayList[CompletionProposal]
    val caretOffset = context.getCaretOffset

    val (objid, prefix) = completionSupport.objidAndPrefix(caretOffset)
    
    if (objid.isEmpty) {
      val (varCompletions, voffset) = completionSupport.varCompletions(prefix)
      varCompletions.foreach { completion =>
        proposals.add(new ScalaCompletionProposal(caretOffset - voffset, completion, 
                                                  ElementKind.VARIABLE,
                                                  methodTemplate(completion)))
      }

      val (keywordCompletions, koffset) = completionSupport.keywordCompletions(prefix)
      keywordCompletions.foreach { completion =>
        proposals.add(new ScalaCompletionProposal(caretOffset - koffset, completion,
                                                  ElementKind.KEYWORD,
                                                  CodeCompletionUtils.KeywordTemplates.getOrElse(completion, null),
                                                  scalaImageIcon))
      }

      // temporary fix for NB7.1.1 not showing code templates
      addTemplateProposals(proposals, prefix)
    }

    if (objid.isDefined) {
      val (methodCompletions, coffset) = completionSupport.methodCompletions2(caretOffset, objid.get, prefix)
      methodCompletions.foreach { completion =>
        proposals.add(new ScalaCompletionProposal2(caretOffset - coffset, completion,
                                                   ElementKind.METHOD))
      }
    }

    if (proposals.size > 1)
      proposals.add(new ScalaCompletionProposal(caretOffset, "               ", ElementKind.OTHER))

    val completionResult = new DefaultCompletionResult(proposals, false)
    // the line of code below seems to fix the completion filtering problem, 
    // in which more specific completions disappeared as you typed more
    completionResult.setFilterable(false) 
//    Log.info("Completion Result: " + proposals)
    return completionResult
  }
  
  def addTemplateProposals(proposals: java.util.ArrayList[CompletionProposal], prefix: Option[String]) {
    def toProposal(offset: Int, ct: CodeTemplate) = new CompletionProposal {
      def getAnchorOffset: Int = offset
      def getName: String = ct.getAbbreviation
      def getInsertPrefix: String = "insert prefix"
      def getSortText: String = ct.getAbbreviation
      def getSortPrioOverride: Int = 0
      def getElement: ElementHandle = null
      def getKind: ElementKind = ElementKind.OTHER
      def getIcon: ImageIcon = ImageUtilities.loadImageIcon("org/netbeans/lib/editor/codetemplates/resources/code_template.png", false)
      def getLhsHtml(fm: HtmlFormatter) = util.JUtils.toHtmlText(
        ct.getParametrizedText.split("\n")(0).replaceAll("""\$\{(.+?)\}""", """$1""").replaceAll("cursor", "|")
      )
      def getRhsHtml(fm: HtmlFormatter) = "<strong>%s<strong>" format ct.getAbbreviation
      def getModifiers: java.util.Set[Modifier] = java.util.Collections.emptySet[Modifier]
      override def toString: String = "Code Template Proposal(%s)" format(ct)
      def isSmart: Boolean = false
      def getCustomInsertTemplate = ct.getParametrizedText
    }
    def ignoreCaseStartsWith(s1: String, s2: String) = s1.toLowerCase.startsWith(s2.toLowerCase)

    val doc = CodeExecutionSupport.instance().codePane.getDocument
    import collection.JavaConversions._
    val cts = CodeTemplateManager.get(doc).getCodeTemplates
    cts.filter {ct => ignoreCaseStartsWith(ct.getAbbreviation, prefix.getOrElse(""))}.foreach { ct =>
      proposals.add(toProposal(0, ct))
    }
  }
  
  override def document(pr: ParserResult, element: ElementHandle): String = element match {
    case e: ScalaElementHandle => Help(e.getName)
    case e2: ScalaElementHandle2 => signature(e2.proposal)
    case _ => null
  }

  override def resolveLink(link: String, elementHandle: ElementHandle): ElementHandle = {
    null
  }

  override def getPrefix(info: ParserResult, lexOffset: Int, upToOffset: Boolean): String = {
    ""
  }

  override def getAutoQuery(component: JTextComponent, typedText: String): QueryType = {
    typedText.charAt(0) match {
      // TODO - auto query on ' and " when you're in $() or $F()
      case '\n' | '(' | '[' | '{' |';' => return QueryType.STOP
      case '.' => return QueryType.COMPLETION
      case _ => return QueryType.NONE
    }
  }


  override def resolveTemplateVariable(variable: String, info: ParserResult, caretOffset: Int,
                                       name: String , parameters: java.util.Map[_, _]): String = {
    throw new UnsupportedOperationException("Not supported yet.")
  }

  override def getApplicableTemplates(doc:  javax.swing.text.Document, selectionBegin: Int, selectionEnd: Int): java.util.Set[String] = {
    java.util.Collections.emptySet[String]
  }

  override def parameters(info: ParserResult, lexOffset: Int, proposal: CompletionProposal): ParameterInfo = {
    ParameterInfo.NONE
  }
}
