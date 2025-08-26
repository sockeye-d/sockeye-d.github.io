import dev.fishies.coho.core.*
import dev.fishies.coho.markdown.*
import java.nio.file.Path
import kotlin.io.path.*

root {
    ProcessedMarkdownFile.defaultTemplate = {
        // language=html
        ogMetadataTemplate("<link rel='stylesheet' href='/style.css'>$it\n<script src='/highlight.js' type='module'></script>")
    }

    val ctx = mapOf("projects" to src.cd("projects").files("*.md").map { it.nameWithoutExtension })
    md(+"other.md")
    ktHtml(+"index.html", ctx)
    cp(+"highlight.js")
    cp(+"style.css")
    path("projects") {
        for (path in src.files("*.md")) {
            md(+path.name)
        }
    }
}
