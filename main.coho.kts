root {
    markdownTemplate = {
        val meta = frontmatter["meta"]?.asMap()
        val title: String? = meta?.get("title") as? String
        val description: String? = meta?.get("description") as? String
        val type: String? = meta?.get("type") as? String

        ktHtmlTemplate(
            src("markdown-template.html"),
            context = mapOf("title" to title, "description" to description, "type" to type),
        )(it)
    }

    KtHTMLFile.globalContext = mapOf(
        "projects" to source.cd("projects").files("*.md").map { it.nameWithoutExtension },
        "gitHash" to exec("git", "rev-parse", "--short", "HEAD"),
        "longGitHash" to exec("git", "rev-parse", "HEAD"),
        "root" to this,
    )

    md(src("other.md"))
    ktHtml(src("index.html"))
    cp(src("main.js"))
    cp(src("style.css"))
    cp(src("font.css"))
    cp(src("color.css"))
    cp(src("favicon.png"))
    shell(
          "magick", "convert", "-background", "transparent", "favicon.png", "-define", "icon:auto-resize=512,16,32", "favicon.ico",
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

    // icons
    dl("https://cdn.jsdelivr.net/npm/@tabler/icons-sprite@latest/dist/tabler-sprite.svg", "tabler.svg")

    // required for github for some reason
    text("fishies.dev", "CNAME")
}
