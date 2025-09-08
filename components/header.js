class PageHeader extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.innerHTML = `<?kt
        import dev.fishies.coho.html.* ktTemplate(root.src("components/header.html"))
        ?>`
    }
}

customElements.define("page-header", PageHeader)
