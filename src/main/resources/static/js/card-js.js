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
document.querySelectorAll('.product-card').forEach((card) => {
    card.addEventListener('mouseenter', () => {
        card.classList.add('hovered');
        document.querySelectorAll('.product-card').forEach((otherCard) => {
            if (otherCard !== card) {
                otherCard.classList.add('faded');
            }
        });
    });

    card.addEventListener('mouseleave', () => {
        card.classList.remove('hovered');
        document.querySelectorAll('.product-card').forEach((otherCard) => {
            otherCard.classList.remove('faded');
        });
    });
});
document.addEventListener('DOMContentLoaded', () => {
    let visibleCount = 12; // Số sản phẩm ban đầu hiển thị
    const step = 8; // Số sản phẩm mỗi lần nhấn 'Xem thêm'
    const allCards = document.querySelectorAll('.product-card');
    const loadMoreButton = document.querySelector('#loadMoreButton');

    // Hàm hiển thị thêm sản phẩm
    function loadMore(event) {
        const nextVisible = visibleCount + step;

        // Hiển thị thêm sản phẩm
        for (let i = visibleCount; i < nextVisible && i < allCards.length; i++) {
            allCards[i].style.display = 'block'; // Hiển thị sản phẩm
        }
        visibleCount = nextVisible;

        // Ẩn nút nếu đã hiển thị hết sản phẩm
        if (visibleCount >= allCards.length) {
            loadMoreButton.style.display = 'none'; // Ẩn nút nếu hết sản phẩm
        }
    }

    // Gán sự kiện click cho nút "Xem thêm"
    if (loadMoreButton) {
        loadMoreButton.addEventListener('click', loadMore);
    } else {
        console.error('Nút "Xem thêm" không được tìm thấy!');
    }

    // Ẩn các sản phẩm ngoài visibleCount khi tải trang
    allCards.forEach((card, index) => {
        if (index < visibleCount) {
            card.style.display = 'block'; // Hiển thị các sản phẩm ban đầu
        } else {
            card.style.display = 'none'; // Ẩn các sản phẩm ngoài visibleCount
        }
    });

    // Ẩn nút "Xem thêm" nếu số sản phẩm <= visibleCount
    if (allCards.length <= visibleCount) {
        loadMoreButton.style.display = 'none'; // Ẩn nút nếu số sản phẩm <= visibleCount
    }
});


