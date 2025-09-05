function waitForElm(selector) {
    return new Promise(resolve => {
        if (document.querySelector(selector)) {
            return resolve(document.querySelector(selector));
        }

        const observer = new MutationObserver(mutations => {
            if (document.querySelector(selector)) {
                observer.disconnect();
                resolve(document.querySelector(selector));
            }
        });

        // If you get "parameter 1 is not of type 'Node'" error, see https://stackoverflow.com/a/77855838/492336
        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    });
}

async function loadTheme() {
    if (localStorage.getItem("theme-mode") === null) {
        setTheme(window.matchMedia('(prefers-color-scheme: light)').matches);
    } else {
        setTheme(localStorage.getItem("theme-mode") === "light");
    }
}

window.matchMedia('(prefers-color-scheme: light)').addEventListener('change', event => {
    setTheme(event.matches);
});

async function toggleTheme() {
    setTheme(document.documentElement.getAttribute("data-theme") !== "light");
}

async function setTheme(lightMode) {
    if (lightMode) {
        localStorage.setItem("theme-mode", "light");
        document.documentElement.setAttribute("data-theme", "light");
        await waitForElm("#themeToggle");
        themeToggle.innerHTML = `
<svg class="icon-medium">
    <use href="/tabler.svg#tabler-moon" />
</svg>
        `
    } else {
        localStorage.setItem("theme-mode", "dark");
        document.documentElement.setAttribute("data-theme", "dark");
        await waitForElm("#themeToggle");
        themeToggle.innerHTML = `
<svg class="icon-medium">
    <use href="/tabler.svg#tabler-sun" />
</svg>
        `
    }
}

function popupMenu() {
    if (linkPopup.classList.contains("open"))
        linkPopup.classList.remove("open");
    else
        linkPopup.classList.add("open");
}

loadTheme();
