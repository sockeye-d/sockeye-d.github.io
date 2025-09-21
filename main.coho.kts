import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Post(
    val source: Path,
    val title: String,
    val description: String,
    val tags: List<String>,
    val pubDate: LocalDateTime,
    val editDate: LocalDateTime
) {
    companion object {
        private fun String?.parseDt(): LocalDateTime =
            this?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) } ?: LocalDateTime.MIN

        fun fromFrontmatter(source: Path, frontmatter: Map<String?, Any?>) = Post(
            source = source,
            title = frontmatter["title"] as? String ?: "null",
            description = frontmatter["description"] as? String ?: "null",
            tags = (frontmatter["tags"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            pubDate = (frontmatter["published-date"] as? String).parseDt(),
            editDate = (frontmatter["updated-date"] as? String).parseDt())
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "source" to source,
        "title" to title,
        "description" to description,
        "tags" to tags,
        "pubDate" to pubDate,
        "editDate" to editDate
    )
}

root {
    val allPosts = source.cd("posts").files("*.md")
        .map { Post.fromFrontmatter(it, parseMarkdownFrontmatter(it.readText()).first ?: emptyMap()) }
    val allTags = allPosts.flatMap { it.tags as? List<*> ?: emptyList() }.distinct()

    markdownTemplate = {
        val title: String? = frontmatter["title"] as? String
        val description: String? = frontmatter["description"] as? String
        val type: String? = frontmatter["type"] as? String

        ktMdTemplate(
            src("markdown-template.html"),
            context = mapOf("title" to title, "description" to description, "type" to type),
        )(it)
    }

    KtHtmlFile.globalContext = mapOf(
        "projects" to source.cd("projects").files("*.md").map { it.nameWithoutExtension },
        "gitHash" to exec("git", "rev-parse", "--short", "HEAD"),
        "longGitHash" to exec("git", "rev-parse", "HEAD"),
        "allTags" to allTags,
        "headerHtml" to ktTemplate(src("components/header.html")),
        "footerHtml" to ktTemplate(src("components/footer.html")),
        "root" to this,
    )

    includes = listOf(src("util.coho.kts"))

    md(src("other.md"))
    ktHtml(src("index.html"))
    cp(src("main.js"))
    cp(src("style.css"))
    cp(src("font.css"))
    cp(src("color.css"))
    cp(src("favicon.png"))
    shell(
        "magick", "-background", "transparent", "favicon.png", "-define", "icon:auto-resize=512,16,32", "favicon.ico",
    )
    path("projects") {
        source.files("*.md").forEach { md(src(it.name)) }
    }
    path("components") {
        ktHtml(src("header.js"))
        ktHtml(src("footer.js"))
    }
    path("fonts") {
        source.files().forEach { cp(src(it.name)) }
    }
    path("posts") {
        val innerHtmls = mutableMapOf<String, String>()
        markdownTemplate = {
            val title: String? = frontmatter["title"] as? String
            val description: String? = frontmatter["description"] as? String
            val type: String? = frontmatter["type"] as? String
            val tags = frontmatter["tags"] as? List<*> ?: emptyList<String>()

            title?.let { title -> innerHtmls[title] = it }
            ktMdTemplate(
                src("post-template.html"),
                context = mapOf("title" to title, "description" to description, "type" to type, "tags" to tags),
            )(it)
        }
        source.files("*.md").forEach { md(src(it.name)) }
        val context = mutableMapOf<String, Any?>()
        run { // populate the posts at build time
            context["posts"] =
                allPosts.sortedBy { it.pubDate }.reversed().map { it.toMap() + mapOf("html" to innerHtmls[it.title]) }
            emptyList()
        }
        cp(src("index.js"))
        ktHtml(src("index.html"), context)
    }

    // icons
    dl("https://cdn.jsdelivr.net/npm/@tabler/icons-sprite@latest/dist/tabler-sprite.svg", "tabler.svg")

    // required for github for some reason
    text("fishies.dev", "CNAME")
}
