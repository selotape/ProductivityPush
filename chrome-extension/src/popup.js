document.addEventListener('DOMContentLoaded', function() {
    const toggleBtn = document.getElementById('toggleBtn');
    const statusDiv = document.getElementById('status');
    const settingsBtn = document.getElementById('settingsBtn');

    // Load current state
    chrome.storage.local.get(['blockingEnabled'], function(result) {
        const isEnabled = result.blockingEnabled || false;
        updateUI(isEnabled);
    });

    toggleBtn.addEventListener('click', function() {
        chrome.storage.local.get(['blockingEnabled'], function(result) {
            const currentState = result.blockingEnabled || false;
            const newState = !currentState;

            chrome.storage.local.set({ blockingEnabled: newState }, function() {
                updateUI(newState);
                // Send message to background script
                chrome.runtime.sendMessage({
                    action: 'toggleBlocking',
                    enabled: newState
                });
            });
        });
    });

    settingsBtn.addEventListener('click', function() {
        chrome.tabs.create({ url: 'src/settings.html' });
    });

    function updateUI(isEnabled) {
        if (isEnabled) {
            statusDiv.textContent = 'Blocking: Active';
            statusDiv.className = 'status active';
            toggleBtn.textContent = 'Disable Blocking';
        } else {
            statusDiv.textContent = 'Blocking: Inactive';
            statusDiv.className = 'status inactive';
            toggleBtn.textContent = 'Enable Blocking';
        }
    }
});