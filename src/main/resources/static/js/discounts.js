let currentDiscountPage = 0;
let discountPageSize = 10;

function searchDiscounts(page = 0) {
    let keyword = document.getElementById("searchKeywordDiscount").value;
    currentDiscountPage = page;

    fetch(`/admin/discounts/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${discountPageSize}`)
        .then(response => {
            if (!response.ok) throw new Error("Server response was not OK");
            return response.json();
        })
        .then(data => {
            let tableBody = document.querySelector("#discountTable tbody");
            tableBody.innerHTML = "";

            if (Array.isArray(data.content) && data.content.length > 0) {
                data.content.forEach(discount => {
                    let appliedTo = "";
                    if (discount.productName) {
                        appliedTo = `Sản phẩm: ${discount.productName}`;
                    } else if (discount.categoryName) {
                        appliedTo = `Danh mục: ${discount.categoryName}`;
                    } else {
                        appliedTo = "Không xác định";
                    }

                    let row = `
                        <tr>
                            <td>${discount.id}</td>
                            <td>${discount.name}</td>
                            <td>${discount.discountPercent}%</td>
                            <td>${appliedTo}</td>
                            <td>${new Date(discount.startTime).toLocaleDateString()}</td>
                            <td>${new Date(discount.endTime).toLocaleDateString()}</td>
                            <td>
                                <a href="/admin/discounts/edit/${discount.id}" class="btn btn-warning btn-sm me-2">
                                    <i class="bi bi-pencil-square"></i> Sửa
                                </a>
                                <a href="/admin/discounts/delete/${discount.id}" class="btn btn-danger btn-sm" onclick="return confirm('Bạn có chắc chắn muốn xóa giảm giá này?')">
                                    <i class="bi bi-trash"></i> Xóa
                                </a>
                            </td>
                        </tr>
                    `;
                    tableBody.innerHTML += row;
                });
            } else {
                // Chỉ gán, không dùng += để không bị ghi đè sai
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="7" class="text-center">Không tìm thấy khuyến mãi nào.</td>
                    </tr>
                `;
            }

            // Truyền cả currentPage để phân trang xử lý chính xác
            updateDiscountPagination(data, currentDiscountPage);
        })
        .catch(error => {
            console.error("Lỗi khi tìm kiếm khuyến mãi:", error);
            alert("Đã có lỗi xảy ra khi tìm kiếm khuyến mãi.");
        });
}
function updateDiscountPagination(data, currentPage) {
    let pagination = document.querySelector(".pagination");
    if (!pagination) {
        console.error("Không tìm thấy phần tử phân trang.");
        return;
    }

    pagination.innerHTML = ""; // Xóa nội dung phân trang cũ

    let prevDisabled = data.first ? "disabled" : "";
    let nextDisabled = data.last ? "disabled" : "";

    // Thêm nút "Prev"
    pagination.innerHTML += `<li class="page-item ${prevDisabled}">
        <a class="page-link" href="#" onclick="searchDiscounts(${currentPage - 1})">&laquo;</a>
    </li>`;

    // Thêm các trang
    for (let i = 0; i < data.totalPages; i++) {
        let activeClass = i === currentPage ? "active" : "";
        pagination.innerHTML += `<li class="page-item ${activeClass}">
            <a class="page-link" href="#" onclick="searchDiscounts(${i})">${i + 1}</a>
        </li>`;
    }

    // Thêm nút "Next"
    pagination.innerHTML += `<li class="page-item ${nextDisabled}">
        <a class="page-link" href="#" onclick="searchDiscounts(${currentPage + 1})">&raquo;</a>
    </li>`;
}

// function updateDiscountPagination(data, currentPage) {
//     let pagination = document.querySelector(".discount-pagination");
//     pagination.innerHTML = "";
//
//     let prevDisabled = data.first ? "disabled" : "";
//     let nextDisabled = data.last ? "disabled" : "";
//
//     pagination.innerHTML += `<li class="page-item ${prevDisabled}">
//         <a class="page-link" href="#" onclick="searchDiscounts(${currentPage - 1})">&laquo;</a>
//     </li>`;
//
//     for (let i = 0; i < data.totalPages; i++) {
//         let activeClass = i === currentPage ? "active" : "";
//         pagination.innerHTML += `<li class="page-item ${activeClass}">
//             <a class="page-link" href="#" onclick="searchDiscounts(${i})">${i + 1}</a>
//         </li>`;
//     }
//
//     pagination.innerHTML += `<li class="page-item ${nextDisabled}">
//         <a class="page-link" href="#" onclick="searchDiscounts(${currentPage + 1})">&raquo;</a>
//     </li>`;
// }

// Function to escape HTML content
// function escapeHtml(str) {
//     return str.replace(/[&<>"']/g, function (match) {
//         const escapeMap = {
//             '&': '&amp;',
//             '<': '&lt;',
//             '>': '&gt;',
//             '"': '&quot;',
//             "'": '&#039;'
//         };
//         return escapeMap[match];
//     });
// }

function validateDates() {
    const start = new Date(document.getElementById("startDate").value);
    const end = new Date(document.getElementById("endDate").value);

    if (start >= end) {
        alert("Ngày bắt đầu phải trước ngày kết thúc!");
        return false; // Ngăn form submit
    }
    return true; // Cho phép submit
}


// function toggleApplyTarget() {
//     const type = document.getElementById("applyType").value;
//     document.getElementById("productSelect").style.display = (type === "product") ? "block" : "none";
//     document.getElementById("categorySelect").style.display = (type === "category") ? "block" : "none";
// }
//
// function clearDiscountFields() {
//     document.querySelector('input[name="name"]').value = "";
//     document.querySelector('input[name="discountPercent"]').value = "";
//     document.querySelector('input[name="startDate"]').value = "";
//     document.querySelector('input[name="endDate"]').value = "";
//     document.querySelector('select[name="applyType"]').value = "";
//     // Ẩn select sản phẩm và danh mục
//     document.getElementById("productSelect").style.display = "none";
//     document.getElementById("categorySelect").style.display = "none";
// }
