
console.log("Global scripts loaded successfully");

// Hàm khởi tạo Bootstrap modals
function initBootstrapComponents() {
    // Khởi tạo tất cả modals
    var modals = document.querySelectorAll('.modal');
    modals.forEach(function(modalEl) {
        new bootstrap.Modal(modalEl);
    });
}

// Thực thi khi tài liệu đã tải xong
document.addEventListener('DOMContentLoaded', function() {
    console.log("Document ready, initializing components");
    initBootstrapComponents();
});