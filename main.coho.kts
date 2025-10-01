import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.net.URI
import java.nio.file.Path

data class Post(
    val source: Path?,
    val title: String,
    val description: String,
    val tags: List<String>,
    val pubDate: LocalDateTime,
    val editDate: LocalDateTime?,
    val commentDid: URI?,
    val hide: Boolean,
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
                editDate = (frontmatter["updated-date"] as? String).parseDt(),
                commentDid = (frontmatter["comment-did"] as? String)?.let { URI(it) },
                hide = (frontmatter["hide"] as? Boolean) ?: false,
            )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "source" to source,
        "title" to title,
        "description" to description,
        "tags" to tags,
        "pubDate" to pubDate,
        "editDate" to editDate,
        "commentDid" to commentDid,
        "hide" to hide,
    )
}

root {
    val allPosts = source.cd("posts").files("*.md")
        .map { Post.fromFrontmatter(it, parseMarkdownFrontmatter(it.readText()).first ?: emptyMap()) }
        .filter { !it.hide }
        .sortedBy { it.pubDate }.reversed()
    val allPostsMap = allPosts.map(Post::toMap)
    val allTagsNotDistinct =
        allPosts.flatMap { it.tags as? List<*> ?: emptyList() }.mapNotNull { it as? String }
    val allTags = allTagsNotDistinct.distinct()
    val tagCounts = mutableMapOf<String, Int>()
    for (tag in allTagsNotDistinct) {
        tagCounts[tag] = (tagCounts[tag] ?: 0) + 1
    }

    markdownTemplate = {
        val title: String? = frontmatter["title"] as? String
        val description: String? = frontmatter["description"] as? String
        val source: String? = frontmatter["source"] as? String
        val docs: String? = frontmatter["docs"] as? String
        val type: String? = frontmatter["type"] as? String

        ktMdTemplate(
            src("markdown-template.html"),
            context = mapOf(
                "title" to title,
                "description" to description,
                "source" to source,
                "type" to type,
                "docs" to docs
            ),
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
        "tagCounts" to tagCounts,
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
    if (src("favicon.ico").notExists())
        exec(
            "convert",
            "-background",
            "transparent",
            "favicon.svg",
            "-define",
            "icon:auto-resize=512,16,32",
            "favicon.ico",
        )
    cp(src("favicon.svg"))
    cp(src("favicon.ico"))
    path("projects") {
        source.files("*.md").forEach { md(it) }
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
                src("index/post-template.html"),
                context = mapOf(
                    "title" to post.title,
                    "description" to post.description,
                    "tags" to post.tags,
                    "pubDate" to post.pubDate,
                    "editDate" to post.editDate,
                    "commentDid" to post.commentDid,
                ),
            )(it)
        }
        allPosts.forEach { md(it.source!!) }
        val context = mutableMapOf<String, Any?>()
        cp(src("index/index.js"))
        cp(src("index/index.css"))
        cp(src("index/bsky-comments.js"))
        ktHtml(src("index/index.html"), context)
    }
    run { path ->
        val rss = "rss"("version" to "2.0") {
            "channel" {
                "title" { append("fishnpotatoes' blog") }
                "link" { append("https://fishies.dev/posts") }
                "description" { append("fishnpotatoes' blog") }
                "language" { append("en") }
                "ttl" { append(15) }

                for (post in allPosts) {
                    "item" {
                        "title" { append(post.title) }
                        "description" {
                            cdata {
                                append(innerHtmls[post.title])
                            }
                        }
                        "link" {
                            append("https://fishies.dev/posts/${post.source?.nameWithoutExtension}.html")
                        }
                        "pubDate" {
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
