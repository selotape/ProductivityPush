// ProductivityPush Settings Page JavaScript

// Default blocked sites
const DEFAULT_BLOCKED_SITES = [
    'youtube.com',
    '9gag.com',
    'facebook.com',
    'instagram.com',
    'twitter.com',
    'reddit.com',
    'tiktok.com',
    'twitch.tv',
    'netflix.com'
];

// Settings object to store all configuration
let settings = {
    blockingEnabled: false,
    blockedSites: [...DEFAULT_BLOCKED_SITES],
    customSites: [],
    dailyGoal: 3,
    motivationEnabled: true,
    redirectUrl: '',
    scheduleType: 'always',
    customSchedule: {
        startTime: '09:00',
        endTime: '17:00',
        activeDays: [1, 2, 3, 4, 5] // Mon-Fri
    }
};

// Statistics object
let stats = {
    todayBlocked: 0,
    tasksCompleted: 0,
    streakDays: 0,
    totalBlocked: 0,
    lastResetDate: new Date().toDateString()
};

// Initialize the settings page
document.addEventListener('DOMContentLoaded', function() {
    loadSettings();
    loadStatistics();
    setupEventListeners();
    updateUI();
});

// Load settings from Chrome storage
function loadSettings() {
    chrome.storage.local.get(['blockingEnabled', 'blockedSites', 'customSites', 'dailyGoal', 'motivationEnabled', 'redirectUrl', 'scheduleType', 'customSchedule'], function(result) {
        if (result.blockingEnabled !== undefined) settings.blockingEnabled = result.blockingEnabled;
        if (result.blockedSites) settings.blockedSites = result.blockedSites;
        if (result.customSites) settings.customSites = result.customSites;
        if (result.dailyGoal) settings.dailyGoal = result.dailyGoal;
        if (result.motivationEnabled !== undefined) settings.motivationEnabled = result.motivationEnabled;
        if (result.redirectUrl) settings.redirectUrl = result.redirectUrl;
        if (result.scheduleType) settings.scheduleType = result.scheduleType;
        if (result.customSchedule) settings.customSchedule = result.customSchedule;

        updateUI();
    });
}

// Load statistics from Chrome storage
function loadStatistics() {
    chrome.storage.local.get(['todayBlocked', 'tasksCompleted', 'streakDays', 'totalBlocked', 'lastResetDate'], function(result) {
        const today = new Date().toDateString();

        // Reset daily stats if it's a new day
        if (result.lastResetDate !== today) {
            stats.todayBlocked = 0;
            stats.lastResetDate = today;
            chrome.storage.local.set({ todayBlocked: 0, lastResetDate: today });
        } else {
            if (result.todayBlocked) stats.todayBlocked = result.todayBlocked;
        }

        if (result.tasksCompleted) stats.tasksCompleted = result.tasksCompleted;
        if (result.streakDays) stats.streakDays = result.streakDays;
        if (result.totalBlocked) stats.totalBlocked = result.totalBlocked;

        updateStatisticsUI();
    });
}

// Setup event listeners
function setupEventListeners() {
    // Toggle blocking
    document.getElementById('toggleBtn').addEventListener('click', toggleBlocking);

    // Add site functionality
    document.getElementById('addSiteBtn').addEventListener('click', addCustomSite);
    document.getElementById('newSiteInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') addCustomSite();
    });

    // Daily goal slider
    const dailyGoalSlider = document.getElementById('dailyGoalSlider');
    dailyGoalSlider.addEventListener('input', function() {
        settings.dailyGoal = parseInt(this.value);
        document.getElementById('dailyGoalValue').textContent = `${this.value} tasks`;
    });

    // Motivation toggle
    document.getElementById('motivationToggle').addEventListener('change', function() {
        settings.motivationEnabled = this.checked;
    });

    // Redirect select
    const redirectSelect = document.getElementById('redirectSelect');
    const customRedirectInput = document.getElementById('customRedirectInput');

    redirectSelect.addEventListener('change', function() {
        const isCustom = this.value === 'custom';
        customRedirectInput.style.display = isCustom ? 'block' : 'none';

        if (!isCustom) {
            settings.redirectUrl = this.value;
        }
    });

    customRedirectInput.addEventListener('input', function() {
        if (redirectSelect.value === 'custom') {
            settings.redirectUrl = this.value;
        }
    });

    // Schedule type radio buttons
    document.querySelectorAll('input[name="scheduleType"]').forEach(radio => {
        radio.addEventListener('change', function() {
            settings.scheduleType = this.value;
            const customDetails = document.getElementById('customScheduleDetails');
            customDetails.style.display = this.value === 'custom' ? 'block' : 'none';
        });
    });

    // Custom schedule inputs
    document.getElementById('startTime').addEventListener('change', function() {
        settings.customSchedule.startTime = this.value;
    });

    document.getElementById('endTime').addEventListener('change', function() {
        settings.customSchedule.endTime = this.value;
    });

    // Day checkboxes
    document.querySelectorAll('.day-checkbox input').forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const day = parseInt(this.value);
            if (this.checked) {
                if (!settings.customSchedule.activeDays.includes(day)) {
                    settings.customSchedule.activeDays.push(day);
                }
            } else {
                settings.customSchedule.activeDays = settings.customSchedule.activeDays.filter(d => d !== day);
            }
        });
    });

    // Action buttons
    document.getElementById('saveBtn').addEventListener('click', saveSettings);
    document.getElementById('resetBtn').addEventListener('click', resetToDefaults);
    document.getElementById('resetStatsBtn').addEventListener('click', resetStatistics);
    document.getElementById('exportDataBtn').addEventListener('click', exportData);
}

// Update UI with current settings
function updateUI() {
    // Update blocking status
    updateBlockingStatus();

    // Update blocked sites lists
    updateSitesLists();

    // Update daily goal slider
    const slider = document.getElementById('dailyGoalSlider');
    slider.value = settings.dailyGoal;
    document.getElementById('dailyGoalValue').textContent = `${settings.dailyGoal} tasks`;

    // Update motivation toggle
    document.getElementById('motivationToggle').checked = settings.motivationEnabled;

    // Update redirect select
    const redirectSelect = document.getElementById('redirectSelect');
    const customInput = document.getElementById('customRedirectInput');

    if (settings.redirectUrl && !redirectSelect.querySelector(`option[value="${settings.redirectUrl}"]`)) {
        redirectSelect.value = 'custom';
        customInput.style.display = 'block';
        customInput.value = settings.redirectUrl;
    } else {
        redirectSelect.value = settings.redirectUrl;
    }

    // Update schedule type
    document.getElementById(`schedule${settings.scheduleType.charAt(0).toUpperCase() + settings.scheduleType.slice(1)}`).checked = true;
    document.getElementById('customScheduleDetails').style.display = settings.scheduleType === 'custom' ? 'block' : 'none';

    // Update custom schedule
    document.getElementById('startTime').value = settings.customSchedule.startTime;
    document.getElementById('endTime').value = settings.customSchedule.endTime;

    // Update day checkboxes
    document.querySelectorAll('.day-checkbox input').forEach(checkbox => {
        const day = parseInt(checkbox.value);
        checkbox.checked = settings.customSchedule.activeDays.includes(day);
    });
}

// Update blocking status display
function updateBlockingStatus() {
    const indicator = document.getElementById('statusIndicator');
    const title = document.getElementById('statusTitle');
    const subtitle = document.getElementById('statusSubtitle');
    const toggleBtn = document.getElementById('toggleBtn');

    if (settings.blockingEnabled) {
        indicator.textContent = '🔴';
        title.textContent = 'Blocking Active';
        subtitle.textContent = 'Websites are currently being blocked';
        toggleBtn.textContent = 'Disable Blocking';
        toggleBtn.className = 'toggle-btn active';
    } else {
        indicator.textContent = '⚪';
        title.textContent = 'Blocking Inactive';
        subtitle.textContent = 'Click to enable website blocking';
        toggleBtn.textContent = 'Enable Blocking';
        toggleBtn.className = 'toggle-btn';
    }
}

// Update sites lists
function updateSitesLists() {
    // Update default sites list
    const defaultList = document.getElementById('defaultSitesList');
    defaultList.innerHTML = '';

    DEFAULT_BLOCKED_SITES.forEach(site => {
        const siteElement = createSiteElement(site, false);
        defaultList.appendChild(siteElement);
    });

    // Update custom sites list
    const customList = document.getElementById('customSitesList');
    const emptyState = document.getElementById('customSitesEmpty');

    customList.innerHTML = '';

    if (settings.customSites.length === 0) {
        emptyState.style.display = 'block';
    } else {
        emptyState.style.display = 'none';
        settings.customSites.forEach(site => {
            const siteElement = createSiteElement(site, true);
            customList.appendChild(siteElement);
        });
    }
}

// Create a site element
function createSiteElement(site, isDeletable) {
    const div = document.createElement('div');
    div.className = 'site-item';

    const icon = document.createElement('span');
    icon.className = 'site-icon';
    icon.textContent = '🌐';

    const name = document.createElement('span');
    name.className = 'site-name';
    name.textContent = site;

    div.appendChild(icon);
    div.appendChild(name);

    if (isDeletable) {
        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'delete-site-btn';
        deleteBtn.textContent = '×';
        deleteBtn.title = 'Remove site';
        deleteBtn.addEventListener('click', () => removeCustomSite(site));
        div.appendChild(deleteBtn);
    }

    return div;
}

// Toggle blocking on/off
function toggleBlocking() {
    settings.blockingEnabled = !settings.blockingEnabled;
    updateBlockingStatus();

    // Send message to background script
    chrome.runtime.sendMessage({
        action: 'toggleBlocking',
        enabled: settings.blockingEnabled
    });
}

// Add custom site
function addCustomSite() {
    const input = document.getElementById('newSiteInput');
    let site = input.value.trim().toLowerCase();

    if (!site) return;

    // Clean up the site input
    site = site.replace(/^https?:\/\//, '').replace(/^www\./, '').replace(/\/.*$/, '');

    // Validate site format
    if (!isValidDomain(site)) {
        showStatus('Please enter a valid domain name', 'error');
        return;
    }

    // Check if site already exists
    if (DEFAULT_BLOCKED_SITES.includes(site) || settings.customSites.includes(site)) {
        showStatus('Site already in blocked list', 'warning');
        return;
    }

    // Add site
    settings.customSites.push(site);
    settings.blockedSites.push(site);

    // Clear input and update UI
    input.value = '';
    updateSitesLists();
    showStatus('Site added successfully', 'success');
}

// Remove custom site
function removeCustomSite(site) {
    settings.customSites = settings.customSites.filter(s => s !== site);
    settings.blockedSites = settings.blockedSites.filter(s => s !== site);
    updateSitesLists();
    showStatus('Site removed successfully', 'success');
}

// Validate domain format
function isValidDomain(domain) {
    const domainRegex = /^[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9]?\.[a-zA-Z]{2,}$/;
    return domainRegex.test(domain);
}

// Save settings
function saveSettings() {
    // Combine default and custom sites
    settings.blockedSites = [...DEFAULT_BLOCKED_SITES, ...settings.customSites];

    // Save to Chrome storage
    chrome.storage.local.set(settings, function() {
        showStatus('Settings saved successfully!', 'success');

        // Notify background script of settings change
        chrome.runtime.sendMessage({
            action: 'settingsUpdated',
            settings: settings
        });
    });
}

// Reset to defaults
function resetToDefaults() {
    if (confirm('Are you sure you want to reset all settings to defaults? This cannot be undone.')) {
        settings = {
            blockingEnabled: false,
            blockedSites: [...DEFAULT_BLOCKED_SITES],
            customSites: [],
            dailyGoal: 3,
            motivationEnabled: true,
            redirectUrl: '',
            scheduleType: 'always',
            customSchedule: {
                startTime: '09:00',
                endTime: '17:00',
                activeDays: [1, 2, 3, 4, 5]
            }
        };

        updateUI();
        saveSettings();
    }
}

// Reset statistics
function resetStatistics() {
    if (confirm('Are you sure you want to reset all statistics? This cannot be undone.')) {
        stats = {
            todayBlocked: 0,
            tasksCompleted: 0,
            streakDays: 0,
            totalBlocked: 0,
            lastResetDate: new Date().toDateString()
        };

        chrome.storage.local.set(stats);
        updateStatisticsUI();
        showStatus('Statistics reset successfully', 'success');
    }
}

// Update statistics UI
function updateStatisticsUI() {
    document.getElementById('todayBlocked').textContent = stats.todayBlocked;
    document.getElementById('tasksCompleted').textContent = stats.tasksCompleted;
    document.getElementById('streakDays').textContent = stats.streakDays;
    document.getElementById('totalBlocked').textContent = stats.totalBlocked;
}

// Export data
function exportData() {
    const exportData = {
        settings: settings,
        statistics: stats,
        exportDate: new Date().toISOString()
    };

    const dataStr = JSON.stringify(exportData, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });

    const link = document.createElement('a');
    link.href = URL.createObjectURL(dataBlob);
    link.download = `productivitypush-data-${new Date().toISOString().split('T')[0]}.json`;
    link.click();

    showStatus('Data exported successfully', 'success');
}

// Show status message
function showStatus(message, type = 'info') {
    const statusElement = document.getElementById('saveStatus');
    statusElement.textContent = message;
    statusElement.className = `save-status ${type}`;
    statusElement.style.display = 'block';

    setTimeout(() => {
        statusElement.style.display = 'none';
    }, 3000);
}