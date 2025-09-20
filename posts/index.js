let style;
let lastTag;

const urlParams = new URLSearchParams(window.location.search);
const tag = urlParams.get("tag");
if (tag !== null) {
    setFilterTag(tag);
}

function setFilterTag(tag) {
    if (style !== undefined)
        style.remove()
    if (lastTag == tag) {
        style.remove()
        style = undefined;
        lastTag = undefined;
        return;
    }
    style = document.createElement("style");

    style.innerHTML =
`.post-tag-button-${tag} {
    background-color: var(--surface1) !important;
}

.post-container:not(.post-tag-${tag}) {
    display: none;
}
`;

    document.getElementsByTagName('head')[0].appendChild(style);

    lastTag = tag;
}
