fun generateTagHtml(tag: String) =
"""<a href="/posts?tag=$tag"><svg class="icon-small"><use href="/tabler.svg#tabler-tag" /></svg>$tag</a>"""
