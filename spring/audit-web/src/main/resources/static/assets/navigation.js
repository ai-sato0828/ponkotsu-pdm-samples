const button = document.querySelector('#details-button');
button.addEventListener('click', () => {
  if (window.confirm('詳細画面を開きますか？')) {
    window.location.assign(button.dataset.url);
  }
});
