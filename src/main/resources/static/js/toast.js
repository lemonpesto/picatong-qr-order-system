let toastTimer = null;

function showToast(message) {
  const toast = document.getElementById('toast');
  if (!toast) return;

  const msg = String(message ?? '').trim();
  if (!msg) return;              // 빈 메시지는 표시하지 않기

  toast.textContent = msg;
  toast.classList.add('show');

  if (toastTimer) clearTimeout(toastTimer);  // 연속 호출 시 타이머 정리
  toastTimer = setTimeout(() => {
    toast.classList.remove('show');
  }, 2000);
}
