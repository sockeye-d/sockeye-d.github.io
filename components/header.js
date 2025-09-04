class PageHeader extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.innerHTML = `<div class="main-header">
    <a href="/">
        <h1>
            fishies.dev
        </h1>
    </a>
    <div class='links'>
        <a onclick="popupMenu()" class="link-menu">
            <svg class="icon-medium">
                <use href="/tabler.svg#tabler-menu-2" />
            </svg>
        </a>
        <div class='link-container'>
            <a class='project-link' href='/projects/sled.html'>sled</button></a><a class='project-link' href='/projects/aaa.html'>aaa</button></a><a class='project-link' href='/projects/test-project.html'>test project</button></a><a class='project-link' href='/projects/coho.html'>coho</button></a><a class='project-link' href='/projects/godl.html'>godl</button></a>
        </div>
    </div>
</div>
<div id="linkPopup" class="link-popup">
    <a class='project-link' href='/projects/sled.html'>sled</button></a><a class='project-link' href='/projects/aaa.html'>aaa</button></a><a class='project-link' href='/projects/test-project.html'>test project</button></a><a class='project-link' href='/projects/coho.html'>coho</button></a><a class='project-link' href='/projects/godl.html'>godl</button></a>
</div>
`
    }
}

customElements.define("page-header", PageHeader)
