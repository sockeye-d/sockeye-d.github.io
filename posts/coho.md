```yaml
title: Why I wrote coho
description: Why I wrote coho, the Kotlin programmer's static website generator
tags: [kotlin, web, tooling]
published-date: "2025-09-21T21:23:37Z"
updated-date: "2025-09-21T21:23:37Z"
```

# Why I wrote coho

There are plenty of other good static website generators, like MkDocs and Hugo.
So why would I write my own static website framework?

The short answer is that I like Kotlin, I wanted to do more with it, and I knew it could be compiled and run at runtime through the scripting APIs. I also had [Kotlin DSLs](https://kotlinlang.org/docs/type-safe-builders.html) on my mind and was thinking about how I could apply that to web development.

The other option in my mind was writing a pure vanilla HTML/JS/CSS static website, but that seemed like a daunting task since I wouldn't have access to any sort of higher level language features. The main ones I desired was Markdown parsing (this article is written in Markdown!) and templating (check out the [posts](/posts) page for templates in action). I also knew that I wanted to use a *real* language as the buildscript instead of a configuration language like YAML.

## The final result

To me coho will likely never be actually fully featured — there'll always be things I can add to make life a little easier. For now though, these are a rough outline of the features it has:

