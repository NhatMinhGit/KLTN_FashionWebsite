const mainPopupBtn = document.getElementById('mainPopupBtn');
const popupLinks = document.getElementById('popupLinks');

    mainPopupBtn.addEventListener('click', () => {
    if (popupLinks.style.display === 'flex') {
    popupLinks.style.display = 'none';
} else {
    popupLinks.style.display = 'flex';
}
});