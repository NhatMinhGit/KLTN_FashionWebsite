class Slider {
    constructor(options) {
        this.sections = document.querySelectorAll(options.section);
        this.navigation = document.querySelector(options.dots);

        this.navigation.addEventListener('click', this.scrollToSection.bind(this));
        this.navigation.addEventListener('scroll', this.scrollToSection.bind(this));
        window.addEventListener('scroll', this.setDotStatus.bind(this));
        window.addEventListener('wheel', this.handleScrollWheel.bind(this)); // Lắng nghe sự kiện cuộn chuột
        this.isScrolling = false; // Biến flag để tránh cuộn liên tục
    }

    removeDotStyles() {
        const dots = this.navigation;
        const is_active = dots.querySelector('.is-active');

        if (is_active != null) {
            is_active.classList.remove('is-active');
        }
    }

    setDotStatus() {
        const scroll_position = window.scrollY;
        const dots = Array.from(this.navigation.children);

        this.sections.forEach((section, index) => {
            const half_window = window.innerHeight / 2;
            const section_top = section.offsetTop;

            if (scroll_position > section_top - half_window && scroll_position < section_top + half_window) {
                this.removeDotStyles();
                dots[index].classList.add('is-active');
            }
        })
    }

    scrollToSection(e) {
        const dots = Array.from(this.navigation.children);
        const window_height = window.innerHeight;

        dots.forEach((dot, index) => {
            if (dot == e.target) {
                let targetPosition = window_height * index;
                let currentPosition = window.scrollY;
                let step = (targetPosition - currentPosition) / 10; // Bước cuộn nhanh hơn, chia thành 10 bước thay vì 30

                // Cuộn nhanh và nhạy hơn
                let scrollInterval = setInterval(() => {
                    currentPosition += step;

                    if (Math.abs(currentPosition - targetPosition) <= Math.abs(step)) {
                        window.scrollTo({
                            top: targetPosition,
                            behavior: 'auto' // Cuộn nhanh chóng và không cần hiệu ứng mượt mà nữa
                        });
                        clearInterval(scrollInterval); // Dừng cuộn khi đã đến mục tiêu
                    } else {
                        window.scrollTo({
                            top: currentPosition,
                            behavior: 'auto' // Cuộn tự động, nhanh và nhạy
                        });
                    }
                }, 10); // Cập nhật mỗi 10ms để cuộn nhanh hơn (tăng tốc độ cuộn)
            }
        });
    }

    handleScrollWheel(e) {
        if (this.isScrolling) return; // Nếu đang cuộn thì không cho phép cuộn lại ngay lập tức
        this.isScrolling = true;

        const scroll_position = window.scrollY;
        const window_height = window.innerHeight;

        // Xác định phần tiếp theo hoặc phần trước
        let next_position;
        if (e.deltaY > 0) { // Cuộn xuống
            next_position = Math.ceil(scroll_position / window_height) * window_height + window_height;
            if (next_position < document.body.scrollHeight) { // Đảm bảo không cuộn quá phần cuối trang
                window.scrollTo({
                    top: next_position,
                    behavior: 'smooth',
                });
            }
        } else { // Cuộn lên
            next_position = Math.floor(scroll_position / window_height) * window_height - window_height;
            if (next_position >= 0) { // Đảm bảo không cuộn quá phần đầu trang
                window.scrollTo({
                    top: next_position,
                    behavior: 'smooth',
                });
            }
        }

        // Đảm bảo rằng cuộn không diễn ra quá nhanh
        setTimeout(() => {
            this.isScrolling = false;
        }, 500); // Cập nhật sau 500ms để cuộn một lần mỗi lần lăn chuột
    }
}

new Slider({
    section: '.section',
    dots: '#js-dots',
});
