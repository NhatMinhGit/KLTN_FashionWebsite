document.addEventListener('DOMContentLoaded', () => {

    // Initialize the charts
    let chartYears = null; // Initialize chartYears as null initially
    let chartYear = null;
    let chartProducts = null;
    let chartCategories = null;

    const ctxYears = document.getElementById('myChartYears').getContext('2d');
    const ctxYear = document.getElementById('myChartYear').getContext('2d');
    const ctxProducts = document.getElementById('myChartProducts').getContext('2d');
    const ctxCategories = document.getElementById('myChartCategories').getContext('2d');

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
                    const chartLabel = "Sản phẩm bán chạy và bán chậm";
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
        });
    });
    // Initialize default data fetch
    fetchRevenueByYear(2025);
    fetchRevenueByYears();
    fetchSalesByProducts(2025,4);
    fetchSalesByCategories(2025,4);
    // Fetch sales data when specific year/month is selected
});
