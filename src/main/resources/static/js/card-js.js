let intervals = new Map();

function startSlideshow(container) {
    let images = container.getElementsByClassName("product-image");
    let index = 0;

    if (images.length <= 1) return;

    images[0].style.opacity = "1";
    intervals.set(container, setInterval(() => {
        images[index].style.opacity = "0";
        index = (index + 1) % images.length;
        images[index].style.opacity = "1";
    }, 1000));
}

function stopSlideshow(container) {
    clearInterval(intervals.get(container));
    intervals.delete(container);

    let images = container.getElementsByClassName("product-image");
    if (images.length > 0) {
        for (let img of images) img.style.opacity = "0";
        images[0].style.opacity = "1";
    }
}
