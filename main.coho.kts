import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class Post(
    val source: Path?,
    val title: String,
    val description: String,
    val tags: List<String>,
    val pubDate: LocalDateTime,
    val editDate: LocalDateTime?
) {
    companion object {
        fun String?.parseDt(): LocalDateTime? =
            this?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }

        fun fromFrontmatter(source: Path?, frontmatter: Map<String?, Any?>) = Post(
            source = source,
            title = frontmatter["title"] as? String ?: "null",
            description = frontmatter["description"] as? String ?: "null",
            tags = (frontmatter["tags"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            pubDate = (frontmatter["published-date"] as? String).parseDt() ?: LocalDateTime.MIN,
            editDate = (frontmatter["updated-date"] as? String).parseDt()
        )
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
        .sortedBy { it.pubDate }.reversed()
    val allPostsMap = allPosts.map(Post::toMap)
    val allTags = allPosts.flatMap { it.tags as? List<*> ?: emptyList() }.distinct()

    markdownTemplate = {
        val title: String? = frontmatter["title"] as? String
        val description: String? = frontmatter["description"] as? String
        val source: String? = frontmatter["source"] as? String
        val type: String? = frontmatter["type"] as? String

        ktMdTemplate(
            src("markdown-template.html"),
            context = mapOf("title" to title, "description" to description, "source" to source, "type" to type),
        )(it)
    }

    val allProjects = source.cd("projects").files("*.md").mapNotNull {
        parseMarkdownFrontmatter(it.readText()).first?.plus(mapOf("path" to it))
    }.toMutableList()
    // can't do sortedBy for some strange reason
    allProjects.sortBy { it["priority"] as? Int }
    var globalContext = mutableMapOf<String, Any?>(
        "projects" to allProjects,
        "posts" to allPostsMap,
        "gitHash" to exec("git", "rev-parse", "--short", "HEAD"),
        "longGitHash" to exec("git", "rev-parse", "HEAD"),
        "allTags" to allTags,
        "root" to this,
    )
    KtHtmlFile.globalContext = globalContext;
    globalContext["headerHtml"] = ktTemplate(src("components/header.html"))
    globalContext["footerHtml"] = ktTemplate(src("components/footer.html"))
    KtHtmlFile.globalContext = globalContext;

    includes = listOf(src("util.coho.kts"))

    md(src("other.md"))
    ktHtml(src("index.html"))
    cp(src("main.js"))
    cp(src("style.css"))
    cp(src("index.css"))
    cp(src("font.css"))
    cp(src("color.css"))
    cp(src("favicon_fixed.svg"))
    shell(
        "magick", "-background", "transparent", "favicon_fixed.svg", "-define", "icon:auto-resize=512,16,32", "favicon.ico",
    )
    path("projects") {
        source.files("*.md").forEach { md(src(it.name)) }
    }
    path("fonts") {
        source.files().forEach { cp(src(it.name)) }
    }
    val innerHtmls = mutableMapOf<String, String>()
    path("posts") {
        markdownTemplate = {
            val post = Post.fromFrontmatter(source = null, frontmatter = frontmatter)

            innerHtmls[post.title] = it
            ktMdTemplate(
                src("post-template.html"),
                context = mapOf(
                    "title" to post.title, "description" to post.description,
                    "tags" to post.tags, "pubDate" to post.pubDate, "editDate" to post.editDate,
                ),
            )(it)
        }
        source.files("*.md").forEach { md(src(it.name)) }
        val context = mutableMapOf<String, Any?>()
        cp(src("index.js"))
        ktHtml(src("index.html"), context)
    }
    run { path ->
        val rss = tag("rss", "version" to "2.0") {
            tag("channel") {
                tag("title") { append("fishnpotatoes' blog") }
                tag("link") { append("https://fishies.dev/posts") }
                tag("description") { append("fishnpotatoes' blog") }
                tag("language") { append("en") }
                tag("ttl") { append(15) }

                for (post in allPosts) {
                    tag("item") {
                        tag("title") { append(post.title) }
                        tag("description") {
                            cdata {
                                append(innerHtmls[post.title])
                            }
                        }
                        tag("link") {
                            append("https://fishies.dev/posts/${post.source?.nameWithoutExtension}.html")
                        }
                        tag("pubDate") {
                            append(
                                post.pubDate.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)
                            )
                        }
                    }
                }
            }
        }
        path.resolve("rss.xml").writeText(rss)
        listOf(path.resolve("rss.xml"))
    }

    // icons
    dl("https://cdn.jsdelivr.net/npm/@tabler/icons-sprite@latest/dist/tabler-sprite.svg", "tabler.svg")

    // required for github for some reason
    text("fishies.dev", "CNAME")
}
