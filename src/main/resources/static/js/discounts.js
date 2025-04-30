function validateDates() {
    const start = new Date(document.getElementById("startDate").value);
    const end = new Date(document.getElementById("endDate").value);

    if (start >= end) {
        alert("Ngày bắt đầu phải trước ngày kết thúc!");
        return false; // Ngăn form submit
    }
    return true; // Cho phép submit
}

function toggleApplyTarget() {
    const type = document.getElementById("applyType").value;
    document.getElementById("productSelect").style.display = (type === "product") ? "block" : "none";
    document.getElementById("categorySelect").style.display = (type === "category") ? "block" : "none";
}

function clearDiscountFields() {
    document.querySelector('input[name="name"]').value = "";
    document.querySelector('input[name="discountPercent"]').value = "";
    document.querySelector('input[name="startDate"]').value = "";
    document.querySelector('input[name="endDate"]').value = "";
    document.querySelector('select[name="applyType"]').value = "";
    // Ẩn select sản phẩm và danh mục
    document.getElementById("productSelect").style.display = "none";
    document.getElementById("categorySelect").style.display = "none";
}
