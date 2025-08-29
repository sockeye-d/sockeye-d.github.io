import dev.fishies.coho.core.*
import dev.fishies.coho.core.markdown.*
import dev.fishies.coho.core.html.*
import java.nio.file.Path
import kotlin.io.path.*

root {
    ProcessedMarkdownFile.defaultTemplate = {
        // language=html
        ogMetadataTemplate(ktHtmlTemplate("markdown-template.html")(it))
    }

    KtHTMLFile.globalContext = mapOf(
        "projects" to source.cd("projects").files("*.md").map { it.nameWithoutExtension },
        "gitHash" to exec("git", "rev-parse", "--short", "HEAD"),
    )
    md(src("other.md"))
    ktHtml(src("index.html"))
    cp(src("highlight.js"))
    cp(src("style.css"))
    cp(src("font.css"))
    cp(src("color.css"))
    cp(src("favicon.png"))
    shell("nu", "-c",
          "magick convert -background transparent favicon.png -define icon:auto-resize=16,32 favicon.ico"
    )
    path("fonts") {
        source.files().forEach { it -> cp(src(it.name)) }
    }
    path("projects") {
        source.files("*.md").forEach { md(src(it.name)) }
    }
}
