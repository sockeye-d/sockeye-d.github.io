class PageFooter extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.innerHTML = `<footer>
    <span class='footer-text'>
        Built with <a href='https://github.com/sockeye-d/coho'>coho</a> from <span class='tooltipped'
            title='76419461d3a64fa96fbd983613a83557979e497d
'>
            7641946

        </span>
    </span>
    <div class="socials">
        <a href="https://github.com/sockeye-d">
            <svg class="icon-small">
                <use href="/tabler.svg#tabler-brand-github" />
            </svg>
        </a>
        <a href="https://bsky.app/profile/fishies.dev">
            <svg class="icon-small">
                <use href="/tabler.svg#tabler-brand-bluesky" />
            </svg>
        </a>
    </div>
</footer>
`
    }
}

customElements.define("page-footer", PageFooter)
