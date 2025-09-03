class PageFooter extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.innerHTML = `<?kt
        import dev.fishies.coho.core.html.* ktTemplate(root.src("components/footer.html"))
        ?>`
    }
}

customElements.define("page-footer", PageFooter)
