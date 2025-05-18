document.addEventListener('DOMContentLoaded', () => {

    // Initialize the charts
    let chartYears = null; // Initialize chartYears as null initially
    let chartYear = null;
    let chartProducts = null;
    let chartLowProducts = null;
    let chartCategories = null;

    const ctxYears = document.getElementById('myChartYears').getContext('2d');
    const ctxYear = document.getElementById('myChartYear').getContext('2d');
    const ctxProducts = document.getElementById('myChartProducts').getContext('2d');
    const ctxCategories = document.getElementById('myChartCategories').getContext('2d');
    const ctxLowProducts = document.getElementById('myLowChartProducts').getContext('2d');

    // Create charts dynamically
    function createChart(ctx, chartRef, labels, data, labelText) {
        if (chartRef) chartRef.destroy(); // Destroy previous chart if exists
        return new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: labelText,
                    data: data,
                    backgroundColor: 'rgba(75, 192, 192, 0.6)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: labelText
                    },
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Triệu VNĐ'
                        }
                    }
                }
            }
        });
    }
    let chartOrderStatus = null;
    const ctxOrderStatus = document.getElementById('myOrderStatusChart').getContext('2d');

// Hàm tạo chart trạng thái đơn hàng
        function fetchOrderStatusChart(year, month) {
            fetch(`/admin/statistic/order-status?year=${year}&month=${month}`)
                .then(res => {
                    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
                    return res.json();
                })
                .then(data => {
                    if (data.length === 0) {
                        console.log("Không có dữ liệu trạng thái đơn hàng.");
                        // Hiển thị thông báo và giữ nguyên biểu đồ nếu đã có
                        if (chartOrderStatus) {
                            chartOrderStatus.destroy();  // Hủy biểu đồ cũ
                        }
                        return; // Dừng lại nếu không có dữ liệu
                    }

                    // Gộp dữ liệu có trạng thái trùng lặp và cộng dồn số lượng
                    const aggregatedData = data.reduce((acc, item) => {
                        if (acc[item.status]) {
                            acc[item.status] += item.count;
                        } else {
                            acc[item.status] = item.count;
                        }
                        return acc;
                    }, {});

                    // Chuyển đổi dữ liệu đã gộp thành dạng phù hợp cho Chart.js
                    const labels = Object.keys(aggregatedData); // Trạng thái
                    const counts = Object.values(aggregatedData); // Tổng số lượng

                    // Kiểm tra dữ liệu sau khi gộp
                    console.log("Gộp dữ liệu: ", aggregatedData);
                    console.log("Labels:", labels);
                    console.log("Counts:", counts);

                    // Nếu biểu đồ đã tồn tại, hủy biểu đồ cũ
                    if (chartOrderStatus) chartOrderStatus.destroy();

                    // Tạo mới biểu đồ
                    chartOrderStatus = new Chart(ctxOrderStatus, {
                        type: 'pie',
                        data: {
                            labels: labels,
                            datasets: [{
                                data: counts,
                                backgroundColor: [
                                    'rgba(255, 99, 132, 0.6)',
                                    'rgba(54, 162, 235, 0.6)',
                                    'rgba(255, 206, 86, 0.6)',
                                    'rgba(75, 192, 192, 0.6)',
                                    'rgba(153, 102, 255, 0.6)'
                                ],
                                borderColor: [
                                    'rgb(231,67,102)',
                                    'rgb(32,156,232)',
                                    'rgba(255, 206, 86, 1)',
                                    'rgba(75, 192, 192, 1)',
                                    'rgba(153, 102, 255, 1)'
                                ],
                                borderWidth: 1
                            }]
                        },
                        options: {
                            responsive: true,
                            plugins: {
                                title: {
                                    display: true,
                                    text: 'Trạng thái đơn hàng'
                                },
                                legend: {
                                    position: 'bottom'
                                },
                                tooltip: {
                                    callbacks: {
                                        label: function(context) {
                                            const label = context.label || '';
                                            const value = context.dataset.data[context.dataIndex];
                                            const total = context.dataset.data.reduce((acc, num) => acc + num, 0);
                                            const percentage = Math.round((value / total) * 100);
                                            return `${label}: ${value} (${percentage}%)`;
                                        }
                                    }
                                },
                                datalabels: {
                                    formatter: (value, ctx) => {
                                        let sum = 0;
                                        let dataArr = ctx.chart.data.datasets[0].data;
                                        dataArr.map(data => {
                                            sum += data;
                                        });
                                        let percentage = (value * 100 / sum).toFixed(2) + "%";
                                        return percentage;
                                    },
                                    color: '#fff',
                                    font: {
                                        weight: 'bold'
                                    }
                                }
                            }
                        }
                    });
                })
                .catch(err => console.error('Lỗi load dữ liệu trạng thái đơn hàng:', err));
        }


    // Fetch data by year
    function fetchRevenueByYear(year) {
        fetch(`/admin/statistic/revenue/yearly?year=${year}`)
            .then(res => res.json())
            .then(data => {
                if (data) {
                    chartYear = createChart(ctxYear, chartYear, data.labels, data.data, data.label);
                } else {
                    console.error('No data for the selected year');
                }
            })
            .catch(err => console.error("Error loading yearly data:", err));
    }

    const selectedYearOnlySpan = document.getElementById('selectedYearOnly');
    const yearOptions = document.querySelectorAll('#yearOnlyDropdown ~ .dropdown-menu .year-option');
    // Event listener for year selection
    yearOptions.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const year = parseInt(item.dataset.value);
            selectedYearOnlySpan.innerText = item.innerText;
            fetchRevenueByYear(year)
            fetchOrderStatusChart(year, 0);
        });
    });

    // Fetch data for multiple years
    function fetchRevenueByYears() {
        fetch(`/admin/statistic/revenue/year`)
            .then(res => res.json())
            .then(data => {
                if (data) {
                    const { labels, data: revenueData, label } = data;
                    chartYears = createChart(ctxYears, chartYears, labels, revenueData, label);
                } else {
                    console.error('No data for multiple years');
                }
            })
            .catch(err => console.error("Error loading multiple years data:", err));
    }

    // Fetch sales by products for the selected year and month
    function fetchSalesByProducts(year, month) {
        fetch(`/admin/statistic/top-products?year=${year}&month=${month}`)
            .then(res => {
                if (!res.ok) {
                    throw new Error(`HTTP error! status: ${res.status}`);
                }
                return res.text();  // Get the raw response as text
            })
            .then(data => {
                try {
                    const jsonData = JSON.parse(data); // Attempt to parse as JSON
                    // Process the JSON data
                    const labels = jsonData.map(product => product.name);
                    const salesData = jsonData.map(product => product.sales);
                    const chartLabel = "Sản phẩm bán chạy";
                    chartProducts = createChart(ctxProducts, chartProducts, labels, salesData, chartLabel);
                } catch (e) {
                    console.error('Error parsing JSON:', e);
                    console.error('Response data:', data);  // Log raw response data
                }
            })
            .catch(err => {
                console.error("Lỗi load dữ liệu sản phẩm:", err);
            });
    }

    // DOM elements for selecting year and month
    const yearForSalesOptions = document.querySelectorAll('#yearForSalesDropdown ~ .dropdown-menu .year-option');
    const monthForSalesOptions = document.querySelectorAll('#monthForSalesDropdown ~ .dropdown-menu .month-option');
    const selectedYearForSalesSpan = document.getElementById('selectedYearForSales');
    const selectedMonthForSalesSpan = document.getElementById('selectedMonthForSales');

    // Event listener for year selection
    yearForSalesOptions.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const year = parseInt(item.dataset.value);
            selectedYearForSalesSpan.innerText = item.innerText;
            const month = parseInt(selectedMonthForSalesSpan.innerText.replace("Tháng ", ""));
            fetchSalesByProducts(year, month);
            fetchOrderStatusChart(year, month);
        });
    });

    // Event listener for month selection
    monthForSalesOptions.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const month = parseInt(item.dataset.value); // Convert to number
            selectedMonthForSalesSpan.innerText = item.innerText;
            const year = parseInt(selectedYearForSalesSpan.innerText.trim());
            fetchSalesByProducts(year, month);
            fetchOrderStatusChart(year, month);
        });
    });


    // Fetch sales by products for the selected year and month
    function fetchLowSalesByProducts(year, month) {
        fetch(`/admin/statistic/top-low-products?year=${year}&month=${month}`)
            .then(res => {
                if (!res.ok) {
                    throw new Error(`HTTP error! status: ${res.status}`);
                }
                return res.text();  // Get the raw response as text
            })
            .then(data => {
                try {
                    const jsonData = JSON.parse(data); // Attempt to parse as JSON
                    // Process the JSON data
                    const labels = jsonData.map(product => product.name);
                    const salesData = jsonData.map(product => product.sales);
                    const chartLabel = "Sản phẩm bán chạy";
                    chartLowProducts = createChart(ctxLowProducts, chartLowProducts, labels, salesData, chartLabel);
                } catch (e) {
                    console.error('Error parsing JSON:', e);
                    console.error('Response data:', data);  // Log raw response data
                }
            })
            .catch(err => {
                console.error("Lỗi load dữ liệu sản phẩm:", err);
            });
    }

    // DOM elements for selecting year and month
    const yearForLowSalesOptions = document.querySelectorAll('#yearForLowSalesDropdown ~ .dropdown-menu .year-option');
    const monthForLowSalesOptions = document.querySelectorAll('#monthForLowSalesDropdown ~ .dropdown-menu .month-option');
    const selectedYearForLowSalesSpan = document.getElementById('selectedYearForLowSales');
    const selectedMonthForLowSalesSpan = document.getElementById('selectedMonthForLowSales');

    // Event listener for year selection
    yearForLowSalesOptions.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const year = parseInt(item.dataset.value);
            selectedYearForLowSalesSpan.innerText = item.innerText;
            const month = parseInt(selectedMonthForLowSalesSpan.innerText.replace("Tháng ", ""));
            fetchLowSalesByProducts(year, month);
            fetchOrderStatusChart(year, month);
        });
    });

    // Event listener for month selection
    monthForLowSalesOptions.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const month = parseInt(item.dataset.value); // Convert to number
            selectedMonthForLowSalesSpan.innerText = item.innerText;
            const year = parseInt(selectedYearForLowSalesSpan.innerText.trim());
            fetchLowSalesByProducts(year, month);
            fetchOrderStatusChart(year, month);
        });
    });


    // Fetch sales by products for the selected year and month
    function fetchSalesByCategories(year, month) {
        fetch(`/admin/statistic/top-categories?year=${year}&month=${month}`)
            .then(res => {
                if (!res.ok) {
                    throw new Error(`HTTP error! status: ${res.status}`);
                }
                return res.text();  // Get the raw response as text
            })
            .then(data => {
                try {
                    const jsonData = JSON.parse(data); // Attempt to parse as JSON
                    // Process the JSON data
                    const labels = jsonData.map(category => category.name);
                    const salesData = jsonData.map(category => category.sales);
                    const chartLabel = "Danh mục yêu thích trong tháng";
                    chartCategories = createChart(ctxCategories, chartCategories, labels, salesData, chartLabel);
                } catch (e) {
                    console.error('Error parsing JSON:', e);
                    console.error('Response data:', data);  // Log raw response data
                }
            })
            .catch(err => {
                console.error("Lỗi load dữ liệu danh mục:", err);
            });
    }

    // DOM elements for selecting year and month
    const yearForCategoriesOptions = document.querySelectorAll('#yearForCategoriesDropdown ~ .dropdown-menu .year-option');
    const monthForCategoriesOptions = document.querySelectorAll('#monthForCategoriesDropdown ~ .dropdown-menu .month-option');
    const selectedYearForCategoriesSpan = document.getElementById('selectedYearForCategories');
    const selectedMonthForCategoriesSpan = document.getElementById('selectedMonthForCategories');

    // Event listener for year selection
    yearForCategoriesOptions.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const year = parseInt(item.dataset.value);
            selectedYearForCategoriesSpan.innerText = item.innerText;
            const month = parseInt(selectedMonthForCategoriesSpan.innerText.replace("Tháng ", ""));
            fetchSalesByCategories(year, month);
            // fetchOrderStatusChart(year, month);
        });
    });

    // Event listener for month selection
    monthForCategoriesOptions.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const month = parseInt(item.dataset.value); // Convert to number
            selectedMonthForCategoriesSpan.innerText = item.innerText;
            const year = parseInt(selectedYearForCategoriesSpan.innerText.trim());
            fetchSalesByCategories(year, month);
            // fetchOrderStatusChart(year, month);
        });
    });
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();
    const currentMonth = currentDate.getMonth() + 1; // Lưu ý: getMonth() trả về từ 0 đến 11

    fetchOrderStatusChart(currentYear, currentMonth);

    // Initialize default data fetch
    fetchRevenueByYear(2025);
    fetchRevenueByYears();
    fetchSalesByProducts(2025,5);
    fetchLowSalesByProducts(2025,5);
    fetchSalesByCategories(2025,5);

    fetch('/admin/statistic/summary')
        .then(response => response.json())
        .then(data => {
            document.getElementById('totalStock').textContent = data.totalStock;
            document.getElementById('stockValue').textContent =  new Intl.NumberFormat('vi-VN').format(data.stockValue) + ' VNĐ';
            document.getElementById('ordersToday').textContent = data.ordersToday;
            document.getElementById('ordersThisMonth').textContent = data.ordersThisMonth;
        })
        .catch(error => {
            console.error('Lỗi load dữ liệu tổng quan:', error);
        });

    // Fetch sales data when specific year/month is selected
});
