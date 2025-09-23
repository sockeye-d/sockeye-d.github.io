function sortByPostDate(a, b) {
    const dateA = new Date(a.post.record.createdAt);
    const dateB = new Date(b.post.record.createdAt);
    return (dateA < dateB) - (dateA > dateB);
}

function formatDate(date) {
    const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    return `${monthNames[date.getMonth()]} ${date.getDate()} ${date.getFullYear()} at ${(date.getHours() - 1) % 12 + 1}:${date.getMinutes()}:${date.getSeconds()} ${date.getHours() >= 12 ? "PM" : "AM"}`
}

function createPostDisplay(post) {
    const div = document.createElement("div");
    div.classList.add("comment");

    let handleText;
    if (post.post.author?.displayName === undefined || post.post.author?.displayName === "") {
        handleText = `<span>${post.post.author?.handle}</span>`;
    } else {
        handleText = `<span>${post.post.author?.displayName} <span class="subtext">(${post.post.author?.handle})</span></span>`;
    }

    div.innerHTML = `<div class="comment-container">
    <div class="comment-author-container">
        <img class="comment-avatar" src="${post.post.author?.avatar}">
        ${handleText}
        <span class="subtext"> ⋅ ${formatDate(new Date(post.post.record.createdAt))}</span>
    </div>
    ${post.post.record.text}
    <div class="comment-stats">
        <svg class="icon-smedium subtext">
            <use href="/tabler.svg#tabler-message" />
        </svg>
        <span>${post.post.replyCount}</span>
        <svg class="icon-smedium subtext">
            <use href="/tabler.svg#tabler-repeat" />
        </svg>
        <span>${post.post.repostCount}</span>
        <svg class="icon-smedium subtext">
            <use href="/tabler.svg#tabler-heart" />
        </svg>
        <span>${post.post.likeCount}</span>
    </div>
</div>`
    const replies = post.replies;
    replies.sort(sortByPostDate);
    for (const reply of replies) {
        div.appendChild(createPostDisplay(reply));
    }
    return div
}

async function populateComments() {
    const commentsContainer = document.querySelector("#comments-container");
    const path = commentsContainer.attributes["atproto-path"].textContent;
    const commentsResponse = await fetch("https://public.api.bsky.app/xrpc/app.bsky.feed.getPostThread?uri=" + path);

    if (!commentsResponse.ok) {
        commentsContainer.innerHTML = `Request failed with ${commentsResponse.status}`;
        return;
    }

    const comments = (await commentsResponse.json()).thread;
    console.log(comments);

    const replies = comments.replies;
    if (replies.length === 0) {
        commentsContainer.innerHTML = "<p>There aren't any comments yet 😢</p>";
        return;
    }
    replies.sort(sortByPostDate);
    for (const reply of replies) {
        commentsContainer.appendChild(createPostDisplay(reply));
    }
}

populateComments()
