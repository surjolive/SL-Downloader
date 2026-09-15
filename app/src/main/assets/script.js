const input = document.getElementById('urlInput');
const message = document.getElementById('message');
const downloads = document.getElementById('downloads');
const downloadButton = document.getElementById('downloadButton');
const themeButton = document.getElementById('themeButton');
const menuButton = document.getElementById('menuButton');
const quickMenu = document.getElementById('quickMenu');
let items = JSON.parse(localStorage.getItem('sl-downloader-queue') || '[]');
if (localStorage.getItem('sl-downloader-theme') === 'light') document.body.classList.add('light');
const settings = JSON.parse(localStorage.getItem('sl-downloader-settings') || '{"remember":true,"reducedMotion":false}');
document.getElementById('rememberToggle').checked = settings.remember;
document.getElementById('motionToggle').checked = settings.reducedMotion;
if (settings.reducedMotion) document.body.classList.add('reduced-motion');

function native() { return window.SLAndroid || null; }
function validDirectUrl(value) {
  try {
    const url = new URL(value.trim());
    const path = url.pathname.toLowerCase();
    const direct = ['.mp4', '.webm', '.mov', '.m4v', '.ogv'].some(ext => path.endsWith(ext));
    const blocked = /(^|\.)youtube\.com$|(^|\.)youtu\.be$|(^|\.)vimeo\.com$/.test(url.hostname);
    return (url.protocol === 'http:' || url.protocol === 'https:') && direct && !blocked;
  } catch (_) { return false; }
}
function validPlatformUrl(value) {
  try {
    const host = new URL(value.trim()).hostname.toLowerCase().replace(/^www\./, '');
    return ['youtube.com', 'youtu.be', 'facebook.com', 'instagram.com', 'tiktok.com', 'vimeo.com']
      .some(platform => host === platform || host.endsWith(`.${platform}`));
  } catch (_) { return false; }
}
function platformName(value) {
  try {
    const host = new URL(value.trim()).hostname.toLowerCase().replace(/^www\./, '');
    if (host === 'youtube.com' || host === 'youtu.be' || host.endsWith('.youtube.com')) return 'YouTube';
    if (host === 'facebook.com' || host.endsWith('.facebook.com')) return 'Facebook';
    if (host === 'instagram.com' || host.endsWith('.instagram.com')) return 'Instagram';
    if (host === 'tiktok.com' || host.endsWith('.tiktok.com')) return 'TikTok';
    if (host === 'vimeo.com' || host.endsWith('.vimeo.com')) return 'Vimeo';
    return 'this platform';
  } catch (_) { return 'this platform'; }
}
function showMessage(text, error = true) {
  message.textContent = text;
  message.style.color = error ? 'var(--danger)' : 'var(--brand)';
}
function render() {
  const active = items.filter(item => item.status === 'queued').length;
  document.getElementById('activeCount').textContent = active;
  document.getElementById('completedCount').textContent = items.filter(item => item.status === 'completed').length;
  document.getElementById('queueBadge').textContent = items.filter(item => item.status === 'queued').length;
  const markup = items.map((item, index) => `<article class="download-item"><div class="file-icon">▾</div><div class="file-info"><strong>${escapeHtml(item.name)}</strong><span>${item.status === 'queued' ? 'Queued for download' : 'Ready to download'}</span></div><b class="file-status">${item.status === 'queued' ? 'QUEUED' : 'DONE'}</b><button class="row-action" data-remove="${index}" aria-label="Remove">×</button></article>`).join('');
  downloads.innerHTML = markup || emptyMarkup('Your library is quiet', 'Downloaded files will appear here.');
  document.getElementById('queueList').innerHTML = markup || emptyMarkup('Your queue is empty', 'New downloads will appear here.');
  document.getElementById('historyList').innerHTML = items.length ? markup : emptyMarkup('No history yet', 'Completed downloads will be kept here.');
  document.querySelectorAll('[data-remove]').forEach(button => button.onclick = () => removeItem(Number(button.dataset.remove)));
}
function persist() { localStorage.setItem('sl-downloader-queue', JSON.stringify(items.slice(0, 20))); }
function emptyMarkup(title, subtitle) { return `<div class="empty-state"><div class="empty-icon">↓</div><strong>${title}</strong><span>${subtitle}</span></div>`; }
function removeItem(index) { items.splice(index, 1); persist(); render(); showMessage('Removed from local queue.', false); }
function escapeHtml(value) { return value.replace(/[&<>'"]/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[char])); }
function submit() {
  const value = input.value.trim();
  if (!validDirectUrl(value) && !validPlatformUrl(value)) {
    showMessage(`This looks like a ${platformName(value)} page URL. Paste an authorized direct media file or supported public platform link.`);
    input.focus();
    return;
  }
  const name = value.split('/').pop().split('?')[0] || 'video file';
  showMessage('Added to your download queue.', false);
  items.unshift({ name, status: 'queued' });
  persist();
  render();
  if (native()) native().download(value);
  downloadButton.classList.add('sent');
  setTimeout(() => downloadButton.classList.remove('sent'), 900);
}
document.getElementById('pasteButton').onclick = () => {
  const value = native() ? native().paste() : '';
  if (value) { input.value = value; showMessage('URL pasted. Ready to analyze.', false); }
  else showMessage('Clipboard is empty.');
};
document.getElementById('copyButton').onclick = () => {
  const value = input.value.trim();
  if (!value) return showMessage('Enter a URL to copy.');
  if (native()) native().copy(value); else navigator.clipboard?.writeText(value);
  showMessage('URL copied to clipboard.', false);
};
document.getElementById('shareButton').onclick = () => {
  const value = input.value.trim();
  if (!value) return showMessage('Enter a URL to share.');
  if (native()) native().share(value);
  else if (navigator.share) navigator.share({ title: 'SL Downloader', text: value });
  else navigator.clipboard?.writeText(value);
  showMessage('Share sheet opened.', false);
};
document.getElementById('clearButton').onclick = () => { input.value = ''; showMessage(''); input.focus(); };
document.getElementById('clearQueueButton').onclick = () => { items = items.filter(item => item.status !== 'queued'); persist(); render(); showMessage('Queued items cleared.', false); };
document.getElementById('clearHistoryButton').onclick = () => { items = []; persist(); render(); showMessage('Local history cleared.', false); };
downloadButton.onclick = submit;
input.addEventListener('keydown', event => { if (event.key === 'Enter') submit(); });
themeButton.onclick = () => {
  document.body.classList.toggle('light');
  localStorage.setItem('sl-downloader-theme', document.body.classList.contains('light') ? 'light' : 'dark');
};
menuButton.onclick = event => { event.stopPropagation(); quickMenu.classList.toggle('hidden'); };
document.addEventListener('click', event => {
  if (!quickMenu.contains(event.target) && event.target !== menuButton) quickMenu.classList.add('hidden');
});
document.querySelectorAll('[data-menu]').forEach(action => action.onclick = () => {
  quickMenu.classList.add('hidden');
  const type = action.dataset.menu;
  if (type === 'paste') document.getElementById('pasteButton').click();
  if (type === 'copy') document.getElementById('copyButton').click();
  if (type === 'share') document.getElementById('shareButton').click();
  if (type === 'theme') themeButton.click();
  if (type === 'clear') document.getElementById('clearQueueButton').click();
  if (type === 'github') document.getElementById('githubButton').click();
  if (type === 'privacy') document.getElementById('privacyButton').click();
});
document.getElementById('githubButton').onclick = () => native() ? native().openGithub() : window.open('https://github.com/surjolive', '_blank');
document.getElementById('reposButton').onclick = () => native() ? native().openRepositories() : window.open('https://github.com/surjolive?tab=repositories', '_blank');
document.getElementById('githubSettings').onclick = () => native() ? native().openGithub() : window.open('https://github.com/surjolive', '_blank');
document.getElementById('privacyButton').onclick = () => showMessage('Use only authorized direct media URLs. No cookies, tokens, or private content are collected.', false);
document.getElementById('rememberToggle').onchange = event => { settings.remember = event.target.checked; localStorage.setItem('sl-downloader-settings', JSON.stringify(settings)); if (!settings.remember) { items = []; persist(); render(); } };
document.getElementById('motionToggle').onchange = event => { settings.reducedMotion = event.target.checked; document.body.classList.toggle('reduced-motion', settings.reducedMotion); localStorage.setItem('sl-downloader-settings', JSON.stringify(settings)); };
document.querySelectorAll('.tab').forEach(tab => tab.onclick = () => {
  document.querySelectorAll('.tab').forEach(item => item.classList.toggle('active', item === tab));
  document.querySelectorAll('.view').forEach(view => view.classList.toggle('hidden', view.id !== `${tab.dataset.view}View` && !(tab.dataset.view === 'home' && view.id === 'homeView')));
  if (tab.dataset.view === 'home') document.getElementById('homeView').classList.remove('hidden');
});
document.getElementById('queueSearch').oninput = event => filterQueue(event.target.value);
document.getElementById('queueFilter').onchange = event => filterQueue(document.getElementById('queueSearch').value, event.target.value);
function filterQueue(query = '', status = document.getElementById('queueFilter').value) {
  const matches = items.filter(item => item.name.toLowerCase().includes(query.toLowerCase()) && (status === 'all' || (status === 'done' ? item.status === 'completed' : item.status === 'queued')));
  document.getElementById('queueList').innerHTML = matches.length ? matches.map(item => `<article class="download-item"><div class="file-icon">▾</div><div class="file-info"><strong>${escapeHtml(item.name)}</strong><span>${item.status}</span></div><b class="file-status">${item.status.toUpperCase()}</b></article>`).join('') : emptyMarkup('Nothing matches', 'Try another search or filter.');
}
window.setSharedUrl = value => { input.value = value || ''; if (value) showMessage('Shared URL received.'); };
render();
