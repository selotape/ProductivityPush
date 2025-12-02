// Default blocked websites
const defaultBlockedSites = [
    'youtube.com',
    '9gag.com',
    'facebook.com',
    'instagram.com',
    'twitter.com',
    'reddit.com',
    'tiktok.com'
];

// Initialize storage on install
chrome.runtime.onInstalled.addListener(() => {
    chrome.storage.local.set({
        blockingEnabled: false,
        blockedSites: defaultBlockedSites,
        dailyTasks: 0,
        lastTaskDate: new Date().toDateString()
    });
});

// Listen for messages from popup
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === 'toggleBlocking') {
        updateBlockingRules(request.enabled);
    }
});

// Check if site should be blocked
function shouldBlockSite(url) {
    return new Promise((resolve) => {
        chrome.storage.local.get(['blockingEnabled', 'blockedSites'], (result) => {
            if (!result.blockingEnabled) {
                resolve(false);
                return;
            }

            const blockedSites = result.blockedSites || defaultBlockedSites;
            const isBlocked = blockedSites.some(site => url.includes(site));
            resolve(isBlocked);
        });
    });
}

// Update declarative net request rules
function updateBlockingRules(enabled) {
    chrome.storage.local.get(['blockedSites'], (result) => {
        const blockedSites = result.blockedSites || defaultBlockedSites;

        if (enabled) {
            // Create rules for each blocked site
            const rules = blockedSites.map((site, index) => ({
                id: index + 1,
                priority: 1,
                action: { type: 'redirect', redirect: { url: chrome.runtime.getURL('src/blocked.html') } },
                condition: { urlFilter: `*://*.${site}/*`, resourceTypes: ['main_frame'] }
            }));

            chrome.declarativeNetRequest.updateDynamicRules({
                addRules: rules,
                removeRuleIds: rules.map(rule => rule.id)
            });
        } else {
            // Remove all blocking rules
            chrome.declarativeNetRequest.getDynamicRules((rules) => {
                const ruleIds = rules.map(rule => rule.id);
                chrome.declarativeNetRequest.updateDynamicRules({
                    removeRuleIds: ruleIds
                });
            });
        }
    });
}

// Daily task tracking
chrome.storage.local.get(['lastTaskDate'], (result) => {
    const today = new Date().toDateString();
    if (result.lastTaskDate !== today) {
        chrome.storage.local.set({
            dailyTasks: 0,
            lastTaskDate: today
        });
    }
});