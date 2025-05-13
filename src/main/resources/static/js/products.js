let currentPage = 0;
let pageSize = 10; // Số sản phẩm mỗi trang

function searchProducts(page = 0) {
    let keyword = document.getElementById("searchKeywordAdmin").value;
    currentPage = page;

    fetch(`/admin/products/search?keyword=${keyword}&page=${page}&size=${pageSize}`)
        .then(response => response.json())
        .then(data => {
            let tableBody = document.querySelector("#userTable tbody");
            tableBody.innerHTML = ""; // Xóa nội dung cũ

            data.content.forEach(product => {
                let row = `<tr>
                        <td>${product.id}</td>
                        <td>${product.name}</td>
                        <td>${product.categoryName}</td>
                        <td>${product.price}</td>
                        <td>${product.brandName}</td>
                        <td>
                            <a href="/admin/products/edit/${product.id}" class="btn btn-warning btn-sm me-2">
                                <i class="bi bi-pencil-square"></i> Sửa
                            </a>
                            <a href="/admin/products/delete/${product.id}" class="btn btn-danger btn-sm" onclick="return confirm('Bạn có chắc chắn muốn xóa?')">
                                <i class="bi bi-trash"></i> Xóa
                            </a>
                        </td>
                    </tr>`;
                tableBody.innerHTML += row;
            });

            updatePagination(data);
        })
        .catch(error => console.error("Lỗi khi tìm kiếm:", error));
}



function updatePagination(data) {
    let pagination = document.querySelector(".pagination");
    pagination.innerHTML = ""; // Xóa phân trang cũ

    let prevDisabled = data.first ? "disabled" : "";
    let nextDisabled = data.last ? "disabled" : "";

    pagination.innerHTML += `<li class="page-item ${prevDisabled}">
            <a class="page-link" href="#" onclick="searchProducts(${currentPage - 1})">&laquo;</a>
        </li>`;

    for (let i = 0; i < data.totalPages; i++) {
        let activeClass = i === currentPage ? "active" : "";
        pagination.innerHTML += `<li class="page-item ${activeClass}">
                <a class="page-link" href="#" onclick="searchProducts(${i})">${i + 1}</a>
            </li>`;
    }

    pagination.innerHTML += `<li class="page-item ${nextDisabled}">
            <a class="page-link" href="#" onclick="searchProducts(${currentPage + 1})">&raquo;</a>
        </li>`;
}
