class PageFooter extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.innerHTML = `<footer>
    <span class='footer-text'>
        Built with <a href='https://github.com/sockeye-d/coho'>coho</a> from <span class='tooltipped'
            title='a451bd156a9bd96ce4a6fbfe4586ff5eb194c575
'>
            a451bd1

        </span>
    </span>
    <div class="socials">
        <a href="https://github.com/sockeye-d">
            <svg class="icon-medium">
                <use href="/tabler.svg#tabler-brand-github" />
            </svg>
        </a>
        <a href="https://bsky.app/profile/fishies.dev">
            <svg class="icon-medium">
                <use href="/tabler.svg#tabler-brand-bluesky" />
            </svg>
        </a>
    </div>
</footer>
`
    }
}

customElements.define("page-footer", PageFooter)
