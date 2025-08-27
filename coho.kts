import dev.fishies.coho.core.*
import java.nio.file.Path
import kotlin.io.path.*

root {
    ProcessedMarkdownFile.defaultTemplate = {
        // language=html
        ogMetadataTemplate(ktHtmlTemplate("markdown-template.html")(it))
    }

    val ctx = mapOf("projects" to src.cd("projects").files("*.md").map { it.nameWithoutExtension })
    md(+"other.md")
    ktHtml(+"index.html", ctx)
    cp(+"highlight.js")
    cp(+"style.css")
    cp(+"font.css")
    cp(+"color.css")
    cp(+"favicon.ico")
    path("fonts") {
        src.files().forEach { it -> cp(+it.name) }
    }
    path("projects") {
        src.files("*.md").forEach { md(+it.name) }
    }
}
