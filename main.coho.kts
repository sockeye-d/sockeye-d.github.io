root {
    markdownTemplate = { ktHtmlTemplate(src("markdown-template.html"))(it) }

    KtHTMLFile.globalContext = mapOf(
        "projects" to source.cd("projects").files("*.md").map { it.nameWithoutExtension },
        "gitHash" to exec("git", "rev-parse", "--short", "HEAD"),
        "longGitHash" to exec("git", "rev-parse", "HEAD"),
        "root" to this,
    )

    md(src("other.md"))
    ktHtml(src("index.html"))
    cp(src("highlight.js"))
    cp(src("style.css"))
    cp(src("font.css"))
    cp(src("color.css"))
    cp(src("favicon.png"))
    shell("nu", "-c",
          "magick convert -background transparent favicon.png -define icon:auto-resize=512,16,32 favicon.ico"
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
    dl("https://unpkg.com/feather-icons@4.29.2/dist/feather-sprite.svg", "feather.svg")
    dl("https://cdn.jsdelivr.net/npm/@tabler/icons-sprite@latest/dist/tabler-sprite.svg", "tabler.svg")
}
