class PageHeader extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.innerHTML = `<h1>
    fishnpotatoes' website
</h1>
<div class='links'>

    <div class='link-container'>
        <a class='project-link' href='/'>home</button></a>
        <a class='project-link' href='/projects/sled.html'>sled</button></a><a class='project-link' href='/projects/aaa.html'>aaa</button></a><a class='project-link' href='/projects/test-project.html'>test project</button></a><a class='project-link' href='/projects/coho.html'>coho</button></a><a class='project-link' href='/projects/godl.html'>godl</button></a>
    </div>
</div>
`
    }
}

customElements.define("page-header", PageHeader)
