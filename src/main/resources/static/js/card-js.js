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
// Thêm các sự kiện hover để mở rộng tên và mô tả sản phẩm
document.querySelectorAll('.product-title').forEach((element) => {
    element.addEventListener('mouseover', () => {
        element.style.whiteSpace = 'normal';
        element.style.maxWidth = 'none';
    });
    element.addEventListener('mouseout', () => {
        element.style.whiteSpace = 'nowrap';
        element.style.maxWidth = '200px';  // Điều chỉnh width theo nhu cầu
    });
});

document.querySelectorAll('.product-description').forEach((element) => {
    element.addEventListener('mouseover', () => {
        element.style.whiteSpace = 'normal';
        element.style.maxWidth = 'none';
    });
    element.addEventListener('mouseout', () => {
        element.style.whiteSpace = 'nowrap';
        element.style.maxWidth = '200px';  // Điều chỉnh width theo nhu cầu
    });
});


let visibleCount = 12; // Ban đầu hiển thị 12 sản phẩm
const step = 8; // Mỗi lần nhấn hiện thêm 8
const allCards = document.querySelectorAll('.product-card');
const loadMoreButton = document.querySelector('#loadMoreButton'); // Thêm id cho nút "Xem thêm"

function loadMore(event) {
    const nextVisible = visibleCount + step;
    for (let i = visibleCount; i < nextVisible && i < allCards.length; i++) {
        const cardCol = allCards[i].closest('.col'); // Lấy phần tử .col chứa .product-card
        cardCol.classList.remove('hidden-col'); // Hiển thị .col
        allCards[i].style.display = 'block'; // Hiển thị .product-card
    }
    visibleCount = nextVisible;

    if (visibleCount >= allCards.length) {
        loadMoreButton.style.display = 'none'; // Ẩn nút nếu hết sản phẩm
    }
}

// Ẩn toàn bộ sản phẩm, chỉ hiện các sản phẩm đầu tiên
window.onload = () => {
    // Kiểm tra xem nút "Xem thêm" có tồn tại hay không
    if (loadMoreButton) {
        loadMoreButton.addEventListener('click', loadMore);
    }
    allCards.forEach((card, index) => {
        const cardCol = card.closest('.col');
        if (index < visibleCount) {
            cardCol.classList.remove('hidden-col'); // Hiển thị .col
            card.style.display = 'block'; // Hiển thị .product-card
        } else {
            cardCol.classList.add('hidden-col'); // Ẩn .col
            card.style.display = 'none'; // Ẩn .product-card
        }
    });

    // Gắn sự kiện cho nút "Xem thêm"
    loadMoreButton.addEventListener('click', loadMore);
};
