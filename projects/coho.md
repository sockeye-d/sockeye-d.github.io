```yaml
priority: 1
title: coho
description: Static site generator written in Kotlin
source: "https://github.com/sockeye-d/coho"
docs: "https://coho.fishies.dev"
long-description: >
    Coho is a tool I refer to as "The Kotlin programmer's static website generator."
    The entire thing is built around the Kotlin scripting engine using a custom Kotlin DSL as a build script.

    This makes it significantly more flexible than existing solutions, since you basically get to build your own mini-framework on top of a coho.
```
Coho is a tool I refer to as "The Kotlin programmer's static website generator."
The entire thing is built around the Kotlin scripting engine using a custom Kotlin DSL as a build script.
This makes it significantly more flexible than existing solutions, since you basically get to build your own mini-framework on top of a coho.

> For more details about it, you can read my blog post [here](/posts/coho.md)


Would be nice to be able to just drop a $\frac{2}{5}$ in a sentence

$$
\begin{aligned} x^5 &= 15 \\ x &= 15^{\frac{1}{5}} \end{aligned}
$$

Its main feature is $ 2 $ being able to template HTML by embedding Kotlin code directly inside it.
It's like PHP, but it runs once at build time to generate the output HTML files.
For example, this is the [posts page's source](/posts/):
```kthtml
<!DOCTYPE HTML>
<html>

<head>
    <meta charset="UTF-8">
    <title>fishnpotatoes' blog</title>
    <meta property="og:title" content="fishnpotatoes&apos; blog">
    <meta property="og:type" content="website">
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <link rel="stylesheet" href="/style.css">
    <link rel="icon" href="/favicon.ico">
</head>

<body>
    <?kt headerHtml ?>
    <content>
        <h1> Blog posts <a href="/rss.xml">(feed)</a> </h1>
        <div class="tag-container">
            <?kt@file:Import; allTags.joinToString("", transform = ::clickableTag) ?>
        </div>
        <?kt@file:Import; (posts as List<Map<String?, Any? >>)
            .joinToString("") {
                val tags = (it["tags"] as List<*>).joinToString(" ") { "post-tag-$it" }
                "div" ("class" to "card post-container ${tags}") {
                    val source = (it["source"] as Path).nameWithoutExtension
                    "a" ("href" to "/posts/$source.html") {
                        "h2" {
                            append(it["title"])
                        }
                    }
                    "p" {
                        append(it["description"])
                    }
                    "p" ("class" to "subtext") {
                        append("Published ")
                        append(it["pubDate"].formatDateTime())
                    }
                    "div" ("class" to "tag-container") {
                        append((it["tags"] as List<*>)
                            .joinToString("", transform = ::clickableTag)
                        )
                    }
                }
            }?>
    </content>
    <?kt footerHtml ?>
</body>
<script src="/main.js"></script>
<script src="/posts/index.js"></script>

</html>
```

That's the entire source HTML.
At build time, I read all the Markdown files from the post directory and pass it as a provided script variable to the template, which then compiles and evaluates down to a bunch of raw HTML that has one card for every post.

Once the website has been built, it's just a bunch of raw HTML, CSS, JS, and whatever other assets you include. 

# main.coho.kts

This is the main buildscript for the project. It's "just Kotlin™" with the coho DSL automatically imported to reduce friction. You can do whatever you want — write classes, functions, and more, but it *must* return a RootPath object, generally with the [`root`](https://coho.fishies.dev/core/dev.fishies.coho/root.html) function:

```kotlin
// the simplest main.coho.kts
root {

}
```

Once you have this, you can basically do anything. For example, converting all Markdown files to HTML files:

```kotlin
root {
    for (file in source.files("*.md")) {
        md(file)
    }
}
```

Since it's "just Kotlin™", let's filter it to just the first 5 files alphabetically:

```kotlin
root {
    for (file in source.files("*.md").sortedBy { it.name }.subList(0, 5)) {
        md(file)
    }
}
```

You can define subpaths of the build path with [`path`](https://coho.fishies.dev/core/dev.fishies.coho/path.html):

```kotlin
root {
    for (file in source.files("*.md").sortedBy { it.name }.subList(0, 5)) {
        md(file)
    }

    path("projects") {
        md(src("my-only-project.md"))
    }
}
```

Also, you might have seen this [`src`](https://coho.fishies.dev/core/dev.fishies.coho/-output-path/src.html) function; it returns a [`java.nio.Path`](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html) that refers to a path in the source directory in the same directory as the current path structure. Basically what that means is you can do


```kotlin
root {
    path("a") {
        path("b") {
            path("c") {
                println(src("nested.txt"))
            }
        }
    }
}
```

and it'll print `./a/b/c/nested.txt`. Of course, this only works if your source hierarchy and build hierarchy match, but they probably should for the most part.

There's also a matching [`build`](https://coho.fishies.dev/core/dev.fishies.coho/-output-path/build.html) function to refer to paths in the build directory. This is useful for shell scripts:

```kotlin
root {
    shell(
        "convert", "-background", "transparent",
        src("favicon.svg"),
        "-define", "icon:auto-resize=512,16,32",
        build("favicon.ico")
    )
}
```

Even though `shell` runs in the current build directory, using `#!kotlin src("favicon.svg")` here expands to an absolute path in the source directory.

## Markdown parsing

Coho has built-in support for parsing GitHub-flavored Markdown and emitting HTML from it. Combined with coho's powerful HTML templating features, you can make Markdown templates that are almost pure HTML like this:

```kthtml
<!DOCTYPE html>
<html>

<head>
    <meta charset="UTF-8">
    <title>
        <?kt title ?>
    </title>
    <meta property="og:title" content="<?kt title ?>">
    <meta property="og:description" content="<?kt description ?>">
    <meta property="og:type" content="website">
    <meta name="viewport" content="width=device-width,initial-scale=1" />
</head>

<link rel="stylesheet" href="/style.css">
<?kt headerHtml ?>
<content>
    <?kt
    buildString {
        "h1" { append(title) }
        if (source != null) {
            "a" ("href" to source) {
                append("""<svg class="icon-medium"><use href="/tabler.svg#tabler-brand-github" /></svg>""")
            }
        }
        if (docs != null) {
            "a" ("href" to docs) {
                append("""<svg class="icon-medium"><use href="/tabler.svg#tabler-book" /></svg>""")
            }
        }
    }
    ?>
    <?kt content ?>
</content>
<?kt footerHtml ?>
<script src="/main.js"></script>

</html>
```

> this is the template that generates this page!

The provided variables `title`, `source`, and `docs` are parsed from the YAML frontmatter and provided through the `markdownTemplate` variable:

```kotlin
root {
    markdownTemplate = {
        val title: String? = frontmatter["title"] as? String
        val description: String? = frontmatter["description"] as? String
        val source: String? = frontmatter["source"] as? String
        val docs: String? = frontmatter["docs"] as? String
        val type: String? = frontmatter["type"] as? String

        ktMdTemplate(
            src("markdown-template.html"),
            context = mapOf("title" to title, "description" to description, "source" to source, "type" to type, "docs" to docs),
        )(it)
    }
}
```

The `markdownTemplate` is inherited through the hierarchy and can be overriden at deeper levels by setting it again.

The `content` provided variable is automatically passed in by the [`ktMdTemplate`](https://coho.fishies.dev/core/dev.fishies.coho.html/kt-md-template.html) function.

### Syntax highlighting

I think coho has one of the best syntax highlighting systems of all the static website generators.
Rather than including a client-side highlighter like Shiki or hljs, codeblocks are highlighted *at build time* with [Prism4j](https://github.com/noties/Prism4j/).
This means that visitors won't get a brief flash of unhighlighted content as the page loads, and it doesn't require any JS to get highlighted code.

You can use the standard fenced code block syntax for highlighted codeblocks:
````markdown
```kotlin
println("hi")
```
````

```kotlin
println("hi")
```

Coho also supports highlighted inline code spans with a shebang syntax:
````markdown
inline `#!kotlin println("hi")` codeblock
````

inline `#!kotlin println("hi")` codeblock
