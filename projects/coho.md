```yaml
meta:
  title: coho
  description: Static site generator written in Kotlin
```
# coho

Coho is a static site generator written in Kotlin. It supports
* live reload
* simple Kotlin-based configuration

[link](/projects/godl.md)

```kotlin
package dev.fishies.coho.core.markdown

import dev.fishies.coho.core.*
import io.noties.prism4j.AbsVisitor
import io.noties.prism4j.Prism4j
import io.noties.prism4j.Prism4j.grammar
import org.apache.commons.text.StringEscapeUtils
import org.intellij.markdown.*
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.*
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser
import java.nio.file.Path
import kotlin.io.path.*

private class SyntaxHighlightedCommonMarkFlavourDescriptor(
    useSafeLinks: Boolean = true, absolutizeAnchorLinks: Boolean = false
) : CommonMarkFlavourDescriptor(useSafeLinks, absolutizeAnchorLinks) {
    override fun createHtmlGeneratingProviders(linkMap: LinkMap, baseURI: URI?): Map<IElementType, GeneratingProvider> {
        val base = super.createHtmlGeneratingProviders(linkMap, baseURI)
        return base + mapOf(
            MarkdownElementTypes.CODE_FENCE to object : GeneratingProvider {
                override fun processNode(
                    visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode
                ) {
                    val prism4j = Prism4j(PrismBundleGrammarLocator())
                    val indentBefore = node.getTextInNode(text).commonPrefixWith(" ".repeat(10)).length

                    visitor.consumeHtml("<pre class=\"codeblock\">")

                    var state = 0

                    var childrenToConsider = node.children
                    if (childrenToConsider.last().type == MarkdownTokenTypes.CODE_FENCE_END) {
                        childrenToConsider = childrenToConsider.subList(0, childrenToConsider.size - 1)
                    }

                    var lastChildWasContent = false

                    val attributes = ArrayList<String>()
                    val content = StringBuilder()
                    var language: String? = null
                    for (child in childrenToConsider) {
                        if (state == 1 && child.type in listOf(
                                MarkdownTokenTypes.CODE_FENCE_CONTENT, MarkdownTokenTypes.EOL
                            )
                        ) {
                            content.append(
                                HtmlGenerator.trimIndents(
                                    HtmlGenerator.leafText(text, child, false), indentBefore
                                ).toString().unescapeHtml()
                            )
                            lastChildWasContent = child.type == MarkdownTokenTypes.CODE_FENCE_CONTENT
                        }
                        if (state == 0 && child.type == MarkdownTokenTypes.FENCE_LANG) {
                            language = HtmlGenerator.leafText(text, child).toString().trim().split(' ')[0]
                            attributes.add(
                                "class=\"language-$language\""
                            )
                        }
                        if (state == 0 && child.type == MarkdownTokenTypes.EOL) {
                            visitor.consumeTagOpen(node, "code", *attributes.toTypedArray())
                            state = 1
                        }
                    }
                    val grammar = language?.run { prism4j.grammar(this) }
                    if (grammar != null) {

                        val tokens = prism4j.tokenize(content.toString(), grammar)
                        val tokenVisitor: AbsVisitor = object : AbsVisitor() {
                            override fun visitText(text: Prism4j.Text) { // raw text
                                visitor.consumeHtml("<span class=\"code-text code-$language-text\">${text.literal().escapeHtml()}</span>")
                            }

                            override fun visitSyntax(syntax: Prism4j.Syntax) { // type of the syntax token
                                val firstChild = syntax.children().first()
                                if (syntax.children().size == 1 && firstChild is Prism4j.Text) {
                                    val inner = firstChild.literal().escapeHtml()
                                    visitor.consumeHtml("<span class=\"code-${syntax.type()} code-$language-${syntax.type()}\">$inner</span>")
                                } else {
                                    visit(syntax.children())
                                }
                            }
                        }
                        tokenVisitor.visit(tokens)
                    } else {
                        visitor.consumeHtml(content.toString().escapeHtml())
                    }

                    if (state == 0) {
                        visitor.consumeTagOpen(node, "code", *attributes.toTypedArray())
                    }
                    if (lastChildWasContent) {
                        visitor.consumeHtml("\n")
                    }
                    visitor.consumeHtml("</code></pre>")
                }
            })
    }
}

open class MarkdownFile(val path: Path, val tagRenderer: HtmlGenerator.TagRenderer) : Element(path.name) {
    protected open fun preprocessMarkdown(src: String) = src
    protected open fun createHtml(src: String, tree: ASTNode, flavour: MarkdownFlavourDescriptor) =
        HtmlGenerator(src, tree, flavour).generateHtml(tagRenderer)

    override fun _generate(location: Path): List<Path> {
        val src = preprocessMarkdown(path.readText())
        val flavour = SyntaxHighlightedCommonMarkFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(src)
        return listOf(
            location.resolve(path.nameWithoutExtension + ".html")
                .apply { writeText(createHtml(src, parsedTree, flavour)) })
    }
}

/**
 * Convert a Markdown file to an HTML body directly with no templating or other post-processing.
 * You likely don't want this one, use [md] for more flexibility.
 */
fun OutputPath.mdBasic(source: Path) = children.add(
    MarkdownFile(
        source, HtmlGenerator.DefaultTagRenderer(
            DUMMY_ATTRIBUTES_CUSTOMIZER, false
        )
    )
)
```
