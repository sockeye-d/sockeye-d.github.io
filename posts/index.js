let style;
let lastTag;

const urlParams = new URLSearchParams(window.location.search);
const tag = urlParams.get("tag");
if (tag !== null) {
    setFilterTag(tag);
}

function setFilterTag(tag) {
    if (style !== undefined) {
        style.remove()
    }
    if (lastTag == tag) {
        const url = new URL(location.href);
        url.searchParams.delete("tag");
        history.replaceState(null, "", url);
        style.remove();
        style = undefined;
        lastTag = undefined;
        return;
    }
    const url = new URL(location.href);
    url.searchParams.set("tag", tag);
    history.replaceState(null, "", url)
    style = document.createElement("style");

    style.innerHTML =
`.post-tag-button-${tag} {
    background-color: var(--surface1) !important;
    border: 0.125rem var(--surface2) solid !important;
}

.post-container:not(.post-tag-${tag}) {
    display: none;
}
`;

    document.getElementsByTagName('head')[0].appendChild(style);

    lastTag = tag;
}
